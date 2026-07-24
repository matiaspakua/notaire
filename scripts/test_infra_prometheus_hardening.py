#!/usr/bin/env python3
"""
Guards against issue #674: the Prometheus container in infra/docker-compose.yml
ran as `user: root` with the host's `/var/run/docker.sock` bind-mounted in,
even though prometheus.yml only ever scrapes static targets (no
docker_sd_configs) — so the socket access served no purpose and just gave
any RCE inside the container a path to full host compromise.

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see scripts/test_report_job_needs_dependencies.py).
Run with: python3 scripts/test_infra_prometheus_hardening.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
INFRA_COMPOSE_FILE = os.path.join(REPO_ROOT, "infra", "docker-compose.yml")


def load_prometheus_service():
    with open(INFRA_COMPOSE_FILE, encoding="utf-8") as f:
        compose = yaml.safe_load(f)
    return compose["services"]["prometheus"]


class PrometheusHardeningTest(unittest.TestCase):
    """Prometheus must not run as root or have host docker.sock access."""

    def test_prometheus_does_not_run_as_root(self):
        prometheus = load_prometheus_service()
        self.assertNotEqual(
            prometheus.get("user"), "root",
            "prometheus service must not run as user: root (issue #674)",
        )

    def test_prometheus_does_not_mount_docker_socket(self):
        prometheus = load_prometheus_service()
        volumes = prometheus.get("volumes", [])
        socket_mounts = [v for v in volumes if "docker.sock" in v]
        self.assertFalse(
            socket_mounts,
            f"prometheus service must not mount /var/run/docker.sock (issue #674): {socket_mounts}",
        )


if __name__ == "__main__":
    unittest.main()
