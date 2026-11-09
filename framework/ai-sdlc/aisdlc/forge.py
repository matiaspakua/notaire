"""ForgePort — read-only forge access for delivery metrics (#2184).

Fetches issue open time and linked pull requests via the `gh` CLI and builds the
same sample document shape consumed by `aisdlc.metrics.lead_time_from_sample`
(#2138). This port MUST NOT mutate the forge: no issue/PR create/edit/close/merge,
no check-run create/update, no `factory/gate` attestation writes.

Default transport is subprocess `gh`. Unit tests inject a runner so the suite
never touches the network.
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
from collections.abc import Callable
from typing import Any

Runner = Callable[[list[str]], subprocess.CompletedProcess[str]]

READ_ONLY_VERBS = frozenset({"issue", "pr", "api", "repo", "status", "auth", "browse"})
# Verbs / actions that would mutate the forge — never used by this port.
WRITE_VERBS = frozenset(
    {
        "create",
        "edit",
        "close",
        "reopen",
        "merge",
        "comment",
        "delete",
        "lock",
        "unlock",
        "ready",
        "approve",
        "review",
        "request-reviewers",
        "add-assignee",
        "remove-assignee",
        "add-label",
        "remove-label",
        "set",
        "upload",
        "run",
        "workflow",
        "cache",
        "secret",
        "variable",
        "gist",
        "release",
        "label",
        "milestone",
        "project",
        "alias",
        "extension",
        "codespace",
        "attestation",
    }
)

_OK_CONCLUSIONS = frozenset({"SUCCESS", "SKIPPED", "NEUTRAL", "NONE", ""})


class ForgeError(Exception):
    """Forge read failed (missing gh, bad JSON, or non-zero gh). Maps to exit 2."""


def _default_runner(argv: list[str]) -> subprocess.CompletedProcess[str]:
    """Invoke `gh` (or `$AISDLC_GH` override for tests) with read-only argv."""
    binary = os.environ.get("AISDLC_GH") or "gh"
    if binary == "gh" and shutil.which("gh") is None:
        raise ForgeError("gh CLI not found on PATH (required for live lead-time)")
    return subprocess.run(
        [binary, *argv],
        check=False,
        capture_output=True,
        text=True,
    )


def checks_green_at(
    checks: list[dict[str, Any]] | None,
    *,
    merged_at: str | None,
) -> str | None:
    """Compute pull_request.checks_green_at from a statusCheckRollup list.

    Rules (honest when product CI is path-scoped / absent on PRs):
    - Any pending / in-progress / queued check → not green.
    - Any failure / cancelled / timed_out / action_required → not green.
    - Otherwise, if there is ≥1 completed check with a timestamp → max(completedAt).
    - Empty rollup + merged_at → merged_at (missing checks with no run = success).
    - Empty rollup + not merged → None.
    """
    rows = list(checks or [])
    if not rows:
        return merged_at
    completed_ats: list[str] = []
    for check in rows:
        status = str(check.get("status") or "").upper()
        conclusion = str(check.get("conclusion") or "").upper()
        if status in {"IN_PROGRESS", "QUEUED", "PENDING", "WAITING", "REQUESTED"}:
            return None
        if status and status not in {"COMPLETED", "SUCCESS"} and not conclusion:
            return None
        if conclusion and conclusion not in _OK_CONCLUSIONS:
            return None
        completed = check.get("completedAt") or check.get("completed_at")
        if completed:
            completed_ats.append(str(completed))
    if completed_ats:
        return max(completed_ats)
    return merged_at


def _run_json(runner: Runner, argv: list[str]) -> Any:
    _assert_read_only(argv)
    result = runner(argv)
    if result.returncode != 0:
        err = (result.stderr or result.stdout or "").strip() or f"gh {' '.join(argv)} failed"
        raise ForgeError(err)
    text = (result.stdout or "").strip()
    if not text:
        raise ForgeError(f"gh {' '.join(argv)} returned empty stdout")
    try:
        return json.loads(text)
    except json.JSONDecodeError as exc:
        raise ForgeError(f"gh {' '.join(argv)} returned invalid JSON: {exc}") from exc


def _assert_read_only(argv: list[str]) -> None:
    if not argv:
        raise ForgeError("empty gh argv")
    head = argv[0]
    if head in WRITE_VERBS:
        raise ForgeError(f"ForgePort refuses write verb {head!r}")
    if head not in READ_ONLY_VERBS:
        # Allow unknown top-level only if no write token appears later.
        pass
    tokens = {t.lower().lstrip("-") for t in argv}
    banned = tokens & {v.lower() for v in WRITE_VERBS}
    # `pr list` / `issue view` are fine; ban mutation actions as discrete tokens.
    action_banned = banned & {
        "create",
        "edit",
        "close",
        "reopen",
        "merge",
        "comment",
        "delete",
        "approve",
        "ready",
        "lock",
        "unlock",
        "attestation",
    }
    if action_banned:
        raise ForgeError(f"ForgePort refuses mutating gh tokens: {sorted(action_banned)}")


def _pr_links_issue(pr: dict[str, Any], issue: int) -> bool:
    refs = pr.get("closingIssuesReferences") or []
    for ref in refs:
        number = ref.get("number") if isinstance(ref, dict) else ref
        if number == issue or str(number) == str(issue):
            return True
    title = str(pr.get("title") or "")
    needle = f"#{issue}"
    return needle in title


def lead_time_sample(
    issue: int,
    *,
    runner: Runner | None = None,
) -> dict[str, Any]:
    """Build the #2138 lead-time sample document for a live issue (read-only)."""
    run = runner or _default_runner
    issue_data = _run_json(
        run,
        ["issue", "view", str(issue), "--json", "number,title,createdAt,state"],
    )
    created = issue_data.get("createdAt") or issue_data.get("created_at")
    if not created:
        raise ForgeError(f"issue #{issue} missing createdAt")

    prs_raw = _run_json(
        run,
        [
            "pr",
            "list",
            "--state",
            "all",
            "--search",
            str(issue),
            "--limit",
            "30",
            "--json",
            "number,title,createdAt,mergedAt,statusCheckRollup,closingIssuesReferences",
        ],
    )
    if not isinstance(prs_raw, list):
        raise ForgeError("gh pr list did not return a JSON array")

    pull_requests: list[dict[str, Any]] = []
    for pr in prs_raw:
        if not isinstance(pr, dict) or pr.get("number") is None:
            continue
        if not _pr_links_issue(pr, int(issue)):
            continue
        green = checks_green_at(
            pr.get("statusCheckRollup") or [],
            merged_at=pr.get("mergedAt") or pr.get("merged_at"),
        )
        entry: dict[str, Any] = {
            "number": int(pr["number"]),
            "title": pr.get("title"),
            "created_at": pr.get("createdAt") or pr.get("created_at"),
            "linked_issues": [int(issue)],
        }
        if green:
            entry["checks_green_at"] = green
        if pr.get("mergedAt") or pr.get("merged_at"):
            entry["merged_at"] = pr.get("mergedAt") or pr.get("merged_at")
        pull_requests.append(entry)

    return {
        "schema": 1,
        "event_pair": {
            "start": "issue.created_at",
            "end": "pull_request.checks_green_at",
            "notes": (
                "Lead time = first linked PR whose required checks are all green, "
                "minus issue open time. Merge is optional and not required for this metric. "
                "Live sample via read-only ForgePort (#2184); never mutates the forge."
            ),
        },
        "sample": {
            "label": f"forge-issue-#{int(issue)}",
            "issue": {
                "number": int(issue_data.get("number") or issue),
                "title": issue_data.get("title"),
                "created_at": str(created),
            },
            "pull_requests": pull_requests,
        },
        "source": "forge-port-readonly",
    }
