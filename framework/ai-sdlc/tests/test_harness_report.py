"""Harness reporting contract — local-worker reference adapter (#2255)."""
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


class HarnessReportTest(unittest.TestCase):
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
            "--profile-ref", "aisdlc.toml", "--json",
        )
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java", "--mcp", "github",
            "--json",
        )
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", "feature-delivery",
                "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))
        started = self.repo.run(
            "run", "start", "--project", "alpha", "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        self.run_id = json.loads(started.stdout)["id"]

    def test_harness_report_moves_run_to_running_then_finished(self):
        started = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.started",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        shown = self.repo.run("run", "show", "--id", self.run_id, "--json")
        self.assertEqual("running", json.loads(shown.stdout)["status"])

        finished = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.finished",
            "--ok", "true",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, finished.returncode, output(finished))
        shown = self.repo.run("run", "show", "--id", self.run_id, "--json")
        self.assertEqual("succeeded", json.loads(shown.stdout)["status"])

    def test_budget_overrun_marks_blocked_skip_never_pass(self):
        self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.started",
            "--adapter", "local-worker",
            "--json",
        )
        skip = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "step.finished",
            "--step", "build",
            "--step-status", "skip",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, skip.returncode, output(skip))
        shown = json.loads(self.repo.run("run", "show", "--id", self.run_id, "--json").stdout)
        step = next(s for s in shown["steps"] if s["id"] == "build")
        self.assertEqual("skip", step["status"])
        self.assertNotEqual("pass", step["status"])

        blocked = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "step.finished",
            "--step", "test",
            "--step-status", "fail",
            "--budget-overrun",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, blocked.returncode, output(blocked))
        shown = json.loads(self.repo.run("run", "show", "--id", self.run_id, "--json").stdout)
        self.assertEqual("blocked", shown["status"])


if __name__ == "__main__":
    unittest.main()
