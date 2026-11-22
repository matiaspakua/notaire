"""Telemetry store: sync, record, report, stats (#2394).

Mirrors evidence ledger, cost log, and RepoLens usage into the SQLite store.
Redacts secrets before insert. Sync is idempotent via source + SHA-256 key
stored in the dedicated ``sync_keys`` table.
"""
from __future__ import annotations

import contextlib
import hashlib
import json
import sqlite3
import sys
from datetime import datetime
from typing import Any

from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.store import open_store, store_path
from aisdlc.telemetry import redact_attributes

VALID_GROUPS = ("intent", "stage", "agent", "skill", "tool")


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _line_hash(line: str) -> str:
    return hashlib.sha256(line.encode("utf-8")).hexdigest()


def _record_key(source: str, line: str) -> str:
    return f"{source}:{_line_hash(line)}"


def _mark_key(conn: sqlite3.Connection, source: str, line: str) -> bool:
    """INSERT OR IGNORE into sync_keys; returns True when newly inserted."""
    cur = conn.execute(
        "INSERT OR IGNORE INTO sync_keys (source, hash) VALUES (?, ?)",
        (source, _line_hash(line)),
    )
    return cur.rowcount > 0


def _is_new_key(conn: sqlite3.Connection, source: str, line: str) -> bool:
    row = conn.execute(
        "SELECT 1 FROM sync_keys WHERE source=? AND hash=?",
        (source, _line_hash(line)),
    ).fetchone()
    return row is None


# ---------------------------------------------------------------------------
# import_line  –  one raw JSONL line  ->  store rows
# ---------------------------------------------------------------------------

def import_line(conn: sqlite3.Connection, source: str, line: str) -> bool:
    """Insert rows for one raw ledger/cost/repolens line.

    Returns ``False`` when the line was already imported (idempotent).
    Derives telemetry rows from real ledger shapes.
    """
    if not _is_new_key(conn, source, line):
        return False

    try:
        record = json.loads(line)
    except json.JSONDecodeError:
        _mark_key(conn, source, line)
        return True

    clean = redact_attributes(dict(record))

    if source == "ledger":
        _import_ledger_line(conn, clean)
    elif source == "cost":
        _import_cost_line(conn, clean)
    elif source == "repolens":
        _import_repolens_line(conn, clean)

    _mark_key(conn, source, line)
    return True


def _import_ledger_line(conn: sqlite3.Connection, record: dict[str, Any]) -> None:
    """Import a single ledger line into events + telemetry."""
    kind = record.get("kind")
    ts = record.get("ts")
    run_id = record.get("run_id")
    intent_id = record.get("intent_id")
    stage = record.get("stage")
    step = record.get("step")
    actor = record.get("actor") or record.get("adapter") or record.get("agent")
    ok_val = record.get("ok")

    # Derive ok from step_status when present
    if record.get("step_status") == "fail" and ok_val is not False:
        ok_val = False

    data_json = json.dumps(record, sort_keys=True)

    # Always write the events row
    conn.execute(
        "INSERT INTO events "
        "(ts, kind, run_id, intent_id, stage, step, actor, data) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        (ts, kind, run_id, intent_id, stage, step, actor, data_json),
    )

    # Derive telemetry rows from real ledger shapes
    if kind == "stage-run":
        # One telemetry row: duration_s = sum of gate seconds
        gates = record.get("gates") or []
        duration_s = sum(float(g.get("seconds") or 0) for g in gates)
        agent = record.get("agent") or actor
        conn.execute(
            "INSERT INTO telemetry (ts, run_id, stage, agent, metric, value, ok) "
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            (ts, run_id, stage, agent, "duration_s", duration_s,
             1 if ok_val else 0),
        )

    elif kind in ("step.started", "step.finished"):
        # For step.finished: compute duration from step.started
        if kind == "step.finished" and run_id and step:
            started_row = conn.execute(
                "SELECT ts FROM events WHERE run_id=? AND step=? AND kind='step.started'",
                (run_id, step),
            ).fetchone()
            duration_s = None
            if started_row and started_row[0] and ts:
                try:
                    t0 = datetime.fromisoformat(started_row[0])
                    t1 = datetime.fromisoformat(ts)
                    duration_s = (t1 - t0).total_seconds()
                except (ValueError, TypeError):
                    pass
            if duration_s is not None and duration_s >= 0:
                step_status = record.get("step_status", "")
                ok_telem = 0 if step_status == "fail" else 1
                agent = record.get("adapter") or actor
                conn.execute(
                    "INSERT INTO telemetry (ts, run_id, stage, step, agent, "
                    "metric, value, ok) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    (ts, run_id, stage or step, step, agent,
                     "duration_s", duration_s, ok_telem),
                )

        # Fill intent_id from run.request carrying intent/intent_id
        if kind == "run.request" and run_id:
            intent = record.get("intent") or record.get("intent_id")
            if intent:
                conn.execute(
                    "UPDATE events SET intent_id=? WHERE run_id=? AND intent_id IS NULL",
                    (intent, run_id),
                )
                conn.execute(
                    "UPDATE telemetry SET intent_id=? WHERE run_id=? AND intent_id IS NULL",
                    (intent, run_id),
                )

    elif kind == "run.request":
        # Fill intent_id if present
        intent = record.get("intent") or record.get("intent_id")
        if intent and run_id:
            conn.execute(
                "UPDATE events SET intent_id=? WHERE run_id=? AND intent_id IS NULL",
                (intent, run_id),
            )


