#!/usr/bin/env python3
"""
Gate 2 checks for #1511 / #2117 / CU76 — discovery-based testing inventory.

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
BACKEND_TEST = REPO / "backend-api" / "src" / "test"
FRONTEND_SRC = REPO / "frontend" / "src"
GENERATE = REPO / "workspace" / "sdlc" / "generate-testing-inventory.py"
VALIDATE = REPO / "workspace" / "sdlc" / "validate-testing-inventory.py"
PREFLIGHT = REPO / "workspace" / "sdlc" / "preflight.sh"
CONSTITUTION = REPO / "CONSTITUTION.md"
TS_NAME = re.compile(r"^TS-\d{4}-.+\.spec\.ts$")
COUNT_ROW = re.compile(
    r"^\|\s*(?:Frontend Vitest files|Backend `\*Test\.java` \(all\))\s*\|\s*\d+\s*\|",
    re.MULTILINE,
)


def _disk_counts() -> tuple[int, int]:
    backend = len(list(BACKEND_TEST.rglob("*Test.java"))) if BACKEND_TEST.is_dir() else 0
    vitest = 0
    if FRONTEND_SRC.is_dir():
        vitest = len(list(FRONTEND_SRC.rglob("*.test.ts"))) + len(
            list(FRONTEND_SRC.rglob("*.test.tsx"))
        )
    return backend, vitest


class InventoryToolingTest(unittest.TestCase):
    def test_generator_and_validator_scripts_exist(self):
        self.assertTrue(GENERATE.is_file(), f"missing {GENERATE}")
        self.assertTrue(VALIDATE.is_file(), f"missing {VALIDATE}")

    def test_committed_inventory_has_no_shared_count_table(self):
        inventory = TESTING_DOCS / "INVENTORY.md"
        self.assertTrue(inventory.is_file(), "committed INVENTORY.md stub must exist")
        text = inventory.read_text(encoding="utf-8")
        self.assertIsNone(
            COUNT_ROW.search(text),
            "committed INVENTORY.md must not hold shared suite count rows (#2117)",
        )
        self.assertRegex(
            text,
            r"generate-testing-inventory|INVENTORY\.generated|on-demand|docs.build",
            msg="stub must point humans at on-demand / docs-build generation",
        )

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
        combined = f"{result.stdout}\n{result.stderr}"
        backend, vitest = _disk_counts()
        self.assertRegex(combined, rf"Vitest[^\n]*\b{vitest}\b|vitest[^\n]*\b{vitest}\b", re.I)
        self.assertRegex(combined, rf"[Bb]ackend[^\n]*\b{backend}\b")

    def test_validator_passes_when_generated_report_absent_or_stale(self):
        generated = TESTING_DOCS / "INVENTORY.generated.md"
        inventory = TESTING_DOCS / "INVENTORY.md"
        original_inv = inventory.read_text(encoding="utf-8") if inventory.is_file() else None
        original_gen = generated.read_text(encoding="utf-8") if generated.is_file() else None
        try:
            if generated.is_file():
                generated.unlink()
            # Corrupt stub so a stale-diff check would fail if still wired.
            inventory.write_text(
                original_inv + "\n<!-- stale-marker-must-not-fail-validator -->\n"
                if original_inv is not None
                else "# stub\n<!-- stale -->\n",
                encoding="utf-8",
            )
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
                "validator must not fail on a lagging committed markdown snapshot:\n"
                f"{result.stdout}\n{result.stderr}",
            )
        finally:
            if original_inv is None:
                if inventory.is_file():
                    inventory.unlink()
            else:
                inventory.write_text(original_inv, encoding="utf-8")
            if original_gen is None:
                if generated.is_file():
                    generated.unlink()
            else:
                generated.write_text(original_gen, encoding="utf-8")

    def test_generator_reports_real_disk_counts(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "INVENTORY.generated.md"
            result = subprocess.run(
                ["python3", str(GENERATE), "--output", str(out)],
                cwd=REPO,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(
                0,
                result.returncode,
                f"generate failed:\n{result.stdout}\n{result.stderr}",
            )
            self.assertTrue(out.is_file())
            text = out.read_text(encoding="utf-8")
            backend, vitest = _disk_counts()
            self.assertRegex(
                text,
                rf"\|\s*Frontend Vitest files\s*\|\s*{vitest}\s*\|",
            )
            self.assertRegex(
                text,
                rf"\|\s*Backend `\*Test\.java` \(all\)\s*\|\s*{backend}\s*\|",
            )
            self.assertGreaterEqual(vitest, 202, "main has at least 202 Vitest files per #2117")

    def test_unprefixed_spec_fails_validation(self):
        bad = E2E_TESTS / "unprefixed-bad-spec.spec.ts"
        self.assertFalse(bad.exists())
        try:
            bad.write_text("// temporary fixture for #1511 / #2117\n", encoding="utf-8")
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
        self.assertRegex(constitution, r"85%\s*line\s*/\s*70%\s*branch")
        # Owner target >90% (#2046) must stay labeled separately from floors.
        self.assertRegex(constitution, r">\s*90%|#2046")
        # Enforced Vitest floors match vitest.config.ts (not a false absolute 90%).
        self.assertNotRegex(
            constitution,
            r"\|\s*Vitest\s*\|[^|\n]*\|\s*90%\s*line",
            msg="CONSTITUTION must not claim Vitest floor already equals 90%",
        )
        self.assertRegex(
            constitution,
            r"raise-only|statements\s*\*\*76%\*\*|76%",
            msg="CONSTITUTION must document current Vitest raise-only floors",
        )
        mtp = (TESTING_DOCS / "TEST-PLAN.md").read_text(encoding="utf-8")
        self.assertRegex(mtp, r"85%\s*line\s*/\s*70%\s*branch")
        self.assertRegex(mtp, r">\s*90%|#2046")
        self.assertRegex(mtp, r"raise-only|enforced floor|vitest\.config", re.IGNORECASE)


if __name__ == "__main__":
    unittest.main()
