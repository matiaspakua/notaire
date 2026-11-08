#!/usr/bin/env python3
"""Guards #1858 (CU76): #1693 hex-fleet lessons land in durable agent-load paths."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CLOUD = REPO_ROOT / "docs" / "300-development" / "304-ai-sdlc-cloud"
LESSONS = CLOUD / "FLEET-LESSONS-HEX-1693.md"
MAC_VS_CI = CLOUD / "MAC-LOCAL-VS-CI.md"
README = CLOUD / "README.md"
MERGE_GATE = CLOUD / "CI-MERGE-GATE.md"
STEWARD = REPO_ROOT / ".claude" / "skills" / "steward" / "SKILL.md"
FOREMAN = REPO_ROOT / ".claude" / "agents" / "cloud-foreman.md"
HEX_RULE = REPO_ROOT / ".claude" / "rules" / "hexagonal-architecture.md"
VALIDATE = REPO_ROOT / "workspace" / "sdlc" / "validate-sdlc-plan.sh"
AGENTS = REPO_ROOT / "AGENTS.md"


class FleetLessons1858Test(unittest.TestCase):
    def test_lessons_doc_exists_with_required_topics(self):
        self.assertTrue(LESSONS.is_file(), f"missing {LESSONS}")
        text = LESSONS.read_text(encoding="utf-8")
        lower = text.lower()
        for needle in (
            "heavy-ci",
            "never rebase",
            "changelog",
            "unreleased",
            "inventory",
            "process checks",
            "one domain",
            "human paste",
            "probe issue",
            "eslint-suppressions",
            "docker",
            "go-offline",
        ):
            self.assertIn(needle, lower, f"lessons doc must mention {needle!r}")

    def test_mac_vs_ci_doc_exists(self):
        self.assertTrue(MAC_VS_CI.is_file(), f"missing {MAC_VS_CI}")
        text = MAC_VS_CI.read_text(encoding="utf-8")
        for needle in ("ArchUnit", "Playwright", "CodeQL", "Bruno", "gh"):
            self.assertIn(needle, text)

    def test_readme_indexes_new_docs(self):
        text = README.read_text(encoding="utf-8")
        self.assertIn("FLEET-LESSONS-HEX-1693.md", text)
        self.assertIn("MAC-LOCAL-VS-CI.md", text)

    def test_merge_gate_has_changelog_fence_and_sync(self):
        text = MERGE_GATE.read_text(encoding="utf-8")
        self.assertIn("Unreleased", text)
        self.assertIn("sync", text.lower())
        self.assertIn("Process Checks", text)

    def test_steward_covers_new_fences(self):
        text = STEWARD.read_text(encoding="utf-8")
        for needle in (
            "Unreleased",
            "Process Checks",
            "FLEET-LESSONS-HEX-1693.md",
            "human paste",
        ):
            self.assertIn(needle, text)

    def test_foreman_and_hex_rule_encode_slice_discipline(self):
        foreman = FOREMAN.read_text(encoding="utf-8")
        hex_rule = HEX_RULE.read_text(encoding="utf-8")
        self.assertIn("CHANGELOG", foreman)
        self.assertIn("one domain", hex_rule.lower())
        self.assertIn("eslint-suppressions", hex_rule)

    def test_validate_sdlc_plan_retries_gh_lookup(self):
        text = VALIDATE.read_text(encoding="utf-8")
        self.assertRegex(
            text,
            re.compile(r"check_issue_live|gh issue view", re.M),
        )
        self.assertIn("retry", text.lower())
        # Bounded retry loop — avoid infinite wait on real missing issues.
        self.assertRegex(text, re.compile(r"for\s+\w+\s+in\s+1\s+2\s+3|attempt|max_attempts|RETRY", re.I))

    def test_agents_md_points_at_lessons(self):
        text = AGENTS.read_text(encoding="utf-8")
        self.assertIn("FLEET-LESSONS-HEX-1693", text)


if __name__ == "__main__":
    unittest.main()
