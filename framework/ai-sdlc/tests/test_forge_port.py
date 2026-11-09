"""Read-only ForgePort lead-time sample (#2184)."""
from __future__ import annotations

import json
import subprocess
import sys
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))


class _FakeGh:
    """Injectable gh runner that records argv and returns canned JSON."""

    def __init__(self, responses: dict[tuple[str, ...], str | tuple[int, str]]) -> None:
        self.calls: list[list[str]] = []
        self._responses = responses

    def __call__(self, argv: list[str]) -> subprocess.CompletedProcess[str]:
        self.calls.append(list(argv))
        key = tuple(argv)
        # Allow prefix match on long argv (e.g. varying --jq)
        payload = self._responses.get(key)
        if payload is None:
            for stored, value in self._responses.items():
                if argv[: len(stored)] == list(stored):
                    payload = value
                    break
        if payload is None:
            return subprocess.CompletedProcess(argv, 1, stdout="", stderr=f"unexpected: {argv}")
        if isinstance(payload, tuple):
            code, stdout = payload
        else:
            code, stdout = 0, payload
        return subprocess.CompletedProcess(argv, code, stdout=stdout, stderr="")


def _issue_json(number: int = 2182, created: str = "2026-11-09T17:40:15Z") -> str:
    return json.dumps(
        {
            "number": number,
            "title": f"issue {number}",
            "createdAt": created,
            "state": "CLOSED",
        }
    )


def _pr_list_json(
    number: int = 2183,
    issue: int = 2182,
    *,
    checks_completed: str | None = "2026-11-09T19:17:26Z",
    merged_at: str | None = "2026-11-09T19:18:03Z",
    conclusion: str = "SUCCESS",
) -> str:
    rollup = []
    if checks_completed is not None:
        rollup.append(
            {
                "name": "submit-maven",
                "conclusion": conclusion,
                "status": "COMPLETED",
                "completedAt": checks_completed,
            }
        )
    return json.dumps(
        [
            {
                "number": number,
                "title": f"[#{issue}] feat",
                "createdAt": "2026-11-09T19:16:24Z",
                "mergedAt": merged_at,
                "statusCheckRollup": rollup,
                "closingIssuesReferences": [{"number": issue}],
            }
        ]
    )


class ForgePortLeadTimeTest(unittest.TestCase):
    def setUp(self) -> None:
        from aisdlc.forge import (  # noqa: E402
            READ_ONLY_VERBS,
            WRITE_VERBS,
            checks_green_at,
            lead_time_sample,
        )
        from aisdlc.metrics import lead_time_from_sample  # noqa: E402

        self.READ_ONLY_VERBS = READ_ONLY_VERBS
        self.WRITE_VERBS = WRITE_VERBS
        self.checks_green_at = checks_green_at
        self.lead_time_sample = lead_time_sample
        self.lead_time_from_sample = lead_time_from_sample

    def test_sample_from_forge_runner(self):
        gh = _FakeGh(
            {
                ("issue", "view", "2182", "--json", "number,title,createdAt,state"): _issue_json(),
                (
                    "pr",
                    "list",
                    "--state",
                    "all",
                    "--search",
                    "2182",
                    "--limit",
                    "30",
                    "--json",
                    "number,title,createdAt,mergedAt,statusCheckRollup,closingIssuesReferences",
                ): _pr_list_json(),
            }
        )
        sample = self.lead_time_sample(2182, runner=gh)
        self.assertEqual(2182, sample["sample"]["issue"]["number"])
        self.assertEqual("2026-11-09T17:40:15Z", sample["sample"]["issue"]["created_at"])
        prs = sample["sample"]["pull_requests"]
        self.assertEqual(1, len(prs))
        self.assertEqual(2183, prs[0]["number"])
        self.assertEqual([2182], prs[0]["linked_issues"])
        self.assertEqual("2026-11-09T19:17:26Z", prs[0]["checks_green_at"])
        result = self.lead_time_from_sample(sample)
        self.assertEqual("ok", result["status"])
        self.assertEqual(2183, result["pull_request"])
        # 17:40:15 → 19:17:26 = 5831s
        self.assertEqual(5831.0, result["seconds"])

    def test_read_only_argv_verbs(self):
        gh = _FakeGh(
            {
                ("issue", "view", "99", "--json", "number,title,createdAt,state"): _issue_json(99),
                (
                    "pr",
                    "list",
                    "--state",
                    "all",
                    "--search",
                    "99",
                    "--limit",
                    "30",
                    "--json",
                    "number,title,createdAt,mergedAt,statusCheckRollup,closingIssuesReferences",
                ): _pr_list_json(number=100, issue=99),
            }
        )
        self.lead_time_sample(99, runner=gh)
        self.assertTrue(gh.calls)
        for argv in gh.calls:
            self.assertTrue(argv, argv)
            head = argv[0]
            self.assertIn(head, self.READ_ONLY_VERBS)
            self.assertNotIn(head, self.WRITE_VERBS)
            joined = " ".join(argv).lower()
            for banned in ("create", "edit", "close", "merge", "comment", "approve", "ready"):
                self.assertNotIn(banned, joined.split())

    def test_checks_green_at_empty_rollup_uses_merged_at(self):
        green = self.checks_green_at([], merged_at="2026-11-08T22:20:38Z")
        self.assertEqual("2026-11-08T22:20:38Z", green)
        self.assertIsNone(self.checks_green_at([], merged_at=None))

    def test_checks_green_at_rejects_failure(self):
        checks = [
            {"conclusion": "FAILURE", "status": "COMPLETED", "completedAt": "2026-01-01T01:00:00Z"},
            {"conclusion": "SUCCESS", "status": "COMPLETED", "completedAt": "2026-01-01T02:00:00Z"},
        ]
        self.assertIsNone(self.checks_green_at(checks, merged_at="2026-01-01T03:00:00Z"))

    def test_no_linked_pr_sample_is_no_evaluable(self):
        gh = _FakeGh(
            {
                ("issue", "view", "7", "--json", "number,title,createdAt,state"): _issue_json(7),
                (
                    "pr",
                    "list",
                    "--state",
                    "all",
                    "--search",
                    "7",
                    "--limit",
                    "30",
                    "--json",
                    "number,title,createdAt,mergedAt,statusCheckRollup,closingIssuesReferences",
                ): "[]",
            }
        )
        sample = self.lead_time_sample(7, runner=gh)
        result = self.lead_time_from_sample(sample)
        self.assertEqual("no-evaluable", result["status"])
        self.assertIsNone(result["seconds"])


