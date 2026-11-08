#!/usr/bin/env python3
"""
Guards observability dashboard wiring for meaningful panels (#1707, CU77).

Covers: postgres-exporter extend-query load path, Postgres PromQL names,
packaged SEMVER on Micrometer tags + dashboards, Sonar quality dashboard,
and removal of the deprecated Grafana piechart plugin install.

Run with: python3 infra/tests/test_obs_dashboard_wiring.py
"""
from __future__ import annotations

import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBS = os.path.join(REPO_ROOT, "infra", "observability")
DASHBOARDS = os.path.join(OBS, "grafana", "provisioning", "dashboards")
QUERIES = os.path.join(OBS, "prometheus", "postgres_exporter.yml")
COMPOSE = os.path.join(OBS, "docker-compose.yml")
APP_PROPS = os.path.join(
    REPO_ROOT, "backend-api", "src", "main", "resources", "application.properties"
)

BUSINESS_METRICS = ("deeds_total", "budgets_total", "users_total", "people_total")
QUALITY_UID = "notaire-quality"


def load_yaml(path: str):
    with open(path, encoding="utf-8") as handle:
        return yaml.safe_load(handle)


def load_dashboard(uid: str):
    with open(os.path.join(DASHBOARDS, uid + ".json"), encoding="utf-8") as handle:
        return json.load(handle)


def exprs(dashboard: dict) -> list[str]:
    return [
        target.get("expr", "")
        for panel in dashboard.get("panels", [])
        for target in panel.get("targets", [])
    ]


class PostgresExporterWiringTest(unittest.TestCase):
    def test_compose_loads_extend_query_path(self):
        service = load_yaml(COMPOSE)["services"]["postgres-exporter"]
        env = service.get("environment") or {}
        if isinstance(env, list):
            joined = "\n".join(env)
        else:
            joined = "\n".join(f"{key}={value}" for key, value in env.items())
        command = " ".join(service.get("command") or [])
        volumes = "\n".join(service.get("volumes") or [])
        self.assertIn("postgres_exporter.yml", volumes)
        self.assertTrue(
            "PG_EXPORTER_EXTEND_QUERY_PATH" in joined
            or "--extend.query-path" in command
            or "extend.query-path" in command,
            "postgres-exporter must load the mounted queries file",
        )
        self.assertTrue(
            "/etc/postgres_exporter/queries.yml" in joined
            or "/etc/postgres_exporter/queries.yml" in command,
            "extend query path must point at the mounted queries.yml",
        )

    def test_queries_file_uses_extend_query_format(self):
        document = load_yaml(QUERIES)
        self.assertIsInstance(document, dict)
        self.assertNotIn("custom_queries", document)
        for metric in BUSINESS_METRICS:
            group = metric.removesuffix("_total")
            self.assertIn(group, document, f"missing extend-query group {group} for {metric}")
            entry = document[group]
            self.assertIn("query", entry)
            self.assertIn("metrics", entry)
            self.assertIn(f"FROM {group}", entry["query"])
            self.assertIn("AS total", entry["query"])


class PostgresDashboardPromqlTest(unittest.TestCase):
    def test_cache_hit_and_connections_use_real_metric_names(self):
        joined = "\n".join(exprs(load_dashboard("notaire-postgres")))
        self.assertIn("pg_stat_database_blks_hit", joined)
        self.assertIn("pg_stat_database_blks_read", joined)
        self.assertNotIn("sum(blks_hit)", joined)
        self.assertNotIn("sum(blks_read)", joined)
        self.assertIn("pg_connections_total", joined)
        self.assertNotIn("pg_connections_total{datname=", joined)
        for metric in BUSINESS_METRICS:
            self.assertIn(metric, joined)


class VersionPanelsTest(unittest.TestCase):
    def test_micrometer_tags_carry_packaged_version(self):
        with open(APP_PROPS, encoding="utf-8") as handle:
            properties = handle.read()
        self.assertIn(
            "management.metrics.tags.version=${app.version}",
            properties,
        )

    def test_status_and_backend_dashboards_show_version(self):
        for uid in ("notaire-status", "notaire-backend"):
            joined = "\n".join(exprs(load_dashboard(uid)))
            self.assertTrue(
                "version=" in joined or 'version!=""' in joined or "version=~" in joined,
                f"{uid} must panel on the Micrometer version tag",
            )


class QualityDashboardTest(unittest.TestCase):
    def test_quality_dashboard_covers_sonar_probes(self):
        dashboard = load_dashboard(QUALITY_UID)
        self.assertEqual(QUALITY_UID, dashboard["uid"])
        joined = "\n".join(exprs(dashboard))
        self.assertIn('service="sonarqube"', joined)
        self.assertIn('service="sonar-db"', joined)
        self.assertIn("probe_success", joined)
        for panel in dashboard["panels"]:
            if panel["type"] in ("stat", "gauge", "timeseries", "bargauge"):
                self.assertIn(
                    "noValue",
                    panel["fieldConfig"]["defaults"],
                    f"{QUALITY_UID}: {panel['title']}",
                )


class GrafanaPluginNoiseTest(unittest.TestCase):
    def test_compose_does_not_install_deprecated_piechart_plugin(self):
        grafana = load_yaml(COMPOSE)["services"]["grafana"]
        env = grafana.get("environment") or {}
        if isinstance(env, list):
            joined = "\n".join(env)
        else:
            joined = "\n".join(f"{key}={value}" for key, value in env.items())
        self.assertNotIn("grafana-piechart-panel", joined)
        self.assertNotIn("GF_INSTALL_PLUGINS", joined)


if __name__ == "__main__":
    unittest.main()
