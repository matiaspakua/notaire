"""Issue intake lint (#2257): an under-specified issue is caught before an agent starts.

A check that cannot run is `no-evaluable`, never a pass (axiom 3).
"""
from __future__ import annotations

import json
import unittest

from aisdlc import intake
from helpers import Repo, output

GOOD_BODY = """## Use Case
CU76. Parent #10.

## Problem
Something concrete.

## Acceptance criteria
- [ ] The behaviour exists.

## Test plan
RED per criterion.

## Size
<= 400 counted lines.

## Out of scope
Everything else.
"""

ISSUE = {"title": "feat: a thing", "body": GOOD_BODY, "labels": ["area:devops", "type:feature"]}


def statuses(results):
    return {row["name"]: row["status"] for row in results}


def resolves_all(_number):
    return True


class IntakeLintTest(unittest.TestCase):
    def test_complete_issue_passes_every_check(self):
        results = intake.lint(ISSUE, intake.IntakeConfig(), resolves_all)
        self.assertEqual({"pass"}, set(statuses(results).values()))

    def test_missing_section_fails_and_names_it(self):
        issue = {**ISSUE, "body": GOOD_BODY.replace("## Test plan\nRED per criterion.\n", "")}
        self.assertEqual("fail", statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all))["section:test plan"])

    def test_empty_section_fails(self):
        issue = {**ISSUE, "body": GOOD_BODY.replace("RED per criterion.", "")}
        self.assertEqual("fail", statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all))["section:test plan"])

    def test_heading_with_suffix_satisfies_the_section(self):
        issue = {**ISSUE, "body": GOOD_BODY.replace("## Test plan", "## Test plan (RED/PRESERVE)")}
        self.assertEqual("pass", statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all))["section:test plan"])

    def test_no_acceptance_checkbox_fails(self):
        issue = {**ISSUE, "body": GOOD_BODY.replace("- [ ] The behaviour exists.", "The behaviour exists.")}
        self.assertEqual("fail", statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all))["checkbox"])

    def test_placeholder_fails(self):
        issue = {**ISSUE, "body": GOOD_BODY + "\nDepends on Phase A [#TBD-link-in-plan].\n"}
        self.assertEqual("fail", statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all))["placeholder"])

    def test_dangling_link_fails_and_names_the_number(self):
        issue = {**ISSUE, "body": GOOD_BODY + "\nDepends on #999.\n"}
        results = intake.lint(issue, intake.IntakeConfig(), lambda number: number != 999)
        row = next(r for r in results if r["name"] == "links")
        self.assertEqual("fail", row["status"])
        self.assertIn("#999", row["detail"])

    def test_link_check_is_no_evaluable_without_a_resolver(self):
        issue = {**ISSUE, "body": GOOD_BODY + "\nDepends on #999.\n"}
        self.assertEqual("no-evaluable", statuses(intake.lint(issue, intake.IntakeConfig(), None))["links"])

    def test_missing_label_prefix_fails(self):
        issue = {**ISSUE, "labels": ["area:devops"]}
        self.assertEqual("fail", statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all))["label:type:"])

    def test_labels_accept_objects_with_a_name(self):
        issue = {**ISSUE, "labels": [{"name": "area:devops"}, {"name": "type:feature"}]}
        self.assertEqual({"pass"}, set(statuses(intake.lint(issue, intake.IntakeConfig(), resolves_all)).values()))

    def test_link_checks_are_capped_in_a_deterministic_order(self):
        issue = {**ISSUE, "body": GOOD_BODY + "\nSee #3 #1 #2.\n"}
        seen = []
        intake.lint(issue, intake.IntakeConfig(max_links=2), lambda number: seen.append(number) or True)
        self.assertEqual([1, 2], seen)


PROFILE = """
    [tracker]
    verify = "true"
    fetch = "cat {root}/issue-{issue}.json"
"""


class IntakeCliTest(unittest.TestCase):
    def test_good_issue_from_fetch_exits_zero(self):
        repo = Repo(self, PROFILE)
        repo.write("issue-7.json", json.dumps(ISSUE))
        result = repo.run("intake", "check", "7")
        self.assertEqual(0, result.returncode, output(result))

    def test_bad_issue_exits_one_and_json_lists_the_failure(self):
        repo = Repo(self, PROFILE)
        repo.write("issue-8.json", json.dumps({**ISSUE, "body": "no sections"}))
        result = repo.run("intake", "check", "8", "--json")
        self.assertEqual(1, result.returncode)
        payload = json.loads(result.stdout)
        self.assertEqual("fail", payload["status"])
        self.assertTrue(any(row["status"] == "fail" for row in payload["checks"]))

    def test_no_fetch_command_is_no_evaluable_exit_three(self):
        repo = Repo(self, '[tracker]\nverify = "true"\n')
        result = repo.run("intake", "check", "7")
        self.assertEqual(3, result.returncode)
        self.assertIn("no-evaluable", output(result))

    def test_file_option_replaces_fetch(self):
        repo = Repo(self, '[tracker]\nverify = "true"\n')
        path = repo.write("issue.json", json.dumps(ISSUE))
        result = repo.run("intake", "check", "7", "--file", str(path))
        self.assertEqual(0, result.returncode, output(result))

    def test_failed_fetch_is_no_evaluable(self):
        repo = Repo(self, '[tracker]\nfetch = "false"\n')
        self.assertEqual(3, repo.run("intake", "check", "7").returncode)

    def test_configured_sections_replace_the_defaults(self):
        repo = Repo(self, PROFILE + '\n[intake]\nsections = ["Problem"]\nlabel_prefixes = []\n')
        repo.write("issue-9.json", json.dumps({"title": "t", "body": "## Problem\nx\n- [ ] y\n", "labels": []}))
        result = repo.run("intake", "check", "9")
        self.assertEqual(0, result.returncode, output(result))


if __name__ == "__main__":
    unittest.main()
