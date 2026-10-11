#!/usr/bin/env python3
"""
Guards that Homer lists every compose service and every Grafana dashboard (issue #1468, CU77).
Spec: docs/openspec/changes/feat-1468-homer-all-services/.

Run with: python3 infra/tests/test_homer_hub.py
"""
import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBS = os.path.join(REPO_ROOT, "infra", "observability")
DASHBOARDS = os.path.join(OBS, "grafana", "provisioning", "dashboards")

# Compose service -> name of its Homer entry.
HOMER_NAMES = {
    "dashboard": "Homer",
    "security-reports": "Security reports",
    "sonarqube": "SonarQube",
    "sonar-db": "SonarQube database",
    "prometheus": "Prometheus",
    "postgres-exporter": "PostgreSQL exporter",
    "blackbox-exporter": "Blackbox probes",
    "otel-collector": "OTel Collector",
    "alertmanager": "Alertmanager",
    "tempo": "Tempo",
    "node-exporter": "Node exporter",
    "cadvisor": "cAdvisor",
    "grafana": "Grafana",
    "loki": "Loki",
    "promtail": "Promtail",
}
APP_SERVICES = {"Frontend", "Backend API", "pgAdmin"}


def homer_items():
    with open(os.path.join(OBS, "dashboard", "config.yml"), encoding="utf-8") as f:
        config = yaml.safe_load(f)
    return [item for group in config["services"] for item in group["items"]]


class HomerHubTest(unittest.TestCase):
    def test_every_compose_service_is_listed(self):
        with open(os.path.join(OBS, "docker-compose.yml"), encoding="utf-8") as f:
            services = set(yaml.safe_load(f)["services"])
        self.assertEqual(services, set(HOMER_NAMES), "update HOMER_NAMES and the Homer config together")
        names = {item["name"] for item in homer_items()}
        self.assertEqual([], sorted(set(HOMER_NAMES.values()) - names))
        self.assertTrue(APP_SERVICES <= names)

    def test_every_dashboard_and_sonar_project_is_linked(self):
        uids = {f[:-5] for f in os.listdir(DASHBOARDS) if f.endswith(".json")}
        for uid in uids:
            with open(os.path.join(DASHBOARDS, uid + ".json"), encoding="utf-8") as f:
                self.assertEqual(uid, json.load(f)["uid"])
        urls = " ".join(item.get("url", "") for item in homer_items())
        self.assertEqual([], sorted(uid for uid in uids if f"/d/{uid}" not in urls))
        self.assertIn("dashboard?id=notaire-backend", urls)
        self.assertIn("dashboard?id=notaire-frontend", urls)


if __name__ == "__main__":
    unittest.main()
