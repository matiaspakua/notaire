#!/usr/bin/env python3
"""The local native helper must not hide missing Flyway migrations (#583)."""

from __future__ import annotations

import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
NATIVE = REPO_ROOT / "workspace" / "stack" / "native.sh"


class NativeDdlAutoTest(unittest.TestCase):
    def test_native_helper_does_not_update_the_schema(self) -> None:
        text = NATIVE.read_text(encoding="utf-8")
        self.assertNotIn(
            "ddl-auto=update",
            text,
            "native.sh must not start Hibernate with ddl-auto=update (#583)",
        )
        self.assertIn(
            "ddl-auto=none",
            text,
            "native.sh must start the backend with ddl-auto=none (#583)",
        )


if __name__ == "__main__":
    unittest.main()
