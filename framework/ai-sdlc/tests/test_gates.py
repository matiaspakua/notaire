"""Gates are profile-driven commands judged only by their exit code (#1958)."""
from __future__ import annotations

import unittest

from helpers import Repo, output


class StageGateTest(unittest.TestCase):
    def test_required_failing_gate_fails_the_stage_and_is_named(self):
        repo = Repo(self, """
            [[gates]]
            name = "unit tests"
            stage = "pre-push"
            run = "exit 1"
        """)
        result = repo.run("gate", "pre-push")
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn("unit tests", output(result))

    def test_passing_gates_pass_the_stage(self):
        repo = Repo(self, """
            [[gates]]
            name = "lint"
            stage = "pre-push"
            run = "true"
        """)
        result = repo.run("gate", "pre-push")
        self.assertEqual(0, result.returncode, output(result))

    def test_red_gate_passes_when_its_command_fails(self):
        repo = Repo(self, """
            [[gates]]
            name = "new test is red"
            stage = "red"
            run = "exit 1"
            expect = "fail"
        """)
        result = repo.run("gate", "red")
        self.assertEqual(0, result.returncode, output(result))

    def test_red_gate_fails_when_its_command_passes(self):
        repo = Repo(self, """
            [[gates]]
            name = "new test is red"
            stage = "red"
            run = "true"
            expect = "fail"
        """)
        self.assertEqual(1, repo.run("gate", "red").returncode)

    def test_path_scoped_gate_is_skipped_without_matching_change(self):
        repo = Repo(self, """
            [[gates]]
            name = "frontend tests"
            stage = "pre-push"
            run = "exit 1"
            paths = ["web/**"]
        """)
        repo.write("docs/readme.md", "docs only\n")
        repo.commit("docs: readme")
        result = repo.run("gate", "pre-push", "--base", repo.base)
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("skip", output(result).lower())

    def test_path_scoped_gate_runs_when_a_matching_file_changed(self):
        repo = Repo(self, """
            [[gates]]
            name = "frontend tests"
            stage = "pre-push"
            run = "exit 1"
            paths = ["web/**"]
        """)
        repo.write("web/app/page.ts", "export {}\n")
        repo.commit("feat: page")
        self.assertEqual(1, repo.run("gate", "pre-push", "--base", repo.base).returncode)

    def test_advisory_gate_failure_does_not_fail_the_stage(self):
        repo = Repo(self, """
            [[gates]]
            name = "style report"
            stage = "pre-push"
            run = "exit 3"
            required = false
        """)
        result = repo.run("gate", "pre-push")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("advisory", output(result).lower())

    def test_stage_without_gates_is_a_configuration_error(self):
        repo = Repo(self, """
            [[gates]]
            name = "lint"
            stage = "pre-push"
            run = "true"
        """)
        self.assertEqual(2, repo.run("gate", "release").returncode)

    def test_base_and_vars_are_substituted_and_other_braces_kept(self):
        repo = Repo(self, """
            [[gates]]
            name = "placeholders"
            stage = "merge"
            run = "test '{base}' = 'abc' && test '{pr}' = '42' && echo '{untouched}' | grep -q untouched"
        """)
        result = repo.run("gate", "merge", "--base", "abc", "--var", "pr=42")
        self.assertEqual(0, result.returncode, output(result))

    def test_aisdlc_prefix_runs_the_kit_itself(self):
        repo = Repo(self, """
            [context]
            max_tokens = 1000
            files = ["POLICY.md"]

            [[gates]]
            name = "context budget"
            stage = "pre-push"
            run = "aisdlc budget"
        """)
        result = repo.run("gate", "pre-push")
        self.assertEqual(0, result.returncode, output(result))

    def test_timeout_fails_the_gate(self):
        repo = Repo(self, """
            [[gates]]
            name = "slow"
            stage = "pre-push"
            run = "sleep 5"
            timeout = 1
        """)
        result = repo.run("gate", "pre-push")
        self.assertEqual(1, result.returncode)
        self.assertIn("timeout", output(result).lower())

    def test_list_prints_stages_and_gates(self):
        repo = Repo(self, """
            [[gates]]
            name = "lint"
            stage = "pre-push"
            run = "true"
        """)
        result = repo.run("gate", "--list")
        self.assertEqual(0, result.returncode)
        self.assertIn("pre-push", result.stdout)
        self.assertIn("lint", result.stdout)


if __name__ == "__main__":
    unittest.main()
