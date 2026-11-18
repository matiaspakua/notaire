#!/usr/bin/env python3
"""Guards #2315: RepoLens Pages snapshot is generated from committed bench JSON."""

from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SCRIPT = REPO / "workspace" / "ci" / "generate-repolens-bench.py"


def load_module():
    spec = importlib.util.spec_from_file_location("generate_repolens_bench", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class GenerateRepoLensBenchTest(unittest.TestCase):
    def test_factor_matches_report_py_rounding(self):
        mod = load_module()
        self.assertEqual(mod.factor(0.891, 0.182, True, "more"), "4.9x worse")
        self.assertEqual(mod.factor(0.182, 0.891, True, "more"), "4.9x more")
        self.assertEqual(mod.factor(0.306, 0.765, True, "more"), "2.5x more")
        self.assertEqual(mod.factor(1279.518, 29.927, False, "faster"), "43x faster")
        self.assertEqual(mod.factor(204.285, 2.251, False, "less"), "91x less")
        self.assertEqual(mod.factor(4.0, 2.0, False, "fewer"), "2.0x fewer")
        self.assertEqual(mod.factor(1.0, 1.0, True, "more"), "same")

    def test_build_writes_validated_snapshot(self):
        mod = load_module()
        snap = mod.build()
        mod.validate(snap)
        self.assertEqual(snap["retrieval"]["tuning"]["tasks"], 11)
        self.assertEqual(snap["retrieval"]["holdout"]["tasks"], 12)
        self.assertGreaterEqual(len(snap["factoryLoop"]["pairs"]), 2)
        self.assertEqual(len(snap["sources"]), 7)
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"
            out.write_text(json.dumps(snap), encoding="utf-8")
            data = json.loads(out.read_text(encoding="utf-8"))
            self.assertEqual(data["headlines"]["tuning_recall"], "4.9x more")


if __name__ == "__main__":
    unittest.main()
