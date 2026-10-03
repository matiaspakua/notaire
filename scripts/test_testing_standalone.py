#!/usr/bin/env python3
"""
Guards issue #1191 (CU76): testing/ is a single, self-contained, documented folder with
which a separate QA team verifies and validates Notaire as black-box modules.

Plain stdlib unittest + PyYAML, consistent with scripts/test_infra_standalone.py.
Run with: python3 scripts/test_testing_standalone.py
"""
import os
import re
import subprocess
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[1]
TESTING = REPO_ROOT / "testing"
DB_COMPOSE = TESTING / "database" / "docker-compose.yml"
RUNNER = TESTING / "scripts" / "run.sh"
WRAPPER = TESTING / "scripts" / "test.sh"
ENV_EXAMPLE = TESTING / ".env.example"
DB_WORKFLOW = REPO_ROOT / ".github" / "workflows" / "database-vv.yml"
PREFLIGHT = REPO_ROOT / "scripts" / "preflight.sh"
TESTING_GUIDE = REPO_ROOT / "docs" / "300-development" / "303-testing" / "README.md"

REQUIRED_PATHS = (
    "integration/http/test-all-endpoints-v2.sh",
    "integration/e2e-login-and-stack.sh",
    "database/docker-compose.yml",
    "database/run.sh",
    "database/checks",
    "scripts/run.sh",
    "scripts/test.sh",
    "scripts/generate-coverage-report.sh",
    ".env.example",
    "README.md",
    "docs/PREPARATION.md",
    "docs/CONFIGURATION.md",
    "docs/DEFINITION.md",
    "docs/OPERATION.md",
    "e2e-swing/README.md",
)

REMOVED_PATHS = (
    "run-all-tests.sh",
    "generate-coverage-report.sh",
    "scripts/test-all.sh",
    "scripts/run-comprehensive-tests.sh",
    "reports",
    "integration/http/01-auth.sh",
    "integration/http/02-usuarios.sh",
    "integration/http/03-conceptos.sh",
    "integration/http/04-people.sh",
    "integration/http/05-tramites.sh",
    "integration/http/06-escrituras.sh",
    "integration/http/07-presupuestos.sh",
    "integration/http/08-items.sh",
    "integration/http/test-all-endpoints.sh",
)

REQUIRED_ENV = (
    "MIGRATIONS_DIR",
    "BASE_URL",
    "POSTGRES_EXPORTER_USER",
    "POSTGRES_EXPORTER_PASSWORD",
)

LEGACY_REFERENCE = re.compile(
    r"testing/http\b"
    r"|testing/integration/http/(?:0[1-8]-|test-all-endpoints\.sh)"
    r"|deprecated-src\.old"
    r"|testing/run-all-tests\.sh"
    r"|testing/scripts/test-all\.sh"
    r"|run-comprehensive-tests\.sh"
    r"|testing/generate-coverage-report\.sh"
    r"|testing/reports/"
)
REFERENCE_EXEMPT_PREFIXES = (
    "docs/000-archive/",
    "docs/archive/",
    "openspec/",
    "CHANGELOG.md",
    ".gitignore",
    "scripts/test_testing_standalone.py",
)

SEAM_MARKER = "TESTING_APP_SEAM"
ESCAPE_CANDIDATE = re.compile(r"(?:\.\./)+[\w.-]*")
SCANNED_SUFFIXES = {".yml", ".yaml", ".sh"}
COMPOSE_VARIABLE = re.compile(r"\$\{([A-Z][A-Z0-9_]*)")
REAL_TOKEN = re.compile(r"sq[pua]_|ghp_|AKIA[0-9A-Z]{8,}")


def tracked_files():
    out = subprocess.run(
        ["git", "ls-files", "-z"], cwd=REPO_ROOT, capture_output=True, check=True
    ).stdout.decode("utf-8")
    return [Path(p) for p in out.split("\0") if p and (REPO_ROOT / p).exists()]


