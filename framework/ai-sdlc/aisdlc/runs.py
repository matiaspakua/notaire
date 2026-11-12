"""Run / RunRequest with evidence-derived status (#2255 Phase A.4).

Start-run appends a ``run.request`` ledger event bound to an **approved**
workflow version + digest. Status is recomputed from evidence only — the kit
never reports ``running`` from the request alone. Harness adapters append
``run.started`` / ``step.*`` / ``run.finished`` via the reporting contract.
"""
from __future__ import annotations

import hashlib
import json
import secrets
from pathlib import Path

from aisdlc import evidence, projects, workflows
from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.evidence import now as evidence_now

RUNS_DIR = ".aisdlc/runs"
RUN_KINDS = (
    "run.request",
    "run.started",
    "run.finished",
    "step.started",
    "step.finished",
)
STATUSES = (
    "requested",
    "queued",
    "running",
    "succeeded",
    "failed",
    "cancelled",
    "blocked",
)


def _assert_id(value: str) -> str:
    if not value or "/" in value or "\\" in value or value.startswith("."):
        raise KitError("invalid_id", f"invalid run id {value!r}")
    return value


def _root(ctx: Context) -> Path:
    return ctx.path(RUNS_DIR)


def _path(ctx: Context, run_id: str) -> Path:
    return _root(ctx) / _assert_id(run_id) / "current.json"


def _write(path: Path, payload: dict) -> dict:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return payload


def _read(path: Path) -> dict:
    if not path.is_file():
        raise KitError("not_found", f"run not found: {path.parent.name}")
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or not data.get("id"):
        raise KitError("invalid_entry", f"invalid run file {path}")
    return data


def _new_run_id() -> str:
    return f"run-{secrets.token_hex(6)}"


