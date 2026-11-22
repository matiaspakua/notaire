#!/usr/bin/env python3
"""Guards #2413 (CU76): the pre-push gate runs only the suite groups a branch touches.

Run with: python3 workspace/tests/test_preflight_affected.py
"""
from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[2] / "workspace" / "sdlc" / "preflight_affected.py"


def load():
    spec = importlib.util.spec_from_file_location("preflight_affected", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class AffectedGroupsTest(unittest.TestCase):
    def setUp(self):
        self.mod = load()

    def test_a_factory_only_branch_skips_the_product_suites(self):
        groups = self.mod.groups(["framework/ai-sdlc/factory-dashboard/src/ui/App.tsx"])
        self.assertEqual(set(), groups & {"backend", "frontend", "pages", "stack", "testing"})

    def test_a_backend_branch_runs_backend_and_the_server_backed_suites(self):
        groups = self.mod.groups(["backend-api/src/main/java/Foo.java"])
        self.assertIn("backend", groups)
        self.assertIn("stack", groups)
        self.assertNotIn("frontend", groups)
        self.assertNotIn("pages", groups)

    def test_a_frontend_branch_runs_frontend_and_the_server_backed_suites(self):
        groups = self.mod.groups(["frontend/src/app/page.tsx"])
        self.assertEqual({"frontend", "stack"}, groups)

    def test_a_docs_branch_runs_pages(self):
        groups = self.mod.groups(["docs/README.md"])
        self.assertEqual({"pages"}, groups)

    def test_a_release_record_runs_pages(self):
        self.assertIn("pages", self.mod.groups(["CHANGELOG.md"]))
        self.assertIn("pages", self.mod.groups(["workspace/release/product_release.py"]))

    def test_the_e2e_suite_runs_its_lint_and_the_server_backed_suites(self):
        self.assertEqual({"testing", "stack"}, self.mod.groups(["testing/e2e/tests/TS-0001.spec.ts"]))

    def test_changing_the_gate_runs_everything(self):
        for path in ("workspace/sdlc/preflight.sh", ".githooks/pre-push", "workspace/stack/start.sh",
                     "docker-compose.yml", "docker-compose.prod.yml", "pom.xml", "backend-api/Dockerfile"):
            self.assertEqual(self.mod.ALL, self.mod.groups([path]), path)

    def test_no_comparison_base_runs_everything(self):
        self.assertEqual(self.mod.ALL, self.mod.groups(None))

    def test_the_cli_prints_the_groups_for_a_base(self):
        self.assertEqual("all", self.mod.render(self.mod.ALL))
        self.assertEqual("backend stack", self.mod.render({"stack", "backend"}))
        self.assertEqual("none", self.mod.render(set()))


if __name__ == "__main__":
    unittest.main()
