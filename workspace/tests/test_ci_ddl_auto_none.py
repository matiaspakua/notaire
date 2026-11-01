#!/usr/bin/env python3
"""CI must not hide missing Flyway migrations with Hibernate ddl-auto=update (#1979)."""

from __future__ import annotations

import os
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
WORKFLOWS = REPO_ROOT / ".github" / "workflows"

# Jobs that start the real backend against Flyway (issue #1979).
GUARDED_WORKFLOWS = (
    "dast-zap.yml",
    "performance-test.yml",
    "playwright-e2e.yml",
)


class CiDdlAutoNoneTest(unittest.TestCase):
    def test_guarded_workflows_do_not_set_ddl_auto_update(self) -> None:
        offenders: list[str] = []
        for name in GUARDED_WORKFLOWS:
            text = (WORKFLOWS / name).read_text(encoding="utf-8")
            if "spring.jpa.hibernate.ddl-auto=update" in text or "ddl-auto=update" in text:
                offenders.append(name)
        self.assertEqual(
            [],
            offenders,
            "E2E/DAST/k6 must use ddl-auto=none (or validate), never update — ADR-007 / #1979",
        )

    def test_guarded_workflows_pin_ddl_auto_none_or_validate(self) -> None:
        for name in GUARDED_WORKFLOWS:
            text = (WORKFLOWS / name).read_text(encoding="utf-8")
            self.assertTrue(
                "ddl-auto=none" in text or "ddl-auto=validate" in text,
                f"{name} must explicitly set ddl-auto=none or validate (#1979)",
            )


if __name__ == "__main__":
    unittest.main()
