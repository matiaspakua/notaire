"""Idempotent catalog schema migration (#2254): workflow body v1 → v2."""
from __future__ import annotations

from aisdlc import catalogs, evidence, workflows
from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError

WORKFLOW_SCHEMA_V2 = 2


def _is_v1_workflow(data: dict) -> bool:
    if int(data.get("schema_version") or 1) >= WORKFLOW_SCHEMA_V2:
        return False
    body = data.get("body") or {}
    if not isinstance(body, dict):
        return True
    if isinstance(body.get("steps"), list):
        return False
    return True


def _pin(ctx: Context, kind: str, entry_id: str) -> str:
    entry = catalogs.load_entry(ctx, kind, entry_id)
    return f"{kind}:{entry_id}@{int(entry.get('version') or 1)}"


def workflow_body_v1_to_v2(ctx: Context, body: dict) -> dict:
    body = body if isinstance(body, dict) else {}
    agents = [x for x in (body.get("agents") or []) if x]
    skills = [x for x in (body.get("skills") or []) if x]
    mcp = [x for x in (body.get("mcp") or []) if x]
    providers = [x for x in (body.get("providers") or []) if x]
    step = {
        "id": "main",
        "stage": "implement",
        "agent": _pin(ctx, "agents", agents[0]) if agents else None,
        "skills": [_pin(ctx, "skills", sid) for sid in skills],
        "mcp": [_pin(ctx, "mcp", mid) for mid in mcp],
        "provider": _pin(ctx, "providers", providers[0]) if providers else None,
        "gates": [],
        "budget": {"tokens": None, "time_seconds": None, "usd": None},
        "on_fail": "stop",
    }
    return {"steps": [step], "inputs": [], "outputs": []}


def migrate_workflows(ctx: Context, *, actor: str | None = None) -> dict:
    """Upgrade every v1 workflow body to v2. Idempotent."""
    actor = actor or from_env(channel="cli")
    listed = catalogs.list_entries(ctx, workflows.KIND)
    upgraded: list[str] = []
    skipped: list[str] = []
    for row in listed.get("entries") or []:
        if row.get("error") or not row.get("id"):
            continue
        wid = row["id"]
        try:
            data = catalogs.load_entry(ctx, workflows.KIND, wid)
        except KitError:
            continue
        if not _is_v1_workflow(data):
            skipped.append(wid)
            continue
        payload = dict(data)
        payload["body"] = workflow_body_v1_to_v2(ctx, data.get("body") or {})
        payload["schema_version"] = WORKFLOW_SCHEMA_V2
        catalogs.write_new_revision(
            ctx,
            workflows.KIND,
            wid,
            payload,
            actor=actor,
            schema_version=WORKFLOW_SCHEMA_V2,
        )
        upgraded.append(wid)

    report = {
        "kind": "migrate",
        "from": 1,
        "to": WORKFLOW_SCHEMA_V2,
        "upgraded": upgraded,
        "skipped": skipped,
        "entities": upgraded,
        "ok": True,
    }
    evidence.append_record(ctx, report)
    return report
