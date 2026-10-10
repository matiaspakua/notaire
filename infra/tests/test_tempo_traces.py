#!/usr/bin/env python3
"""
Guards distributed tracing: Tempo, collector traces pipeline, OTel tracer in the backend
(issue #1464, CU77). Spec: docs/openspec/changes/feat-1464-tempo-traces/.

Run with: python3 infra/tests/test_tempo_traces.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OBS = os.path.join(REPO_ROOT, "infra", "observability")


def load_yaml(*parts):
    with open(os.path.join(*parts), encoding="utf-8") as f:
        return yaml.safe_load(f)


def read(*parts):
    with open(os.path.join(REPO_ROOT, *parts), encoding="utf-8") as f:
        return f.read()


class TracingTest(unittest.TestCase):
    def test_collector_traces_pipeline_to_tempo(self):
        compose = load_yaml(OBS, "docker-compose.yml")["services"]
        self.assertEqual("grafana/tempo:2.10.8", compose["tempo"]["image"])
        self.assertTrue(os.path.exists(os.path.join(OBS, "tempo", "tempo.yml")))
        config = load_yaml(OBS, "otel", "collector.yml")
        pipeline = config["service"]["pipelines"]["traces"]
        self.assertEqual(["otlp"], pipeline["receivers"])
        exporter = pipeline["exporters"][0]
        self.assertIn("tempo:4317", config["exporters"][exporter]["endpoint"])

    def test_backend_uses_otel_tracer(self):
        pom = read("backend-api", "pom.xml")
        self.assertIn("spring-boot-starter-opentelemetry", pom)
        self.assertNotIn("micrometer-tracing-bridge-brave", pom)
        self.assertNotIn("zipkin-reporter-brave", pom)
        props = read("backend-api", "src", "main", "resources", "application.properties")
        self.assertIn("management.opentelemetry.tracing.export.otlp.endpoint", props)
        self.assertIn("MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT", read("docker-compose.yml"))

    def test_grafana_tempo_datasource(self):
        sources = load_yaml(
            OBS, "grafana", "provisioning", "datasources", "datasources.yml"
        )["datasources"]
        tempo = next(s for s in sources if s["uid"] == "tempo")
        self.assertEqual("tempo", tempo["type"])
        self.assertEqual("loki", tempo["jsonData"]["tracesToLogsV2"]["datasourceUid"])


if __name__ == "__main__":
    unittest.main()
