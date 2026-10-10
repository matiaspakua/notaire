#!/usr/bin/env python3
"""
Guards #1417 / #1256 / #1197 P0.1: reproducible repository metrics baseline.

Run with: python3 workspace/tests/test_repo_metrics.py
"""
from __future__ import annotations

import json
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "ci" / "repo-metrics.py"
BASELINE = REPO_ROOT / "docs" / "300-development" / "REPO-METRICS-BASELINE.md"


class RepoMetricsTest(unittest.TestCase):
    def test_script_exists(self) -> None:
        self.assertTrue(SCRIPT.is_file(), f"missing {SCRIPT.relative_to(REPO_ROOT)}")

    def test_script_writes_json_offline(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "metrics.json"
            completed = subprocess.run(
                ["python3", str(SCRIPT), "--json", str(out)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(
                completed.returncode,
                0,
                msg=f"stdout={completed.stdout}\nstderr={completed.stderr}",
            )
            data = json.loads(out.read_text(encoding="utf-8"))
            self.assertIn("modules", data)
            self.assertIn("workflows_total", data)
            self.assertIn("workflows_with_paths_filter", data)
            self.assertIn("always_loaded_bytes", data)
            self.assertGreater(data["workflows_total"], 0)
            self.assertGreaterEqual(len(data["modules"]), 5)

    def test_baseline_markdown_committed(self) -> None:
        self.assertTrue(BASELINE.is_file(), f"missing {BASELINE.relative_to(REPO_ROOT)}")
        text = BASELINE.read_text(encoding="utf-8")
        self.assertIn("workspace/ci/repo-metrics.py", text)
        self.assertIn("modules", text.lower())

    def test_paths_filter_count_includes_dorny_action(self) -> None:
        """P0.2 uses dorny/paths-filter; the heuristic must count those workflows (#1256)."""
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "metrics.json"
            completed = subprocess.run(
                ["python3", str(SCRIPT), "--json", str(out)],
                cwd=REPO_ROOT,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(completed.returncode, 0, msg=completed.stderr)
            data = json.loads(out.read_text(encoding="utf-8"))
            # classic on.paths (2) + dorny/paths-filter (4) after P0.2
            self.assertGreaterEqual(
                data["workflows_with_paths_filter"],
                6,
                msg="path-scoped CI heuristics under-count dorny/paths-filter workflows",
            )
            self.assertIn("dorny/paths-filter", " ".join(data.get("notes") or []))


if __name__ == "__main__":
    unittest.main()
