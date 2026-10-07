#!/usr/bin/env python3
"""Assert CI concurrency cancel-in-progress flags (#1148 / CU76)."""

from __future__ import annotations

import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]


def concurrency_cancel_flag(workflow_path: Path) -> bool | None:
    """Return cancel-in-progress under the first top-level concurrency block."""
    in_concurrency = False
    for raw in workflow_path.read_text(encoding="utf-8").splitlines():
        if raw.startswith("concurrency:"):
            in_concurrency = True
            continue
        if in_concurrency:
            stripped = raw.strip()
            if stripped.startswith("cancel-in-progress:"):
                value = stripped.split(":", 1)[1].strip()
                return value == "true"
            # left the concurrency mapping
            if raw and not raw[0].isspace() and not raw.startswith("#"):
                break
    return None


class WorkflowConcurrencyTest(unittest.TestCase):
    def test_ci_yml_cancels_in_progress(self) -> None:
        path = REPO_ROOT / ".github/workflows/ci.yml"
        self.assertTrue(path.is_file(), path)
        self.assertIs(concurrency_cancel_flag(path), True)

    def test_playwright_yml_cancels_in_progress(self) -> None:
        path = REPO_ROOT / ".github/workflows/playwright-e2e.yml"
        self.assertTrue(path.is_file(), path)
        self.assertIs(concurrency_cancel_flag(path), True)

    def test_deploy_pages_does_not_cancel(self) -> None:
        path = REPO_ROOT / ".github/workflows/deploy-github-page.yml"
        self.assertTrue(path.is_file(), path)
        self.assertIs(concurrency_cancel_flag(path), False)


if __name__ == "__main__":
    unittest.main()
