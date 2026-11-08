"""FeatureFlagPort: smallest OpenFeature-shaped stub (#2125 / ADR-003).

Provider + evaluation context + hooks + result (value, reason). Evaluation of an optional
flag must not throw. Ordinary settings stay config; approval, merge rights, security gates
and allowlists stay policy and fail closed — they are not feature flags.

`core.*` and `security.*` keys cannot override invariants through this port. OFREP and the
numeric error-code list are not implemented; this is not a claim of OpenFeature SDK conformance.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Protocol


FORBIDDEN_PREFIXES = ("core.", "security.")


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
