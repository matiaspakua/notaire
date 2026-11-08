"""Run the gates of one stage. A gate is a command, an exit code and an expectation; nothing else."""
from __future__ import annotations

from aisdlc import evidence, repo
from aisdlc.context import Context, run_shell
from aisdlc.profile import STAGES, Gate

SHOWN_TAIL = 15


def list_gates(ctx: Context) -> int:
    for stage in ctx.profile.stages():
        print(f"{stage}  [{STAGES.get(stage, 'custom stage')}]")
        for gate in ctx.profile.gates_for(stage):
            flags = [f for f, on in (("advisory", not gate.required), ("expect fail", gate.expect == "fail"),
                                     ("placeholder", gate.placeholder), ("paths", bool(gate.paths))) if on]
            print(f"  - {gate.name}{' (' + ', '.join(flags) + ')' if flags else ''}: {gate.run}")
    return 0


def run_stage(ctx: Context, stage: str, base: str | None, variables: dict[str, str], stream: bool) -> int:
    gates = ctx.profile.gates_for(stage)
    if not gates:
        print(f"error: no gates declared for stage {stage!r} (declared: {', '.join(ctx.profile.stages()) or 'none'})")
        return 2
    base = base or ctx.profile.base
    changed = _changed(ctx, base) if any(g.paths for g in gates) else None
    print(f"stage {stage} · {STAGES.get(stage, 'custom stage')} · base {base}")
    started = evidence.now()
    results = [_run_gate(ctx, gate, base, variables, changed, stream) for gate in gates]
    ok = all(r["status"] in ("pass", "skip", "todo") or not r["required"] for r in results)
    evidence.record_stage(ctx, {"ts": started, "stage": stage, "commit": repo.head(ctx.root),
                                "base": base, "ok": ok, "gates": results})
    print(f"stage {stage}: {'PASSED' if ok else 'FAILED'}")
    return 0 if ok else 1


def _changed(ctx: Context, base: str) -> list[str] | None:
    try:
        return repo.changed_files(ctx.root, base)
    except repo.GitError as exc:
        print(f"note: {exc}; path-scoped gates will run")
        return None


def _run_gate(ctx: Context, gate: Gate, base: str, variables: dict[str, str],
              changed: list[str] | None, stream: bool) -> dict:
    result = {"name": gate.name, "required": gate.required, "expect": gate.expect,
              "exit": None, "seconds": 0.0, "tail": []}
    if gate.placeholder:
        print(f"  TODO  {gate.name} — placeholder; configure its command in the profile")
        return {**result, "status": "todo"}
    if gate.paths and changed is not None and not any(repo.matches(p, gate.paths) for p in changed):
        print(f"  SKIP  {gate.name} — no change matches {', '.join(gate.paths)}")
        return {**result, "status": "skip"}
    command = ctx.expand(gate.run, {"base": base, **variables})
    outcome = run_shell(command, ctx.root, gate.timeout, stream=stream)
    passed = outcome.exit is not None and (outcome.exit == 0) == (gate.expect == "pass")
    status = "pass" if passed else "fail"
    _print_gate(gate, status, outcome, stream)
    return {**result, "status": status, "exit": outcome.exit, "seconds": round(outcome.seconds, 3),
            "timed_out": outcome.timed_out, "tail": [] if passed else outcome.tail}


def _print_gate(gate: Gate, status: str, outcome, stream: bool) -> None:
    detail = f"timeout after {gate.timeout}s" if outcome.timed_out else f"exit {outcome.exit}"
    if gate.expect == "fail":
        detail += ", expected to fail"
    if not gate.required:
        detail = "advisory, " + detail
    print(f"  {status.upper():4}  {gate.name} ({detail}, {outcome.seconds:.1f}s)")
    if status == "fail" and not stream:
        for line in outcome.tail[-SHOWN_TAIL:]:
            print(f"        | {line}")
