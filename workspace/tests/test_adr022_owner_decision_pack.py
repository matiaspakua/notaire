#!/usr/bin/env python3
"""Guards #1261 / #1197 P0.6: ADR-022 packages Owner decision options without choosing."""
from __future__ import annotations

import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
ADR_022 = os.path.join(
    REPO_ROOT,
    "docs",
    "200-architecture",
    "202-ADR",
    "ADR-022-git-history-rewrite-and-large-binaries.md",
)
PAGES_ARCH = os.path.join(REPO_ROOT, "github-page", "app", "docs", "architecture", "page.tsx")


class Adr022OwnerDecisionPackTest(unittest.TestCase):
    def test_adr022_documents_pending_1261_options(self):
        with open(ADR_022, encoding="utf-8") as fh:
            text = fh.read()
        self.assertTrue("#1435" in text or "#1261" in text, "ADR-022 must reference live Owner issue")
        self.assertRegex(text, r"(?i)pending owner decision")
        for opt in ("Option A", "Option B", "Option C"):
            self.assertIn(opt, text, f"ADR-022 must list {opt}")
        self.assertNotRegex(
            text,
            r"(?i)#1261[^\n]{0,80}(removed deprecated|history purge completed|filter-repo completed)",
        )

    def test_pages_architecture_links_adr022(self):
        with open(PAGES_ARCH, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn('id: "022"', text)
        self.assertIn("ADR-022-git-history-rewrite-and-large-binaries.md", text)
        self.assertTrue("#1435" in text or "#1261" in text, "ADR-022 must reference live Owner issue")


if __name__ == "__main__":
    unittest.main()
