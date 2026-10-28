"""#1958: stack-agnostic AI SDLC gate runner (TDD)."""

from __future__ import annotations

import re
import subprocess
import sys
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[3]
RUNNER = REPO / "framework" / "ai-sdlc" / "run_gates.py"
REPORT = REPO / "ai_sdlc_framework.md"
SKILLS_README = REPO / ".claude" / "skills" / "README.md"
CASE_STUDY_NAV = REPO / "github-page" / "lib" / "ai-sdlc-content.ts"


class RunGatesTest(unittest.TestCase):
    def run_runner(self, *args: str) -> subprocess.CompletedProcess[str]:
        return subprocess.run(
            [sys.executable, str(RUNNER), *args],
            cwd=REPO,
            capture_output=True,
            text=True,
            timeout=30,
        )

    def test_loads_notaire_profile(self):
        result = self.run_runner("--profile", "notaire", "--list")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("notaire", result.stdout.lower())
        self.assertRegex(result.stdout, r"gate|sdlc|preflight", result.stdout)

    def test_unknown_profile_exits_nonzero(self):
        result = self.run_runner("--profile", "does-not-exist", "--list")
        self.assertNotEqual(result.returncode, 0)
        self.assertRegex(
            (result.stderr + result.stdout).lower(),
            r"unknown|not found|does-not-exist",
        )

    def test_runner_source_has_no_product_path_hardcodes(self):
        text = RUNNER.read_text(encoding="utf-8")
        for banned in ("backend-api/", "frontend/", "docs/100-business", "CU-"):
            self.assertNotIn(banned, text, f"kit runner must not hardcode {banned!r}")


class ReportAndSurfaceTest(unittest.TestCase):
    REQUIRED_REPORT_HEADINGS = (
        "## Purpose",
        "## Principles",
        "## Non-Goals",
        "## Components",
        "## Workflow",
        "## Roles",
        "## Gates",
        "## State",
        "## Observability",
        "## Cost",
        "## Adaptation",
        "## Derivation from Notaire evidence",
    )

    def test_report_exists_with_required_sections(self):
        self.assertTrue(REPORT.is_file(), f"missing {REPORT}")
        text = REPORT.read_text(encoding="utf-8")
        for heading in self.REQUIRED_REPORT_HEADINGS:
            self.assertIn(heading, text, f"missing heading {heading}")
        self.assertIn("```mermaid", text)

    def test_skills_index_lists_ui_design(self):
        text = SKILLS_README.read_text(encoding="utf-8")
        self.assertRegex(text, r"ui-design")

    def test_case_study_route_is_in_factory_nav(self):
        text = CASE_STUDY_NAV.read_text(encoding="utf-8")
        self.assertIn("/ai-sdlc/case-study", text)


if __name__ == "__main__":
    unittest.main()
