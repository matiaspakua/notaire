#!/usr/bin/env python3
"""Product version coherence gate (#1591)."""

from __future__ import annotations

import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import textwrap
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
MODULE = REPO_ROOT / "workspace" / "release" / "product_release.py"
CHECKER = REPO_ROOT / "workspace" / "sdlc" / "check-product-version-coherence.py"
PREFLIGHT = REPO_ROOT / "workspace" / "sdlc" / "preflight.sh"


def load_module():
    spec = importlib.util.spec_from_file_location("product_release", MODULE)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def write_minimal_tree(root: Path, version: str = "1.0.0") -> None:
    (root / "backend-api" / "src" / "main" / "resources" / "db" / "migration").mkdir(
        parents=True, exist_ok=True
    )
    (root / "frontend").mkdir(parents=True, exist_ok=True)
    (root / "backend-api" / "src" / "main" / "resources" / "db" / "migration" / "V1__init.sql").write_text(
        "-- init\n", encoding="utf-8"
    )
    (root / "release").mkdir(exist_ok=True)
    root_pom = textwrap.dedent(
        f"""
        <project>
          <artifactId>notaire-root</artifactId>
          <version>{version}</version>
        </project>
        """
    )
    (root / "pom.xml").write_text(root_pom, encoding="utf-8")
    backend_pom = textwrap.dedent(
        f"""
        <project>
          <parent>
            <artifactId>notaire-root</artifactId>
            <version>{version}</version>
          </parent>
        </project>
        """
    )
    (root / "backend-api" / "pom.xml").write_text(backend_pom, encoding="utf-8")
    (root / "frontend" / "package.json").write_text(
        json.dumps({"name": "notaire-frontend", "version": version}) + "\n",
        encoding="utf-8",
    )
    (root / ".release-please-manifest.json").write_text(
        json.dumps({".": version}) + "\n", encoding="utf-8"
    )
    (root / "CHANGELOG.md").write_text(
        f"## [{version}](https://github.com/matiaspakua/notaire/releases/tag/v{version}) (2026-10-13)\n\n",
        encoding="utf-8",
    )
    mod = load_module()
    snapshot = mod.collect(root)
    (root / "release" / "product-release.json").write_text(
        json.dumps(mod.product_release_document(snapshot), indent=2) + "\n",
        encoding="utf-8",
    )
    release_please = {
        "packages": {
            ".": {
                "include-component-in-tag": False,
                "extra-files": [
                    {"path": "pom.xml"},
                    {"path": "backend-api/pom.xml"},
                    {"path": "frontend/package.json"},
                    {"path": "release/product-release.json"},
                    {"path": "docker-compose.yml"},
                    {"path": "docker-compose.prod.yml"},
                    {"path": "infra/deploy/kustomize/overlays/staging/kustomization.yaml"},
                ],
            }
        }
    }
    (root / "release-please-config.json").write_text(
        json.dumps(release_please, indent=2) + "\n", encoding="utf-8"
    )
    image_line = f"    image: ghcr.io/matiaspakua/notaire/backend:{version} # x-release-please-version\n"
    fe_image = f"    image: ghcr.io/matiaspakua/notaire/frontend:{version} # x-release-please-version\n"
    compose = (
        "services:\n  backend:\n    build: {}\n"
        + image_line
        + "  frontend:\n    build: {}\n"
        + fe_image
    )
    (root / "docker-compose.yml").write_text(compose, encoding="utf-8")
    (root / "docker-compose.prod.yml").write_text(compose, encoding="utf-8")
    kustomize_dir = root / "infra" / "deploy" / "kustomize" / "overlays" / "staging"
    kustomize_dir.mkdir(parents=True, exist_ok=True)
    (kustomize_dir / "kustomization.yaml").write_text(
        textwrap.dedent(
            f"""
            images:
              - name: notaire-backend
                newTag: {version} # x-release-please-version
              - name: notaire-frontend
                newTag: {version} # x-release-please-version
            labels:
              - pairs:
                  app.kubernetes.io/version: {version} # x-release-please-version
            """
        ),
        encoding="utf-8",
    )
    workflow = (REPO_ROOT / ".github" / "workflows" / "cd.yml").read_text(encoding="utf-8")
    (root / ".github" / "workflows").mkdir(parents=True, exist_ok=True)
    (root / ".github" / "workflows" / "cd.yml").write_text(workflow, encoding="utf-8")


