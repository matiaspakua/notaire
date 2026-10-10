#!/usr/bin/env python3
"""
Guards issues #1315 (CU76): the Owner-accepted OpenAPI breaking changes live in
backend-api/openapi/accepted-breaking-changes.d/, one file per pull request, and the list
that oasdiff ignores is empty on main.

Why a directory: with a single accepted-breaking-changes.txt every open pull request edited
the same lines and main emptied them after each merge, so every merge conflicted with every
other open pull request. Each pull request now adds its own <issue>-<slug>.txt; two pull
requests never touch the same file, and deleting a merged file on two branches merges
cleanly.

workspace/sdlc/check-accepted-breaking-changes.py:
- reads every *.txt in the directory; a file that exists unchanged on the base ref is
  "merged": its break is already in the base spec, so its entries are never passed to
  oasdiff (they would only hide a later break with the same text), and --prune deletes it;
- every other file belongs to the pull request: each entry must match a breaking change
  oasdiff reports between the base and the revision (no ignore list, oasdiff err-ignore
  rule: case-insensitive, METHOD + path and the change text), sit under a "# #<issue>"
  comment, and the file name must start with the issue number;
- --write-ignore writes the pull request's entries, the only list oasdiff ignores;
- the legacy accepted-breaking-changes.txt must not exist.

Asserts the rules above with a fake oasdiff in a throwaway git repository, the JSON shape
against a real oasdiff when installed, and the CI / preflight wiring.

Run with: python3 workspace/tests/test_check_accepted_breaking_changes.py
"""

from __future__ import annotations

import glob
import json
import os
import re
import shutil
import stat
import subprocess
import tempfile
import textwrap
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CHECKER = os.path.join(REPO_ROOT, "workspace", "sdlc", "check-accepted-breaking-changes.py")
CHECKER_REL = "workspace/sdlc/check-accepted-breaking-changes.py"
OPENAPI_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "openapi-contract.yml")
PREFLIGHT = os.path.join(REPO_ROOT, "workspace", "sdlc", "preflight.sh")
ACCEPTED_DIR_REL = "backend-api/openapi/accepted-breaking-changes.d"
LEGACY_LIST_REL = "backend-api/openapi/accepted-breaking-changes.txt"
CI_IGNORE_FILE = ".openapi-ci/accepted-breaking-changes.txt"

HISTORY_CHANGE = {
    "id": "response-body-type-changed",
    "text": "the response's body `type` changed from `array<object>` to `object` for status `200`",
    "level": 3,
    "operation": "GET",
    "path": "/api/v1/historial",
}
HISTORY_ENTRY = (
    "GET /api/v1/historial the response's body `type` changed from `array<object>` "
    "to `object` for status `200`"
)
PAGOS_CHANGE = {
    "id": "request-property-became-required",
    "text": "the request property `amount` became required",
    "level": 3,
    "operation": "POST",
    "path": "/api/v1/pagos",
}
PAGOS_ENTRY = "POST /api/v1/pagos the request property `amount` became required"


def git(cwd: str, *args: str) -> None:
    subprocess.run(
        ["git", "-c", "user.name=t", "-c", "user.email=t@t", "-c", "commit.gpgsign=false", *args],
        cwd=cwd, check=True, capture_output=True,
    )


