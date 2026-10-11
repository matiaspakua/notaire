#!/usr/bin/env python3
"""
Guards the drill-down dashboards and the scrape targets behind them (issue #1467, CU77).
Spec: docs/openspec/changes/feat-1467-infra-dashboards/.

Run with: python3 infra/tests/test_infra_dashboards.py
"""
import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBS = os.path.join(REPO_ROOT, "infra", "observability")
DASHBOARDS = os.path.join(OBS, "grafana", "provisioning", "dashboards")

NEW_UIDS = {
    "notaire-infra-resources", "notaire-frontend", "notaire-observability-health", "notaire-traces",
}
EXISTING_UIDS = {
    "notaire-backend", "notaire-postgres", "notaire-auth", "notaire-logs",
    "notaire-stack-status", "notaire-status", "notaire-security",
}
DATASOURCE_UIDS = {"prometheus", "loki", "tempo", "alertmanager"}
TOOL_JOBS = {"tempo": "tempo:3200", "alertmanager": "alertmanager:9093", "promtail": "promtail:9080"}


def load_dashboard(uid):
    with open(os.path.join(DASHBOARDS, uid + ".json"), encoding="utf-8") as f:
        return json.load(f)


def load_yaml(*parts):
    with open(os.path.join(OBS, *parts), encoding="utf-8") as f:
        return yaml.safe_load(f)


def targets(dashboard):
    return [t for p in dashboard["panels"] for t in p.get("targets", [])]


class ToolScrapeTest(unittest.TestCase):
    def test_tool_scrape_jobs_and_collector_telemetry(self):
        jobs = {j["job_name"]: j for j in load_yaml("prometheus", "prometheus.yml")["scrape_configs"]}
        for name, target in TOOL_JOBS.items():
            job = jobs[f"{name}-metrics"]
            static = job["static_configs"][0]
            self.assertEqual([target], static["targets"])
            self.assertEqual(name, static["labels"]["service"])
        telemetry = load_yaml("otel", "collector.yml")["service"]["telemetry"]["metrics"]
        self.assertNotEqual("none", telemetry["level"])
        self.assertEqual("otel-collector-self", jobs["otel-collector-self"]["static_configs"][0]["labels"]["service"])


class DashboardTest(unittest.TestCase):
    def test_dashboards_are_provisioned_and_linked(self):
        for uid in NEW_UIDS:
            dashboard = load_dashboard(uid)
            self.assertEqual(uid, dashboard["uid"])
            linked = {link["url"].split("/d/")[1].split("/")[0] for link in dashboard["links"]}
            self.assertTrue((NEW_UIDS | EXISTING_UIDS) - {uid} <= linked, f"{uid} links {linked}")

    def test_queries_use_provisioned_datasources(self):
        for uid in NEW_UIDS:
            for target in targets(load_dashboard(uid)):
                self.assertIn(target["datasource"]["uid"], DATASOURCE_UIDS, uid)

    def test_value_panels_have_no_data_settings(self):
        for uid in NEW_UIDS:
            for panel in load_dashboard(uid)["panels"]:
                if panel["type"] in ("stat", "gauge", "timeseries", "bargauge"):
                    self.assertIn("noValue", panel["fieldConfig"]["defaults"], f"{uid}: {panel['title']}")

    def test_dashboards_cover_their_subject(self):
        text = {uid: json.dumps(load_dashboard(uid)) for uid in NEW_UIDS}
        self.assertIn("node_cpu_seconds_total", text["notaire-infra-resources"])
        self.assertIn("node_memory_MemAvailable_bytes", text["notaire-infra-resources"])
        self.assertIn("nodejs_eventloop_lag_seconds", text["notaire-frontend"])
        self.assertIn("api_proxy", text["notaire-frontend"])
        for service in ("prometheus", "loki", "tempo", "alertmanager", "promtail"):
            self.assertIn(f'service=\\"{service}\\"', text["notaire-observability-health"])
        self.assertIn("traceqlSearch", text["notaire-traces"])
        self.assertIn("http_request", text["notaire-traces"])

    def test_existing_dashboards_preserved(self):
        for uid in EXISTING_UIDS:
            self.assertEqual(uid, load_dashboard(uid)["uid"])


if __name__ == "__main__":
    unittest.main()