def _import_cost_line(conn: sqlite3.Connection, record: dict[str, Any]) -> None:
    """Import a single cost line into telemetry."""
    agent = record.get("agent")
    model = record.get("model")
    ts = record.get("ts")
    for metric, key in [("usd", "usd"), ("tokens_in", "input"),
                        ("tokens_out", "output")]:
        value = record.get(key)
        if value is not None:
            conn.execute(
                "INSERT INTO telemetry (ts, agent, model, metric, value, ok) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (ts, agent, model, metric,
                 float(value) if value else 0.0, 1),
            )


def _import_repolens_line(conn: sqlite3.Connection, record: dict[str, Any]) -> None:
    """Import a single RepoLens line into telemetry only (no events rows).

    Uses ONLY contract metrics: duration_s (= ms/1000), tokens_out (= tokens).
    Drops results, bytes, input, files.
    """
    # Never store input or files
    record.pop("input", None)
    record.pop("files", None)

    tool = record.get("tool")
    ms = record.get("ms")
    tokens = record.get("tokens")
    ts = record.get("ts")

    if ms is not None:
        conn.execute(
            "INSERT INTO telemetry (ts, agent, tool, metric, value, ok) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (ts, "repolens", tool, "duration_s", float(ms) / 1000, 1),
        )
    if tokens is not None:
        conn.execute(
            "INSERT INTO telemetry (ts, agent, tool, metric, value, ok) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (ts, "repolens", tool, "tokens_out", float(tokens), 1),
        )


# ---------------------------------------------------------------------------
# mirror_line  –  called from evidence._append (dual-write choke point)
# ---------------------------------------------------------------------------

def mirror_line(ctx: Context, source: str, line: str) -> None:
    """Mirror one ledger/cost line into the store (fail-open)."""
    try:
        conn = open_store(ctx.root)
        with contextlib.closing(conn):
            import_line(conn, source, line)
            conn.commit()
    except Exception as exc:  # noqa: BLE001 – fail-open by design
        print(f"aisdlc: store mirror failed: {exc}", file=sys.stderr)


# ---------------------------------------------------------------------------
# sync  –  split into one function per source
# ---------------------------------------------------------------------------

def sync(ctx: Context) -> dict[str, int]:
    """Import ledger, cost, and RepoLens rows. Returns counts per source."""
    conn = open_store(ctx.root)
    with contextlib.closing(conn):
        counts = {
            "ledger": _sync_ledger(conn, ctx),
            "cost": _sync_cost(conn, ctx),
            "repolens": _sync_repolens(conn, ctx),
        }
        conn.commit()
    return counts


def _sync_ledger(conn: sqlite3.Connection, ctx: Context) -> int:
    """Sync ledger.jsonl into the store."""
    evidence_dir = ctx.path(ctx.profile.evidence_dir)
    ledger_path = evidence_dir / "ledger.jsonl"
    if not ledger_path.is_file():
        return 0
    count = 0
    for raw in ledger_path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if line and import_line(conn, "ledger", line):
            count += 1
    return count


