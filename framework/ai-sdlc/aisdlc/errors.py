"""Typed service-layer error shared by every adapter (#2252)."""
from __future__ import annotations

from aisdlc.profile import ProfileError

CODES = {
    "no_catalogs": "no [catalogs] dir configured",
    "unknown_kind": "catalog kind is not one of catalogs.KINDS",
    "invalid_id": "catalog id is empty, hidden or contains a path separator",
    "not_found": "catalog entry does not exist",
    "already_exists": "catalog entry already exists",
    "revision_exists": "immutable catalog revision already exists",
    "invalid_entry": "stored entry is not a JSON object with an id",
    "unknown_transition": "workflow transition name is not defined",
    "illegal_transition": "workflow governance state does not allow the transition",
    "invalid_governance": "stored workflow governance is not a known state",
    "invalid_body": "workflow body is not an object",
    "refs_unresolved": "workflow references unknown catalog ids",
    "waive_reason_required": "request_validation needs a waive reason",
    "approve_failed": "ApprovalPort signature verification failed",
    "reason_required": "owner override needs a reason",
    "agent_approve_forbidden": "agents must never approve workflows",
    "signature_required": "approve needs a signature when approvals are live",
    "approve_fail_closed": "approvals are not live and no owner override was given",
    "immutable_approved": "approved workflows cannot be edited before demotion",
}


class KitError(ProfileError):
    """A failed service call: a stable ``code`` (see ``CODES``) and a human ``detail``."""

    def __init__(self, code: str, detail: str) -> None:
        super().__init__(detail)
        self.code = code
        self.detail = detail
