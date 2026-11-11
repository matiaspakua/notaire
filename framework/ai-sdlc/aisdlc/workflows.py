"""First-class workflow entity + governance FSM (#2239 Phase A).

Stages (foreman knobs) are not workflows. A workflow is a versioned blueprint with
governance ``draft → defined → validated → approved``. List/show never execute
workflow steps. ``approve`` requires ApprovalPort verify or an explicit Owner
override — never a cosmetic badge (ADR-005).
"""
from __future__ import annotations

import json
from typing import Any

from aisdlc import approval, catalogs
from aisdlc.context import Context
from aisdlc.factory import redact
from aisdlc.profile import ProfileError

KIND = "workflows"
STATES = ("draft", "defined", "validated", "approved")

# action -> (from_state, to_state)
TRANSITIONS: dict[str, tuple[str, str]] = {
    "define": ("draft", "defined"),
    "request_validation": ("defined", "validated"),
    "approve": ("validated", "approved"),
    "reopen": ("defined", "draft"),
    "demote_validated": ("validated", "defined"),
    "demote_approved": ("approved", "validated"),
}


def _known_ids(ctx: Context, kind: str) -> set[str]:
    listed = catalogs.list_entries(ctx, kind)
    return {entry["id"] for entry in listed["entries"] if entry.get("id") and not entry.get("error")}


def _resolve_refs(ctx: Context, body: dict) -> list[str]:
    errors: list[str] = []
    for kind, key in (("agents", "agents"), ("skills", "skills"), ("mcp", "mcp"), ("providers", "providers")):
        ids = body.get(key) or []
        if not isinstance(ids, list):
            errors.append(f"body.{key} must be a list of catalog ids")
            continue
        if not ids:
            continue
        if kind not in catalogs.KINDS:
            continue
        try:
            known = _known_ids(ctx, kind)
        except ProfileError as exc:
            errors.append(str(exc))
            continue
        for entry_id in ids:
            if entry_id not in known:
                errors.append(f"unknown {kind} id {entry_id!r}")
    return errors


def create(
    ctx: Context,
    workflow_id: str,
    *,
    summary: str = "",
    agents: list[str] | None = None,
    skills: list[str] | None = None,
    mcp: list[str] | None = None,
    providers: list[str] | None = None,
    as_json: bool = False,
) -> int:
    body = {
        "agents": list(agents or []),
        "skills": list(skills or []),
        "mcp": list(mcp or []),
        "providers": list(providers or []),
    }
    extra = {
        "governance": "draft",
        "body": body,
    }
    code = catalogs.add(
        ctx,
        KIND,
        workflow_id,
        ref=None,
        summary=summary,
        extra=extra,
        quiet=True,
    )
    if code != 0:
        return code
    data = catalogs.load_entry(ctx, KIND, workflow_id)
    return _emit(data, as_json=as_json)


def list_workflows(ctx: Context, *, approved_only: bool = False, as_json: bool = False) -> int:
    listed = catalogs.list_entries(ctx, KIND)
    entries = listed["entries"]
    if approved_only:
        enriched = []
        for row in entries:
            try:
                full = catalogs.load_entry(ctx, KIND, row["id"])
            except ProfileError:
                continue
            if full.get("governance") == "approved":
                enriched.append(
                    {
                        "id": full["id"],
                        "kind": KIND,
                        "version": full.get("version"),
                        "governance": full.get("governance"),
                        "summary": full.get("summary"),
                        "digest": full.get("digest"),
                    }
                )
        entries = enriched
    payload = {"kind": KIND, "entries": entries, "approved_only": approved_only}
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        label = "approved workflows" if approved_only else "workflows"
        print(f"[{label}] {len(entries)}")
        for entry in entries:
            gov = entry.get("governance") or ""
            print(f"  - {entry['id']}" + (f" · {gov}" if gov else ""))
    return 0


def show(ctx: Context, workflow_id: str, *, as_json: bool = False) -> int:
    data = catalogs.show(ctx, KIND, workflow_id, redacted=True)
    return _emit(data, as_json=as_json)


