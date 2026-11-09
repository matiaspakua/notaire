"""IsolatorPort stub (#2173): design boundary only — never an OS sandbox.

A real Isolator is a sole-committer **runtime** (grants, write set, agent never
commits). Until that runtime exists, status is `no-evaluable` and `sandbox` is
always false. YAML, harness adapters, and profile flags are **not** isolation
(ADR-002 / ADR-004).

`aisdlc committer` (#2167/#2169) is the **policy** dry-run Isolator must eventually
enforce. This module does not flip `[committer] required` and does not invent
Owner `runtime_*` identities.
"""
from __future__ import annotations

from typing import Any, Protocol


class IsolatorPort(Protocol):
    """Boundary for a future OS sole-committer runtime."""

    def status(self) -> dict[str, Any]: ...


class UnevaluableIsolator:
    """Default Isolator: honest no-evaluable stub (no sandbox claim)."""

    kind: str = "unevaluable"

    def status(self) -> dict[str, Any]:
        return isolator_status()


def isolator_status() -> dict[str, Any]:
    """Report Isolator readiness. Always unevaluable until a real runtime ships."""
    return {
        "status": "no-evaluable",
        "sandbox": False,
        "enforces_committer": False,
        "detail": (
            "no OS Isolator runtime configured — YAML/harness flags are not isolation; "
            "aisdlc committer is the policy dry-run Isolator must eventually enforce "
            "(see ADR-004)"
        ),
    }


def doctor_note() -> str:
    """One doctor line: informational, never an error by itself."""
    payload = isolator_status()
    return (
        f"isolator: status={payload['status']} sandbox={payload['sandbox']} "
        f"— {payload['detail']}"
    )
