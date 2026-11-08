"""TelemetryPort: optional OpenTelemetry behind a no-op-by-default port (#2125).

The kit stays stdlib-only. When `opentelemetry-api` / `opentelemetry-sdk` are importable
(e.g. from `framework/ai-sdlc/.venv-telemetry`) and `[telemetry] enabled = true`, the port
binds the SDK's `NoOpTracerProvider` for `exporter = "none"` — a real no-op SDK path, not a
hand-rolled fake. Exporters stay off unless a later Issue configures one. Prompts, diffs and
secrets are never attributes. This module does not claim GenAI semantic-convention conformance.

`TRACEPARENT`: not injected when telemetry is off or when the provider is not recording
(`exporter = "none"` / `OTEL_SDK_DISABLED`). `OTEL_SDK_DISABLED=true` is honored by the SDK.
"""
from __future__ import annotations

import importlib
import re
from dataclasses import dataclass, field
from typing import Any, Protocol

# Attribute keys that must never leave the process on a span/event.
_BANNED_KEY = re.compile(
    r"(prompt|diff|secret|password|passwd|token|authorization|api[_-]?key|private[_-]?key|credential)",
    re.I,
)
_ALLOWED_EXPORTERS = frozenset({"none"})


@dataclass(frozen=True)
class TelemetrySettings:
    enabled: bool = False
    exporter: str = "none"


class TelemetryPort(Protocol):
    kind: str
    sdk_name: str

    def is_recording(self) -> bool: ...
    def emit(self, name: str, attributes: dict[str, Any]) -> None: ...
    def traceparent(self) -> str: ...
    def exported_spans(self) -> list[str]: ...
    def debug_attributes(self) -> list[dict[str, Any]]: ...


def redact_attributes(attributes: dict[str, Any]) -> dict[str, Any]:
    """Drop prompts, diffs and secret-shaped keys. Values are never scanned into new keys."""
    clean: dict[str, Any] = {}
    for key, value in attributes.items():
        if _BANNED_KEY.search(str(key)):
            continue
        if isinstance(value, str) and _BANNED_KEY.search(value):
            continue
        clean[key] = value
    return clean


@dataclass
class MemoryTelemetry:
    """In-process recorder for unit tests and lead-time/cost hooks when OTel is off."""

    kind: str = "memory"
    sdk_name: str = "stdlib"
    _events: list[dict[str, Any]] = field(default_factory=list)

    def is_recording(self) -> bool:
        return True

    def emit(self, name: str, attributes: dict[str, Any]) -> None:
        self._events.append({"name": name, "attributes": redact_attributes(attributes)})

    def traceparent(self) -> str:
        return ""

    def exported_spans(self) -> list[str]:
        return []

    def debug_attributes(self) -> list[dict[str, Any]]:
        return [dict(e["attributes"]) for e in self._events]

    def events(self) -> list[dict[str, Any]]:
        return list(self._events)


@dataclass
class StdlibNoOpTelemetry:
    kind: str = "noop"
    sdk_name: str = "stdlib"

    def is_recording(self) -> bool:
        return False

    def emit(self, name: str, attributes: dict[str, Any]) -> None:
        return None

    def traceparent(self) -> str:
        return ""

    def exported_spans(self) -> list[str]:
        return []

    def debug_attributes(self) -> list[dict[str, Any]]:
        return []


class OtelNoOpTelemetry:
    """Real OpenTelemetry NoOpTracerProvider path (`exporter = none`)."""

    kind = "otel-noop"
    sdk_name = "opentelemetry"

    def __init__(self, trace_mod: Any, provider: Any) -> None:
        self._trace = trace_mod
        self._provider = provider
        self._tracer = trace_mod.get_tracer("aisdlc")
        self._last_attrs: list[dict[str, Any]] = []
        self._saw_recording = False

    def is_recording(self) -> bool:
        return self._saw_recording

    def emit(self, name: str, attributes: dict[str, Any]) -> None:
        clean = redact_attributes(attributes)
        self._last_attrs.append(clean)
        with self._tracer.start_as_current_span(name) as span:
            self._saw_recording = bool(span.is_recording())
            for key, value in clean.items():
                span.set_attribute(key, value)

    def traceparent(self) -> str:
        try:
            prop = importlib.import_module("opentelemetry.trace.propagation.tracecontext")
            carrier: dict[str, str] = {}
            prop.TraceContextTextMapPropagator().inject(carrier)
            return carrier.get("traceparent", "")
        except Exception:
            return ""

    def exported_spans(self) -> list[str]:
        return []

    def debug_attributes(self) -> list[dict[str, Any]]:
        return list(self._last_attrs)


