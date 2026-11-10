"""ApprovalPort stub (#2190): design boundary only — never a live approval runtime.

A real ApprovalPort mints and verifies authority signatures (Git SSH sign /
DSSE or equivalent) bound to token + channel identity. Agents never
self-approve; request-body fields are not authority (V1 axioms 2 + 4 / F-02 /
F-03). Until that custody runtime exists, status is `no-evaluable` and
`approvals_live` / `agent_can_approve` are always false.

`aisdlc improve validate` already refuses agent self-approval on proposal
artifacts. This module does not mint signatures, accept forge UI clicks as
custody, or promote Wilson autonomy levels.
"""
from __future__ import annotations

from typing import Any, Protocol


class ApprovalPort(Protocol):
    """Boundary for a future cryptographic approval / custody runtime."""

    def status(self) -> dict[str, Any]: ...


class UnevaluableApproval:
    """Default ApprovalPort: honest no-evaluable stub (no live approvals)."""

    kind: str = "unevaluable"

    def status(self) -> dict[str, Any]:
        return approval_status()


def approval_status() -> dict[str, Any]:
    """Report ApprovalPort readiness. Always unevaluable until a real runtime ships."""
    return {
        "status": "no-evaluable",
        "approvals_live": False,
        "agent_can_approve": False,
        "detail": (
            "no ApprovalPort custody runtime configured — SSH/DSSE (or equivalent) "
            "mint/verify and token+channel authority are not live; agents never "
            "self-approve; aisdlc improve validate is the policy tip ApprovalPort "
            "must eventually enforce (see ADR-005)"
        ),
    }


def doctor_note() -> str:
    """One doctor line: informational, never an error by itself."""
    payload = approval_status()
    return (
        f"approval: status={payload['status']} approvals_live={payload['approvals_live']} "
        f"agent_can_approve={payload['agent_can_approve']} — {payload['detail']}"
    )
