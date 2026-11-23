"""Answer three questions per intent, per run and overall (#2447).

1. Time: wall time (first to last run event), active time (sum of step attempts), time per
   stage, and corrections (failed step attempts).
2. Cost: tokens (input, output, cache read, cache write) and USD from the cost rows attributed to
   the run. USD is null when nothing is recorded, and ``fully_priced`` says whether every row has a
   price, so a missing price never reads as $0.
3. RepoLens: calls and tokens served inside each run's window (``.repolens/usage.jsonl``), plus
   the measured effect from the agent benchmarks (same model and tasks, with and without RepoLens)
   and the live A/B samples of ``metrics intents``, each with its sample size.
"""
from __future__ import annotations

import json
import statistics
from datetime import datetime, timezone
from pathlib import Path

from aisdlc import evidence, intent_model, intents, runs
from aisdlc.context import Context
from aisdlc.usage_import import parse_ts, step_windows

TOKEN_KINDS = ("input", "output", "cache_read", "cache_write")


def _round(value: float | None, digits: int = 1) -> float | None:
    return None if value is None else round(value, digits)


def _median(values: list[float]) -> float | None:
    return round(statistics.median(values), 2) if values else None


def _usage_events(path: Path | None) -> list[dict]:
    if not path or not path.is_file():
        return []
    events = []
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        try:
            event = json.loads(line)
        except json.JSONDecodeError:
            continue
        if isinstance(event, dict) and isinstance(event.get("ts"), (int, float)) and event.get("tool"):
            events.append(event)
    return events


def _repolens(events: list[dict]) -> dict:
    latencies = [float(e["ms"]) for e in events if isinstance(e.get("ms"), (int, float))]
    by_tool: dict[str, int] = {}
    by_client: dict[str, int] = {}
    for event in events:
        by_tool[event["tool"]] = by_tool.get(event["tool"], 0) + 1
        client = str(event.get("client") or "unknown")
        by_client[client] = by_client.get(client, 0) + 1
    searches = [e for e in events if e["tool"] == "search"]
    return {
        "calls": len(events),
        "tokens_served": sum(int(e.get("tokens") or 0) for e in events),
        "p50_ms": _median(latencies),
        "zero_result_rate": _round(100.0 * sum(1 for e in searches if not e.get("results")) / len(searches))
        if searches else None,
        "by_tool": by_tool,
        "by_client": by_client,
    }


def _cost(rows: list[dict]) -> dict:
    tokens = {kind: sum(int(r.get(kind) or 0) for r in rows) for kind in TOKEN_KINDS}
    priced = [r["usd"] for r in rows if r.get("usd") is not None]
    by_source: dict[str, dict] = {}
    for row in rows:
        key = f"{row.get('source') or row.get('agent')}:{row.get('model')}"
        bucket = by_source.setdefault(key, {"rows": 0, **{k: 0 for k in TOKEN_KINDS}, "usd": None})
        bucket["rows"] += 1
        for kind in TOKEN_KINDS:
            bucket[kind] += int(row.get(kind) or 0)
        if row.get("usd") is not None:
            bucket["usd"] = round((bucket["usd"] or 0.0) + row["usd"], 6)
    return {
        "tokens": tokens,
        "cost": {
            "usd": round(sum(priced), 6) if priced else None,
            "fully_priced": bool(rows) and len(priced) == len(rows),
            "rows": len(rows),
            "reported_rows": sum(1 for r in rows if r.get("usd_source") == "reported"),
            "by_source": by_source,
        },
    }


def _run_metrics(ctx: Context, run: dict, cost_rows: list[dict], usage: list[dict]) -> dict:
    events = [e for e in runs.events_for_run(ctx, run["id"]) if e.get("ts")]
    stamps = [parse_ts(e["ts"]) for e in events]
    start, end = (min(stamps), max(stamps)) if stamps else (None, None)
    stage_of = {s["id"]: s.get("stage") or s["id"] for s in run.get("steps") or [] if s.get("id")}
    by_stage: dict[str, float] = {}
    active = 0.0
    for window in step_windows(ctx, run["id"]):
        seconds = (window["end"] - window["start"]).total_seconds()
        active += seconds
        stage = stage_of.get(window["step"], window["step"])
        by_stage[stage] = round(by_stage.get(stage, 0.0) + seconds, 1)
    corrections = sum(1 for e in events if e["kind"] == "step.finished" and e.get("step_status") == "fail")
    in_window = [e for e in usage if start and start.timestamp() <= e["ts"] <= end.timestamp()]
    mine = [r for r in cost_rows if r.get("run") == run["id"]]
    return {
        "id": run["id"],
        "intent": run.get("intent"),
        "status": run.get("status"),
        "workflow": (run.get("workflow") or {}).get("id") if isinstance(run.get("workflow"), dict) else run.get("workflow"),
        "started_at": start.isoformat() if start else None,
        "finished_at": end.isoformat() if end else None,
        "time": {
            "wall_s": _round((end - start).total_seconds()) if start else None,
            "active_s": round(active, 1),
            "by_stage": by_stage,
            "corrections": corrections,
        },
        **_cost(mine),
        "repolens": _repolens(in_window),
    }