class CheckerTestCase(unittest.TestCase):
    """A throwaway repository whose commit tagged `base` plays origin/main."""

    def setUp(self):
        self.repo = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, self.repo)
        self.dir = os.path.join(self.repo, *ACCEPTED_DIR_REL.split("/"))
        os.makedirs(self.dir)
        self._write(f"{ACCEPTED_DIR_REL}/README.md", "# Accepted breaking changes\n")
        self.base_spec = self._write("base.yaml", "openapi: 3.0.1\n")
        self.revision_spec = self._write("revision.yaml", "openapi: 3.0.1\n")
        git(self.repo, "init", "-q")
        self.commit_base()

    def _write(self, rel: str, content: str) -> str:
        path = os.path.join(self.repo, *rel.split("/"))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def accepted(self, name: str, content: str) -> str:
        return self._write(f"{ACCEPTED_DIR_REL}/{name}", content)

    def commit_base(self) -> None:
        git(self.repo, "add", "-A")
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "base")
        git(self.repo, "tag", "-f", "base")

    def _fake_oasdiff(self, changes: list[dict], exit_code: int) -> str:
        tools = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, tools)
        self.args_file = os.path.join(tools, "args")
        report = os.path.join(tools, "report.json")
        with open(report, "w", encoding="utf-8") as f:
            json.dump(changes, f)
        script = os.path.join(tools, "oasdiff")
        with open(script, "w", encoding="utf-8") as f:
            f.write(f'#!/bin/sh\necho "$@" > "{self.args_file}"\ncat "{report}"\nexit {exit_code}\n')
        os.chmod(script, os.stat(script).st_mode | stat.S_IXUSR)
        return script

    def run_checker(self, changes: list[dict], *extra: str, exit_code: int = 0, oasdiff: str | None = None):
        self.ignore_out = os.path.join(self.repo, "ignore.txt")
        env = dict(os.environ, OASDIFF=oasdiff or self._fake_oasdiff(changes, exit_code))
        result = subprocess.run(
            ["python3", CHECKER, self.base_spec, self.revision_spec,
             "--base-ref", "base", "--write-ignore", self.ignore_out, *extra],
            cwd=self.repo, capture_output=True, text=True, env=env, check=False,
        )
        result.output = result.stdout + result.stderr
        return result

    def ignored_entries(self) -> list[str]:
        with open(self.ignore_out, encoding="utf-8") as f:
            return [line.strip() for line in f if line.strip() and not line.startswith("#")]


class PullRequestFilesTest(CheckerTestCase):
    def test_empty_directory_passes_and_ignores_nothing(self):
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.output)
        self.assertEqual(self.ignored_entries(), [])

    def test_live_entry_of_the_pull_request_is_ignored(self):
        self.accepted("596-history-pagination.txt", f"# #596 reason\n{HISTORY_ENTRY}\n")
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.output)
        self.assertEqual(self.ignored_entries(), [HISTORY_ENTRY])
        with open(self.args_file, encoding="utf-8") as f:
            args = f.read().split()
        self.assertEqual(args[0], "breaking")
        self.assertIn("json", args)
        self.assertNotIn("--err-ignore", args, "the checker must see every breaking change")

    def test_files_of_several_pull_requests_are_concatenated(self):
        self.accepted("655-pagos.txt", f"# #655\n{PAGOS_ENTRY}\n")
        self.accepted("596-history.txt", f"# #596\n{HISTORY_ENTRY}\n")
        result = self.run_checker([HISTORY_CHANGE, PAGOS_CHANGE])
        self.assertEqual(result.returncode, 0, result.output)
        self.assertEqual(self.ignored_entries(), [HISTORY_ENTRY, PAGOS_ENTRY], "sorted by file name")

    def test_stale_entry_fails_and_names_file_and_entry(self):
        self.accepted("596-history.txt", f"# #596\n{HISTORY_ENTRY}\n")
        result = self.run_checker([])
        self.assertEqual(result.returncode, 1, result.output)
        self.assertIn(HISTORY_ENTRY, result.output)
        self.assertIn("596-history.txt", result.output)
        self.assertRegex(result.output, r"(?i)stale")

    def test_only_the_stale_entry_is_reported(self):
        self.accepted("655-mixed.txt", f"# #655\n{PAGOS_ENTRY}\n# #596\n{HISTORY_ENTRY}\n")
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 1)
        self.assertIn(PAGOS_ENTRY, result.output)
        self.assertNotIn(f"  {HISTORY_ENTRY}", result.output)

    def test_matching_is_case_insensitive_like_oasdiff_err_ignore(self):
        self.accepted("596-history.txt", f"# #596\n{HISTORY_ENTRY.upper()}\n")
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.output)

    def test_entry_for_another_operation_does_not_match(self):
        entry = HISTORY_ENTRY.replace("GET /api/v1/historial", "GET /api/v1/gestiones")
        self.accepted("596-history.txt", f"# #596\n{entry}\n")
        self.assertEqual(self.run_checker([HISTORY_CHANGE]).returncode, 1)

    def test_oasdiff_failure_is_an_error_not_a_clean_list(self):
        self.accepted("596-history.txt", f"# #596\n{HISTORY_ENTRY}\n")
        result = self.run_checker([], exit_code=3)
        self.assertEqual(result.returncode, 2, result.output)

    def test_file_name_must_start_with_the_issue_number(self):
        self.accepted("history.txt", f"# #596\n{HISTORY_ENTRY}\n")
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 1, result.output)
        self.assertIn("history.txt", result.output)

    def test_entry_needs_an_issue_comment_above_it(self):
        self.accepted("596-history.txt", f"{HISTORY_ENTRY}\n")
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 1, result.output)
        self.assertRegex(result.output, r"#<issue>")

    def test_entry_must_have_the_oasdiff_shape(self):
        self.accepted("596-history.txt", "# #596\nhistorial is paginated now\n")
        self.assertEqual(self.run_checker([HISTORY_CHANGE]).returncode, 1)

    def test_legacy_single_list_fails_with_a_migration_hint(self):
        self._write(LEGACY_LIST_REL, f"# #596\n{HISTORY_ENTRY}\n")
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 1, result.output)
        self.assertIn(ACCEPTED_DIR_REL, result.output)


