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

from aisdlc.flags import FlagsSettings
from aisdlc.telemetry import TelemetrySettings, validate_telemetry_settings

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
class CommitterPolicy:
    """Dry-run sole-committer policy (#2167). Never implies OS isolation."""

    required: bool = False
    runtime_names: tuple[str, ...] = ()
    runtime_emails: tuple[str, ...] = ()
    agent_names: tuple[str, ...] = ()
    agent_emails: tuple[str, ...] = ()
    agent_trailers: tuple[str, ...] = ()

    def evaluable(self) -> bool:
        # Opt-in (#2169): only when a runtime sole-committer identity is declared.
        # Agent deny lists alone stay no-evaluable so greenfield PRs are not failed.
        return bool(self.runtime_names or self.runtime_emails)


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


PLUGIN_KINDS = ("method", "harness", "tracker", "gate", "evidence")


@dataclass(frozen=True)
class PluginDecl:
    id: str
    kind: str
    version: str
    manifest: str
    trusted: bool = False


@dataclass(frozen=True)
class HarnessAdapter:
    """An agent runtime discovered by path. Not a commercial Harness product."""

    id: str
    runtime: str
    source: str


@dataclass(frozen=True)
class Role:
    name: str
    tier: str
    model: str | None = None
    may_merge: bool = False


# Effort budget units (#2182): tokens (prompt+completion), time_seconds (wall clock),
# questions (clarification budget), review_lines (changed-line review size).
EFFORT_KEYS = ("tokens", "time_seconds", "questions", "review_lines")


@dataclass(frozen=True)
class StageEffort:
    tokens: int | None = None
    time_seconds: int | None = None
    questions: int | None = None
    review_lines: int | None = None


@dataclass(frozen=True)
class StageProfile:
    """Optional per-stage knobs. Empty lists / None fields are a no-op."""

    id: str
    agents: tuple[str, ...] = ()
    skills: tuple[str, ...] = ()
    mcp: tuple[str, ...] = ()
    model: str | None = None
    effort: StageEffort = StageEffort()


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
    context_memory: tuple[str, ...] = ()
    skills_dir: str | None = None
    skills_generic: tuple[str, ...] = ()
    skills_forbid: tuple[str, ...] = ()
    evidence_dir: str = ".aisdlc/evidence"
    prices: dict[str, dict[str, float]] = field(default_factory=dict)
    protected_paths: tuple[str, ...] = ()
    protected_owner: str = "the Owner"
    size_limit: int | None = None
    size_exclude: tuple[str, ...] = ()
    scope_heading: str = "Write set"
    scope_always: tuple[str, ...] = ()
    max_open_prs: int = 3
    concurrency: int = 1
    timeout_seconds: int = 1800
    retry: int = 2
    backoff_seconds: float = 2.0
    plugin_allow: tuple[str, ...] = ()
    plugins: tuple["PluginDecl", ...] = ()
    inventory: str | None = None
    harness_adapters: tuple[HarnessAdapter, ...] = ()
    telemetry: TelemetrySettings = field(default_factory=TelemetrySettings)
    flags: FlagsSettings = field(default_factory=FlagsSettings)
    catalogs_dir: str | None = None
    committer: CommitterPolicy = field(default_factory=CommitterPolicy)
    stage_profiles: tuple[StageProfile, ...] = ()

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
    protected, size, scope = data.get("protected", {}), data.get("size", {}), data.get("scope", {})
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
        context_memory=_string_list(context.get("memory", []), "context.memory", errors),
        skills_dir=skills.get("dir"),
        skills_generic=tuple(skills.get("generic", ())),
        skills_forbid=tuple(_regex(p, "skills.forbid_in_generic", errors) for p in skills.get("forbid_in_generic", ())),
        evidence_dir=data.get("evidence", {}).get("dir", ".aisdlc/evidence"),
        prices=data.get("cost", {}).get("prices", {}),
        protected_paths=tuple(protected.get("paths", ())),
        protected_owner=protected.get("owner", "the Owner"),
        size_limit=_positive(size.get("limit"), "size.limit", errors),
        size_exclude=tuple(size.get("exclude", ())),
        scope_heading=scope.get("heading", "Write set"),
        scope_always=tuple(scope.get("always", ())),
    )
    factory, plugins = data.get("factory", {}), data.get("plugins", {})
    profile.max_open_prs = _fallback(_nonneg(factory.get("max_open_prs", 3), "factory.max_open_prs", errors), 3)
    profile.concurrency = _fallback(_positive(factory.get("concurrency", 1), "factory.concurrency", errors), 1)
    profile.timeout_seconds = _fallback(
        _positive(factory.get("timeout_seconds", 1800), "factory.timeout_seconds", errors), 1800
    )
    profile.retry = _nonneg(factory.get("retry", 2), "factory.retry", errors)
    if profile.retry is None:
        profile.retry = 2
    backoff = factory.get("backoff_seconds", 2)
    if not isinstance(backoff, (int, float)) or isinstance(backoff, bool) or backoff < 0:
        errors.append(f"factory.backoff_seconds must be a non-negative number, got {backoff!r}")
        backoff = 2
    profile.backoff_seconds = float(backoff)
    profile.inventory = factory.get("inventory")
    profile.plugin_allow = tuple(plugins.get("allow", ()))
    profile.plugins = _plugins(plugins.get("entries", []), errors)
    profile.harness_adapters = _harness_adapters(data.get("harness", {}).get("adapters", []), errors)
    profile.gates = _gates(data.get("gates", []), errors)
    profile.ratchets = _ratchets(data.get("ratchets", []), errors)
    profile.roles = _roles(data.get("roles", {}), errors)
    profile.telemetry = _telemetry(data.get("telemetry", {}), errors)
    profile.flags = _flags(data.get("flags", {}), errors)
    catalogs = data.get("catalogs", {})
    profile.catalogs_dir = catalogs.get("dir")
    if profile.catalogs_dir is not None and (
        not isinstance(profile.catalogs_dir, str) or not profile.catalogs_dir.strip()
    ):
        errors.append(f"catalogs.dir must be a non-empty string, got {profile.catalogs_dir!r}")
        profile.catalogs_dir = None
    profile.committer = _committer(data.get("committer", {}), errors)
    profile.stage_profiles = _stage_profiles(
        data.get("stages", []),
        gate_stages={g.stage for g in profile.gates},
        errors=errors,
    )
    _check_prices(profile.prices, errors)
    return profile


