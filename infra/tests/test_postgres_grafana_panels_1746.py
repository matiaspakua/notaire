#!/usr/bin/env python3
"""
#1746 / CU77: notaire-postgres empty-panel guards.

Asserts extend-query scraped names match Grafana PromQL (no double-prefix),
Flyway grants exporter SELECT on business tables, and ops-rate uses a live metric.
"""
from __future__ import annotations

import json
import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
QUERIES = os.path.join(REPO_ROOT, "infra", "observability", "prometheus", "postgres_exporter.yml")
DASHBOARD = os.path.join(
    REPO_ROOT,
    "infra",
    "observability",
    "grafana",
    "provisioning",
    "dashboards",
    "notaire-postgres.json",
)
MIGRATIONS = os.path.join(REPO_ROOT, "backend-api", "src", "main", "resources", "db", "migration")

# PromQL series Grafana expects (must equal scraped names).
EXPECTED_SERIES = (
    "pg_connections_total",
    "deeds_total",
    "budgets_total",
    "people_total",
    "users_total",
)
BUSINESS_TABLES = ("deeds", "budgets", "people", "users")


def load_queries():
    with open(QUERIES, encoding="utf-8") as handle:
        return yaml.safe_load(handle)


def scraped_name(group_key: str, gauge_column: str) -> str:
    """postgres-exporter concatenates group + column; identical names double-prefix."""
    if group_key == gauge_column:
        return f"{group_key}_{gauge_column}"
    return f"{group_key}_{gauge_column}"


def gauge_columns(entry: dict) -> list[str]:
    columns = []
    for item in entry.get("metrics") or []:
        if not isinstance(item, dict):
            continue
        for name, meta in item.items():
            if isinstance(meta, dict) and meta.get("usage") == "GAUGE":
                columns.append(name)
    return columns


class ExtendQueryNamingTest(unittest.TestCase):
    def test_extend_queries_emit_expected_series_without_double_prefix(self):
        document = load_queries()
        emitted = set()
        for group_key, entry in document.items():
            if not isinstance(entry, dict) or "query" not in entry:
                continue
            for column in gauge_columns(entry):
                name = scraped_name(group_key, column)
                self.assertNotEqual(
                    group_key,
                    column,
                    f"group {group_key!r} must not reuse gauge column {column!r} "
                    f"(scraped name would be {group_key}_{column})",
                )
                emitted.add(name)
        for series in EXPECTED_SERIES:
            self.assertIn(
                series,
                emitted,
                f"extend-query must emit scraped series {series} for Grafana PromQL",
            )


class ExporterSelectGrantsTest(unittest.TestCase):
    def test_flyway_grants_exporter_select_on_business_tables(self):
        grants = [
            name
            for name in os.listdir(MIGRATIONS)
            if name.startswith("V") and "exporter" in name.lower() and "select" in name.lower()
        ]
        # Also accept a broader grant migration name used by this change.
        if not grants:
            grants = [
                name
                for name in os.listdir(MIGRATIONS)
                if re.match(r"V5[1-9]__.*exporter.*\.sql$", name)
                or re.match(r"V5[1-9]__.*exporter.*grant.*\.sql$", name, re.I)
            ]
        candidates = [
            name
            for name in os.listdir(MIGRATIONS)
            if name.startswith("V51") or ("exporter" in name.lower() and "grant" in name.lower())
        ]
        self.assertTrue(
            candidates,
            "expected Flyway V51+ migration granting exporter SELECT on business tables",
        )
        body = ""
        for name in sorted(candidates):
            with open(os.path.join(MIGRATIONS, name), encoding="utf-8") as handle:
                body += handle.read() + "\n"
        self.assertIn("GRANT SELECT", body)
        self.assertIn("${exporterUsername}", body)
        for table in BUSINESS_TABLES:
            self.assertRegex(
                body,
                re.compile(rf"GRANT\s+SELECT\s+ON\s+(TABLE\s+)?{table}\b", re.I),
                f"missing GRANT SELECT on {table}",
            )
        rollback = [
            name
            for name in os.listdir(MIGRATIONS)
            if name.startswith("R51") or (name.startswith("R") and "exporter" in name.lower() and "grant" in name.lower())
        ]
        self.assertTrue(rollback, "expected R51 rollback for exporter SELECT grants")


class OpsRatePanelTest(unittest.TestCase):
    def test_ops_rate_uses_existing_notaire_operation_total(self):
        with open(DASHBOARD, encoding="utf-8") as handle:
            dashboard = json.load(handle)
        exprs = [
            target.get("expr", "")
            for panel in dashboard["panels"]
            for target in panel.get("targets", [])
        ]
        joined = "\n".join(exprs)
        self.assertNotIn(
            "notaire_database_operation_duration",
            joined,
            "unused Micrometer timer must not drive a permanent No data panel",
        )
        self.assertIn("notaire_operation_total", joined)


if __name__ == "__main__":
    unittest.main()