def new_version(
    ctx: Context,
    workflow_id: str,
    *,
    summary: str | None = None,
    agents: list[str] | None = None,
    skills: list[str] | None = None,
    mcp: list[str] | None = None,
    providers: list[str] | None = None,
    as_json: bool = False,
) -> int:
    data = catalogs.load_entry(ctx, KIND, workflow_id)
    if data.get("governance") == "approved":
        raise ProfileError("approved workflows are immutable; demote before editing body")
    body = dict(data.get("body") or {})
    if agents is not None:
        body["agents"] = list(agents)
    if skills is not None:
        body["skills"] = list(skills)
    if mcp is not None:
        body["mcp"] = list(mcp)
    if providers is not None:
        body["providers"] = list(providers)
    payload = dict(data)
    payload["body"] = body
    if summary is not None:
        payload["summary"] = summary
    # Body edits keep governance but allocate a new revision.
    written = catalogs.write_new_revision(ctx, KIND, workflow_id, payload)
    return _emit(written, as_json=as_json)


def transition(
    ctx: Context,
    workflow_id: str,
    action: str,
    *,
    signature: str | None = None,
    owner_override: bool = False,
    reason: str | None = None,
    waive_reason: str | None = None,
    as_json: bool = False,
) -> int:
    if action not in TRANSITIONS:
        raise ProfileError(
            f"unknown workflow transition {action!r} "
            f"(expected {', '.join(TRANSITIONS)})"
        )
    data = catalogs.load_entry(ctx, KIND, workflow_id)
    current = data.get("governance") or "draft"
    if current not in STATES:
        raise ProfileError(f"workflow {workflow_id} has invalid governance {current!r}")
    expected_from, target = TRANSITIONS[action]
    if current != expected_from:
        raise ProfileError(
            f"illegal transition {action!r}: governance is {current!r}, expected {expected_from!r}"
        )

    body = data.get("body") or {}
    if not isinstance(body, dict):
        raise ProfileError("workflow body must be an object")

    if action == "define":
        errors = _resolve_refs(ctx, body)
        if errors:
            raise ProfileError("define failed: " + "; ".join(errors))

    if action == "request_validation":
        errors = _resolve_refs(ctx, body)
        if errors:
            raise ProfileError("request_validation failed: " + "; ".join(errors))
        if not waive_reason or not str(waive_reason).strip():
            # Phase A: require explicit waive/evidence reason (no fake dry-run runner).
            raise ProfileError(
                "request_validation requires --waive-reason (or recorded test evidence in a later phase)"
            )

    if action == "approve":
        _guard_approve(ctx, workflow_id, signature=signature, owner_override=owner_override, reason=reason)

    payload = dict(data)
    payload["governance"] = target
    if action == "request_validation":
        payload["validation"] = {"waive_reason": str(waive_reason).strip()}
    if action == "approve":
        payload["approval"] = {
            "mode": "signature" if signature else "owner_override",
            "reason": (reason or "").strip() or None,
        }
    written = catalogs.write_new_revision(ctx, KIND, workflow_id, payload)
    return _emit(written, as_json=as_json)


def _guard_approve(
    ctx: Context,
    workflow_id: str,
    *,
    signature: str | None,
    owner_override: bool,
    reason: str | None,
) -> None:
    status = approval.approval_status(ctx)
    if signature:
        subject = catalogs.current_subject_path(ctx, KIND, workflow_id)
        verified = approval.verify(subject, signature=signature, ctx=ctx)
        if not verified.get("ok"):
            raise ProfileError(
                f"approve failed ApprovalPort verify: {verified.get('detail') or 'not ok'}"
            )
        return
    if owner_override:
        if not reason or not str(reason).strip():
            raise ProfileError("approve --owner-override requires --reason")
        # Owner path is explicit and audited on the revision; still refuse agent self-approve.
        if status.get("agent_can_approve"):
            raise ProfileError("agents must never approve workflows")
        return
    live = bool(status.get("approvals_live"))
    if live:
        raise ProfileError(
            "approve requires --signature (ApprovalPort verify) when approvals_live=true"
        )
    raise ProfileError(
        "approve fail-closed: approvals_live=false; pass --owner-override --reason "
        "or configure ApprovalPort keys and --signature"
    )


def _emit(data: dict[str, Any], *, as_json: bool) -> int:
    view = redact("", data)
    if as_json:
        print(json.dumps(view, indent=2, sort_keys=True))
    else:
        print(
            f"workflow {view.get('id')}: governance={view.get('governance')} "
            f"version={view.get('version')} digest={view.get('digest')}"
        )
    return 0
