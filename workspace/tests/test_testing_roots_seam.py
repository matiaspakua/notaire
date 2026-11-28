"""Guards #2514: NOTAIRE_APP_ROOT / NOTAIRE_TESTING_ROOT are honoured by harness scripts."""

from __future__ import annotations

import os
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


class TestingRootsSeamTest(unittest.TestCase):
    def test_preflight_exports_testing_roots(self) -> None:
        text = (ROOT / "workspace" / "sdlc" / "preflight.sh").read_text(encoding="utf-8")
        self.assertIn("NOTAIRE_APP_ROOT", text)
        self.assertIn("NOTAIRE_TESTING_ROOT", text)
        self.assertIn("TESTING_ROOT=", text)

    def test_validate_cu_api_matrix_honours_app_root(self) -> None:
        text = (ROOT / "workspace" / "sdlc" / "validate-cu-api-matrix.py").read_text(encoding="utf-8")
        self.assertIn("NOTAIRE_APP_ROOT", text)

    def test_validate_testing_inventory_honours_testing_root(self) -> None:
        text = (ROOT / "workspace" / "sdlc" / "validate-testing-inventory.py").read_text(
            encoding="utf-8"
        )
        self.assertIn("NOTAIRE_TESTING_ROOT", text)


if __name__ == "__main__":
    unittest.main()
