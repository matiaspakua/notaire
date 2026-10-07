#!/usr/bin/env python3
"""
Guards issue #1179 (CU77): infra/ is a single, self-contained, documented folder
that can be split into its own repository.

Plain stdlib unittest + PyYAML, consistent with infra/tests/test_prod_compose.py.
Run with: python3 infra/tests/test_infra_standalone.py
"""
import os
import re
import shutil
import subprocess
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
INFRA = REPO_ROOT / "infra"
NGINX_CONF = INFRA / "deploy" / "kustomize" / "base" / "nginx.conf"
KUSTOMIZE_BASE = INFRA / "deploy" / "kustomize" / "base"
PROD_COMPOSE = REPO_ROOT / "docker-compose.prod.yml"
OBSERVABILITY_COMPOSE = INFRA / "observability" / "docker-compose.yml"

REQUIRED_PATHS = (
    "observability/docker-compose.yml",
    "observability/prometheus",
    "observability/grafana",
    "observability/loki",
    "observability/dashboard",
    "deploy/kustomize/base",
    "deploy/kustomize/overlays/staging",
    "performance/k6/load-test.js",
    "scripts/start-infra.sh",
    "scripts/check-infra.sh",
    ".env.example",
    "README.md",
    "docs/PREPARATION.md",
    "docs/CONFIGURATION.md",
    "docs/DEFINITION.md",
    "docs/OPERATION.md",
)

LEGACY_PATHS = (
    "deploy",
    "performance-test",
    "infra/docker-compose.yml",
    "infra/prometheus",
    "infra/grafana",
    "infra/loki",
    "infra/dashboard",
)

LEGACY_REFERENCE = re.compile(
    r"(?<![\w/.-])deploy/(?:kustomize|nginx)"
    r"|(?<![\w/.-])performance-test/"
    r"|infra/(?:docker-compose\.yml|prometheus|grafana|loki|dashboard)\b"
)
REFERENCE_EXEMPT_PREFIXES = (
    "docs/000-archive/",
    "docs/openspec/",
    "backend-api/src/main/resources/db/migration/",
    "CHANGELOG.md",
    "infra/tests/test_infra_standalone.py",
)

ESCAPE_CANDIDATE = re.compile(r"(?:\.\./)+[\w.-]*")
ESCAPE_EXEMPTION_MARKER = "INFRA_ROOT_ENV_FALLBACK"
SCANNED_SUFFIXES = {".yml", ".yaml", ".sh"}

COMPOSE_VARIABLE = re.compile(r"\$\{([A-Z][A-Z0-9_]*)")
REAL_SONAR_TOKEN = re.compile(r"^SONAR_TOKEN=\S*sq[pua]_", re.MULTILINE)


def tracked_files():
    out = subprocess.run(
        ["git", "ls-files", "-z"], cwd=REPO_ROOT, capture_output=True, check=True
    ).stdout.decode("utf-8")
    return [Path(p) for p in out.split("\0") if p and (REPO_ROOT / p).exists()]


def infra_files(suffixes):
    return [
        p for p in INFRA.rglob("*")
        if p.is_file() and p.suffix in suffixes and "node_modules" not in p.parts
    ]


class InfraLayoutTest(unittest.TestCase):
    def test_all_infra_assets_live_under_infra(self):
        missing = [p for p in REQUIRED_PATHS if not (INFRA / p).exists()]
        self.assertEqual([], missing, f"missing under infra/: {missing}")

    def test_legacy_locations_no_longer_exist(self):
        present = [p for p in LEGACY_PATHS if (REPO_ROOT / p).exists()]
        self.assertEqual([], present, f"legacy locations still present: {present}")

    def test_stale_e2e_suite_removed(self):
        specs = list((INFRA / "tests").glob("**/*.spec.ts"))
        self.assertEqual([], specs, "infra/ holds guards only; Playwright specs live in testing/e2e (#1179)")


