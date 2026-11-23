"""Attribute real token usage to a run's steps from worker logs (#2447).

Two sources, read as counts only (never prompts or text):

- ``junie``: each ``log-*.log`` under a directory is one Junie invocation. Its last
  ``llm_cost_trajectory_snapshot`` holds the session totals and the provider-reported cost.
- ``claude-code``: Claude Code transcripts (``*.jsonl``) carry ``message.usage`` per request;
  a request spans several lines, so each ``requestId`` is counted once.

A session is attributed to the step whose window (from the evidence ledger) contains its start.
A Claude Code window is the named step's, an explicit ``since``/``until``, or the whole run.
Every imported row carries a ``key`` so importing the same source twice records nothing new.
"""
from __future__ import annotations

import json
import re
from collections import defaultdict
from datetime import datetime, timedelta
from pathlib import Path

from aisdlc import evidence, runs
from aisdlc.context import Context
from aisdlc.errors import KitError

SOURCES = ("junie", "claude-code")
_LINE_TS = re.compile(r"^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})")
_TOTAL_COST = re.compile(r"total_cost=([0-9.]+)")
# The task text names the issue a worker was given; a run id like issue-760 or issue-2395-r2 names it too.
_TASK_ISSUE = re.compile(r"(?:GitHub issue #|gh issue view )(\d+)")
_RUN_ISSUE = re.compile(r"(?:^|-)issue-(\d+)(?:-|$)")
_SLACK = timedelta(seconds=5)


def parse_ts(value: str) -> datetime:
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def _local(stamp: str) -> datetime:
    return datetime.strptime(stamp, "%Y-%m-%d %H:%M:%S").astimezone()


def junie_sessions(directory: Path) -> list[dict]:
    """One entry per Junie log: start, end, model, tokens and the reported cost."""
    sessions = []
    for log in sorted(directory.glob("log-*.log")):
        start = end = None
        snapshot: dict | None = None
        reported: float | None = None
        issue: str | None = None
        for line in log.read_text(encoding="utf-8", errors="replace").splitlines():
            named = issue is None and _TASK_ISSUE.search(line)
            if named:
                issue = named.group(1)
            stamp = _LINE_TS.match(line)
            if stamp:
                end = _local(stamp.group(1))
                start = start or end
            if "llm_cost_trajectory_snapshot" in line and "snapshot=" in line:
                try:
                    snapshot, _ = json.JSONDecoder().raw_decode(line[line.index("snapshot=") + len("snapshot="):])
                except json.JSONDecodeError:
                    continue
                cost = _TOTAL_COST.search(line)
                reported = float(cost.group(1)) if cost else None
        if not snapshot or start is None:
            continue
        totals = snapshot.get("totals") or {}
        groups = snapshot.get("attributedGroups") or [{}]
        sessions.append({
            "key": f"junie:{log.name}", "start": start, "end": end, "model": groups[0].get("modelName") or "junie",
            "input": int(totals.get("inputTokens") or 0), "output": int(totals.get("outputTokens") or 0),
            "cache_read": int(totals.get("cacheReadTokens") or 0), "usd": reported, "issue": issue,
        })
    return sessions


def claude_messages(path: Path) -> list[dict]:
    """One entry per Claude Code request with usage, deduplicated by request id."""
    files = [path] if path.is_file() else sorted(path.glob("*.jsonl"))
    seen: set[str] = set()
    messages = []
    for transcript in files:
        for line in transcript.read_text(encoding="utf-8", errors="replace").splitlines():
            try:
                record = json.loads(line)
            except json.JSONDecodeError:
                continue
            message = record.get("message") if isinstance(record, dict) else None
            usage = message.get("usage") if isinstance(message, dict) else None
            request = record.get("requestId") or (message or {}).get("id")
            if not usage or not request or request in seen or not record.get("timestamp"):
                continue
            seen.add(request)
            if not any(usage.get(k) for k in ("input_tokens", "output_tokens", "cache_read_input_tokens",
                                               "cache_creation_input_tokens")):
                continue
            split = usage.get("cache_creation") or {}
            messages.append({
                "request": request, "ts": parse_ts(record["timestamp"]), "model": message.get("model") or "claude",
                "input": int(usage.get("input_tokens") or 0), "output": int(usage.get("output_tokens") or 0),
                "cache_read": int(usage.get("cache_read_input_tokens") or 0),
                "cache_write": int(usage.get("cache_creation_input_tokens") or 0),
                "cache_write_1h": int(split.get("ephemeral_1h_input_tokens") or 0),
            })
    return messages


def step_windows(ctx: Context, run_id: str) -> list[dict]:
    """Each step attempt of a run: step id, start, end (from the ledger)."""
    open_steps: dict[str, datetime] = {}
    windows = []
    for event in runs.events_for_run(ctx, run_id):
        step = event.get("step")
        if not step or not event.get("ts"):
            continue
        if event["kind"] == "step.started":
            open_steps[step] = parse_ts(event["ts"])
        elif event["kind"] == "step.finished" and step in open_steps:
            windows.append({"step": step, "start": open_steps.pop(step), "end": parse_ts(event["ts"])})
    return windows


def run_window(ctx: Context, run_id: str) -> tuple[datetime, datetime]:
    stamps = [parse_ts(e["ts"]) for e in runs.events_for_run(ctx, run_id) if e.get("ts")]
    if not stamps:
        raise KitError("not_found", f"run {run_id!r} has no events")
    return min(stamps), max(stamps)


