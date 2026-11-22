"""Telemetry store: sync, record, report, stats (#2394).

Mirrors evidence ledger, cost log, and RepoLens usage into the SQLite store.
Redacts secrets before insert. Sync is idempotent via source + SHA-256 key.
"""
from __future__ import annotations

import hashlib
import json
import sqlite3
from typing import Any

from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.store import open_store, store_path
from aisdlc.telemetry import redact_attributes

VALID_GROUPS = ("intent", "stage", "agent", "skill", "tool")


def _conn(ctx: Context) -> sqlite3.Connection:
    return open_store(ctx.root)


def _line_hash(line: str) -> str:
    return hashlib.sha256(line.encode("utf-8")).hexdigest()


def _is_new_key(conn: sqlite3.Connection, seen: set[str], source: str,
                line: str) -> bool:
    h = _line_hash(line)
    key = f"{source}:{h}"
    if key in seen:
        return False
    # Check persistent store
    row = conn.execute(
        "SELECT 1 FROM events WHERE kind='sync-key' AND data=?",
        (key,),
    ).fetchone()
    if row is not None:
        seen.add(key)
        return False
    return True


def _mark_synced(conn: sqlite3.Connection, seen: set[str], source: str,
                 line: str) -> None:
    h = _line_hash(line)
    key = f"{source}:{h}"
    seen.add(key)
    conn.execute(
        "INSERT INTO events (kind, data) VALUES ('sync-key', ?)",
        (key,),
    )


def _redact_record(record: dict[str, Any]) -> dict[str, Any]:
    return redact_attributes(dict(record))


def sync(ctx: Context) -> dict[str, int]:
    """Import ledger, cost, and RepoLens rows. Returns counts per source."""
    conn = _conn(ctx)
    counts: dict[str, int] = {"ledger": 0, "cost": 0, "repolens": 0}
    seen: set[str] = set()
    evidence_dir = ctx.path(ctx.profile.evidence_dir)

    # Ledger
    ledger_path = evidence_dir / "ledger.jsonl"
    if ledger_path.is_file():
        for line in ledger_path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line:
                continue
            if not _is_new_key(conn, seen, "ledger", line):
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError:
                continue
            clean = _redact_record(record)
            # Derive ok from step_status when present
            if clean.get("step_status") == "fail" and clean.get("ok") is not False:
                clean["ok"] = False
            data_json = json.dumps(clean, sort_keys=True)
            conn.execute(
                "INSERT INTO events "
                "(ts, kind, run_id, intent_id, stage, step, actor, data) "
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                (clean.get("ts"), clean.get("kind"), clean.get("run_id"),
                 clean.get("intent_id"), clean.get("stage"), clean.get("step"),
                 clean.get("actor") or clean.get("adapter")
                 or clean.get("agent"),
                 data_json),
            )
            _mark_synced(conn, seen, "ledger", line)
            counts["ledger"] += 1

    # Cost
    cost_path = evidence_dir / "cost.jsonl"
    if cost_path.is_file():
        for line in cost_path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line:
                continue
            if not _is_new_key(conn, seen, "cost", line):
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError:
                continue
            clean = _redact_record(record)
            agent = clean.get("agent")
            model = clean.get("model")
            ts = clean.get("ts")
            for metric, key in [("usd", "usd"), ("tokens_in", "input"),
                                ("tokens_out", "output")]:
                value = clean.get(key)
                if value is not None:
                    conn.execute(
                        "INSERT INTO telemetry (ts, agent, model, metric, value, ok) "
                        "VALUES (?, ?, ?, ?, ?, ?)",
                        (ts, agent, model, metric, float(value) if value else 0.0, 1),
                    )
            _mark_synced(conn, seen, "cost", line)
            counts["cost"] += 1

    # RepoLens — store in telemetry table as tool-level metrics
    repolens_path = ctx.root / ".repolens" / "usage.jsonl"
    if repolens_path.is_file():
        for line in repolens_path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line:
                continue
            if not _is_new_key(conn, seen, "repolens", line):
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError:
                continue
            clean = _redact_record(record)
            # Never store input or files
            clean.pop("input", None)
            clean.pop("files", None)
            tool = clean.get("tool")
            ms = clean.get("ms")
            tokens = clean.get("tokens")
            results = clean.get("results")
            ts = clean.get("ts")
            # Store ms as duration_s metric
            if ms is not None:
                conn.execute(
                    "INSERT INTO telemetry (ts, tool, metric, value, ok) "
                    "VALUES (?, ?, ?, ?, ?)",
                    (ts, tool, "repolens_ms", float(ms), 1),
                )
            if tokens is not None:
                conn.execute(
                    "INSERT INTO telemetry (ts, tool, metric, value, ok) "
                    "VALUES (?, ?, ?, ?, ?)",
                    (ts, tool, "repolens_tokens", float(tokens), 1),
                )
            if results is not None:
                conn.execute(
                    "INSERT INTO telemetry (ts, tool, metric, value, ok) "
                    "VALUES (?, ?, ?, ?, ?)",
                    (ts, tool, "repolens_results", float(results), 1),
                )
            # Store src/client in events for traceability
            data_json = json.dumps({
                k: v for k, v in clean.items()
                if k in ("src", "client", "tool", "ts")
            }, sort_keys=True)
            conn.execute(
                "INSERT INTO events (ts, kind, actor, data) "
                "VALUES (?, ?, ?, ?)",
                (ts, "repolens-sync", tool, data_json),
            )
            _mark_synced(conn, seen, "repolens", line)
            counts["repolens"] += 1

    conn.commit()
    return counts


