#!/usr/bin/env python3
"""
Validates the k6 load-test suite and its CI wiring (issue #594).

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see scripts/test_generate_e2e_coverage_report.py).
Run with: python3 scripts/test_performance_test_assets.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
K6_SCRIPT_PATH = os.path.join(REPO_ROOT, "performance-test", "k6", "load-test.js")
WORKFLOW_PATH = os.path.join(REPO_ROOT, ".github", "workflows", "performance-test.yml")


class K6LoadTestScriptTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        with open(K6_SCRIPT_PATH, encoding="utf-8") as f:
            cls.script = f.read()

    def test_imports_k6_http_module(self):
        self.assertIn("from 'k6/http'", self.script)

    def test_declares_stages_and_thresholds(self):
        self.assertIn("stages:", self.script)
        self.assertIn("thresholds:", self.script)
        self.assertIn("http_req_duration", self.script)
        self.assertIn("http_req_failed", self.script)

    def test_authenticates_in_setup_and_reuses_token(self):
        self.assertIn("export function setup()", self.script)
        self.assertIn("/api/v1/usuarios/login", self.script)
        self.assertIn("Authorization", self.script)
        self.assertIn("Bearer", self.script)

    def test_covers_the_highest_traffic_endpoints(self):
        for path in ("/api/v1/gestiones", "/api/v1/presupuestos", "/api/v1/tramites"):
            self.assertIn(path, self.script)


class PerformanceTestWorkflowTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        with open(WORKFLOW_PATH, encoding="utf-8") as f:
            cls.workflow = yaml.safe_load(f)

    def test_is_scheduled_not_gated_on_pull_requests(self):
        triggers = self.workflow.get(True, self.workflow.get("on"))
        self.assertIn("schedule", triggers, "workflow must run on a schedule")
        self.assertNotIn("pull_request", triggers, "load tests must not gate every PR")

    def test_runs_k6_against_a_live_stack(self):
        raw = yaml.dump(self.workflow)
        self.assertIn("k6-action", raw)
        self.assertIn("performance-test/k6/load-test.js", raw)
        self.assertIn("actuator/health", raw)


if __name__ == "__main__":
    unittest.main()
