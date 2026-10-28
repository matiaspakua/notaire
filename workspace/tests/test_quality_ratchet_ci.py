#!/usr/bin/env python3
"""Guards Checkstyle/SpotBugs ratchet wiring in CI workflows (#566)."""
import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CI_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "ci.yml")
PR_VALIDATION = os.path.join(REPO_ROOT, ".github", "workflows", "pr-validation.yml")
BASELINE = os.path.join(REPO_ROOT, "workspace", "ci", "quality-ratchet-baseline.json")
ENFORCER = os.path.join(REPO_ROOT, "workspace", "ci", "enforce-quality-ratchet.py")


def load_workflow(name: str) -> dict:
    with open(os.path.join(REPO_ROOT, ".github", "workflows", name), encoding="utf-8") as handle:
        workflow = yaml.safe_load(handle)
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def step_runs(job: dict, needle: str) -> str | None:
    for step in job.get("steps", []):
        run = step.get("run")
        if isinstance(run, str) and needle in run:
            return run
    return None


class QualityRatchetCiTest(unittest.TestCase):
    def test_baseline_records_current_floors(self):
        with open(BASELINE, encoding="utf-8") as handle:
            data = json.load(handle)
        self.assertGreaterEqual(data["checkstyle_max_violations"], 1)
        self.assertGreaterEqual(data["spotbugs_max_bug_instances"], 0)
        self.assertEqual(data["zero_debt_issues"]["checkstyle"], 710)
        self.assertEqual(data["zero_debt_issues"]["spotbugs"], 711)

    def test_pr_validation_checkstyle_uses_ratchet_enforcer(self):
        lint_job = load_workflow("pr-validation.yml")["jobs"]["lint"]
        run = step_runs(lint_job, "enforce-quality-ratchet.py")
        self.assertIsNotNone(run)
        self.assertNotIn("|| true", run)

    def test_ci_quality_job_enforces_spotbugs_ratchet(self):
        quality_job = load_workflow("ci.yml")["jobs"]["quality"]
        run = step_runs(quality_job, "enforce-quality-ratchet.py")
        self.assertIsNotNone(run)
        self.assertIn("spotbugs", run)

    def test_ci_coverage_verify_passes_checkstyle_ratchet_properties(self):
        coverage_job = load_workflow("ci.yml")["jobs"]["coverage"]
        run = step_runs(coverage_job, "mvn verify")
        self.assertIsNotNone(run)
        self.assertIn("checkstyle.maxAllowedViolations", run)
        self.assertIn("checkstyle.failOnViolation=true", run)

    def test_enforcer_script_exists(self):
        self.assertTrue(os.path.isfile(ENFORCER))


if __name__ == "__main__":
    unittest.main()
