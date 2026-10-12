#!/usr/bin/env python3
"""Apply or check the Notaire milestone schedule against GitHub.

  python3 workspace/sdlc/apply_milestone_policy.py --check
  python3 workspace/sdlc/apply_milestone_policy.py --dry-run
  python3 workspace/sdlc/apply_milestone_policy.py --apply

``--check`` is read-only and exits 1 when an issue disagrees with
``milestone_policy``. Without ``gh`` auth it exits 0 and says it skipped.
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from milestone_policy import (  # noqa: E402
    HISTORY_ERAS,
    MILESTONE_DESCRIPTION,
    MILESTONE_DUE,
    RENAMES,
    ROADMAP_WINDOW,
    SCHEDULED,
    STRIP_USE_CASE_LABEL,
    classify_closed,
    classify_open,
    lane,
    suggested_priority,
)

REPO = "matiaspakua/notaire"
OWNER = "matiaspakua"
PROJECT_ID = "PVT_kwHOATJKDs4BRC9u"
ROADMAP_VIEW_ID = "PVTV_lAHOATJKDs4BRC9uzgMBbKM"
STATUS_FIELD_ID = "PVTSSF_lAHOATJKDs4BRC9uzg-_XNo"
STATUS_IN_PROGRESS = "47fc9ee4"
THIS_ISSUE = 1561
ROADMAP_FILTER = (
    "is:open -label:requerimiento-funcional "
    "-label:requerimiento-no-funcional -label:CASO-DE-USO"
)
DELIVERY_VIEW = "Delivery schedule"
SNAPSHOT = Path(__file__).resolve().parents[2] / "docs" / "github" / "milestone-snapshot.json"
PAGE_SNAPSHOT = Path(__file__).resolve().parents[2] / "github-page" / "lib" / "milestone-snapshot.json"

CLOSE_WHEN_EMPTY = set(RENAMES.values()) | set(HISTORY_ERAS)


class GitHub:
    def __init__(self) -> None:
        self.token = subprocess.check_output(["gh", "auth", "token"], text=True).strip()

    def request(self, method: str, path: str, body: dict | None = None) -> tuple[int, dict | list | None]:
        data = None if body is None else json.dumps(body).encode()
        url = path if path.startswith("https://") else f"https://api.github.com{path}"
        req = urllib.request.Request(
            url,
            data=data,
            method=method,
            headers={
                "Authorization": f"Bearer {self.token}",
                "Accept": "application/vnd.github+json",
                "X-GitHub-Api-Version": "2022-11-28",
                "User-Agent": "notaire-milestone-policy",
                "Content-Type": "application/json",
            },
        )
        for attempt in range(6):
            try:
                with urllib.request.urlopen(req, timeout=60) as resp:
                    raw = resp.read().decode()
                    return resp.status, json.loads(raw) if raw else None
            except urllib.error.HTTPError as exc:
                detail = exc.read().decode()
                if exc.code in {403, 429, 500, 502, 503, 504} and attempt < 5:
                    time.sleep(2 ** attempt)
                    continue
                raise SystemExit(f"{method} {path} -> {exc.code} {detail[:500]}") from exc
        raise SystemExit(f"{method} {path} failed")

    def graphql(self, query: str, variables: dict | None = None) -> dict:
        status, payload = self.request("POST", "/graphql", {"query": query, "variables": variables or {}})
        if status != 200 or not isinstance(payload, dict):
            raise SystemExit(f"graphql status {status}")
        if payload.get("errors"):
            raise SystemExit(json.dumps(payload["errors"])[:800])
        return payload["data"]


def load_issues(gh: GitHub) -> list[dict]:
    query = """
    query($cursor: String) {
      repository(owner: "matiaspakua", name: "notaire") {
        issues(first: 100, after: $cursor, states: [OPEN, CLOSED],
               orderBy: {field: CREATED_AT, direction: ASC}) {
          pageInfo { hasNextPage endCursor }
          nodes {
            number state title closedAt
            milestone { number title }
            labels(first: 30) { nodes { name } }
          }
        }
      }
    }
    """
    cursor = None
    issues: list[dict] = []
    while True:
        data = gh.graphql(query, {"cursor": cursor})
        conn = data["repository"]["issues"]
        issues.extend(conn["nodes"])
        if not conn["pageInfo"]["hasNextPage"]:
            return issues
        cursor = conn["pageInfo"]["endCursor"]


def target_for(issue: dict) -> str | None:
    labels = [node["name"] for node in issue["labels"]["nodes"]]
    if issue["state"] == "OPEN":
        return classify_open(issue["number"], issue["title"], labels)
    return classify_closed(issue["closedAt"] or "", issue["milestone"] is not None)


def ensure_milestones(gh: GitHub, apply: bool) -> dict[str, dict]:
    status, rows = gh.request("GET", f"/repos/{REPO}/milestones?state=all&per_page=100")
    if status != 200 or not isinstance(rows, list):
        raise SystemExit("could not list milestones")
    by_title = {row["title"]: row for row in rows}
    for old, new in RENAMES.items():
        if old in by_title and new not in by_title:
            print(f"rename milestone {by_title[old]['number']}: {old} -> {new}")
            if apply:
                gh.request(
                    "PATCH",
                    f"/repos/{REPO}/milestones/{by_title[old]['number']}",
                    {"title": new},
                )
            by_title[new] = by_title.pop(old)
            by_title[new]["title"] = new
    wanted = list(SCHEDULED) + list(HISTORY_ERAS)
    for title in wanted:
        if title in by_title:
            continue
        print(f"create milestone {title}")
        if not apply:
            by_title[title] = {"number": -1, "title": title, "state": "open"}
            continue
        _, created = gh.request(
            "POST",
            f"/repos/{REPO}/milestones",
            {
                "title": title,
                "description": MILESTONE_DESCRIPTION[title],
                "due_on": MILESTONE_DUE[title],
                "state": "open",
            },
        )
        if not isinstance(created, dict):
            raise SystemExit(f"create failed for {title}")
        by_title[title] = created
    return by_title


def assign_issues(gh: GitHub, issues: list[dict], milestones: dict[str, dict], apply: bool) -> Counter:
    moves: Counter = Counter()
    done = 0
    for issue in issues:
        title = target_for(issue)
        if title is None:
            continue
        current = issue["milestone"]["title"] if issue["milestone"] else None
        if current == title:
            continue
        number = milestones[title]["number"]
        moves[f"{issue['state']}:{current or '(none)'} -> {title}"] += 1
        if apply:
            if number < 0:
                raise SystemExit("dry-run milestone number used during apply")
            gh.request("PATCH", f"/repos/{REPO}/issues/{issue['number']}", {"milestone": number})
            done += 1
            if done % 50 == 0:
                print(f"assigned {done}", flush=True)
    return moves


def fix_labels(gh: GitHub, issues: list[dict], apply: bool) -> None:
    gh.request(
        "PATCH",
        f"/repos/{REPO}/labels/{urllib.parse.quote('FRONTEND')}",
        {"description": "Next.js web client: pages, forms, and dashboard UI"},
    ) if apply else print("would update FRONTEND label description")
    for name in (
        "phase:1-analysis",
        "phase:2-foundation",
        "phase:3-backend-api",
        "phase:4-frontend",
        "phase:5-observability",
        "phase:6-deprecation",
    ):
        if apply:
            gh.request(
                "PATCH",
                f"/repos/{REPO}/labels/{urllib.parse.quote(name)}",
                {"description": "Historical phase alias. Schedule work with a milestone, not this label."},
            )
    for issue in issues:
        labels = [node["name"] for node in issue["labels"]["nodes"]]
        number = issue["number"]
        if number in STRIP_USE_CASE_LABEL and "CASO-DE-USO" in labels:
            print(f"remove CASO-DE-USO from #{number}")
            if apply:
                gh.request("DELETE", f"/repos/{REPO}/issues/{number}/labels/{urllib.parse.quote('CASO-DE-USO')}")
            labels = [name for name in labels if name != "CASO-DE-USO"]
        if "documentation" in labels:
            print(f"replace documentation with DOC on #{number}")
            if apply:
                gh.request("POST", f"/repos/{REPO}/issues/{number}/labels", {"labels": ["DOC"]})
                gh.request("DELETE", f"/repos/{REPO}/issues/{number}/labels/{urllib.parse.quote('documentation')}")
            labels = ["DOC" if name == "documentation" else name for name in labels]
        if issue["state"] != "OPEN":
            continue
        milestone = target_for(issue)
        if milestone is None:
            continue
        wanted = suggested_priority(number, issue["title"], labels, milestone)
        if wanted and wanted not in labels:
            print(f"add {wanted} on #{number}")
            if apply:
                gh.request("POST", f"/repos/{REPO}/issues/{number}/labels", {"labels": [wanted]})
    if apply:
        gh.request("POST", f"/repos/{REPO}/issues/{THIS_ISSUE}/labels", {"labels": ["in-progress"]})


def close_empty_history(gh: GitHub, apply: bool) -> None:
    _, rows = gh.request("GET", f"/repos/{REPO}/milestones?state=all&per_page=100")
    if not isinstance(rows, list):
        return
    for row in rows:
        if row["title"] not in CLOSE_WHEN_EMPTY:
            continue
        if row["open_issues"] != 0 or row["state"] == "closed":
            continue
        print(f"close milestone {row['title']}")
        if apply:
            gh.request("PATCH", f"/repos/{REPO}/milestones/{row['number']}", {"state": "closed"})


def project_items(gh: GitHub) -> dict[int, str]:
    query = """
    query($cursor: String) {
      node(id: "%s") {
        ... on ProjectV2 {
          items(first: 100, after: $cursor) {
            pageInfo { hasNextPage endCursor }
            nodes { id content { ... on Issue { number } } }
          }
        }
      }
    }
    """ % PROJECT_ID
    cursor = None
    found: dict[int, str] = {}
    while True:
        data = gh.graphql(query, {"cursor": cursor})
        conn = data["node"]["items"]
        for node in conn["nodes"]:
            content = node.get("content") or {}
            if "number" in content:
                found[content["number"]] = node["id"]
        if not conn["pageInfo"]["hasNextPage"]:
            return found
        cursor = conn["pageInfo"]["endCursor"]


def ensure_date_fields(gh: GitHub) -> tuple[str, str]:
    data = gh.graphql(
        """
        query($id: ID!) {
          node(id: $id) {
            ... on ProjectV2 {
              fields(first: 40) { nodes { ... on ProjectV2FieldCommon { id name } } }
            }
          }
        }
        """,
        {"id": PROJECT_ID},
    )
    by_name = {node["name"]: node["id"] for node in data["node"]["fields"]["nodes"] if node.get("name")}
    ids = []
    for name in ("Start", "Target"):
        if name in by_name:
            ids.append(by_name[name])
            continue
        created = gh.graphql(
            """
            mutation($id: ID!, $name: String!) {
              createProjectV2Field(input: {projectId: $id, dataType: DATE, name: $name}) {
                projectV2Field { ... on ProjectV2FieldCommon { id } }
              }
            }
            """,
            {"id": PROJECT_ID, "name": name},
        )
        ids.append(created["createProjectV2Field"]["projectV2Field"]["id"])
    return ids[0], ids[1]


def set_roadmap_dates(gh: GitHub, issues: list[dict], items: dict[int, str], start_id: str, target_id: str) -> None:
    mutation = """
    mutation($project: ID!, $item: ID!, $field: ID!, $date: Date!) {
      updateProjectV2ItemFieldValue(input: {
        projectId: $project, itemId: $item, fieldId: $field, value: {date: $date}
      }) { projectV2Item { id } }
    }
    """
    dated = 0
    for issue in issues:
        if issue["state"] != "OPEN":
            continue
        labels = [node["name"] for node in issue["labels"]["nodes"]]
        if lane(labels, issue["number"], issue["title"]) == "catalog":
            continue
        window = ROADMAP_WINDOW.get(target_for(issue) or "")
        item = items.get(issue["number"])
        if window is None or item is None:
            continue
        for field, day in ((start_id, window[0]), (target_id, window[1])):
            gh.graphql(mutation, {"project": PROJECT_ID, "item": item, "field": field, "date": day})
        dated += 1
        if dated % 25 == 0:
            print(f"dated {dated}", flush=True)


def configure_views(gh: GitHub) -> None:
    gh.graphql(
        """
        mutation($id: ID!, $filter: String!) {
          updateProjectV2View(input: {viewId: $id, name: "Roadmap", filter: $filter}) {
            projectV2View { id }
          }
        }
        """,
        {"id": ROADMAP_VIEW_ID, "filter": ROADMAP_FILTER},
    )
    data = gh.graphql(
        """
        query($id: ID!) {
          node(id: $id) { ... on ProjectV2 { views(first: 30) { nodes { name id } } } }
        }
        """,
        {"id": PROJECT_ID},
    )
    names = {node["name"] for node in data["node"]["views"]["nodes"]}
    if DELIVERY_VIEW in names:
        return
    gh.graphql(
        """
        mutation($id: ID!, $name: String!) {
          createProjectV2View(input: {projectId: $id, name: $name, layout: TABLE_LAYOUT}) {
            projectV2View { id }
          }
        }
        """,
        {"id": PROJECT_ID, "name": DELIVERY_VIEW},
    )
    data = gh.graphql(
        """
        query($id: ID!) {
          node(id: $id) { ... on ProjectV2 { views(first: 30) { nodes { name id } } } }
        }
        """,
        {"id": PROJECT_ID},
    )
    view_id = next(node["id"] for node in data["node"]["views"]["nodes"] if node["name"] == DELIVERY_VIEW)
    gh.graphql(
        """
        mutation($id: ID!, $filter: String!) {
          updateProjectV2View(input: {viewId: $id, filter: $filter}) { projectV2View { id } }
        }
        """,
        {"id": view_id, "filter": ROADMAP_FILTER},
    )


def mark_this_issue(gh: GitHub, items: dict[int, str]) -> None:
    item = items.get(THIS_ISSUE)
    if item is None:
        issue = gh.graphql(
            """
            query { repository(owner: "matiaspakua", name: "notaire") {
              issue(number: %d) { id } } }
            """
            % THIS_ISSUE
        )
        added = gh.graphql(
            """
            mutation($project: ID!, $content: ID!) {
              addProjectV2ItemById(input: {projectId: $project, contentId: $content}) {
                item { id }
              }
            }
            """,
            {"project": PROJECT_ID, "content": issue["repository"]["issue"]["id"]},
        )
        item = added["addProjectV2ItemById"]["item"]["id"]
    gh.graphql(
        """
        mutation($project: ID!, $item: ID!, $field: ID!, $option: String!) {
          updateProjectV2ItemFieldValue(input: {
            projectId: $project, itemId: $item, fieldId: $field,
            value: {singleSelectOptionId: $option}
          }) { projectV2Item { id } }
        }
        """,
        {
            "project": PROJECT_ID,
            "item": item,
            "field": STATUS_FIELD_ID,
            "option": STATUS_IN_PROGRESS,
        },
    )


def snapshot(issues: list[dict], milestones: dict[str, dict]) -> dict:
    open_counts: Counter = Counter()
    closed_counts: Counter = Counter()
    lanes: Counter = Counter()
    for issue in issues:
        labels = [node["name"] for node in issue["labels"]["nodes"]]
        title = (issue["milestone"]["title"] if issue["milestone"] else None) or "(none)"
        if issue["state"] == "OPEN":
            open_counts[title] += 1
            lanes[lane(labels, issue["number"], issue["title"])] += 1
        else:
            closed_counts[title] += 1
    numbers = {title: row.get("number") for title, row in milestones.items()}
    return {
        "generated": "2026-10-12",
        "open": open_counts.total() if hasattr(open_counts, "total") else sum(open_counts.values()),
        "closed": closed_counts.total() if hasattr(closed_counts, "total") else sum(closed_counts.values()),
        "plannedOpen": dict(open_counts.most_common()),
        "plannedClosed": dict(closed_counts.most_common()),
        "openByMilestone": dict(open_counts.most_common()),
        "closedByMilestone": dict(closed_counts.most_common()),
        "plannedLanes": dict(lanes.most_common()),
        "openByLane": dict(lanes.most_common()),
        "milestoneNumbers": numbers,
        "board": "https://github.com/users/matiaspakua/projects/1",
        "roadmapFilter": ROADMAP_FILTER,
    }


def write_snapshot(payload: dict) -> None:
    text = json.dumps(payload, indent=2, ensure_ascii=False) + "\n"
    SNAPSHOT.write_text(text)
    PAGE_SNAPSHOT.write_text(text)


def check(issues: list[dict]) -> int:
    bad = 0
    for issue in issues:
        title = target_for(issue)
        current = issue["milestone"]["title"] if issue["milestone"] else None
        if issue["state"] == "OPEN":
            if current != title:
                bad += 1
                if bad <= 15:
                    print(f"open #{issue['number']} has {current!r}, policy wants {title}")
        elif title is not None and current != title:
            bad += 1
            if bad <= 15:
                print(f"closed #{issue['number']} has {current!r}, policy wants {title}")
    print(f"mismatches: {bad}")
    return 1 if bad else 0


def main() -> int:
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true")
    mode.add_argument("--dry-run", action="store_true")
    mode.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    try:
        gh = GitHub()
    except subprocess.CalledProcessError:
        print("milestone policy check skipped: gh auth token unavailable")
        return 0
    issues = load_issues(gh)
    if args.check:
        return check(issues)
    apply = args.apply
    milestones = ensure_milestones(gh, apply)
    moves = assign_issues(gh, issues, milestones, apply)
    for key, count in moves.most_common():
        print(f"{count:4}  {key}")
    print(f"moves: {sum(moves.values())}")
    fix_labels(gh, issues, apply)
    if not apply:
        return 0
    close_empty_history(gh, True)
    items = project_items(gh)
    start_id, target_id = ensure_date_fields(gh)
    # Reload so dates follow the milestones just written.
    issues = load_issues(gh)
    milestones = ensure_milestones(gh, True)
    set_roadmap_dates(gh, issues, items, start_id, target_id)
    configure_views(gh)
    mark_this_issue(gh, items if THIS_ISSUE in items else project_items(gh))
    write_snapshot(snapshot(issues, milestones))
    print(f"wrote {SNAPSHOT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
