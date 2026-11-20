#!/usr/bin/env python3
"""The packaged application must not let an env var change Hibernate ddl-auto (#583)."""

from __future__ import annotations

import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
APPLICATION = REPO_ROOT / "backend-api" / "src" / "main" / "resources" / "application.properties"


class DdlAutoPinnedTest(unittest.TestCase):
    def test_application_properties_pins_ddl_auto_none(self) -> None:
        text = APPLICATION.read_text(encoding="utf-8")
        self.assertIn(
            "spring.jpa.hibernate.ddl-auto=none",
            text,
            "the packaged application must set ddl-auto=none (#583)",
        )
        self.assertNotIn(
            "SPRING_JPA_HIBERNATE_DDL_AUTO",
            text,
            "an environment variable must not be able to switch ddl-auto (#583)",
        )


if __name__ == "__main__":
    unittest.main()
