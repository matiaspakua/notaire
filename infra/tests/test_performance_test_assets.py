#!/usr/bin/env python3
"""
Validates the k6 load-test suite and its CI wiring (issues #594, #1047).

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see testing/tests/test_generate_e2e_coverage_report.py).
Run with: python3 infra/tests/test_performance_test_assets.py
"""
import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
K6_SCRIPT_PATH = os.path.join(REPO_ROOT, "infra", "performance", "k6", "load-test.js")
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

    def test_thresholds_enforce_cu74_slos(self):
        # CU74 objective: p95 < 2s; reliability budget: error rate < 1%.
        duration_match = re.search(
            r"http_req_duration\s*:\s*\[\s*['\"]p\(95\)<(\d+)['\"]",
            self.script,
        )
        self.assertIsNotNone(
            duration_match,
            "http_req_duration must declare a p(95)<Nms threshold",
        )
        p95_ms = int(duration_match.group(1))
        self.assertLessEqual(
            p95_ms,
            2000,
            f"p95 threshold must be ≤2000ms (CU74); found p(95)<{p95_ms}",
        )
        self.assertRegex(
            self.script,
            r"http_req_failed\s*:\s*\[\s*['\"]rate<0\.01['\"]",
            "http_req_failed must enforce rate<0.01",
        )

    def test_authenticates_in_setup_and_reuses_token(self):
        self.assertIn("export function setup()", self.script)
        self.assertIn("/api/v1/usuarios/login", self.script)
        self.assertIn("Authorization", self.script)
        self.assertIn("Bearer", self.script)

    def test_uses_english_login_dto_fields(self):
        self.assertRegex(
            self.script,
            r"JSON\.stringify\(\{\s*name:\s*ADMIN_USER,\s*password:\s*ADMIN_PASSWORD\s*\}\)",
            "login body must use English fields name/password",
        )
        self.assertNotIn("nombre:", self.script)
        self.assertNotIn("contrasenia:", self.script)

    def test_covers_the_highest_traffic_endpoints(self):
        for path in ("/api/v1/gestiones", "/api/v1/presupuestos", "/api/v1/tramites"):
            self.assertIn(path, self.script)

    def test_writes_summary_json_artifact(self):
        self.assertIn("handleSummary", self.script)
        self.assertIn("summary.json", self.script)


class PerformanceTestWorkflowTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        with open(WORKFLOW_PATH, encoding="utf-8") as f:
            cls.raw = f.read()
            cls.workflow = yaml.safe_load(cls.raw)

    def test_is_scheduled_not_gated_on_pull_requests(self):
        triggers = self.workflow.get(True, self.workflow.get("on"))
        self.assertIn("schedule", triggers, "workflow must run on a schedule")
        self.assertIn(
            "workflow_dispatch",
            triggers,
            "workflow must support manual dispatch",
        )
        self.assertNotIn("pull_request", triggers, "load tests must not gate every PR")

    def test_runs_k6_against_a_live_stack(self):
        raw = yaml.dump(self.workflow)
        self.assertIn("k6-action", raw)
        self.assertIn("infra/performance/k6/load-test.js", raw)
        self.assertIn("actuator/health", raw)

    def test_k6_runs_on_the_runner_so_localhost_reaches_the_backend(self):
        steps = self.workflow["jobs"]["load-test"]["steps"]
        uses = [step.get("uses", "") for step in steps]
        self.assertFalse(
            any(u.startswith("grafana/k6-action") for u in uses),
            "the container action cannot reach the backend on the runner's localhost (#1266)",
        )
        self.assertTrue(
            any(u.startswith("grafana/setup-k6-action") for u in uses),
            "install k6 on the runner with grafana/setup-k6-action (#1266)",
        )
        runs = " ".join(step.get("run", "") for step in steps)
        self.assertIn("k6 run", runs)

    def test_uploads_summary_json_artifact(self):
        self.assertIn("summary.json", self.raw)
        self.assertIn("upload-artifact", self.raw)
        self.assertNotRegex(
            self.raw,
            r"if-no-files-found:\s*ignore",
            "upload must not silently ignore a missing summary.json",
        )


if __name__ == "__main__":
    unittest.main()
