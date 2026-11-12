"""Append-only evidence: one JSON line per stage run, one per cost entry, and a report over both.

Agents' claims are never evidence; these records are written by the kit from real exit codes.
Each record carries `prev`, the SHA-256 of the line before it ("genesis" for the first), so an
edited, deleted or reordered record breaks the chain and `aisdlc evidence verify` reports where.
A chain alone cannot see a truncated tail or an emptied file: any prefix of a valid chain is
valid. So the verifier prints the head digest, the Foreman anchors it where agents cannot rewrite
it (a PR body, a commit message, a release note), and `verify --anchor <digest>` later fails
unless a record with that digest is still in the ledger. An empty or missing ledger is never
reported as verified.
"""
from __future__ import annotations

import datetime as dt
import hashlib
import json
import statistics
from collections import defaultdict
from pathlib import Path

from aisdlc.context import Context
from aisdlc.metrics import stage_durations
from aisdlc.telemetry import get_telemetry, record_cost_event, record_stage_event

LEDGER = "ledger.jsonl"
COSTS = "cost.jsonl"
GENESIS = "genesis"


def now() -> str:
    return dt.datetime.now(dt.timezone.utc).isoformat(timespec="seconds")


def _append(ctx: Context, name: str, record: dict) -> Path:
    folder = ctx.path(ctx.profile.evidence_dir)
    folder.mkdir(parents=True, exist_ok=True)
    path = folder / name
    record = {**record, "prev": _digest(_last_line(path))}
    with path.open("a", encoding="utf-8") as handle:
        handle.write(json.dumps(record, sort_keys=True) + "\n")
    return path


def _digest(line: str | None) -> str:
    return GENESIS if line is None else hashlib.sha256(line.encode("utf-8")).hexdigest()


