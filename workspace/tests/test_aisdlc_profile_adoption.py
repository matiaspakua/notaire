#!/usr/bin/env python3
"""Guards #2052 (CU76): Notaire's profile adopts the cross-repo AI SDLC checks.

Owner-only paths are enforced at merge, the evidence chain is verified there, the write set and
review budget run before the PR, and the agent protocol is written where agents read it.
"""

from __future__ import annotations

import fnmatch
import os
import subprocess
import sys
import tomllib
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
PROFILE = REPO_ROOT / ".aisdlc" / "aisdlc.toml"
RULE = REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md"
PR_TEMPLATE = REPO_ROOT / ".github" / "PULL_REQUEST_TEMPLATE.md"
PROPOSAL_TEMPLATE = REPO_ROOT / "docs" / "openspec" / "schemas" / "notaire-sdlc" / "templates" / "proposal.md"
KIT = REPO_ROOT / "framework" / "ai-sdlc" / "bin" / "aisdlc"
ENV = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}


def tracked_files() -> list[str]:
    out = subprocess.run(["git", "-C", str(REPO_ROOT), "ls-files"], capture_output=True, text=True, env=ENV)
    return out.stdout.split()


def matches(path: str, pattern: str) -> bool:
    return fnmatch.fnmatchcase(path, pattern) or (pattern.startswith("**/") and fnmatch.fnmatchcase(path, pattern[3:]))


class ProfileAdoptionTest(unittest.TestCase):
    def setUp(self):
        self.profile = tomllib.loads(PROFILE.read_text(encoding="utf-8"))

    def gates(self, stage: str) -> dict[str, dict]:
        return {g["run"]: g for g in self.profile["gates"] if g["stage"] == stage}

    def test_policy_and_merge_path_are_owner_only(self):
        paths = self.profile["protected"]["paths"]
        for required in ("CONSTITUTION.md", "AGENTS.md", ".aisdlc/**", "framework/ai-sdlc/**",
                         ".claude/hooks/**", "workspace/sdlc/check-heavy-ci.sh"):
            self.assertIn(required, paths)

    def test_every_owner_only_glob_matches_a_tracked_file(self):
        files = tracked_files()
        if not files:
            self.skipTest("not a git checkout")
        dead = [p for p in self.profile["protected"]["paths"] if not any(matches(f, p) for f in files)]
        self.assertEqual([], dead, "a typo'd owner-only glob protects nothing")

    def test_merge_stage_runs_protected_and_evidence_verify_as_required(self):
        merge = self.gates("merge")
        for run in ("aisdlc protected --base {base}", "aisdlc evidence verify"):
            self.assertIn(run, merge)
            self.assertTrue(merge[run].get("required", True))

    def test_pre_pr_runs_scope_and_size_as_advisory(self):
        pre_pr = self.gates("pre-pr")
        for run in ("aisdlc scope --base {base}", "aisdlc size --base {base}"):
            self.assertIn(run, pre_pr)
            self.assertFalse(pre_pr[run]["required"])

    def test_doctor_accepts_the_profile(self):
        result = subprocess.run([sys.executable, str(KIT), "--root", str(REPO_ROOT), "--profile", str(PROFILE), "doctor"],
                                capture_output=True, text=True, env=ENV)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)


class AgentProtocolDocumentedTest(unittest.TestCase):
    def test_workflow_rule_carries_the_protocol(self):
        rule = RULE.read_text(encoding="utf-8")
        for marker in ("Decision packet", "Deviations from the approved OpenSpec change", "Bounded corrections",
                       "RED per rule, PRESERVE per touched behavior", "Risk line", "--anchor"):
            self.assertIn(marker, rule)

    def test_pr_template_asks_for_risk_deviations_and_ai_assistance(self):
        template = PR_TEMPLATE.read_text(encoding="utf-8")
        for marker in ("## Deviations from the approved change", "## AI assistance", "## Review budget", "Risk: none"):
            self.assertIn(marker, template)

    def test_proposal_template_offers_a_write_set(self):
        self.assertIn("### Write set", PROPOSAL_TEMPLATE.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
