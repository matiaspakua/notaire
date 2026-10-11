#!/usr/bin/env python3
"""The September 2026 audit index keeps the issue titles recorded on its baseline."""

import unittest
from pathlib import Path

AUDIT = (
    Path(__file__).resolve().parents[1]
    / "github"
    / "PRODUCTION-READINESS-AUDIT-2026-09.md"
)


class AuditIndexCitationsEnglishTest(unittest.TestCase):
    def test_recorded_issue_title_stays(self):
        text = AUDIT.read_text(encoding="utf-8")
        self.assertIn("on `main` @ `5a8fa14`", text)
        self.assertIn("State (at audit)", text)
        self.assertIn(
            "an unused /pagos/params endpoint",
            text,
        )
        self.assertNotIn("/payments/params", text)


if __name__ == "__main__":
    unittest.main()
