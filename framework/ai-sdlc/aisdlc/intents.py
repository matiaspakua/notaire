"""Append-only intent runs for comparing a factory with and without a knowledge backend.

The log never stores prompts, diffs, or secrets. Token counts and cost stay null
unless a flag, a price table, or a reported provider cost supplies them.
"""
from __future__ import annotations

import csv
import io
import json
import os
import re
import statistics
import subprocess
import uuid
from datetime import datetime, timezone
from pathlib import Path

from aisdlc.context import Context
from aisdlc.errors import KitError

SCHEMA_VERSION = 1
TOOL_KINDS = (
    "repolens_search",
    "repolens_impact",
    "rg",
    "grep",
    "read",
    "shell",
)
ALIASES = {
    "search": "repolens_search",
    "impact": "repolens_impact",
    "ripgrep": "rg",
    "read": "read",
    "grep": "grep",
    "shell": "shell",
}
MIN_SAMPLE = 2
_VERSION = re.compile(r"\d+\.\d+(?:\.\d+)?")


def knowledge_backend(provider: str, env: dict[str, str] | None = None) -> str:
    """`AISDLC_KNOWLEDGE=none` disables the configured knowledge command."""
    raw = ((env if env is not None else os.environ).get("AISDLC_KNOWLEDGE") or "").strip().lower()
    if raw in {"none", "off", "0", "false"}:
        return "none"
    if raw:
        return raw
    return provider or "none"


