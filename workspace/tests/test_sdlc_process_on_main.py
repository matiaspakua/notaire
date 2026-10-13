#!/usr/bin/env python3
"""Guards #1622 (CU76): process self-tests and the OpenSpec inventory also run on main.

On 2026-10-13 `main` failed two self-tests while every main workflow was green,
because `sdlc-process.yml` ran only on pull requests.
"""

from __future__ import annotations

import unittest
from pathlib import Path

import yaml

WORKFLOW = Path(__file__).resolve().parents[2] / ".github" / "workflows" / "sdlc-process.yml"


def load() -> dict:
    data = yaml.safe_load(WORKFLOW.read_text(encoding="utf-8"))
    data["on"] = data.pop(True, data.get("on"))
    return data


def run_lines(job: dict) -> str:
    return "\n".join(step.get("run", "") for step in job.get("steps", []))


class SdlcProcessOnMainTest(unittest.TestCase):
    def setUp(self):
        self.workflow = load()

    def test_runs_on_push_to_main_daily_and_on_demand(self):
        triggers = self.workflow["on"]
        self.assertIn("main", triggers["push"]["branches"])
        self.assertTrue(triggers["schedule"])
        self.assertIn("workflow_dispatch", triggers)

    def test_pull_request_job_stays_pull_request_only(self):
        job = self.workflow["jobs"]["process-checks"]
        self.assertIn("pull_request", job.get("if", ""))

    def test_main_job_runs_self_tests_and_strict_inventory(self):
        job = self.workflow["jobs"]["main-health"]
        self.assertEqual(job.get("if"), "github.event_name != 'pull_request'")
        commands = run_lines(job)
        for suite in ("local-ai/sdlc/tests", "workspace/tests", "security/tests", "docs/tests"):
            self.assertIn(f"unittest discover -s {suite}", commands)
        self.assertIn("check-openspec-archive-hygiene.py --inventory-only", commands)
        self.assertIn("validate-sdlc-plan.sh", commands)


if __name__ == "__main__":
    unittest.main()
