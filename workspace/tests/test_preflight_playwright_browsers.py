#!/usr/bin/env python3
"""preflight must drop an empty PLAYWRIGHT_BROWSERS_PATH override (#1862 hygiene)."""
import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PREFLIGHT = os.path.join(REPO_ROOT, "workspace", "sdlc", "preflight.sh")


class PreflightPlaywrightBrowsersTest(unittest.TestCase):
    def test_clears_unusable_playwright_browsers_path(self):
        with open(PREFLIGHT, encoding="utf-8") as f:
            body = f.read()
        self.assertIn("PLAYWRIGHT_BROWSERS_PATH", body)
        self.assertIn("chrome-headless-shell", body)
        self.assertIn("unset PLAYWRIGHT_BROWSERS_PATH", body)


if __name__ == "__main__":
    unittest.main()
