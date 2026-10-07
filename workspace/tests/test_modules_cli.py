#!/usr/bin/env python3
"""
Guards issue #1292 (ADR-026): the Foreman reads workspace/modules.yaml through workspace/modules.py
to list modules in dependency order, find the modules a change affects, and run each verify command.

Run with: python3 workspace/tests/test_modules_cli.py
"""
import subprocess
import sys
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CLI = REPO_ROOT / "workspace" / "modules.py"


def run(*args):
    return subprocess.run([sys.executable, str(CLI), *args], cwd=REPO_ROOT, capture_output=True, text=True)


class ModulesCliTest(unittest.TestCase):
    def test_list_puts_dependencies_before_dependents(self):
        names = run("list").stdout.split()
        self.assertLess(names.index("backend-api"), names.index("frontend"))
        self.assertLess(names.index("frontend"), names.index("testing"))
        self.assertLess(names.index("contracts"), names.index("workspace"))

    def test_affected_by_a_backend_change_includes_its_dependents(self):
        names = run("affected", "backend-api/src/main/java/A.java").stdout.split()
        self.assertEqual({"backend-api", "frontend", "infra", "testing", "workspace"}, set(names))

    def test_affected_by_a_docs_change_is_docs_and_workspace(self):
        names = run("affected", "docs/200-architecture/README.md").stdout.split()
        self.assertEqual({"docs", "workspace"}, set(names))

    def test_affected_ignores_paths_outside_every_module(self):
        self.assertEqual("", run("affected", "README.md").stdout.strip())

    def test_verify_dry_run_prints_the_module_command(self):
        out = run("verify", "contracts", "--dry-run").stdout
        self.assertIn("bash contracts/verify.sh", out)

    def test_unknown_module_fails(self):
        self.assertNotEqual(0, run("verify", "nope", "--dry-run").returncode)


if __name__ == "__main__":
    unittest.main()
