#!/usr/bin/env python3
"""
Locks #2203 / CU76: Pages metrics generator must not publish fabricated 0% coverage
when the JaCoCo snapshot (or Vitest summary) is missing.

Run with: python3 -m unittest workspace.tests.test_generate_github_page_metrics
"""
from __future__ import annotations

import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "ci" / "generate-github-page-metrics.sh"


class GenerateGithubPageMetricsHonestyTest(unittest.TestCase):
    def _run(
        self,
        *,
        live: dict | None,
        backend_snapshot: dict | None = None,
        frontend_summary: dict | None = None,
    ) -> dict:
        self.assertTrue(SCRIPT.is_file(), f"missing {SCRIPT}")
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = Path(tmp)
            out = tmp_path / "metrics.json"
            live_path = tmp_path / "live-metrics.json"
            live_path.write_text(json.dumps(live if live is not None else {}), encoding="utf-8")

            env = {
                **os.environ,
                "OUTPUT_FILE": str(out),
                "LIVE_METRICS_URL": live_path.resolve().as_uri(),
                "GH_REPO": "matiaspakua/notaire",
                "BACKEND_SNAPSHOT": str(tmp_path / "missing-backend.json"),
                "FRONTEND_SUMMARY": str(tmp_path / "missing-frontend.json"),
            }

            if backend_snapshot is not None:
                snap = tmp_path / "coverage-snapshot.json"
                snap.write_text(json.dumps(backend_snapshot), encoding="utf-8")
                env["BACKEND_SNAPSHOT"] = str(snap)

            if frontend_summary is not None:
                summary = tmp_path / "coverage-summary.json"
                summary.write_text(json.dumps(frontend_summary), encoding="utf-8")
                env["FRONTEND_SUMMARY"] = str(summary)

            completed = subprocess.run(
                ["bash", str(SCRIPT)],
                cwd=str(REPO_ROOT),
                env=env,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(
                completed.returncode,
                0,
                msg=f"stdout={completed.stdout}\nstderr={completed.stderr}",
            )
            self.assertTrue(out.is_file(), f"generator did not write {out}")
            return json.loads(out.read_text(encoding="utf-8"))

    def test_missing_snapshot_and_missing_live_do_not_publish_zero_coverage(self) -> None:
        data = self._run(live={})
        coverage = data["coverage"]
        self.assertIsNone(coverage["backendInstruction"])
        self.assertIsNone(coverage["backendBranch"])
        self.assertEqual(coverage["lastUpdated"], "unknown")

    def test_poisoned_live_zeros_with_unknown_stamp_are_not_copied_forward(self) -> None:
        data = self._run(
            live={
                "coverage": {
                    "backendInstruction": 0,
                    "backendBranch": 0,
                    "frontendComponent": 0,
                    "e2eTests": 0,
                    "apiEndpoints": 0,
                    "lastUpdated": "unknown",
                }
            }
        )
        coverage = data["coverage"]
        self.assertIsNone(coverage["backendInstruction"])
        self.assertIsNone(coverage["backendBranch"])
        self.assertIsNone(coverage["frontendComponent"])
        self.assertEqual(coverage["lastUpdated"], "unknown")

    def test_present_jacoco_snapshot_updates_backend_fields(self) -> None:
        data = self._run(
            live={},
            backend_snapshot={
                "backendInstruction": 87,
                "backendBranch": 71,
                "lastUpdated": "2026-11-10",
            },
        )
        coverage = data["coverage"]
        self.assertEqual(coverage["backendInstruction"], 87)
        self.assertEqual(coverage["backendBranch"], 71)
        self.assertEqual(coverage["lastUpdated"], "2026-11-10")

    def test_present_frontend_summary_updates_frontend_component(self) -> None:
        data = self._run(
            live={},
            frontend_summary={"total": {"statements": {"pct": 82.7}}},
        )
        self.assertEqual(data["coverage"]["frontendComponent"], 82)

    def test_measured_live_fallback_reused_when_snapshot_missing(self) -> None:
        data = self._run(
            live={
                "coverage": {
                    "backendInstruction": 86,
                    "backendBranch": 70,
                    "frontendComponent": 81,
                    "e2eTests": 10,
                    "apiEndpoints": 20,
                    "lastUpdated": "2026-11-09",
                }
            }
        )
        coverage = data["coverage"]
        self.assertEqual(coverage["backendInstruction"], 86)
        self.assertEqual(coverage["backendBranch"], 70)
        self.assertEqual(coverage["frontendComponent"], 81)
        self.assertEqual(coverage["lastUpdated"], "2026-11-09")


if __name__ == "__main__":
    unittest.main()
