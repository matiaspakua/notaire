"""Production line model (#2396): ordered stage view with gates, I/O, and guardrails.

``build_line(ctx)`` composes from existing kit functions — it owns no new extraction
logic. ``render_text(payload)`` formats the payload for human consumption.

Design: docs/openspec/changes/feat-2396-production-line-model/design.md
"""
from __future__ import annotations

import re

from aisdlc import catalogs, isolator
from aisdlc.context import Context
from aisdlc.stages import stage_dicts

# Default I/O per canonical stage id (design D3).
DEFAULT_IO: dict[str, dict[str, list[str]]] = {
    "plan": {
        "inputs": ["GitHub issue", "use case"],
        "outputs": ["OpenSpec change"],
    },
    "red": {
        "inputs": ["OpenSpec change"],
        "outputs": ["failing tests"],
    },
    "green": {
        "inputs": ["failing tests"],
        "outputs": ["passing tests"],
    },
    "pre-push": {
        "inputs": ["branch with passing tests"],
        "outputs": ["preflight evidence"],
    },
    "pre-pr": {
        "inputs": ["preflight evidence"],
        "outputs": ["pull request"],
    },
    "merge": {
        "inputs": ["pull request", "CI green"],
        "outputs": ["merged commit"],
    },
    "release": {
        "inputs": ["merged commit"],
        "outputs": ["deployed release"],
    },
}

_SLUG_NONALNUM = re.compile(r"[^a-z0-9]+")


def _gate_slug(name: str) -> str:
    """Deterministic lowercase slug from a gate name (design D2)."""
    return _SLUG_NONALNUM.sub("-", name.lower()).strip("-")


def _unique_slugs(gates: list) -> list[str]:
    """Return one slug per gate, deduplicating with a suffix counter (design D2 mitigation)."""
    slugs: list[str] = []
    seen: dict[str, int] = {}
    for g in gates:
        base = _gate_slug(g.name)
        if base not in seen:
            seen[base] = 0
            slugs.append(base)
        else:
            seen[base] += 1
            slugs.append(f"{base}-{seen[base] + 1}")
    return slugs


def _rules_catalog(ctx: Context) -> list[dict]:
    """Return rules catalog entries; return [] on any error (design D1 / tolerates missing)."""
    try:
        result = catalogs.list_entries(ctx, "rules")
        entries = result.get("entries", []) if isinstance(result, dict) else []
        return [e for e in entries if not e.get("error")]
    except Exception:  # noqa: BLE001
        return []


def build_line(ctx: Context) -> dict:
    """Compose the production line payload from existing kit functions.

    Never executes commands, never reads secrets.
    """
    profile = ctx.profile

    # Ordered stage ids: profile.stages() gives canonical order for gate-bearing stages.
    stage_ids_ordered = profile.stages()

    # Also include any stages declared in [[stages]] that profile.stages() might miss
    # (design D6: union of gate stages and [[stages]] ids, ordered by lifecycle first).
    stage_profile_ids = {sp.id for sp in profile.stage_profiles}
    extra_ids = sorted(stage_profile_ids - set(stage_ids_ordered))
    all_stage_ids = stage_ids_ordered + extra_ids

    # Build a lookup from stage id → StageProfile (may be absent for gate-only stages).
    sp_by_id = {sp.id: sp for sp in profile.stage_profiles}

    # Build a lookup from stage id → stage_dicts row (for inputs/outputs from profile).
    sd_by_id = {row["id"]: row for row in stage_dicts(profile)}

    stages_out: list[dict] = []
    for sid in all_stage_ids:
        raw_gates = profile.gates_for(sid)
        slugs = _unique_slugs(raw_gates)
        gates_out = [
            {
                "id": slug,
                "name": g.name,
                "command": g.run,
                "required": g.required,
                "mode": "advisory" if not g.required else "required",
                "expect": g.expect,
                "paths": list(g.paths),
            }
            for g, slug in zip(raw_gates, slugs)
        ]

        # Determine inputs/outputs and io_source (design D3).
        sp = sp_by_id.get(sid)
        sd = sd_by_id.get(sid)
        if sp is not None and (sp.inputs or sp.outputs):
            # Profile explicitly declared at least one of inputs/outputs.
            inputs = list(sp.inputs) if sp.inputs else []
            outputs = list(sp.outputs) if sp.outputs else []
            io_source = "profile"
        elif sd is not None and (sd.get("inputs") or sd.get("outputs")):
            # stage_dicts row has non-empty inputs/outputs (came from profile).
            inputs = sd.get("inputs") or []
            outputs = sd.get("outputs") or []
            io_source = "profile"
        else:
            # Fall back to kit defaults.
            defaults = DEFAULT_IO.get(sid, {})
            inputs = list(defaults.get("inputs", []))
            outputs = list(defaults.get("outputs", []))
            io_source = "default"

        stages_out.append(
            {
                "id": sid,
                "gates": gates_out,
                "inputs": inputs,
                "outputs": outputs,
                "io_source": io_source,
            }
        )

    # Guardrails block (design D4).
    protected_paths = list(profile.protected_paths)
    hooks = [p for p in protected_paths if "hook" in p.lower()]
    rules = _rules_catalog(ctx)
    iso = isolator.isolator_status()

    guardrails: dict = {
        "protected": {
            "owner": profile.protected_owner,
            "paths": protected_paths,
        },
        "hooks": hooks,
        "rules": rules,
        "isolator": {
            "status": iso.get("status"),
            "sandbox": bool(iso.get("sandbox")),
            "backend": iso.get("backend"),
            "enforces_committer": bool(iso.get("enforces_committer")),
        },
    }

    return {
        "schema_version": 1,
        "stages": stages_out,
        "guardrails": guardrails,
    }


def render_text(payload: dict) -> str:
    """Human-readable production line view."""
    lines: list[str] = []
    lines.append("=== Production Line ===")
    lines.append("")

    for stage in payload.get("stages", []):
        sid = stage["id"]
        io_note = f"  [{stage['io_source']}]" if stage.get("io_source") else ""
        lines.append(f"Stage: {sid}{io_note}")
        if stage.get("inputs"):
            lines.append(f"  inputs:  {', '.join(stage['inputs'])}")
        if stage.get("outputs"):
            lines.append(f"  outputs: {', '.join(stage['outputs'])}")
        gate_list = stage.get("gates", [])
        if gate_list:
            lines.append(f"  gates ({len(gate_list)}):")
            for g in gate_list:
                mode = "advisory" if not g["required"] else "required"
                lines.append(f"    - [{mode}] {g['name']}")
        lines.append("")

    g = payload.get("guardrails", {})
    lines.append("=== Guardrails ===")
    protected = g.get("protected", {})
    lines.append(f"  owner: {protected.get('owner', '')}")
    path_count = len(protected.get("paths", []))
    lines.append(f"  protected paths: {path_count}")
    hook_count = len(g.get("hooks", []))
    lines.append(f"  hooks: {hook_count}")
    rules = g.get("rules", [])
    lines.append(f"  rules catalog entries: {len(rules)}")
    iso = g.get("isolator", {})
    lines.append(
        f"  isolator: status={iso.get('status')} sandbox={iso.get('sandbox')} "
        f"backend={iso.get('backend')} enforces_committer={iso.get('enforces_committer')}"
    )

    return "\n".join(lines)
