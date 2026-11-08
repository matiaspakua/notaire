"""FeatureFlagPort: OpenFeature-shaped in-process provider (#2137 / ADR-003).

Smallest real path: stdlib JSON file provider (flat map or flagd-shaped variants).
No OpenFeature SDK dependency — kit stays stdlib-only. Evaluation never throws.

Ordinary settings stay config. Approval, merge rights, security gates and allowlists stay
policy and fail closed. Flags may gate optional experiments only:

- required gates cannot be skipped via `gate.<name>.skip`
- `factory.max_open_prs` may lower the ceiling, never raise it
- `core.*` / `security.*` keys always return the default with reason ERROR

OFREP and numeric error codes are not claimed.
"""
from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Protocol

FORBIDDEN_PREFIXES = ("core.", "security.")
PR_CEILING_FLAG = "factory.max_open_prs"


@dataclass(frozen=True)
class FlagsSettings:
    enabled: bool = False
    file: str = ".aisdlc/flags.json"


@dataclass(frozen=True)
class EvaluationContext:
    targeting_key: str | None = None
    attributes: dict[str, Any] = field(default_factory=dict)


@dataclass(frozen=True)
class FlagResult:
    value: Any
    reason: str  # STATIC | DEFAULT | ERROR
    flag_key: str


class FlagHooks:
    def before(self, flag_key: str, context: EvaluationContext) -> None:
        return None

    def after(self, flag_key: str, result: FlagResult, context: EvaluationContext) -> None:
        return None

    def error(self, flag_key: str, error: BaseException, context: EvaluationContext) -> None:
        return None


class FlagProvider(Protocol):
    def resolve(self, flag_key: str, default: Any, context: EvaluationContext) -> FlagResult: ...


@dataclass
class InMemoryProvider:
    flags: dict[str, Any] = field(default_factory=dict)
    hooks: FlagHooks = field(default_factory=FlagHooks)

    def resolve(self, flag_key: str, default: Any, context: EvaluationContext) -> FlagResult:
        if flag_key in self.flags:
            return FlagResult(value=self.flags[flag_key], reason="STATIC", flag_key=flag_key)
        return FlagResult(value=default, reason="DEFAULT", flag_key=flag_key)


@dataclass
class JsonFileProvider:
    """Load flags from a JSON file: flat `{key: value}` or flagd-shaped `{flags: {key: {...}}}`."""

    path: Path
    hooks: FlagHooks = field(default_factory=FlagHooks)

    def resolve(self, flag_key: str, default: Any, context: EvaluationContext) -> FlagResult:
        data = self._load()
        if data is None:
            return FlagResult(value=default, reason="ERROR", flag_key=flag_key)
        if flag_key not in data:
            return FlagResult(value=default, reason="DEFAULT", flag_key=flag_key)
        return FlagResult(value=data[flag_key], reason="STATIC", flag_key=flag_key)

    def _load(self) -> dict[str, Any] | None:
        if not self.path.is_file():
            return {}
        try:
            raw = json.loads(self.path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            return None
        if not isinstance(raw, dict):
            return None
        if "flags" in raw and isinstance(raw["flags"], dict):
            parsed: dict[str, Any] = {}
            for key, body in raw["flags"].items():
                parsed[key] = _flagd_value(body)
            return parsed
        return {str(k): v for k, v in raw.items() if k != "flags"}


def _flagd_value(body: Any) -> Any:
    if not isinstance(body, dict):
        return body
    variants = body.get("variants")
    default_variant = body.get("defaultVariant")
    if isinstance(variants, dict) and default_variant in variants:
        return variants[default_variant]
    if "default" in body:
        return body["default"]
    return body.get("value", False)


def evaluate(provider: FlagProvider, flag_key: str, default: Any, context: EvaluationContext) -> FlagResult:
    hooks = getattr(provider, "hooks", FlagHooks())
    try:
        if any(flag_key.startswith(prefix) for prefix in FORBIDDEN_PREFIXES):
            result = FlagResult(value=default, reason="ERROR", flag_key=flag_key)
            hooks.before(flag_key, context)
            hooks.after(flag_key, result, context)
            return result
        hooks.before(flag_key, context)
        result = provider.resolve(flag_key, default, context)
        hooks.after(flag_key, result, context)
        return result
    except Exception as exc:  # never throw to callers
        hooks.error(flag_key, exc, context)
        return FlagResult(value=default, reason="ERROR", flag_key=flag_key)


def can_skip_gate(
    provider: FlagProvider,
    *,
    gate_name: str,
    required: bool,
    context: EvaluationContext | None = None,
) -> bool:
    """Required gates are never skippable. Optional gates honor `gate.<name>.skip`."""
    if required:
        return False
    context = context or EvaluationContext()
    result = evaluate(provider, f"gate.{gate_name}.skip", False, context)
    return bool(result.value)


def clamp_pr_ceiling(
    provider: FlagProvider,
    *,
    ceiling: int,
    context: EvaluationContext | None = None,
) -> tuple[int, str]:
    """Flags may lower max_open_prs, never raise it above the profile ceiling."""
    context = context or EvaluationContext()
    result = evaluate(provider, PR_CEILING_FLAG, ceiling, context)
    value = result.value
    if isinstance(value, bool) or not isinstance(value, int):
        try:
            value = int(value)  # type: ignore[arg-type]
        except (TypeError, ValueError):
            return ceiling, "ERROR"
    if value > ceiling:
        return ceiling, "ERROR"
    if value < 0:
        return ceiling, "ERROR"
    return value, result.reason


_ACTIVE: FlagProvider | None = None


def get_flags() -> FlagProvider | None:
    return _ACTIVE


def bind_flags(root: Path, settings: FlagsSettings) -> FlagProvider | None:
    """Bind process-wide provider when enabled; otherwise clear."""
    global _ACTIVE
    if not settings.enabled:
        _ACTIVE = None
        return None
    _ACTIVE = JsonFileProvider(root / settings.file)
    return _ACTIVE


def noop_provider() -> InMemoryProvider:
    return InMemoryProvider({})
