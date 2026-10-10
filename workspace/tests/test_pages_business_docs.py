#!/usr/bin/env python3
"""Guards #1441 / #1197: Pages Docs surface includes business knowledge entry points."""
from __future__ import annotations

import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CHROME = os.path.join(REPO_ROOT, "github-page", "components", "DocsChrome.tsx")
HOME = os.path.join(REPO_ROOT, "github-page", "app", "docs", "page.tsx")
BUSINESS = os.path.join(REPO_ROOT, "github-page", "app", "docs", "business", "page.tsx")


class PagesBusinessDocsTest(unittest.TestCase):
    def test_docs_chrome_nav_includes_business(self):
        with open(CHROME, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn('/docs/business/', text)
        self.assertIn("Business", text)

    def test_docs_home_card_includes_business(self):
        with open(HOME, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn('/docs/business/', text)
        self.assertRegex(text, r"(?i)business")

    def test_business_page_deep_links_100_business(self):
        self.assertTrue(os.path.isfile(BUSINESS), "missing github-page/app/docs/business/page.tsx")
        with open(BUSINESS, encoding="utf-8") as fh:
            text = fh.read()
        for needle in (
            "100-business",
            "101-requirements",
            "102-use-cases",
            "103-actors",
            "104-traceability",
            "105-manuals",
        ):
            self.assertIn(needle, text, f"business page must deep-link {needle}")


if __name__ == "__main__":
    unittest.main()
