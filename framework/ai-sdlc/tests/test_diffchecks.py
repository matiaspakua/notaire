"""Owner-only paths, the review budget and the write set are checked on the change's own diff.

Why: agents cannot change their own rules; a reviewer reads at most 400 changed lines in one sitting;
changed paths are compared with the task's write set before anything is accepted (Notaire audit S2).
"""
from __future__ import annotations

import unittest

from helpers import Repo, git, output

PROTECTED = """
    [protected]
    paths = ["POLICY.md", ".agents/**", "**/*.lock"]
    owner = "the Owner"
"""


class ProtectedPathsTest(unittest.TestCase):
    def test_change_to_an_owner_only_path_fails_and_names_it(self):
        repo = Repo(self, PROTECTED)
        repo.write("POLICY.md", "# Policy\n\nGates are optional.\n")
        repo.commit("docs: relax the policy")
        result = repo.run("protected", "--base", repo.base)
        self.assertEqual(1, result.returncode)
        self.assertIn("POLICY.md", output(result))
        self.assertIn("the Owner", output(result))

    def test_double_star_prefix_matches_at_the_root_too(self):
        repo = Repo(self, PROTECTED)
        repo.write("deps.lock", "x\n")
        repo.commit("build: add lock")
        self.assertEqual(1, repo.run("protected", "--base", repo.base).returncode)

    def test_ordinary_change_passes(self):
        repo = Repo(self, PROTECTED)
        repo.write("src/app.py", "print('hi')\n")
        repo.commit("feat: app")
        self.assertEqual(0, repo.run("protected", "--base", repo.base).returncode)

    def test_no_configuration_passes_with_a_notice(self):
        repo = Repo(self, "")
        result = repo.run("protected", "--base", repo.base)
        self.assertEqual(0, result.returncode)
        self.assertIn("no owner-only paths", output(result))


class ReviewSizeTest(unittest.TestCase):
    def test_diff_over_the_budget_fails_and_lists_the_largest_files(self):
        repo = Repo(self, "[size]\nlimit = 10\n")
        repo.write("src/big.py", "x = 1\n" * 30)
        repo.commit("feat: big")
        result = repo.run("size", "--base", repo.base)
        self.assertEqual(1, result.returncode)
        self.assertIn("src/big.py", output(result))
        self.assertIn("chained", output(result))

    def test_excluded_paths_do_not_count(self):
        repo = Repo(self, '[size]\nlimit = 10\nexclude = ["**/*.lock", "docs/**"]\n')
        repo.write("deps.lock", "x\n" * 50)
        repo.write("docs/notes.md", "y\n" * 50)
        repo.write("src/small.py", "z = 1\n")
        repo.commit("feat: small change with generated noise")
        result = repo.run("size", "--base", repo.base)
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("100 excluded", output(result))

    def test_limit_flag_overrides_the_profile(self):
        repo = Repo(self, "[size]\nlimit = 1000\n")
        repo.write("src/a.py", "a = 1\n" * 20)
        repo.commit("feat: a")
        self.assertEqual(1, repo.run("size", "--base", repo.base, "--limit", "5").returncode)

    def test_invalid_limit_is_a_configuration_error(self):
        repo = Repo(self, "[size]\nlimit = 0\n")
        self.assertEqual(2, repo.run("size", "--base", repo.base).returncode)


CHANGE = """\
# Proposal

## Impact

### Write set

- `src/billing/**`
- `tests/test_billing.py`
- docs/billing.md (user guide)

### Next section

- `not/in/the/write/set.py`
"""


