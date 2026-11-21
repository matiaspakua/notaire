"""Agent CLI provider selection for RepoLens benchmarks.

Anthropic when `claude` is logged in or `ANTHROPIC_API_KEY` is set; otherwise the
Cursor agent CLI when that CLI is logged in or `CURSOR_API_KEY` is set. Order and
binaries come from flags, the environment, or an optional TOML table. A forced
provider that is unavailable is an error: it does not fall through.

This module is standalone. It does not import the factory kit.
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import time
import tomllib
from collections.abc import Callable
from dataclasses import dataclass, field
from pathlib import Path

DEFAULT_ORDER = ("anthropic", "cursor", "codex", "agy", "kiro", "opencode")
BUILTIN_TOOLS = ("Read", "Grep", "Glob")
ENV_ORDER = "REPOLENS_AGENT_ORDER"
ENV_PROVIDER = "REPOLENS_AGENT_PROVIDER"

Which = Callable[[str], str | None]
ProbeRunner = Callable[[list[str]], int]


@dataclass(frozen=True)
class ProviderSpec:
    id: str
    command: str
    fallback_commands: tuple[str, ...] = ()
    auth_env: tuple[str, ...] = ()
    probe: tuple[str, ...] = ()


@dataclass(frozen=True)
class ProviderStatus:
    id: str
    available: bool
    command: str = ""
    auth: str = ""
    reason: str = ""


@dataclass
class AgentSettings:
    order: tuple[str, ...] = DEFAULT_ORDER
    force: str = ""
    providers: dict[str, ProviderSpec] = field(default_factory=dict)

    def __post_init__(self) -> None:
        if not self.providers:
            self.providers = default_providers()


@dataclass(frozen=True)
class Selection:
    selected: str | None
    via: str
    command: str
    statuses: tuple[ProviderStatus, ...]
    reason: str = ""

    def to_dict(self) -> dict:
        return {
            "selected": self.selected,
            "via": self.via,
            "command": self.command,
            "reason": self.reason,
            "providers": [
                {
                    "id": item.id,
                    "available": item.available,
                    "command": item.command,
                    "auth": item.auth,
                    "reason": item.reason,
                }
                for item in self.statuses
            ],
        }


def default_providers() -> dict[str, ProviderSpec]:
    return {
        "anthropic": ProviderSpec(
            id="anthropic",
            command="claude",
            auth_env=("ANTHROPIC_API_KEY",),
            probe=("{command}", "auth", "status"),
        ),
        "cursor": ProviderSpec(
            id="cursor",
            command="agent",
            fallback_commands=("cursor-agent",),
            auth_env=("CURSOR_API_KEY",),
            probe=("{command}", "status"),
        ),
        "codex": ProviderSpec(
            id="codex",
            command="codex",
            auth_env=("OPENAI_API_KEY",),
            probe=("{command}", "--version"),
        ),
        "agy": ProviderSpec(
            id="agy",
            command="agy",
            auth_env=("ANTHROPIC_API_KEY",),
            probe=("{command}", "--version"),
        ),
        "kiro": ProviderSpec(
            id="kiro",
            command="kiro-cli",
            auth_env=("ANTHROPIC_API_KEY",),
            probe=("{command}", "--version"),
        ),
        "opencode": ProviderSpec(
            id="opencode",
            command="opencode",
            auth_env=("OPENAI_API_KEY", "ANTHROPIC_API_KEY"),
            probe=("{command}", "--version"),
        ),
    }


def _names(value: object, label: str) -> tuple[str, ...]:
    if value is None:
        return ()
    if isinstance(value, str):
        return tuple(part.strip() for part in value.split(",") if part.strip())
    if isinstance(value, list) and all(isinstance(item, str) and item.strip() for item in value):
        return tuple(item.strip() for item in value)
    raise ValueError(f"{label} must be a list of strings")


def settings_from_table(
    table: dict | None,
    env: dict[str, str] | None = None,
    *,
    provider: str = "",
    order: str = "",
    env_order: str = ENV_ORDER,
    env_provider: str = ENV_PROVIDER,
) -> AgentSettings:
    """Merge a TOML ``[agents]`` table with env and flags. Flags win, then env, then the file."""
    env = env if env is not None else dict(os.environ)
    raw = table or {}
    providers = default_providers()
    for key, spec in list(providers.items()):
        block = raw.get(key) if isinstance(raw.get(key), dict) else {}
        command = str(block.get("command") or spec.command)
        fallbacks = _names(
            block.get("fallback_commands", list(spec.fallback_commands)),
            f"agents.{key}.fallback_commands",
        )
        auth_env = _names(block.get("auth_env", list(spec.auth_env)), f"agents.{key}.auth_env")
        probe = _names(block.get("probe", list(spec.probe)), f"agents.{key}.probe")
        providers[key] = ProviderSpec(key, command, fallbacks, auth_env, probe or spec.probe)
    file_order = _names(raw.get("order", list(DEFAULT_ORDER)), "agents.order") or DEFAULT_ORDER
    env_order_value = (env.get(env_order) or "").strip()
    chosen_order = _names(order, "order") or _names(env_order_value, env_order) or file_order
    force = (provider or "").strip()
    if not force or force == "auto":
        force = (env.get(env_provider) or "").strip()
    if force == "auto":
        force = ""
    unknown = [name for name in (*chosen_order, *([force] if force else ())) if name not in providers]
    if unknown:
        raise ValueError(f"unknown agent provider {unknown[0]!r} (known: {', '.join(providers)})")
    return AgentSettings(order=chosen_order, force=force, providers=providers)


def load_settings(
    profile_path: Path | None,
    env: dict[str, str] | None = None,
    *,
    provider: str = "",
    order: str = "",
    env_order: str = ENV_ORDER,
    env_provider: str = ENV_PROVIDER,
) -> AgentSettings:
    table: dict = {}
    if profile_path is not None and profile_path.is_file():
        data = tomllib.loads(profile_path.read_text(encoding="utf-8"))
        raw = data.get("agents", {})
        if raw is None:
            raw = {}
        if not isinstance(raw, dict):
            raise ValueError("[agents] must be a table")
        table = raw
    return settings_from_table(
        table, env, provider=provider, order=order, env_order=env_order, env_provider=env_provider
    )


def _env_value(env: dict[str, str], name: str) -> str:
    return (env.get(name) or "").strip()


def _first_on_path(spec: ProviderSpec, which: Which) -> str:
    for name in (spec.command, *spec.fallback_commands):
        if which(name):
            return name
    return ""


def probe_provider(
    spec: ProviderSpec,
    env: dict[str, str],
    *,
    which: Which | None = None,
    run_probe: ProbeRunner | None = None,
) -> ProviderStatus:
    which = which or shutil.which
    command = _first_on_path(spec, which)
    key = next((name for name in spec.auth_env if _env_value(env, name)), "")
    names = ", ".join((spec.command, *spec.fallback_commands))
    keys = ", ".join(spec.auth_env) or "(none)"
    if command and key:
        return ProviderStatus(spec.id, True, command, f"env:{key}")
    if command and spec.probe and run_probe is not None:
        argv = [part.replace("{command}", command) for part in spec.probe]
        try:
            code = run_probe(argv)
        except (OSError, subprocess.TimeoutExpired):
            code = 127
        if code == 0:
            return ProviderStatus(spec.id, True, command, "login")
        return ProviderStatus(
            spec.id,
            False,
            command,
            reason=f"{command} is on PATH but '{' '.join(argv)}' exited {code} and {keys} is unset",
        )
    if command:
        return ProviderStatus(
            spec.id,
            False,
            command,
            reason=f"{command} is on PATH but {keys} is unset and no login probe is configured",
        )
    if key:
        return ProviderStatus(spec.id, False, reason=f"{key} is set but none of [{names}] is on PATH")
    return ProviderStatus(spec.id, False, reason=f"none of [{names}] is on PATH and {keys} is unset")


def select_provider(
    settings: AgentSettings,
    env: dict[str, str] | None = None,
    *,
    which: Which | None = None,
    run_probe: ProbeRunner | None = None,
) -> Selection:
    env = env if env is not None else dict(os.environ)
    statuses = tuple(
        probe_provider(settings.providers[name], env, which=which, run_probe=run_probe)
        for name in settings.order
    )
    by_id = {item.id: item for item in statuses}
    if settings.force:
        status = by_id.get(settings.force)
        if status is None or not status.available:
            reason = status.reason if status else f"unknown agent provider {settings.force!r}"
            return Selection(None, "forced", "", statuses, reason or f"{settings.force} is unavailable")
        return Selection(status.id, "forced", status.command, statuses)
    for index, status in enumerate(statuses):
        if status.available:
            return Selection(status.id, "preferred" if index == 0 else "fallback", status.command, statuses)
    reason = "; ".join(f"{item.id}: {item.reason}" for item in statuses)
    return Selection(None, "none", "", statuses, reason)


def build_argv(
    provider: str,
    command: str,
    prompt: str,
    *,
    model: str,
    budget: float = 1.0,
    mcp: Path | None = None,
    arm: str = "baseline",
) -> list[str]:
    """Argv for one headless session. The prompt is the last argument and is never a telemetry field."""
    if provider == "anthropic":
        cmd = [
            command, "-p", prompt, "--model", model, "--output-format", "stream-json", "--verbose",
            "--setting-sources", "", "--no-session-persistence", "--strict-mcp-config",
            "--max-budget-usd", str(budget), "--tools", ",".join(BUILTIN_TOOLS),
        ]
        allowed = list(BUILTIN_TOOLS)
        if arm == "graph" and mcp is not None:
            cmd += ["--mcp-config", str(mcp)]
            allowed.append("mcp__repolens")
        return cmd + ["--allowedTools", *allowed]
    if provider == "cursor":
        cmd = [
            command, "-p", "--output-format", "stream-json", "--mode", "ask", "--trust",
            "--model", model,
        ]
        if arm == "graph" and mcp is not None:
            cmd += ["--mcp-config", str(mcp)]
        return cmd + [prompt]
    if provider == "codex":
        # Codex: exec subcommand, --json for JSONL output, --dangerously-bypass for auto-approve
        cmd = [
            command, "exec", "--model", model, "--json",
            "--dangerously-bypass-approvals-and-sandbox",
            "--ephemeral",
        ]
        if arm == "graph" and mcp is not None:
            # Codex MCP via config override: mcp_servers.repolens.command, etc.
            cmd += ["-c", f"mcp_servers.repolens.config_path=\"{mcp}\""]
        return cmd + [prompt]
    if provider == "agy":
        # AGY: --print for non-interactive, --output-format stream-json
        cmd = [
            command, "--print", "--output-format", "stream-json",
            "--model", model,
        ]
        if arm == "graph" and mcp is not None:
            # AGY MCP via --settings JSON override
            server = {"command": sys.executable, "args": ["-m", "repolens", "mcp"]}
            cmd += ["--settings", json.dumps({"mcpConfig": {"repolens": server}})]
        return cmd + ["--prompt", prompt]
    if provider == "kiro":
        # kiro-cli: chat subcommand, --no-interactive, --trust-all-tools
        cmd = [
            command, "chat", "--model", model,
            "--no-interactive", "--trust-all-tools",
        ]
        if arm == "graph" and mcp is not None:
            # kiro MCP via --settings override
            server = {"command": sys.executable, "args": ["-m", "repolens", "mcp"]}
            cmd += ["--settings", json.dumps({"mcpServers": {"repolens": server}})]
        return cmd + [prompt]
    if provider == "opencode":
        # opencode: run subcommand, --format json, --auto for auto-approve
        cmd = [
            command, "run", "--model", model, "--format", "json", "--auto",
        ]
        if arm == "graph" and mcp is not None:
            # opencode MCP via environment or config file
            cmd += ["--mcp-config", str(mcp)]
        return cmd + [prompt]
    raise ValueError(f"no argv template for provider {provider!r}")


def usage_from_stream(stream: str) -> dict[str, int]:
    """Token counts from a Claude or Cursor stream-json result, when the event carries them."""
    usage: dict = {}
    for line in stream.splitlines():
        try:
            event = json.loads(line)
        except ValueError:
            continue
        if not isinstance(event, dict):
            continue
        if isinstance(event.get("usage"), dict) and event.get("type") in {"assistant", "result"}:
            usage = event["usage"]
    return {
        "input_tokens": int(usage.get("input_tokens") or 0)
        + int(usage.get("cache_creation_input_tokens") or 0)
        + int(usage.get("cache_read_input_tokens") or 0),
        "output_tokens": int(usage.get("output_tokens") or 0),
    }


def child_env(env: dict[str, str] | None = None) -> dict[str, str]:
    source = env if env is not None else dict(os.environ)
    blocked = ("CLAUDECODE", "CLAUDE_CODE_", "CLAUDE_AGENT")
    return {key: value for key, value in source.items() if not key.startswith(blocked)}


def default_probe_runner(argv: list[str]) -> int:
    try:
        completed = subprocess.run(
            argv, capture_output=True, text=True, timeout=8, check=False, env=child_env()
        )
    except (OSError, subprocess.TimeoutExpired):
        return 127
    return completed.returncode


def probe_main(argv: list[str] | None = None) -> int:
    """Print the selection JSON. Exit 2 when no provider can run."""
    import argparse

    parser = argparse.ArgumentParser(prog="providers")
    parser.add_argument("action", choices=("probe",))
    parser.add_argument("--provider", default="")
    parser.add_argument("--order", default="")
    parser.add_argument("--config", type=Path)
    args = parser.parse_args(argv)
    settings = load_settings(
        args.config, provider=args.provider, order=args.order,
        env_order=ENV_ORDER, env_provider=ENV_PROVIDER,
    )
    selection = select_provider(settings, run_probe=default_probe_runner)
    print(json.dumps(selection.to_dict(), indent=1))
    return 0 if selection.selected else 2


def run_session(
    argv: list[str],
    *,
    cwd: Path,
    timeout: int = 900,
    env: dict[str, str] | None = None,
) -> tuple[int, str, str, float]:
    started = time.perf_counter()
    try:
        completed = subprocess.run(
            argv, cwd=cwd, env=child_env(env), capture_output=True, text=True, timeout=timeout, check=False
        )
        return completed.returncode, completed.stdout, completed.stderr, time.perf_counter() - started
    except subprocess.TimeoutExpired as exc:
        stdout = exc.stdout or ""
        if isinstance(stdout, bytes):
            stdout = stdout.decode(errors="replace")
        return 124, stdout, "timeout", time.perf_counter() - started


if __name__ == "__main__":
    import sys

    sys.exit(probe_main())