class InfraSelfContainmentTest(unittest.TestCase):
    def test_infra_does_not_reference_paths_outside_itself(self):
        escapes = []
        for path in infra_files(SCANNED_SUFFIXES):
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                if ESCAPE_EXEMPTION_MARKER in line:
                    continue
                for match in ESCAPE_CANDIDATE.finditer(line):
                    resolved = Path(os.path.normpath(path.parent / match.group(0)))
                    if INFRA not in (resolved, *resolved.parents):
                        escapes.append(f"{path.relative_to(REPO_ROOT)}:{number}: {match.group(0)}")
        self.assertEqual([], escapes, f"infra/ references outside itself: {escapes}")

    def test_infra_scripts_do_not_assume_repo_root(self):
        offenders = [
            str(p.relative_to(REPO_ROOT))
            for p in (INFRA / "scripts").glob("*.sh")
            if "REPO_DIR" in p.read_text(encoding="utf-8")
        ]
        self.assertEqual([], offenders, f"scripts still resolve REPO_DIR: {offenders}")

    def test_env_example_covers_every_variable_the_stack_uses(self):
        example = (INFRA / ".env.example").read_text(encoding="utf-8")
        declared = set(re.findall(r"^([A-Z][A-Z0-9_]*)=", example, re.MULTILINE))
        used = set(COMPOSE_VARIABLE.findall(OBSERVABILITY_COMPOSE.read_text(encoding="utf-8")))
        self.assertEqual([], sorted(used - declared), "infra/.env.example is missing variables")

    def test_env_example_contains_no_real_tokens(self):
        example = (INFRA / ".env.example").read_text(encoding="utf-8")
        self.assertIsNone(REAL_SONAR_TOKEN.search(example), "SONAR_TOKEN must not be committed")


class ObservabilityComposeProjectTest(unittest.TestCase):
    def test_compose_pins_project_name_so_existing_volumes_survive_the_move(self):
        with open(OBSERVABILITY_COMPOSE, encoding="utf-8") as f:
            compose = yaml.safe_load(f)
        self.assertEqual("infra", compose.get("name"), "project name must stay 'infra' (volume/network names)")


class NginxSingleSourceTest(unittest.TestCase):
    def test_nginx_conf_exists_exactly_once(self):
        found = [str(p) for p in tracked_files() if p.name == "nginx.conf"]
        self.assertEqual(
            ["infra/deploy/kustomize/base/nginx.conf"], found, f"nginx.conf copies: {found}"
        )

    def test_kustomize_generates_the_configmap_from_the_file(self):
        with open(KUSTOMIZE_BASE / "kustomization.yaml", encoding="utf-8") as f:
            kustomization = yaml.safe_load(f)
        generators = kustomization.get("configMapGenerator") or []
        files = [entry for g in generators for entry in (g.get("files") or [])]
        self.assertIn("nginx.conf", files, "configMapGenerator must read nginx.conf")
        self.assertNotIn("reverse-proxy-configmap.yaml", kustomization.get("resources", []))
        self.assertFalse((KUSTOMIZE_BASE / "reverse-proxy-configmap.yaml").exists())

    def test_prod_compose_mounts_the_same_file(self):
        compose = PROD_COMPOSE.read_text(encoding="utf-8")
        self.assertIn("./infra/deploy/kustomize/base/nginx.conf:/etc/nginx/nginx.conf", compose)

    @unittest.skipUnless(shutil.which("kustomize"), "kustomize not installed")
    def test_rendered_configmap_equals_the_file(self):
        rendered = subprocess.run(
            ["kustomize", "build", str(KUSTOMIZE_BASE)],
            capture_output=True, check=True, text=True,
        ).stdout
        configmaps = [
            d for d in yaml.safe_load_all(rendered)
            if d and d.get("kind") == "ConfigMap" and "nginx.conf" in (d.get("data") or {})
        ]
        self.assertEqual(1, len(configmaps))
        self.assertEqual(NGINX_CONF.read_text(encoding="utf-8"), configmaps[0]["data"]["nginx.conf"])


class InfraDocumentationTest(unittest.TestCase):
    def test_docs_link_to_infra_documentation(self):
        for doc in ("207-monitoring", "209-deployment"):
            text = (REPO_ROOT / "docs" / "200-architecture" / doc / "README.md").read_text(
                encoding="utf-8"
            )
            self.assertRegex(text, r"infra/(?:README\.md|docs/)", f"{doc} must link to infra/")

    def test_infra_readme_links_every_guide(self):
        readme = (INFRA / "README.md").read_text(encoding="utf-8")
        for guide in ("PREPARATION", "CONFIGURATION", "DEFINITION", "OPERATION"):
            self.assertIn(f"docs/{guide}.md", readme, f"infra/README.md must link {guide}")


class ConsumersFollowNewPathsTest(unittest.TestCase):
    def test_no_active_file_references_a_legacy_path(self):
        offenders = []
        for path in tracked_files():
            rel = path.as_posix()
            if rel.startswith(REFERENCE_EXEMPT_PREFIXES) or "node_modules" in path.parts:
                continue
            try:
                text = (REPO_ROOT / path).read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue
            for number, line in enumerate(text.splitlines(), 1):
                if LEGACY_REFERENCE.search(line):
                    offenders.append(f"{rel}:{number}")
        self.assertEqual([], offenders, f"legacy path references:\n" + "\n".join(offenders))


if __name__ == "__main__":
    unittest.main()