def _existing_keys(ctx: Context) -> set[str]:
    return {row["key"] for row in evidence.cost_rows(ctx) if row.get("key")}


def _import_junie(ctx: Context, path: Path, run_id: str, known: set[str]) -> list[dict]:
    windows = step_windows(ctx, run_id)
    run_issue = _RUN_ISSUE.search(run_id)
    rows = []
    for session in junie_sessions(path):
        if session["key"] in known:
            continue
        if session["issue"] and session["issue"] != (run_issue.group(1) if run_issue else None):
            continue
        step = next((w["step"] for w in windows if w["start"] - _SLACK <= session["start"] <= w["end"]), None)
        if step is None:
            continue
        rows.append(evidence.add_cost(
            ctx, "junie", session["model"], session["input"], session["output"], session["usd"], None,
            run=run_id, step=step, cache_read=session["cache_read"], source="junie", key=session["key"], quiet=True))
    return rows


def _window_for(ctx: Context, run_id: str, step: str | None, since: str | None,
                until: str | None) -> list[tuple[str, datetime, datetime]]:
    if since or until:
        start, end = run_window(ctx, run_id)
        return [(step or "foreman", parse_ts(since) if since else start, parse_ts(until) if until else end)]
    attempts = [w for w in step_windows(ctx, run_id) if step and w["step"] == step]
    if attempts:
        return [(step, w["start"], w["end"]) for w in attempts]
    start, end = run_window(ctx, run_id)
    return [(step or "foreman", start, end)]


def _import_claude(ctx: Context, path: Path, run_id: str, known: set[str], step: str | None,
                   since: str | None, until: str | None) -> list[dict]:
    messages = claude_messages(path)
    rows = []
    for name, start, end in _window_for(ctx, run_id, step, since, until):
        grouped: dict[str, list[dict]] = defaultdict(list)
        for message in messages:
            if start <= message["ts"] <= end:
                grouped[message["model"]].append(message)
        for model, items in sorted(grouped.items()):
            key = f"claude-code:{run_id}:{name}:{items[0]['request']}:{items[-1]['request']}:{len(items)}"
            if key in known:
                continue
            total = {k: sum(m[k] for m in items) for k in ("input", "output", "cache_read", "cache_write",
                                                            "cache_write_1h")}
            rows.append(evidence.add_cost(
                ctx, "foreman", model, total["input"], total["output"], None, None, run=run_id, step=name,
                cache_read=total["cache_read"], cache_write=total["cache_write"],
                cache_write_1h=total["cache_write_1h"], source="claude-code", key=key, quiet=True))
    return rows


def import_all_runs(ctx: Context, path: Path) -> dict:
    """Attribute every Claude Code request between the first and last run event to the runs.

    A request inside one run's window goes to that run; inside several overlapping windows it is
    split evenly between them (``allocation: split``); between runs it is factory overhead
    (``run: null``, ``step: overhead``), so overall cost stays complete.
    """
    spans = {}
    for run in runs.list_runs(ctx)["entries"]:
        try:
            spans[run["id"]] = run_window(ctx, run["id"])
        except KitError:
            continue
    if not spans:
        return {"run": None, "source": "claude-code", "recorded": 0, "rows": []}
    first, last = min(s for s, _ in spans.values()), max(e for _, e in spans.values())
    kinds = ("input", "output", "cache_read", "cache_write", "cache_write_1h")
    buckets: dict[tuple[str | None, str], dict] = {}
    for message in claude_messages(path):
        if not first <= message["ts"] <= last:
            continue
        owners = [run_id for run_id, (start, end) in spans.items() if start <= message["ts"] <= end] or [None]
        for owner in owners:
            bucket = buckets.setdefault((owner, message["model"]), {k: 0.0 for k in kinds} | {
                "requests": [], "split": False})
            for kind in kinds:
                bucket[kind] += message[kind] / len(owners)
            bucket["requests"].append(message["request"])
            bucket["split"] = bucket["split"] or len(owners) > 1
    known = _existing_keys(ctx)
    rows = []
    for (owner, model), bucket in sorted(buckets.items(), key=lambda item: (item[0][0] or "", item[0][1])):
        requests = bucket["requests"]
        key = f"claude-code:all:{owner or 'overhead'}:{model}:{requests[0]}:{requests[-1]}:{len(requests)}"
        if key in known:
            continue
        total = {kind: round(bucket[kind]) for kind in kinds}
        row = evidence.add_cost(
            ctx, "foreman", model, total["input"], total["output"], None, None, run=owner,
            step="foreman" if owner else "overhead", cache_read=total["cache_read"],
            cache_write=total["cache_write"], cache_write_1h=total["cache_write_1h"], source="claude-code",
            key=key, quiet=True)
        rows.append(row)
    return {"run": None, "source": "claude-code", "recorded": len(rows), "rows": rows}


def import_usage(ctx: Context, *, source: str, path: Path, run_id: str, step: str | None = None,
                 since: str | None = None, until: str | None = None) -> dict:
    if source not in SOURCES:
        raise KitError("invalid_body", f"source must be one of {', '.join(SOURCES)}")
    if not path.exists():
        raise KitError("not_found", f"{path} does not exist")
    runs.show(ctx, run_id)
    known = _existing_keys(ctx)
    if source == "junie":
        rows = _import_junie(ctx, path if path.is_dir() else path.parent, run_id, known)
    else:
        rows = _import_claude(ctx, path, run_id, known, step, since, until)
    return {"run": run_id, "source": source, "recorded": len(rows), "rows": rows}
