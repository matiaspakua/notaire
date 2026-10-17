"""Closes/Fixes guard for multi_pr and Owner-pending OpenSpec changes (#1878)."""

from __future__ import annotations

import importlib.util
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GUARD = ROOT / "workspace" / "sdlc" / "check-closes-refs-guard.py"


def load():
    spec = importlib.util.spec_from_file_location("closes_refs_guard", GUARD)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = mod
    spec.loader.exec_module(mod)
    return mod


class ClosesRefsGuardTests(unittest.TestCase):
    def test_closes_blocked_on_multi_pr_change(self):
        mod = load()
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            change = root / "fix-9999-demo"
            change.mkdir()
            (change / ".openspec.yaml").write_text("schema: notaire-sdlc\nmulti_pr: true\n")
            (change / "proposal.md").write_text("| GitHub Issue | #9999 |\n")
            errors = mod.check(body="Closes #9999", changes_root=root)
            self.assertTrue(errors)
            self.assertTrue(any("Refs #9999" in e for e in errors))

    def test_refs_passes_on_multi_pr_change(self):
        mod = load()
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            change = root / "fix-9999-demo"
            change.mkdir()
            (change / ".openspec.yaml").write_text("schema: notaire-sdlc\nmulti_pr: true\n")
            (change / "proposal.md").write_text("| GitHub Issue | #9999 |\n")
            self.assertEqual(mod.check(body="Refs #9999", changes_root=root), [])

    def test_owner_pending_blocks_closes(self):
        mod = load()
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            change = root / "fix-8888-demo"
            change.mkdir()
            (change / ".openspec.yaml").write_text("schema: notaire-sdlc\n")
            (change / "tasks.md").write_text("- [ ] owner-pending: live apply\n#8888\n")
            errors = mod.check(body="Fixes #8888", changes_root=root)
            self.assertTrue(any("#8888" in e for e in errors))


if __name__ == "__main__":
    unittest.main()
