#!/usr/bin/env python3
"""
Guards issue #1666 (CU77): the repository's Prometheus scrape configuration and alert rules work
against a live backend, without Docker.

Skips unless a `prometheus` binary and a backend answering on localhost:8080 exist (start the
stack with `bash workspace/stack/native.sh up`, the binary comes from the cloud toolchain hook).
Run with: python3 infra/tests/test_prometheus_live.py
"""
import glob
import json
import os
import re
import shutil
import socket
import subprocess
import tempfile
import time
import unittest
import urllib.parse
import urllib.request

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PROMETHEUS_DIR = os.path.join(REPO_ROOT, "infra", "observability", "prometheus")
DASHBOARDS = os.path.join(REPO_ROOT, "infra", "observability", "grafana", "provisioning", "dashboards", "*.json")
BACKEND_HEALTH = "http://localhost:8080/actuator/health"
ACTUATOR_USER = os.environ.get("ACTUATOR_USER", "ci-e2e-actuator-user")
ACTUATOR_PASSWORD = os.environ.get("ACTUATOR_PASSWORD", "ci-e2e-actuator-pass")
METRIC_FAMILIES = (
    "http_server_requests_seconds_count",
    "hikaricp_connections_active",
    "jvm_memory_used_bytes",
)


def backend_is_up():
    try:
        with urllib.request.urlopen(BACKEND_HEALTH, timeout=3) as response:
            return response.status == 200
    except OSError:
        return False


def free_port():
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def get_json(url):
    with urllib.request.urlopen(url, timeout=5) as response:
        return json.load(response)


def dashboard_prometheus_queries():
    """PromQL of every Prometheus panel target, with Grafana macros and variables made concrete."""
    queries = []
    for path in sorted(glob.glob(DASHBOARDS)):
        with open(path, encoding="utf-8") as handle:
            dashboard = json.load(handle)
        stack = list(dashboard.get("panels", []))
        while stack:
            panel = stack.pop()
            stack.extend(panel.get("panels", []))
            for target in panel.get("targets", []):
                datasource = json.dumps([panel.get("datasource", ""), target.get("datasource", "")]).lower()
                if target.get("expr") and "prometheus" in datasource:
                    expression = re.sub(r"\$__rate_interval|\$__interval", "5m", target["expr"])
                    queries.append(re.sub(r"\$\{?\w+\}?", ".*", expression))
    return queries


class PrometheusLiveTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        binary = shutil.which("prometheus")
        if not binary:
            raise unittest.SkipTest("no prometheus binary on PATH")
        if not backend_is_up():
            raise unittest.SkipTest("no backend on localhost:8080 (bash workspace/stack/native.sh up)")
        cls.workdir = tempfile.mkdtemp(prefix="prom-live-")
        with open(os.path.join(PROMETHEUS_DIR, "prometheus.yml"), encoding="utf-8") as handle:
            config = handle.read()
        config = (config.replace("notary-backend:8080", "localhost:8080")
                  .replace("username: admin", f"username: {ACTUATOR_USER}")
                  .replace("password: admin", f"password: {ACTUATOR_PASSWORD}"))
        for name in ("alert-rules.yml", "alert-rules-stack.yml"):
            shutil.copy(os.path.join(PROMETHEUS_DIR, name), cls.workdir)
        with open(os.path.join(cls.workdir, "prometheus.yml"), "w", encoding="utf-8") as handle:
            handle.write(config)
        port = free_port()
        cls.base = f"http://127.0.0.1:{port}"
        cls.process = subprocess.Popen(
            [binary, f"--config.file={os.path.join(cls.workdir, 'prometheus.yml')}",
             f"--storage.tsdb.path={os.path.join(cls.workdir, 'data')}", f"--web.listen-address=127.0.0.1:{port}"],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        cls.wait_for_backend_target()

    @classmethod
    def tearDownClass(cls):
        process = getattr(cls, "process", None)
        if process:
            process.terminate()
            process.wait(timeout=20)
        shutil.rmtree(getattr(cls, "workdir", ""), ignore_errors=True)

    @classmethod
    def wait_for_backend_target(cls):
        deadline = time.time() + 40
        while time.time() < deadline:
            try:
                if cls.backend_target_health() == "up":
                    return
            except OSError:
                pass
            time.sleep(1)

    @classmethod
    def backend_target_health(cls):
        targets = get_json(f"{cls.base}/api/v1/targets")["data"]["activeTargets"]
        return next((t["health"] for t in targets if t["labels"].get("job") == "notaire-backend"), None)

    def test_the_backend_target_is_scraped(self):
        self.assertEqual("up", self.backend_target_health())

    def test_every_alert_rule_is_healthy(self):
        deadline = time.time() + 75
        rules = self.rules()
        while time.time() < deadline and any(r["health"] == "unknown" for r in rules):
            time.sleep(3)
            rules = self.rules()
        self.assertGreaterEqual(len(rules), 13)
        unhealthy = [f"{r['name']}: {r['health']} {r.get('lastError', '')}" for r in rules if r["health"] != "ok"]
        self.assertEqual([], unhealthy)

    def rules(self):
        groups = get_json(f"{self.base}/api/v1/rules")["data"]["groups"]
        return [rule for group in groups for rule in group["rules"]]

    def test_the_metric_families_the_alerts_rely_on_exist(self):
        deadline = time.time() + 30
        missing = list(METRIC_FAMILIES)
        while missing and time.time() < deadline:
            missing = [m for m in METRIC_FAMILIES if not self.series(m)]
            if missing:
                time.sleep(2)
        self.assertEqual([], missing)

    def test_every_dashboard_query_is_valid_promql(self):
        failures = []
        for query in dashboard_prometheus_queries():
            url = f"{self.base}/api/v1/query?" + urllib.parse.urlencode({"query": query})
            try:
                get_json(url)
            except OSError as error:
                failures.append(f"{query[:80]}: {error}")
        self.assertEqual([], failures)

    def series(self, query):
        url = f"{self.base}/api/v1/query?" + urllib.parse.urlencode({"query": query})
        return get_json(url)["data"]["result"]


if __name__ == "__main__":
    unittest.main()
