#!/usr/bin/env python3
"""
Guards issue #1292 (ADR-026): OpenSpec lives under docs/ (the knowledge base) and the CLI resolves
its root from there, because it hard-codes the folder name `openspec`.

Run with: python3 workspace/tests/test_openspec_location.py
"""
import shutil
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
OPENSPEC = REPO_ROOT / "docs" / "openspec"


class OpenSpecLocationTest(unittest.TestCase):
    def test_openspec_lives_under_docs(self):
        for name in ("config.yaml", "schemas", "specs", "changes"):
            self.assertTrue((OPENSPEC / name).exists(), f"docs/openspec/{name} missing")

    def test_no_openspec_folder_at_the_repository_root(self):
        self.assertFalse((REPO_ROOT / "openspec").exists(), "openspec/ moved to docs/openspec/")

    @unittest.skipUnless(shutil.which("openspec"), "openspec CLI not installed")
    def test_cli_resolves_the_root_from_docs(self):
        out = subprocess.run(["openspec", "list", "--specs"], cwd=REPO_ROOT / "docs",
                             capture_output=True, text=True, check=True).stdout
        self.assertIn("login-lockout-e2e", out)


if __name__ == "__main__":
    unittest.main()
