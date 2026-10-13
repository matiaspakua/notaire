#!/usr/bin/env python3
"""Guards #1624 (CU76): one merge-authority rule across the governance docs and harness.

The Foreman may merge a Worker's PR only when every required check is green and
`check-heavy-ci.sh` passes; Constitution amendments and `sdlc-exception` PRs stay
Owner-merged. Every agent-facing document must state that same rule.
"""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
FOREMAN_SH = REPO_ROOT / "local-ai" / "sdlc" / "foreman.sh"
RULE_DOCS = (
    REPO_ROOT / "CONSTITUTION.md",
    REPO_ROOT / "CONSTITUTION-AGENT-CARD.md",
    REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md",
    REPO_ROOT / ".claude" / "agents" / "cloud-foreman.md",
    REPO_ROOT / "docs" / "300-development" / "304-ai-sdlc-cloud" / "CI-MERGE-GATE.md",
)
OWNER_ONLY_PHRASES = (
    "agents never merge on their own authority",
    "approves specifications and merges prs;",
)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


class MergeAuthorityTest(unittest.TestCase):
    def test_local_foreman_runs_heavy_ci_gate_before_merging(self):
        body = read(FOREMAN_SH).split("merge_and_close() {", 1)[1].split("\n}\n", 1)[0]
        self.assertIn("check-heavy-ci.sh", body, "foreman merge must call check-heavy-ci.sh (#1624)")
        self.assertLess(body.index("check-heavy-ci.sh"), body.index("gh pr merge"))

    def test_rule_docs_name_the_foreman_merge_rule(self):
        for path in RULE_DOCS:
            with self.subTest(doc=path.name):
                text = read(path)
                self.assertIn("check-heavy-ci.sh", text)
                self.assertRegex(text, re.compile(r"foreman merge rule", re.IGNORECASE))

    def test_no_doc_keeps_the_owner_only_merge_rule(self):
        for path in RULE_DOCS:
            text = read(path).lower()
            for phrase in OWNER_ONLY_PHRASES:
                with self.subTest(doc=path.name, phrase=phrase):
                    self.assertNotIn(phrase, text)

    def test_constitution_keeps_owner_only_merges_for_amendments_and_exceptions(self):
        text = read(REPO_ROOT / "CONSTITUTION.md")
        rule = text.split("**Foreman merge rule.**", 1)[1].split("\n\n", 1)[0]
        self.assertIn("sdlc-exception", rule)
        self.assertIn("Constitution", rule)


if __name__ == "__main__":
    unittest.main()
