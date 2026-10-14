#!/usr/bin/env python3
"""Guards #1662 (CU76): the steward skill exists, is well formed and cites real paths."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SKILL = REPO_ROOT / ".claude" / "skills" / "steward" / "SKILL.md"
REPO_DIRS = ("workspace/", "docs/", ".claude/", ".github/", "security/", "infra/", "testing/")
PATH_TOKEN = re.compile(r"`([A-Za-z0-9_.\-/]+)`")


class StewardSkillTest(unittest.TestCase):
    def setUp(self):
        self.text = SKILL.read_text(encoding="utf-8")

    def test_skill_has_frontmatter(self):
        match = re.match(r"---\nname: steward\ndescription: (.+)\n---\n", self.text)
        self.assertIsNotNone(match, "frontmatter must name steward and describe it")

    def test_cited_paths_exist(self):
        cited = {t for t in PATH_TOKEN.findall(self.text) if t.startswith(REPO_DIRS)}
        self.assertTrue(cited, "the skill must cite repository paths")
        missing = sorted(p for p in cited if not (REPO_ROOT / p).exists())
        self.assertEqual([], missing)

    def test_skill_covers_the_required_topics(self):
        for needle in (
            "check-heavy-ci.sh",
            "merge `origin/main`",
            "never rebase",
            "archive",
            "Workers never merge",
            "git ls-remote --heads origin",
        ):
            self.assertIn(needle, self.text)


if __name__ == "__main__":
    unittest.main()