def _committer(raw: dict, errors: list[str]) -> CommitterPolicy:
    required = bool(raw.get("required", False))

    def strings(key: str) -> tuple[str, ...]:
        value = raw.get(key, [])
        if value is None:
            return ()
        if not isinstance(value, list) or any(not isinstance(item, str) for item in value):
            errors.append(f"committer.{key} must be a list of strings")
            return ()
        return tuple(item for item in value if item.strip())

    return CommitterPolicy(
        required=required,
        runtime_names=strings("runtime_names"),
        runtime_emails=strings("runtime_emails"),
        agent_names=strings("agent_names"),
        agent_emails=strings("agent_emails"),
        agent_trailers=strings("agent_trailers"),
    )


def _flags(raw: dict, errors: list[str]) -> FlagsSettings:
    enabled = bool(raw.get("enabled", False))
    file_path = raw.get("file", ".aisdlc/flags.json")
    if not isinstance(file_path, str) or not file_path.strip():
        errors.append(f"flags.file must be a non-empty string, got {file_path!r}")
        file_path = ".aisdlc/flags.json"
    return FlagsSettings(enabled=enabled, file=file_path)


def _telemetry(raw: dict, errors: list[str]) -> TelemetrySettings:
    enabled = bool(raw.get("enabled", False))
    exporter = raw.get("exporter", "none")
    if not isinstance(exporter, str):
        errors.append(f"telemetry.exporter must be a string, got {exporter!r}")
        exporter = "none"
    errors.extend(validate_telemetry_settings(enabled, exporter))
    return TelemetrySettings(enabled=enabled, exporter=exporter)


def _fallback(value: int | None, default: int) -> int:
    return default if value is None else value


def _nonneg(value, where: str, errors: list[str]) -> int | None:
    if not isinstance(value, int) or isinstance(value, bool) or value < 0:
        errors.append(f"{where} must be a non-negative integer, got {value!r}")
        return None
    return value