_ACTIVE: TelemetryPort = StdlibNoOpTelemetry()


def get_telemetry() -> TelemetryPort:
    return _ACTIVE


def bind_telemetry(profile: Any, env: dict[str, str] | None = None) -> TelemetryPort:
    """Bind the process-wide port from profile + env. Safe to call repeatedly in tests via subprocess."""
    global _ACTIVE
    settings = getattr(profile, "telemetry", TelemetrySettings())
    env = dict(env or {})
    if not settings.enabled:
        _ACTIVE = StdlibNoOpTelemetry()
        return _ACTIVE
    if settings.exporter not in _ALLOWED_EXPORTERS:
        raise ValueError(f"telemetry.exporter must be one of {sorted(_ALLOWED_EXPORTERS)}")
    otel = _try_otel_noop(env)
    _ACTIVE = otel if otel is not None else StdlibNoOpTelemetry()
    return _ACTIVE


def _try_otel_noop(env: dict[str, str]) -> TelemetryPort | None:
    try:
        trace_mod = importlib.import_module("opentelemetry.trace")
        noop_cls = getattr(trace_mod, "NoOpTracerProvider")
    except Exception:
        return None
    disabled = env.get("OTEL_SDK_DISABLED", "").strip().lower() == "true"
    if disabled:
        # Construct the SDK TracerProvider so OTEL_SDK_DISABLED is proven against that SDK
        # (spans do not record; TRACEPARENT is not injected).
        try:
            sdk_trace = importlib.import_module("opentelemetry.sdk.trace")
            provider = sdk_trace.TracerProvider()
        except Exception:
            provider = noop_cls()
    else:
        # Real no-op SDK path: exporter=none binds NoOpTracerProvider, not a hand-rolled fake.
        provider = noop_cls()
    try:
        trace_mod.set_tracer_provider(provider)
    except Exception:
        # Provider already set in this process; still wrap whatever get_tracer returns.
        pass
    return OtelNoOpTelemetry(trace_mod, provider)


def record_stage_event(port: TelemetryPort, *, stage: str, ok: bool, seconds: float, **extra: Any) -> None:
    """Lead-time signal: stage duration only. Extra keys are redacted."""
    attrs = {"aisdlc.stage": stage, "aisdlc.ok": bool(ok), "aisdlc.duration_seconds": float(seconds), **extra}
    port.emit("aisdlc.stage.run", attrs)


def record_cost_event(
    port: TelemetryPort,
    *,
    agent: str,
    model: str,
    tokens_in: int,
    tokens_out: int,
    usd: float | None,
    **extra: Any,
) -> None:
    """Cost signal: tokens and USD. Model/agent are low-cardinality labels for factory cost, not prompts."""
    attrs = {
        "aisdlc.agent": agent,
        "aisdlc.model": model,
        "aisdlc.tokens_in": int(tokens_in),
        "aisdlc.tokens_out": int(tokens_out),
        "aisdlc.cost_usd": float(usd or 0.0),
        **extra,
    }
    port.emit("aisdlc.cost.record", attrs)


def validate_telemetry_settings(enabled: bool, exporter: str) -> list[str]:
    errors: list[str] = []
    if exporter not in _ALLOWED_EXPORTERS:
        errors.append(f"telemetry.exporter must be one of {sorted(_ALLOWED_EXPORTERS)}, got {exporter!r}")
    if enabled and exporter not in _ALLOWED_EXPORTERS:
        errors.append("telemetry.enabled requires a supported exporter (none until a collector Issue)")
    return errors
