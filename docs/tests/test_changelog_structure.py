#!/usr/bin/env python3
"""
Guards issue #1185 (CU76): a release section of CHANGELOG.md must not repeat a
`###` heading (markdownlint MD024, siblings_only), which kept the pipeline red.

Plain stdlib unittest, consistent with infra/tests/test_prod_compose.py.
Run with: python3 docs/tests/test_changelog_structure.py
"""
import re
import unittest
from collections import Counter
from pathlib import Path

CHANGELOG = Path(__file__).resolve().parents[2] / "CHANGELOG.md"
RELEASE_HEADING = re.compile(r"^## ")
SUBSECTION_HEADING = re.compile(r"^### (.+?)\s*$")


def subsection_headings_by_release():
    releases = {}
    current = None
    in_fence = False
    for line in CHANGELOG.read_text(encoding="utf-8").splitlines():
        if line.lstrip().startswith("```"):
            in_fence = not in_fence
        if in_fence:
            continue
        if RELEASE_HEADING.match(line):
            current = line.strip()
            releases[current] = []
        elif current and (match := SUBSECTION_HEADING.match(line)):
            releases[current].append(match.group(1))
    return releases


class ChangelogStructureTest(unittest.TestCase):
    def test_each_release_has_unique_subsection_headings(self):
        duplicated = {
            release: sorted(h for h, n in Counter(headings).items() if n > 1)
            for release, headings in subsection_headings_by_release().items()
        }
        duplicated = {r: h for r, h in duplicated.items() if h}
        self.assertEqual({}, duplicated, f"repeated ### headings per release: {duplicated}")

    def test_unreleased_section_exists(self):
        self.assertIn("## [Unreleased]", subsection_headings_by_release())


if __name__ == "__main__":
    unittest.main()
