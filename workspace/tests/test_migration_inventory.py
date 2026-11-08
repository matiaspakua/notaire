"""The adopter inventory stays honest (#2121, CU76)."""
from __future__ import annotations

import subprocess
import sys
import tomllib
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
INVENTORY = ROOT / ".aisdlc" / "migration-inventory.toml"


class MigrationInventoryTest(unittest.TestCase):
    def test_cli_accepts_the_adopter_inventory(self):
        result = subprocess.run(
            [sys.executable, "framework/ai-sdlc/bin/aisdlc", "--profile", ".aisdlc/aisdlc.toml", "inventory", "check"],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_product_contracts_stay_and_the_move_is_not_finished(self):
        data = tomllib.loads(INVENTORY.read_text(encoding="utf-8"))
        rows = {row["source"]: row for row in data["rows"]}
        self.assertEqual("framework/ai-sdlc", data["canonical_root"])
        self.assertEqual("stay", rows["backend-api/openapi/openapi.yaml"]["class"])
        self.assertEqual("stay", rows["frontend/src"]["status"])
        self.assertTrue(rows["docs/300-development/303-testing/INVENTORY.md"]["hotspot"])
        self.assertEqual("planned", rows["framework/ai-sdlc/plugins/planned/spec-kit.toml"]["status"])
        self.assertEqual("planned", rows["local-ai/sdlc"]["status"])
        statuses = {row["status"] for row in data["rows"]}
        self.assertIn("migrated", statuses)
        self.assertIn("planned", statuses)


if __name__ == "__main__":
    unittest.main()
