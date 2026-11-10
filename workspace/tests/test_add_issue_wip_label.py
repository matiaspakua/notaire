#!/usr/bin/env python3
"""Guards #2221 (CU76): Gate 1.6 add-label is honest on integration 403."""

from __future__ import annotations

import os
import re
import stat
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "sdlc" / "add-issue-wip-label.sh"
FLEET_IDENTITY = (
    REPO_ROOT
    / "docs"
    / "300-development"
    / "304-ai-sdlc-cloud"
    / "FLEET-IDENTITY.md"
)
CHECKLIST = (
    REPO_ROOT
    / "docs"
    / "300-development"
    / "304-ai-sdlc-cloud"
    / "ENVIRONMENT-CHECKLIST.md"
)
SKILL = REPO_ROOT / ".claude" / "skills" / "ai-agent-workflow" / "SKILL.md"
RULE = REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md"
TASKS_TEMPLATE = (
    REPO_ROOT / "docs" / "openspec" / "schemas" / "notaire-sdlc" / "templates" / "tasks.md"
)


class AddIssueWipLabelDocsTest(unittest.TestCase):
    def test_docs_name_issues_write_and_addLabelsToLabelable(self) -> None:
        """Scenario: durable docs name the Issues write / GraphQL scope."""
        identity = FLEET_IDENTITY.read_text(encoding="utf-8")
        checklist = CHECKLIST.read_text(encoding="utf-8")
        blob = identity + "\n" + checklist
        self.assertRegex(blob, r"issues:\s*write|Issues.*[Rr]ead\s*&\s*[Ww]rite", re.I)
        self.assertIn("addLabelsToLabelable", blob)

    def test_docs_say_where_owner_grants_cloud_or_fleet_token(self) -> None:
        """Scenario: docs say where the Owner grants label write."""
        identity = FLEET_IDENTITY.read_text(encoding="utf-8")
        checklist = CHECKLIST.read_text(encoding="utf-8")
        blob = identity + "\n" + checklist
        self.assertRegex(blob, r"Cursor Cloud|GitHub Apps|FLEET_APP|notaire-fleet", re.I)
        self.assertRegex(blob, r"Owner|owner", re.I)

    def test_workflow_points_at_helper_not_raw_gh_only(self) -> None:
        """Scenario: agent workflow uses the honest helper for Gate 1.6."""
        skill = SKILL.read_text(encoding="utf-8")
        rule = RULE.read_text(encoding="utf-8")
        template = TASKS_TEMPLATE.read_text(encoding="utf-8")
        self.assertIn("add-issue-wip-label.sh", skill)
        self.assertIn("add-issue-wip-label.sh", rule)
        self.assertIn("add-issue-wip-label.sh", template)
        self.assertRegex(skill + rule + template, r"SKIP|honest|403|Resource not accessible", re.I)


class AddIssueWipLabelScriptTest(unittest.TestCase):
    def setUp(self) -> None:
        self.assertTrue(SCRIPT.is_file(), "workspace/sdlc/add-issue-wip-label.sh must exist (#2221)")
        self.tmp = tempfile.mkdtemp(prefix="wip-label-")
        self.addCleanup(lambda: __import__("shutil").rmtree(self.tmp, ignore_errors=True))
        self.fake_bin = Path(self.tmp) / "bin"
        self.fake_bin.mkdir()
        self.gh = self.fake_bin / "gh"
        self.log = Path(self.tmp) / "gh.log"

    def _install_gh(self, body: str) -> None:
        self.gh.write_text("#!/usr/bin/env bash\n" + body, encoding="utf-8")
        self.gh.chmod(self.gh.stat().st_mode | stat.S_IXUSR)

    def _run(self, *args: str) -> subprocess.CompletedProcess[str]:
        env = {**os.environ, "PATH": f"{self.fake_bin}:{os.environ.get('PATH', '')}"}
        return subprocess.run(
            ["bash", str(SCRIPT), *args],
            capture_output=True,
            text=True,
            env=env,
            cwd=str(REPO_ROOT),
        )

    def test_permission_denied_is_honest_skip_not_success(self) -> None:
        """Scenario: addLabelsToLabelable 403 → honest SKIP, exit 0, no false labeled."""
        self._install_gh(
            f"""
set -euo pipefail
echo "$*" >> "{self.log}"
if [[ "$*" == label*list* ]]; then
  printf '%s\\n' 'status:in-progress\\tin progress\\t#0075ca'
  exit 0
fi
echo 'GraphQL: Resource not accessible by integration (addLabelsToLabelable)' >&2
echo 'failed to update 1 issue' >&2
exit 1
"""
        )
        result = self._run("2221")
        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        out = result.stdout + result.stderr
        self.assertRegex(out, r"SKIPPED", re.I)
        self.assertNotRegex(out, r"^✓", re.M)
        self.assertNotIn("moved to IN PROGRESS", out)
        self.assertIn("addLabelsToLabelable", out)

    def test_success_reports_labeled(self) -> None:
        """Scenario: when gh can write labels, report success with the WIP label."""
        self._install_gh(
            f"""
set -euo pipefail
echo "$*" >> "{self.log}"
if [[ "$*" == label*list* ]]; then
  printf '%s\\n' 'status:in-progress\\tin progress\\t#0075ca'
  exit 0
fi
exit 0
"""
        )
        result = self._run("2119")
        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        self.assertRegex(result.stdout + result.stderr, r"✓|#2119|status:in-progress")
        self.assertNotRegex(result.stdout + result.stderr, r"SKIPPED", re.I)

    def test_legacy_in_progress_fallback_when_status_missing(self) -> None:
        """Scenario: if only legacy in-progress exists, use that name."""
        self._install_gh(
            f"""
set -euo pipefail
echo "$*" >> "{self.log}"
if [[ "$*" == label*list* ]]; then
  printf '%s\\n' 'in-progress\\tlegacy\\t#0075ca'
  exit 0
fi
if [[ "$*" == *"--add-label"*status:in-progress* ]]; then
  echo "'status:in-progress' not found" >&2
  exit 1
fi
exit 0
"""
        )
        result = self._run("100")
        self.assertEqual(result.returncode, 0, result.stderr + result.stdout)
        log = self.log.read_text(encoding="utf-8")
        self.assertIn("in-progress", log)


if __name__ == "__main__":
    unittest.main()
