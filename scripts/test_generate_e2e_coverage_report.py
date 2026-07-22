#!/usr/bin/env python3
"""
Tests for generate_e2e_coverage_report.build_report (issue #587).

Plain stdlib unittest — no extra dependency, consistent with this project's
other one-off CI scripts (generate-coverage-snapshot.py) which also have no
test framework wired in. Run with: python3 scripts/test_generate_e2e_coverage_report.py
"""
import unittest

from generate_e2e_coverage_report import build_report

OLD_HARDCODED_ACTION_ITEM = "Fix backend 500 errors on testimonio endpoints (CU07, CU08)"


def playwright_result(expected=10, unexpected=0, skipped=0, flaky=0, failing_specs=None):
    specs = []
    for title, file in (failing_specs or []):
        specs.append({"ok": False, "title": title, "file": file})
    return {
        "stats": {"expected": expected, "unexpected": unexpected, "skipped": skipped, "flaky": flaky},
        "suites": [{"specs": specs, "suites": []}],
    }


def bruno_result(total=20, passed=20, failed=0):
    return {"summary": {"totalRequests": total, "passedRequests": passed, "failedRequests": failed}}


class BuildReportTest(unittest.TestCase):
    def test_reports_real_pass_counts_not_the_old_hardcoded_template(self):
        report = build_report(
            pw_data=playwright_result(expected=42, unexpected=0),
            bruno_data=bruno_result(),
            report_date="2026-07-22",
            trigger="pull_request",
        )
        self.assertIn("42", report)
        self.assertNotIn(OLD_HARDCODED_ACTION_ITEM, report)

    def test_lists_actual_failing_test_titles_as_action_items(self):
        report = build_report(
            pw_data=playwright_result(
                expected=9,
                unexpected=1,
                failing_specs=[("should reject invalid login", "auth.spec.ts")],
            ),
            bruno_data=bruno_result(),
            report_date="2026-07-22",
            trigger="pull_request",
        )
        self.assertIn("should reject invalid login", report)
        self.assertIn("auth.spec.ts", report)

    def test_reports_no_action_items_when_everything_passed(self):
        report = build_report(
            pw_data=playwright_result(expected=15, unexpected=0),
            bruno_data=bruno_result(),
            report_date="2026-07-22",
            trigger="schedule",
        )
        self.assertIn("No action items", report)

    def test_handles_missing_playwright_results_honestly(self):
        report = build_report(
            pw_data=None,
            bruno_data=bruno_result(),
            report_date="2026-07-22",
            trigger="workflow_dispatch",
        )
        self.assertIn("No Playwright results", report)
        self.assertNotIn(OLD_HARDCODED_ACTION_ITEM, report)

    def test_handles_missing_bruno_results_honestly(self):
        report = build_report(
            pw_data=playwright_result(),
            bruno_data=None,
            report_date="2026-07-22",
            trigger="push",
        )
        self.assertIn("No Bruno", report)

    def test_includes_trigger_and_date(self):
        report = build_report(
            pw_data=playwright_result(),
            bruno_data=bruno_result(),
            report_date="2026-07-22",
            trigger="pull_request",
        )
        self.assertIn("2026-07-22", report)
        self.assertIn("pull_request", report)


if __name__ == "__main__":
    unittest.main()
