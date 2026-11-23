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


    # ------------------------------------------------------------------
    # #2429: snapshot must stay byte-identical when underlying data does not change
    # ------------------------------------------------------------------

    def test_running_twice_on_unchanged_inputs_keeps_file_byte_identical(self):
        """Running the generator twice on unchanged inputs must keep the file byte-identical.

        The second run should preserve the first run's generated_at so the file is
        byte-for-byte identical and git sees no change.
        """
        mod = load_module()
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"

            # First run: build and write
            snap1 = mod.build()
            out.write_text(json.dumps(snap1, indent=2) + "\n", encoding="utf-8")
            first_bytes = out.read_bytes()

            import time

            time.sleep(1.1)  # ensure a different second for generated_at

            # Second run: build and write again
            snap2 = mod.build()
            out.write_text(json.dumps(snap2, indent=2) + "\n", encoding="utf-8")
            second_bytes = out.read_bytes()

            self.assertEqual(
                first_bytes,
                second_bytes,
                "Second run should produce byte-identical output when input data has not changed. "
                "The generator should preserve the existing generated_at when only the timestamp "
                "would differ.",
            )

    def test_changed_input_refreshes_generated_at(self):
        """When underlying bench data changes, generated_at should be refreshed."""
        mod = load_module()
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"

            # First run
            snap1 = mod.build()
            out.write_text(json.dumps(snap1, indent=2) + "\n", encoding="utf-8")
            first_generated_at = snap1["generated_at"]

            import time

            time.sleep(1.1)  # ensure a different second

            # Second run with modified data (simulates changed bench results)
            snap2 = mod.build()
            snap2["headlines"]["tuning_recall"] = "CHANGED"
            out.write_text(json.dumps(snap2, indent=2) + "\n", encoding="utf-8")
            final = json.loads(out.read_text(encoding="utf-8"))

            self.assertNotEqual(
                final["generated_at"],
                first_generated_at,
                "generated_at should be refreshed when underlying data changes",
            )

    def test_committed_snapshot_is_up_to_date(self):
        """Generating into a temp copy of the committed snapshot should change nothing.

        The committed github-page/lib/repolens-bench-snapshot.json should already
        reflect the current bench data — regenerating should produce a byte-identical file.
        """
        mod = load_module()
        committed = REPO / "github-page" / "lib" / "repolens-bench-snapshot.json"
        self.assertTrue(committed.exists(), "committed snapshot should exist")

        committed_bytes = committed.read_bytes()

        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "repolens-bench-snapshot.json"
            # Copy committed file to temp
            out.write_bytes(committed_bytes)

            # Build fresh snapshot and write
            snap = mod.build()
            out.write_text(json.dumps(snap, indent=2) + "\n", encoding="utf-8")
            regenerated_bytes = out.read_bytes()

            self.assertEqual(
                committed_bytes,
                regenerated_bytes,
                "Regenerated snapshot should be byte-identical to committed snapshot "
                "when underlying data has not changed. The generator should preserve the "
                "existing generated_at.",
            )


if __name__ == "__main__":
    unittest.main()
