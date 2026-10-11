#!/usr/bin/env python3
"""Security gates stay enabled locally and in CI, and the report is on the infra hub.

Issue #1506, CU78.
Run with: python3 security/tests/test_security_gate_surface.py
"""

from __future__ import annotations

import json
import unittest
from pathlib import Path

import yaml

REPO = Path(__file__).resolve().parents[2]
CI = REPO / ".github" / "workflows" / "ci.yml"
CODEQL = REPO / ".github" / "workflows" / "codeql.yml"
ZAP = REPO / ".github" / "workflows" / "dast-zap.yml"
PIPELINE = REPO / "workspace" / "sdlc" / "run_pipeline.sh"
PREFLIGHT = REPO / "workspace" / "sdlc" / "preflight.sh"
COMPOSE = REPO / "infra" / "observability" / "docker-compose.yml"
HOMER = REPO / "infra" / "observability" / "dashboard" / "config.yml"
PROMETHEUS = REPO / "infra" / "observability" / "prometheus" / "prometheus.yml"
STACK = REPO / "infra" / "observability" / "grafana" / "provisioning" / "dashboards" / "notaire-stack-status.json"
SUMMARY = REPO / "security" / "publish_summary.py"
PAGE = REPO / "infra" / "observability" / "security-reports" / "index.html"


def load_workflow(path: Path) -> dict:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


class GatePresenceTest(unittest.TestCase):
    def test_local_pipeline_runs_the_catalog_and_the_dast_baseline(self):
        pipeline = PIPELINE.read_text(encoding="utf-8")
        preflight = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("python3 security/run_gates.py --profile local --skip-dast", pipeline)
        self.assertIn("python3 security/run_gates.py --profile local --only dast", pipeline)
        self.assertIn("python3 security/run_gates.py --profile local --skip-dast", preflight)
        self.assertNotIn("continue-on-error", pipeline)

    def test_ci_security_job_is_enabled_without_the_java_build(self):
        workflow = load_workflow(CI)
        job = workflow["jobs"]["security"]
        needs = job["needs"]
        if isinstance(needs, str):
            needs = [needs]
        self.assertIn("changes", needs)
        self.assertNotIn("build", needs)
        condition = str(job.get("if", ""))
        self.assertNotIn("false", condition)
        text = CI.read_text(encoding="utf-8")
        for step in ("python3 security/scan_secrets.py", "python3 security/run_gates.py --profile ci"):
            index = text.index(step)
            window = text[max(0, index - 200) : index + len(step) + 80]
            self.assertNotIn("continue-on-error", window, step)
        self.assertIn("aquasecurity/trivy-action", text)
        self.assertIn("GITHUB_STEP_SUMMARY", text)
        self.assertIn("security-case-report", text)

    def test_codeql_and_zap_workflows_are_enabled(self):
        codeql = load_workflow(CODEQL)
        self.assertIn("pull_request", codeql["on"])
        self.assertIn("schedule", codeql["on"])
        codeql_text = CODEQL.read_text(encoding="utf-8")
        self.assertIn("github/codeql-action/analyze", codeql_text)
        self.assertNotIn("if: false", codeql_text)
        zap = load_workflow(ZAP)
        self.assertIn("schedule", zap["on"])
        zap_text = ZAP.read_text(encoding="utf-8")
        self.assertIn("zaproxy/action-baseline", zap_text)
        self.assertNotIn("if: false", zap_text)

    def test_summary_renders_case_status_without_secret_values(self):
        module_path = SUMMARY
        self.assertTrue(module_path.is_file(), "publish_summary.py")
        import importlib.util
        import sys

        spec = importlib.util.spec_from_file_location("publish_summary", module_path)
        module = importlib.util.module_from_spec(spec)
        sys.modules["publish_summary"] = module
        spec.loader.exec_module(module)
        rendered = module.render(
            {
                "profile": "ci",
                "summary": {"pass": 1, "fail": 0, "skip": 0},
                "cases": [
                    {
                        "id": "SEC-SEC-01",
                        "status": "PASS",
                        "family": "secrets",
                        "tool": "security/scan_secrets.py",
                        "detail": "no high-confidence secret",
                    }
                ],
            }
        )
        self.assertIn("SEC-SEC-01", rendered)
        self.assertIn("PASS", rendered)
        self.assertNotIn("ghp_", rendered)


class InfraReportServiceTest(unittest.TestCase):
    def test_homer_and_compose_publish_the_report_service(self):
        compose = yaml.safe_load(COMPOSE.read_text(encoding="utf-8"))
        service = compose["services"]["security-reports"]
        self.assertIn("8890:80", service["ports"])
        homer = HOMER.read_text(encoding="utf-8")
        self.assertIn("http://localhost:8890", homer)
        prometheus = PROMETHEUS.read_text(encoding="utf-8")
        self.assertIn("service: 'security-reports'", prometheus)
        dashboard = json.loads(STACK.read_text(encoding="utf-8"))
        blob = json.dumps(dashboard["panels"])
        self.assertIn('service=\\"security-reports\\"', blob)
        self.assertTrue(PAGE.is_file())
        page = PAGE.read_text(encoding="utf-8")
        self.assertIn("reports/latest/index.html", page)
        self.assertIn("Security", page)


if __name__ == "__main__":
    unittest.main()
