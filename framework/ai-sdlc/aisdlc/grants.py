"""Grant profile data (#2192/#2194/#2198): list/show, write-set policy, expire kill-switch.

A Grant names worktree, write set, required checks, budget, and optional expire.
`grant check` is a **policy** dry-run (same honesty as `aisdlc committer`): it compares
changed paths to `write_set` globs and fails closed on expired grants. Expire schema
alone never claims OS isolation or Isolator sandbox (ADR-004).
"""
from __future__ import annotations

import json
from datetime import datetime, timedelta, timezone
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


def _format_expires_at(value: datetime | None) -> str | None:
    if value is None:
        return None
    utc = value.astimezone(timezone.utc)
    text = utc.strftime("%Y-%m-%dT%H:%M:%SZ")
    return text


def grant_dict(grant: Grant) -> dict:
    """Serializable grant for config show / grant list. Never executes or enforces."""
    view: dict[str, Any] = {
        "id": grant.id,
        "stage": grant.stage,
        "worktree": grant.worktree,
        "write_set": list(grant.write_set),
        "required_checks": list(grant.required_checks),
        "budget": _budget_dict(grant.budget),
    }
    expires = _format_expires_at(grant.expires_at)
    if expires is not None:
        view["expires_at"] = expires
    if grant.ttl_seconds is not None:
        view["ttl_seconds"] = grant.ttl_seconds
    return view


def grant_dicts(profile: Profile) -> list[dict]:
    return [grant_dict(grant) for grant in profile.grants]


def is_expired(grant: Grant, now: datetime | None = None) -> bool:
    """True when expires_at or ttl_seconds deadline is past (fail closed)."""
    clock = now or datetime.now(timezone.utc)
    if clock.tzinfo is None:
        clock = clock.replace(tzinfo=timezone.utc)
    else:
        clock = clock.astimezone(timezone.utc)
    if grant.expires_at is not None:
        deadline = grant.expires_at
        if deadline.tzinfo is None:
            deadline = deadline.replace(tzinfo=timezone.utc)
        if clock >= deadline.astimezone(timezone.utc):
            return True
    if grant.ttl_seconds is not None and grant.loaded_at is not None:
        loaded = grant.loaded_at
        if loaded.tzinfo is None:
            loaded = loaded.replace(tzinfo=timezone.utc)
        if clock >= loaded.astimezone(timezone.utc) + timedelta(seconds=grant.ttl_seconds):
            return True
    return False


def active_grants(
    grants: tuple[Grant, ...] | list[Grant],
    now: datetime | None = None,
) -> list[Grant]:
    """Non-expired grants only."""
    return [grant for grant in grants if not is_expired(grant, now=now)]


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
        if grant.get("expires_at"):
            bits.append(f"expires_at={grant['expires_at']}")
        if grant.get("ttl_seconds") is not None:
            bits.append(f"ttl_seconds={grant['ttl_seconds']}")
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
    *,
    now: datetime | None = None,
) -> dict[str, Any]:
    """Return status payload: pass | fail | no-evaluable."""
    expired = [g.id for g in grants if is_expired(g, now=now)]
    live = active_grants(grants, now=now)
    patterns = _write_set_patterns(live)
    base: dict[str, Any] = {
        "paths_scanned": len(changed),
        "grants": [g.id for g in live],
        "expired_grants": expired,
        "write_set": list(patterns),
        "outside": [],
        "note": POLICY_NOTE,
        "sandbox": False,
    }
    if grants and not live:
        return {
            **base,
            "status": "fail",
            "detail": "all grants expired — fail closed (expire kill-switch; not Isolator OS)",
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
        if result.get("expired_grants"):
            print(f"  expired_grants: {', '.join(result['expired_grants'])}")
        if result.get("write_set"):
            print(f"  write_set: {', '.join(result['write_set'])}")
        for path in result.get("outside") or []:
            print(f"  FAIL  outside write_set: {path}")
        if status == "no-evaluable":
            print(f"  detail: {result.get('detail')}")
        elif status == "pass":
            print("grant check: PASSED — all changed paths inside Grant write_set (policy)")
        elif status == "fail":
            detail = result.get("detail")
            if detail:
                print(f"grant check: FAILED — {detail}")
            else:
                print(
                    "grant check: FAILED — path(s) outside Grant write_set "
                    "(policy dry-run; not Isolator OS)"
                )
    if result["status"] == "fail":
        return 1
    return 0


def doctor_note() -> str:
    """Doctor INFO: Grant write-set/expire is policy-only; never a sandbox."""
    return (
        "grant: write-set check + expire kill-switch are policy only "
        "(aisdlc grant check) — not a sandbox; Isolator OS needs real containment (ADR-004)"
    )