class WriteSetTest(unittest.TestCase):
    def repo(self) -> Repo:
        return Repo(self, '[spec]\ndir = "changes"\n[scope]\nalways = ["CHANGELOG.md"]\n')

    def test_paths_inside_the_write_set_pass(self):
        repo = self.repo()
        repo.write("changes/billing/proposal.md", CHANGE)
        repo.write("src/billing/invoice.py", "x = 1\n")
        repo.write("tests/test_billing.py", "y = 1\n")
        repo.write("docs/billing.md", "guide\n")
        repo.write("CHANGELOG.md", "- billing\n")
        repo.commit("feat: billing")
        result = repo.run("scope", "--base", repo.base)
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("PASSED", output(result))

    def test_path_outside_the_write_set_fails_and_is_named(self):
        repo = self.repo()
        repo.write("changes/billing/proposal.md", CHANGE)
        repo.write("src/billing/invoice.py", "x = 1\n")
        repo.write("src/auth/session.py", "unplanned = True\n")
        repo.commit("feat: billing")
        result = repo.run("scope", "--base", repo.base)
        self.assertEqual(1, result.returncode)
        self.assertIn("src/auth/session.py", output(result))
        self.assertNotIn("not/in/the/write/set.py", output(result).split("write set from")[1].split("\n")[0])

    def test_record_in_a_nested_folder_is_found(self):
        repo = self.repo()
        repo.write("changes/archive/2026-11-07-billing/proposal.md", CHANGE)
        repo.write("changes/archive/2026-11-07-billing/specs/billing/spec.md", "# Spec\n")
        repo.write("src/billing/invoice.py", "x = 1\n")
        repo.commit("feat: billing")
        self.assertEqual(0, repo.run("scope", "--base", repo.base).returncode)

    def test_template_examples_inside_html_comments_are_ignored(self):
        repo = self.repo()
        repo.write("changes/billing/proposal.md", """\
            ## Impact

            ### Write set

            <!-- Example:
            - `src/**`
            -->
            - `src/billing/**`
        """)
        repo.write("src/auth/session.py", "unplanned = True\n")
        repo.commit("feat: billing")
        result = repo.run("scope", "--base", repo.base)
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn("src/auth/session.py", output(result))

    def test_no_write_set_is_not_evaluable_and_strict_fails(self):
        repo = self.repo()
        repo.write("src/anything.py", "x = 1\n")
        repo.commit("feat: no record")
        result = repo.run("scope", "--base", repo.base)
        self.assertEqual(0, result.returncode)
        self.assertIn("NOT EVALUABLE", output(result))
        self.assertEqual(1, repo.run("scope", "--base", repo.base, "--strict").returncode)


class ReviewSizeRenameTest(unittest.TestCase):
    """Rename-aware size checks — red tests for issue #2466."""

    def test_pure_rename_counts_zero_lines(self):
        """A commit that only renames a 600-line file passes size --limit 400 and reports 0 changed lines."""
        repo = Repo(self, "[size]\nlimit = 400\n")
        file_content = "line\n" * 600
        repo.write("src/old_name.py", file_content)
        repo.commit("feat: add file")
        git(repo.root, "mv", "src/old_name.py", "src/new_name.py")
        repo.commit("refactor: rename file")
        result = repo.run("size", "--base", repo.base)
        out = output(result)
        self.assertEqual(0, result.returncode, out)
        self.assertIn("0 changed lines", out)

    def test_rename_with_edits_counts_only_edited_lines(self):
        """A commit that renames a 600-line file and edits 3 lines counts only those edits and reports the NEW path."""
        repo = Repo(self, "[size]\nlimit = 400\n")
        file_content = "line\n" * 600
        repo.write("src/old_name.py", file_content)
        repo.commit("feat: add file")
        git(repo.root, "mv", "src/old_name.py", "src/new_name.py")
        edited_content = "line\n" * 598 + "edited_line_1\n" + "edited_line_2\n" + "edited_line_3\n"
        (repo.root / "src/new_name.py").write_text(edited_content)
        repo.commit("refactor: rename and edit file")
        result = repo.run("size", "--base", repo.base)
        out = output(result)
        self.assertEqual(0, result.returncode, out)
        self.assertIn("src/new_name.py", out)

    def test_renamed_file_new_path_matched_against_exclude(self):
        """The NEW path of a renamed file is the one matched against [size] exclude.

        A file is renamed from an excluded directory to a non-excluded one.
        With rename detection the change is a pure rename (0 lines) — the new path
        is reported but there are no lines to exclude.  Without rename detection the
        new file appears as 600 additions at the non-excluded path and blows past
        the budget.
        """
        repo = Repo(self, '[size]\nlimit = 10\nexclude = ["generated/**"]\n')
        file_content = "line\n" * 600
        repo.write("generated/data.py", file_content)
        repo.commit("feat: add file")
        (repo.root / "src").mkdir(exist_ok=True)
        git(repo.root, "mv", "generated/data.py", "src/data.py")
        repo.commit("refactor: move out of generated")
        result = repo.run("size", "--base", repo.base)
        out = output(result)
        self.assertEqual(0, result.returncode, out)


class DoctorTest(unittest.TestCase):
    def test_agent_merge_role_without_owner_only_paths_warns(self):
        repo = Repo(self, """
            [project]
            name = "demo"
            policy = "POLICY.md"
            level = 3
            [roles.foreman]
            tier = "mid"
            may_merge = true
        """)
        self.assertIn("owner-only paths", output(repo.run("doctor")))


if __name__ == "__main__":
    unittest.main()
