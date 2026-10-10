#!/usr/bin/env python3
"""Guards #1259 / #1197 P0.4 — always-loaded agent context ≤8k tokens.

Requires workspace/ci/agent-context-budget.py and CONSTITUTION-AGENT-CARD.md.
"""
from __future__ import annotations

import importlib.util
import os
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
BUDGET_SCRIPT = REPO_ROOT / "workspace" / "ci" / "agent-context-budget.py"
CARD = REPO_ROOT / "CONSTITUTION-AGENT-CARD.md"
FRONTEND_SKILL = REPO_ROOT / ".claude" / "skills" / "frontend-design" / "SKILL.md"
MAX_TOKENS = 8000
MAX_CARD_BYTES = 3072


def _load_budget():
    spec = importlib.util.spec_from_file_location("agent_context_budget", BUDGET_SCRIPT)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod


class AgentContextBudgetTest(unittest.TestCase):
    def test_budget_script_exists(self):
        self.assertTrue(BUDGET_SCRIPT.is_file(), f"missing {BUDGET_SCRIPT}")

    def test_always_loaded_within_budget(self):
        mod = _load_budget()
        data = mod.measure(REPO_ROOT)
        self.assertLessEqual(
            data["tokens_est"],
            MAX_TOKENS,
            f"always-loaded tokens_est={data['tokens_est']} exceeds {MAX_TOKENS}: {data}",
        )
        missing = [r["path"] for r in data["files"] if r["missing"]]
        self.assertEqual(missing, [], f"missing always-loaded files: {missing}")

    def test_constitution_agent_card_small(self):
        self.assertTrue(CARD.is_file())
        size = CARD.stat().st_size
        self.assertLessEqual(size, MAX_CARD_BYTES, f"card {size} > {MAX_CARD_BYTES}")
        text = CARD.read_text(encoding="utf-8")
        self.assertIn("CONSTITUTION.md", text)

    def test_frontend_design_skill_not_always_apply(self):
        text = FRONTEND_SKILL.read_text(encoding="utf-8")
        # Frontmatter may omit alwaysApply or set false; true is forbidden.
        if text.lstrip().startswith("---"):
            fm = text.lstrip()[3:].split("---", 1)[0]
            self.assertNotRegex(
                fm,
                r"(?mi)^\s*alwaysApply\s*:\s*true\s*$",
                "frontend-design skill must not be alwaysApply: true (#1259)",
            )


if __name__ == "__main__":
    unittest.main()
