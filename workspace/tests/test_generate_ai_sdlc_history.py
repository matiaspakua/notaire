#!/usr/bin/env python3
"""Guards #1958 (CU76): the Pages case-study history is aggregated, not hand-typed.

workspace/ci/generate-ai-sdlc-history.py turns merged PRs, issues and git history into
github-page/lib/ai-sdlc-history.json. These tests pin the aggregation rules on fixtures.

Run with: python3 workspace/tests/test_generate_ai_sdlc_history.py
"""
from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "ci" / "generate-ai-sdlc-history.py"


def load():
    spec = importlib.util.spec_from_file_location("generate_ai_sdlc_history", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def pr(title: str, merged: str, author: str = "owner") -> dict:
    return {"title": title, "mergedAt": merged, "author": {"login": author}}


def issue(created: str, *labels: str) -> dict:
    return {"createdAt": created, "labels": [{"name": n} for n in labels]}


class HistoryAggregationTest(unittest.TestCase):
    def setUp(self):
        self.gen = load()

    def test_dependabot_prs_are_not_counted(self):
        stats = self.gen.pr_stats([pr("feat: a", "2026-09-01T00:00:00Z"),
                                   pr("chore(deps): bump", "2026-09-02T00:00:00Z", "app/dependabot")])
        self.assertEqual(1, stats["2026-09"]["merged_prs"])

    def test_process_prs_are_those_about_the_sdlc_itself(self):
        stats = self.gen.pr_stats([
            pr("[#1] chore(sdlc): archive OpenSpec change", "2026-10-01T00:00:00Z"),
            pr("[#2] docs(agents): fleet lessons", "2026-10-02T00:00:00Z"),
            pr("[#3] feat(budget): totals", "2026-10-03T00:00:00Z"),
        ])
        self.assertEqual(3, stats["2026-10"]["merged_prs"])
        self.assertEqual(2, stats["2026-10"]["process_prs"])

    def test_bug_issues_are_counted_by_creation_month(self):
        bugs = self.gen.bug_stats([issue("2026-07-05T00:00:00Z", "type:bug"),
                                   issue("2026-07-06T00:00:00Z", "type:feature"),
                                   issue("2026-08-01T00:00:00Z", "type:bug", "area:backend")])
        self.assertEqual({"2026-07": 1, "2026-08": 1}, bugs)

    def test_months_merge_every_source_and_start_at_since(self):
        months = self.gen.merge_months(
            commits={"2014-03": 7, "2026-09": 10, "2026-10": 20},
            prs={"2026-10": {"merged_prs": 5, "process_prs": 1}},
            bugs={"2026-09": 2},
            since="2026-09",
        )
        self.assertEqual(["2026-09", "2026-10"], [m["month"] for m in months])
        self.assertEqual({"month": "2026-10", "commits": 20, "merged_prs": 5, "process_prs": 1, "bug_issues": 0},
                         months[1])

    def test_snapshot_records_provenance(self):
        snapshot = self.gen.snapshot(months=[], mechanisms=[], legacy_commits=3, totals={})
        self.assertIn("generate-ai-sdlc-history.py", snapshot["generated_by"])
        self.assertIn("generated_at", snapshot)
        self.assertEqual(3, snapshot["legacy_commits"])


if __name__ == "__main__":
    unittest.main()