class CoherentTreeTest(unittest.TestCase):
    def test_real_repository_passes(self):
        mod = load_module()
        errors = mod.coherence_errors(REPO_ROOT)
        self.assertEqual(errors, [])

    def test_minimal_temp_tree_passes(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "0.2.0")
            self.assertEqual(mod.coherence_errors(root), [])


class VersionDriftTest(unittest.TestCase):
    def test_maven_npm_drift(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "1.0.0")
            pkg = json.loads((root / "frontend" / "package.json").read_text(encoding="utf-8"))
            pkg["version"] = "1.0.1"
            (root / "frontend" / "package.json").write_text(json.dumps(pkg) + "\n", encoding="utf-8")
            errors = mod.coherence_errors(root)
            self.assertTrue(any("frontend/package.json" in item for item in errors))

    def test_snapshot_version_fails(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "1.0.0-SNAPSHOT")
            errors = mod.check(root)
            self.assertTrue(any("snapshot" in item.lower() for item in errors))


class TagValidationTest(unittest.TestCase):
    def setUp(self):
        self.mod = load_module()

    def test_matching_tag_passes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "0.2.0")
            self.assertEqual(self.mod.coherence_errors(root, tag="v0.2.0"), [])

    def test_tag_ahead_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "0.2.0")
            errors = self.mod.coherence_errors(root, tag="v0.2.1")
            self.assertTrue(errors)

    def test_legacy_tag_shape_only(self):
        ok, _ = self.mod.tag_is_valid("notaire-v0.1.2", "9.9.9")
        self.assertTrue(ok)

    def test_legacy_prefix_on_new_version_fails(self):
        ok, message = self.mod.tag_is_valid("notaire-v0.2.0", "0.2.0")
        self.assertFalse(ok)
        self.assertIn("v", message.lower())

    def test_malformed_tags_fail(self):
        for tag in ("0.2.0", "v0.2", "v0.2.0-rc.1", "release-v0.2.0"):
            ok, _ = self.mod.tag_is_valid(tag, "0.2.0")
            self.assertFalse(ok, tag)


class PackagedVersionTest(unittest.TestCase):
    def test_packaged_matches_record(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "0.2.0")
            packaged = root / "application.properties"
            packaged.write_text("app.version=0.2.0\n", encoding="utf-8")
            self.assertEqual(mod.coherence_errors(root, packaged=packaged), [])

    def test_unfiltered_token_fails(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "0.2.0")
            packaged = root / "application.properties"
            packaged.write_text("app.version=@project.version@\n", encoding="utf-8")
            errors = mod.coherence_errors(root, packaged=packaged)
            self.assertTrue(any("@project.version@" in item for item in errors))


class AuthFreeWiringTest(unittest.TestCase):
    def test_preflight_and_workflows_invoke_checker(self):
        preflight = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("check-product-version-coherence.py", preflight)
        for relative in (
            ".github/workflows/sdlc-process.yml",
            ".github/workflows/ci.yml",
            ".github/workflows/cd.yml",
        ):
            text = (REPO_ROOT / relative).read_text(encoding="utf-8")
            self.assertIn("check-product-version-coherence.py", text)

    def test_auth_free_execution(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "0.2.0")
            gh_shim = Path(directory) / "gh"
            gh_shim.write_text("#!/bin/sh\necho gh-called >&2\nexit 1\n", encoding="utf-8")
            gh_shim.chmod(0o755)
            env = os.environ.copy()
            env["PATH"] = f"{directory}:{env.get('PATH', '')}"
            for key in ("GH_TOKEN", "GITHUB_TOKEN", "GH_ENTERPRISE_TOKEN"):
                env.pop(key, None)
            result = subprocess.run(
                [sys.executable, str(CHECKER), "--root", str(root)],
                env=env,
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.returncode, 0)
            self.assertNotIn("gh-called", result.stderr + result.stdout)


class SbomAndAssetDriftTest(unittest.TestCase):
    def test_release_assets_constant_matches_cd(self):
        mod = load_module()
        text = (REPO_ROOT / ".github" / "workflows" / "cd.yml").read_text(encoding="utf-8")
        for asset in mod.RELEASE_ASSETS:
            self.assertIn(asset, text)

    def test_extra_asset_name_in_cd_fails_check(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_minimal_tree(root, "1.0.0")
            cd = (root / ".github" / "workflows" / "cd.yml").read_text(encoding="utf-8")
            cd = cd.replace("release-assets/sbom-database.cdx.json\n", "")
            (root / ".github" / "workflows" / "cd.yml").write_text(cd, encoding="utf-8")
            errors = mod.check_cd_release_assets(root)
            self.assertTrue(errors)


if __name__ == "__main__":
    unittest.main()
