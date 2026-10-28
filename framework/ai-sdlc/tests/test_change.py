"""Built-in change records: complete sections and a real, resolvable issue (#1958)."""
from __future__ import annotations

import re
import unittest

from helpers import Repo, output

PROFILE = """
    [spec]
    dir = "changes"
    sections = ["Why", "Acceptance scenarios", "Test plan"]
"""


def fill(text: str) -> str:
    return re.sub(r"<!--.*?-->", "Filled in.", text, flags=re.S)


class ChangeRecordTest(unittest.TestCase):
    def setUp(self):
        self.repo = Repo(self, PROFILE)

    def new(self, name: str = "add-login", issue: str = "12") -> str:
        result = self.repo.run("change", "new", name, "--issue", issue, "--title", "Add login")
        self.assertEqual(0, result.returncode, output(result))
        return (self.repo.root / "changes" / f"{name}.md").read_text(encoding="utf-8")

    def test_new_scaffolds_every_required_section(self):
        text = self.new()
        for section in ("Why", "Acceptance scenarios", "Test plan"):
            self.assertIn(f"## {section}", text)
        self.assertIn("#12", text)

    def test_new_refuses_to_overwrite(self):
        self.new()
        self.assertEqual(2, self.repo.run("change", "new", "add-login", "--issue", "12").returncode)

    def test_unfilled_section_fails_naming_it(self):
        self.new()
        result = self.repo.run("change", "check")
        self.assertEqual(1, result.returncode)
        self.assertIn("Why", output(result))

    def test_filled_record_passes(self):
        path = self.repo.root / "changes" / "add-login.md"
        path.write_text(fill(self.new()), encoding="utf-8")
        result = self.repo.run("change", "check", "add-login")
        self.assertEqual(0, result.returncode, output(result))

    def test_missing_section_fails(self):
        path = self.repo.root / "changes" / "add-login.md"
        path.write_text(fill(self.new()).replace("## Test plan", "## Something else"), encoding="utf-8")
        result = self.repo.run("change", "check")
        self.assertEqual(1, result.returncode)
        self.assertIn("Test plan", output(result))

    def test_record_without_issue_reference_fails(self):
        path = self.repo.root / "changes" / "add-login.md"
        path.write_text(fill(self.new()).replace("#12", "TBD"), encoding="utf-8")
        result = self.repo.run("change", "check")
        self.assertEqual(1, result.returncode)
        self.assertIn("issue", output(result).lower())


class LiveIssueVerificationTest(unittest.TestCase):
    def record(self, verify: str) -> Repo:
        repo = Repo(self, PROFILE + f'\n[tracker]\nverify = "{verify}"\n')
        repo.run("change", "new", "fix-bug", "--issue", "999999")
        path = repo.root / "changes" / "fix-bug.md"
        path.write_text(fill(path.read_text(encoding="utf-8")), encoding="utf-8")
        return repo

    def test_invented_issue_number_fails(self):
        repo = self.record("test {issue} -lt 1000")
        result = repo.run("change", "check")
        self.assertEqual(1, result.returncode)
        self.assertIn("999999", output(result))

    def test_resolvable_issue_passes(self):
        repo = self.record("test {issue} -gt 0")
        self.assertEqual(0, repo.run("change", "check").returncode)


if __name__ == "__main__":
    unittest.main()
