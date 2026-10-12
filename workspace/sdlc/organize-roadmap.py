#!/usr/bin/env python3
"""Plan (and optionally apply) the Notaire roadmap milestone and lane layout.

Default is a dry run. This command never edits GitHub issues, labels, or
milestones. `--apply` exits 2 and points at the maintainer steps in
docs/300-development/305-project-management/ROADMAP.md.

  python3 workspace/sdlc/organize-roadmap.py --inventory issues.jsonl --snapshot out.json
  python3 workspace/sdlc/organize-roadmap.py --fetch --snapshot docs/.../roadmap-snapshot.json

--fetch pages issues through `gh api graphql` (read). Classification rules live
in roadmap_classify.py.
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "workspace" / "sdlc"))

from roadmap_classify import (  # noqa: E402
    BACKLOG,
    CLOUD,
    PHASE1_HIST,
    PHASE2_HIST,
    PHASE3_HIST,
    PHASE4,
    PHASE4_HIST,
    PHASE5_HIST,
    PHASE6,
    SCOPE_CU,
    SCOPE_NFR,
    SCOPE_RF,
    V10,
    V11,
    Assignment,
    classify,
    summarize,
)

REPO = "matiaspakua/notaire"

# Due dates already used on historical milestones, plus sequence anchors for
# trains that have no date yet. These are ordering markers for the roadmap
# view, not effort estimates.
MILESTONE_META = {
    PHASE1_HIST: {
        "state": "closed",
        "due_on": "2026-05-31T00:00:00Z",
        "description": "Closed historical cohort: work finished through May 2026 (analysis and early foundation) that had no milestone.",
    },
    PHASE2_HIST: {
        "state": "closed",
        "due_on": "2026-06-30T00:00:00Z",
        "description": "Closed historical cohort for issues explicitly labeled phase:2-foundation.",
    },
    PHASE3_HIST: {
        "state": "closed",
        "due_on": "2026-06-30T00:00:00Z",
        "description": "Closed historical cohort: June 2026 backend API deliveries that had no milestone. SAD Phase 3.",
    },
    PHASE4_HIST: {
        "state": "closed",
        "due_on": "2026-08-31T00:00:00Z",
        "description": "Closed historical cohort: July–August 2026 frontend deliveries that had no milestone. Kept off the open Phase 4 train so progress is not inflated.",
    },
    PHASE4: {
        "state": "open",
        "due_on": "2026-12-31T00:00:00Z",
        "description": "Remaining Next.js use-case screens. Reopen milestone #9. Historical July–August work is a separate closed milestone.",
    },
    PHASE5_HIST: {
        "state": "closed",
        "due_on": "2026-09-30T00:00:00Z",
        "description": "Closed historical cohort: September 2026 observability work and the production-readiness audit wave.",
    },
    PHASE6: {
        "state": "open",
        "due_on": "2027-01-31T00:00:00Z",
        "description": "Legacy retirement: jpa package and remaining deprecation. Critical production blockers stay on v1.0.",
    },
    CLOUD: {
        "state": "closed",
        "due_on": "2026-10-31T00:00:00Z",
        "description": "Closed historical cohort: October 2026 Cloud AI SDLC deliveries that had no milestone.",
    },
    V10: {
        "state": "open",
        "due_on": "2027-03-31T00:00:00Z",
        "description": "Production gate: critical and high security, data-integrity, and release blockers. Not the home of every high-priority feature.",
    },
    V11: {
        "state": "open",
        "due_on": "2027-06-30T00:00:00Z",
        "description": "Hardening after the production gate: medium quality, performance, tests, and docs.",
    },
    BACKLOG: {
        "state": "open",
        "due_on": None,
        "description": "Unscheduled low-priority work and issues that still need an owner decision.",
    },
    SCOPE_RF: {
        "state": "open",
        "due_on": None,
        "description": "Permanent functional-requirement catalog (RF-*). Not claimable delivery work. Stays open for traceability.",
    },
    SCOPE_CU: {
        "state": "open",
        "due_on": None,
        "description": "Permanent use-case catalog (CU). Not claimable delivery work. Stays open for traceability.",
    },
    SCOPE_NFR: {
        "state": "open",
        "due_on": None,
        "description": "Permanent non-functional catalog (RNF-*). Delivery assessment is recorded per theme (security→v1.0, experience→Phase 4, performance→v1.1, platform→Backlog) without moving the catalog onto those trains.",
    },
}

LANE_COLORS = {
    "lane:backend": "1d76db",
    "lane:frontend": "0096C7",
    "lane:fullstack": "0A84FF",
    "lane:devops": "7cd5b4",
    "lane:docs": "fad8c3",
    "lane:test": "9b59b6",
    "lane:security": "b60205",
    "lane:owner": "fbca04",
}

FRONTEND_DESCRIPTION = "Next.js web client, forms, and pages. Swing is not a target."


def load_jsonl(path: Path) -> list[dict]:
    rows = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line:
            rows.append(json.loads(line))
    return rows


def gh_api(args: list[str], check: bool = True) -> str:
    proc = subprocess.run(["gh", "api", *args], capture_output=True, text=True)
    if check and proc.returncode != 0:
        raise SystemExit(proc.stderr.strip() or proc.stdout.strip() or "gh api failed")
    return proc.stdout


def fetch_issues() -> list[dict]:
    query = """
    query($cursor:String) {
      repository(owner:"matiaspakua", name:"notaire") {
        issues(first:50, after:$cursor, states:[OPEN,CLOSED], orderBy:{field:CREATED_AT, direction:ASC}) {
          pageInfo { hasNextPage endCursor }
          nodes {
            number title state createdAt closedAt
            milestone { number title }
            labels(first:30) { nodes { name } }
          }
        }
      }
    }
    """
    cursor = ""
    rows: list[dict] = []
    while True:
        raw = gh_api(["graphql", "-f", "query=" + query, "-f", f"cursor={cursor}"])
        data = json.loads(raw)
        conn = data["data"]["repository"]["issues"]
        for node in conn["nodes"]:
            rows.append(
                {
                    "number": node["number"],
                    "title": node["title"],
                    "state": node["state"].lower(),
                    "milestone": (node["milestone"] or {}).get("title", ""),
                    "milestone_n": (node["milestone"] or {}).get("number"),
                    "created": node["createdAt"][:10],
                    "closed": (node["closedAt"] or "")[:10],
                    "labels": [label["name"] for label in node["labels"]["nodes"]],
                }
            )
        if not conn["pageInfo"]["hasNextPage"]:
            break
        cursor = conn["pageInfo"]["endCursor"]
    return rows


def build_snapshot(issues: list[dict], rows: list[Assignment]) -> dict:
    summary = summarize(rows)
    current = Counter((issue.get("milestone") or "(none)") for issue in issues)
    moves = []
    for issue, row in zip(issues, rows):
        now = (issue.get("milestone") or "").strip()
        if now != row.milestone:
            moves.append(
                {
                    "number": row.number,
                    "state": row.state,
                    "from": now or "(none)",
                    "to": row.milestone,
                    "reason": row.reason,
                    "lane": row.lane,
                }
            )
    milestones = []
    for title, counts in sorted(summary["by_milestone"].items()):
        meta = MILESTONE_META.get(title, {})
        state = meta.get("state")
        if not state:
            state = "closed" if counts.get("open", 0) == 0 else "open"
        if title.startswith("Scope —"):
            kind = "scope"
        elif state == "closed":
            kind = "history"
        else:
            kind = "delivery"
        milestones.append(
            {
                "title": title,
                "state": state,
                "kind": kind,
                "open": counts.get("open", 0),
                "closed": counts.get("closed", 0),
                "due_on": meta.get("due_on"),
                "description": meta.get("description", ""),
            }
        )
    open_n = sum(1 for issue in issues if issue.get("state") == "open")
    return {
        "as_of": "2026-10-12",
        "repo": REPO,
        "issues": len(issues),
        "open": open_n,
        "closed": len(issues) - open_n,
        "current_unassigned": current.get("(none)", 0),
        "moves": len(moves),
        "claimable_open": summary["claimable_open"],
        "by_milestone": milestones,
        "by_lane_claimable_open": summary["by_lane_claimable_open"],
        "nfr_trains": summary["nfr_trains"],
        "move_reasons": dict(Counter(item["reason"] for item in moves)),
        "labels_to_add": [
            {"name": name, "color": LANE_COLORS[name], "description": "Parallel-agent lane. Exactly one per issue."}
            for name in LANE_COLORS
        ],
        "label_updates": [
            {"name": "FRONTEND", "description": FRONTEND_DESCRIPTION},
        ],
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--inventory", type=Path, help="JSONL export of issues")
    parser.add_argument("--fetch", action="store_true", help="Read issues with gh")
    parser.add_argument("--snapshot", type=Path, help="Write the aggregate snapshot JSON")
    parser.add_argument(
        "--moves",
        type=Path,
        help="Write the per-issue move list JSONL (number, from, to, lane)",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Mutate GitHub milestones, labels, and issue milestone assignment",
    )
    args = parser.parse_args()
    if args.fetch:
        issues = fetch_issues()
    elif args.inventory:
        issues = load_jsonl(args.inventory)
    else:
        parser.error("pass --inventory or --fetch")
    rows = [classify(issue) for issue in issues]
    snapshot = build_snapshot(issues, rows)
    text = json.dumps(snapshot, indent=2, ensure_ascii=False)
    if args.snapshot:
        args.snapshot.parent.mkdir(parents=True, exist_ok=True)
        args.snapshot.write_text(text + "\n", encoding="utf-8")
    if args.moves:
        args.moves.parent.mkdir(parents=True, exist_ok=True)
        with args.moves.open("w", encoding="utf-8") as handle:
            for issue, row in zip(issues, rows):
                now = (issue.get("milestone") or "").strip()
                if now == row.milestone and row.lane in set(issue.get("labels") or []):
                    continue
                handle.write(
                    json.dumps(
                        {
                            "number": row.number,
                            "state": row.state,
                            "from": now or "(none)",
                            "to": row.milestone,
                            "reason": row.reason,
                            "lane": row.lane,
                            "milestone_change": now != row.milestone,
                        },
                        ensure_ascii=False,
                    )
                    + "\n"
                )
    print(text)
    if args.apply:
        print(
            "Not applied. This command plans the board; it does not mutate GitHub. "
            "Create the milestones and lane labels from the snapshot, then set each "
            "issue milestone from --moves. See docs/300-development/305-project-management/ROADMAP.md.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
