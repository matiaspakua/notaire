"""Context budget, commit subjects and TDD evidence are checked mechanically (#1958)."""
from __future__ import annotations

import unittest

from helpers import Repo, output


class BudgetTest(unittest.TestCase):
    def budget(self, max_tokens: int) -> Repo:
        repo = Repo(self, f"""
            [context]
            max_tokens = {max_tokens}
            files = ["AGENTS.md", "POLICY.md"]
        """)
        repo.write("AGENTS.md", "x" * 400)
        return repo

    def test_over_budget_fails_and_prints_the_total(self):
        result = self.budget(50).run("budget")
        self.assertEqual(1, result.returncode)
        self.assertIn("tokens", output(result))

    def test_within_budget_passes(self):
        self.assertEqual(0, self.budget(1000).run("budget").returncode)

    def test_missing_always_loaded_file_fails(self):
        repo = self.budget(1000)
        repo.remove("AGENTS.md")
        result = repo.run("budget")
        self.assertEqual(1, result.returncode)
        self.assertIn("AGENTS.md", output(result))


class CommitsTest(unittest.TestCase):
    def test_non_conventional_subject_fails(self):
        repo = Repo(self, "")
        repo.commit("feat: good one")
        repo.commit("update stuff")
        result = repo.run("commits", "--base", repo.base)
        self.assertEqual(1, result.returncode)
        self.assertIn("update stuff", output(result))

    def test_conventional_subjects_pass(self):
        repo = Repo(self, "")
        repo.commit("feat(api): add endpoint")
        repo.commit("[#12] fix: handle null")
        self.assertEqual(0, repo.run("commits", "--base", repo.base).returncode)

    def test_profile_pattern_overrides_the_default(self):
        repo = Repo(self, """
            [commits]
            pattern = '^JIRA-[0-9]+ .+'
        """)
        base = repo.commit("JIRA-1 initial profile")
        repo.commit("JIRA-2 add thing")
        self.assertEqual(0, repo.run("commits", "--base", base).returncode)


class TddEvidenceTest(unittest.TestCase):
    PROFILE = """
        [tdd]
        production = ["src/**"]
        tests = ["tests/**", "**/*_test.go"]
    """

    def test_production_change_without_tests_fails(self):
        repo = Repo(self, self.PROFILE)
        repo.write("src/app.py", "x = 1\n")
        repo.commit("feat: app")
        result = repo.run("tdd", "--base", repo.base)
        self.assertEqual(1, result.returncode)
        self.assertIn("src/app.py", output(result))

    def test_production_change_with_tests_passes(self):
        repo = Repo(self, self.PROFILE)
        repo.write("tests/test_app.py", "def test(): pass\n")
        repo.write("src/app.py", "x = 1\n")
        repo.commit("feat: app")
        self.assertEqual(0, repo.run("tdd", "--base", repo.base).returncode)

    def test_root_level_test_glob_matches(self):
        repo = Repo(self, self.PROFILE)
        repo.write("src/app.go", "package app\n")
        repo.write("app_test.go", "package app\n")
        repo.commit("feat: app")
        self.assertEqual(0, repo.run("tdd", "--base", repo.base).returncode)

    def test_docs_only_change_passes(self):
        repo = Repo(self, self.PROFILE)
        repo.write("README.md", "docs\n")
        repo.commit("docs: readme")
        self.assertEqual(0, repo.run("tdd", "--base", repo.base).returncode)

    def code_then_tests(self, profile: str) -> tuple[Repo, str]:
        repo = Repo(self, profile)
        repo.write("src/app.py", "x = 1\n")
        sha = repo.commit("feat: app")
        repo.write("tests/test_app.py", "def test(): pass\n")
        repo.commit("test: app")
        return repo, sha

    def test_order_fails_on_code_before_tests(self):
        repo, sha = self.code_then_tests(self.PROFILE + "order = true\n")
        result = repo.run("tdd", "--base", repo.base)
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn(sha[:7], output(result))
        self.assertIn("feat: app", output(result))

    def test_order_passes_tests_first(self):
        repo = Repo(self, self.PROFILE + "order = true\n")
        repo.write("tests/test_app.py", "def test(): pass\n")
        repo.commit("test: app red")
        repo.write("src/app.py", "x = 1\n")
        repo.commit("feat: app")
        self.assertEqual(0, repo.run("tdd", "--base", repo.base).returncode)

    def test_order_counts_a_commit_with_both_as_tests_first(self):
        repo = Repo(self, self.PROFILE + "order = true\n")
        repo.write("tests/test_app.py", "def test(): pass\n")
        repo.write("src/app.py", "x = 1\n")
        repo.commit("feat: app with test")
        self.assertEqual(0, repo.run("tdd", "--base", repo.base).returncode)

    def test_without_order_only_presence_counts(self):
        repo, _ = self.code_then_tests(self.PROFILE)
        self.assertEqual(0, repo.run("tdd", "--base", repo.base).returncode)

    WAIVED = PROFILE + 'waive_labels = ["sdlc-exception"]\n'

    def test_waive_label_passes(self):
        repo = Repo(self, self.WAIVED)
        repo.write("src/app.py", "x = 1\n")
        repo.commit("feat: app")
        result = repo.run("tdd", "--base", repo.base, "--labels", "chore,sdlc-exception")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("sdlc-exception", output(result))

    def test_unlisted_label_does_not_waive(self):
        repo = Repo(self, self.WAIVED)
        repo.write("src/app.py", "x = 1\n")
        repo.commit("feat: app")
        self.assertEqual(1, repo.run("tdd", "--base", repo.base, "--labels", "chore").returncode)


if __name__ == "__main__":
    unittest.main()