class MergedFilesTest(CheckerTestCase):
    """A file unchanged on the base belongs to an already merged pull request."""

    def setUp(self):
        super().setUp()
        self.merged = self.accepted("596-history.txt", f"# #596\n{HISTORY_ENTRY}\n")
        self.commit_base()

    def test_merged_file_is_never_ignored_and_does_not_fail(self):
        # Even when the revision re-introduces the same break, the merged entry must not hide it.
        result = self.run_checker([HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.output)
        self.assertEqual(self.ignored_entries(), [])
        self.assertIn("596-history.txt", result.output)
        self.assertRegex(result.output, r"(?i)merged")
        self.assertTrue(os.path.exists(self.merged), "without --prune nothing is deleted")

    def test_prune_deletes_merged_files_only(self):
        own = self.accepted("655-pagos.txt", f"# #655\n{PAGOS_ENTRY}\n")
        result = self.run_checker([PAGOS_CHANGE], "--prune")
        self.assertEqual(result.returncode, 0, result.output)
        self.assertFalse(os.path.exists(self.merged))
        self.assertTrue(os.path.exists(own))
        self.assertTrue(os.path.exists(os.path.join(self.dir, "README.md")))
        self.assertEqual(self.ignored_entries(), [PAGOS_ENTRY])

    def test_editing_a_merged_file_makes_it_the_pull_requests_and_its_old_entry_stale(self):
        with open(self.merged, "a", encoding="utf-8") as f:
            f.write(f"# #655\n{PAGOS_ENTRY}\n")
        result = self.run_checker([PAGOS_CHANGE])
        self.assertEqual(result.returncode, 1, result.output)
        self.assertIn(HISTORY_ENTRY, result.output)

    def test_on_main_every_file_is_merged_and_nothing_is_ignored(self):
        self.accepted("655-pagos.txt", f"# #655\n{PAGOS_ENTRY}\n")
        self.commit_base()
        result = self.run_checker([])
        self.assertEqual(result.returncode, 0, result.output)
        self.assertEqual(self.ignored_entries(), [])


OASDIFF_REAL = shutil.which("oasdiff") or os.environ.get("OASDIFF")


@unittest.skipUnless(OASDIFF_REAL and os.path.exists(OASDIFF_REAL), "oasdiff not installed")
class RealOasdiffTest(CheckerTestCase):
    """Pins the JSON shape the checker relies on against the real tool."""

    SPEC = textwrap.dedent(
        """\
        openapi: 3.0.1
        info: {{title: t, version: '1'}}
        paths:
          /api/v1/historial:
            get:
              responses:
                '200':
                  description: OK
                  content:
                    application/json:
                      schema: {schema}
        """
    )

    def test_changed_response_type_is_a_live_entry_and_stale_once_on_base(self):
        self.base_spec = self._write("real-base.yaml", self.SPEC.format(schema="{type: array, items: {type: object}}"))
        self.revision_spec = self._write("real-revision.yaml", self.SPEC.format(schema="{type: object}"))
        self.accepted("596-history.txt", f"# #596\n{HISTORY_ENTRY}\n")
        live = self.run_checker([], oasdiff=OASDIFF_REAL)
        self.assertEqual(live.returncode, 0, live.output)
        self.base_spec = self.revision_spec
        stale = self.run_checker([], oasdiff=OASDIFF_REAL)
        self.assertEqual(stale.returncode, 1, stale.output)


ENTRY_SHAPE = re.compile(r"^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) /\S* \S.*$")


class RepositoryStateTest(unittest.TestCase):
    def test_directory_exists_and_the_legacy_list_is_gone(self):
        self.assertTrue(os.path.isdir(os.path.join(REPO_ROOT, *ACCEPTED_DIR_REL.split("/"))))
        self.assertTrue(os.path.isfile(os.path.join(REPO_ROOT, *ACCEPTED_DIR_REL.split("/"), "README.md")),
                        "the README documents the rule and keeps the directory in git")
        self.assertFalse(os.path.exists(os.path.join(REPO_ROOT, *LEGACY_LIST_REL.split("/"))))

    def test_committed_files_are_traceable(self):
        for path in glob.glob(os.path.join(REPO_ROOT, *ACCEPTED_DIR_REL.split("/"), "*.txt")):
            name = os.path.basename(path)
            self.assertRegex(name, r"^[0-9]+-[a-z0-9._-]+\.txt$")
            issue_seen = False
            with open(path, encoding="utf-8") as f:
                for number, line in enumerate(f.read().splitlines(), start=1):
                    if not line.strip():
                        continue
                    if line.startswith("#"):
                        issue_seen = issue_seen or bool(re.search(r"#[0-9]+", line))
                        continue
                    self.assertRegex(line, ENTRY_SHAPE, f"{name}:{number}")
                    self.assertTrue(issue_seen, f"{name}:{number} needs a '# #<issue>' comment above it")


class WiringTest(unittest.TestCase):
    def setUp(self):
        with open(OPENAPI_WORKFLOW, encoding="utf-8") as f:
            self.steps = yaml.safe_load(f)["jobs"]["openapi-contract"]["steps"]

    def _index(self, needle: str) -> list[int]:
        return [i for i, s in enumerate(self.steps) if needle in str(s.get("run", ""))]

    def test_ci_assembles_the_ignore_list_with_the_checker_before_the_diff(self):
        checker = self._index(CHECKER_REL)
        diff = self._index("oasdiff breaking")
        self.assertEqual(len(checker), 1, "exactly one step runs the checker")
        self.assertEqual(len(diff), 1, "exactly one step runs the breaking diff")
        self.assertLess(checker[0], diff[0], "the diff ignores what the checker assembled")
        step = self.steps[checker[0]]
        run = " ".join(str(step["run"]).replace("\\\n", " ").split())
        self.assertIn("steps.base.outputs.has_base", str(step.get("if", "")))
        self.assertIn("OASDIFF=/tmp/oasdiff", run)
        self.assertIn(f"--write-ignore {CI_IGNORE_FILE}", run)
        self.assertRegex(run, r'--base-ref "?origin/')
        self.assertNotIn("--prune", run, "CI only reports merged files")
        diff_run = " ".join(str(self.steps[diff[0]]["run"]).replace("\\\n", " ").split())
        self.assertIn(f"--err-ignore {CI_IGNORE_FILE}", diff_run)
        install = [s for s in self.steps[:checker[0]] if "oasdiff_1.33.0_linux_amd64.tar.gz" in str(s.get("run", ""))]
        self.assertEqual(len(install), 1, "oasdiff 1.33.0 is installed before the checker (#1380)")
        self.assertIn("sha256sum", str(install[0].get("run")), "the downloaded binary is verified")

    def test_preflight_assembles_the_same_list_and_prunes_only_with_fix(self):
        with open(PREFLIGHT, encoding="utf-8") as f:
            script = f.read()
        self.assertIn(CHECKER_REL, script)
        self.assertIn("--write-ignore", script)
        self.assertIn("--base-ref origin/main", script)
        self.assertRegex(script, r'--err-ignore "\$OPENAPI_IGNORE"')
        self.assertRegex(script, r'MODE_FIX" = "1" \]\s*&&\s*OPENAPI_PRUNE=\(--prune\)')
        self.assertNotIn(LEGACY_LIST_REL, script)


if __name__ == "__main__":
    unittest.main()
