#!/usr/bin/env python3
"""Guards #1646 (CU76): two agents never own the same issue or a conflicting lane.

Label + Status writes after a read are not atomic. The claim posts a marker,
waits, re-reads, and only the earliest marker since the last release wins.
"""

from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path

SDLC = Path(__file__).resolve().parents[2] / "workspace" / "sdlc"
sys.path.insert(0, str(SDLC))


def load():
    spec = importlib.util.spec_from_file_location("claim_issue", SDLC / "claim-issue.py")
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


claim = load()


def marker(agent: str, nonce: str, at: str) -> dict:
    return {"id": hash(nonce), "body": claim.claim_marker(agent, nonce), "created_at": at}


class FakeGh:
    def __init__(self, issue: dict, wip: list[dict] | None = None, rival_at: str | None = None):
        self.issue_data = issue
        self.wip = wip or []
        self.rival_at = rival_at
        self.comments_by_issue: dict[int, list[dict]] = {issue["number"]: []}
        self.labels = set(issue["labels"])
        self.status: str | None = None
        self.deleted: list[int] = []

    def issue(self, number):
        return {**self.issue_data, "labels": sorted(self.labels)}

    def open_wip(self):
        return self.wip

    def comments(self, number):
        return list(self.comments_by_issue[number])

    def comment(self, number, body):
        if self.rival_at and "notaire-claim" in body:
            self.comments_by_issue[number].append(marker("rival", "r1", self.rival_at))
        entry = {"id": len(self.comments_by_issue[number]) + 100, "body": body, "created_at": "2026-10-13T20:00:05Z"}
        self.comments_by_issue[number].append(entry)
        return entry["id"]

    def delete_comment(self, comment_id):
        self.deleted.append(comment_id)

    def add_label(self, number, label):
        self.labels.add(label)

    def remove_label(self, number, label):
        self.labels.discard(label)

    def open_prs_closing(self, number):
        return 0

    def set_status(self, number, status):
        self.status = status
        return True


def issue(number=10, labels=("BACKEND", "priority:high"), title="fix: x"):
    return {"number": number, "state": "OPEN", "title": title, "labels": list(labels)}


class DecisionTest(unittest.TestCase):
    def test_catalog_epic_blocked_and_needs_info_are_not_claimable(self):
        self.assertIsNotNone(claim.refusal(issue(labels=("CASO-DE-USO",))))
        self.assertIsNotNone(claim.refusal(issue(labels=("roadmap", "BACKEND"))))
        self.assertIsNotNone(claim.refusal(issue(labels=("BACKEND", "blocked"))))
        self.assertIsNotNone(claim.refusal(issue(labels=("BACKEND", "needs-info"))))
        self.assertIsNone(claim.refusal(issue()))

    def test_busy_conflicting_lane_refuses(self):
        security_wip = issue(number=20, labels=("security", "BACKEND", "in-progress"))
        self.assertEqual(claim.lane_holder(issue(), [security_wip]), 20)
        frontend_wip = issue(number=21, labels=("FRONTEND", "in-progress"))
        self.assertIsNone(claim.lane_holder(issue(), [frontend_wip]))

    def test_earliest_marker_since_last_release_wins(self):
        comments = [
            marker("old", "o1", "2026-10-13T09:00:00Z"),
            {"id": 2, "body": claim.RELEASE_MARKER, "created_at": "2026-10-13T10:00:00Z"},
            marker("b", "b1", "2026-10-13T20:00:03Z"),
            marker("a", "a1", "2026-10-13T20:00:01Z"),
        ]
        self.assertEqual(claim.winning_nonce(comments), "a1")

    def test_stale_claim_without_open_pr(self):
        self.assertTrue(claim.is_stale("2026-10-12T08:00:00Z", "2026-10-13T20:00:00Z", hours=24, open_prs=0))
        self.assertFalse(claim.is_stale("2026-10-12T08:00:00Z", "2026-10-13T20:00:00Z", hours=24, open_prs=1))
        self.assertFalse(claim.is_stale("2026-10-13T08:00:00Z", "2026-10-13T20:00:00Z", hours=24, open_prs=0))


class ClaimFlowTest(unittest.TestCase):
    def test_uncontested_claim_wins_and_marks_the_board(self):
        gh = FakeGh(issue())
        self.assertEqual(claim.claim(gh, 10, "agent-a", settle=0), 0)
        self.assertIn("in-progress", gh.labels)
        self.assertEqual(gh.status, "In Progress")

    def test_losing_a_race_withdraws_and_fails(self):
        gh = FakeGh(issue(), rival_at="2026-10-13T20:00:01Z")
        self.assertEqual(claim.claim(gh, 10, "agent-a", settle=0), 1)
        self.assertNotIn("in-progress", gh.labels)
        self.assertTrue(gh.deleted)

    def test_busy_lane_refuses_before_posting(self):
        gh = FakeGh(issue(), wip=[issue(number=20, labels=("BACKEND", "in-progress"))])
        self.assertEqual(claim.claim(gh, 10, "agent-a", settle=0), 1)
        self.assertEqual(gh.comments(10), [])


if __name__ == "__main__":
    unittest.main()
