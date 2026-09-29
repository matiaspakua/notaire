import os
import re
import subprocess
import sys
import tempfile
import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import adapter

REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
REAL = os.path.join(REPO, ".aisdlc", "project.yml")

CFG = {
    "spec": {"validate": "openspec validate {change} --strict"},
    "gates": {"main_workflows": ["ci.yml", "cd.yml"]},
    "surfaces": {
        "backend": {"root": "backend-api/", "test_one": "mvn -q -B test -pl backend-api -Dtest={test}",
                    "suite": "mvn -q -B test -pl backend-api"},
        "frontend": {"root": "frontend/", "test_one": "cd frontend && npx vitest run {test}",
                     "suite": "cd frontend && npx vitest run"},
    },
}


class GetTest(unittest.TestCase):
    def test_substitutes_placeholders(self):
        self.assertEqual(adapter.get(CFG, "spec.validate", {"change": "foo-1"}), "openspec validate foo-1 --strict")

    def test_list_value_prints_one_item_per_line(self):
        self.assertEqual(adapter.get(CFG, "gates.main_workflows"), "ci.yml\ncd.yml")

    def test_missing_key_names_key_and_file(self):
        path = os.path.join(tempfile.mkdtemp(), "project.yml")
        with open(path, "w") as f:
            f.write("spec:\n  schema: x\n")
        r = subprocess.run([sys.executable, os.path.join(helpers.BIN, "adapter.py"), "--file", path,
                            "get", "gates.preflight"], capture_output=True, text=True)
        self.assertNotEqual(r.returncode, 0)
        self.assertIn("gates.preflight", r.stderr)
        self.assertIn(path, r.stderr)


class ValidateTest(unittest.TestCase):
    def test_lists_every_missing_required_key(self):
        missing = adapter.missing_keys({"spec": {"schema": "x"}})
        self.assertIn("gates.preflight", missing)
        self.assertNotIn("spec.schema", missing)

    def test_lint_fix_command_is_required(self):
        self.assertIn("gates.docs_lint_fix", adapter.missing_keys({"gates": {"docs_lint": "lint {files}"}}))

    def test_real_adapter_is_complete(self):
        self.assertEqual(adapter.missing_keys(adapter.load(REAL)), [])


class SurfacesTest(unittest.TestCase):
    def test_two_surfaces_in_adapter_order(self):
        self.assertEqual(adapter.surfaces(CFG, ["frontend/src/a.tsx", "backend-api/pom.xml"]), "backend,frontend")

    def test_no_surface(self):
        self.assertEqual(adapter.surfaces(CFG, ["docs/README.md"]), "none")

    def test_fallback_used_when_no_path_under_a_surface(self):
        self.assertEqual(adapter.surfaces(CFG, ["docs/x.csv"], fallback="backend"), "backend")

    def test_path_surface_wins_over_fallback(self):
        self.assertEqual(adapter.surfaces(CFG, ["frontend/a.tsx"], fallback="backend"), "frontend")

    def test_unknown_fallback_ignored(self):
        self.assertEqual(adapter.surfaces(CFG, ["docs/x.csv"], fallback="mobile"), "none")

    def test_cli_fallback_flag(self):
        r = subprocess.run([sys.executable, os.path.join(helpers.BIN, "adapter.py"), "surfaces", "--fallback", "backend"],
                           input="docs/x.csv\n", capture_output=True, text=True)
        self.assertEqual(r.stdout.strip(), "backend")

    def test_suite_for_two_surfaces_runs_each_in_a_subshell(self):
        self.assertEqual(adapter.suite(CFG, "backend,frontend"),
                         "(mvn -q -B test -pl backend-api) && (cd frontend && npx vitest run)")

    def test_legacy_both_means_every_surface(self):
        self.assertEqual(adapter.suite(CFG, "both"), adapter.suite(CFG, "backend,frontend"))

    def test_no_surface_suite_is_true(self):
        self.assertEqual(adapter.suite(CFG, "none"), "true")


class CheckTestCmdTest(unittest.TestCase):
    def test_missing_module_flag_shows_expected_form(self):
        err = adapter.check_test_cmd(CFG, "backend", "mvn -q -B test -Dtest=FooTest")
        self.assertIn("mvn -q -B test -pl backend-api -Dtest=<TestClass>", err)

    def test_correct_command_passes(self):
        self.assertIsNone(adapter.check_test_cmd(CFG, "backend", "mvn -q -B test -pl backend-api -Dtest=FooTest,BarTest"))

    def test_any_surface_of_the_change_is_accepted(self):
        self.assertIsNone(adapter.check_test_cmd(CFG, "backend,frontend", "cd frontend && npx vitest run src/a.test.tsx"))


class RealAdapterTest(unittest.TestCase):
    def test_real_adapter_regexes_compile(self):
        cfg = adapter.load(REAL)
        for key in ("test_files", "source_roots", "guards.forbidden"):
            re.compile(adapter.get(cfg, key))


if __name__ == "__main__":
    unittest.main()
