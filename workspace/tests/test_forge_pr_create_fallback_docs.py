#!/usr/bin/env python3
"""Guards #2231 (CU76): forge PR-create 403 runbook stays durable and honest."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CLOUD = REPO_ROOT / "docs" / "300-development" / "304-ai-sdlc-cloud"
FLEET_IDENTITY = CLOUD / "FLEET-IDENTITY.md"
FLEET_LESSONS = CLOUD / "FLEET-LESSONS-HEX-1693.md"
STEWARD = REPO_ROOT / ".claude" / "skills" / "steward" / "SKILL.md"
SKILL = REPO_ROOT / ".claude" / "skills" / "ai-agent-workflow" / "SKILL.md"
RULE = REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md"
FOREMAN = REPO_ROOT / ".claude" / "agents" / "cloud-foreman.md"


class ForgePrCreateFallbackDocsTest(unittest.TestCase):
    def _cloud_blob(self) -> str:
        return (
            FLEET_IDENTITY.read_text(encoding="utf-8")
            + "\n"
            + FLEET_LESSONS.read_text(encoding="utf-8")
        )

    def test_docs_name_createPullRequest_friction(self) -> None:
        """Scenario: docs name createPullRequest friction."""
        blob = self._cloud_blob()
        self.assertIn("createPullRequest", blob)
        self.assertRegex(blob, r"403|Resource not accessible|permission", re.I)
        self.assertRegex(blob, r"ambient|Cursor.*gh|gh pr create", re.I)

    def test_docs_prefer_manage_pull_request_on_cursor_tip(self) -> None:
        """Scenario: docs prefer ManagePullRequest on cursor tip."""
        blob = self._cloud_blob()
        self.assertRegex(blob, r"ManagePullRequest|Cursor PR tools", re.I)
        self.assertRegex(blob, r"cursor/", re.I)

    def test_docs_give_owner_compare_link_fallback(self) -> None:
        """Scenario: docs give Owner compare-link fallback."""
        blob = self._cloud_blob()
        self.assertRegex(blob, r"compare|compare-link|compare URL", re.I)
        self.assertRegex(blob, r"Owner|owner", re.I)
        self.assertRegex(blob, r"Closes\s*#", re.I)

    def test_docs_keep_fleet_app_optional_no_false_pr_grant(self) -> None:
        """Scenario: FLEET_APP optional; do not claim PR scopes were granted."""
        identity = FLEET_IDENTITY.read_text(encoding="utf-8")
        self.assertIn("FLEET_APP_ID", identity)
        self.assertIn("FLEET_APP_PRIVATE_KEY", identity)
        self.assertRegex(identity, r"optional|Without the two variables|when .* exist", re.I)
        # Must not claim PR-create was already granted for ambient/fleet mid-run.
        self.assertNotRegex(
            identity,
            r"(?i)fleet App (PR|pull.?request).*(already )?(granted|enabled)|"
            r"createPullRequest.*(already )?(granted|enabled)",
        )

    def test_workflow_points_at_cloud_docs(self) -> None:
        """Scenario: steward or workflow points at cloud docs."""
        pointers = (
            STEWARD.read_text(encoding="utf-8")
            + "\n"
            + SKILL.read_text(encoding="utf-8")
            + "\n"
            + RULE.read_text(encoding="utf-8")
            + "\n"
            + FOREMAN.read_text(encoding="utf-8")
        )
        self.assertRegex(
            pointers,
            r"304-ai-sdlc-cloud|FLEET-IDENTITY|FLEET-LESSONS",
        )
        self.assertRegex(
            pointers,
            r"ManagePullRequest|createPullRequest|compare",
            re.I,
        )


if __name__ == "__main__":
    unittest.main()
