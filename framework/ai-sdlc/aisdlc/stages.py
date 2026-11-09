"""Per-stage profile knobs (#2182): agents/skills/MCP/model/effort.

List and validate only read the profile and catalog metadata — they never execute
MCP servers, agent code, or skill scripts.
"""
from __future__ import annotations

from aisdlc.catalogs import KINDS, list_entries
from aisdlc.context import Context
from aisdlc.profile import Profile, ProfileError, StageProfile


def stage_dicts(profile: Profile) -> list[dict]:
    """Serializable stage knobs for config show / stage list. Never executes."""
    rows = []
    for stage in profile.stage_profiles:
        effort = {
            key: value
            for key, value in {
                "tokens": stage.effort.tokens,
                "time_seconds": stage.effort.time_seconds,
                "questions": stage.effort.questions,
                "review_lines": stage.effort.review_lines,
            }.items()
            if value is not None
        }
        rows.append(
            {
                "id": stage.id,
                "agents": list(stage.agents),
                "skills": list(stage.skills),
                "mcp": list(stage.mcp),
                "model": stage.model,
                "effort": effort,
            }
        )
    return rows


def _known_ids(ctx: Context, kind: str) -> set[str]:
    if kind not in KINDS:
        raise ProfileError(f"unknown catalog kind {kind!r}")
    listed = list_entries(ctx, kind)
    return {entry["id"] for entry in listed["entries"] if entry.get("id") and not entry.get("error")}


def validate_stage_profiles(ctx: Context) -> list[str]:
    """Fail-closed catalog / skills-dir resolution. Read-only; never executes."""
    if not ctx.profile.stage_profiles:
        return []
    errors: list[str] = []
    cache: dict[str, set[str]] = {}
    for stage in ctx.profile.stage_profiles:
        for kind, ids in (
            ("agents", stage.agents),
            ("skills", stage.skills),
            ("mcp", stage.mcp),
        ):
            if not ids:
                continue
            if kind not in cache:
                try:
                    cache[kind] = _known_ids(ctx, kind)
                except ProfileError as exc:
                    errors.append(f"stage {stage.id!r}: {exc}")
                    cache[kind] = set()
                    continue
            known = cache[kind]
            for entry_id in ids:
                if entry_id not in known:
                    if kind in ("agents", "mcp") and not ctx.profile.catalogs_dir:
                        errors.append(
                            f"stage {stage.id!r}: {kind} id {entry_id!r} needs "
                            f"[catalogs] dir (missing catalogs — never silent pass)"
                        )
                    else:
                        errors.append(
                            f"stage {stage.id!r}: unknown {kind} id {entry_id!r} "
                            f"(not in catalogs"
                            f"{' or skills-dir' if kind == 'skills' else ''})"
                        )
    return errors


def list_stages(ctx: Context, *, as_json: bool) -> int:
    """Print declared stage knobs. Never executes MCP/agents."""
    import json

    rows = stage_dicts(ctx.profile)
    if as_json:
        print(json.dumps({"stages": rows}, indent=2, sort_keys=True))
        return 0
    if not rows:
        print("stages: (none declared — gates still run)")
        return 0
    for stage in rows:
        bits = [f"stage {stage['id']}"]
        if stage["model"]:
            bits.append(f"model={stage['model']}")
        if stage["agents"]:
            bits.append("agents=" + ",".join(stage["agents"]))
        if stage["skills"]:
            bits.append("skills=" + ",".join(stage["skills"]))
        if stage["mcp"]:
            bits.append("mcp=" + ",".join(stage["mcp"]))
        if stage["effort"]:
            bits.append(
                "effort="
                + ",".join(f"{k}:{v}" for k, v in sorted(stage["effort"].items()))
            )
        print("  ".join(bits))
    return 0
