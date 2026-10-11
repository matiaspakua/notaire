#!/usr/bin/env python3
"""
Guards issue #1499: a broad .gitignore pattern silently dropped source code. `reports/` (meant for the
pipeline output at the repository root) also matched frontend/src/app/dashboard/reports/, so the page was
never committed after the English rename. Source directories must never be ignored; generated output must be.

Run with: python3 workspace/tests/test_gitignore_keeps_source_dirs.py
"""
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]


def is_ignored(path):
    return subprocess.run(
        ["git", "check-ignore", "-q", path], cwd=REPO_ROOT, capture_output=True
    ).returncode == 0


class GitignoreTest(unittest.TestCase):
    def test_application_source_directories_named_reports_are_not_ignored(self):
        self.assertFalse(is_ignored("frontend/src/app/dashboard/reports/page.tsx"))

    def test_pipeline_output_is_still_ignored(self):
        self.assertTrue(is_ignored("reports/pipeline/20261010/index.html"))


if __name__ == "__main__":
    unittest.main()
