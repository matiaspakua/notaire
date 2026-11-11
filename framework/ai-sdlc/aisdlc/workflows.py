"""First-class workflow entity + governance FSM (#2239 Phase A).

Stages (foreman knobs) are not workflows. A workflow is a versioned blueprint with
governance ``draft → defined → validated → approved``. List/show never execute
workflow steps. ``approve`` requires ApprovalPort verify or an explicit Owner
override — never a cosmetic badge (ADR-005).
"""
from __future__ import annotations

from aisdlc import approval, catalogs
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import redact

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
        except KitError as exc:
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
) -> dict:
    body = {
        "agents": list(agents or []),
        "skills": list(skills or []),
        "mcp": list(mcp or []),
        "providers": list(providers or []),
    }
    catalogs.add(ctx, KIND, workflow_id, ref=None, summary=summary, extra={"governance": "draft", "body": body})
    return redact("", catalogs.load_entry(ctx, KIND, workflow_id))


def list_workflows(ctx: Context, *, approved_only: bool = False) -> dict:
    entries = catalogs.list_entries(ctx, KIND)["entries"]
    if approved_only:
        enriched = []
        for row in entries:
            try:
                full = catalogs.load_entry(ctx, KIND, row["id"])
            except KitError:
                continue
            if full.get("governance") == "approved":
                enriched.append({key: full.get(key) for key in ("id", "version", "governance", "summary", "digest")})
        entries = [dict(row, kind=KIND) for row in enriched]
    return {"kind": KIND, "entries": entries, "approved_only": approved_only}


def show(ctx: Context, workflow_id: str) -> dict:
    return redact("", catalogs.show(ctx, KIND, workflow_id, redacted=True))


def new_version(
    ctx: Context,
    workflow_id: str,
    *,
    summary: str | None = None,
    agents: list[str] | None = None,
    skills: list[str] | None = None,
    mcp: list[str] | None = None,
    providers: list[str] | None = None,
) -> dict:
    data = catalogs.load_entry(ctx, KIND, workflow_id)
    if data.get("governance") == "approved":
        raise KitError("immutable_approved", "approved workflows are immutable; demote before editing body")
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
    return redact("", catalogs.write_new_revision(ctx, KIND, workflow_id, payload))


def transition(
    ctx: Context,
    workflow_id: str,
    action: str,
    *,
    signature: str | None = None,
    owner_override: bool = False,
    reason: str | None = None,
    waive_reason: str | None = None,
) -> dict:
    if action not in TRANSITIONS:
        raise KitError(
            "unknown_transition",
            f"unknown workflow transition {action!r} (expected {', '.join(TRANSITIONS)})",
        )
    data = catalogs.load_entry(ctx, KIND, workflow_id)
    current = data.get("governance") or "draft"
    if current not in STATES:
        raise KitError("invalid_governance", f"workflow {workflow_id} has invalid governance {current!r}")
    expected_from, target = TRANSITIONS[action]
    if current != expected_from:
        raise KitError(
            "illegal_transition",
            f"illegal transition {action!r}: governance is {current!r}, expected {expected_from!r}",
        )

    body = data.get("body") or {}
    if not isinstance(body, dict):
        raise KitError("invalid_body", "workflow body must be an object")

    if action == "define":
        errors = _resolve_refs(ctx, body)
        if errors:
            raise KitError("refs_unresolved", "define failed: " + "; ".join(errors))

    if action == "request_validation":
        errors = _resolve_refs(ctx, body)
        if errors:
            raise KitError("refs_unresolved", "request_validation failed: " + "; ".join(errors))
        if not waive_reason or not str(waive_reason).strip():
            # Phase A: require explicit waive/evidence reason (no fake dry-run runner).
            raise KitError(
                "waive_reason_required",
                "request_validation requires --waive-reason (or recorded test evidence in a later phase)",
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
    return redact("", catalogs.write_new_revision(ctx, KIND, workflow_id, payload))


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
            raise KitError(
                "approve_failed", f"approve failed ApprovalPort verify: {verified.get('detail') or 'not ok'}"
            )
        return
    if owner_override:
        if not reason or not str(reason).strip():
            raise KitError("reason_required", "approve --owner-override requires --reason")
        # Owner path is explicit and audited on the revision; still refuse agent self-approve.
        if status.get("agent_can_approve"):
            raise KitError("agent_approve_forbidden", "agents must never approve workflows")
        return
    live = bool(status.get("approvals_live"))
    if live:
        raise KitError(
            "signature_required", "approve requires --signature (ApprovalPort verify) when approvals_live=true"
        )
    raise KitError(
        "approve_fail_closed",
        "approve fail-closed: approvals_live=false; pass --owner-override --reason "
        "or configure ApprovalPort keys and --signature",
    )

