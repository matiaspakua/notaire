#!/usr/bin/env python3
"""Gate 2 tests for board Status ↔ issue state ↔ labels coherence (#1550, CU76).

Covers the normative rules and wiring into Process Checks / preflight.

Run: python3 -m unittest workspace.tests.test_board_status_label_coherence
"""

from __future__ import annotations

import importlib.util
import os
import sys
import unittest
from pathlib import Path

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CHECKER = os.path.join(
    REPO_ROOT, "workspace", "sdlc", "check-board-status-label-coherence.py"
)
CHECKER_REL = "workspace/sdlc/check-board-status-label-coherence.py"
PREFLIGHT = os.path.join(REPO_ROOT, "workspace", "sdlc", "preflight.sh")
SDLC_PROCESS = os.path.join(REPO_ROOT, ".github", "workflows", "sdlc-process.yml")
CONSTITUTION = os.path.join(REPO_ROOT, "CONSTITUTION.md")
AGENT_CARD = os.path.join(REPO_ROOT, "CONSTITUTION-AGENT-CARD.md")
MERGE_GATE = os.path.join(
    REPO_ROOT, "docs", "300-development", "304-ai-sdlc-cloud", "CI-MERGE-GATE.md"
)
FLEET_README = os.path.join(
    REPO_ROOT, "docs", "300-development", "304-ai-sdlc-cloud", "README.md"
)
GITHUB_BOARD = os.path.join(REPO_ROOT, "docs", "github", "README.md")


def load_checker():
    spec = importlib.util.spec_from_file_location(
        "check_board_status_label_coherence", CHECKER
    )
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = mod
    spec.loader.exec_module(mod)
    return mod


def item(mod, *, number=1, state="OPEN", status=None, labels=(), title="t"):
    return mod.BoardItem(
        number=number,
        state=state,
        status=status,
        labels=frozenset(labels),
        title=title,
    )


class CoherenceRulesTest(unittest.TestCase):
    """Normative board ↔ issue ↔ label rules."""

    def test_closed_must_be_done_not_in_progress(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="CLOSED", status="In Progress", labels=())
        )
        self.assertTrue(errors)
        self.assertTrue(any("Done" in e or "CLOSED" in e for e in errors))

    def test_closed_must_be_done_not_todo(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="CLOSED", status="Todo", labels=())
        )
        self.assertTrue(errors)

    def test_closed_done_without_wip_label_passes(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="CLOSED", status="Done", labels=("chore",))
        )
        self.assertEqual(errors, [])

    def test_closed_must_drop_in_progress_label(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="CLOSED", status="Done", labels=("in-progress",))
        )
        self.assertTrue(errors)
        self.assertTrue(any("in-progress" in e for e in errors))

    def test_open_in_progress_requires_label(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="In Progress", labels=())
        )
        self.assertTrue(errors)
        self.assertTrue(any("in-progress" in e for e in errors))

    def test_open_in_progress_with_label_passes(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="In Progress", labels=("in-progress",))
        )
        self.assertEqual(errors, [])

    def test_open_label_requires_in_progress_status(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="Todo", labels=("in-progress",))
        )
        self.assertTrue(errors)
        self.assertTrue(any("In Progress" in e for e in errors))

    def test_blocked_open_must_not_be_done(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="Done", labels=("blocked",))
        )
        self.assertTrue(errors)
        self.assertTrue(any("blocked" in e.lower() for e in errors))

    def test_blocked_closed_should_drop_blocked_label(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="CLOSED", status="Done", labels=("blocked", "chore"))
        )
        self.assertTrue(any("blocked" in e for e in errors))

    def test_closed_empty_status_fails(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="CLOSED", status=None, labels=())
        )
        self.assertTrue(errors)

    def test_open_blocked_plus_in_progress_flagged(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(
                mod,
                state="OPEN",
                status="In Progress",
                labels=("blocked", "in-progress"),
            )
        )
        self.assertTrue(any("blocked" in e and "in-progress" in e for e in errors))

    def test_done_must_not_carry_in_progress(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="Done", labels=("in-progress",))
        )
        self.assertTrue(len(errors) >= 1)

    def test_done_prefers_closed(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="Done", labels=())
        )
        self.assertTrue(errors)
        self.assertTrue(any("CLOSED" in e or "close" in e.lower() for e in errors))

    def test_open_todo_without_wip_passes(self):
        mod = load_checker()
        errors = mod.evaluate_item(
            item(mod, state="OPEN", status="Todo", labels=("DOC",))
        )
        self.assertEqual(errors, [])

    def test_status_none_skips_status_rules_but_checks_closed_label(self):
        mod = load_checker()
        ok = mod.evaluate_item(item(mod, state="OPEN", status=None, labels=()))
        self.assertEqual(ok, [])
        bad = mod.evaluate_item(
            item(mod, state="CLOSED", status=None, labels=("in-progress",))
        )
        self.assertTrue(bad)


class EvaluateManyTest(unittest.TestCase):
    def test_collects_numbered_violations(self):
        mod = load_checker()
        items = [
            item(mod, number=10, state="CLOSED", status="In Progress"),
            item(mod, number=11, state="OPEN", status="In Progress", labels=("in-progress",)),
        ]
        errors = mod.evaluate_items(items)
        self.assertTrue(any("#10" in e for e in errors))
        self.assertFalse(any("#11" in e for e in errors))


class DocGuardTest(unittest.TestCase):
    def test_constitution_names_board_coherence_gate(self):
        text = Path(CONSTITUTION).read_text(encoding="utf-8")
        self.assertIn("check-board-status-label-coherence", text)
        self.assertRegex(text, r"[Bb]oard.*[Ss]tatus|[Ii]ssue.*label.*coher")

    def test_agent_card_points_at_board_coherence(self):
        text = Path(AGENT_CARD).read_text(encoding="utf-8")
        self.assertIn("board", text.lower())
        self.assertIn("check-board-status-label-coherence", text)

    def test_merge_gate_or_fleet_docs_mention_board_sync(self):
        merge = Path(MERGE_GATE).read_text(encoding="utf-8").lower()
        fleet = Path(FLEET_README).read_text(encoding="utf-8").lower()
        combined = merge + fleet
        self.assertTrue(
            "board" in combined
            and (
                "check-board-status-label-coherence" in combined
                or "status" in combined and "label" in combined
            )
        )

    def test_github_board_doc_states_contract(self):
        text = Path(GITHUB_BOARD).read_text(encoding="utf-8")
        self.assertIn("in-progress", text)
        self.assertIn("Done", text)


class WiringTest(unittest.TestCase):
    def test_checker_file_exists(self):
        self.assertTrue(os.path.isfile(CHECKER), CHECKER)

    def test_sdlc_process_invokes_checker(self):
        text = Path(SDLC_PROCESS).read_text(encoding="utf-8")
        self.assertIn(CHECKER_REL, text)

    def test_preflight_lists_and_runs_checker(self):
        text = Path(PREFLIGHT).read_text(encoding="utf-8")
        self.assertIn(CHECKER_REL, text)
        self.assertIn("board", text.lower())


class SkipWithoutGhTest(unittest.TestCase):
    def test_skip_when_forced(self):
        mod = load_checker()
        code, msg = mod.run_check(scope="wip", force_skip=True)
        self.assertEqual(code, 0)
        self.assertRegex(msg.lower(), r"skip")


if __name__ == "__main__":
    unittest.main()
