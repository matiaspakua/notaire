"""Adoption: `init` scaffolds the policy, profile, card and role agents into a repository;
`doctor` tells whether a profile is usable and what its maturity level still lacks."""
from __future__ import annotations

import shlex
import shutil
from pathlib import Path

from aisdlc.context import Context
from aisdlc.profile import STAGES

TEMPLATES = Path(__file__).resolve().parents[1] / "templates" / "project"
SHELL_WORDS = {"cd", "test", "[", "exit", "echo", "true", "false", "export", "set", "if", "for", "!", "env"}
INTERPRETERS = {"bash", "sh", "zsh", "python", "python3", "node", "ruby", "pwsh"}

# What each maturity level adds (docs: framework README "Adoption levels").
LEVELS = {
    1: {"stages": ("plan", "pre-push"), "needs": "policy, profile, change records, local gates"},
    2: {"stages": ("red", "pre-pr"), "needs": "red gates, ratchets, CI mirror of the local gates"},
    3: {"stages": ("merge",), "needs": "roles with a merge rule, merge gate on the full suite"},
    4: {"stages": ("release",), "needs": "release gate, cost prices, measured autonomy"},
}


def init(root: Path, name: str, level: int, force: bool) -> int:
    if not 1 <= level <= 4:
        print("error: --level must be 1-4")
        return 2
    values = {"{{project}}": name, "{{level}}": str(level)}
    for source in sorted(p for p in TEMPLATES.rglob("*") if p.is_file()):
        rel = source.relative_to(TEMPLATES)
        target = root / rel
        if target.exists() and not force:
            print(f"  skip     {rel} (exists; --force overwrites)")
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        text = source.read_text(encoding="utf-8")
        for key, value in values.items():
            text = text.replace(key, value)
        target.write_text(text, encoding="utf-8")
        shutil.copymode(source, target)
        print(f"  created  {rel}")
    print("\nnext: edit CONSTITUTION.md and aisdlc.toml (replace placeholder gates), then run `aisdlc doctor`")
    return 0


def doctor(ctx: Context) -> int:
    errors, warnings = _errors(ctx), _warnings(ctx)
    for message in errors:
        print(f"  ERROR  {message}")
    for message in warnings:
        print(f"  WARN   {message}")
    p = ctx.profile
    print(f"doctor: {p.name} · level {p.level} · {len(p.gates)} gates in {len(p.stages())} stages · "
          f"{len(p.ratchets)} ratchets · {len(p.roles)} roles — "
          f"{'FAILED' if errors else 'OK'} ({len(errors)} errors, {len(warnings)} warnings)")
    return 1 if errors else 0


def _errors(ctx: Context) -> list[str]:
    p = ctx.profile
    errors = []
    if p.policy and not ctx.path(p.policy).is_file():
        errors.append(f"policy document {p.policy} does not exist")
    errors += [f"always-loaded file {f} does not exist" for f in p.context_files if not ctx.path(f).is_file()]
    errors += [f"ratchet {r.name}: baseline {r.file} does not exist"
               for r in p.ratchets if r.kind == "baseline" and not ctx.path(r.file).is_file()]
    if p.skills_dir and not ctx.path(p.skills_dir).is_dir():
        errors.append(f"skills dir {p.skills_dir} does not exist")
    for gate in p.gates:
        script = _script(gate.run)
        if script and not gate.placeholder and not ctx.path(script).is_file():
            errors.append(f"gate {gate.stage}/{gate.name} runs {script}, which does not exist")
    return errors


def _warnings(ctx: Context) -> list[str]:
    p = ctx.profile
    warnings = [] if p.policy else ["no policy document ([project] policy): agents need one source of truth"]
    warnings += [f"gate {g.stage}/{g.name} is a placeholder" for g in p.gates if g.placeholder]
    warnings += [f"stage {s!r} is not canonical ({', '.join(STAGES)})" for s in p.stages() if s not in STAGES]
    for gate in p.gates:
        program = _program(gate.run)
        if program and not gate.placeholder and not shutil.which(program) and not ctx.path(program).exists():
            warnings.append(f"gate {gate.stage}/{gate.name}: {program!r} is not on PATH")
    for level in range(1, p.level + 1):
        missing = [s for s in LEVELS[level]["stages"] if not p.gates_for(s)]
        if missing:
            warnings.append(f"level {level} expects gates for {', '.join(missing)} ({LEVELS[level]['needs']})")
    if p.level >= 2 and not p.ratchets:
        warnings.append("level 2 expects ratchets for legacy debt (baselines that only shrink, floors that only rise)")
    if p.level >= 3 and not any(r.may_merge for r in p.roles):
        warnings.append("level 3 expects a role with may_merge = true (the merge rule)")
    return warnings


def _tokens(command: str) -> list[str]:
    try:
        return shlex.split(command)
    except ValueError:
        return []


def _program(command: str) -> str | None:
    tokens = _tokens(command)
    if not tokens or "=" in tokens[0] or tokens[0] in SHELL_WORDS or tokens[0] == "aisdlc":
        return None
    return tokens[0]


def _script(command: str) -> str | None:
    """The repository script an interpreter is asked to run (`bash tools/x.sh`), if any."""
    tokens = _tokens(command)
    if len(tokens) >= 2 and tokens[0] in INTERPRETERS and not tokens[1].startswith("-") \
            and "/" in tokens[1] and not tokens[1].startswith("/"):
        return tokens[1]
    return None
