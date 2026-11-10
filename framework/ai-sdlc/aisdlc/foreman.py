"""Foreman dispatch plan from [[stages]] knobs (#2196).

Resolves a read-only dispatch plan for Cloud Foreman / kit foreman templates.
Never executes agents, skills, or MCP. Never claims Isolator OS or sandbox.
"""
from __future__ import annotations

import json
from typing import Any

from aisdlc.context import Context
from aisdlc.profile import ProfileError
from aisdlc.stages import stage_dicts

PLAN_NOTE = (
    "dispatch plan only - resolves [[stages]] knobs for Foreman routing; "
    "does not execute agents or MCP; not Isolator OS / not sandbox; "
    "unmapped gate stages are expected until knobs are filled (do not invent fake knobs)"
)


def build_plan(ctx: Context, stage_id: str | None = None) -> dict[str, Any]:
    """Resolve Foreman dispatch plan from profile stage knobs. Never executes."""
    gate_stages = set(ctx.profile.stages())
    if stage_id is not None and stage_id not in gate_stages:
        raise ProfileError(
            f"unknown stage {stage_id!r} (declared: {', '.join(sorted(gate_stages)) or 'none'})"
        )

    rows = stage_dicts(ctx.profile)
    if stage_id is not None:
        rows = [row for row in rows if row["id"] == stage_id]

    enriched: list[dict[str, Any]] = []
    for row in rows:
        item = dict(row)
        item["gates"] = [gate.name for gate in ctx.profile.gates if gate.stage == row["id"]]
        enriched.append(item)

    mapped_ids = {row["id"] for row in enriched}
    if stage_id is not None:
        candidates = {stage_id}
    else:
        candidates = gate_stages
    unmapped = sorted(candidates - mapped_ids)

    return {
        "note": PLAN_NOTE,
        "executes": False,
        "sandbox": False,
        "filter_stage": stage_id,
        "mapped_count": len(enriched),
        "unmapped_stages": unmapped,
        "stages": enriched,
    }


def plan(ctx: Context, *, stage_id: str | None = None, as_json: bool = False) -> int:
    """Print the Foreman dispatch plan. Exit 2 on unknown stage filter."""
    try:
        data = build_plan(ctx, stage_id)
    except ProfileError as exc:
        print(f"error: {exc}")
        return 2

    if as_json:
        print(json.dumps(data, indent=2, sort_keys=True))
        return 0

    print(
        f"foreman plan: mapped_count={data['mapped_count']} "
        f"unmapped_count={len(data['unmapped_stages'])} executes=false sandbox=false"
    )
    print(f"  note: {data['note']}")
    if data["unmapped_stages"]:
        print(
            "  unmapped_stages: "
            + ",".join(data["unmapped_stages"])
            + " (expected until [[stages]] knobs are filled)"
        )
    if not data["stages"]:
        print("  stages: (none declared — gates still run; consult fleet-manifest for roles)")
        return 0
    for stage in data["stages"]:
        bits = [f"stage {stage['id']}"]
        if stage.get("model"):
            bits.append(f"model={stage['model']}")
        if stage.get("agents"):
            bits.append("agents=" + ",".join(stage["agents"]))
        if stage.get("skills"):
            bits.append("skills=" + ",".join(stage["skills"]))
        if stage.get("mcp"):
            bits.append("mcp=" + ",".join(stage["mcp"]))
        if stage.get("effort"):
            bits.append(
                "effort="
                + ",".join(f"{k}:{v}" for k, v in sorted(stage["effort"].items()))
            )
        if stage.get("gates"):
            bits.append("gates=" + ",".join(stage["gates"]))
        print("  " + "  ".join(bits))
    return 0


def doctor_note() -> str:
    return (
        "Foreman routing: `aisdlc foreman plan|route` resolves [[stages]] knobs only "
        "(never executes agents/MCP; not Isolator OS / sandbox=false); "
        "unmapped stages are expected until knobs are filled"
    )
