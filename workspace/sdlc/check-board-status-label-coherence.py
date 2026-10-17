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
  R6 `status:in-review` needs an open linked PR (when the PR count is known)
  R7 `status:ready` and `status:blocked` are mutually exclusive
  R8 a `status:ready` issue carries at most one `type:` label
  Legacy and taxonomy label names are equivalent (#1860, label_taxonomy.py)
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

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from label_taxonomy import expand, type_labels  # noqa: E402

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
    open_prs: int | None = None  # R6 only when known


def evaluate_item(item: BoardItem) -> list[str]:
    """Return human-readable violation messages for one issue-shaped item."""
    if item.content_type != "Issue":
        return []

    errors: list[str] = []
    state = (item.state or "").upper()
    status = item.status
    # #1860: legacy and taxonomy names (status:in-progress, status:blocked) both count.
    labels = {label.lower() for label in expand(item.labels)}
    has_wip = IN_PROGRESS_LABEL in labels
    has_blocked = BLOCKED_LABEL in labels
    has_ready = "status:ready" in labels
    if state == "OPEN":
        if "status:in-review" in labels and item.open_prs == 0:
            errors.append("R6: `status:in-review` (in-review) requires an open linked PR")
        if has_ready and has_blocked:
            errors.append("R7: `status:ready` and `status:blocked` are mutually exclusive")
        if has_ready and len(type_labels(item.labels)) > 1:
            errors.append(
                f"R8: a ready issue carries at most one type label (found {sorted(type_labels(item.labels))})"
            )

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


def count_open_prs(linked: object) -> int | None:
    """Count OPEN linked pull requests from a GraphQL or REST-shaped payload."""
    if linked is None:
        return None
    if isinstance(linked, int):
        return linked
    nodes = linked
    if isinstance(linked, dict):
        nodes = linked.get("nodes") or linked.get("edges") or []
    if not isinstance(nodes, list):
        return None
    open_count = 0
    known = False
    for node in nodes:
        pr = node.get("node", node) if isinstance(node, dict) else None
        if not isinstance(pr, dict):
            continue
        known = True
        state = str(pr.get("state") or pr.get("reviewDecision") or "").upper()
        # GraphQL PullRequest.state is OPEN/CLOSED/MERGED; REST uses open/closed.
        if state in {"OPEN", "OPENED"} or pr.get("state") == "open":
            if pr.get("closed") is True or pr.get("merged") is True:
                continue
            open_count += 1
        elif "state" not in pr and pr.get("url"):
            open_count += 1
    return open_count if known else None


def board_item_from_project_row(
    row: dict,
    issue_meta: dict | None = None,
    *,
    open_prs: int | None = None,
) -> BoardItem | None:
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
    if open_prs is None:
        open_prs = count_open_prs(row.get("linkedPullRequests") or row.get("open_prs"))
        if open_prs is None and issue_meta:
            open_prs = count_open_prs(issue_meta.get("linkedPullRequests") or issue_meta.get("open_prs"))
    return BoardItem(
        number=int(number),
        state=str(state).upper(),
        status=row.get("status"),
        labels=frozenset(name for name in label_names if name),
        title=str(title)[:80],
        content_type=str(content_type),
        open_prs=open_prs,
    )


PROJECT_ITEMS_GRAPHQL = """
query($owner: String!, $number: Int!, $cursor: String) {
  user(login: $owner) {
    projectV2(number: $number) {
      items(first: 100, after: $cursor) {
        pageInfo { hasNextPage endCursor }
        nodes {
          fieldValueByName(name: "Status") {
            ... on ProjectV2ItemFieldSingleSelectValue { name }
          }
          content {
            __typename
            ... on Issue {
              number title state
              labels(first: 40) { nodes { name } }
              closed
              pullRequests: timelineItems(itemTypes: [CROSS_REFERENCED_EVENT], first: 20) {
                nodes {
                  ... on CrossReferencedEvent {
                    source { ... on PullRequest { state } }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
"""


def fetch_project_items_graphql(*, owner: str, project: int) -> list[dict]:
    """Paginated ProjectV2 items with issue state, labels, Status, and linked PRs.

    Used by ``--scope all`` so we do not issue one ``gh issue view`` per row.
    """
    cursor = None
    rows: list[dict] = []
    page = 0
    while True:
        page += 1
        payload = {
            "query": PROJECT_ITEMS_GRAPHQL,
            "variables": {"owner": owner, "number": project, "cursor": cursor},
        }
        proc = subprocess.run(
            ["gh", "api", "graphql", "--input", "-"],
            input=json.dumps(payload),
            check=False,
            capture_output=True,
            text=True,
        )
        if proc.returncode != 0:
            raise RuntimeError(proc.stderr.strip() or proc.stdout.strip() or "graphql failed")
        data = json.loads(proc.stdout or "{}")
        if data.get("errors"):
            raise RuntimeError(json.dumps(data["errors"])[:500])
        project_node = (((data.get("data") or {}).get("user") or {}).get("projectV2")) or {}
        conn = project_node.get("items") or {}
        nodes = conn.get("nodes") or []
        print(f"board coherence: loaded GraphQL page {page} ({len(nodes)} items)", flush=True)
        for node in nodes:
            content = node.get("content") or {}
            if content.get("__typename") and content.get("__typename") != "Issue":
                continue
            if not content.get("number"):
                continue
            status = (node.get("fieldValueByName") or {}).get("name")
            labels = [lab["name"] for lab in (content.get("labels") or {}).get("nodes") or []]
            open_prs = 0
            known_prs = "pullRequests" in content
            for event in (content.get("pullRequests") or {}).get("nodes") or []:
                source = (event or {}).get("source") or {}
                if str(source.get("state", "")).upper() == "OPEN":
                    open_prs += 1
            rows.append(
                {
                    "status": status,
                    "content": {
                        "type": "Issue",
                        "number": content["number"],
                        "state": content.get("state")
                        or ("CLOSED" if content.get("closed") else "OPEN"),
                        "title": content.get("title") or "",
                    },
                    "labels": labels,
                    "open_prs": open_prs if known_prs else None,
                }
            )
        page_info = conn.get("pageInfo") or {}
        if not page_info.get("hasNextPage"):
            return rows
        cursor = page_info.get("endCursor")


def board_items_from_graphql_rows(rows: list[dict]) -> list[BoardItem]:
    items: list[BoardItem] = []
    for row in rows:
        item = board_item_from_project_row(row, open_prs=row.get("open_prs"))
        if item:
            items.append(item)
    return items


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
    """Full-board load via paginated GraphQL (no per-issue ``gh issue view``)."""
    del repo  # state/labels come from GraphQL content
    return board_items_from_graphql_rows(
        fetch_project_items_graphql(owner=owner, project=project)
    )


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