def testing_scripts():
    return [
        p for p in TESTING.rglob("*")
        if p.is_file() and p.suffix in SCANNED_SUFFIXES
        and "e2e-swing" not in p.parts and "node_modules" not in p.parts
    ]


class LayoutTest(unittest.TestCase):
    def test_suites_live_under_testing(self):
        missing = [p for p in REQUIRED_PATHS if not (TESTING / p).exists()]
        self.assertEqual([], missing, f"missing under testing/: {missing}")

    def test_dead_scripts_and_stale_reports_are_gone(self):
        present = [p for p in REMOVED_PATHS if (TESTING / p).exists()]
        self.assertEqual([], present, f"should be removed: {present}")

    def test_swing_readme_is_kept_for_the_retirement_spec(self):
        self.assertTrue((TESTING / "e2e-swing" / "README.md").is_file())


class RunnerTest(unittest.TestCase):
    def run_runner(self, *args):
        return subprocess.run(["bash", str(RUNNER), *args], capture_output=True, text=True, cwd=REPO_ROOT)

    def test_runner_lists_the_suites(self):
        result = self.run_runner("--list")
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual({"integration", "database"}, set(result.stdout.split()))

    def test_runner_rejects_an_unknown_suite(self):
        self.assertNotEqual(0, self.run_runner("nonsense").returncode)

    def test_wrapper_delegates_to_the_integration_suite(self):
        text = WRAPPER.read_text(encoding="utf-8")
        self.assertRegex(text, r'run\.sh"?\s+integration')
        self.assertNotIn("test-all", text)


class DatabaseHarnessTest(unittest.TestCase):
    def setUp(self):
        with open(DB_COMPOSE, encoding="utf-8") as f:
            self.services = yaml.safe_load(f)["services"]

    def test_images_are_pinned_to_the_versions_the_product_runs(self):
        images = {s["image"] for s in self.services.values()}
        self.assertEqual({"postgres:16.15", "flyway/flyway:12.4.0"}, images)

    def test_no_service_publishes_a_host_port(self):
        published = {name: s["ports"] for name, s in self.services.items() if s.get("ports")}
        self.assertEqual({}, published)

    def test_migrations_are_mounted_read_only_from_a_variable(self):
        flyway = next(s for s in self.services.values() if s["image"].startswith("flyway/"))
        mounts = [v for v in flyway.get("volumes", []) if "/flyway/sql" in v]
        self.assertEqual(1, len(mounts), mounts)
        self.assertRegex(mounts[0], r"^\$\{MIGRATIONS_DIR[^}]*\}:/flyway/sql:ro$")

    def test_at_least_one_sql_check_exists(self):
        checks = sorted((TESTING / "database" / "checks").glob("*.sql"))
        self.assertGreaterEqual(len(checks), 4, [c.name for c in checks])


class SelfContainmentTest(unittest.TestCase):
    def test_testing_does_not_reference_paths_outside_itself(self):
        escapes = []
        for path in testing_scripts():
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                if SEAM_MARKER in line:
                    continue
                for match in ESCAPE_CANDIDATE.finditer(line):
                    resolved = Path(os.path.normpath(path.parent / match.group(0)))
                    if TESTING not in (resolved, *resolved.parents):
                        escapes.append(f"{path.relative_to(REPO_ROOT)}:{number}: {match.group(0)}")
        self.assertEqual([], escapes, f"testing/ references outside itself: {escapes}")

    def test_env_example_declares_every_variable_the_suites_use(self):
        example = ENV_EXAMPLE.read_text(encoding="utf-8")
        declared = set(re.findall(r"^#?\s*([A-Z][A-Z0-9_]*)=", example, re.MULTILINE))
        used = set(REQUIRED_ENV)
        used |= set(COMPOSE_VARIABLE.findall(DB_COMPOSE.read_text(encoding="utf-8")))
        self.assertEqual([], sorted(used - declared), "testing/.env.example is missing variables")

    def test_env_example_contains_no_real_tokens(self):
        self.assertIsNone(REAL_TOKEN.search(ENV_EXAMPLE.read_text(encoding="utf-8")))


