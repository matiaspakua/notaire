"""First-class workflow entity + governance FSM (#2239 Phase A / #2254 A.3).

Stages (foreman knobs) are not workflows. A workflow is a versioned blueprint with
governance ``draft → defined → validated → approved``. List/show never execute
workflow steps. ``approve`` requires ApprovalPort verify or an explicit Owner
override — never a cosmetic badge (ADR-005).

Body schema v1 keeps bare id lists (PRESERVE). Schema v2 uses ``steps[]`` /
``inputs[]`` / ``outputs[]`` with pinned ``<kind>:<id>@<version>`` refs; migrate
upgrades v1→v2. ``request_validation`` records digests; ``approve`` refuses drift.
"""
from __future__ import annotations

import re

from aisdlc import approval, catalogs
from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import redact

KIND = "workflows"
STATES = ("draft", "defined", "validated", "approved")
REF_RE = re.compile(r"^(?P<kind>[a-z]+):(?P<id>[^@]+)@(?P<version>\d+)$")

# action -> (from_state, to_state)
TRANSITIONS: dict[str, tuple[str, str]] = {
    "define": ("draft", "defined"),
    "request_validation": ("defined", "validated"),
    "approve": ("validated", "approved"),
    "reopen": ("defined", "draft"),
    "demote_validated": ("validated", "defined"),
    "demote_approved": ("approved", "validated"),
}


def _actor(actor: str | None) -> str:
    return actor or from_env(channel="cli")


def _known_ids(ctx: Context, kind: str) -> set[str]:
    listed = catalogs.list_entries(ctx, kind)
    return {entry["id"] for entry in listed["entries"] if entry.get("id") and not entry.get("error")}


def _is_v2_body(body: dict) -> bool:
    return isinstance(body.get("steps"), list)


def _resolve_refs_v1(ctx: Context, body: dict) -> list[str]:
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


def parse_ref(ref: str) -> tuple[str, str, int]:
    match = REF_RE.fullmatch(str(ref or "").strip())
    if not match:
        raise KitError("refs_unresolved", f"invalid pinned ref {ref!r} (expected kind:id@version)")
    kind = match.group("kind")
    if kind not in catalogs.KINDS:
        raise KitError("refs_unresolved", f"unknown ref kind {kind!r}")
    return kind, match.group("id"), int(match.group("version"))


def _collect_refs(body: dict) -> list[str]:
    refs: list[str] = []
    for step in body.get("steps") or []:
        if not isinstance(step, dict):
            continue
        for key in ("agent", "provider"):
            value = step.get(key)
            if value:
                refs.append(str(value))
        for key in ("skills", "mcp", "gates"):
            for item in step.get(key) or []:
                if isinstance(item, str) and ":" in item and "@" in item:
                    refs.append(item)
    for bucket in ("inputs", "outputs"):
        for item in body.get(bucket) or []:
            if isinstance(item, str) and ":" in item and "@" in item:
                refs.append(item)
            elif isinstance(item, dict) and item.get("ref"):
                refs.append(str(item["ref"]))
    # de-dupe preserving order
    seen: set[str] = set()
    out: list[str] = []
    for ref in refs:
        if ref not in seen:
            seen.add(ref)
            out.append(ref)
    return out


def _resolve_and_pin(ctx: Context, body: dict) -> tuple[list[str], list[dict]]:
    errors: list[str] = []
    pins: list[dict] = []
    for ref in _collect_refs(body):
        try:
            kind, entry_id, version = parse_ref(ref)
            rev = catalogs.load_revision(ctx, kind, entry_id, version)
        except KitError as exc:
            errors.append(str(exc.detail if hasattr(exc, "detail") else exc))
            continue
        digest = catalogs.compute_digest(rev)
        pins.append({"ref": ref, "digest": digest})
    return errors, pins


def _verify_pins(ctx: Context, pins: list[dict]) -> list[str]:
    errors: list[str] = []
    for pin in pins or []:
        ref = pin.get("ref")
        expected = pin.get("digest")
        try:
            kind, entry_id, version = parse_ref(str(ref))
            rev = catalogs.load_revision(ctx, kind, entry_id, version)
        except KitError as exc:
            errors.append(str(exc.detail if hasattr(exc, "detail") else exc))
            continue
        # Recompute so on-disk tampering is detected even if the digest field was left alone.
        if catalogs.compute_digest(rev) != expected:
            errors.append(f"digest drift for {ref}")
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
    actor: str | None = None,
) -> dict:
    body = {
        "agents": list(agents or []),
        "skills": list(skills or []),
        "mcp": list(mcp or []),
        "providers": list(providers or []),
    }
    catalogs.add(
        ctx,
        KIND,
        workflow_id,
        ref=None,
        summary=summary,
        extra={"governance": "draft", "body": body},
        actor=_actor(actor),
        schema_version=1,
    )
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
    actor: str | None = None,
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
    # Body edits allocate a new revision and return to draft (#2254).
    payload["governance"] = "draft"
    return redact(
        "",
        catalogs.write_new_revision(
            ctx,
            KIND,
            workflow_id,
            payload,
            actor=_actor(actor),
        ),
    )


def transition(
    ctx: Context,
    workflow_id: str,
    action: str,
    *,
    signature: str | None = None,
    owner_override: bool = False,
    reason: str | None = None,
    waive_reason: str | None = None,
    actor: str | None = None,
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

    pins: list[dict] | None = None
    if action in ("define", "request_validation"):
        if _is_v2_body(body):
            errors, pins = _resolve_and_pin(ctx, body)
        else:
            errors = _resolve_refs_v1(ctx, body)
        if errors:
            raise KitError("refs_unresolved", f"{action} failed: " + "; ".join(errors))

    if action == "request_validation":
        if not waive_reason or not str(waive_reason).strip():
            raise KitError(
                "waive_reason_required",
                "request_validation requires --waive-reason (or recorded test evidence in a later phase)",
            )

    if action == "approve":
        stored = (data.get("validation") or {}).get("pins") or []
        if stored:
            drift = _verify_pins(ctx, stored)
            if drift:
                raise KitError(
                    "pin_digest_mismatch",
                    "approve failed: pinned ref digest mismatch: " + "; ".join(drift),
                )
        _guard_approve(ctx, workflow_id, signature=signature, owner_override=owner_override, reason=reason)

    payload = dict(data)
    payload["governance"] = target
    if action == "request_validation":
        payload["validation"] = {
            "waive_reason": str(waive_reason).strip(),
            "pins": pins or [],
        }
    if action == "approve":
        payload["approval"] = {
            "mode": "signature" if signature else "owner_override",
            "reason": (reason or "").strip() or None,
        }
    return redact(
        "",
        catalogs.write_new_revision(
            ctx,
            KIND,
            workflow_id,
            payload,
            actor=_actor(actor),
        ),
    )


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
