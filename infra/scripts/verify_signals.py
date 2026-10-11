#!/usr/bin/env python3
"""Checks that every observability service holds real data after traffic (issue #1469, CU77).

Run after the Playwright suite (or any traffic) against the local stack:

    python3 infra/scripts/verify_signals.py

Exit code is non-zero when a check FAILs; a check that needs credentials the environment does not
provide is reported as SKIP. Data counts as present when it exists within the last 30 minutes.
"""
import base64
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass

PROMETHEUS = os.environ.get("PROMETHEUS_URL", "http://localhost:9090")
LOKI = os.environ.get("LOKI_URL", "http://localhost:3100")
GRAFANA = os.environ.get("GRAFANA_URL", "http://localhost:3001")
SONAR = os.environ.get("SONAR_URL", "http://localhost:9000")
WINDOW_SECONDS = 30 * 60
TIMEOUT_SECONDS = 15


@dataclass
class Result:
    name: str
    status: str
    detail: str = ""


def basic_auth(user, password):
    return "Basic " + base64.b64encode(f"{user}:{password}".encode()).decode()


def get_json(url, headers=None):
    request = urllib.request.Request(url, headers=headers or {})
    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, None


def http_fetch(kind, query):
    """Maps a check's request to the real endpoint; returns (status, normalized body)."""
    grafana_auth = {"Authorization": basic_auth(
        os.environ.get("GRAFANA_ADMIN_USER", "admin"), os.environ.get("GRAFANA_ADMIN_PASSWORD", "admin"))}
    if kind == "prom_query":
        status, body = get_json(f"{PROMETHEUS}/api/v1/query?query={urllib.parse.quote(query)}")
        return status, {"result": body["data"]["result"]} if body else None
    if kind == "prom_targets":
        status, body = get_json(f"{PROMETHEUS}/api/v1/targets")
        targets = [{"job": t["labels"]["job"], "health": t["health"]} for t in body["data"]["activeTargets"]]
        return status, {"targets": targets}
    if kind == "alertmanagers":
        status, body = get_json(f"{PROMETHEUS}/api/v1/alertmanagers")
        return status, {"count": len(body["data"]["activeAlertmanagers"])}
    if kind == "loki_query":
        now = int(time.time() * 1e9)
        url = (f"{LOKI}/loki/api/v1/query_range?limit=1&start={now - WINDOW_SECONDS * 10**9}&end={now}"
               f"&query={urllib.parse.quote(query)}")
        status, body = get_json(url)
        return status, {"result": body["data"]["result"]} if body else None
    if kind == "tempo_search":
        url = f"{GRAFANA}/api/datasources/proxy/uid/tempo/api/search?limit=5&q={urllib.parse.quote(query)}"
        return get_json(url, grafana_auth)
    if kind == "sonar_project":
        auth = {"Authorization": basic_auth(
            os.environ.get("SONAR_ADMIN_USER", "admin"), os.environ.get("SONAR_ADMIN_PASSWORD", "admin"))}
        return get_json(f"{SONAR}/api/components/show?component={urllib.parse.quote(query)}", auth)
    raise ValueError(kind)


def has_data(fetch, kind, query, name, what):
    try:
        status, body = fetch(kind, query)
    except OSError as error:
        return Result(name, "FAIL", f"{what}: unreachable ({error})")
    if status != 200 or body is None:
        return Result(name, "FAIL", f"{what}: HTTP {status}")
    items = body.get("result", body.get("traces", []))
    if not items:
        return Result(name, "FAIL", f"no {what} in the last 30 minutes")
    return Result(name, "PASS", f"{len(items)} found")


def targets_up(fetch):
    name = "prometheus targets up"
    try:
        status, body = fetch("prom_targets", "")
    except OSError as error:
        return Result(name, "FAIL", f"unreachable ({error})")
    down = sorted({t["job"] for t in (body or {}).get("targets", []) if t["health"] != "up"})
    if status != 200 or not body or not body["targets"]:
        return Result(name, "FAIL", f"no targets (HTTP {status})")
    return Result(name, "FAIL", "down: " + ", ".join(down)) if down else Result(name, "PASS", f"{len(body['targets'])} up")


def alertmanager_wired(fetch):
    name = "alertmanager wired"
    try:
        status, body = fetch("alertmanagers", "")
    except OSError as error:
        return Result(name, "FAIL", f"unreachable ({error})")
    if status == 200 and body and body["count"] >= 1:
        return Result(name, "PASS", f"{body['count']} alertmanager(s) active")
    return Result(name, "FAIL", "Prometheus has no active Alertmanager")


def sonar_project(fetch, key):
    name = f"sonarqube project {key}"
    try:
        status, _ = fetch("sonar_project", key)
    except OSError as error:
        return Result(name, "SKIP", f"SonarQube unreachable ({error})")
    if status in (401, 403):
        return Result(name, "SKIP", "not authenticated (set SONAR_ADMIN_USER / SONAR_ADMIN_PASSWORD)")
    if status == 404:
        return Result(name, "FAIL", "project not found: run infra/scripts/run-sonar.sh")
    return Result(name, "PASS" if status == 200 else "FAIL", f"HTTP {status}")


def run_checks(fetch):
    backend = '{container="notary-backend"}'
    return [
        targets_up(fetch),
        alertmanager_wired(fetch),
        has_data(fetch, "prom_query", 'sum(http_server_requests_seconds_count{application="notaire-backend"})',
                 "backend metrics (Actuator scrape)", "backend request metrics"),
        has_data(fetch, "prom_query", 'jvm_memory_used_bytes{job="otel-collector"}',
                 "backend metrics (OTLP via collector)", "backend metrics through the collector"),
        has_data(fetch, "prom_query", 'nodejs_heap_size_used_bytes{job="frontend-metrics"}',
                 "frontend metrics", "frontend Node metrics"),
        has_data(fetch, "loki_query", backend + ' |= "http_request"', "loki backend request logs",
                 "backend request logs"),
        has_data(fetch, "loki_query", '{container="notaire-frontend"} |= "api_proxy"', "loki frontend request logs",
                 "frontend proxy logs"),
        has_data(fetch, "loki_query", backend + ' | traceId != ""', "loki trace id on log lines",
                 "log lines with a trace id"),
        has_data(fetch, "tempo_search", '{resource.service.name="notaire-backend"}', "tempo traces",
                 "backend traces in Tempo"),
        sonar_project(fetch, "notaire-backend"),
        sonar_project(fetch, "notaire-frontend"),
    ]


def exit_code(results):
    return 1 if any(r.status == "FAIL" for r in results) else 0


def main():
    results = run_checks(http_fetch)
    width = max(len(r.name) for r in results)
    for r in results:
        print(f"{r.status:4}  {r.name:<{width}}  {r.detail}")
    failed = sum(r.status == "FAIL" for r in results)
    skipped = sum(r.status == "SKIP" for r in results)
    print(f"\n{len(results) - failed - skipped} passed, {failed} failed, {skipped} skipped")
    return exit_code(results)


if __name__ == "__main__":
    sys.exit(main())
