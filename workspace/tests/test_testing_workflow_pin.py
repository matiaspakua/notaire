"""Guards #2514: Notaire pins notaire-testing reusable workflows; local tree is stub-only."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
E2E_CALLER = ROOT / ".github" / "workflows" / "playwright-e2e.yml"
DB_CALLER = ROOT / ".github" / "workflows" / "database-vv.yml"
TESTING = ROOT / "testing"
PIN = re.compile(
    r"uses:\s*matiaspakua/notaire-testing/\.github/workflows/(e2e|database-vv)\.yml@[0-9a-f]{7,40}"
)


class TestingWorkflowPinTest(unittest.TestCase):
    def test_e2e_and_database_callers_pin_notaire_testing(self) -> None:
        for path in (E2E_CALLER, DB_CALLER):
            text = path.read_text(encoding="utf-8")
            self.assertIsNotNone(PIN.search(text), f"{path.name} must pin notaire-testing by SHA")
            self.assertIn("app-ref:", text)
            self.assertIn("testing-ref:", text)

    def test_local_testing_tree_is_stub_only(self) -> None:
        self.assertTrue((TESTING / "README.md").is_file())
        for forbidden in ("e2e", "database", "integration", "scripts", "tools", "verify.sh"):
            self.assertFalse(
                (TESTING / forbidden).exists(),
                f"testing/{forbidden} must not be vendored after the extract",
            )
        # e2e-swing/ may keep a hard-deprecation README stub (#811 / #2514).
        readme = (TESTING / "README.md").read_text(encoding="utf-8")
        self.assertIn("matiaspakua/notaire-testing", readme)


if __name__ == "__main__":
    unittest.main()
