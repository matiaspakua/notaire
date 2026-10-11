#!/usr/bin/env python3
"""
Guards the Grafana overview dashboards and the availability probes behind them
(issue #1408, CU77). Spec: docs/openspec/changes/add-grafana-status-dashboards/.

Plain stdlib unittest, like the other infra config guards.
Run with: python3 infra/tests/test_grafana_status_dashboards.py
"""
import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBSERVABILITY = os.path.join(REPO_ROOT, "infra", "observability")
DASHBOARDS = os.path.join(OBSERVABILITY, "grafana", "provisioning", "dashboards")

STACK_UID = "notaire-stack-status"
NOTAIRE_UID = "notaire-status"
SPECIALIST_UIDS = {"notaire-backend", "notaire-postgres", "notaire-auth", "notaire-logs"}
NOTAIRE_SERVICES = {"frontend", "backend", "postgres", "pgadmin"}
STACK_SERVICES = NOTAIRE_SERVICES | {
    "prometheus", "grafana", "loki", "promtail", "sonarqube", "sonar-db", "homer",
    "security-reports",
}
DATASOURCE_UIDS = {"prometheus", "loki"}


def load_yaml(*parts):
    with open(os.path.join(OBSERVABILITY, *parts), encoding="utf-8") as f:
        return yaml.safe_load(f)


def load_dashboard(uid):
    with open(os.path.join(DASHBOARDS, uid + ".json"), encoding="utf-8") as f:
        return json.load(f)


def targets(dashboard):
    return [t for p in dashboard["panels"] for t in p.get("targets", [])]


def exprs(dashboard):
    return [t.get("expr", "") for t in targets(dashboard)]


def probed_services():
    scrape_configs = load_yaml("prometheus", "prometheus.yml")["scrape_configs"]
    services = set()
    for job in scrape_configs:
        if job.get("metrics_path") != "/probe":
            continue
        for static in job["static_configs"]:
            services.add(static["labels"]["service"])
    return services


class ProbeTest(unittest.TestCase):
    def test_probe_targets_cover_stack_services(self):
        self.assertEqual(STACK_SERVICES, probed_services())

    def test_probe_module_reports_failure(self):
        compose = load_yaml("docker-compose.yml")
        self.assertEqual(
            "prom/blackbox-exporter:v0.28.0", compose["services"]["blackbox-exporter"]["image"]
        )
        modules = load_yaml("prometheus", "blackbox.yml")["modules"]
        self.assertIn("http_2xx", modules)
        self.assertIn("tcp_connect", modules)
        rules = load_yaml("prometheus", "alert-rules.yml")["groups"]
        alerts = {r["alert"]: r["expr"] for g in rules for r in g["rules"] if "alert" in r}
        self.assertIn("probe_success", alerts.get("ServiceDown", ""))

    def test_prometheus_mounts_alert_rules(self):
        volumes = load_yaml("docker-compose.yml")["services"]["prometheus"]["volumes"]
        self.assertTrue(any("alert-rules.yml:/etc/prometheus/alert-rules.yml" in v for v in volumes))

    def test_probe_jobs_relabel_and_carry_no_credentials(self):
        scrape_configs = load_yaml("prometheus", "prometheus.yml")["scrape_configs"]
        jobs = [j for j in scrape_configs if j.get("metrics_path") == "/probe"]
        self.assertTrue(jobs)
        for job in jobs:
            self.assertNotIn("basic_auth", job)
            self.assertIn("module", job["params"])


class DashboardTest(unittest.TestCase):
    def test_stack_dashboard_has_availability_and_alert_panels(self):
        dashboard = load_dashboard(STACK_UID)
        self.assertEqual(STACK_UID, dashboard["uid"])
        joined = "\n".join(exprs(dashboard))
        self.assertIn("probe_success", joined)
        self.assertIn("ALERTS{alertstate=\"firing\"", joined)
        text = json.dumps(dashboard["panels"])
        for service in STACK_SERVICES:
            self.assertIn(f'service=\\"{service}\\"', text)

    def test_stack_dashboard_has_error_and_alert_queries(self):
        queries = exprs(load_dashboard(STACK_UID))
        self.assertTrue(any("ALERTS" in q for q in queries))
        self.assertTrue(any(q.startswith("{") and "ERROR" in q for q in queries))

    def test_overview_queries_have_no_data_safe_settings(self):
        for uid in (STACK_UID, NOTAIRE_UID):
            dashboard = load_dashboard(uid)
            for panel in dashboard["panels"]:
                if panel["type"] in ("logs", "row", "text"):
                    continue
                defaults = panel["fieldConfig"]["defaults"]
                self.assertIn("noValue", defaults, f"{uid}: {panel['title']}")
                if panel["type"] == "stat" and "probe_success{service=" in panel["targets"][0]["expr"]:
                    self.assertTrue(
                        defaults.get("mappings"), f"{uid}: {panel['title']} needs value mappings"
                    )

    def test_notaire_dashboard_covers_application_services(self):
        dashboard = load_dashboard(NOTAIRE_UID)
        self.assertEqual(NOTAIRE_UID, dashboard["uid"])
        text = json.dumps(dashboard["panels"])
        for service in NOTAIRE_SERVICES:
            self.assertIn(f'service=\\"{service}\\"', text)

    def test_notaire_dashboard_uses_probe_statuses(self):
        queries = exprs(load_dashboard(NOTAIRE_UID))
        self.assertGreaterEqual(sum("probe_success" in q for q in queries), len(NOTAIRE_SERVICES))
        self.assertTrue(any("ALERTS" in q for q in queries))
        self.assertTrue(any(q.startswith("{") and "ERROR" in q for q in queries))

    def test_overview_dashboards_link_to_specialists(self):
        for uid in (STACK_UID, NOTAIRE_UID):
            linked = {
                link["url"].split("/d/")[1].split("/")[0].split("?")[0]
                for link in load_dashboard(uid)["links"]
            }
            self.assertTrue(SPECIALIST_UIDS <= linked, f"{uid} links {linked}")

    def test_dashboards_use_provisioned_datasource_uids(self):
        for uid in (STACK_UID, NOTAIRE_UID):
            for target in targets(load_dashboard(uid)):
                self.assertIn(target["datasource"]["uid"], DATASOURCE_UIDS)

    def test_specialist_dashboards_are_preserved(self):
        for uid in SPECIALIST_UIDS:
            self.assertEqual(uid, load_dashboard(uid)["uid"])


if __name__ == "__main__":
    unittest.main()
