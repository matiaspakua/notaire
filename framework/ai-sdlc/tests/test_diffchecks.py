"""Owner-only paths, the review budget and the write set are checked on the change's own diff.

Sources: agentic-os C14-004 (agents cannot change their own rules) and factory-control
`protectedPaths`; gentle-ai's 400-line review budget; agentic-os C15-006 (changed paths are
compared with the task's write set before anything is accepted) and Notaire audit item S2.
"""
from __future__ import annotations

import unittest

from helpers import Repo, output

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
