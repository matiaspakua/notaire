#!/usr/bin/env python3
"""
Gate 2 checks for #1511 / CU76 — living testing inventory and process architecture.

Plain stdlib unittest. Run with:
  python3 -m unittest discover -s testing/tests -p 'test_testing_inventory.py'
"""
from __future__ import annotations

import re
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
TESTING_DOCS = REPO / "docs" / "300-development" / "303-testing"
E2E_TESTS = REPO / "testing" / "e2e" / "tests"
GENERATE = REPO / "workspace" / "sdlc" / "generate-testing-inventory.py"
VALIDATE = REPO / "workspace" / "sdlc" / "validate-testing-inventory.py"
PREFLIGHT = REPO / "workspace" / "sdlc" / "preflight.sh"
CONSTITUTION = REPO / "CONSTITUTION.md"
TS_NAME = re.compile(r"^TS-\d{4}-.+\.spec\.ts$")


class InventoryToolingTest(unittest.TestCase):
    def test_generator_and_validator_scripts_exist(self):
        self.assertTrue(GENERATE.is_file(), f"missing {GENERATE}")
        self.assertTrue(VALIDATE.is_file(), f"missing {VALIDATE}")

    def test_inventory_markdown_exists(self):
        inventory = TESTING_DOCS / "INVENTORY.md"
        self.assertTrue(inventory.is_file(), "INVENTORY.md must be generated and committed")

    def test_validator_passes_on_current_tree(self):
        result = subprocess.run(
            ["python3", str(VALIDATE)],
            cwd=REPO,
            capture_output=True,
            text=True,
            check=False,
        )
        self.assertEqual(
            0,
            result.returncode,
            f"validate-testing-inventory failed:\n{result.stdout}\n{result.stderr}",
        )

    def test_stale_inventory_fails_validation(self):
        inventory = TESTING_DOCS / "INVENTORY.md"
        original = inventory.read_text(encoding="utf-8")
        try:
            inventory.write_text(original + "\n<!-- stale-marker -->\n", encoding="utf-8")
            result = subprocess.run(
                ["python3", str(VALIDATE)],
                cwd=REPO,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertNotEqual(0, result.returncode, "stale inventory must fail validation")
        finally:
            inventory.write_text(original, encoding="utf-8")

    def test_unprefixed_spec_fails_validation(self):
        with tempfile.TemporaryDirectory() as tmp:
            fake_root = Path(tmp)
            # Point validator at a temp tree by generating a tiny fixture layout
            # through env is not supported; instead write a bad file into the real
            # tests dir briefly.
            bad = E2E_TESTS / "unprefixed-bad-spec.spec.ts"
            self.assertFalse(bad.exists())
            try:
                bad.write_text("// temporary fixture for #1511\n", encoding="utf-8")
                result = subprocess.run(
                    ["python3", str(VALIDATE)],
                    cwd=REPO,
                    capture_output=True,
                    text=True,
                    check=False,
                )
                self.assertNotEqual(0, result.returncode, "unprefixed spec must fail validation")
            finally:
                if bad.exists():
                    bad.unlink()


class SpecNamingTest(unittest.TestCase):
    def test_every_playwright_spec_uses_ts_nnnn_prefix(self):
        offenders = sorted(
            path.name
            for path in E2E_TESTS.glob("*.spec.ts")
            if not TS_NAME.match(path.name)
        )
        self.assertEqual(
            [],
            offenders,
            "every Playwright spec must match TS-nnnn-*.spec.ts",
        )


class MasterTestPlanDocsTest(unittest.TestCase):
    def test_mtp_declares_levels_and_id_scheme(self):
        text = (TESTING_DOCS / "TEST-PLAN.md").read_text(encoding="utf-8")
        self.assertRegex(text, r"Master Test Plan", msg="TEST-PLAN.md must be the MTP")
        for level in (
            "Unit",
            "Integration",
            "Bruno",
            "Vitest",
            "Playwright",
            "Security",
            "Performance",
        ):
            self.assertIn(level, text, f"MTP missing level mention: {level}")
        self.assertRegex(text, r"CU.*TC.*TS|ID scheme|Identificación", re.IGNORECASE)

    def test_process_architecture_document_exists(self):
        path = TESTING_DOCS / "PROCESS-ARCHITECTURE.md"
        self.assertTrue(path.is_file())
        text = path.read_text(encoding="utf-8")
        self.assertRegex(text, r"process|CI|owner|evidence", re.IGNORECASE)

    def test_preflight_runs_inventory_validation(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("validate-testing-inventory.py", text)

    def test_coverage_floors_documented_match_tools(self):
        constitution = CONSTITUTION.read_text(encoding="utf-8")
        self.assertRegex(constitution, r"80%\s*line\s*/\s*65%\s*branch")
        # Enforced Vitest floors are raise-only teens, not a false 75%.
        self.assertNotRegex(
            constitution,
            r"\|\s*Vitest\s*\|[^|\n]*\|\s*75%\s*line",
            msg="CONSTITUTION Coverage Enforcement must not claim Vitest 75%",
        )
        self.assertRegex(
            constitution,
            r"raise-only|statements:\s*14|14%\s*line",
            msg="CONSTITUTION must document Vitest raise-only floors",
        )
        mtp = (TESTING_DOCS / "TEST-PLAN.md").read_text(encoding="utf-8")
        self.assertRegex(mtp, r"80%\s*line\s*/\s*65%\s*branch")
        self.assertRegex(mtp, r"raise-only|enforced floor|vitest\.config", re.IGNORECASE)


if __name__ == "__main__":
    unittest.main()
