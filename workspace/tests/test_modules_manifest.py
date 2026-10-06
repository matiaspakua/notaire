#!/usr/bin/env python3
"""
Guards issue #1292 (CU76): every module is declared in workspace/modules.yaml, documents its
contract in MODULE.md and verifies itself with an executable verify.sh.

Run with: python3 workspace/tests/test_modules_manifest.py
"""
import os
import subprocess
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
MANIFEST = REPO_ROOT / "workspace" / "modules.yaml"
REQUIRED_KEYS = ("path", "responsibility", "verify", "depends_on")


def load_modules():
    return yaml.safe_load(MANIFEST.read_text(encoding="utf-8"))["modules"]


class ModulesManifestTest(unittest.TestCase):
    def test_manifest_exists(self):
        self.assertTrue(MANIFEST.is_file(), "workspace/modules.yaml is the Foreman's single manifest")

    def test_every_module_declares_the_required_keys(self):
        for name, module in load_modules().items():
            for key in REQUIRED_KEYS:
                self.assertIn(key, module, f"{name} must declare '{key}'")

    def test_every_module_has_module_md_and_executable_verify(self):
        for name, module in load_modules().items():
            folder = REPO_ROOT / module["path"]
            self.assertTrue((folder / "MODULE.md").is_file(), f"{name}: MODULE.md missing")
            verify = folder / "verify.sh"
            self.assertTrue(verify.is_file(), f"{name}: verify.sh missing")
            self.assertTrue(os.access(verify, os.X_OK), f"{name}: verify.sh must be executable")

    def test_verify_scripts_parse_and_fail_fast(self):
        for name, module in load_modules().items():
            verify = REPO_ROOT / module["path"] / "verify.sh"
            self.assertEqual(0, subprocess.run(["bash", "-n", str(verify)]).returncode, f"{name}: syntax error")
            self.assertIn("set -euo pipefail", verify.read_text(encoding="utf-8"), f"{name}: must fail fast")

    def test_depends_on_names_listed_modules(self):
        modules = load_modules()
        for name, module in modules.items():
            for dependency in module["depends_on"]:
                self.assertIn(dependency, modules, f"{name} depends on unknown module {dependency}")

    def test_dependencies_are_acyclic(self):
        modules = load_modules()
        resolved, visiting = set(), set()

        def visit(name):
            self.assertNotIn(name, visiting, f"dependency cycle through {name}")
            if name in resolved:
                return
            visiting.add(name)
            for dependency in modules[name]["depends_on"]:
                visit(dependency)
            visiting.discard(name)
            resolved.add(name)

        for name in modules:
            visit(name)


if __name__ == "__main__":
    unittest.main()
