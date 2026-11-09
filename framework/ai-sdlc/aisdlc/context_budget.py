"""Memory catalog ↔ always-loaded context budget (#2186).

Profile `[context].memory` pack ids resolve against the memory catalog. List,
validate, show, and budget only read metadata and measure file sizes — they
never execute pack payloads.
"""
from __future__ import annotations

from pathlib import Path

from aisdlc.catalogs import list_entries
from aisdlc.context import Context
from aisdlc.profile import ProfileError


def _memory_entries(ctx: Context) -> dict[str, dict]:
    """Map memory pack id → catalog list row. Read-only; never executes."""
    listed = list_entries(ctx, "memory")
    return {
        entry["id"]: entry
        for entry in listed["entries"]
        if entry.get("id") and not entry.get("error")
    }


def validate_context_memory(ctx: Context) -> list[str]:
    """Fail-closed memory pack resolution. Read-only; never executes."""
    packs = ctx.profile.context_memory
    if not packs:
        return []
    errors: list[str] = []
    if not ctx.profile.catalogs_dir:
        errors.append(
            "context.memory needs [catalogs] dir (missing catalogs — never silent pass)"
        )
        return errors
    try:
        known = _memory_entries(ctx)
    except ProfileError as exc:
        errors.append(f"context.memory: {exc}")
        return errors
    for pack_id in packs:
        if pack_id not in known:
            errors.append(
                f"context.memory: unknown memory pack id {pack_id!r} (not in catalogs)"
            )
    return errors


def memory_pack_paths(ctx: Context) -> tuple[list[tuple[str, Path]], list[str]]:
    """Resolve memory pack refs to paths to measure. Never executes.

    Returns ([(pack_id, path), ...], errors). Directory refs expand to
    non-hidden regular files one level deep.
    """
    packs = ctx.profile.context_memory
    if not packs:
        return [], []
    errors: list[str] = []
    if not ctx.profile.catalogs_dir:
        return [], [
            "context.memory needs [catalogs] dir (missing catalogs — never silent pass)"
        ]
    try:
        known = _memory_entries(ctx)
    except ProfileError as exc:
        return [], [f"context.memory: {exc}"]

    resolved: list[tuple[str, Path]] = []
    seen: set[Path] = set()
    for pack_id in packs:
        entry = known.get(pack_id)
        if entry is None:
            errors.append(
                f"context.memory: unknown memory pack id {pack_id!r} (not in catalogs)"
            )
            continue
        ref = entry.get("ref")
        if not ref:
            errors.append(f"memory pack {pack_id!r}: missing ref")
            continue
        path = ctx.path(str(ref))
        if path.is_file():
            if path not in seen:
                seen.add(path)
                resolved.append((pack_id, path))
            continue
        if path.is_dir():
            for child in sorted(path.iterdir()):
                if child.name.startswith(".") or not child.is_file():
                    continue
                if child not in seen:
                    seen.add(child)
                    resolved.append((pack_id, child))
            continue
        errors.append(f"memory pack {pack_id!r}: ref {ref} not found")
    return resolved, errors


def context_dict(profile) -> dict | None:
    """Serializable [context] knobs for config show. Never executes."""
    if (
        profile.context_max_tokens is None
        and not profile.context_files
        and not profile.context_memory
    ):
        return None
    return {
        "max_tokens": profile.context_max_tokens,
        "files": list(profile.context_files),
        "memory": list(profile.context_memory),
    }
