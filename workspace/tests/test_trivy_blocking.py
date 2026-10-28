#!/usr/bin/env python3
"""Guards the blocking Trivy filesystem scan (#712, CU78)."""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CI_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "ci.yml")
RUN_GATES = os.path.join(REPO_ROOT, "security", "run_gates.py")


def trivy_steps() -> list[dict]:
    with open(CI_WORKFLOW, encoding="utf-8") as handle:
        workflow = yaml.safe_load(handle)
    steps = []
    for job in workflow["jobs"].values():
        for step in job.get("steps", []):
            if str(step.get("uses", "")).startswith("aquasecurity/trivy-action"):
                steps.append(step)
    if not steps:
        raise AssertionError("ci.yml has no trivy-action step")
    return steps


def trivy_fs_step() -> dict:
    for step in trivy_steps():
        if step.get("with", {}).get("scan-type") == "fs":
            return step
    raise AssertionError("security job has no filesystem trivy-action step")


def trivy_image_step() -> dict:
    for step in trivy_steps():
        if step.get("with", {}).get("scan-type") == "image":
            return step
    raise AssertionError("docker-build job has no image trivy-action step")


class TrivyBlockingTest(unittest.TestCase):
    def test_filesystem_scan_fails_the_build_on_high_and_critical(self):
        step = trivy_fs_step()["with"]
        self.assertEqual(str(step["exit-code"]), "1")
        self.assertEqual(step["severity"], "CRITICAL,HIGH")

    def test_filesystem_scan_ignores_unfixed_findings(self):
        self.assertTrue(trivy_fs_step()["with"]["ignore-unfixed"])

    def test_results_are_kept_when_the_scan_fails(self):
        with open(CI_WORKFLOW, encoding="utf-8") as handle:
            steps = yaml.safe_load(handle)["jobs"]["security"]["steps"]
        after = steps[steps.index(trivy_fs_step()) + 1:]
        for step in after:
            self.assertEqual(step.get("if"), "always()", step.get("name"))

    def test_no_blanket_trivyignore(self):
        path = os.path.join(REPO_ROOT, ".trivyignore")
        if not os.path.exists(path):
            return
        with open(path, encoding="utf-8") as handle:
            lines = [l.rstrip() for l in handle if l.strip()]
        for index, line in enumerate(lines):
            if not line.startswith("#"):
                self.assertTrue(lines[index - 1].startswith("#"), f"{line} has no justification")

    def test_local_gate_blocks_on_the_same_severities(self):
        with open(RUN_GATES, encoding="utf-8") as handle:
            text = handle.read()
        self.assertIn('"CRITICAL,HIGH"', text)
        self.assertNotIn("report-only until #282", text)

    def test_image_scan_fails_on_critical(self):
        step = trivy_image_step()["with"]
        self.assertEqual(str(step["exit-code"]), "1")
        self.assertEqual(step["severity"], "CRITICAL")


if __name__ == "__main__":
    unittest.main()