class ForgePortCliTest(unittest.TestCase):
    def test_metrics_lead_time_issue_uses_forge_port(self):
        from aisdlc import forge  # noqa: E402

        calls: list[list[str]] = []

        def runner(argv: list[str]) -> subprocess.CompletedProcess[str]:
            calls.append(list(argv))
            if argv[:2] == ["issue", "view"]:
                return subprocess.CompletedProcess(
                    argv, 0, stdout=_issue_json(99, "2026-01-01T00:00:00Z"), stderr=""
                )
            if argv[:2] == ["pr", "list"]:
                return subprocess.CompletedProcess(
                    argv,
                    0,
                    stdout=_pr_list_json(
                        number=100,
                        issue=99,
                        checks_completed="2026-01-01T01:00:00Z",
                        merged_at="2026-01-01T01:05:00Z",
                    ),
                    stderr="",
                )
            return subprocess.CompletedProcess(argv, 1, stdout="", stderr="unexpected")

        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            """,
        )
        # Patch default runner for CLI path
        previous = forge._default_runner
        forge._default_runner = runner  # type: ignore[assignment]
        self.addCleanup(lambda: setattr(forge, "_default_runner", previous))

        result = repo.run("metrics", "lead-time", "--issue", "99", "--json")
        self.assertEqual(0, result.returncode, output(result))
        payload = json.loads(result.stdout)
        self.assertEqual("ok", payload["status"])
        self.assertEqual(99, payload["issue"])
        self.assertEqual(100, payload["pull_request"])
        self.assertEqual(3600.0, payload["seconds"])
        self.assertTrue(calls)
        self.assertNotIn("prompt", json.dumps(payload))

    def test_metrics_lead_time_issue_no_evaluable(self):
        from aisdlc import forge  # noqa: E402

        def runner(argv: list[str]) -> subprocess.CompletedProcess[str]:
            if argv[:2] == ["issue", "view"]:
                return subprocess.CompletedProcess(
                    argv, 0, stdout=_issue_json(8, "2026-01-01T00:00:00Z"), stderr=""
                )
            if argv[:2] == ["pr", "list"]:
                return subprocess.CompletedProcess(argv, 0, stdout="[]", stderr="")
            return subprocess.CompletedProcess(argv, 1, stdout="", stderr="unexpected")

        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            """,
        )
        previous = forge._default_runner
        forge._default_runner = runner  # type: ignore[assignment]
        self.addCleanup(lambda: setattr(forge, "_default_runner", previous))

        result = repo.run("metrics", "lead-time", "--issue", "8", "--json")
        self.assertEqual(1, result.returncode, output(result))
        payload = json.loads(result.stdout)
        self.assertEqual("no-evaluable", payload["status"])


if __name__ == "__main__":
    unittest.main()
