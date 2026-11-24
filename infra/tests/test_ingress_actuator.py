#!/usr/bin/env python3
"""
Guards #2487 / CU78: the public ingress forwards only the actuator health endpoints.

infra/deploy/kustomize/base/nginx.conf is the single proxy config: docker-compose.prod.yml mounts it and the
Kustomize base renders it into a ConfigMap. Prometheus and the probes reach the backend on the private network,
so nothing outside needs threaddump, loggers, mappings, metrics or prometheus.

Run with: python3 infra/tests/test_ingress_actuator.py
"""
import re
import unittest
from pathlib import Path

NGINX = Path(__file__).resolve().parents[2] / "infra" / "deploy" / "kustomize" / "base" / "nginx.conf"
LOCATION = re.compile(r"location\s+(=\s+)?(\S+)\s*\{(.*?)\n\s*\}", re.S)


def locations() -> list[tuple[str, str, str]]:
    text = re.sub(r"#[^\n]*", "", NGINX.read_text(encoding="utf-8"))
    return [(exact.strip(), path, body) for exact, path, body in LOCATION.findall(text)]


class IngressActuatorTest(unittest.TestCase):
    def test_health_is_forwarded_exactly_and_by_prefix(self):
        forwarded = {(exact, path) for exact, path, body in locations() if "proxy_pass" in body}
        self.assertIn(("=", "/actuator/health"), forwarded)
        self.assertIn(("", "/actuator/health/"), forwarded)

    def test_every_other_actuator_path_answers_404_without_proxying(self):
        catch_all = [body for exact, path, body in locations() if not exact and path == "/actuator/"]
        self.assertEqual(len(catch_all), 1, "one catch-all location /actuator/")
        self.assertIn("return 404", catch_all[0])
        self.assertNotIn("proxy_pass", catch_all[0])

    def test_no_other_location_forwards_an_actuator_path(self):
        allowed = {("=", "/actuator/health"), ("", "/actuator/health/")}
        for exact, path, body in locations():
            if path.startswith("/actuator") and "proxy_pass" in body:
                self.assertIn((exact, path), allowed, f"{exact} {path} forwards actuator")


if __name__ == "__main__":
    unittest.main()