def stats(ctx: Context) -> dict[str, int]:
    """Return row counts per table. Does not create the store."""
    db = store_path(ctx.root)
    if not db.is_file():
        return {"events": 0, "telemetry": 0, "benchmarks": 0,
                "config_snapshots": 0, "intents": 0}
    conn = sqlite3.connect(str(db))
    result: dict[str, int] = {}
    for table in ("events", "telemetry", "benchmarks", "config_snapshots", "intents"):
        try:
            if table == "events":
                row = conn.execute(
                    "SELECT COUNT(*) FROM events WHERE kind != 'sync-key'"
                ).fetchone()
            else:
                row = conn.execute(f"SELECT COUNT(*) FROM [{table}]").fetchone()
            result[table] = row[0] if row else 0
        except Exception:
            result[table] = 0
    conn.close()
    return result


def record(
    ctx: Context,
    *,
    intent_id: str | None = None,
    run_id: str | None = None,
    stage: str | None = None,
    step: str | None = None,
    agent: str | None = None,
    model: str | None = None,
    skill: str | None = None,
    tool: str | None = None,
    metric: str,
    value: float,
    ok: bool = True,
    **kwargs: Any,
) -> None:
    """Insert a single telemetry row after redaction."""
    attrs = redact_attributes(kwargs)
    from aisdlc.evidence import now as evidence_now

    conn = _conn(ctx)
    conn.execute(
        "INSERT INTO telemetry (ts, intent_id, run_id, stage, step, agent, "
        "model, skill, tool, metric, value, ok) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        (evidence_now(), intent_id, run_id, stage, step, agent,
         model, skill, tool, metric, value, 1 if ok else 0),
    )
    if attrs:
        data_json = json.dumps(attrs, sort_keys=True)
        conn.execute(
            "INSERT INTO events (ts, kind, run_id, stage, step, actor, data) "
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            (evidence_now(), "telemetry-extra", run_id, stage, step, agent,
             data_json),
        )
    conn.commit()