def _sum_runs(items: list[dict], cost_rows: list[dict], usage: list[dict]) -> dict:
    starts = [parse_ts(r["started_at"]) for r in items if r["started_at"]]
    ends = [parse_ts(r["finished_at"]) for r in items if r["finished_at"]]
    by_stage: dict[str, float] = {}
    for item in items:
        for stage, seconds in item["time"]["by_stage"].items():
            by_stage[stage] = round(by_stage.get(stage, 0.0) + seconds, 1)
    run_ids = {r["id"] for r in items}
    windows = [(parse_ts(r["started_at"]).timestamp(), parse_ts(r["finished_at"]).timestamp())
               for r in items if r["started_at"]]
    return {
        "time": {
            "wall_s": _round((max(ends) - min(starts)).total_seconds()) if starts and ends else None,
            "active_s": round(sum(r["time"]["active_s"] for r in items), 1),
            "by_stage": by_stage,
            "corrections": sum(r["time"]["corrections"] for r in items),
        },
        **_cost([r for r in cost_rows if r.get("run") in run_ids]),
        "repolens": _repolens([e for e in usage if any(a <= e["ts"] <= b for a, b in windows)]),
    }


def _change_pct(before: float | None, after: float | None) -> float | None:
    if not before or after is None:
        return None
    return round(100.0 * (after - before) / before, 1)


def bench_effect(bench_dir: Path | None) -> list[dict]:
    """Agent benchmark summaries: the same model and tasks with (graph) and without (baseline) RepoLens."""
    if not bench_dir or not bench_dir.is_dir():
        return []
    rows = []
    for path in sorted(bench_dir.glob("agent-*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            continue
        summary = data.get("summary") if isinstance(data, dict) else None
        base, graph = (summary or {}).get("baseline"), (summary or {}).get("graph")
        if not isinstance(base, dict) or not isinstance(graph, dict):
            continue
        rows.append({
            "file": path.name,
            "model": data.get("model"),
            "taskset": data.get("taskset"),
            "runs_per_arm": min(int(base.get("runs") or 0), int(graph.get("runs") or 0)),
            "baseline": {k: base.get(k) for k in ("recall", "tool_calls", "duration_s", "cost_usd", "input_tokens")},
            "repolens": {k: graph.get(k) for k in ("recall", "tool_calls", "duration_s", "cost_usd", "input_tokens")},
            "cost_usd_change_pct": _change_pct(base.get("cost_usd"), graph.get("cost_usd")),
            "input_tokens_change_pct": _change_pct(base.get("input_tokens"), graph.get("input_tokens")),
            "tool_calls_change_pct": _change_pct(base.get("tool_calls"), graph.get("tool_calls")),
            "duration_change_pct": _change_pct(base.get("duration_s"), graph.get("duration_s")),
            "recall_change": round(graph["recall"] - base["recall"], 3)
            if isinstance(base.get("recall"), (int, float)) and isinstance(graph.get("recall"), (int, float)) else None,
        })
    return rows


def _live_ab(ctx: Context) -> dict:
    events = intents.read_events(intents.log_path(ctx))
    result = intents.compare(events, "knowledge=repolens", "knowledge=none")
    return {"source": "aisdlc metrics intents (paired factory runs)", **result}


def build(ctx: Context, *, usage_path: Path | None = None, bench_dir: Path | None = None) -> dict:
    cost_rows = evidence.cost_rows(ctx)
    usage = _usage_events(usage_path)
    run_metrics = [_run_metrics(ctx, run, cost_rows, usage) for run in runs.list_runs(ctx)["entries"]]
    run_metrics.sort(key=lambda r: r["started_at"] or "")
    intent_rows = []
    for intent in intent_model.list_intents(ctx).get("entries", []):
        mine = [r for r in run_metrics if r["intent"] == intent["id"]]
        if not mine:
            continue
        intent_rows.append({"id": intent["id"], "title": intent.get("title"), "status": intent.get("status"),
                            "runs": [r["id"] for r in mine], **_sum_runs(mine, cost_rows, usage)})
    totals = _sum_runs(run_metrics, cost_rows, usage)
    everything = _cost(cost_rows)
    overhead = _cost([r for r in cost_rows if not r.get("run")])
    return {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "intents": intent_rows,
        "runs": run_metrics,
        "overall": {
            "runs": len(run_metrics),
            "intents": len(intent_rows),
            "succeeded": sum(1 for r in run_metrics if r["status"] == "succeeded"),
            "time": {"active_s": totals["time"]["active_s"], "corrections": totals["time"]["corrections"],
                     "by_stage": totals["time"]["by_stage"]},
            "tokens": everything["tokens"],
            "cost": everything["cost"],
            "attributed": {"tokens": totals["tokens"], "cost": totals["cost"]},
            "overhead": {"tokens": overhead["tokens"], "cost": overhead["cost"]},
            "repolens": _repolens(usage),
            "per_intent": {
                "median_wall_s": _median([i["time"]["wall_s"] for i in intent_rows if i["time"]["wall_s"] is not None]),
                "median_active_s": _median([i["time"]["active_s"] for i in intent_rows]),
                "median_usd": _median([i["cost"]["usd"] for i in intent_rows if i["cost"]["usd"] is not None]),
                "median_tokens": _median([sum(i["tokens"].values()) for i in intent_rows]),
            },
        },
        "repolens_effect": {"bench": bench_effect(bench_dir), "live_ab": _live_ab(ctx)},
    }
