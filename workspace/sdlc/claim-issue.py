#!/usr/bin/env python3
"""Claim a Notaire issue for one agent, or release stale claims (#1646, CU76).

Adding `in-progress` after reading the queue is not atomic: two agents can both
see a lane free. A claim therefore posts a marker comment, waits, re-reads, and
only the earliest marker since the last release wins; the loser withdraws.

USAGE
  python3 workspace/sdlc/claim-issue.py <issue> --agent <id> [--settle 15]
  python3 workspace/sdlc/claim-issue.py --release-stale [--hours 24]

Exit 0 when this agent owns the issue (or the sweep ran), 1 when refused or lost.
Uses `gh api`; set GH_TOKEN to the fleet identity (#1648) when available.
"""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import time
import uuid
from datetime import datetime, timezone
from urllib.parse import quote

from label_taxonomy import expand, wip_label
from milestone_policy import lane, lanes_blocked_by

REPO = "matiaspakua/notaire"
PROJECT_ID = "PVT_kwHOATJKDs4BRC9u"
STATUS_FIELD = "PVTSSF_lAHOATJKDs4BRC9uzg-_XNo"
STATUS_OPTIONS = {"Todo": "f75ad846", "In Progress": "47fc9ee4", "Done": "98236657"}
REFUSED_LABELS = ("blocked", "needs-info")  # legacy names; expand() adds status:*
READY_LABEL = "status:ready"  # or legacy ready-for-dev (#1860)
RELEASE_MARKER = "<!-- notaire-release -->"
CLAIM = re.compile(r"<!-- notaire-claim agent=(\S+) nonce=(\S+) -->")


def claim_marker(agent: str, nonce: str) -> str:
    return f"<!-- notaire-claim agent={agent} nonce={nonce} -->\nClaimed by `{agent}` (#1646 claim protocol)."


def refusal(issue: dict) -> str | None:
    if issue["state"] != "OPEN":
        return "issue is not open"
    labels = expand(issue["labels"])
    if lane(labels, issue["number"], issue["title"]) in {"catalog", "epic"}:
        return "catalog anchors and roadmap epics are not claimable"
    held = labels & set(REFUSED_LABELS)
    if held:
        return f"issue carries {sorted(held)}"
    if READY_LABEL not in labels:
        return "issue is not status:ready (or ready-for-dev)"
    return None


def lane_holder(issue: dict, wip: list[dict]) -> int | None:
    wanted = lane(issue["labels"], issue["number"], issue["title"])
    for other in wip:
        if other["number"] == issue["number"]:
            continue
        held = lane(other["labels"], other["number"], other["title"])
        if wanted in lanes_blocked_by(held):
            return other["number"]
    return None


def active_claims(comments: list[dict]) -> list[tuple[str, str]]:
    """(created_at, nonce) of claim markers after the latest release."""
    claims: list[tuple[str, str]] = []
    for entry in comments:
        if RELEASE_MARKER in entry["body"]:
            claims = []
            continue
        match = CLAIM.search(entry["body"])
        if match:
            claims.append((entry["created_at"], match.group(2)))
    return claims


def winning_nonce(comments: list[dict]) -> str | None:
    claims = active_claims(comments)
    return min(claims)[1] if claims else None


def is_stale(claimed_at: str, now: str, *, hours: float, open_prs: int) -> bool:
    age = datetime.fromisoformat(now.replace("Z", "+00:00")) - datetime.fromisoformat(claimed_at.replace("Z", "+00:00"))
    return open_prs == 0 and age.total_seconds() > hours * 3600


def claim(gh, number: int, agent: str, *, settle: float) -> int:
    issue = gh.issue(number)
    reason = refusal(issue)
    if reason:
        print(f"✗ #{number} not claimable: {reason}")
        return 1
    holder = lane_holder(issue, gh.open_wip())
    if holder:
        print(f"✗ #{number} lane busy: #{holder} is in progress in a conflicting lane")
        return 1
    nonce = uuid.uuid4().hex[:12]
    mine = gh.comment(number, claim_marker(agent, nonce))
    time.sleep(settle)
    if winning_nonce(gh.comments(number)) != nonce:
        gh.delete_comment(mine)
        print(f"✗ #{number} claimed first by another agent; withdrew")
        return 1
    gh.add_label(number, wip_label(gh.repo_labels()))
    if not gh.set_status(number, "In Progress"):
        print(f"! handoff: set #{number} Status to In Progress (token cannot write the Project)")
    print(f"✓ #{number} claimed by {agent}")
    return 0