def _plugins(raw: list[dict], errors: list[str]) -> tuple[PluginDecl, ...]:
    found: list[PluginDecl] = []
    seen: set[str] = set()
    for index, item in enumerate(raw, 1):
        label = f"plugin #{index} ({item.get('id', '?')})"
        plugin_id = item.get("id")
        kind = item.get("kind")
        version = item.get("version")
        manifest = item.get("manifest")
        if not plugin_id or not version or not manifest:
            errors.append(f"{label} needs id, version and manifest")
            continue
        if kind not in PLUGIN_KINDS:
            errors.append(f"{label} kind must be one of {', '.join(PLUGIN_KINDS)}")
            continue
        if plugin_id in seen:
            errors.append(f"{label} duplicates id {plugin_id}")
        seen.add(plugin_id)
        text = str(manifest)
        if text.startswith("/") or ".." in Path(text).parts:
            errors.append(f"{label} manifest must be a relative path inside the project")
            continue
        found.append(PluginDecl(plugin_id, kind, str(version), text, bool(item.get("trusted", False))))
    return tuple(found)


def _harness_adapters(raw: list[dict], errors: list[str]) -> tuple[HarnessAdapter, ...]:
    found: list[HarnessAdapter] = []
    seen: set[str] = set()
    for index, item in enumerate(raw, 1):
        label = f"harness adapter #{index} ({item.get('id', '?')})"
        adapter_id = item.get("id")
        runtime = item.get("runtime")
        source = item.get("source")
        if not adapter_id or not runtime or not source:
            errors.append(f"{label} needs id, runtime and source")
            continue
        if adapter_id in seen:
            errors.append(f"{label} duplicates id {adapter_id}")
        seen.add(adapter_id)
        text = str(source)
        if text.startswith("/") or ".." in Path(text).parts:
            errors.append(f"{label} source must be a relative path inside the project")
            continue
        found.append(HarnessAdapter(str(adapter_id), str(runtime), text))
    return tuple(found)


def _positive(value, where: str, errors: list[str]) -> int | None:
    if value is None:
        return None
    if not isinstance(value, int) or isinstance(value, bool) or value <= 0:
        errors.append(f"{where} must be a positive integer, got {value!r}")
        return None
    return value


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


def _string_list(raw, where: str, errors: list[str]) -> tuple[str, ...]:
    if raw is None:
        return ()
    if not isinstance(raw, list) or any(not isinstance(item, str) or not item.strip() for item in raw):
        errors.append(f"{where} must be a list of non-empty strings")
        return ()
    return tuple(item.strip() for item in raw)


def _effort(raw, where: str, errors: list[str]) -> StageEffort:
    if raw is None:
        return StageEffort()
    if not isinstance(raw, dict):
        errors.append(f"{where} must be a table of budget fields ({', '.join(EFFORT_KEYS)})")
        return StageEffort()
    values: dict[str, int | None] = {key: None for key in EFFORT_KEYS}
    for key, value in raw.items():
        if key not in EFFORT_KEYS:
            errors.append(f"{where} unknown field {key!r} (expected {', '.join(EFFORT_KEYS)})")
            continue
        if not isinstance(value, int) or isinstance(value, bool) or value < 0:
            errors.append(f"{where}.{key} must be a non-negative integer, got {value!r}")
            continue
        values[key] = value
    return StageEffort(**values)


def _stage_profiles(raw: list, gate_stages: set[str], errors: list[str]) -> tuple[StageProfile, ...]:
    if not isinstance(raw, list):
        errors.append("[[stages]] must be an array of tables")
        return ()
    found: list[StageProfile] = []
    seen: set[str] = set()
    for index, item in enumerate(raw, 1):
        if not isinstance(item, dict):
            errors.append(f"stage #{index} must be a table")
            continue
        label = f"stage #{index} ({item.get('id', '?')})"
        stage_id = item.get("id")
        if not isinstance(stage_id, str) or not stage_id.strip():
            errors.append(f"{label} needs a non-empty id matching a gate stage")
            continue
        stage_id = stage_id.strip()
        if stage_id not in gate_stages:
            errors.append(
                f"{label} id {stage_id!r} does not match any gate stage "
                f"(declared: {', '.join(sorted(gate_stages)) or 'none'})"
            )
            continue
        if stage_id in seen:
            errors.append(f"{label} duplicates id {stage_id!r}")
            continue
        seen.add(stage_id)
        model = item.get("model")
        if model is not None and (not isinstance(model, str) or not model.strip()):
            errors.append(f"{label} model must be a non-empty string when set")
            model = None
        found.append(
            StageProfile(
                id=stage_id,
                agents=_string_list(item.get("agents"), f"{label} agents", errors),
                skills=_string_list(item.get("skills"), f"{label} skills", errors),
                mcp=_string_list(item.get("mcp"), f"{label} mcp", errors),
                model=model.strip() if isinstance(model, str) else None,
                effort=_effort(item.get("effort"), f"{label} effort", errors),
            )
        )
    return tuple(found)
