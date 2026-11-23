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
            self.assertEqual(data["headlines"]["tuning_recall"], "5.0x more")


    def test_validate_accepts_any_well_formed_headline_label(self):
        """#2469: a refresh re-measures latency, so the label changes; validation checks its form, not its value."""
        mod = load_module()
        snap = mod.build()
        snap["headlines"] = dict(snap["headlines"], latency="42x faster", holdout_recall="+12%")
        mod.validate(snap)

    def test_validate_rejects_a_malformed_headline_label(self):
        mod = load_module()
        snap = mod.build()
        snap["headlines"] = dict(snap["headlines"], latency="fast!")
        with self.assertRaises(SystemExit):
            mod.validate(snap)

    # #2429: a Pages build must not rewrite the tracked snapshot when its data is unchanged.

    def test_writing_unchanged_data_keeps_the_file_byte_identical(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"
            first = dict(mod.build(), generated_at="2026-01-01T00:00:00Z")
            self.assertTrue(mod.write(first, out))
            before = out.read_bytes()
            second = dict(mod.build(), generated_at="2026-02-02T00:00:00Z")
            self.assertFalse(mod.write(second, out))
            self.assertEqual(before, out.read_bytes())

    def test_changed_data_refreshes_generated_at(self):
        mod = load_module()
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"
            mod.write(dict(mod.build(), generated_at="2026-01-01T00:00:00Z"), out)
            changed = dict(mod.build(), generated_at="2026-02-02T00:00:00Z", method="changed")
            self.assertTrue(mod.write(changed, out))
            data = json.loads(out.read_text(encoding="utf-8"))
            self.assertEqual("2026-02-02T00:00:00Z", data["generated_at"])
            self.assertEqual("changed", data["method"])

    def test_the_committed_snapshot_is_up_to_date(self):
        mod = load_module()
        committed = REPO / "github-page" / "lib" / "repolens-bench-snapshot.json"
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"
            out.write_bytes(committed.read_bytes())
            self.assertFalse(mod.write(mod.build(), out), "regenerate and commit the snapshot")
            self.assertEqual(committed.read_bytes(), out.read_bytes())


if __name__ == "__main__":
    unittest.main()