def log_path(ctx: Context, env: dict[str, str] | None = None) -> Path:
    override = ((env if env is not None else os.environ).get("AISDLC_INTENTS_LOG") or "").strip()
    if override:
        return Path(override)
    return ctx.root / ".aisdlc" / "metrics" / "intents.jsonl"


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _append(path: Path, event: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    line = json.dumps(event, separators=(",", ":"), ensure_ascii=False)
    with path.open("a", encoding="utf-8") as handle:
        handle.write(line + "\n")


def read_events(path: Path) -> list[dict]:
    if not path.is_file():
        return []
    rows = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        try:
            item = json.loads(line)
        except json.JSONDecodeError:
            continue
        if isinstance(item, dict) and not item.get("example"):
            rows.append(item)
    return rows


def _parse_time(value: str | None) -> datetime | None:
    if not value:
        return None
    text = value.replace("Z", "+00:00")
    try:
        return datetime.fromisoformat(text)
    except ValueError:
        return None


def probe_version(command: str, args: tuple[str, ...] = ()) -> str | None:
    """Ask the knowledge CLI for its version. Missing or unparseable output is null."""
    try:
        done = subprocess.run(
            [command, *args, "--version"],
            capture_output=True,
            text=True,
            timeout=3,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return None
    match = _VERSION.search((done.stdout or "") + (done.stderr or ""))
    return match.group(0) if match else None


def normalize_kind(kind: str) -> str:
    key = kind.strip().lower()
    name = ALIASES.get(key, key)
    if name not in TOOL_KINDS:
        raise KitError("invalid_intent", f"unknown tool kind {kind!r}")
    return name


def start_intent(
    ctx: Context,
    *,
    intent_id: str,
    intent_type: str,
    issue: str | None = None,
    harness: str | None = None,
    provider: str | None = None,
    model: str | None = None,
    env: dict[str, str] | None = None,
) -> dict:
    if not intent_id.strip() or not intent_type.strip():
        raise KitError("invalid_intent", "intent id and type are required")
    environ = env if env is not None else os.environ
    backend = knowledge_backend(ctx.profile.knowledge.provider, environ)
    version = None
    if backend != "none":
        version = probe_version(ctx.profile.knowledge.command, ctx.profile.knowledge.args)
    event = {
        "schema_version": SCHEMA_VERSION,
        "event": "start",
        "run_id": uuid.uuid4().hex[:16],
        "intent_id": intent_id.strip(),
        "type": intent_type.strip(),
        "issue": issue,
        "pull_request": None,
        "harness": harness,
        "provider": provider,
        "model": model,
        "knowledge": backend,
        "knowledge_version": version,
        "started_at": _now(),
        "recorded_at": _now(),
        "example": False,
    }
    path = log_path(ctx, environ)
    _append(path, event)
    current = path.parent / "current-run"
    current.write_text(event["run_id"] + "\n", encoding="utf-8")
    return event


def record_tool(ctx: Context, run_id: str | None, kind: str, n: int = 1, env: dict[str, str] | None = None) -> dict:
    environ = env if env is not None else os.environ
    resolved = (run_id or environ.get("AISDLC_INTENT_RUN") or "").strip()
    if not resolved:
        current = log_path(ctx, environ).parent / "current-run"
        resolved = current.read_text(encoding="utf-8").strip() if current.is_file() else ""
    if not resolved:
        raise KitError("invalid_intent", "no open intent run (pass --run or set AISDLC_INTENT_RUN)")
    if n < 1:
        raise KitError("invalid_intent", "tool count must be >= 1")
    event = {
        "schema_version": SCHEMA_VERSION,
        "event": "tool",
        "run_id": resolved,
        "kind": normalize_kind(kind),
        "n": n,
        "recorded_at": _now(),
        "example": False,
    }
    _append(log_path(ctx, environ), event)
    return event


def _start_for(events: list[dict], run_id: str) -> dict | None:
    matches = [item for item in events if item.get("event") == "start" and item.get("run_id") == run_id]
    return matches[-1] if matches else None


def _tools_for(events: list[dict], run_id: str) -> dict:
    by_kind: dict[str, int] = {}
    for item in events:
        if item.get("event") != "tool" or item.get("run_id") != run_id:
            continue
        name = str(item.get("kind") or "")
        by_kind[name] = by_kind.get(name, 0) + int(item.get("n") or 1)
    return {"total": sum(by_kind.values()), "by_kind": by_kind}


def usage_tool_counts(path: Path, started_at: str | None, ended_at: str | None) -> dict[str, int]:
    """Count a RepoLens-style usage JSONL between two timestamps. Missing file is zero."""
    if not path.is_file():
        return {}
    start = _parse_time(started_at)
    end = _parse_time(ended_at)
    counts: dict[str, int] = {}
    mapping = {
        "search": "repolens_search",
        "impact": "repolens_impact",
        "grep": "grep",
        "rg": "rg",
        "read": "read",
    }
    for line in path.read_text(encoding="utf-8").splitlines():
        try:
            item = json.loads(line)
        except json.JSONDecodeError:
            continue
        if not isinstance(item, dict):
            continue
        stamp = _parse_time(str(item.get("ts") or "")) if not isinstance(item.get("ts"), (int, float)) else None
        if isinstance(item.get("ts"), (int, float)):
            stamp = datetime.fromtimestamp(float(item["ts"]), tz=timezone.utc)
        if start and stamp and stamp < start:
            continue
        if end and stamp and stamp > end:
            continue
        tool = str(item.get("tool") or "").lower()
        kind = mapping.get(tool)
        if kind:
            counts[kind] = counts.get(kind, 0) + 1
    return counts


def cost_usd(prices: dict, model: str | None, input_tokens: int | None, output_tokens: int | None, reported: float | None) -> float | None:
    if reported is not None:
        return float(reported)
    if model is None or input_tokens is None or output_tokens is None:
        return None
    row = prices.get(model) if isinstance(prices, dict) else None
    if not isinstance(row, dict):
        return None
    try:
        per_in = float(row["input"])
        per_out = float(row["output"])
    except (KeyError, TypeError, ValueError):
        return None
    return (input_tokens * per_in + output_tokens * per_out) / 1_000_000


def end_intent(
    ctx: Context,
    run_id: str,
    *,
    outcome: str,
    input_tokens: int | None = None,
    output_tokens: int | None = None,
    cached_tokens: int | None = None,
    reported_cost: float | None = None,
    files: list[str] | None = None,
    commit: str | None = None,
    pull_request: str | None = None,
    preflight_restarts: int = 0,
    retries: int = 0,
    usage_log: Path | None = None,
    env: dict[str, str] | None = None,
) -> dict:
    if outcome not in {"success", "fail"}:
        raise KitError("invalid_intent", "outcome must be success or fail")
    environ = env if env is not None else os.environ
    path = log_path(ctx, environ)
    events = read_events(path)
    started = _start_for(events, run_id)
    if started is None:
        raise KitError("not_found", f"intent run {run_id} was not started")
    ended = _now()
    start_at = _parse_time(started.get("started_at"))
    duration = None
    if start_at is not None:
        duration = max(0.0, (datetime.now(timezone.utc) - start_at).total_seconds())
    tools = _tools_for(events, run_id)
    if started.get("knowledge") != "none":
        usage = usage_log if usage_log is not None else ctx.root / ".repolens" / "usage.jsonl"
        for kind, count in usage_tool_counts(usage, started.get("started_at"), ended).items():
            tools["by_kind"][kind] = tools["by_kind"].get(kind, 0) + count
        tools["total"] = sum(tools["by_kind"].values())
    event = {
        "schema_version": SCHEMA_VERSION,
        "event": "end",
        "run_id": run_id,
        "intent_id": started["intent_id"],
        "type": started["type"],
        "issue": started.get("issue"),
        "pull_request": pull_request,
        "harness": started.get("harness"),
        "provider": started.get("provider"),
        "model": started.get("model"),
        "knowledge": started.get("knowledge"),
        "knowledge_version": started.get("knowledge_version"),
        "started_at": started.get("started_at"),
        "ended_at": ended,
        "duration_s": duration,
        "tool_calls": tools,
        "input_tokens": input_tokens,
        "output_tokens": output_tokens,
        "cached_tokens": cached_tokens,
        "cost_usd": cost_usd(ctx.profile.prices, started.get("model"), input_tokens, output_tokens, reported_cost),
        "outcome": outcome,
        "preflight_restarts": preflight_restarts,
        "retries": retries,
        "files_touched": files or [],
        "commit": commit,
        "example": False,
        "recorded_at": ended,
    }
    _append(path, event)
    return event


def finished_runs(events: list[dict]) -> list[dict]:
    return [item for item in events if item.get("event") == "end" and not item.get("example")]


def summary(events: list[dict]) -> dict:
    done = finished_runs(events)
    by_knowledge: dict[str, int] = {}
    for item in done:
        key = str(item.get("knowledge") or "none")
        by_knowledge[key] = by_knowledge.get(key, 0) + 1
    return {"runs": len(done), "by_knowledge": by_knowledge, "open": sum(1 for item in events if item.get("event") == "start")}


def _stats(values: list[float]) -> dict:
    if not values:
        return {"n": 0, "mean": None, "median": None}
    return {"n": len(values), "mean": statistics.fmean(values), "median": statistics.median(values)}


def _metric_values(rows: list[dict], name: str) -> list[float]:
    values = []
    for row in rows:
        if name == "tool_calls":
            raw = (row.get("tool_calls") or {}).get("total")
        elif name == "duration_s":
            raw = row.get("duration_s")
        else:
            raw = row.get(name)
        if isinstance(raw, (int, float)):
            values.append(float(raw))
    return values


def compare(events: list[dict], side_a: str, side_b: str, intent_id: str | None = None) -> dict:
    """Paired summary of finished runs. `side_*` is `knowledge=<name>`."""
    def select(spec: str) -> list[dict]:
        if not spec.startswith("knowledge="):
            raise KitError("invalid_intent", "compare filters look like knowledge=repolens")
        backend = spec.split("=", 1)[1]
        rows = [item for item in finished_runs(events) if item.get("knowledge") == backend]
        if intent_id:
            rows = [item for item in rows if item.get("intent_id") == intent_id]
        return rows

    left, right = select(side_a), select(side_b)
    metrics = {}
    warnings = []
    for name in ("duration_s", "tool_calls", "input_tokens", "output_tokens", "cached_tokens", "cost_usd"):
        a_stats = _stats(_metric_values(left, name))
        b_stats = _stats(_metric_values(right, name))
        delta = None
        if a_stats["median"] is not None and b_stats["median"] is not None:
            delta = b_stats["median"] - a_stats["median"]
        if a_stats["n"] < MIN_SAMPLE or b_stats["n"] < MIN_SAMPLE:
            warnings.append(f"insufficient sample for {name}: n={a_stats['n']} vs {b_stats['n']} (need {MIN_SAMPLE} each)")
        metrics[name] = {"a": a_stats, "b": b_stats, "delta_median_b_minus_a": delta}
    return {
        "a": side_a,
        "b": side_b,
        "intent_id": intent_id,
        "runs_a": len(left),
        "runs_b": len(right),
        "metrics": metrics,
        "warnings": warnings,
    }


def export_csv(events: list[dict]) -> str:
    buffer = io.StringIO()
    fields = [
        "run_id", "intent_id", "type", "knowledge", "knowledge_version", "outcome",
        "duration_s", "input_tokens", "output_tokens", "cached_tokens", "cost_usd",
        "tool_calls_total", "issue", "pull_request", "commit",
    ]
    writer = csv.DictWriter(buffer, fieldnames=fields)
    writer.writeheader()
    for item in finished_runs(events):
        writer.writerow({
            "run_id": item.get("run_id"),
            "intent_id": item.get("intent_id"),
            "type": item.get("type"),
            "knowledge": item.get("knowledge"),
            "knowledge_version": item.get("knowledge_version"),
            "outcome": item.get("outcome"),
            "duration_s": item.get("duration_s"),
            "input_tokens": item.get("input_tokens"),
            "output_tokens": item.get("output_tokens"),
            "cached_tokens": item.get("cached_tokens"),
            "cost_usd": item.get("cost_usd"),
            "tool_calls_total": (item.get("tool_calls") or {}).get("total"),
            "issue": item.get("issue"),
            "pull_request": item.get("pull_request"),
            "commit": item.get("commit"),
        })
    return buffer.getvalue()


def run_command(ctx: Context, argv: list[str], *, intent_id: str, intent_type: str, issue: str | None, env: dict[str, str] | None = None) -> tuple[int, dict]:
    """Start an intent, run argv, and append the end event. The child sees AISDLC_INTENT_RUN."""
    if not argv:
        raise KitError("invalid_intent", "intent run needs a command after --")
    started = start_intent(ctx, intent_id=intent_id, intent_type=intent_type, issue=issue, env=env)
    child_env = dict(os.environ if env is None else env)
    child_env["AISDLC_INTENT_RUN"] = started["run_id"]
    try:
        done = subprocess.run(argv, cwd=ctx.root, env=child_env, check=False)
        code = done.returncode
    except OSError as exc:
        code = 127
        print(f"error: {exc}")
    outcome = "success" if code == 0 else "fail"
    files = _git_files(ctx.root)
    commit = _git_head(ctx.root)
    finished = end_intent(
        ctx, started["run_id"], outcome=outcome, files=files, commit=commit, env=child_env,
    )
    return code, finished


def _git_files(root: Path) -> list[str]:
    try:
        done = subprocess.run(
            ["git", "diff", "--name-only", "HEAD"], cwd=root, capture_output=True, text=True, timeout=5, check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return []
    if done.returncode != 0:
        return []
    return [line for line in done.stdout.splitlines() if line.strip()]


def _git_head(root: Path) -> str | None:
    try:
        done = subprocess.run(
            ["git", "rev-parse", "HEAD"], cwd=root, capture_output=True, text=True, timeout=5, check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return None
    text = done.stdout.strip()
    return text or None
