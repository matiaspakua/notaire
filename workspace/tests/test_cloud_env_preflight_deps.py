#!/usr/bin/env python3
"""Guards #2217 (CU76): cloud install ships python3-venv + Playwright browsers for preflight."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
INSTALL = REPO_ROOT / ".cursor" / "install.sh"
CHECKLIST = (
    REPO_ROOT
    / "docs"
    / "300-development"
    / "304-ai-sdlc-cloud"
    / "ENVIRONMENT-CHECKLIST.md"
)


class CloudEnvPreflightDepsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.install = INSTALL.read_text(encoding="utf-8")
        self.checklist = CHECKLIST.read_text(encoding="utf-8")

    def test_install_sh_installs_python3_venv(self) -> None:
        """Scenario: install.sh installs python3-venv."""
        self.assertRegex(
            self.install,
            r"apt-get install[\s\S]*?\bpython3-venv\b",
            "install.sh must apt-install python3-venv for security/run_gates.py venv",
        )

    def test_install_sh_installs_e2e_chromium_and_webkit(self) -> None:
        """Scenario: install.sh installs e2e chromium and webkit."""
        self.assertRegex(
            self.install,
            r"cd testing/e2e[\s\S]*?playwright install --with-deps[^\n]*chromium[^\n]*webkit"
            r"|cd testing/e2e[\s\S]*?playwright install --with-deps[^\n]*webkit[^\n]*chromium",
            "install.sh must install chromium+webkit under testing/e2e",
        )

    def test_install_sh_installs_github_page_chromium(self) -> None:
        """Scenario: install.sh installs github-page chromium."""
        self.assertRegex(
            self.install,
            r"cd github-page[\s\S]*?playwright install --with-deps[^\n]*chromium",
            "install.sh must install chromium under github-page",
        )

    def test_checklist_documents_venv_and_playwright_browsers(self) -> None:
        """Scenario: checklist documents python3-venv and Playwright browsers."""
        self.assertRegex(self.checklist, r"python3-venv|ensurepip", re.IGNORECASE)
        self.assertRegex(self.checklist, r"webkit", re.IGNORECASE)
        self.assertRegex(self.checklist, r"github-page", re.IGNORECASE)
        self.assertRegex(self.checklist, r"testing/e2e", re.IGNORECASE)


if __name__ == "__main__":
    unittest.main()
