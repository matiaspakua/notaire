"""A run started from the CLI carries its intent (#2426)."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class RunStartIntentTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        steps = [
            ("catalog", "add", "--kind", "agents", "--id", "implementer", "--ref", ".aisdlc/agents/x", "--summary", "x"),
            ("workflow", "create", "--id", "feature", "--summary", "v1", "--agent", "implementer"),
            ("migrate", "--json"),
            ("workflow", "transition", "--id", "feature", "--action", "define"),
            ("workflow", "transition", "--id", "feature", "--action", "request_validation", "--waive-reason", "unit"),
            ("workflow", "transition", "--id", "feature", "--action", "approve", "--owner-override", "--reason", "unit"),
            ("project", "create", "--id", "web", "--repo-id", "web", "--url", "https://example.invalid/web",
             "--forge", "github"),
        ]
        for args in steps:
            result = self.repo.run(*args)
            self.assertEqual(0, result.returncode, output(result))
        submitted = self.repo.run("intent", "submit", "--text", "Fix the pages metrics", "--json")
        self.assertEqual(0, submitted.returncode, output(submitted))
        self.intent = json.loads(submitted.stdout)["id"]

    def test_the_run_records_its_intent(self):
        started = self.repo.run("run", "start", "--project", "web", "--workflow", "feature", "--id", "r1",
                                "--intent", self.intent, "--json")
        self.assertEqual(0, started.returncode, output(started))
        self.assertEqual(self.intent, json.loads(started.stdout)["intent"])
        shown = json.loads(self.repo.run("intent", "show", self.intent, "--json").stdout)
        self.assertIn("r1", shown.get("run_ids", []))
        self.repo.run("harness", "report", "--run", "r1", "--adapter", "local-worker", "--event", "run.started")
        status = json.loads(self.repo.run("status", "--json").stdout)
        self.assertEqual(self.intent, status["intent"])

    def test_an_existing_run_is_attached_to_an_intent(self):
        self.assertEqual(0, self.repo.run("run", "start", "--project", "web", "--workflow", "feature",
                                          "--id", "r3").returncode)
        attached = self.repo.run("intent", "attach", "--id", self.intent, "--run", "r3", "--json")
        self.assertEqual(0, attached.returncode, output(attached))
        run = json.loads(self.repo.run("run", "show", "--id", "r3", "--json").stdout)
        self.assertEqual(self.intent, run["intent"])
        shown = json.loads(self.repo.run("intent", "show", self.intent, "--json").stdout)
        self.assertIn("r3", shown["run_ids"])
        other = json.loads(self.repo.run("intent", "submit", "--text", "Another goal", "--json").stdout)["id"]
        moved = self.repo.run("intent", "attach", "--id", other, "--run", "r3")
        self.assertEqual(2, moved.returncode)
        self.assertIn("already attached", output(moved))

    def test_an_unknown_intent_is_refused(self):
        started = self.repo.run("run", "start", "--project", "web", "--workflow", "feature", "--id", "r2",
                                "--intent", "int-000000000000")
        self.assertEqual(2, started.returncode, output(started))
        self.assertIn("intent not found", output(started))
        listed = json.loads(self.repo.run("run", "list", "--json").stdout)
        self.assertNotIn("r2", [r["id"] for r in listed.get("entries", [])])


if __name__ == "__main__":
    unittest.main()
