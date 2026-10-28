"""The project profile (`aisdlc.toml`): the only project-specific input of the kit.

Everything the kit knows about a project (its stages and gate commands, baselines, test
globs, skills catalog, budgets and prices) comes from this file, so the kit itself never
names a stack, a path or a tool of any adopter.
"""
from __future__ import annotations

import re
import tomllib
from dataclasses import dataclass, field
from pathlib import Path

PROFILE_NAMES = ("aisdlc.toml", ".aisdlc/aisdlc.toml")

# Canonical stages in lifecycle order and the hard gate each one belongs to.
STAGES = {
    "intake": "G0 ready",
    "plan": "G1 specified",
    "red": "G2 tests fail first",
    "green": "G3 verified",
    "pre-push": "G3 verified",
    "pre-pr": "G3 verified",
    "merge": "G4 reviewed",
    "release": "G5 shipped",
}
STAGE_NAME = re.compile(r"^[a-z][a-z0-9-]*$")
RATCHET_KINDS = ("baseline", "floor")
TIERS = ("human", "top", "mid", "low")

DEFAULT_COMMIT_PATTERN = (
    r"^(\[#[0-9]+\] )?((feat|fix|docs|chore|test|refactor|ci|build|perf|style|revert)"
    r"(\([^()]+\))?!?: .+|Revert \".+\")$"
)
DEFAULT_ISSUE_PATTERN = r"#(\d+)"
DEFAULT_SECTIONS = ("Why", "What changes", "Acceptance scenarios", "Test plan", "Traceability")


class ProfileError(Exception):
    """The profile is missing, unreadable or invalid. Maps to exit code 2."""


@dataclass(frozen=True)
class Gate:
    name: str
    stage: str
    run: str
    expect: str = "pass"
    required: bool = True
    timeout: int = 1800
    paths: tuple[str, ...] = ()
    placeholder: bool = False


@dataclass(frozen=True)
class Ratchet:
    name: str
    kind: str
    file: str | None = None
    run: str | None = None
    floor: float | None = None
    direction: str = "min"


@dataclass(frozen=True)
class Role:
    name: str
    tier: str
    model: str | None = None
    may_merge: bool = False


@dataclass
class Profile:
    name: str
    policy: str | None = None
    level: int = 1
    base: str = "origin/main"
    gates: list[Gate] = field(default_factory=list)
    ratchets: list[Ratchet] = field(default_factory=list)
    roles: list[Role] = field(default_factory=list)
    issue_pattern: str = DEFAULT_ISSUE_PATTERN
    verify: str | None = None
    spec_dir: str = ".aisdlc/changes"
    sections: tuple[str, ...] = DEFAULT_SECTIONS
    commit_pattern: str = DEFAULT_COMMIT_PATTERN
    tdd_production: tuple[str, ...] = ()
    tdd_tests: tuple[str, ...] = ()
    context_max_tokens: int | None = None
    context_files: tuple[str, ...] = ()
    skills_dir: str | None = None
    skills_generic: tuple[str, ...] = ()
    skills_forbid: tuple[str, ...] = ()
    evidence_dir: str = ".aisdlc/evidence"
    prices: dict[str, dict[str, float]] = field(default_factory=dict)

    def stages(self) -> list[str]:
        declared = {g.stage for g in self.gates}
        return [s for s in STAGES if s in declared] + sorted(declared - set(STAGES))

    def gates_for(self, stage: str) -> list[Gate]:
        return [g for g in self.gates if g.stage == stage]


def find_profile(root: Path) -> Path | None:
    for name in PROFILE_NAMES:
        candidate = root / name
        if candidate.is_file():
            return candidate
    return None


def load_profile(path: Path | None = None, text: str | None = None) -> Profile:
    if text is None:
        if path is None or not path.is_file():
            raise ProfileError(f"no profile found (looked for {', '.join(PROFILE_NAMES)})")
        text = path.read_text(encoding="utf-8")
    try:
        data = tomllib.loads(text)
    except tomllib.TOMLDecodeError as exc:
        raise ProfileError(f"profile is not valid TOML: {exc}") from exc
    errors: list[str] = []
    profile = _build(data, errors)
    if errors:
        raise ProfileError("invalid profile:\n  - " + "\n  - ".join(errors))
    return profile


