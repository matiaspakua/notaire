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
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from milestone_policy import (  # noqa: E402
    DELIVERY_FILTER,
    FULL_LIFECYCLE_FILTER,
    HISTORY_ERAS,
    MILESTONE_DESCRIPTION,
    MILESTONE_DUE,
    MILESTONE_WINDOW,
    RENAMES,
    SCHEDULED,
    STRIP_USE_CASE_LABEL,
    VIEW_CONTRACTS,
    VIEW_FIELDS,
    classify_closed,
    classify_open,
    lane,
    suggested_priority,
)

REPO = "matiaspakua/notaire"
OWNER = "matiaspakua"
PROJECT_ID = "PVT_kwHOATJKDs4BRC9u"
SNAPSHOT = Path(__file__).resolve().parents[2] / "docs" / "github" / "milestone-snapshot.json"
PAGE_SNAPSHOT = Path(__file__).resolve().parents[2] / "github-page" / "lib" / "milestone-snapshot.json"

CLOSE_WHEN_EMPTY = set(RENAMES.values()) | set(HISTORY_ERAS)
VIEW_ALIASES = {
    "Agent queue": "Delivery schedule",
    "Needs triage": "Triage",
    "Todo by priority": "Backlog by priority",
}


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
            id number state title closedAt
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


def missing_project_issues(issues: list[dict], items: dict[int, str]) -> list[dict]:
    return [issue for issue in issues if issue["number"] not in items]


def ensure_project_items(gh: GitHub, issues: list[dict], items: dict[int, str]) -> dict[int, str]:
    mutation = """
    mutation($project: ID!, $content: ID!) {
      addProjectV2ItemById(input: {projectId: $project, contentId: $content}) {
        item { id }
      }
    }
    """
    for issue in missing_project_issues(issues, items):
        added = gh.graphql(mutation, {"project": PROJECT_ID, "content": issue["id"]})
        items[issue["number"]] = added["addProjectV2ItemById"]["item"]["id"]
        print(f"added issue #{issue['number']} to project")
    return items


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
        labels = [node["name"] for node in issue["labels"]["nodes"]]
        if issue["state"] == "OPEN" and lane(labels, issue["number"], issue["title"]) == "catalog":
            continue
        milestone = issue.get("milestone") or {}
        window = MILESTONE_WINDOW.get(milestone.get("title", ""))
        item = items.get(issue["number"])
        if window is None or item is None:
            continue
        for field, day in ((start_id, window[0]), (target_id, window[1])):
            gh.graphql(mutation, {"project": PROJECT_ID, "item": item, "field": field, "date": day})
        dated += 1
        if dated % 25 == 0:
            print(f"dated {dated}", flush=True)


def project_views(gh: GitHub) -> list[dict]:
    data = gh.graphql(
        """
        query($id: ID!) {
          node(id: $id) {
            ... on ProjectV2 {
              views(first: 30) {
                nodes {
                  id name layout filter
                  fields(first: 20) {
                    nodes { ... on ProjectV2FieldCommon { name } }
                  }
                  groupByFields(first: 5) {
                    nodes { ... on ProjectV2FieldCommon { name } }
                  }
                  verticalGroupByFields(first: 5) {
                    nodes { ... on ProjectV2FieldCommon { name } }
                  }
                }
              }
            }
          }
        }
        """,
        {"id": PROJECT_ID},
    )
    return data["node"]["views"]["nodes"]


def project_field_ids(gh: GitHub) -> dict[str, str]:
    data = gh.graphql(
        """
        query($id: ID!) {
          node(id: $id) {
            ... on ProjectV2 {
              fields(first: 40) {
                nodes { ... on ProjectV2FieldCommon { id name } }
              }
            }
          }
        }
        """,
        {"id": PROJECT_ID},
    )
    return {node["name"]: node["id"] for node in data["node"]["fields"]["nodes"] if node.get("name")}


