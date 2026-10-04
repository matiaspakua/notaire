#!/usr/bin/env python3
"""
Guards issue #921 / CU76: relative Markdown links in the active documentation resolve.

External URLs are not fetched (slow and flaky in CI). Archives are skipped. Every exemption must carry
a reason, ideally an issue.

Run with: python3 scripts/test_docs_links.py
"""
import re
import unittest
from pathlib import Path
from urllib.parse import unquote

REPO_ROOT = Path(__file__).resolve().parents[1]
ROOT_DOCS = ("README.md", "AGENTS.md", "CLAUDE.md", "CONSTITUTION.md", "CHANGELOG.md")
SKIPPED_DIRS = ("docs/000-archive", "docs/archive")
LINK = re.compile(r"\[[^\]]*\]\(([^)\s]+)")
FENCE = re.compile(r"```.*?```", re.DOTALL)
EXEMPT_REASONS = {
    ("README.md", "LICENSE"): "no license file yet; choosing one is the Owner's decision (issue #1226)",
}


def active_documents():
    docs = [p for p in (REPO_ROOT / "docs").rglob("*.md")
            if not any(p.relative_to(REPO_ROOT).as_posix().startswith(s) for s in SKIPPED_DIRS)]
    return sorted(docs + [REPO_ROOT / name for name in ROOT_DOCS])


def broken_links():
    broken = []
    for path in active_documents():
        text = FENCE.sub("", path.read_text(encoding="utf-8", errors="ignore"))
        for target in LINK.findall(text):
            if re.match(r"(https?:|mailto:|#)", target):
                continue
            relative = unquote(target.split("#")[0])
            if not relative:
                continue
            if not (path.parent / relative).resolve().exists():
                broken.append((path.relative_to(REPO_ROOT).as_posix(), target))
    return broken


class DocumentationLinksTest(unittest.TestCase):
    def test_relative_links_resolve(self):
        unexpected = [b for b in broken_links() if b not in EXEMPT_REASONS]
        self.assertEqual([], unexpected)

    def test_exemptions_are_still_needed_and_explained(self):
        current = set(broken_links())
        for exemption, reason in EXEMPT_REASONS.items():
            with self.subTest(exemption=exemption):
                self.assertIn(exemption, current, "stale exemption: the link now resolves, remove it")
                self.assertTrue(reason.strip())

    def test_audit_report_exists_and_is_linked_from_the_development_index(self):
        report = REPO_ROOT / "docs" / "300-development" / "DOCUMENTATION-AUDIT-2026-10.md"
        self.assertTrue(report.is_file())
        index = (REPO_ROOT / "docs" / "README.md").read_text(encoding="utf-8")
        self.assertIn("DOCUMENTATION-AUDIT-2026-10.md", index)


if __name__ == "__main__":
    unittest.main()
