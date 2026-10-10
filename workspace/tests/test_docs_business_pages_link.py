#!/usr/bin/env python3
"""Guards public Business Docs link + archived OpenSpec packaging trees."""
from __future__ import annotations

import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
README = os.path.join(REPO_ROOT, "docs", "100-business", "README.md")
ARCHIVE = os.path.join(REPO_ROOT, "docs", "openspec", "changes", "archive")


class DocsBusinessPagesLinkTest(unittest.TestCase):
    def test_business_readme_links_pages_business_docs(self):
        with open(README, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("matiaspakua.github.io/notaire/docs/business", text)

    def test_shipped_openspec_packaging_changes_archived(self):
        names = os.listdir(ARCHIVE)
        for needle in (
            "docs-1441-pages-business",
            "docs-1443-owner-tracker",
            "docs-1445-owner-umbrella",
        ):
            self.assertTrue(
                any(needle in n for n in names),
                f"expected archived change containing {needle}, got {names[-10:]}",
            )


if __name__ == "__main__":
    unittest.main()
