#!/usr/bin/env python3
"""Prove provisioned Grafana dashboards have useful panel series (#1735, CU77).

Queries Prometheus for the same PromQL the panels use (what Grafana renders) and
checks Grafana HTTP API that each dashboard UID is provisioned.

    python3 infra/scripts/verify_grafana_panels.py
    bash infra/scripts/verify-signals.sh --with-grafana-panels

Exit non-zero on any FAIL. Optional checks may SKIP.
"""
from __future__ import annotations

import base64
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass

from grafana_panel_inventory import PANEL_CHECKS, REQUIRED_DASHBOARD_UIDS

PROMETHEUS = os.environ.get("PROMETHEUS_URL", "http://localhost:9090")
GRAFANA = os.environ.get("GRAFANA_URL", "http://localhost:3001")
TIMEOUT_SECONDS = 15


@dataclass
class Result:
    name: str
    status: str
    detail: str = ""


def basic_auth(user: str, password: str) -> str:
    return "Basic " + base64.b64encode(f"{user}:{password}".encode()).decode()


def get_json(url: str, headers: dict | None = None):
    request = urllib.request.Request(url, headers=headers or {})
    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, None


def grafana_headers() -> dict:
    return {
        "Authorization": basic_auth(
            os.environ.get("GRAFANA_ADMIN_USER", "admin"),
            os.environ.get("GRAFANA_ADMIN_PASSWORD", "admin"),
        )
    }


def check_dashboard_provisioned(uid: str) -> Result:
    name = f"grafana dashboard {uid}"
    try:
        status, body = get_json(f"{GRAFANA}/api/dashboards/uid/{uid}", grafana_headers())
    except OSError as error:
        return Result(name, "FAIL", f"unreachable ({error})")
    if status in (401, 403):
        return Result(name, "FAIL", "Grafana auth failed (set GRAFANA_ADMIN_USER / GRAFANA_ADMIN_PASSWORD)")
    if status == 404 or body is None:
        return Result(name, "FAIL", "dashboard not provisioned")
    title = (body.get("dashboard") or {}).get("title", uid)
    return Result(name, "PASS", title)


def check_panel_series(label: str, expr: str, optional: bool) -> Result:
    name = f"panel {label}"
    url = f"{PROMETHEUS}/api/v1/query?query={urllib.parse.quote(expr)}"
    try:
        status, body = get_json(url)
    except OSError as error:
        return Result(name, "FAIL", f"Prometheus unreachable ({error})")
    if status != 200 or not body or body.get("status") != "success":
        return Result(name, "FAIL", f"PromQL HTTP {status}")
    series = body.get("data", {}).get("result") or []
    if not series:
        if optional:
            return Result(name, "SKIP", "no series (optional)")
        return Result(name, "FAIL", "No data for panel PromQL")
    return Result(name, "PASS", f"{len(series)} series")


def run_checks() -> list[Result]:
    results: list[Result] = []
    for uid in sorted(REQUIRED_DASHBOARD_UIDS):
        results.append(check_dashboard_provisioned(uid))
    for check in PANEL_CHECKS:
        results.append(check_panel_series(f"{check.dashboard_uid}/{check.label}", check.expr, check.optional))
    return results


def main() -> int:
    results = run_checks()
    width = max(len(r.name) for r in results)
    for result in results:
        print(f"{result.status:4}  {result.name:<{width}}  {result.detail}")
    failed = sum(r.status == "FAIL" for r in results)
    skipped = sum(r.status == "SKIP" for r in results)
    print(f"\n{len(results) - failed - skipped} passed, {failed} failed, {skipped} skipped")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
