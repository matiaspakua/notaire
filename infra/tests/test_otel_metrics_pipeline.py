#!/usr/bin/env python3
"""
Guards the metrics pipeline: OTel collector, backend OTLP export, frontend metrics,
cAdvisor and node-exporter (issue #1462, CU77).
Spec: docs/openspec/changes/feat-1462-otel-metrics-exporters/.

Run with: python3 infra/tests/test_otel_metrics_pipeline.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBS = os.path.join(REPO_ROOT, "infra", "observability")


def load_yaml(path):
    with open(path, encoding="utf-8") as f:
        return yaml.safe_load(f)


def read(*parts):
    with open(os.path.join(REPO_ROOT, *parts), encoding="utf-8") as f:
        return f.read()


def jobs_by_service():
    scrape = load_yaml(os.path.join(OBS, "prometheus", "prometheus.yml"))["scrape_configs"]
    by_service = {}
    for job in scrape:
        for static in job.get("static_configs", []):
            by_service[static.get("labels", {}).get("service")] = (job, static)
    return by_service


class MetricsPipelineTest(unittest.TestCase):
    def test_collector_service_and_scrape(self):
        compose = load_yaml(os.path.join(OBS, "docker-compose.yml"))["services"]
        image = compose["otel-collector"]["image"]
        self.assertTrue(image.startswith("otel/opentelemetry-collector-contrib:"))
        self.assertNotIn(":latest", image)
        config = load_yaml(os.path.join(OBS, "otel", "collector.yml"))
        self.assertIn("otlp", config["receivers"])
        self.assertIn("prometheus", config["exporters"])
        self.assertIn("metrics", config["service"]["pipelines"])
        self.assertIn("otel-collector", jobs_by_service())

    def test_backend_exports_otlp_and_keeps_actuator(self):
        self.assertIn("micrometer-registry-otlp", read("backend-api", "pom.xml"))
        props = read("backend-api", "src", "main", "resources", "application.properties")
        self.assertIn("management.otlp.metrics.export.url", props)
        self.assertIn("management.endpoints.web.exposure.include", props)
        self.assertIn("prometheus", props)
        self.assertIn("MANAGEMENT_OTLP_METRICS_EXPORT_URL", read("docker-compose.yml"))

    def test_frontend_scrape_job(self):
        job, static = jobs_by_service()["frontend-metrics"]
        self.assertEqual("/api/metrics", job["metrics_path"])
        self.assertIn("prom-client", read("frontend", "package.json"))

    def test_exporters_scraped(self):
        compose = load_yaml(os.path.join(OBS, "docker-compose.yml"))["services"]
        for name in ("cadvisor", "node-exporter"):
            self.assertIn(name, compose)
            self.assertNotIn(":latest", compose[name]["image"])
            self.assertFalse(compose[name].get("privileged", False))
            self.assertFalse(any("docker.sock" in v for v in compose[name].get("volumes", [])))
        services = jobs_by_service()
        self.assertIn("cadvisor", services)
        self.assertIn("node-exporter", services)


if __name__ == "__main__":
    unittest.main()
