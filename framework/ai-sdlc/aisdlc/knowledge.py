"""Code-knowledge CLI. RepoLens is the default. The kit shells out; it does not import it.

Telemetry records the tool name, latency, exit code and output size. It does not
record the query, the prompt, or file contents.
"""
from __future__ import annotations

import json
import subprocess
import time
from dataclasses import dataclass

from aisdlc.context import Context
from aisdlc.profile import KnowledgeSettings
from aisdlc.telemetry import get_telemetry, redact_attributes


@dataclass(frozen=True)
class KnowledgeCall:
    argv: tuple[str, ...]
    provider: str
    tool: str


def plan_call(settings: KnowledgeSettings, tool: str, args: list[str]) -> KnowledgeCall:
    if not settings.provider or not settings.command:
        raise ValueError("knowledge provider and command are required")
    argv = (settings.command, *settings.args, tool, *args)
    return KnowledgeCall(argv=argv, provider=settings.provider, tool=tool)


def _record(ctx: Context, call: KnowledgeCall, *, exit_code: int, latency_ms: int, output_bytes: int) -> dict:
    event = redact_attributes({
        "provider": call.provider,
        "tool": call.tool,
        "exit_code": exit_code,
        "latency_ms": latency_ms,
        "output_bytes": output_bytes,
    })
    get_telemetry().emit("knowledge.call", event)
    folder = ctx.root / ctx.profile.evidence_dir
    try:
        folder.mkdir(parents=True, exist_ok=True)
        line = json.dumps({"ts": round(time.time(), 3), "name": "knowledge.call", **event}, separators=(",", ":"))
        with (folder / "knowledge.jsonl").open("a", encoding="utf-8") as handle:
            handle.write(line + "\n")
    except OSError:
        pass
    return event


def execute(ctx: Context, tool: str, args: list[str], *, dry_run: bool = False, timeout: int = 120) -> tuple[int, str]:
    call = plan_call(ctx.profile.knowledge, tool, args)
    if dry_run:
        return 0, json.dumps({
            "provider": call.provider,
            "tool": call.tool,
            "argv": list(call.argv),
            "executes": False,
        }, indent=2)
    started = time.perf_counter()
    try:
        completed = subprocess.run(
            list(call.argv), cwd=ctx.root, capture_output=True, text=True, timeout=timeout, check=False
        )
        code, stdout, stderr = completed.returncode, completed.stdout, completed.stderr
    except (OSError, subprocess.TimeoutExpired) as exc:
        code, stdout, stderr = 127, "", str(exc)
    latency_ms = int((time.perf_counter() - started) * 1000)
    _record(ctx, call, exit_code=code, latency_ms=latency_ms, output_bytes=len(stdout.encode()))
    text = stdout
    if stderr and code != 0:
        text = stdout + (("\n" if stdout else "") + stderr)
    return code, text