def _sync_cost(conn: sqlite3.Connection, ctx: Context) -> int:
    """Sync cost.jsonl into the store."""
    evidence_dir = ctx.path(ctx.profile.evidence_dir)
    cost_path = evidence_dir / "cost.jsonl"
    if not cost_path.is_file():
        return 0
    count = 0
    for raw in cost_path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if line and import_line(conn, "cost", line):
            count += 1
    return count


def _sync_repolens(conn: sqlite3.Connection, ctx: Context) -> int:
    """Sync .repolens/usage.jsonl into the store."""
    repolens_path = ctx.root / ".repolens" / "usage.jsonl"
    if not repolens_path.is_file():
        return 0
    count = 0
    for raw in repolens_path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if line and import_line(conn, "repolens", line):
            count += 1
    return count


# ---------------------------------------------------------------------------
# stats
# ---------------------------------------------------------------------------

def stats(ctx: Context) -> dict[str, int]:
    """Return row counts per table. Does not create the store."""
    db = store_path(ctx.root)
    if not db.is_file():
        return {"events": 0, "telemetry": 0, "benchmarks": 0,
                "config_snapshots": 0, "intents": 0}
    conn = sqlite3.connect(str(db))
    with contextlib.closing(conn):
        result: dict[str, int] = {}
        for table in ("events", "telemetry", "benchmarks",
                       "config_snapshots", "intents"):
            try:
                row = conn.execute(
                    f"SELECT COUNT(*) FROM [{table}]"
                ).fetchone()
                result[table] = row[0] if row else 0
            except sqlite3.Error:
                result[table] = 0
        return result


# ---------------------------------------------------------------------------
# record
# ---------------------------------------------------------------------------

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

    conn = open_store(ctx.root)
    with contextlib.closing(conn):
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


# ---------------------------------------------------------------------------
# report
# ---------------------------------------------------------------------------

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
    with contextlib.closing(conn):
        col = "intent_id" if group == "intent" else group

        # Single grouped query with conditional aggregates
        grouped = conn.execute(
            f"SELECT {col} AS key, "
            "SUM(CASE WHEN metric='duration_s' THEN value ELSE 0 END) AS dur, "
            "SUM(CASE WHEN metric='usd' THEN value ELSE 0 END) AS usd, "
            "SUM(CASE WHEN ok=0 THEN 1 ELSE 0 END) AS fails, "
            "COUNT(DISTINCT CASE WHEN run_id IS NOT NULL THEN run_id END) AS runs "
            f"FROM telemetry WHERE {col} IS NOT NULL "
            f"GROUP BY {col}"
        ).fetchall()

        result_rows = []
        total_dur = 0.0
        total_usd = 0.0
        total_fail = 0
        total_runs = 0

        for row in grouped:
            key = row[0]
            if key is None:
                continue
            dur = row[1] or 0.0
            usd = row[2] or 0.0
            fails = row[3] or 0
            runs = row[4] or 0
            result_rows.append({
                "key": key,
                "runs": runs,
                "duration_s": round(dur, 4),
                "usd": round(usd, 6),
                "failures": fails,
            })
            total_dur += dur
            total_usd += usd
            total_fail += fails
            total_runs += runs

        # failures_by_integration: telemetry ok=0 grouped by COALESCE(agent, tool)
        fbi_rows = conn.execute(
            "SELECT COALESCE(agent, tool) AS integration, COUNT(*) AS cnt "
            "FROM telemetry WHERE ok=0 AND (agent IS NOT NULL OR tool IS NOT NULL) "
            "GROUP BY integration"
        ).fetchall()
        fbi: dict[str, int] = {}
        for integration, cnt in fbi_rows:
            if integration:
                fbi[integration] = cnt

        # repolens summary: telemetry rows where agent='repolens' and metric='duration_s'
        rp_rows = conn.execute(
            "SELECT COUNT(*) AS calls, "
            "COALESCE(SUM(value), 0) AS total_s, "
            "tool "
            "FROM telemetry WHERE agent='repolens' AND metric='duration_s' "
            "GROUP BY tool"
        ).fetchall()
        rp_calls = sum(r[0] for r in rp_rows)
        rp_total_ms = sum(r[1] for r in rp_rows) * 1000
        rp_avg = round(rp_total_ms / rp_calls, 2) if rp_calls else 0
        rp_by_tool: dict[str, int] = {}
        for calls, _total_s, tool in rp_rows:
            if tool:
                rp_by_tool[tool] = calls

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
