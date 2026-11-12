"""schema_version envelope + aisdlc migrate workflow v1→v2 (#2254)."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class SchemaMigrateTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            result = self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
            self.assertEqual(0, result.returncode, output(result))

    def test_entity_carries_schema_version(self):
        shown = self.repo.run("catalog", "show", "--kind", "agents", "--id", "implementer", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        payload = json.loads(shown.stdout)
        self.assertEqual(1, payload["schema_version"])
        self.assertIn("created_at", payload)
        self.assertIn("created_by", payload)

    def test_migrate_upgrades_workflow_v1_to_v2(self):
        created = self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "v1 blueprint",
            "--agent", "implementer", "--skill", "java", "--mcp", "github",
            "--json",
        )
        self.assertEqual(0, created.returncode, output(created))
        before = json.loads(created.stdout)
        self.assertEqual(1, before.get("schema_version", 1))
        self.assertIn("agents", before["body"])

        migrated = self.repo.run("migrate", "--json")
        self.assertEqual(0, migrated.returncode, output(migrated))
        report = json.loads(migrated.stdout)
        self.assertIn("feature-delivery", report.get("upgraded") or report.get("entities") or [])

        shown = self.repo.run("workflow", "show", "--id", "feature-delivery", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        after = json.loads(shown.stdout)
        self.assertEqual(2, after["schema_version"])
        body = after["body"]
        self.assertIn("steps", body)
        self.assertIn("inputs", body)
        self.assertIn("outputs", body)
        step = body["steps"][0]
        self.assertTrue(str(step.get("agent", "")).startswith("agents:implementer@"))
        self.assertTrue(any(str(s).startswith("skills:java@") for s in step.get("skills") or []))

    def test_migrate_is_idempotent_and_records_evidence(self):
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--agent", "implementer", "--skill", "java", "--json",
        )
        first = self.repo.run("migrate", "--json")
        self.assertEqual(0, first.returncode, output(first))
        shown = self.repo.run("workflow", "show", "--id", "feature-delivery", "--json")
        digest = json.loads(shown.stdout)["digest"]

        second = self.repo.run("migrate", "--json")
        self.assertEqual(0, second.returncode, output(second))
        shown2 = self.repo.run("workflow", "show", "--id", "feature-delivery", "--json")
        self.assertEqual(digest, json.loads(shown2.stdout)["digest"])

        ledger = self.repo.ledger()
        migrate_rows = [row for row in ledger if row.get("kind") == "migrate"]
        self.assertGreaterEqual(len(migrate_rows), 1)
        self.assertTrue(all(row.get("ok") for row in migrate_rows))


if __name__ == "__main__":
    unittest.main()
