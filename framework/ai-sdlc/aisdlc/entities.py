"""Factory entity toolset: list, visualize, add, edit, delete, apply, evolve.

Entities are the catalog revisions (skills, agents, rules, MCP, prompts, hooks,
and any later kind in ``REGISTRY``). The kit stores them as data. It does not
import a harness. Project entries override factory defaults with the same id.
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

from aisdlc import catalogs
from aisdlc.context import Context
from aisdlc.errors import KitError

REGISTRY: dict[str, dict] = {
    "skills": {"required": ("summary",), "relations": ("skills",)},
    "agents": {"required": ("summary",), "relations": ("agents",)},
    "rules": {"required": ("summary",), "relations": ()},
    "mcp": {"required": ("summary",), "relations": ("mcp",)},
    "prompts": {"required": ("summary",), "relations": ()},
    "hooks": {"required": ("summary",), "relations": ()},
    "memory": {"required": ("summary",), "relations": ()},
    "workflows": {"required": ("summary",), "relations": ()},
    "providers": {"required": ("summary",), "relations": ()},
    "integrations": {"required": ("summary",), "relations": ()},
}
ALLOWED = {
    "id", "kind", "summary", "ref", "scope", "harness", "version", "digest",
    "schema_version", "created_at", "created_by", "evolution", "command", "args",
    "body", "governance", "source",
}
DEFAULT_TARGETS = {"cursor": ".cursor/mcp.json", "claude": ".mcp.json"}
DEFAULTS = Path(__file__).resolve().parents[1] / "entities"


def kinds() -> tuple[str, ...]:
    return tuple(REGISTRY)


def _harness_list(value: object) -> list[str]:
    if value is None or value == "" or value == "*":
        return ["*"]
    if isinstance(value, str):
        return [part.strip() for part in value.split(",") if part.strip()]
    if isinstance(value, list) and all(isinstance(item, str) for item in value):
        return [item.strip() for item in value if item.strip()]
    raise KitError("invalid_entity", "harness must be a string or a list of strings")


def validate_fields(kind: str, payload: dict) -> None:
    if kind not in REGISTRY:
        raise KitError("unknown_kind", f"unknown entity kind {kind!r} (expected {', '.join(kinds())})")
    unknown = sorted(set(payload) - ALLOWED)
    if unknown:
        raise KitError("invalid_entity", f"unknown field {unknown[0]} on {kind}")
    for key in REGISTRY[kind]["required"]:
        if not str(payload.get(key) or "").strip():
            raise KitError("invalid_entity", f"{kind} requires {key}")
    if "scope" in payload and payload["scope"] not in {"project", "factory"}:
        raise KitError("invalid_entity", "scope must be project or factory")
    if "args" in payload and not isinstance(payload["args"], list):
        raise KitError("invalid_entity", "args must be a list of strings")
    _harness_list(payload.get("harness"))


def _defaults() -> list[dict]:
    rows = []
    if not DEFAULTS.is_dir():
        return rows
    for path in sorted(DEFAULTS.rglob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        data.setdefault("scope", "factory")
        data.setdefault("version", 1)
        data["source"] = "factory"
        rows.append(data)
    return rows


def _project_rows(ctx: Context) -> list[dict]:
    if not ctx.profile.catalogs_dir:
        return []
    listed = catalogs.list_entries(ctx)
    rows = []
    for block in listed.values():
        for entry in block["entries"]:
            if entry.get("error"):
                continue
            item = dict(entry)
            item["source"] = "project"
            item.setdefault("scope", "project")
            rows.append(item)
    return rows


def list_entities(
    ctx: Context,
    *,
    kind: str | None = None,
    scope: str | None = None,
    harness: str | None = None,
    version: int | None = None,
) -> list[dict]:
    if kind and kind not in REGISTRY:
        raise KitError("unknown_kind", f"unknown entity kind {kind!r}")
    merged: dict[tuple[str, str], dict] = {}
    for row in _defaults():
        merged[(row["kind"], row["id"])] = row
    for row in _project_rows(ctx):
        key = (row["kind"], row["id"])
        if key in merged:
            row = {**row, "overrides": "factory"}
        merged[key] = row
    rows = list(merged.values())
    if kind:
        rows = [row for row in rows if row["kind"] == kind]
    if scope:
        rows = [row for row in rows if row.get("scope", "project") == scope]
    if harness:
        rows = [row for row in rows if _matches_harness(row, harness)]
    if version is not None:
        rows = [row for row in rows if int(row.get("version") or 1) == int(version)]
    return sorted(rows, key=lambda row: (row["kind"], row["id"]))


def _matches_harness(row: dict, harness: str) -> bool:
    values = _harness_list(row.get("harness"))
    return "*" in values or harness in values


def _references(ctx: Context, kind: str, entry_id: str) -> list[str]:
    hits: list[str] = []
    relation = REGISTRY.get(kind, {}).get("relations", ())
    for stage in ctx.profile.stage_profiles:
        named = {
            "agents": stage.agents,
            "skills": stage.skills,
            "mcp": stage.mcp,
        }
        for field in relation:
            if entry_id in named.get(field, ()):
                hits.append(f"stage:{stage.id}")
    if ctx.profile.catalogs_dir:
        folder = ctx.path(ctx.profile.catalogs_dir) / "workflows"
        if folder.is_dir():
            for path in sorted(folder.glob("*/current.json")):
                data = json.loads(path.read_text(encoding="utf-8"))
                if _workflow_mentions(data, kind, entry_id):
                    hits.append(f"workflow:{data.get('id', path.parent.name)}")
    return hits


def _workflow_mentions(data: dict, kind: str, entry_id: str) -> bool:
    body = data.get("body") if isinstance(data.get("body"), dict) else data
    for key in ("agents", "skills", "mcp"):
        if kind == key and entry_id in (body.get(key) or []):
            return True
    for step in body.get("steps") or []:
        if not isinstance(step, dict):
            continue
        if kind == "agents" and step.get("agent") in {entry_id, f"agents:{entry_id}"}:
            return True
        if kind == "skills" and entry_id in (step.get("skills") or []):
            return True
        if kind == "mcp" and entry_id in (step.get("mcp") or []):
            return True
    return False


def visualize(ctx: Context, *, kind: str | None = None, entry_id: str | None = None) -> dict:
    rows = list_entities(ctx, kind=kind)
    if entry_id:
        rows = [row for row in rows if row["id"] == entry_id]
    nodes = []
    edges = []
    for row in rows:
        nodes.append({
            "id": f"{row['kind']}:{row['id']}",
            "kind": row["kind"],
            "version": row.get("version", 1),
            "scope": row.get("scope", "project"),
            "source": row.get("source"),
            "drift": "override" if row.get("overrides") else "in_sync",
        })
    for stage in ctx.profile.stage_profiles:
        nodes.append({"id": f"stage:{stage.id}", "kind": "stage", "version": None, "drift": "n/a"})
        linked = {"agents": stage.agents, "skills": stage.skills, "mcp": stage.mcp}
        for field, values in linked.items():
            for value in values:
                if any(row["kind"] == field and row["id"] == value for row in rows):
                    edges.append({"from": f"stage:{stage.id}", "to": f"{field}:{value}", "rel": "uses"})
    drift = apply_entities(ctx, dry_run=True)
    return {
        "nodes": nodes,
        "edges": edges,
        "apply_drift": drift["drift"],
        "mermaid": _mermaid(nodes, edges),
    }


def _mermaid(nodes: list[dict], edges: list[dict]) -> str:
    lines = ["flowchart LR"]
    for node in nodes:
        token = node["id"].replace(":", "_").replace("-", "_")
        lines.append(f"  {token}[\"{node['id']}\"]")
    for edge in edges:
        lines.append(
            f"  {edge['from'].replace(':', '_').replace('-', '_')} --> {edge['to'].replace(':', '_').replace('-', '_')}"
        )
    return "\n".join(lines)


def _comparable(payload: dict) -> dict:
    return {key: payload.get(key) for key in ("summary", "ref", "scope", "harness", "command", "args", "body")}


def add_entity(ctx: Context, kind: str, entry_id: str, payload: dict, *, actor: str, dry_run: bool = False) -> dict:
    body = {**payload, "id": entry_id, "kind": kind, "scope": payload.get("scope") or "project"}
    validate_fields(kind, body)
    if ctx.profile.catalogs_dir and catalogs._exists(ctx, kind, entry_id):
        current = catalogs.load_entry(ctx, kind, entry_id)
        if _comparable(current) == _comparable(body):
            return {**current, "idempotent": True, "dry_run": dry_run}
        raise KitError("already_exists", f"entity {kind}/{entry_id} exists and differs (use edit)")
    if dry_run:
        return {**body, "version": 1, "dry_run": True, "idempotent": False}
    written = catalogs.add(
        ctx, kind, entry_id, ref=body.get("ref"), summary=body.get("summary"), extra=body, actor=actor,
    )
    return {**written, "idempotent": False, "dry_run": False}


def edit_entity(
    ctx: Context,
    kind: str,
    entry_id: str,
    payload: dict,
    *,
    actor: str,
    expected_version: int | None = None,
    dry_run: bool = False,
) -> dict:
    current = catalogs.load_entry(ctx, kind, entry_id)
    body = {**current, **payload, "id": entry_id, "kind": kind}
    validate_fields(kind, body)
    if dry_run:
        return {**body, "version": int(current.get("version") or 1) + 1, "dry_run": True}
    written = catalogs.write_new_revision(
        ctx, kind, entry_id, body, expected_version=expected_version, actor=actor,
    )
    return {**written, "dry_run": False}


def delete_entity(ctx: Context, kind: str, entry_id: str, *, force: bool = False, dry_run: bool = False) -> dict:
    if kind not in REGISTRY:
        raise KitError("unknown_kind", f"unknown entity kind {kind!r}")
    if not catalogs._exists(ctx, kind, entry_id):
        raise KitError("not_found", f"entity {kind}/{entry_id} not found")
    refs = _references(ctx, kind, entry_id)
    if refs and not force:
        raise KitError("in_use", f"{kind}/{entry_id} is referenced by {', '.join(refs)}")
    if dry_run:
        return {"kind": kind, "id": entry_id, "references": refs, "dry_run": True, "deleted": False}
    current = catalogs._current_path(ctx, kind, entry_id)
    legacy = catalogs._legacy_path(ctx, kind, entry_id)
    entity = catalogs._entity_dir(ctx, kind, entry_id)
    if not current.is_file() and not legacy.is_file() and not entity.is_dir():
        raise KitError("not_found", f"entity {kind}/{entry_id} not found")
    if legacy.is_file():
        legacy.unlink()
    if entity.is_dir():
        shutil.rmtree(entity)
    return {"kind": kind, "id": entry_id, "references": refs, "dry_run": False, "deleted": True}


def evolve_entity(
    ctx: Context,
    kind: str,
    entry_id: str,
    *,
    actor: str,
    rationale: str = "",
    summary: str | None = None,
    expected_version: int | None = None,
    rollback_to: int | None = None,
    dry_run: bool = False,
) -> dict:
    current = catalogs.load_entry(ctx, kind, entry_id)
    prior = int(current.get("version") or 1)
    if rollback_to is not None:
        source = catalogs.load_revision(ctx, kind, entry_id, rollback_to)
        rationale = rationale or f"rollback to version {rollback_to}"
        body = {**source, "id": entry_id, "kind": kind}
    else:
        if not rationale.strip():
            raise KitError("invalid_entity", "evolve requires --rationale")
        body = dict(current)
        if summary is not None:
            body["summary"] = summary
    body["evolution"] = {"rationale": rationale, "from_version": prior, "rollback_to": rollback_to}
    validate_fields(kind, body)
    if dry_run:
        return {**body, "version": prior + 1, "dry_run": True}
    written = catalogs.write_new_revision(
        ctx, kind, entry_id, body, expected_version=expected_version, actor=actor,
    )
    return {**written, "dry_run": False}


def _targets(ctx: Context, harness: str | None) -> dict[str, str]:
    selected = {harness} if harness else set(ctx.profile.entities.harnesses)
    targets = dict(DEFAULT_TARGETS)
    targets.update(dict(ctx.profile.entities.targets))
    return {name: path for name, path in targets.items() if name in selected}


def _mcp_document(ctx: Context, harness: str) -> dict:
    servers = {}
    for row in list_entities(ctx, kind="mcp", harness=harness):
        if row.get("source") == "factory" and any(
            item["id"] == row["id"] and item.get("source") == "project" for item in list_entities(ctx, kind="mcp")
        ):
            continue
        command = row.get("command") or "repolens"
        args = row.get("args") or ["mcp"]
        servers[row["id"]] = {"command": command, "args": args}
    return {"mcpServers": servers}


def apply_entities(ctx: Context, *, harness: str | None = None, dry_run: bool = False) -> dict:
    files = []
    drift = []
    for name, relative in _targets(ctx, harness).items():
        path = ctx.root / relative
        desired = _mcp_document(ctx, name)
        current = {}
        if path.is_file():
            try:
                current = json.loads(path.read_text(encoding="utf-8") or "{}")
            except json.JSONDecodeError as exc:
                raise KitError("invalid_entity", f"{relative} is not JSON") from exc
        merged = dict(current)
        servers = dict(merged.get("mcpServers") or {})
        servers.update(desired["mcpServers"])
        # Drop servers we own that disappeared. Owned ids are the entity ids we render.
        owned = set(desired["mcpServers"])
        previous_owned = set(current.get("mcpServers") or {}) & owned
        for gone in previous_owned - owned:
            servers.pop(gone, None)
        merged["mcpServers"] = servers
        changed = merged != current
        files.append({"path": relative, "harness": name, "changed": changed})
        if changed:
            drift.append(relative)
        if changed and not dry_run:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(merged, indent=2) + "\n", encoding="utf-8")
    state_path = ctx.root / ".aisdlc" / "applied" / "state.json"
    stable = [{"harness": item["harness"], "path": item["path"]} for item in files]
    manifest = {
        "files": stable,
        "entities": [
            {"kind": row["kind"], "id": row["id"], "version": row.get("version")}
            for row in list_entities(ctx)
        ],
    }
    previous = {}
    if state_path.is_file():
        previous = json.loads(state_path.read_text(encoding="utf-8"))
    if previous.get("manifest") != manifest:
        drift.append(".aisdlc/applied/state.json")
    if not dry_run:
        state_path.parent.mkdir(parents=True, exist_ok=True)
        state_path.write_text(json.dumps({"manifest": manifest}, indent=2) + "\n", encoding="utf-8")
    return {"dry_run": dry_run, "drift": drift, "files": files, "in_sync": not drift}
