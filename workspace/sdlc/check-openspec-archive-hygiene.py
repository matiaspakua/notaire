#!/usr/bin/env python3
"""OpenSpec archive hygiene + implementation task completion gate (#1252, CU76).

CONSTITUTION Gate 5 requires archiving shipped OpenSpec changes. Process Checks
and preflight invoke this script so a PR that touches an active change fails when
mandatory task groups 3–8 remain unchecked (unless `sdlc-exception`). Inventory
mode fails when a CLOSED issue still has an active change tree.

USAGE
  python3 workspace/sdlc/check-openspec-archive-hygiene.py <base> [head]
  python3 workspace/sdlc/check-openspec-archive-hygiene.py --inventory-only

Env:
  PR_LABELS  comma-separated PR labels (sdlc-exception bypasses task gate)
  SKIP_INVENTORY=1  skip live gh issue lookup (task gate only)
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path

CHANGES_PREFIX = "docs/openspec/changes/"
ARCHIVE_PREFIX = "docs/openspec/changes/archive/"
ISSUE_ROW = re.compile(
    r"^\|\s*GitHub Issue\s*\|\s*#(\d+)\s*\|", re.I | re.M
)
ISSUE_FALLBACK = re.compile(r"Issue\s*\|\s*#(\d+)", re.I)
HEADING = re.compile(r"^##\s+(\d+)\.\s+")
CHECKBOX = re.compile(r"^-\s+\[([ xX])\]\s+(\d+\.\d+)\b(.*)$")
MERGE_GATE_NEEDLES = (
    "openspec archive",
    "task-completion",
    "task completion",
    "archive hygiene",
    "check-openspec-archive-hygiene",
)


def git(args: list[str], *, cwd: str | None = None) -> str:
    result = subprocess.run(
        ["git", "-c", "core.quotepath=false", *args],
        check=True,
        capture_output=True,
        text=True,
        cwd=cwd,
    )
    return result.stdout


def diff_base(base: str, head: str) -> str:
    parents = subprocess.run(
        ["git", "rev-parse", "--verify", "--quiet", f"{head}^2"],
        capture_output=True,
        text=True,
    )
    if parents.returncode == 0:
        return git(["rev-parse", f"{head}^1"]).strip()
    return base


def touched_active_changes(changed: list[str]) -> set[str]:
    names: set[str] = set()
    for path in changed:
        if not path.startswith(CHANGES_PREFIX):
            continue
        if path.startswith(ARCHIVE_PREFIX):
            continue
        rest = path[len(CHANGES_PREFIX) :]
        name = rest.split("/", 1)[0]
        if name and name != "archive":
            names.add(name)
    return names


def parse_issue(proposal_text: str) -> int | None:
    match = ISSUE_ROW.search(proposal_text)
    if match:
        return int(match.group(1))
    match = ISSUE_FALLBACK.search(proposal_text)
    return int(match.group(1)) if match else None


def unchecked_impl_tasks(tasks_text: str) -> list[str]:
    """Return unchecked task ids under mandatory groups 3–8."""
    current_group: int | None = None
    unchecked: list[str] = []
    for line in tasks_text.splitlines():
        heading = HEADING.match(line.strip())
        if heading:
            current_group = int(heading.group(1))
            continue
        if current_group is None or current_group < 3 or current_group > 8:
            continue
        box = CHECKBOX.match(line.strip())
        if not box:
            continue
        mark, task_id, _rest = box.group(1), box.group(2), box.group(3)
        if mark.lower() != "x":
            unchecked.append(task_id)
    return unchecked


def check_inventory(
    active: dict[str, int | None],
    issue_states: dict[int, str],
    archived_names: set[str],
) -> list[str]:
    """Fail when a CLOSED issue still has an active (non-archived) change."""
    errors: list[str] = []
    for name, issue in sorted(active.items()):
        if issue is None:
            continue
        state = issue_states.get(issue)
        if state != "CLOSED":
            continue
        # Already archived under a dated prefix that contains the change name.
        if any(name in archived for archived in archived_names):
            continue
        errors.append(
            f"{name}: linked issue #{issue} is CLOSED but the change is still active "
            f"(archive under {ARCHIVE_PREFIX})"
        )
    return errors


def check_merge_gate_doc(text: str) -> list[str]:
    lowered = text.lower()
    if any(needle in lowered for needle in MERGE_GATE_NEEDLES):
        return []
    return [
        "CI-MERGE-GATE.md must instruct agents to verify OpenSpec archive hygiene "
        "and task completion (or run check-openspec-archive-hygiene) before merge"
    ]


def list_active_changes(repo_root: Path) -> dict[str, int | None]:
    root = repo_root / "docs" / "openspec" / "changes"
    active: dict[str, int | None] = {}
    if not root.is_dir():
        return active
    for entry in sorted(root.iterdir()):
        if not entry.is_dir() or entry.name == "archive":
            continue
        proposal = entry / "proposal.md"
        issue = None
        if proposal.is_file():
            issue = parse_issue(proposal.read_text(encoding="utf-8"))
        active[entry.name] = issue
    return active


def list_archived_names(repo_root: Path) -> set[str]:
    archive = repo_root / "docs" / "openspec" / "changes" / "archive"
    if not archive.is_dir():
        return set()
    return {entry.name for entry in archive.iterdir() if entry.is_dir()}


def resolve_issue_states(issues: set[int]) -> dict[int, str]:
    states: dict[int, str] = {}
    if not issues:
        return states
    if not shutil_which("gh"):
        print("! gh not available — skipping live inventory issue lookup", file=sys.stderr)
        return states
    for issue in sorted(issues):
        try:
            out = subprocess.run(
                ["gh", "issue", "view", str(issue), "--json", "state"],
                check=True,
                capture_output=True,
                text=True,
            )
            states[issue] = json.loads(out.stdout).get("state", "UNKNOWN")
        except (subprocess.CalledProcessError, json.JSONDecodeError) as exc:
            print(f"! could not resolve issue #{issue}: {exc}", file=sys.stderr)
    return states


def shutil_which(name: str) -> str | None:
    from shutil import which

    return which(name)


def task_errors_for_change(repo_root: Path, name: str) -> list[str]:
    tasks = repo_root / "docs" / "openspec" / "changes" / name / "tasks.md"
    if not tasks.is_file():
        return [f"{name}: missing tasks.md"]
    unchecked = unchecked_impl_tasks(tasks.read_text(encoding="utf-8"))
    if not unchecked:
        return []
    sample = ", ".join(unchecked[:5])
    more = f" (+{len(unchecked) - 5} more)" if len(unchecked) > 5 else ""
    return [
        f"{name}: unchecked implementation tasks in groups 3–8: {sample}{more}"
    ]


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base", nargs="?", help="git base ref / SHA")
    parser.add_argument("head", nargs="?", default="HEAD", help="git head ref (default HEAD)")
    parser.add_argument(
        "--inventory-only",
        action="store_true",
        help="only run closed-issue active-change inventory",
    )
    parser.add_argument(
        "--skip-inventory",
        action="store_true",
        help="skip live inventory (task completion only)",
    )
    args = parser.parse_args(argv)

    repo_root = Path(git(["rev-parse", "--show-toplevel"]).strip())
    labels = {part.strip() for part in os.environ.get("PR_LABELS", "").split(",") if part.strip()}
    skip_inventory = (
        args.skip_inventory
        or os.environ.get("SKIP_INVENTORY", "") == "1"
        or "sdlc-exception" in labels
    )

    errors: list[str] = []

    if not args.inventory_only:
        if not args.base:
            print("usage: check-openspec-archive-hygiene.py <base> [head]", file=sys.stderr)
            return 2
        head = args.head
        base = diff_base(args.base, head)
        changed = [
            line
            for line in git(["diff", "--name-only", f"{base}...{head}"]).splitlines()
            if line
        ]
        touched = touched_active_changes(changed)
        if "sdlc-exception" in labels:
            print("✓ task-completion gate bypassed (sdlc-exception)")
        elif not touched:
            print("✓ no active OpenSpec change touched — task-completion gate n/a")
        else:
            for name in sorted(touched):
                errors.extend(task_errors_for_change(repo_root, name))

    if args.inventory_only or not skip_inventory:
        active = list_active_changes(repo_root)
        archived = list_archived_names(repo_root)
        issue_ids = {i for i in active.values() if i is not None}
        states = resolve_issue_states(issue_ids)
        if states or args.inventory_only:
            errors.extend(check_inventory(active, states, archived))
        elif not skip_inventory:
            print("! inventory skipped (no issue states resolved)", file=sys.stderr)

    if errors:
        print("✗ OpenSpec archive hygiene / task completion failed")
        for error in errors:
            print(f"  {error}")
        print(
            "  CONSTITUTION Gate 5 / #1252: archive shipped changes; tick groups 3–8 "
            "on touched changes (or add sdlc-exception with an Exceptions row)."
        )
        return 1

    if args.inventory_only:
        print("✓ OpenSpec inventory: no CLOSED-issue leftovers in the active set")
    else:
        print("✓ OpenSpec archive hygiene / task completion OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