def _last_line(path: Path) -> str | None:
    if not path.is_file():
        return None
    lines = [line for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    return lines[-1] if lines else None


def verify_chain(lines: list[str]) -> tuple[int, int, list[str]]:
    """(chained records, legacy records, problems). Records written before the chain existed carry
    no `prev`; they are accepted only as a prefix, never after the first chained record."""
    chained, legacy, problems, previous = 0, 0, [], None
    for number, line in enumerate(lines, 1):
        try:
            record = json.loads(line)
        except json.JSONDecodeError:
            problems.append(f"line {number}: not valid JSON")
            previous = line
            continue
        if "prev" not in record:
            if chained:
                problems.append(f"line {number}: record without prev after the chain started")
            else:
                legacy += 1
        else:
            chained += 1
            if record["prev"] != _digest(previous):
                problems.append(f"line {number}: prev does not match the line before it "
                                f"(edited, deleted or reordered record)")
        previous = line
    return chained, legacy, problems


def verify(ctx: Context, anchors: list[str] | None = None) -> int:
    folder = ctx.path(ctx.profile.evidence_dir)
    anchors = list(anchors or [])
    failed, digests, records = False, set(), 0
    for name in (LEDGER, COSTS):
        path = folder / name
        if not path.is_file():
            print(f"  -     {name}: no records")
            continue
        lines = [line for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
        records += len(lines)
        digests.update(_digest(line) for line in lines)
        chained, legacy, problems = verify_chain(lines)
        if problems:
            failed = True
            print(f"  FAIL  {name}: {len(problems)} break(s) in the chain")
            for problem in problems:
                print(f"        {problem}")
        else:
            print(f"  PASS  {name}: {chained} chained record(s), {legacy} legacy; head {_digest(lines[-1] if lines else None)}")
    for anchor in anchors:
        if anchor in digests:
            print(f"  PASS  anchor {anchor[:12]}… is still in the ledger")
        else:
            failed = True
            print(f"  FAIL  anchor {anchor[:12]}… is gone: the ledger was truncated, emptied or replaced")
    if failed:
        print("evidence: FAILED — the ledger was altered after it was written")
        return 1
    if records == 0:
        print("evidence: NO EVIDENCE — nothing to verify (an empty ledger is not a verified one)")
        return 0
    print("evidence: PASSED")
    return 0


def _read(ctx: Context, name: str, since: str | None) -> list[dict]:
    path = ctx.path(ctx.profile.evidence_dir) / name
    if not path.is_file():
        return []
    records = [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    return [r for r in records if since is None or r.get("ts", "") >= since]


def record_stage(ctx: Context, record: dict) -> Path:
    path = _append(ctx, LEDGER, record)
    seconds = sum(float(g.get("seconds") or 0) for g in record.get("gates", []))
    record_stage_event(
        get_telemetry(),
        stage=str(record.get("stage", "")),
        ok=bool(record.get("ok")),
        seconds=seconds,
    )
    return path


def append_record(ctx: Context, record: dict) -> Path:
    """Append an arbitrary kit-authored ledger row (e.g. migrate evidence)."""
    payload = dict(record)
    payload.setdefault("ts", now())
    return _append(ctx, LEDGER, payload)


def add_cost(ctx: Context, agent: str, model: str, tokens_in: int, tokens_out: int,
             usd: float | None, issue: str | None) -> int:
    price = ctx.profile.prices.get(model)
    if usd is None and price:
        usd = round(tokens_in / 1e6 * price["input"] + tokens_out / 1e6 * price["output"], 6)
    _append(ctx, COSTS, {"ts": now(), "agent": agent, "model": model, "input": tokens_in,
                         "output": tokens_out, "usd": usd, "issue": issue})
    record_cost_event(
        get_telemetry(),
        agent=agent,
        model=model,
        tokens_in=tokens_in,
        tokens_out=tokens_out,
        usd=usd,
    )
    print(f"cost recorded: {agent} · {model} · {tokens_in}+{tokens_out} tokens · "
          f"{'unpriced' if usd is None else f'${usd:.4f}'}")
    return 0


def ledger_records(ctx: Context, since: str | None = None) -> list[dict]:
    """Public read of evidence ledger rows (same source as ``aisdlc report``)."""
    return _read(ctx, LEDGER, since)


def summarize(ctx: Context, since: str | None = None) -> dict:
    runs = _read(ctx, LEDGER, since)
    per_gate: dict[tuple[str, str], list[dict]] = defaultdict(list)
    for run in runs:
        for gate in run.get("gates", []):
            per_gate[(run["stage"], gate["name"])].append({**gate, "ts": run["ts"]})
    gates = [_gate_summary(stage, name, results) for (stage, name), results in sorted(per_gate.items())]
    return {
        "runs": len(runs),
        "failed_runs": sum(1 for r in runs if not r.get("ok")),
        "gates": gates,
        "stages": stage_durations(runs),
        "cost": _cost_summary(_read(ctx, COSTS, since)),
        "lead_time_event_pair": {
            "start": "issue.created_at",
            "end": "pull_request.checks_green_at",
        },
    }


def report_summary(ctx: Context, since: str | None = None) -> dict:
    """Same payload shape as ``aisdlc report --json`` (evidence + autonomy + profile).

    Backoffice Runs module reuses this so measure/tune hooks stay on TelemetryPort /
    evidence/cost — no parallel metrics store (#2241).
    """
    data = summarize(ctx, since)
    return _with_autonomy(ctx, {**data, "profile": ctx.profile.name}, since)


def _gate_summary(stage: str, name: str, results: list[dict]) -> dict:
    count = {s: sum(1 for r in results if r["status"] == s) for s in ("pass", "fail", "skip", "todo")}
    judged = count["pass"] + count["fail"]
    durations = [r["seconds"] for r in results if r["status"] in ("pass", "fail")]
    return {
        "stage": stage, "gate": name, "runs": len(results), **count,
        "pass_rate": round(100.0 * count["pass"] / judged, 1) if judged else None,
        "median_seconds": round(statistics.median(durations), 2) if durations else None,
        "last_status": results[-1]["status"], "last_ts": results[-1]["ts"],
    }


def _cost_summary(entries: list[dict]) -> dict:
    by_model: dict[str, dict] = defaultdict(lambda: {"entries": 0, "input": 0, "output": 0, "usd": 0.0})
    by_agent: dict[str, float] = defaultdict(float)
    for entry in entries:
        bucket = by_model[entry["model"]]
        bucket["entries"] += 1
        bucket["input"] += entry["input"]
        bucket["output"] += entry["output"]
        bucket["usd"] += entry["usd"] or 0.0
        by_agent[entry["agent"]] += entry["usd"] or 0.0
    return {
        "total_usd": round(sum(b["usd"] for b in by_model.values()), 6),
        "unpriced_entries": sum(1 for e in entries if e["usd"] is None),
        "by_model": dict(by_model),
        "by_agent": dict(by_agent),
    }


def _with_autonomy(ctx: Context, data: dict, since: str | None) -> dict:
    """Attach Wilson L0 from allowlisted stages without importing improve into summarize (#2165/#2171)."""
    from aisdlc.improve import autonomy_from_runs

    runs = _read(ctx, LEDGER, since)
    return {**data, "autonomy": autonomy_from_runs(runs)}


def report(
    ctx: Context,
    as_json: bool,
    since: str | None,
    html_path: Path | None = None,
    lead_time_fixture: Path | None = None,
) -> int:
    from aisdlc.report_html import write_monitor

    data = summarize(ctx, since)
    data = _with_autonomy(ctx, {**data, "profile": ctx.profile.name}, since)
    if html_path is not None:
        write_monitor(html_path, data, lead_time_fixture=lead_time_fixture)
        print(f"wrote HTML monitor: {html_path}")
        return 0
    if as_json:
        print(json.dumps(data, indent=2, sort_keys=True))
        return 0
    print(f"# AI SDLC evidence — {ctx.profile.name}\n")
    print(f"{data['runs']} stage runs, {data['failed_runs']} failed.\n")
    auto = data.get("autonomy") or {}
    print(
        f"Autonomy (Wilson L0 / allowlisted): status={auto.get('status')} "
        f"level={auto.get('level')} "
        f"detail={auto.get('detail')} "
        f"wilson_lower={auto.get('wilson_lower')} "
        f"({auto.get('successes')}/{auto.get('trials')})\n"
    )
    print("| Stage | Gate | Runs | Pass rate | Median s | Last |")
    print("|-------|------|-----:|----------:|---------:|------|")
    for g in data["gates"]:
        rate = "—" if g["pass_rate"] is None else f"{g['pass_rate']:.1f}%"
        median = "—" if g["median_seconds"] is None else f"{g['median_seconds']:.1f}"
        print(f"| {g['stage']} | {g['gate']} | {g['runs']} | {rate} | {median} | {g['last_status']} |")
    if data.get("stages"):
        print("\n| Stage | Runs | OK | Median s | Total s |")
        print("|-------|-----:|---:|---------:|--------:|")
        for s in data["stages"]:
            print(
                f"| {s['stage']} | {s['runs']} | {s['ok_runs']} | "
                f"{s['median_seconds']:.1f} | {s['total_seconds']:.1f} |"
            )
    pair = data.get("lead_time_event_pair") or {}
    if pair:
        print(
            f"\nLead time event pair: `{pair.get('start')}` → `{pair.get('end')}` "
            "(see `aisdlc metrics lead-time --fixture`)."
        )
    cost = data["cost"]
    if cost["by_model"]:
        print("\n| Model | Entries | Input tokens | Output tokens | USD |")
        print("|-------|--------:|-------------:|--------------:|----:|")
        for model, b in sorted(cost["by_model"].items()):
            print(f"| {model} | {b['entries']} | {b['input']} | {b['output']} | {b['usd']:.4f} |")
        print(f"\nTotal: ${cost['total_usd']:.4f} ({cost['unpriced_entries']} unpriced entries)")
    return 0