def report(ctx: Context, group: str) -> dict:
    """Aggregate telemetry by group dimension. Does not create the store."""
    if group not in VALID_GROUPS:
        raise KitError("invalid_group", f"invalid group {group!r}; "
                           f"must be one of {', '.join(VALID_GROUPS)}")
    db = store_path(ctx.root)
    if not db.is_file():
        return {"group": group, "rows": [], "total_runs": 0,
                "total_duration_s": 0, "total_usd": 0, "total_failures": 0,
                "failures_by_integration": {}, "repolens": {}}
    conn = sqlite3.connect(str(db))

    col = "intent_id" if group == "intent" else group
    rows = conn.execute(
        f"SELECT DISTINCT {col} FROM telemetry WHERE {col} IS NOT NULL",
    ).fetchall()
    result_rows = []
    total_dur = 0.0
    total_usd = 0.0
    total_fail = 0
    total_runs = 0

    for (key,) in rows:
        if key is None:
            continue
        dur = conn.execute(
            "SELECT COALESCE(SUM(value), 0) FROM telemetry "
            f"WHERE {col}=? AND metric='duration_s'", (key,)
        ).fetchone()[0]
        usd = conn.execute(
            "SELECT COALESCE(SUM(value), 0) FROM telemetry "
            f"WHERE {col}=? AND metric='usd'", (key,)
        ).fetchone()[0]
        fails = conn.execute(
            "SELECT COUNT(*) FROM telemetry "
            f"WHERE {col}=? AND ok=0", (key,)
        ).fetchone()[0]
        run_count = conn.execute(
            f"SELECT COUNT(DISTINCT run_id) FROM telemetry "
            f"WHERE {col}=? AND run_id IS NOT NULL",
            (key,),
        ).fetchone()[0]
        rep_calls = conn.execute(
            "SELECT COUNT(*) FROM telemetry WHERE tool=? AND metric='repolens_ms'",
            (key,),
        ).fetchone()[0]
        rep_ms = conn.execute(
            "SELECT COALESCE(SUM(value), 0) FROM telemetry "
            "WHERE tool=? AND metric='repolens_ms'", (key,)
        ).fetchone()[0]
        result_rows.append({
            "key": key,
            "runs": run_count,
            "duration_s": round(dur, 4),
            "usd": round(usd, 6),
            "failures": fails,
            "repolens_calls": rep_calls,
            "repolens_ms": round(rep_ms, 2),
        })
        total_dur += dur
        total_usd += usd
        total_fail += fails
        total_runs += run_count

    # failures_by_integration: count step.finished with ok=false from events
    fbi: dict[str, int] = {}
    fail_evts = conn.execute(
        "SELECT actor, data FROM events WHERE kind='step.finished' AND data IS NOT NULL"
    ).fetchall()
    for actor, data_json in fail_evts:
        try:
            data = json.loads(data_json)
        except (json.JSONDecodeError, TypeError):
            continue
        if not data.get("ok", True):
            adapter = actor or ""
            if adapter:
                fbi[adapter] = fbi.get(adapter, 0) + 1
    # Also count from telemetry ok=0 grouped by agent
    fail_agents = conn.execute(
        "SELECT agent, COUNT(*) FROM telemetry WHERE ok=0 AND agent IS NOT NULL "
        "GROUP BY agent"
    ).fetchall()
    for agent, cnt in fail_agents:
        if agent:
            fbi[agent] = fbi.get(agent, 0) + cnt

    # repolens summary from telemetry table
    rp_calls = conn.execute(
        "SELECT COUNT(*) FROM telemetry WHERE metric='repolens_ms'"
    ).fetchone()[0]
    rp_total_ms = conn.execute(
        "SELECT COALESCE(SUM(value), 0) FROM telemetry WHERE metric='repolens_ms'"
    ).fetchone()[0]
    rp_avg = round(rp_total_ms / rp_calls, 2) if rp_calls else 0
    rp_by_tool: dict[str, int] = {}
    for (tool, cnt) in conn.execute(
        "SELECT tool, COUNT(*) FROM telemetry WHERE metric='repolens_ms' "
        "GROUP BY tool"
    ).fetchall():
        if tool:
            rp_by_tool[tool] = cnt

    conn.close()
    return {
        "group": group,
        "rows": result_rows,
        "total_runs": total_runs,
        "total_duration_s": round(total_dur, 4),
        "total_usd": round(total_usd, 6),
        "total_failures": total_fail,
        "failures_by_integration": fbi,
        "repolens": {
            "calls": rp_calls,
            "total_ms": round(rp_total_ms, 2),
            "avg_ms": rp_avg,
            "by_tool": rp_by_tool,
        },
    }


def dual_write_event(ctx: Context, kind: str, data: dict) -> None:
    """Write an event row to the store (called from runs.py hooks)."""
    try:
        conn = _conn(ctx.root)
        clean = redact_attributes(data)
        data_json = json.dumps(clean, sort_keys=True)
        conn.execute(
            "INSERT INTO events "
            "(ts, kind, run_id, intent_id, stage, step, actor, data) "
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            (clean.get("ts"), kind, clean.get("run_id"),
             clean.get("intent_id"), clean.get("stage"), clean.get("step"),
             clean.get("actor") or clean.get("adapter"), data_json),
        )
        conn.commit()
    except Exception:
        pass
