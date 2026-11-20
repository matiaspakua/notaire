"""Entity toolset: list, visualize, add, edit, delete, apply, evolve."""
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

[[gates]]
name = "plan gate"
stage = "plan"
run = "true"

[[stages]]
id = "plan"
skills = ["review"]
mcp = ["repolens"]
"""


class EntityToolsetTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def test_factory_default_mcp_is_listed_without_a_project_copy(self):
        result = self.repo.run("entity", "list", "--kind", "mcp", "--scope", "factory", "--json")
        self.assertEqual(0, result.returncode, output(result))
        rows = json.loads(result.stdout)
        self.assertEqual("repolens", rows[0]["id"])
        self.assertEqual("factory", rows[0]["source"])
        self.assertEqual("repolens", rows[0]["command"])
        self.assertNotIn("notaire", json.dumps(rows).lower())

    def test_add_is_idempotent_and_edit_keeps_history(self):
        first = self.repo.run(
            "entity", "add", "--kind", "prompts", "--id", "locate", "--summary", "How to locate code", "--json",
        )
        self.assertEqual(0, first.returncode, output(first))
        self.assertEqual(1, json.loads(first.stdout)["version"])
        again = self.repo.run(
            "entity", "add", "--kind", "prompts", "--id", "locate", "--summary", "How to locate code", "--json",
        )
        self.assertEqual(0, again.returncode, output(again))
        self.assertTrue(json.loads(again.stdout)["idempotent"])
        edited = self.repo.run(
            "entity", "edit", "--kind", "prompts", "--id", "locate", "--summary", "Shorter locate prompt",
            "--if-match", "1", "--json",
        )
        self.assertEqual(0, edited.returncode, output(edited))
        self.assertEqual(2, json.loads(edited.stdout)["version"])
        history = self.repo.run("catalog", "history", "--kind", "prompts", "--id", "locate", "--json")
        versions = [row["version"] for row in json.loads(history.stdout)["versions"]]
        self.assertEqual([1, 2], versions)

    def test_dry_run_add_does_not_write(self):
        result = self.repo.run(
            "entity", "add", "--kind", "rules", "--id", "brief", "--summary", "Be brief", "--dry-run", "--json",
        )
        self.assertEqual(0, result.returncode, output(result))
        self.assertTrue(json.loads(result.stdout)["dry_run"])
        listed = self.repo.run("entity", "list", "--kind", "rules", "--scope", "project", "--json")
        self.assertEqual([], json.loads(listed.stdout))

    def test_delete_refuses_a_referenced_skill_until_forced(self):
        added = self.repo.run("entity", "add", "--kind", "skills", "--id", "review", "--summary", "Review a change")
        self.assertEqual(0, added.returncode, output(added))
        blocked = self.repo.run("entity", "delete", "--kind", "skills", "--id", "review", "--json")
        self.assertEqual(1, blocked.returncode, output(blocked))
        self.assertIn("stage:plan", blocked.stdout)
        forced = self.repo.run("entity", "delete", "--kind", "skills", "--id", "review", "--force", "--json")
        self.assertEqual(0, forced.returncode, output(forced))
        self.assertTrue(json.loads(forced.stdout)["deleted"])

    def test_apply_is_idempotent_and_visualize_shows_the_stage_edge(self):
        self.repo.run("entity", "add", "--kind", "skills", "--id", "review", "--summary", "Review")
        dry = self.repo.run("entity", "apply", "--dry-run", "--json")
        self.assertEqual(0, dry.returncode, output(dry))
        self.assertFalse(json.loads(dry.stdout)["in_sync"])
        applied = self.repo.run("entity", "apply", "--json")
        self.assertEqual(0, applied.returncode, output(applied))
        cursor = json.loads((self.repo.root / ".cursor" / "mcp.json").read_text(encoding="utf-8"))
        self.assertEqual(["mcp"], cursor["mcpServers"]["repolens"]["args"])
        again = self.repo.run("entity", "apply", "--dry-run", "--json")
        self.assertTrue(json.loads(again.stdout)["in_sync"], output(again))
        graph = self.repo.run("entity", "visualize", "--json")
        data = json.loads(graph.stdout)
        self.assertIn({"from": "stage:plan", "rel": "uses", "to": "skills:review"}, data["edges"])
        self.assertIn("stage_plan", data["mermaid"])

    def test_evolve_records_rationale_and_rollback_is_a_new_version(self):
        self.repo.run("entity", "add", "--kind", "agents", "--id", "reviewer", "--summary", "Reviews changes")
        evolved = self.repo.run(
            "entity", "evolve", "--kind", "agents", "--id", "reviewer",
            "--summary", "Reviews with a checklist", "--rationale", "checklist reduced misses", "--json",
        )
        self.assertEqual(0, evolved.returncode, output(evolved))
        body = json.loads(evolved.stdout)
        self.assertEqual(2, body["version"])
        self.assertEqual("checklist reduced misses", body["evolution"]["rationale"])
        rolled = self.repo.run(
            "entity", "evolve", "--kind", "agents", "--id", "reviewer", "--rollback", "1", "--json",
        )
        self.assertEqual(0, rolled.returncode, output(rolled))
        restored = json.loads(rolled.stdout)
        self.assertEqual(3, restored["version"])
        self.assertEqual("Reviews changes", restored["summary"])
        self.assertEqual(1, restored["evolution"]["rollback_to"])

    def test_invalid_scope_is_rejected(self):
        # scope is not a CLI flag; a direct library call covers the schema.
        from aisdlc.entities import validate_fields
        from aisdlc.errors import KitError

        with self.assertRaises(KitError):
            validate_fields("rules", {"id": "x", "kind": "rules", "summary": "ok", "scope": "nope"})


if __name__ == "__main__":
    unittest.main()
