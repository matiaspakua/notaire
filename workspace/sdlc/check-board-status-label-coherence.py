#!/usr/bin/env python3
"""Board Status ↔ GitHub Issue state ↔ labels coherence gate (#1550, CU76).

CONSTITUTION Gate 5 / AI SDLC board hygiene: Issues, Delivery Board Status, and
workflow labels must stay coherent. Process Checks and preflight invoke this
script so drift fails (or skips clearly when `gh` is unavailable).

Normative rules (internal/board-status-label-audit.md + docs/github/README.md):
  R1 CLOSED ⇒ Status Done (not In Progress / Todo / empty); drop WIP labels
  R2 OPEN WIP ⇒ Status In Progress ⇔ `in-progress` label; must not be Done
  R3 Blocked via `blocked` label (no Blocked Status column); not Done while OPEN;
     prefer not combining `blocked` + `in-progress`
  R4 Done / AC met ⇒ no WIP labels; prefer CLOSED + Done
  R5 Do not invent Owner product scope — coherence only
  Fail when drift is detected for items in scope; skip clearly without gh auth

USAGE
  python3 workspace/sdlc/check-board-status-label-coherence.py
  python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip
  python3 workspace/sdlc/check-board-status-label-coherence.py --scope pr
  python3 workspace/sdlc/check-board-status-label-coherence.py --scope all
  python3 workspace/sdlc/check-board-status-label-coherence.py --issue 1550

Env:
  SKIP_BOARD_COHERENCE=1   skip live checks (exit 0 with a clear message)
  BOARD_COHERENCE_OWNER    GitHub project owner (default matiaspakua)
  BOARD_COHERENCE_PROJECT  Project number (default 1)
  BOARD_COHERENCE_REPO     owner/name (default matiaspakua/notaire)
  GH_TOKEN / GITHUB_TOKEN  used by `gh` when present
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from shutil import which
from typing import Iterable

IN_PROGRESS_LABEL = "in-progress"
BLOCKED_LABEL = "blocked"
STATUS_TODO = "Todo"
STATUS_IN_PROGRESS = "In Progress"
STATUS_DONE = "Done"
WIP_STATUSES = frozenset({STATUS_IN_PROGRESS})
NON_DONE_STATUSES = frozenset({STATUS_TODO, STATUS_IN_PROGRESS})

ISSUE_REF = re.compile(
    r"(?:clos(?:e|es|ed|ing)|fix(?:es|ed)?|resolve(?:s|d)?|refs?)\s+#(\d+)",
    re.I,
)
BARE_ISSUE = re.compile(r"(?:^|[\s\[(])#(\d+)\b")


@dataclass(frozen=True)
class BoardItem:
    number: int
    state: str
    status: str | None
    labels: frozenset[str]
    title: str = ""
    content_type: str = "Issue"


def evaluate_item(item: BoardItem) -> list[str]:
    """Return human-readable violation messages for one issue-shaped item."""
    if item.content_type != "Issue":
        return []

    errors: list[str] = []
    state = (item.state or "").upper()
    status = item.status
    labels = {label.lower() for label in item.labels}
    has_wip = IN_PROGRESS_LABEL in labels
    has_blocked = BLOCKED_LABEL in labels

    if state == "CLOSED":
        if status != STATUS_DONE:
            errors.append(
                f"CLOSED issue must have Status Done (found {status!r})"
            )
        if has_wip:
            errors.append(
                f"CLOSED issue must not carry the `{IN_PROGRESS_LABEL}` label"
            )
        if has_blocked:
            errors.append(
                f"CLOSED issue should not keep the `{BLOCKED_LABEL}` label "
                "(drop WIP / blocked labels when Done)"
            )
    elif state == "OPEN":
        if status == STATUS_IN_PROGRESS and not has_wip:
            errors.append(
                f"OPEN + Status In Progress requires the `{IN_PROGRESS_LABEL}` label"
            )
        if has_wip and status is not None and status != STATUS_IN_PROGRESS:
            errors.append(
                f"`{IN_PROGRESS_LABEL}` label requires Status In Progress "
                f"(found {status!r})"
            )
        if status == STATUS_DONE:
            errors.append(
                "Done Status prefers a CLOSED issue when acceptance criteria are met"
            )
        if has_blocked and status == STATUS_DONE:
            errors.append(
                "OPEN issue labeled `blocked` must not have Status Done "
                "(Project has no Blocked column — use Todo or In Progress)"
            )
        if has_blocked and has_wip:
            errors.append(
                f"prefer `{BLOCKED_LABEL}` without `{IN_PROGRESS_LABEL}` "
                "(or note why both apply)"
            )
        if status == STATUS_DONE and has_wip:
            errors.append(
                f"Done Status must not carry the `{IN_PROGRESS_LABEL}` label"
            )
    else:
        errors.append(f"unknown issue state {item.state!r}")

    return errors


def evaluate_items(items: Iterable[BoardItem]) -> list[str]:
    out: list[str] = []
    for item in items:
        for msg in evaluate_item(item):
            title = f" ({item.title})" if item.title else ""
            out.append(f"#{item.number}{title}: {msg}")
    return out


def gh_available() -> bool:
    if which("gh") is None:
        return False
    token = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")
    if token:
        return True
    try:
        subprocess.run(
            ["gh", "auth", "status"],
            check=True,
            capture_output=True,
            text=True,
        )
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False


def run_gh(args: list[str]) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["gh", *args],
        check=False,
        capture_output=True,
        text=True,
    )


def parse_issue_numbers(*texts: str) -> set[int]:
    found: set[int] = set()
    for text in texts:
        if not text:
            continue
        for match in ISSUE_REF.finditer(text):
            found.add(int(match.group(1)))
        for match in BARE_ISSUE.finditer(text):
            found.add(int(match.group(1)))
    return found


def fetch_project_items(
    *, owner: str, project: int, limit: int = 1000
) -> list[dict]:
    result = run_gh(
        [
            "project",
            "item-list",
            str(project),
            "--owner",
            owner,
            "--format",
            "json",
            "--limit",
            str(limit),
        ]
    )
    if result.returncode != 0:
        raise RuntimeError(
            result.stderr.strip() or result.stdout.strip() or "gh project item-list failed"
        )
    data = json.loads(result.stdout or "{}")
    if isinstance(data, list):
        return data
    return list(data.get("items") or [])


def fetch_issue(repo: str, number: int) -> dict:
    result = run_gh(
        ["issue", "view", str(number), "--repo", repo, "--json", "number,state,title,labels"]
    )
    if result.returncode != 0:
        raise RuntimeError(
            result.stderr.strip() or f"gh issue view #{number} failed"
        )
    return json.loads(result.stdout)


def board_item_from_project_row(row: dict, issue_meta: dict | None = None) -> BoardItem | None:
    content = row.get("content") or {}
    content_type = content.get("type") or "Issue"
    number = content.get("number")
    if not number:
        return None
    labels = row.get("labels")
    if labels is None and issue_meta:
        labels = [lab.get("name", lab) if isinstance(lab, dict) else lab for lab in issue_meta.get("labels") or []]
    elif labels is None:
        labels = []
    label_names = []
    for lab in labels:
        if isinstance(lab, dict):
            label_names.append(lab.get("name", ""))
        else:
            label_names.append(str(lab))
    state = (issue_meta or {}).get("state") or content.get("state") or "UNKNOWN"
    title = (issue_meta or {}).get("title") or content.get("title") or row.get("title") or ""
    return BoardItem(
        number=int(number),
        state=str(state).upper(),
        status=row.get("status"),
        labels=frozenset(name for name in label_names if name),
        title=str(title)[:80],
        content_type=str(content_type),
    )


def load_wip_items(*, owner: str, project: int, repo: str) -> list[BoardItem]:
    rows = fetch_project_items(owner=owner, project=project)
    items: list[BoardItem] = []
    for row in rows:
        if row.get("status") not in WIP_STATUSES:
            continue
        content = row.get("content") or {}
        if (content.get("type") or "Issue") != "Issue":
            continue
        number = content.get("number")
        if not number:
            continue
        meta = fetch_issue(repo, int(number))
        item = board_item_from_project_row(row, meta)
        if item:
            items.append(item)
    return items


def load_all_issue_items(*, owner: str, project: int, repo: str) -> list[BoardItem]:
    rows = fetch_project_items(owner=owner, project=project)
    items: list[BoardItem] = []
    for row in rows:
        content = row.get("content") or {}
        if (content.get("type") or "Issue") != "Issue":
            continue
        number = content.get("number")
        if not number:
            continue
        meta = fetch_issue(repo, int(number))
        item = board_item_from_project_row(row, meta)
        if item:
            items.append(item)
    return items


def load_pr_linked_items(*, repo: str, owner: str, project: int) -> list[BoardItem]:
    pr = run_gh(["pr", "view", "--repo", repo, "--json", "body,title,commits"])
    if pr.returncode != 0:
        return []
    payload = json.loads(pr.stdout or "{}")
    texts = [payload.get("body") or "", payload.get("title") or ""]
    for commit in payload.get("commits") or []:
        msg = commit.get("messageHeadline") or commit.get("messageBody") or ""
        texts.append(msg)
    numbers = parse_issue_numbers(*texts)
    if not numbers:
        return []

    status_by_number: dict[int, str | None] = {}
    try:
        for row in fetch_project_items(owner=owner, project=project):
            content = row.get("content") or {}
            if (content.get("type") or "Issue") != "Issue":
                continue
            number = content.get("number")
            if number and int(number) in numbers:
                status_by_number[int(number)] = row.get("status")
    except RuntimeError as exc:
        print(f"! project lookup skipped for PR scope: {exc}", file=sys.stderr)

    items: list[BoardItem] = []
    for number in sorted(numbers):
        meta = fetch_issue(repo, number)
        labels = [
            lab.get("name", "") if isinstance(lab, dict) else str(lab)
            for lab in meta.get("labels") or []
        ]
        items.append(
            BoardItem(
                number=number,
                state=str(meta.get("state", "UNKNOWN")).upper(),
                status=status_by_number.get(number),
                labels=frozenset(name for name in labels if name),
                title=str(meta.get("title") or "")[:80],
            )
        )
    return items


def load_single_issue(*, repo: str, owner: str, project: int, number: int) -> list[BoardItem]:
    meta = fetch_issue(repo, number)
    status = None
    try:
        for row in fetch_project_items(owner=owner, project=project):
            content = row.get("content") or {}
            if content.get("number") == number and (content.get("type") or "Issue") == "Issue":
                status = row.get("status")
                break
    except RuntimeError as exc:
        print(f"! project lookup skipped for --issue: {exc}", file=sys.stderr)
    labels = [
        lab.get("name", "") if isinstance(lab, dict) else str(lab)
        for lab in meta.get("labels") or []
    ]
    return [
        BoardItem(
            number=number,
            state=str(meta.get("state", "UNKNOWN")).upper(),
            status=status,
            labels=frozenset(name for name in labels if name),
            title=str(meta.get("title") or "")[:80],
        )
    ]


def run_check(
    *,
    scope: str = "pr",
    issue: int | None = None,
    force_skip: bool = False,
    owner: str | None = None,
    project: int | None = None,
    repo: str | None = None,
) -> tuple[int, str]:
    """Return (exit_code, summary_message). Pure skip path is force_skip=True."""
    if force_skip or os.environ.get("SKIP_BOARD_COHERENCE") == "1":
        return 0, "board Status/label coherence skipped (SKIP_BOARD_COHERENCE=1)"

    if not gh_available():
        return (
            0,
            "board Status/label coherence skipped "
            "(gh not authenticated / GH_TOKEN absent — live check not verified)",
        )

    owner = owner or os.environ.get("BOARD_COHERENCE_OWNER", "matiaspakua")
    project = project or int(os.environ.get("BOARD_COHERENCE_PROJECT", "1"))
    repo = repo or os.environ.get("BOARD_COHERENCE_REPO", "matiaspakua/notaire")

    try:
        if issue is not None:
            items = load_single_issue(repo=repo, owner=owner, project=project, number=issue)
        elif scope == "wip":
            items = load_wip_items(owner=owner, project=project, repo=repo)
        elif scope == "all":
            items = load_all_issue_items(owner=owner, project=project, repo=repo)
        elif scope == "pr":
            items = load_pr_linked_items(repo=repo, owner=owner, project=project)
            if not items:
                return 0, "board Status/label coherence: no PR-linked issues in scope"
        else:
            return 2, f"unknown scope {scope!r}"
    except RuntimeError as exc:
        return (
            0,
            f"board Status/label coherence skipped (live lookup failed: {exc})",
        )

    errors = evaluate_items(items)
    if errors:
        lines = ["✗ board Status/label/issue-state coherence failed"]
        lines.extend(f"  {error}" for error in errors)
        lines.append(
            "  CONSTITUTION Gate 5 / #1550: sync Issue state, Project Status, and "
            f"`{IN_PROGRESS_LABEL}` / `{BLOCKED_LABEL}` labels "
            "(python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip)."
        )
        return 1, "\n".join(lines)

    return (
        0,
        f"✓ board Status/label coherence OK ({scope}: {len(items)} item(s))",
    )


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--scope",
        choices=("pr", "wip", "all"),
        default="pr",
        help="pr=issues linked on the open PR (default CI); wip=In Progress column; all=full board",
    )
    parser.add_argument("--issue", type=int, help="check a single issue number")
    parser.add_argument(
        "--owner",
        default=None,
        help="GitHub project owner (default BOARD_COHERENCE_OWNER or matiaspakua)",
    )
    parser.add_argument(
        "--project",
        type=int,
        default=None,
        help="Project number (default BOARD_COHERENCE_PROJECT or 1)",
    )
    parser.add_argument(
        "--repo",
        default=None,
        help="owner/name (default BOARD_COHERENCE_REPO or matiaspakua/notaire)",
    )
    args = parser.parse_args(argv)

    code, message = run_check(
        scope=args.scope,
        issue=args.issue,
        owner=args.owner,
        project=args.project,
        repo=args.repo,
    )
    print(message)
    return code


if __name__ == "__main__":
    sys.exit(main())
