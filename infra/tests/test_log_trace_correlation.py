#!/usr/bin/env python3
"""
Guards log-to-trace navigation: Loki derived field to Tempo (issue #1463, CU77).
Spec: docs/openspec/changes/feat-1463-log-trace-correlation/.

Run with: python3 infra/tests/test_log_trace_correlation.py
"""
import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
LOKI_DIR = os.path.join(REPO_ROOT, "infra", "observability", "loki")
DATASOURCES = os.path.join(
    REPO_ROOT, "infra", "observability", "grafana", "provisioning", "datasources", "datasources.yml"
)


def load_yaml(path):
    with open(path, encoding="utf-8") as f:
        return yaml.safe_load(f)


class LogTraceCorrelationTest(unittest.TestCase):
    def test_loki_derived_field_links_trace_id_to_tempo(self):
        sources = load_yaml(DATASOURCES)["datasources"]
        loki = next(s for s in sources if s["uid"] == "loki")
        trace = next(d for d in loki["jsonData"]["derivedFields"] if d["name"] == "TraceID")
        self.assertEqual("tempo", trace["datasourceUid"])
        self.assertEqual("label", trace["matcherType"])
        self.assertEqual("traceId", trace["matcherRegex"])
        # Grafana expands ${...} in provisioning files, so the literal needs $$.
        self.assertEqual("$${__value.raw}", trace["url"])

    def test_trace_id_is_stored_as_structured_metadata_not_a_label(self):
        limits = load_yaml(os.path.join(LOKI_DIR, "local-config.yaml"))["limits_config"]
        self.assertTrue(limits["allow_structured_metadata"])
        stages = load_yaml(os.path.join(LOKI_DIR, "promtail-config.yaml"))["scrape_configs"][0]["pipeline_stages"]
        metadata = next(st["structured_metadata"] for st in stages if "structured_metadata" in st)
        self.assertIn("traceId", metadata)
        labels = next(st["labels"] for st in stages if "labels" in st)
        self.assertNotIn("traceId", labels)


if __name__ == "__main__":
    unittest.main()
