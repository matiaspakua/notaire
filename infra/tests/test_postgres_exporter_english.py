#!/usr/bin/env python3
"""The postgres exporter queries the live English tables (issue #1490, CU75)."""
import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
QUERIES = os.path.join(REPO_ROOT, "infra", "observability", "prometheus", "postgres_exporter.yml")
DASHBOARD = os.path.join(
    REPO_ROOT, "infra", "observability", "grafana", "provisioning", "dashboards", "notaire-postgres.json"
)

LIVE_TABLES = ("deeds", "budgets", "users", "people")
SPANISH_TABLES = ("escritura", "presupuesto", "usuario", "persona", "estado_de_gestion")
# Extend-query group keys (#1746): scraped series = {group}_total for Grafana.
QUERY_GROUPS = ("deeds", "budgets", "users", "people")
METRICS = ("deeds_total", "budgets_total", "users_total", "people_total")
SPANISH_METRICS = ("escrituras_total", "presupuestos_total", "usuarios_total", "personas_total")


class PostgresExporterEnglishTest(unittest.TestCase):
    def test_custom_queries_use_live_english_tables(self):
        with open(QUERIES, encoding="utf-8") as handle:
            document = yaml.safe_load(handle)
        self.assertNotIn("custom_queries", document)
        sql = "\n".join(
            entry["query"]
            for key, entry in document.items()
            if isinstance(entry, dict) and "query" in entry
        )
        for table in LIVE_TABLES:
            self.assertIn(f"FROM {table}", sql)
        for table in SPANISH_TABLES:
            self.assertNotIn(table, sql)
        for group in QUERY_GROUPS:
            self.assertIn(group, document)
            self.assertIn("AS total", document[group]["query"])
        for metric in SPANISH_METRICS:
            self.assertNotIn(metric, sql)
            self.assertNotIn(metric, document)

    def test_postgres_dashboard_reads_the_english_metrics(self):
        with open(DASHBOARD, encoding="utf-8") as handle:
            dashboard = json.load(handle)
        exprs = [
            target.get("expr", "")
            for panel in dashboard["panels"]
            for target in panel.get("targets", [])
        ]
        joined = "\n".join(exprs)
        for metric in METRICS:
            self.assertIn(metric, joined)
        for metric in SPANISH_METRICS:
            self.assertNotIn(metric, joined)
        legends = json.dumps(dashboard)
        self.assertNotIn("Escrituras", legends)
        self.assertNotIn("Presupuestos", legends)
        self.assertNotIn("estado_nombre", legends)


if __name__ == "__main__":
    unittest.main()
