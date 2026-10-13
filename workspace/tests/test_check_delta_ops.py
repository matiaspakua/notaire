#!/usr/bin/env python3
"""Guards #1623 (CU76): a delta's operation must match the main spec at Gate 1.

`openspec validate --strict` accepts `MODIFIED` for a spec that does not exist;
`openspec archive` then aborts. 22 of 32 shipped changes hit that on 2026-10-13.
"""

from __future__ import annotations

import importlib.util
import shutil
import tempfile
import textwrap
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CHECKER = REPO_ROOT / "workspace" / "sdlc" / "check-delta-ops.py"


def load_checker():
    spec = importlib.util.spec_from_file_location("check_delta_ops", CHECKER)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def requirement_block(header: str, *names: str) -> str:
    body = "".join(
        f"\n### Requirement: {name}\n\nIt SHALL work.\n\n#### Scenario: ok\n\n- **WHEN** x\n- **THEN** y\n"
        for name in names
    )
    return f"## {header} Requirements\n{body}"


class DeltaOpsTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="delta-ops-"))
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        self.change = self.root / "docs" / "openspec" / "changes" / "demo"
        self.checker = load_checker()

    def main_spec(self, capability: str, *names: str) -> None:
        path = self.root / "docs" / "openspec" / "specs" / capability / "spec.md"
        path.parent.mkdir(parents=True)
        path.write_text(f"# {capability}\n\n## Purpose\n\nx\n\n## Requirements\n" + requirement_block("X", *names).split("\n", 1)[1])

    def delta(self, capability: str, text: str) -> None:
        path = self.change / "specs" / capability / "spec.md"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(textwrap.dedent(text))

    def problems(self) -> list[str]:
        return self.checker.find_problems(self.change)

    def test_modified_on_a_missing_spec_fails(self):
        self.delta("brand-new", requirement_block("MODIFIED", "Shows boxes"))
        self.assertTrue(any("brand-new" in p and "MODIFIED" in p for p in self.problems()))

    def test_modified_on_a_missing_requirement_fails(self):
        self.main_spec("ui", "Old rule")
        self.delta("ui", requirement_block("MODIFIED", "New rule"))
        self.assertTrue(any("New rule" in p for p in self.problems()))

    def test_added_requirement_that_already_exists_fails(self):
        self.main_spec("release", "Tag starts delivery")
        self.delta("release", requirement_block("ADDED", "Tag starts delivery"))
        self.assertTrue(any("ADDED" in p and "Tag starts delivery" in p for p in self.problems()))

    def test_matching_operations_pass(self):
        self.main_spec("ui", "Old rule")
        self.delta("ui", requirement_block("MODIFIED", "Old rule") + "\n" + requirement_block("ADDED", "Fresh rule"))
        self.delta("brand-new", requirement_block("ADDED", "Shows boxes"))
        self.assertEqual(self.problems(), [])

    def test_removed_requirement_must_exist(self):
        self.main_spec("ui", "Old rule")
        self.delta("ui", requirement_block("REMOVED", "Ghost rule"))
        self.assertTrue(any("Ghost rule" in p for p in self.problems()))

    def test_plan_validator_runs_the_check(self):
        text = (REPO_ROOT / "workspace" / "sdlc" / "validate-sdlc-plan.sh").read_text(encoding="utf-8")
        self.assertIn("check-delta-ops.py", text)


if __name__ == "__main__":
    unittest.main()