def _build(data: dict, errors: list[str]) -> Profile:
    project = data.get("project", {})
    name = project.get("name")
    if not isinstance(name, str) or not name:
        errors.append("[project] name is required")
        name = "?"
    level = project.get("level", 1)
    if not isinstance(level, int) or not 1 <= level <= 4:
        errors.append(f"[project] level must be 1-4, got {level!r}")
        level = 1
    tracker, spec, commits = data.get("tracker", {}), data.get("spec", {}), data.get("commits", {})
    tdd, context, skills = data.get("tdd", {}), data.get("context", {}), data.get("skills", {})
    profile = Profile(
        name=name,
        policy=project.get("policy"),
        level=level,
        base=project.get("base", "origin/main"),
        issue_pattern=_regex(tracker.get("issue_pattern", DEFAULT_ISSUE_PATTERN), "tracker.issue_pattern", errors),
        verify=tracker.get("verify"),
        spec_dir=spec.get("dir", ".aisdlc/changes"),
        sections=tuple(spec.get("sections", DEFAULT_SECTIONS)),
        commit_pattern=_regex(commits.get("pattern", DEFAULT_COMMIT_PATTERN), "commits.pattern", errors),
        tdd_production=tuple(tdd.get("production", ())),
        tdd_tests=tuple(tdd.get("tests", ())),
        context_max_tokens=context.get("max_tokens"),
        context_files=tuple(context.get("files", ())),
        skills_dir=skills.get("dir"),
        skills_generic=tuple(skills.get("generic", ())),
        skills_forbid=tuple(_regex(p, "skills.forbid_in_generic", errors) for p in skills.get("forbid_in_generic", ())),
        evidence_dir=data.get("evidence", {}).get("dir", ".aisdlc/evidence"),
        prices=data.get("cost", {}).get("prices", {}),
    )
    profile.gates = _gates(data.get("gates", []), errors)
    profile.ratchets = _ratchets(data.get("ratchets", []), errors)
    profile.roles = _roles(data.get("roles", {}), errors)
    _check_prices(profile.prices, errors)
    return profile


def _regex(pattern: str, where: str, errors: list[str]) -> str:
    try:
        re.compile(pattern)
    except re.error as exc:
        errors.append(f"{where} pattern {pattern!r} is not a valid regex: {exc}")
    return pattern


def _gates(raw: list[dict], errors: list[str]) -> list[Gate]:
    gates, seen = [], set()
    for index, item in enumerate(raw, 1):
        label = f"gate #{index} ({item.get('name', '?')})"
        missing = [k for k in ("name", "stage", "run") if not item.get(k)]
        if missing:
            errors.append(f"{label} needs {', '.join(missing)}")
            continue
        if not STAGE_NAME.match(item["stage"]):
            errors.append(f"{label} stage {item['stage']!r} must match {STAGE_NAME.pattern}")
        if item.get("expect", "pass") not in ("pass", "fail"):
            errors.append(f"{label} expect must be 'pass' or 'fail'")
        key = (item["stage"], item["name"])
        if key in seen:
            errors.append(f"{label} is a duplicate gate name in stage {item['stage']}")
        seen.add(key)
        gates.append(Gate(
            name=item["name"],
            stage=item["stage"],
            run=item["run"],
            expect=item.get("expect", "pass"),
            required=bool(item.get("required", True)),
            timeout=int(item.get("timeout", 1800)),
            paths=tuple(item.get("paths", ())),
            placeholder=bool(item.get("placeholder", False)),
        ))
    return gates


def _ratchets(raw: list[dict], errors: list[str]) -> list[Ratchet]:
    ratchets = []
    for index, item in enumerate(raw, 1):
        label = f"ratchet #{index} ({item.get('name', '?')})"
        kind = item.get("kind")
        if not item.get("name"):
            errors.append(f"{label} needs a name")
        if kind not in RATCHET_KINDS:
            errors.append(f"{label} kind must be one of {', '.join(RATCHET_KINDS)}, got {kind!r}")
            continue
        if kind == "baseline" and not item.get("file"):
            errors.append(f"{label} baseline needs a file")
        if kind == "floor" and (not item.get("run") or not isinstance(item.get("floor"), (int, float))):
            errors.append(f"{label} floor needs run and a numeric floor")
        if item.get("direction", "min") not in ("min", "max"):
            errors.append(f"{label} direction must be 'min' or 'max'")
        ratchets.append(Ratchet(
            name=item.get("name", "?"),
            kind=kind,
            file=item.get("file"),
            run=item.get("run"),
            floor=float(item["floor"]) if isinstance(item.get("floor"), (int, float)) else None,
            direction=item.get("direction", "min"),
        ))
    return ratchets


def _roles(raw: dict, errors: list[str]) -> list[Role]:
    roles = []
    for name, item in raw.items():
        tier = item.get("tier")
        if tier not in TIERS:
            errors.append(f"role {name} tier must be one of {', '.join(TIERS)}, got {tier!r}")
        roles.append(Role(name=name, tier=tier or "?", model=item.get("model"), may_merge=bool(item.get("may_merge"))))
    return roles


def _check_prices(prices: dict, errors: list[str]) -> None:
    for model, price in prices.items():
        for key in ("input", "output"):
            if not isinstance(price.get(key), (int, float)):
                errors.append(f"cost.prices.{model} needs a numeric {key} (USD per million tokens)")
