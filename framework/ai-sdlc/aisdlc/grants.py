"""Grant profile data (#2192/#2194): V1 axiom 5 — list/show + write-set policy dry-run.

A Grant names worktree, write set, required checks, and budget. Declaring grants
in the adopter profile is **data toward Isolator OS** (ADR-004). `grant check` is a
**policy** dry-run (same honesty as `aisdlc committer`): it compares changed paths
to `write_set` globs. It never claims OS isolation, sandbox, bubblewrap/landlock,
expires grants, or changes Isolator status (`no-evaluable` / `sandbox=false`).
"""
from __future__ import annotations

import json
from typing import Any

from aisdlc import repo
from aisdlc.context import Context
from aisdlc.profile import Grant, Profile, StageEffort

POLICY_NOTE = (
    "dry-run policy check only — Grant write-set globs vs changed paths; "
    "does not claim OS isolation or sandbox"
)


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


def _write_set_patterns(grants: tuple[Grant, ...] | list[Grant]) -> list[str]:
    """Union of non-empty Grant.write_set globs (repo-relative)."""
    patterns: list[str] = []
    for grant in grants:
        patterns.extend(grant.write_set)
    return patterns


def _in_write_set(path: str, patterns: list[str]) -> bool:
    """Match like scope write-set: bare dirs also cover descendants."""
    expanded: list[str] = []
    for pattern in patterns:
        expanded.append(pattern)
        if pattern.endswith("/"):
            expanded.append(pattern + "**")
        elif not any(ch in pattern for ch in "*?["):
            expanded.append(pattern.rstrip("/") + "/**")
    return repo.matches(path, expanded)


def evaluate_write_set(
    changed: list[str],
    grants: tuple[Grant, ...] | list[Grant],
) -> dict[str, Any]:
    """Return status payload: pass | fail | no-evaluable."""
    patterns = _write_set_patterns(grants)
    base: dict[str, Any] = {
        "paths_scanned": len(changed),
        "grants": [g.id for g in grants],
        "write_set": list(patterns),
        "outside": [],
        "note": POLICY_NOTE,
        "sandbox": False,
    }
    if not patterns:
        return {
            **base,
            "status": "no-evaluable",
            "detail": "no Grant write_set declared "
            "(add [[grants]] write_set globs to activate the policy dry-run)",
        }
    outside = [path for path in changed if not _in_write_set(path, patterns)]
    if outside:
        return {**base, "status": "fail", "outside": outside}
    return {**base, "status": "pass", "outside": []}


def check(ctx: Context, base: str | None, as_json: bool) -> int:
    """Compare changed paths since base to Grant write_set globs (policy only)."""
    base = base or ctx.profile.base
    try:
        changed = repo.changed_files(ctx.root, base)
    except repo.GitError as exc:
        print(f"grant check: FAILED — {exc}")
        return 2
    result = evaluate_write_set(changed, ctx.profile.grants)
    result["base"] = base
    if as_json:
        print(json.dumps(result, indent=2, sort_keys=True))
    else:
        status = result["status"]
        print(f"grant check: {status} ({result['paths_scanned']} path(s) since {base})")
        print(f"  note: {result['note']}")
        print(f"  sandbox: {result['sandbox']} (never a sandbox claim)")
        if result.get("write_set"):
            print(f"  write_set: {', '.join(result['write_set'])}")
        for path in result.get("outside") or []:
            print(f"  FAIL  outside write_set: {path}")
        if status == "no-evaluable":
            print(f"  detail: {result.get('detail')}")
        elif status == "pass":
            print("grant check: PASSED — all changed paths inside Grant write_set (policy)")
        elif status == "fail":
            print(
                "grant check: FAILED — path(s) outside Grant write_set "
                "(policy dry-run; not Isolator OS)"
            )
    if result["status"] == "fail":
        return 1
    return 0


def doctor_note() -> str:
    """Doctor INFO: Grant write-set check is policy-only; never a sandbox."""
    return (
        "grant: write-set check is policy dry-run only "
        "(aisdlc grant check) — not a sandbox; Isolator OS still missing (ADR-004)"
    )
