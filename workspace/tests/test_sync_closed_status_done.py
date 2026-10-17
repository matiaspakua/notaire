"""Tests for sync_closed_status_done.py (#1878)."""

from __future__ import annotations

import importlib.util
import os
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SYNC = ROOT / "workspace" / "sdlc" / "sync_closed_status_done.py"
COHERENCE = ROOT / "workspace" / "sdlc" / "check-board-status-label-coherence.py"


def load(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[name] = mod
    spec.loader.exec_module(mod)
    return mod


class PlanSyncTests(unittest.TestCase):
    def test_closed_non_done_is_planned(self):
        sync = load("sync_closed_status_done", SYNC)
        coherence = load("board_coherence_for_sync", COHERENCE)
        items = [
            coherence.BoardItem(1, "CLOSED", "In Progress", frozenset()),
            coherence.BoardItem(2, "CLOSED", "Done", frozenset()),
            coherence.BoardItem(3, "OPEN", "Todo", frozenset()),
        ]
        planned = sync.plan_sync(items)
        self.assertEqual([item.number for item in planned], [1])


if __name__ == "__main__":
    unittest.main()
