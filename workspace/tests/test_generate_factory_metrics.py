#!/usr/bin/env python3
"""Guards #2447: the Pages factory metrics are a slim, stable snapshot of `aisdlc metrics factory`."""
from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SCRIPT = REPO / "workspace" / "ci" / "generate-factory-metrics.py"

REPORT = {
    "generated_at": "2026-11-23T18:00:00+00:00",
    "intents": [
        {"id": "int-a", "title": "Fast checkout", "runs": ["issue-1"],
         "time": {"wall_s": 600.0, "active_s": 300.0, "by_stage": {"red": 300.0}, "corrections": 1},
         "tokens": {"input": 10, "output": 5, "cache_read": 100, "cache_write": 0},
         "cost": {"usd": 1.25, "fully_priced": True, "rows": 2, "reported_rows": 1, "by_source": {}},
         "repolens": {"calls": 3, "tokens_served": 900, "p50_ms": 40.0, "zero_result_rate": 0.0}},
        {"id": "int-b", "title": "No cost yet", "runs": ["issue-2"],
         "time": {"wall_s": 60.0, "active_s": 0.0, "by_stage": {}, "corrections": 0},
         "tokens": {"input": 0, "output": 0, "cache_read": 0, "cache_write": 0},
         "cost": {"usd": None, "fully_priced": False, "rows": 0, "reported_rows": 0, "by_source": {}},
         "repolens": {"calls": 0, "tokens_served": 0, "p50_ms": None, "zero_result_rate": None}},
    ],
    "runs": [{"id": "issue-1"}, {"id": "issue-2"}],
    "overall": {
        "runs": 2, "intents": 2, "succeeded": 2, "time": {"active_s": 300.0, "corrections": 1, "by_stage": {}},
        "tokens": {"input": 10, "output": 5, "cache_read": 100, "cache_write": 0},
        "cost": {"usd": 3.25, "fully_priced": True, "rows": 3, "reported_rows": 1, "by_source": {}},
        "attributed": {"cost": {"usd": 1.25}}, "overhead": {"cost": {"usd": 2.0}},
        "repolens": {"calls": 3, "tokens_served": 900, "p50_ms": 40.0, "zero_result_rate": 0.0},
        "per_intent": {"median_wall_s": 330.0, "median_active_s": 150.0, "median_usd": 1.25, "median_tokens": 57.5},
    },
    "repolens_effect": {
        "bench": [{"file": "agent-haiku.json", "model": "haiku", "runs_per_arm": 33, "cost_usd_change_pct": -32.1,
                   "input_tokens_change_pct": -32.3, "tool_calls_change_pct": -19.5, "duration_change_pct": -24.5,
                   "recall_change": 0.005}],
        "live_ab": {"metrics": {"cost_usd": {"a": {"n": 0}, "b": {"n": 0}}}},
    },
}


def load():
    spec = importlib.util.spec_from_file_location("generate_factory_metrics", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class GenerateFactoryMetricsTest(unittest.TestCase):
    def test_the_snapshot_keeps_the_answers_and_drops_internals(self):
        snap = load().slim(REPORT)
        self.assertEqual(2, snap["overall"]["intents"])
        self.assertEqual(150.0, snap["overall"]["median_active_s"])
        self.assertEqual({"total": 3.25, "attributed": 1.25, "overhead": 2.0}, snap["overall"]["usd"])
        first = snap["intents"][0]
        self.assertEqual(("Fast checkout", 1, 300.0, 115, 1.25, 3), (first["title"], first["runs"], first["active_s"],
                                                                     first["tokens"], first["usd"], first["repolens_calls"]))
        self.assertNotIn("by_source", json.dumps(snap))
        self.assertIsNone(snap["intents"][1]["usd"], "a missing cost is null, never 0")
        self.assertEqual(-32.1, snap["repolens"]["bench"][0]["cost_change_pct"])
        self.assertEqual({"with_repolens": 0, "without": 0}, snap["repolens"]["live_pairs"])

    def test_writing_unchanged_data_keeps_the_file(self):
        mod = load()
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "factory-metrics.json"
            self.assertTrue(mod.write(mod.slim(REPORT), out))
            before = out.read_bytes()
            later = {**REPORT, "generated_at": "2026-11-24T00:00:00+00:00"}
            self.assertFalse(mod.write(mod.slim(later), out))
            self.assertEqual(before, out.read_bytes())

    def test_the_committed_snapshot_is_valid(self):
        mod = load()
        mod.validate(json.loads((REPO / "github-page" / "lib" / "factory-metrics.json").read_text(encoding="utf-8")))


if __name__ == "__main__":
    unittest.main()
