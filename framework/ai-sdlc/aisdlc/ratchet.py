"""Ratchets let a strict rule land on a legacy codebase: known violations are listed in a baseline
that may only shrink, and a measured value has a floor that may only hold or rise.

The baseline check compares the working tree with the base revision, so a line added by hand is
caught even when the tool that owns the baseline would accept it.
"""
from __future__ import annotations

import json
import re
from collections import Counter

from aisdlc import repo
from aisdlc.context import Context, run_shell
from aisdlc.profile import Ratchet

NUMBER = re.compile(r"-?\d+(?:\.\d+)?")


def run(ctx: Context, base: str | None, name: str | None) -> int:
    ratchets = [r for r in ctx.profile.ratchets if name is None or r.name == name]
    if name is not None and not ratchets:
        print(f"error: no ratchet named {name!r}")
        return 2
    if not ratchets:
        print("no ratchets declared")
        return 0
    base = base or ctx.profile.base
    if any(r.kind == "baseline" for r in ratchets) and not repo.ref_exists(ctx.root, base):
        print(f"error: base revision {base!r} does not exist (fetch it, or pass --base)")
        return 2
    failed = [r.name for r in ratchets if not (_baseline(ctx, r, base) if r.kind == "baseline" else _floor(ctx, r))]
    print(f"ratchets: {'FAILED (' + ', '.join(failed) + ')' if failed else 'PASSED'}")
    return 1 if failed else 0


def entries(text: str, is_json: bool) -> Counter:
    if is_json:
        return Counter(_leaves(json.loads(text or "{}")))
    return Counter(line.strip() for line in text.splitlines() if line.strip() and not line.strip().startswith("#"))


def _leaves(node, prefix: str = "") -> dict[str, int]:
    if isinstance(node, dict):
        out: dict[str, int] = {}
        for key, value in node.items():
            out.update(_leaves(value, f"{prefix} › {key}" if prefix else str(key)))
        return out
    if isinstance(node, list):
        return {f"{prefix}[{i}]={item}": 1 for i, item in enumerate(node)}
    if isinstance(node, (int, float)) and not isinstance(node, bool):
        return {prefix: int(node)}
    return {f"{prefix}={node}": 1}


def grown(before: Counter, after: Counter) -> list[str]:
    return sorted(f"{key} (+{after[key] - before.get(key, 0)})" for key in after if after[key] > before.get(key, 0))


def _baseline(ctx: Context, ratchet: Ratchet, base: str) -> bool:
    is_json = ratchet.file.endswith(".json")
    previous = repo.show(ctx.root, base, ratchet.file)
    current_path = ctx.path(ratchet.file)
    current = current_path.read_text(encoding="utf-8") if current_path.is_file() else ""
    after = entries(current, is_json)
    if previous is None:
        print(f"  PASS  {ratchet.name} — new baseline ({sum(after.values())} entries); it may only shrink from now on")
        return True
    before = entries(previous, is_json)
    added = grown(before, after)
    if added:
        print(f"  FAIL  {ratchet.name} — {len(added)} entr{'y' if len(added) == 1 else 'ies'} added to "
              f"{ratchet.file} since {base}; fix the violation instead of listing it:")
        for entry in added:
            print(f"        + {entry}")
        return False
    print(f"  PASS  {ratchet.name} — {sum(after.values())} entries (base {sum(before.values())})")
    return True


def _floor(ctx: Context, ratchet: Ratchet) -> bool:
    outcome = run_shell(ctx.expand(ratchet.run, {}), ctx.root, timeout=1800)
    numbers = NUMBER.findall("\n".join(outcome.tail))
    if outcome.exit != 0 or not numbers:
        print(f"  FAIL  {ratchet.name} — command {'printed no number' if outcome.exit == 0 else f'exited {outcome.exit}'}")
        return False
    value = float(numbers[-1])
    held = value >= ratchet.floor if ratchet.direction == "min" else value <= ratchet.floor
    bound = "floor" if ratchet.direction == "min" else "ceiling"
    print(f"  {'PASS' if held else 'FAIL'}  {ratchet.name} — {value:g} against {bound} {ratchet.floor:g}")
    if held and ratchet.direction == "min" and value > ratchet.floor:
        print(f"        raise the floor: floor = {value:g}")
    return held
