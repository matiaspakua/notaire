#!/usr/bin/env python3
"""Guard: Flyway docs must not hard-code the stale V1→V11+ range (#1980)."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STALE = "V1→V11+"

PATHS = [
    ROOT / "backend-api/src/main/resources/db/migration/README.md",
    ROOT / ".claude/rules/database-migrations.md",
    ROOT / "backend-api/src/main/resources/application.properties",
]


class FlywayDocsNoStaleVersionRangeTest(unittest.TestCase):
    def test_flyway_docs_omit_stale_v1_to_v11_range(self):
        for path in PATHS:
            with self.subTest(path=str(path.relative_to(ROOT))):
                text = path.read_text(encoding="utf-8")
                self.assertNotIn(
                    STALE,
                    text,
                    f"{path.relative_to(ROOT)} still contains {STALE!r}",
                )

    def test_migration_readme_points_to_pages_and_explains_rollbacks(self):
        readme = (ROOT / "backend-api/src/main/resources/db/migration/README.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("/docs/database/", readme)
        self.assertIn("R{n}__", readme)
        self.assertNotIn("If the highest is V2, your new migration will be V3", readme)
        self.assertNotIn("| V1 | `V1__initial_schema.sql`", readme)


if __name__ == "__main__":
    unittest.main()
