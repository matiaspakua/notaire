"""Run / RunRequest evidence-derived status (#2255)."""
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


class RunsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
        self.repo.run(
            "project", "create", "--id", "alpha",
            "--profile-ref", "aisdlc.toml",
            "--repo-id", "notaire",
            "--forge", "github",
            "--url", "https://github.com/example/notaire",
            "--default-branch", "main",
            "--json",
        )
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java", "--mcp", "github",
            "--json",
        )

    def _approve(self, wid: str = "feature-delivery") -> dict:
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", wid, "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))
        shown = self.repo.run("workflow", "show", "--id", wid, "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        return json.loads(shown.stdout)

    def test_start_run_requires_approved_workflow(self):
        started = self.repo.run(
            "run", "start",
            "--project", "alpha",
            "--workflow", "feature-delivery",
            "--json",
        )
        self.assertNotEqual(0, started.returncode)
        self.assertIn("workflow_not_approved", output(started))
        ledger = self.repo.ledger()
        self.assertFalse(any(row.get("kind") == "run.request" for row in ledger))

    def test_start_run_appends_run_request_evidence(self):
        wf = self._approve()
        started = self.repo.run(
            "run", "start",
            "--project", "alpha",
            "--workflow", "feature-delivery",
            "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run = json.loads(started.stdout)
        self.assertIn(run["status"], ("requested", "queued"))
        self.assertNotEqual("running", run["status"])
        self.assertEqual(wf["id"], run["workflow"]["id"])
        self.assertEqual(wf["version"], run["workflow"]["version"])
        self.assertEqual(wf["digest"], run["workflow"]["digest"])
        self.assertTrue(run.get("evidence_head"))

        ledger = self.repo.ledger()
        requests = [row for row in ledger if row.get("kind") == "run.request"]
        self.assertEqual(1, len(requests))
        self.assertEqual(run["id"], requests[0]["run_id"])

    def test_status_stays_queued_without_harness_events(self):
        self._approve()
        started = self.repo.run(
            "run", "start", "--project", "alpha", "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]
        shown = self.repo.run("run", "show", "--id", run_id, "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        status = json.loads(shown.stdout)["status"]
        self.assertIn(status, ("requested", "queued"))
        self.assertNotEqual("running", status)

    def test_evidence_verify_covers_new_run_events(self):
        self._approve()
        started = self.repo.run(
            "run", "start", "--project", "alpha", "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]
        report = self.repo.run(
            "harness", "report",
            "--run", run_id,
            "--event", "run.started",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, report.returncode, output(report))
        verify = self.repo.run("evidence", "verify")
        self.assertEqual(0, verify.returncode, output(verify))
        self.assertIn("chained", output(verify))


if __name__ == "__main__":
    unittest.main()
