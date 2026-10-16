#!/usr/bin/env python3
"""Guards #1907 (CU76): post-#1859 fleet lessons deltas land in agent-load paths."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CLOUD = REPO_ROOT / "docs" / "300-development" / "304-ai-sdlc-cloud"
LESSONS = CLOUD / "FLEET-LESSONS-HEX-1693.md"
MAC_VS_CI = CLOUD / "MAC-LOCAL-VS-CI.md"
MERGE_GATE = CLOUD / "CI-MERGE-GATE.md"
STEWARD = REPO_ROOT / ".claude" / "skills" / "steward" / "SKILL.md"
FOREMAN = REPO_ROOT / ".claude" / "agents" / "cloud-foreman.md"
HEX_RULE = REPO_ROOT / ".claude" / "rules" / "hexagonal-architecture.md"
VALIDATE = REPO_ROOT / "workspace" / "sdlc" / "validate-sdlc-plan.sh"
DOCKERFILE = REPO_ROOT / "backend-api" / "Dockerfile"
WORKFLOW = REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md"


class FleetLessons1907DeltaTest(unittest.TestCase):
    def test_r2_path_hygiene_documented(self):
        text = LESSONS.read_text(encoding="utf-8")
        lower = text.lower()
        for needle in (
            "adapter.out.persistence",
            "english",
            "path",
            "inventory",
            "process checks",
        ):
            self.assertIn(needle, lower, f"lessons must cover R2 path hygiene: {needle!r}")
        # Explicit post-#1859 package-move checklist (seen on #1894).
        self.assertRegex(
            text,
            re.compile(r"docs/tests|english.?local|path ref", re.I),
        )

    def test_human_paste_templates_present(self):
        text = LESSONS.read_text(encoding="utf-8")
        self.assertIn("human paste", text.lower())
        self.assertRegex(text, re.compile(r"```(?:text)?\s*\nIssue #", re.M))
        self.assertIn("Status", text)
        self.assertIn("close", text.lower())

    def test_performance_cost_section(self):
        text = LESSONS.read_text(encoding="utf-8")
        lower = text.lower()
        for needle in ("cost", "serialize", "docs-only", "domain-sized", "main tip"):
            self.assertIn(needle, lower, f"cost section must mention {needle!r}")

    def test_mac_green_is_not_ci_green(self):
        text = MAC_VS_CI.read_text(encoding="utf-8")
        lower = text.lower()
        self.assertTrue(
            "mac green" in lower or "does not replace" in lower or "not ci green" in lower,
            "MAC-LOCAL-VS-CI must state Mac green ≠ CI green",
        )
        self.assertIn("check-heavy-ci", text)

    def test_steward_and_foreman_cite_delta_topics(self):
        steward = STEWARD.read_text(encoding="utf-8")
        foreman = FOREMAN.read_text(encoding="utf-8")
        self.assertIn("adapter.out.persistence", steward + HEX_RULE.read_text(encoding="utf-8"))
        self.assertTrue(
            "paste" in steward.lower() and ("template" in steward.lower() or "Issue #" in steward),
            "steward should point at paste templates",
        )
        self.assertIn("cost", (LESSONS.read_text(encoding="utf-8") + foreman).lower())

    def test_dockerfile_already_has_u_flag(self):
        text = DOCKERFILE.read_text(encoding="utf-8")
        self.assertIn("go-offline", text)
        self.assertRegex(text, re.compile(r"go-offline.*-U|mvn package.*-U", re.S))

    def test_validate_sdlc_plan_retry_hardened(self):
        text = VALIDATE.read_text(encoding="utf-8")
        # Post-#1859: at least 3 attempts; delta may raise the cap.
        self.assertRegex(
            text,
            re.compile(r"max_attempts\s*=\s*[3-9]|for\s+\w+\s+in\s+1\s+2\s+3(?:\s+4\s+5)?", re.I),
        )
        self.assertIn("retry", text.lower())

    def test_merge_gate_or_workflow_points_at_cost_or_path_hygiene(self):
        merged = (
            MERGE_GATE.read_text(encoding="utf-8")
            + WORKFLOW.read_text(encoding="utf-8")
            + LESSONS.read_text(encoding="utf-8")
        )
        lower = merged.lower()
        self.assertIn("path", lower)
        self.assertTrue("cost" in lower or "serialize" in lower)


if __name__ == "__main__":
    unittest.main()
