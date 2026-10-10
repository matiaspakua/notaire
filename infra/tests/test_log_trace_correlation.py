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
DATASOURCES = os.path.join(
    REPO_ROOT, "infra", "observability", "grafana", "provisioning", "datasources", "datasources.yml"
)


class LogTraceCorrelationTest(unittest.TestCase):
    def test_loki_derived_field_links_trace_id_to_tempo(self):
        with open(DATASOURCES, encoding="utf-8") as f:
            sources = yaml.safe_load(f)["datasources"]
        loki = next(s for s in sources if s["uid"] == "loki")
        fields = loki["jsonData"]["derivedFields"]
        trace = next(d for d in fields if d["name"] == "TraceID")
        self.assertEqual("tempo", trace["datasourceUid"])
        self.assertEqual("${__value.raw}", trace["url"])
        match = re.search(trace["matcherRegex"], '{"traceId":"4bf92f3577b34da6a3ce929d0e0e4736"}')
        self.assertEqual("4bf92f3577b34da6a3ce929d0e0e4736", match.group(1))


if __name__ == "__main__":
    unittest.main()
