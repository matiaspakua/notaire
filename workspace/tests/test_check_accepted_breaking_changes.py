#!/usr/bin/env python3
"""
Guards issue #1315 (CU76): backend-api/openapi/accepted-breaking-changes.txt stays empty by
default. An entry is only meaningful while the pull request that introduces its break is
open; once that break is on main, the base spec already contains it, the entry matches
nothing and would only hide a later break with the same text.

workspace/sdlc/check-accepted-breaking-changes.py compares the list with the breaking
changes oasdiff reports between the base spec and the revision (no ignore list) and fails on
every entry that matches none of them, so the next pull request after the merge removes it.

Asserts:
- the checker passes on a list without entries and on entries the diff still produces
- it fails, naming the entry, when an entry matches no current breaking change
- matching follows oasdiff err-ignore: case-insensitive, METHOD + path and the change text
- an oasdiff failure is reported as an error, not as a clean list
- with a real oasdiff on PATH, a changed response type is reported in the shape it parses
- CI (openapi-contract.yml, pull requests only) and preflight.sh run the checker

Run with: python3 workspace/tests/test_check_accepted_breaking_changes.py
"""

from __future__ import annotations

import json
import os
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
HEADER = "# Owner-accepted breaking changes.\n#\n"


class CheckerTestCase(unittest.TestCase):
    """Runs the checker against a fake oasdiff that prints a canned JSON report."""

    def setUp(self):
        self.tmp = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, self.tmp)
        self.base = self._write("base.yaml", "openapi: 3.0.1\n")
        self.revision = self._write("revision.yaml", "openapi: 3.0.1\n")

    def _write(self, name: str, content: str) -> str:
        path = os.path.join(self.tmp, name)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return path

    def _fake_oasdiff(self, changes: list[dict], exit_code: int = 0) -> str:
        report = self._write("report.json", json.dumps(changes))
        script = self._write(
            "oasdiff",
            "#!/bin/sh\n"
            f'echo "$@" > "{self.tmp}/args"\n'
            f'cat "{report}"\n'
            f"exit {exit_code}\n",
        )
        os.chmod(script, os.stat(script).st_mode | stat.S_IXUSR)
        return script

    def run_checker(self, entries: str, changes: list[dict], exit_code: int = 0):
        listing = self._write("accepted.txt", HEADER + entries)
        env = dict(os.environ, OASDIFF=self._fake_oasdiff(changes, exit_code))
        return subprocess.run(
            ["python3", CHECKER, self.base, self.revision, listing],
            capture_output=True,
            text=True,
            env=env,
            check=False,
        )


class AcceptedListCheckerTest(CheckerTestCase):
    def test_list_without_entries_passes(self):
        result = self.run_checker("\n", [HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_entry_still_produced_by_the_diff_passes(self):
        result = self.run_checker(f"# #596 reason\n{HISTORY_ENTRY}\n", [HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        with open(os.path.join(self.tmp, "args"), encoding="utf-8") as f:
            args = f.read().split()
        self.assertEqual(args[0], "breaking")
        self.assertIn("json", args)
        self.assertNotIn("--err-ignore", args, "the checker must see every breaking change")

    def test_entry_already_on_main_fails_and_is_named(self):
        result = self.run_checker(f"# #596 reason\n{HISTORY_ENTRY}\n", [])
        self.assertEqual(result.returncode, 1, result.stdout + result.stderr)
        self.assertIn(HISTORY_ENTRY, result.stdout + result.stderr)
        self.assertRegex(result.stdout + result.stderr, r"(?i)stale")

    def test_only_the_stale_entry_is_reported(self):
        other = "POST /api/v1/pagos the request property `amount` became required"
        result = self.run_checker(f"# #655\n{other}\n# #596\n{HISTORY_ENTRY}\n", [HISTORY_CHANGE])
        self.assertEqual(result.returncode, 1)
        output = result.stdout + result.stderr
        self.assertIn(other, output)
        self.assertNotIn(f"  {HISTORY_ENTRY}", output)

    def test_matching_is_case_insensitive_like_oasdiff_err_ignore(self):
        result = self.run_checker(f"# #596\n{HISTORY_ENTRY.upper()}\n", [HISTORY_CHANGE])
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_entry_for_another_operation_does_not_match(self):
        entry = HISTORY_ENTRY.replace("GET /api/v1/historial", "GET /api/v1/gestiones")
        result = self.run_checker(f"# #596\n{entry}\n", [HISTORY_CHANGE])
        self.assertEqual(result.returncode, 1)

    def test_oasdiff_failure_is_an_error_not_a_clean_list(self):
        result = self.run_checker(f"# #596\n{HISTORY_ENTRY}\n", [], exit_code=3)
        self.assertEqual(result.returncode, 2, result.stdout + result.stderr)


@unittest.skipUnless(shutil.which("oasdiff") or os.path.exists("/workspace/tools/oasdiff"),
                     "oasdiff not installed")
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
        oasdiff = shutil.which("oasdiff") or "/workspace/tools/oasdiff"
        base = self._write("real-base.yaml", self.SPEC.format(schema="{type: array, items: {type: object}}"))
        revision = self._write("real-revision.yaml", self.SPEC.format(schema="{type: object}"))
        listing = self._write("real-accepted.txt", f"{HEADER}# #596\n{HISTORY_ENTRY}\n")
        env = dict(os.environ, OASDIFF=oasdiff)
        live = subprocess.run(["python3", CHECKER, base, revision, listing],
                              capture_output=True, text=True, env=env, check=False)
        self.assertEqual(live.returncode, 0, live.stdout + live.stderr)
        stale = subprocess.run(["python3", CHECKER, revision, revision, listing],
                               capture_output=True, text=True, env=env, check=False)
        self.assertEqual(stale.returncode, 1, stale.stdout + stale.stderr)


class WiringTest(unittest.TestCase):
    def test_pull_requests_run_the_checker_after_the_breaking_diff(self):
        with open(OPENAPI_WORKFLOW, encoding="utf-8") as f:
            workflow = yaml.safe_load(f)
        steps = workflow["jobs"]["openapi-contract"]["steps"]
        names = [i for i, s in enumerate(steps) if CHECKER_REL in str(s.get("run", ""))]
        self.assertEqual(len(names), 1, "exactly one step runs the stale-entry checker")
        step = steps[names[0]]
        diff = [i for i, s in enumerate(steps) if "oasdiff-action/breaking" in str(s.get("uses", ""))]
        self.assertGreater(names[0], diff[0], "the checker runs after the breaking diff")
        condition = str(step.get("if", ""))
        self.assertIn("steps.base.outputs.has_base", condition)
        self.assertIn("pull_request", condition,
                      "on main the base equals the revision, so every entry would look stale")
        self.assertIn("1.33.0", str(step.get("run")), "same oasdiff version as oasdiff-action@v0.1.18")
        self.assertIn("sha256sum", str(step.get("run")), "the downloaded binary is verified")

    def test_preflight_runs_the_checker(self):
        with open(PREFLIGHT, encoding="utf-8") as f:
            self.assertIn(CHECKER_REL, f.read())


if __name__ == "__main__":
    unittest.main()
