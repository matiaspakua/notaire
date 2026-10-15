#!/usr/bin/env python3
"""
#1735 / CU77: Grafana panel inventory must match provisioned dashboards.

Offline wiring only — live series asserts live in verify_grafana_panels.py.
"""
from __future__ import annotations

import importlib.util
import json
import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DASHBOARDS = os.path.join(
    REPO_ROOT, "infra", "observability", "grafana", "provisioning", "dashboards"
)
INVENTORY_PATH = os.path.join(REPO_ROOT, "infra", "scripts", "grafana_panel_inventory.py")
VERIFY_SCRIPT = os.path.join(REPO_ROOT, "infra", "scripts", "verify_grafana_panels.py")
VERIFY_SH = os.path.join(REPO_ROOT, "infra", "scripts", "verify-signals.sh")
OPERATION = os.path.join(REPO_ROOT, "infra", "docs", "OPERATION.md")


def load_inventory():
    import sys

    scripts = os.path.join(REPO_ROOT, "infra", "scripts")
    if scripts not in sys.path:
        sys.path.insert(0, scripts)
    import grafana_panel_inventory  # noqa: WPS433 — offline load from infra/scripts

    return grafana_panel_inventory


def dashboard_exprs(uid: str) -> str:
    with open(os.path.join(DASHBOARDS, f"{uid}.json"), encoding="utf-8") as handle:
        dashboard = json.load(handle)
    parts = []
    for panel in dashboard.get("panels", []):
        for target in panel.get("targets", []):
            expr = target.get("expr")
            if expr:
                parts.append(expr)
    return "\n".join(parts)


class GrafanaPanelInventoryWiringTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.inventory = load_inventory()

    def test_inventory_covers_required_dashboards(self):
        uids = {c.dashboard_uid for c in self.inventory.PANEL_CHECKS}
        for uid in ("notaire-status", "notaire-postgres", "notaire-backend", "notaire-quality"):
            self.assertIn(uid, uids)

    def test_each_inventory_expr_appears_in_dashboard_json(self):
        for check in self.inventory.PANEL_CHECKS:
            joined = dashboard_exprs(check.dashboard_uid)
            self.assertIn(
                check.expr,
                joined,
                f"{check.dashboard_uid}: missing PromQL for {check.label!r}: {check.expr}",
            )

    def test_live_script_and_shell_flag_exist(self):
        self.assertTrue(os.path.isfile(VERIFY_SCRIPT), "verify_grafana_panels.py missing")
        with open(VERIFY_SH, encoding="utf-8") as handle:
            shell = handle.read()
        self.assertIn("--with-grafana-panels", shell)
        with open(OPERATION, encoding="utf-8") as handle:
            docs = handle.read()
        self.assertIn("verify_grafana_panels", docs)
        self.assertIn("--with-grafana-panels", docs)


if __name__ == "__main__":
    unittest.main()
