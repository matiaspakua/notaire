#!/usr/bin/env python3
"""
Guards issue #1185 (CU76): a release section of CHANGELOG.md must not repeat a
`###` heading (markdownlint MD024, siblings_only), which kept the pipeline red.

Also guards #2033: `[Unreleased]` must not contain `closes #<n>` so Release Please
does not auto-close open multi_pr epics when it opens a release PR.

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
UNRELEASED_HEADING = re.compile(r"^## \[Unreleased\]")
CLOSES_ISSUE = re.compile(r"closes\s+#\d+", re.I)


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


def unreleased_section_text():
    lines = CHANGELOG.read_text(encoding="utf-8").splitlines()
    collecting = False
    out = []
    for line in lines:
        if UNRELEASED_HEADING.match(line.strip()):
            collecting = True
            continue
        if collecting and RELEASE_HEADING.match(line):
            break
        if collecting:
            out.append(line)
    return "\n".join(out)


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

    def test_unreleased_has_no_closes_issue_footer(self):
        """Release Please promotes Unreleased into PRs; `closes #N` auto-closes issues (#2033)."""
        text = unreleased_section_text()
        match = CLOSES_ISSUE.search(text)
        self.assertIsNone(
            match,
            f"Unreleased must not contain closes #N (found {match.group(0) if match else None}); "
            "use Refs in commits and avoid closes until the epic is Done (#2033)",
        )


if __name__ == "__main__":
    unittest.main()
