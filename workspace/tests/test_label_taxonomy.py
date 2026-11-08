#!/usr/bin/env python3
"""#1860: phase 1 of the label taxonomy — every reader accepts old AND new names."""

from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path

SDLC = Path(__file__).resolve().parents[2] / "workspace" / "sdlc"
sys.path.insert(0, str(SDLC))

import label_taxonomy as lt  # noqa: E402
from milestone_policy import lane  # noqa: E402


def load(name: str, file: str):
    spec = importlib.util.spec_from_file_location(name, SDLC / file)
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


claim = load("claim_issue_tax", "claim-issue.py")
coherence = load("coherence_tax", "check-board-status-label-coherence.py")


class ExpandTests(unittest.TestCase):
    def test_new_name_brings_its_legacy_name(self):
        self.assertIn("BACKEND", lt.expand({"area:backend"}))
        self.assertIn("in-progress", lt.expand({"status:in-progress"}))
        self.assertIn("requerimiento-funcional", lt.expand({"trace:rf"}))

    def test_old_name_brings_its_new_name(self):
        self.assertIn("status:ready", lt.expand({"ready-for-dev"}))
        self.assertIn("type:chore", lt.expand({"TAREA"}))
        self.assertIn("concern:tech-debt", lt.expand({"tech-debt"}))

    def test_merged_new_name_maps_back_to_every_legacy_name(self):
        self.assertTrue({"chore", "TAREA"} <= lt.expand({"type:chore"}))

    def test_every_rename_target_is_prefixed(self):
        for new in lt.RENAMES.values():
            self.assertRegex(new, r"^[a-z]+:[a-z0-9-]+$")

    def test_type_count_counts_merged_names_once(self):
        self.assertEqual(lt.type_labels({"TAREA", "chore"}), {"type:chore"})
        self.assertEqual(lt.type_labels({"bug", "type:feature"}), {"type:bug", "type:feature"})

    def test_delete_list_has_phase_labels_and_no_bot_labels(self):
        self.assertIn("phase:5-observability", lt.DELETE)
        for bot in ("dependencies", "java", "javascript", "maven", "autorelease: pending"):
            self.assertNotIn(bot, lt.DELETE)
            self.assertNotIn(bot, lt.RENAMES)


class LaneAcceptsNewNames(unittest.TestCase):
    def test_new_area_names_derive_the_same_lane(self):
        self.assertEqual(lane({"area:backend", "area:frontend"}, 1, "x"), "cross")
        self.assertEqual(lane({"concern:security"}, 1, "x"), "security")
        self.assertEqual(lane({"trace:cu"}, 1, "x"), "catalog")
        self.assertEqual(lane({"type:epic", "area:backend"}, 1, "x"), "epic")


class ClaimRules(unittest.TestCase):
    def issue(self, *labels):
        return {"number": 5, "state": "OPEN", "title": "fix: y", "labels": list(labels)}

    def test_claim_requires_ready(self):
        self.assertIn("ready", claim.refusal(self.issue("BACKEND", "priority:high")))

    def test_claim_accepts_old_or_new_ready(self):
        self.assertIsNone(claim.refusal(self.issue("BACKEND", "ready-for-dev")))
        self.assertIsNone(claim.refusal(self.issue("area:backend", "status:ready")))

    def test_claim_refuses_new_blocked_names(self):
        self.assertIsNotNone(claim.refusal(self.issue("BACKEND", "status:ready", "status:blocked")))
        self.assertIsNotNone(claim.refusal(self.issue("BACKEND", "status:ready", "status:needs-info")))

    def test_wip_label_follows_the_repo(self):
        self.assertEqual(lt.wip_label({"in-progress", "bug"}), "in-progress")
        self.assertEqual(lt.wip_label({"status:in-progress", "in-progress"}), "status:in-progress")


class CoherenceRules(unittest.TestCase):
    def item(self, *labels, status="In Progress", state="OPEN", open_prs=None):
        return coherence.BoardItem(1, state, status, frozenset(labels), open_prs=open_prs)

    def test_new_wip_name_satisfies_r2(self):
        self.assertEqual(coherence.evaluate_item(self.item("status:in-progress")), [])

    def test_closed_with_new_wip_name_fails(self):
        self.assertTrue(coherence.evaluate_item(self.item("status:in-progress", status="Done", state="CLOSED")))

    def test_r6_in_review_needs_open_pr(self):
        errors = coherence.evaluate_item(self.item("in-progress", "status:in-review", open_prs=0))
        self.assertTrue(any("R6" in e for e in errors))
        self.assertEqual(coherence.evaluate_item(self.item("in-progress", "in-review", open_prs=1)), [])

    def test_r7_ready_and_blocked_exclusive(self):
        errors = coherence.evaluate_item(self.item("ready-for-dev", "status:blocked", status="Todo"))
        self.assertTrue(any("R7" in e for e in errors))

    def test_r8_ready_issue_has_at_most_one_type(self):
        errors = coherence.evaluate_item(self.item("status:ready", "bug", "type:feature", status="Todo"))
        self.assertTrue(any("R8" in e for e in errors))
        self.assertEqual(coherence.evaluate_item(self.item("status:ready", "TAREA", "chore", status="Todo")), [])


if __name__ == "__main__":
    unittest.main()
