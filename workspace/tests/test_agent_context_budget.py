#!/usr/bin/env python3
"""Guards #1259 / #1197 P0.4 — always-loaded agent context ≤8k tokens.

`aisdlc budget` measures the always-loaded set declared in `.aisdlc/aisdlc.toml` `[context]` (#2458).
"""
from __future__ import annotations

import subprocess
import tomllib
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
AISDLC = REPO_ROOT / "framework" / "ai-sdlc" / "bin" / "aisdlc"
PROFILE = REPO_ROOT / ".aisdlc" / "aisdlc.toml"
CARD = REPO_ROOT / "CONSTITUTION-AGENT-CARD.md"
FRONTEND_SKILL = REPO_ROOT / ".claude" / "skills" / "frontend-design" / "SKILL.md"
MAX_TOKENS = 8000
MAX_CARD_BYTES = 3072


def run_budget():
    return subprocess.run(["python3", str(AISDLC), "--root", str(REPO_ROOT), "--profile", str(PROFILE), "budget"],
                          capture_output=True, text=True)


class AgentContextBudgetTest(unittest.TestCase):
    def test_profile_declares_the_budget(self):
        context = tomllib.loads(PROFILE.read_text(encoding="utf-8"))["context"]
        self.assertEqual(MAX_TOKENS, context["max_tokens"])
        self.assertIn("AGENTS.md", context["files"])

    def test_always_loaded_within_budget(self):
        result = run_budget()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

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
