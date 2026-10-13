#!/usr/bin/env python3
"""Governance principles KISS and Boy Scout (#1591)."""

from __future__ import annotations

import re
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]

GOVERNED_FILES = (
    "CONSTITUTION.md",
    "CONSTITUTION-AGENT-CARD.md",
    "AGENTS.md",
    ".claude/rules/ai-agent-workflow.md",
    "docs/300-development/304-ai-sdlc-cloud/README.md",
    "docs/300-development/INGENIERIA.md",
    "docs/openspec/NOTAIRE-ADAPTATIONS.md",
    "infra/deploy/kustomize/README.md",
    "local-ai/sdlc/prompts/05-implement.md",
)

KIS_WORD = re.compile(r"\bKIS\b")


class KissSpellingTest(unittest.TestCase):
    def test_constitution_p2_is_kiss(self):
        text = (REPO_ROOT / "CONSTITUTION.md").read_text(encoding="utf-8")
        self.assertIn("KISS — Keep It Simple", text)
        principles = text.split("## 2. Engineering Principles", 1)[1].split("## 3.", 1)[0]
        self.assertNotRegex(principles, KIS_WORD)

    def test_governed_files_have_no_bare_kis(self):
        offenders = []
        for relative in GOVERNED_FILES:
            path = REPO_ROOT / relative
            self.assertTrue(path.is_file(), relative)
            if KIS_WORD.search(path.read_text(encoding="utf-8")):
                offenders.append(relative)
        self.assertEqual(offenders, [])


class BoyScoutPrincipleTest(unittest.TestCase):
    def test_p11_bounds_and_stable_numbers(self):
        text = (REPO_ROOT / "CONSTITUTION.md").read_text(encoding="utf-8")
        self.assertIn("| P10 |", text)
        self.assertIn("| P11 |", text)
        self.assertNotIn("| P12 |", text.split("## 3.", 1)[0])
        p11 = text.split("| P11 |", 1)[1].split("\n", 1)[0]
        for needle in ("same PR", "no behavior change", "P10"):
            self.assertIn(needle, p11, msg=needle)


class DigestBudgetTest(unittest.TestCase):
    def test_digests_mention_kiss_and_boy_scout(self):
        for relative in (
            "CONSTITUTION-AGENT-CARD.md",
            "AGENTS.md",
            ".claude/rules/ai-agent-workflow.md",
        ):
            text = (REPO_ROOT / relative).read_text(encoding="utf-8")
            self.assertIn("KISS", text)
            self.assertIn("Boy Scout", text)
            self.assertIn("CONSTITUTION", text)

    def test_context_budget_script_passes(self):
        result = subprocess.run(
            [
                "python3",
                "workspace/ci/agent-context-budget.py",
                "--max-tokens",
                "8000",
            ],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
        )
        self.assertEqual(result.returncode, 0, msg=result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()