def release_stale(gh, hours: float) -> int:
    now = datetime.now(timezone.utc).isoformat()
    for issue in gh.open_wip():
        number = issue["number"]
        claims = active_claims(gh.comments(number))
        claimed_at = min(claims)[0] if claims else None
        if claimed_at is None or not is_stale(claimed_at, now, hours=hours, open_prs=gh.open_prs_closing(number)):
            continue
        for name in ("in-progress", "status:in-progress"):
            if name in issue["labels"]:
                gh.remove_label(number, name)
        gh.comment(number, f"{RELEASE_MARKER}\nReleased: claimed {claimed_at}, no open PR after {hours:g} h (#1646).")
        if not gh.set_status(number, "Todo"):
            print(f"! handoff: set #{number} Status to Todo")
        print(f"released #{number}")
    return 0


class Gh:
    """Thin `gh api` client; the fake in the tests mirrors this surface."""

    def _api(self, *args: str) -> object:
        out = subprocess.run(["gh", "api", *args], check=True, capture_output=True, text=True).stdout
        return json.loads(out) if out.strip() else None

    @staticmethod
    def _issue(raw: dict) -> dict:
        return {
            "number": raw["number"],
            "state": raw["state"].upper(),
            "title": raw["title"],
            "labels": [label["name"] for label in raw["labels"]],
        }

    def issue(self, number: int) -> dict:
        return self._issue(self._api(f"repos/{REPO}/issues/{number}"))

    def open_wip(self) -> list[dict]:
        raw = [
            item
            for name in ("in-progress", "status:in-progress")
            for item in self._api(f"repos/{REPO}/issues?state=open&labels={name}&per_page=100")
        ]
        return [self._issue(item) for item in raw if "pull_request" not in item]

    def comments(self, number: int) -> list[dict]:
        return self._api("--paginate", f"repos/{REPO}/issues/{number}/comments?per_page=100")

    def comment(self, number: int, body: str) -> int:
        return self._api("-X", "POST", f"repos/{REPO}/issues/{number}/comments", "-f", f"body={body}")["id"]

    def delete_comment(self, comment_id: int) -> None:
        self._api("-X", "DELETE", f"repos/{REPO}/issues/comments/{comment_id}")

    def add_label(self, number: int, label: str) -> None:
        self._api("-X", "POST", f"repos/{REPO}/issues/{number}/labels", "-f", f"labels[]={label}")

    def remove_label(self, number: int, label: str) -> None:
        self._api("-X", "DELETE", f"repos/{REPO}/issues/{number}/labels/{quote(label, safe='')}")

    def repo_labels(self) -> set[str]:
        return {label["name"] for label in self._api("--paginate", f"repos/{REPO}/labels?per_page=100")}

    def open_prs_closing(self, number: int) -> int:
        found = self._api("search/issues", "-f", f"q=repo:{REPO} is:pr is:open {number} in:body")
        return int(found["total_count"])

    def set_status(self, number: int, status: str) -> bool:
        query = (
            'query($n:Int!){repository(owner:"matiaspakua",name:"notaire"){issue(number:$n)'
            "{id projectItems(first:10){nodes{id project{id}}}}}}"
        )
        try:
            data = self._api("graphql", "-f", f"query={query}", "-F", f"n={number}")["data"]["repository"]["issue"]
            item = next((n["id"] for n in data["projectItems"]["nodes"] if n["project"]["id"] == PROJECT_ID), None)
            if item is None:
                return False
            mutation = (
                "mutation($p:ID!,$i:ID!,$f:ID!,$o:String!){updateProjectV2ItemFieldValue(input:"
                "{projectId:$p,itemId:$i,fieldId:$f,value:{singleSelectOptionId:$o}}){projectV2Item{id}}}"
            )
            self._api(
                "graphql", "-f", f"query={mutation}", "-f", f"p={PROJECT_ID}", "-f", f"i={item}",
                "-f", f"f={STATUS_FIELD}", "-f", f"o={STATUS_OPTIONS[status]}",
            )
            return True
        except subprocess.CalledProcessError:
            return False


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("issue", nargs="?", type=int)
    parser.add_argument("--agent")
    parser.add_argument("--settle", type=float, default=15.0, help="seconds to wait before re-reading claims")
    parser.add_argument("--release-stale", action="store_true")
    parser.add_argument("--hours", type=float, default=24.0)
    args = parser.parse_args(argv)
    if args.release_stale:
        return release_stale(Gh(), args.hours)
    if args.issue is None or not args.agent:
        parser.error("<issue> and --agent are required unless --release-stale")
    return claim(Gh(), args.issue, args.agent, settle=args.settle)


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
