#!/usr/bin/env python3
"""
Guards Alertmanager and the alert catalogue (issue #1465, CU77).
Spec: docs/openspec/changes/feat-1465-alertmanager/.

Rules and routing are validated with the real promtool/amtool when Docker is available.
Run with: python3 infra/tests/test_alertmanager.py
"""
import os
import shutil
import subprocess
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBS = os.path.join(REPO_ROOT, "infra", "observability")
PROM = os.path.join(OBS, "prometheus")
PROMETHEUS_IMAGE = "prom/prometheus:v3.15.0"
ALERTMANAGER_IMAGE = "prom/alertmanager:v0.34.1"
REQUIRED_ALERTS = {
    "ServiceDown", "TargetDown", "HighHttp5xxRatio", "HighApiLatency", "HighJvmHeapUsage",
    "HighDatabaseConnections", "ConnectionPoolSaturated", "HostMemoryLow", "HostDiskLow",
}


def load_yaml(*parts):
    with open(os.path.join(*parts), encoding="utf-8") as f:
        return yaml.safe_load(f)


def all_alerts():
    alerts = []
    for name in ("alert-rules.yml", "alert-rules-stack.yml"):
        for group in load_yaml(PROM, name)["groups"]:
            alerts += [r for r in group["rules"] if "alert" in r]
    return alerts


def docker_run(image, entrypoint, args, mount):
    if not shutil.which("docker") or subprocess.run(["docker", "info"], capture_output=True).returncode:
        raise unittest.SkipTest("docker is not available")
    cmd = ["docker", "run", "--rm", "--entrypoint", entrypoint, "-v", f"{mount}:/cfg:ro", image] + args
    return subprocess.run(cmd, capture_output=True, text=True)


class AlertmanagerTest(unittest.TestCase):
    def test_prometheus_targets_alertmanager(self):
        compose = load_yaml(OBS, "docker-compose.yml")["services"]
        self.assertEqual(ALERTMANAGER_IMAGE, compose["alertmanager"]["image"])
        targets = load_yaml(PROM, "prometheus.yml")["alerting"]["alertmanagers"][0]["static_configs"][0]["targets"]
        self.assertEqual(["alertmanager:9093"], targets)
        self.assertTrue(any("alert-rules-stack.yml" in v for v in compose["prometheus"]["volumes"]))
        self.assertIn("alert-rules-stack.yml", load_yaml(PROM, "prometheus.yml")["rule_files"])

    def test_every_alert_has_severity_summary_description(self):
        alerts = all_alerts()
        self.assertTrue(REQUIRED_ALERTS <= {a["alert"] for a in alerts}, REQUIRED_ALERTS - {a["alert"] for a in alerts})
        for alert in alerts:
            self.assertIn(alert["labels"].get("severity"), ("warning", "critical"), alert["alert"])
            self.assertTrue(alert["annotations"].get("summary"), alert["alert"])
            self.assertTrue(alert["annotations"].get("description"), alert["alert"])

    def test_promtool_accepts_rules(self):
        result = docker_run(PROMETHEUS_IMAGE, "promtool", ["check", "rules", "/cfg/alert-rules.yml", "/cfg/alert-rules-stack.yml"], PROM)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_routing_groups_and_inhibits(self):
        config = load_yaml(OBS, "alertmanager", "alertmanager.yml")
        self.assertEqual(["alertname", "service"], config["route"]["group_by"])
        self.assertTrue(config["inhibit_rules"])
        self.assertEqual("critical", config["inhibit_rules"][0]["source_matchers"][0].split("=")[1].strip('"'))
        for receiver in config["receivers"]:
            self.assertFalse({"email_configs", "slack_configs", "webhook_configs", "pagerduty_configs"} & set(receiver))

    def test_amtool_accepts_config(self):
        result = docker_run(ALERTMANAGER_IMAGE, "amtool", ["check-config", "/cfg/alertmanager.yml"], os.path.join(OBS, "alertmanager"))
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_grafana_alertmanager_datasource(self):
        sources = load_yaml(OBS, "grafana", "provisioning", "datasources", "datasources.yml")["datasources"]
        source = next(s for s in sources if s["uid"] == "alertmanager")
        self.assertEqual("alertmanager", source["type"])
        self.assertEqual("prometheus", source["jsonData"]["implementation"])


if __name__ == "__main__":
    unittest.main()