GNU_ONLY_IDIOMS = (
    (re.compile(r"\bhead\s+-n\s+-\d"), "head -n -N (BSD head rejects it)"),
    (re.compile(r"\bsed\s+-i\b"), "sed -i (BSD and GNU disagree on its argument)"),
    (re.compile(r"\bgrep\s+-\w*P\b"), "grep -P (not on BSD grep)"),
    (re.compile(r"\breadlink\s+-f\b"), "readlink -f (not on BSD)"),
    (re.compile(r"\bdate\s+-d\b"), "date -d (not on BSD date)"),
)
CI_ONLY_SCRIPTS = {"generate-coverage-report.sh"}
# Called from outside testing/ (Constitution step 14, preflight, agent rules) or by CI.
EXTERNALLY_CALLED_SCRIPTS = {"test.sh", "generate-coverage-report.sh"}


class PortabilityTest(unittest.TestCase):
    def test_suite_scripts_avoid_gnu_only_idioms(self):
        offenders = []
        for path in testing_scripts():
            if path.suffix != ".sh" or path.name in CI_ONLY_SCRIPTS:
                continue
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                if line.lstrip().startswith("#"):
                    continue
                for pattern, label in GNU_ONLY_IDIOMS:
                    if pattern.search(line):
                        offenders.append(f"{path.relative_to(REPO_ROOT)}:{number}: {label}")
        self.assertEqual([], offenders, "GNU-only idioms in QA scripts:\n" + "\n".join(offenders))


class ReachabilityTest(unittest.TestCase):
    def test_every_script_is_called_by_the_runner_or_another_suite_script(self):
        scripts = [p for p in testing_scripts() if p.suffix == ".sh"]
        orphans = []
        for script in scripts:
            if script.name in EXTERNALLY_CALLED_SCRIPTS:
                continue
            callers = [
                other for other in scripts
                if other != script and any(
                    script.name in line and not line.lstrip().startswith("#")
                    for line in other.read_text(encoding="utf-8").splitlines()
                )
            ]
            if not callers:
                orphans.append(str(script.relative_to(REPO_ROOT)))
        self.assertEqual([], orphans, f"scripts nothing calls: {orphans}")


class DocumentationTest(unittest.TestCase):
    def test_readme_links_every_guide(self):
        readme = (TESTING / "README.md").read_text(encoding="utf-8")
        for guide in ("PREPARATION", "CONFIGURATION", "DEFINITION", "OPERATION"):
            self.assertIn(f"docs/{guide}.md", readme, f"testing/README.md must link {guide}")

    def test_project_testing_guide_links_to_testing(self):
        self.assertRegex(TESTING_GUIDE.read_text(encoding="utf-8"), r"\.\./\.\./\.\./testing/")


class GatesTest(unittest.TestCase):
    def test_workflow_runs_the_database_suite_on_relevant_changes(self):
        text = DB_WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("testing/database/**", text)
        self.assertIn("backend-api/src/main/resources/db/migration/**", text)
        self.assertIn("run.sh database", text)

    def test_preflight_carries_the_same_gate(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("run.sh database", text)
        self.assertIn("database-vv.yml", text)


class ConsumersFollowNewPathsTest(unittest.TestCase):
    def test_no_active_file_references_a_removed_path(self):
        offenders = []
        for path in tracked_files():
            rel = path.as_posix()
            if rel.startswith(REFERENCE_EXEMPT_PREFIXES) or "node_modules" in path.parts:
                continue
            if rel.startswith("testing/e2e-swing/"):
                continue
            try:
                text = (REPO_ROOT / path).read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue
            for number, line in enumerate(text.splitlines(), 1):
                if LEGACY_REFERENCE.search(line):
                    offenders.append(f"{rel}:{number}")
        self.assertEqual([], offenders, "references to removed testing/ paths:\n" + "\n".join(offenders))


if __name__ == "__main__":
    unittest.main()
