#!/usr/bin/env python3
"""Set Project Status to Done for CLOSED issues that are not Done (#1878).

  python3 workspace/sdlc/sync_closed_status_done.py          # preview (default)
  python3 workspace/sdlc/sync_closed_status_done.py --apply  # mutate Project #1
"""

from __future__ import annotations

import argparse
import importlib.util
import json
import os
import subprocess
import sys
from pathlib import Path


def _load_coherence():
    path = Path(__file__).with_name("check-board-status-label-coherence.py")
    spec = importlib.util.spec_from_file_location("board_coherence_sync", path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = mod
    spec.loader.exec_module(mod)
    return mod


coherence = _load_coherence()

OWNER = os.environ.get("BOARD_COHERENCE_OWNER", "matiaspakua")
PROJECT = int(os.environ.get("BOARD_COHERENCE_PROJECT", "1"))
PROJECT_NODE_ID = os.environ.get("BOARD_COHERENCE_PROJECT_ID", "PVT_kwHOATJKDs4BRC9u")


def plan_sync(items: list) -> list:
    """CLOSED issues whose Project Status is not Done."""
    return [
        item
        for item in items
        if item.content_type == "Issue"
        and (item.state or "").upper() == "CLOSED"
        and item.status != coherence.STATUS_DONE
    ]


def status_field_and_done_option(project_id: str) -> tuple[str, str]:
    query = """
    query($id: ID!) {
      node(id: $id) {
        ... on ProjectV2 {
          field(name: "Status") {
            ... on ProjectV2SingleSelectField {
              id
              options { id name }
            }
          }
        }
      }
    }
    """
    payload = {"query": query, "variables": {"id": project_id}}
    proc = subprocess.run(
        ["gh", "api", "graphql", "--input", "-"],
        input=json.dumps(payload),
        check=False,
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or "status field lookup failed")
    data = json.loads(proc.stdout or "{}")
    field = (((data.get("data") or {}).get("node") or {}).get("field")) or {}
    field_id = field.get("id")
    done_id = next(
        (opt.get("id") for opt in field.get("options") or [] if opt.get("name") == coherence.STATUS_DONE),
        None,
    )
    if not field_id or not done_id:
        raise RuntimeError("could not resolve Status / Done option ids")
    return field_id, done_id


def set_status_done(item_id: str, field_id: str, option_id: str, project_id: str) -> None:
    mutation = """
    mutation($project: ID!, $item: ID!, $field: ID!, $option: String!) {
      updateProjectV2ItemFieldValue(input: {
        projectId: $project, itemId: $item, fieldId: $field,
        value: { singleSelectOptionId: $option }
      }) { projectV2Item { id } }
    }
    """
    payload = {
        "query": mutation,
        "variables": {
            "project": project_id,
            "item": item_id,
            "field": field_id,
            "option": option_id,
        },
    }
    proc = subprocess.run(
        ["gh", "api", "graphql", "--input", "-"],
        input=json.dumps(payload),
        check=False,
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or f"set Done failed for {item_id}")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="mutate Project Status")
    args = parser.parse_args(argv)
    if not coherence.gh_available():
        print("sync closed Status Done skipped: gh auth unavailable")
        return 0
    rows = coherence.fetch_project_items_graphql(owner=OWNER, project=PROJECT)
    items = coherence.board_items_from_graphql_rows(rows)
    todo = plan_sync(items)
    print(f"closed issues not Done: {len(todo)}")
    for item in todo:
        print(f"  #{item.number} status={item.status!r} -> Done")
    if not args.apply or not todo:
        return 0
    field_id, option_id = status_field_and_done_option(PROJECT_NODE_ID)
    id_by_number: dict[int, str] = {}
    for row in coherence.fetch_project_items(owner=OWNER, project=PROJECT):
        content = row.get("content") or {}
        number = content.get("number")
        if number and row.get("id"):
            id_by_number[int(number)] = row["id"]
    for item in todo:
        item_id = id_by_number.get(item.number)
        if not item_id:
            print(f"  ! skip #{item.number}: no project item id", file=sys.stderr)
            continue
        set_status_done(item_id, field_id, option_id, PROJECT_NODE_ID)
        print(f"  set #{item.number} -> Done")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