def _ledger_head(ctx: Context) -> str:
    folder = ctx.path(ctx.profile.evidence_dir)
    path = folder / evidence.LEDGER
    line = None
    if path.is_file():
        lines = [ln for ln in path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        line = lines[-1] if lines else None
    if line is None:
        return evidence.GENESIS
    return hashlib.sha256(line.encode("utf-8")).hexdigest()


def events_for_run(ctx: Context, run_id: str) -> list[dict]:
    rows = evidence.ledger_records(ctx)
    return [
        row for row in rows
        if row.get("run_id") == run_id and row.get("kind") in RUN_KINDS
    ]


def derive_status(events: list[dict], steps: list[dict] | None = None) -> tuple[str, list[dict]]:
    """Derive (status, steps[]) from ordered evidence events. Never invents running."""
    step_map: dict[str, dict] = {s["id"]: dict(s) for s in (steps or []) if s.get("id")}
    status = "requested"
    blocked = False
    finished_ok: bool | None = None

    for event in events:
        kind = event.get("kind")
        if kind == "run.request":
            status = "queued"
        elif kind == "run.started":
            status = "running"
        elif kind == "step.started":
            step_id = str(event.get("step") or "").strip()
            if step_id:
                step_map[step_id] = {"id": step_id, "status": "running"}
        elif kind == "step.finished":
            step_id = str(event.get("step") or "").strip()
            step_status = str(event.get("step_status") or "fail").strip().casefold()
            # Skip is never pass.
            if step_status == "pass" and event.get("skip"):
                step_status = "skip"
            if step_id:
                step_map[step_id] = {"id": step_id, "status": step_status}
            if event.get("budget_overrun"):
                blocked = True
        elif kind == "run.finished":
            finished_ok = bool(event.get("ok"))
            if event.get("cancelled"):
                status = "cancelled"
            elif blocked or event.get("blocked"):
                status = "blocked"
            elif finished_ok:
                status = "succeeded"
            else:
                status = "failed"

    if blocked and status == "running":
        status = "blocked"
    elif blocked and status not in ("succeeded", "failed", "cancelled", "blocked"):
        status = "blocked"

    return status, list(step_map.values())


def _refresh(ctx: Context, payload: dict) -> dict:
    events = events_for_run(ctx, payload["id"])
    status, steps = derive_status(events, payload.get("steps"))
    payload = dict(payload)
    payload["status"] = status
    payload["steps"] = steps
    payload["evidence_head"] = _ledger_head(ctx)
    return payload


def start(
    ctx: Context,
    *,
    project_id: str,
    workflow_id: str,
    actor: str | None = None,
    run_id: str | None = None,
) -> dict:
    """Create a RunRequest evidence event for an approved workflow. Never sets running."""
    projects.show(ctx, project_id)  # ensure project exists
    workflow = workflows.show(ctx, workflow_id)
    if workflow.get("governance") != "approved":
        raise KitError(
            "workflow_not_approved",
            f"workflow {workflow_id!r} governance is {workflow.get('governance')!r}; "
            "runtime start accepts approved only",
        )

    rid = _assert_id(run_id) if run_id else _new_run_id()
    path = _path(ctx, rid)
    if path.is_file():
        raise KitError("already_exists", f"run {rid!r} already exists")

    requested_by = actor or from_env(channel="cli")
    requested_at = evidence_now()
    wf_ref = {
        "id": workflow["id"],
        "version": workflow["version"],
        "digest": workflow["digest"],
    }

    # Seed steps from workflow body when present (v2 steps or empty).
    body = workflow.get("body") or {}
    seed_steps: list[dict] = []
    for step in body.get("steps") or []:
        if isinstance(step, dict) and step.get("id"):
            seed_steps.append({"id": str(step["id"]), "status": "pending"})

    evidence.append_record(
        ctx,
        {
            "kind": "run.request",
            "run_id": rid,
            "project": project_id,
            "workflow": wf_ref,
            "requested_by": requested_by,
            "ts": requested_at,
            "ok": True,
        },
    )

    payload = {
        "id": rid,
        "project": project_id,
        "workflow": wf_ref,
        "status": "queued",
        "requested_by": requested_by,
        "requested_at": requested_at,
        "evidence_head": _ledger_head(ctx),
        "steps": seed_steps,
    }
    # Re-derive so status never claims running from the request alone.
    payload = _refresh(ctx, payload)
    if payload["status"] == "running":
        raise KitError("invalid_entry", "run.request must not yield running status")
    return _write(path, payload)


def show(ctx: Context, run_id: str) -> dict:
    payload = _refresh(ctx, _read(_path(ctx, run_id)))
    return _write(_path(ctx, run_id), payload)


def list_runs(ctx: Context) -> dict:
    root = _root(ctx)
    entries: list[dict] = []
    if root.is_dir():
        for child in sorted(root.iterdir()):
            current = child / "current.json"
            if current.is_file():
                try:
                    entries.append(show(ctx, child.name))
                except KitError:
                    continue
    return {"kind": "runs", "entries": entries}


def list_events(ctx: Context, run_id: str) -> dict:
    show(ctx, run_id)  # not_found if missing
    return {"run_id": run_id, "events": events_for_run(ctx, run_id)}


def append_harness_event(
    ctx: Context,
    *,
    run_id: str,
    event: str,
    adapter: str = "local-worker",
    step: str | None = None,
    step_status: str | None = None,
    ok: bool | None = None,
    budget_overrun: bool = False,
    cancelled: bool = False,
    blocked: bool = False,
) -> dict:
    """Reference harness adapter: hash-chain-append a run lifecycle event."""
    if event not in RUN_KINDS or event == "run.request":
        raise KitError(
            "invalid_body",
            f"harness event must be one of {', '.join(k for k in RUN_KINDS if k != 'run.request')}",
        )
    payload = show(ctx, run_id)
    record: dict = {
        "kind": event,
        "run_id": run_id,
        "adapter": adapter,
        "ts": evidence_now(),
        "ok": True if ok is None else bool(ok),
    }
    if step:
        record["step"] = step
    if step_status:
        status = step_status.strip().casefold()
        if status not in ("pass", "fail", "skip", "todo", "running", "pending"):
            raise KitError("invalid_body", f"invalid step_status {step_status!r}")
        # Skip is never pass — callers that send skip keep skip.
        record["step_status"] = status
    if budget_overrun:
        record["budget_overrun"] = True
        record["blocked"] = True
    if cancelled:
        record["cancelled"] = True
    if blocked:
        record["blocked"] = True
    if event == "run.finished" and ok is None and not cancelled and not blocked:
        record["ok"] = True

    evidence.append_record(ctx, record)
    refreshed = _refresh(ctx, payload)
    return _write(_path(ctx, run_id), refreshed)


def cancel_unavailable(ctx: Context, run_id: str) -> None:
    """Honest cancel refusal while Runner port is not proven."""
    show(ctx, run_id)
    raise KitError(
        "not_implemented",
        "cancel unavailable: no executable runner port proven — harness stubs are inventory only",
    )
