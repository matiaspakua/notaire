#!/usr/bin/env python3
"""Every Playwright project must match at least one test (#1983 / CU76)."""

from __future__ import annotations

import re
import subprocess
import unittest
from pathlib import Path

E2E_ROOT = Path(__file__).resolve().parents[1] / "e2e"
CONFIG = E2E_ROOT / "playwright.config.ts"


def read_config() -> str:
    return CONFIG.read_text(encoding="utf-8")


class PlaywrightProjectsNonEmptyTest(unittest.TestCase):
    def test_mobile_project_targets_real_mobile_specs(self) -> None:
        config = read_config()
        self.assertNotIn(
            "mobile-viewport.spec.ts",
            config,
            "mobile project must not point at the missing mobile-viewport.spec.ts (#1983)",
        )
        self.assertRegex(
            config,
            r'name:\s*["\']mobile["\'][\s\S]*?testMatch:\s*["\']\*\*/\*mobile\*\.spec\.ts["\']',
            "mobile project must match **/*mobile*.spec.ts",
        )

    def test_chromium_excludes_mobile_specs(self) -> None:
        config = read_config()
        # Either testIgnore for mobile specs, or an explicit comment+pattern in chromium.
        self.assertTrue(
            re.search(r'testIgnore:\s*\[[^\]]*mobile', config)
            or re.search(r'chromium[\s\S]*?testIgnore:[\s\S]*?mobile', config),
            "chromium must exclude *mobile* specs so they run only under the mobile project (#1983)",
        )

    def test_each_named_project_lists_at_least_one_test(self) -> None:
        """Fail when a Playwright project matches zero tests (acceptance criterion #1983)."""
        playwright = E2E_ROOT / "node_modules" / ".bin" / "playwright"
        # Always use the tree-local CLI. Never `npx --no-install` (loads a global
        # Playwright that cannot resolve this tree's `@playwright/test`). Process
        # Checks does not preinstall testing/e2e — npm ci here when missing.
        if not playwright.is_file():
            install = subprocess.run(
                ["npm", "ci"],
                cwd=E2E_ROOT,
                capture_output=True,
                text=True,
                check=False,
                timeout=300,
            )
            self.assertEqual(
                install.returncode,
                0,
                "testing/e2e npm ci failed while installing Playwright:\n"
                f"{install.stdout}\n{install.stderr}",
            )
        self.assertTrue(
            playwright.is_file(),
            "testing/e2e/node_modules/.bin/playwright missing after npm ci",
        )
        # Default reporters (incl. coverage-report) hide the listing; force `list`.
        listed = subprocess.run(
            [str(playwright), "test", "--list", "--reporter=list"],
            cwd=E2E_ROOT,
            capture_output=True,
            text=True,
            check=False,
            timeout=120,
        )
        self.assertEqual(
            listed.returncode,
            0,
            f"playwright --list failed:\n{listed.stdout}\n{listed.stderr}",
        )
        # Lines look like: "  [chromium] › tests/TS-….spec.ts:12:5 › title"
        by_project: dict[str, int] = {}
        for line in listed.stdout.splitlines():
            match = re.search(r"\[([^\]]+)\]", line)
            if not match:
                continue
            name = match.group(1)
            by_project[name] = by_project.get(name, 0) + 1
        # Config declares these four projects; each must appear with ≥1 test.
        for name in ("smoke", "health", "chromium", "mobile"):
            count = by_project.get(name, 0)
            self.assertGreater(
                count,
                0,
                f"Playwright project '{name}' matches zero tests (#1983). Got counts={by_project}",
            )


if __name__ == "__main__":
    unittest.main()