def configure_views(gh: GitHub) -> None:
    views = project_views(gh)
    by_name = {view["name"]: view for view in views}
    fields = project_field_ids(gh)
    for wanted_name, (layout, view_filter) in VIEW_CONTRACTS.items():
        view = by_name.get(wanted_name) or by_name.get(VIEW_ALIASES.get(wanted_name, ""))
        if view is None:
            created = gh.graphql(
                """
                mutation($project: ID!, $name: String!, $layout: ProjectV2ViewLayout!) {
                  createProjectV2View(input: {projectId: $project, name: $name, layout: $layout}) {
                    projectV2View { id name layout filter }
                  }
                }
                """,
                {"project": PROJECT_ID, "name": wanted_name, "layout": layout},
            )
            view = created["createProjectV2View"]["projectV2View"]
        variables = {
            "id": view["id"],
            "name": wanted_name,
            "layout": layout,
            "filter": view_filter,
        }
        if wanted_name not in VIEW_FIELDS:
            gh.graphql(
                """
                mutation($id: ID!, $name: String!, $layout: ProjectV2ViewLayout!, $filter: String!) {
                  updateProjectV2View(input: {
                    viewId: $id, name: $name, layout: $layout, filter: $filter
                  }) { projectV2View { id } }
                }
                """,
                variables,
            )
            continue
        variables["configuration"] = {
            "visibleFieldIds": [fields[name] for name in VIEW_FIELDS[wanted_name]],
        }
        gh.graphql(
            """
            mutation(
              $id: ID!,
              $name: String!,
              $layout: ProjectV2ViewLayout!,
              $filter: String!,
              $configuration: ProjectV2ViewConfigurationInput!
            ) {
              updateProjectV2View(input: {
                viewId: $id,
                name: $name,
                layout: $layout,
                filter: $filter,
                configuration: $configuration
              }) { projectV2View { id } }
            }
            """,
            variables,
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
        "generated": date.today().isoformat(),
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
        "roadmapFilter": FULL_LIFECYCLE_FILTER,
        "deliveryFilter": DELIVERY_FILTER,
    }


def write_snapshot(payload: dict) -> None:
    text = json.dumps(payload, indent=2, ensure_ascii=False) + "\n"
    SNAPSHOT.write_text(text)
    PAGE_SNAPSHOT.write_text(text)


def check_views(gh: GitHub) -> int:
    bad = 0
    by_name = {view["name"]: view for view in project_views(gh)}
    for name, (layout, view_filter) in VIEW_CONTRACTS.items():
        view = by_name.get(name)
        if view is None:
            print(f"project view missing: {name}")
            bad += 1
            continue
        if view["layout"] != layout:
            print(f"project view {name} layout is {view['layout']!r}, wants {layout!r}")
            bad += 1
        if (view.get("filter") or "") != view_filter:
            print(f"project view {name} filter is {view.get('filter')!r}, wants {view_filter!r}")
            bad += 1
        if name in VIEW_FIELDS:
            visible = tuple(node["name"] for node in view["fields"]["nodes"])
            if visible != VIEW_FIELDS[name]:
                print(f"project view {name} fields are {visible!r}, wants {VIEW_FIELDS[name]!r}")
                bad += 1
    roadmap = by_name.get("Roadmap")
    if roadmap and [node["name"] for node in roadmap["groupByFields"]["nodes"]] != ["Milestone"]:
        print("project view Roadmap must be grouped by Milestone")
        bad += 1
    kanban = by_name.get("KANBAN")
    if kanban and [node["name"] for node in kanban["verticalGroupByFields"]["nodes"]] != ["Status"]:
        print("project view KANBAN must be grouped vertically by Status")
        bad += 1
    return bad


def check(gh: GitHub, issues: list[dict]) -> int:
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
    missing_items = missing_project_issues(issues, project_items(gh))
    for issue in missing_items:
        print(f"issue #{issue['number']} is missing from Project #1")
    bad += len(missing_items)
    bad += check_views(gh)
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
        return check(gh, issues)
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
    items = ensure_project_items(gh, issues, items)
    start_id, target_id = ensure_date_fields(gh)
    # Reload so dates follow the milestones just written.
    issues = load_issues(gh)
    milestones = ensure_milestones(gh, True)
    set_roadmap_dates(gh, issues, items, start_id, target_id)
    configure_views(gh)
    write_snapshot(snapshot(issues, milestones))
    print(f"wrote {SNAPSHOT}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
