"""Grant profile data (#2192): V1 axiom 5 — validate/list/show only.

A Grant names worktree, write set, required checks, and budget. Declaring grants
in the adopter profile is **data toward Isolator OS** (ADR-004). This module never
enforces write sets, expires grants, claims sandbox isolation, or changes Isolator
status (`no-evaluable` / `sandbox=false` until a real runtime ships).
"""
from __future__ import annotations

import json

from aisdlc.context import Context
from aisdlc.profile import Grant, Profile, StageEffort


def _budget_dict(budget: StageEffort) -> dict:
    return {
        key: value
        for key, value in {
            "tokens": budget.tokens,
            "time_seconds": budget.time_seconds,
            "questions": budget.questions,
            "review_lines": budget.review_lines,
        }.items()
        if value is not None
    }


def grant_dict(grant: Grant) -> dict:
    """Serializable grant for config show / grant list. Never executes or enforces."""
    return {
        "id": grant.id,
        "stage": grant.stage,
        "worktree": grant.worktree,
        "write_set": list(grant.write_set),
        "required_checks": list(grant.required_checks),
        "budget": _budget_dict(grant.budget),
    }


def grant_dicts(profile: Profile) -> list[dict]:
    return [grant_dict(grant) for grant in profile.grants]


def validate_grants(ctx: Context) -> list[str]:
    """Fail-closed cross-check of required_checks against gate names. Read-only."""
    if not ctx.profile.grants:
        return []
    errors: list[str] = []
    gate_names = {gate.name for gate in ctx.profile.gates}
    gate_stages = {gate.stage for gate in ctx.profile.gates}
    for grant in ctx.profile.grants:
        if grant.stage is not None and grant.stage not in gate_stages:
            errors.append(
                f"grant {grant.id!r}: stage {grant.stage!r} does not match any gate stage"
            )
        for check in grant.required_checks:
            if check not in gate_names:
                errors.append(
                    f"grant {grant.id!r}: unknown required_checks {check!r} "
                    f"(not a declared gate name; fail closed)"
                )
    return errors


def list_grants(ctx: Context, *, as_json: bool) -> int:
    """Print declared grants. Never enforces or claims sandbox."""
    rows = grant_dicts(ctx.profile)
    if as_json:
        print(json.dumps({"grants": rows}, indent=2, sort_keys=True))
        return 0
    if not rows:
        print("grants: (none declared — omit is no-op; not Isolator OS)")
        return 0
    for grant in rows:
        bits = [f"grant {grant['id']}"]
        if grant.get("stage"):
            bits.append(f"stage={grant['stage']}")
        bits.append(f"worktree={grant['worktree']}")
        if grant["write_set"]:
            bits.append("write_set=" + ",".join(grant["write_set"]))
        if grant["required_checks"]:
            bits.append("checks=" + ",".join(grant["required_checks"]))
        if grant["budget"]:
            bits.append(
                "budget="
                + ",".join(f"{k}:{v}" for k, v in sorted(grant["budget"].items()))
            )
        print("  ".join(bits))
    return 0


def show_grant(ctx: Context, grant_id: str, *, as_json: bool) -> int:
    """Show one grant by id. Never enforces."""
    match = next((g for g in ctx.profile.grants if g.id == grant_id), None)
    if match is None:
        print(f"error: grant {grant_id!r} not found")
        return 2
    view = grant_dict(match)
    if as_json:
        print(json.dumps(view, indent=2, sort_keys=True))
    else:
        for key, value in view.items():
            print(f"{key}={value}")
    return 0
