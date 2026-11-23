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
    # ------------------------------------------------------------------
    # Fixtures that mirror real CI artefacts (issue #760)
    # ------------------------------------------------------------------

    @staticmethod
    def _playwright_results_json(
        *,
        expected: int = 42,
        unexpected: int = 2,
        skipped: int = 1,
        flaky: int = 0,
    ) -> dict:
        """Return a realistic Playwright JSON reporter payload.

        Shape matches what Playwright writes to ``test-results/results.json``
        (config line 21: ``["json", { outputFile: "test-results/results.json" }]``).
        The ``stats`` keys are ``expected | unexpected | skipped | flaky``.
        Total tests = sum of all four.
        """
        specs = []
        for idx in range(expected):
            specs.append({"title": f"passing test {idx}", "ok": True, "file": "tests/smoke.spec.ts"})
        for idx in range(unexpected):
            specs.append({"title": f"failing test {idx}", "ok": False, "file": "tests/auth.spec.ts"})
        for idx in range(skipped):
            specs.append({"title": f"skipped test {idx}", "ok": True, "file": "tests/skip.spec.ts"})

        return {
            "config": {"rootDir": str(REPO_ROOT / "testing" / "e2e")},
            "suites": [
                {
                    "title": "smoke",
                    "specs": specs[:expected],
                    "suites": [],
                },
                {
                    "title": "chromium",
                    "specs": specs[expected:],
                    "suites": [],
                },
            ],
            "stats": {
                "expected": expected,
                "unexpected": unexpected,
                "skipped": skipped,
                "flaky": flaky,
                "total": expected + unexpected + skipped + flaky,
            },
        }

    @staticmethod
    def _bruno_results_json(
        *,
        total_requests: int = 86,
        passed_requests: int = 84,
        failed_requests: int = 2,
        total_tests: int = 147,
        passed_tests: int = 145,
        failed_tests: int = 2,
    ) -> list:
        """Return a realistic Bruno CLI ``--reporter-json`` payload.

        Shape matches ``@usebruno/cli run --reporter-json`` output: a LIST of
        per-iteration objects, each with its own ``summary`` dict.
        """
        return [
            {
                "iterationIndex": 0,
                "results": [],
                "summary": {
                    "totalRequests": total_requests,
                    "passedRequests": passed_requests,
                    "failedRequests": failed_requests,
                    "errorRequests": 0,
                    "skippedRequests": 0,
                    "skippedByBail": 0,
                    "totalAssertions": 0,
                    "passedAssertions": 0,
                    "failedAssertions": 0,
                    "totalTests": total_tests,
                    "passedTests": passed_tests,
                    "failedTests": failed_tests,
                    "totalPreRequestTests": 0,
                    "passedPreRequestTests": 0,
                    "failedPreRequestTests": 0,
                    "totalPostResponseTests": 0,
                    "passedPostResponseTests": 0,
                    "failedPostResponseTests": 0,
                },
            }
        ]

    # ------------------------------------------------------------------
    # Runner
    # ------------------------------------------------------------------

    def _run(
        self,
        *,
        live: dict | None,
        backend_snapshot: dict | None = None,
        frontend_summary: dict | None = None,
        playwright_results: dict | None = None,
        bruno_results: list | None = None,
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

            if playwright_results is not None:
                pw = tmp_path / "playwright-results.json"
                pw.write_text(json.dumps(playwright_results), encoding="utf-8")
                env["PLAYWRIGHT_RESULTS_JSON"] = str(pw)

            if bruno_results is not None:
                br = tmp_path / "bruno-results.json"
                br.write_text(json.dumps(bruno_results), encoding="utf-8")
                env["BRUNO_RESULTS_JSON"] = str(br)

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

    # ------------------------------------------------------------------
    # Issue #760 — fresh e2eTests / apiEndpoints + stale fallback
    # ------------------------------------------------------------------

    def test_e2e_tests_computed_from_playwright_results(self) -> None:
        """e2eTests must be computed from a Playwright results.json fixture,
        not carried forward from the live metrics."""
        pw = self._playwright_results_json(expected=42, unexpected=2, skipped=1, flaky=0)
        data = self._run(
            live={
                "coverage": {
                    "e2eTests": 5,
                    "apiEndpoints": 10,
                    "lastUpdated": "2026-11-01",
                }
            },
            playwright_results=pw,
        )
        coverage = data["coverage"]
        # Total = expected + unexpected + skipped + flaky = 45
        self.assertEqual(coverage["e2eTests"], 45)
        # Must NOT be marked stale when computed fresh
        self.assertIsNone(coverage.get("e2eTestsStale"))

    def test_api_endpoints_computed_from_bruno_results(self) -> None:
        """apiEndpoints must be computed from a Bruno JSON report fixture,
        not carried forward from the live metrics."""
        br = self._bruno_results_json(
            total_requests=86,
            passed_requests=84,
            failed_requests=2,
            total_tests=147,
            passed_tests=145,
            failed_tests=2,
        )
        data = self._run(
            live={
                "coverage": {
                    "e2eTests": 5,
                    "apiEndpoints": 10,
                    "lastUpdated": "2026-11-01",
                }
            },
            bruno_results=br,
        )
        coverage = data["coverage"]
        # apiEndpoints = totalRequests from Bruno summary = 86
        self.assertEqual(coverage["apiEndpoints"], 86)
        # Must NOT be marked stale when computed fresh
        self.assertIsNone(coverage.get("apiEndpointsStale"))

    def test_e2e_and_api_both_fresh_when_both_inputs_present(self) -> None:
        """When both Playwright and Bruno artefacts are present, both
        coverage keys are computed fresh and neither is stale."""
        pw = self._playwright_results_json(expected=30, unexpected=1, skipped=0, flaky=0)
        br = self._bruno_results_json(total_requests=50, passed_requests=48, failed_requests=2)
        data = self._run(
            live={
                "coverage": {
                    "e2eTests": 99,
                    "apiEndpoints": 99,
                    "lastUpdated": "2026-10-01",
                }
            },
            playwright_results=pw,
            bruno_results=br,
        )
        coverage = data["coverage"]
        # e2eTests = 30 + 1 + 0 + 0 = 31
        self.assertEqual(coverage["e2eTests"], 31)
        self.assertIsNone(coverage.get("e2eTestsStale"))
        # apiEndpoints = 50
        self.assertEqual(coverage["apiEndpoints"], 50)
        self.assertIsNone(coverage.get("apiEndpointsStale"))

    def test_e2e_fallback_to_live_marked_stale_when_playwright_missing(self) -> None:
        """When Playwright results are absent, e2eTests falls back to the
        last live value and is visibly marked stale."""
        data = self._run(
            live={
                "coverage": {
                    "e2eTests": 40,
                    "apiEndpoints": 20,
                    "lastUpdated": "2026-11-05",
                }
            },
        )
        coverage = data["coverage"]
        self.assertEqual(coverage["e2eTests"], 40)
        # Stale flag must be present (boolean or date string)
        self.assertTrue(
            coverage.get("e2eTestsStale") is True
            or isinstance(coverage.get("e2eTestsStale"), str),
            f"e2eTestsStale should be set; got {coverage.get('e2eTestsStale')!r}",
        )

    def test_api_fallback_to_live_marked_stale_when_bruno_missing(self) -> None:
        """When Bruno results are absent, apiEndpoints falls back to the
        last live value and is visibly marked stale."""
        data = self._run(
            live={
                "coverage": {
                    "e2eTests": 10,
                    "apiEndpoints": 30,
                    "lastUpdated": "2026-11-05",
                }
            },
        )
        coverage = data["coverage"]
        self.assertEqual(coverage["apiEndpoints"], 30)
        # Stale flag must be present
        self.assertTrue(
            coverage.get("apiEndpointsStale") is True
            or isinstance(coverage.get("apiEndpointsStale"), str),
            f"apiEndpointsStale should be set; got {coverage.get('apiEndpointsStale')!r}",
        )

    def test_e2e_fresh_but_api_stale_when_only_playwright_present(self) -> None:
        """Mixed scenario: Playwright present (fresh e2eTests), Bruno absent
        (stale apiEndpoints fallback)."""
        pw = self._playwright_results_json(expected=20, unexpected=0, skipped=0, flaky=0)
        data = self._run(
            live={
                "coverage": {
                    "e2eTests": 99,
                    "apiEndpoints": 50,
                    "lastUpdated": "2026-11-05",
                }
            },
            playwright_results=pw,
        )
        coverage = data["coverage"]
        # e2eTests fresh = 20
        self.assertEqual(coverage["e2eTests"], 20)
        self.assertIsNone(coverage.get("e2eTestsStale"))
        # apiEndpoints stale fallback = 50
        self.assertEqual(coverage["apiEndpoints"], 50)
        self.assertTrue(
            coverage.get("apiEndpointsStale") is True
            or isinstance(coverage.get("apiEndpointsStale"), str),
        )

    def test_existing_coverage_keys_unchanged_with_new_inputs(self) -> None:
        """Passing Playwright / Bruno inputs must not disturb the existing
        coverage keys (backendInstruction, backendBranch, frontendComponent,
        lastUpdated) when their own snapshots are also present."""
        pw = self._playwright_results_json(expected=15, unexpected=0)
        br = self._bruno_results_json(total_requests=40)
        data = self._run(
            live={},
            backend_snapshot={
                "backendInstruction": 88,
                "backendBranch": 72,
                "lastUpdated": "2026-11-20",
            },
            frontend_summary={"total": {"statements": {"pct": 85.3}}},
            playwright_results=pw,
            bruno_results=br,
        )
        coverage = data["coverage"]
        self.assertEqual(coverage["backendInstruction"], 88)
        self.assertEqual(coverage["backendBranch"], 72)
        self.assertEqual(coverage["frontendComponent"], 85)
        self.assertEqual(coverage["lastUpdated"], "2026-11-20")
        # New keys should also be correct
        self.assertEqual(coverage["e2eTests"], 15)
        self.assertEqual(coverage["apiEndpoints"], 40)

    def test_no_stale_flag_when_no_live_fallback_needed(self) -> None:
        """When there is no live metrics to fall back to and no fresh input,
        e2eTests / apiEndpoints should be null — no stale flag on a null value."""
        data = self._run(live={})
        coverage = data["coverage"]
        self.assertIsNone(coverage["e2eTests"])
        self.assertIsNone(coverage["apiEndpoints"])
        # Stale flags should not appear when value is null
        self.assertNotIn("e2eTestsStale", coverage)
        self.assertNotIn("apiEndpointsStale", coverage)


if __name__ == "__main__":
    unittest.main()
