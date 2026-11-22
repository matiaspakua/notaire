"""Live factory status derived from ledger evidence and telemetry (#2397).

Pure functions only — no module-level mutable state.
Opens the telemetry store read-only; never creates it.
"""
from __future__ import annotations

import contextlib
import datetime as dt
import sqlite3

from aisdlc.context import Context
from aisdlc.runs import derive_status, events_for_run, list_runs, show
from aisdlc.store import store_path


def _empty_shape() -> dict:
    return {
        "intent": None,
        "run": None,
        "workflow": None,
        "run_status": None,
        "stage": None,
        "step": None,
        "previous": [],
        "next": [],
        "updated_at": None,
    }


def _last_event_ts(events: list[dict]) -> str | None:
    """Return the ts of the last ledger event, or None."""
    for ev in reversed(events):
        ts = ev.get("ts")
        if ts:
            return str(ts)
    return None


def _pick_current_run(ctx: Context) -> dict | None:
    """Pick the current run: latest not-finished, else latest overall."""
    runs_result = list_runs(ctx)
    entries = runs_result.get("entries", [])
    if not entries:
        return None

    def sort_key(r: dict) -> str:
        events = events_for_run(ctx, r["id"])
        last = _last_event_ts(events)
        return last or r.get("requested_at", "")

    not_finished = [
        r for r in entries
        if r.get("status") in ("requested", "queued", "running", "blocked")
    ]
    candidates = not_finished if not_finished else entries
    candidates.sort(key=sort_key, reverse=True)
    return candidates[0] if candidates else None


def _intent_from_run(ctx: Context, run: dict) -> str | None:
    """Extract intent id from run.request ledger row or current.json."""
    events = events_for_run(ctx, run["id"])
    for ev in events:
        if ev.get("kind") == "run.request":
            intent = ev.get("intent") or ev.get("intent_id")
            if intent:
                return str(intent)
    current = run.get("intent") or run.get("intent_id")
    return str(current) if current else None


def _step_stage(step_id: str, events: list[dict], profile_stages: list[str]) -> str | None:
    """Derive stage for a step from its ledger events or profile."""
    for ev in events:
        if ev.get("step") == step_id and ev.get("stage"):
            return str(ev["stage"])
    if step_id in profile_stages:
        return step_id
    return None


def _parse_ts(ts: str | None) -> dt.datetime | None:
    if ts is None:
        return None
    try:
        return dt.datetime.fromisoformat(ts)
    except (ValueError, TypeError):
        return None


def _duration_s(started_at: str | None, finished_at: str | None) -> float | None:
    t0 = _parse_ts(started_at)
    t1 = _parse_ts(finished_at)
    if t0 and t1:
        return (t1 - t0).total_seconds()
    return None


def _usd_for_step(ctx: Context, run_id: str, step: str) -> float | None:
    """Sum USD telemetry for a run+step. Returns None when no rows."""
    db = store_path(ctx.root)
    if not db.is_file():
        return None
    try:
        with contextlib.closing(
            sqlite3.connect(f"file:{db}?mode=ro", uri=True)
        ) as conn:
            row = conn.execute(
                "SELECT SUM(value) FROM telemetry "
                "WHERE run_id=? AND step=? AND metric='usd'",
                (run_id, step),
            ).fetchone()
            total = row[0] if row and row[0] is not None else None
            return round(total, 6) if total is not None else None
    except sqlite3.Error:
        return None


def _build_previous(
    ctx: Context, run_id: str, events: list[dict], profile_stages: list[str],
) -> list[dict]:
    """Build the 'previous' list from completed steps."""
    started_map: dict[str, dict] = {}
    finished_map: dict[str, dict] = {}
    for ev in events:
        step_id = ev.get("step")
        if not step_id:
            continue
        kind = ev.get("kind")
        if kind == "step.started":
            started_map[step_id] = ev
        elif kind == "step.finished":
            finished_map[step_id] = ev

    previous: list[dict] = []
    for step_id, fin_ev in finished_map.items():
        start_ev = started_map.get(step_id)
        started_at = start_ev.get("ts") if start_ev else None
        finished_at = fin_ev.get("ts")
        step_status = str(fin_ev.get("step_status", "fail")).strip().casefold()
        if step_status == "pass" and fin_ev.get("skip"):
            step_status = "skip"
        stage = _step_stage(step_id, events, profile_stages)
        previous.append({
            "step": step_id,
            "stage": stage,
            "status": step_status,
            "started_at": started_at,
            "finished_at": finished_at,
            "duration_s": _duration_s(started_at, finished_at),
            "usd": _usd_for_step(ctx, run_id, step_id),
        })
    previous.sort(key=lambda p: p["started_at"] or "")
    return previous


def _find_in_flight_step(events: list[dict]) -> str | None:
    """Return the step id currently in-flight (started but not finished)."""
    started: set[str] = set()
    finished: set[str] = set()
    last_started: str | None = None
    for ev in events:
        kind = ev.get("kind")
        step_id = ev.get("step")
        if not step_id:
            continue
        if kind == "step.started":
            started.add(step_id)
            last_started = step_id
        elif kind == "step.finished":
            finished.add(step_id)
    in_flight = started - finished
    if len(in_flight) == 1:
        return in_flight.pop()
    if len(in_flight) > 1:
        return last_started
    return None


def _workflow_next(wf_steps: list[dict], done_ids: set[str]) -> list[dict]:
    """Pending workflow steps not yet started or finished."""
    items: list[dict] = []
    for ws in wf_steps:
        sid = ws.get("id")
        if not sid or sid in done_ids:
            continue
        stage = ws.get("stage")
        items.append({
            "step": sid,
            "stage": str(stage) if stage else None,
            "source": "workflow",
        })
    return items


def _line_next(
    profile_stages: list[str],
    current_stage: str | None,
    last_completed_stage: str | None,
    taken_ids: set[str],
) -> list[dict]:
    """Profile stages after the current/last completed stage."""
    items: list[dict] = []
    for ps in profile_stages:
        if ps in taken_ids:
            continue
        if current_stage and ps == current_stage:
            continue
        if last_completed_stage and last_completed_stage in profile_stages:
            last_idx = profile_stages.index(last_completed_stage)
            ps_idx = profile_stages.index(ps) if ps in profile_stages else -1
            if ps_idx >= 0 and ps_idx <= last_idx:
                continue
        items.append({
            "step": ps,
            "stage": ps,
            "source": "line",
        })
    return items


def _build_next(
    ctx: Context, run: dict, events: list[dict],
    profile_stages: list[str], previous: list[dict],
    current_step: str | None,
) -> list[dict]:
    """Build the 'next' list: workflow steps first, then profile stages."""
    run_view = show(ctx, run["id"])
    wf_steps = run_view.get("steps", [])

    started_ids: set[str] = set()
    for ev in events:
        step_id = ev.get("step")
        if step_id and ev.get("kind") == "step.started":
            started_ids.add(step_id)

    prev_ids = {p["step"] for p in previous}
    all_done = prev_ids | started_ids

    next_items = _workflow_next(wf_steps, all_done)
    next_step_ids = {n["step"] for n in next_items} | all_done

    current_stage = _step_stage(current_step, events, profile_stages) if current_step else None

    last_completed_stage = None
    if previous:
        last_completed_stage = previous[-1].get("stage")

    next_items.extend(
        _line_next(profile_stages, current_stage, last_completed_stage, next_step_ids)
    )
    return next_items


def status(ctx: Context) -> dict:
    """Derive live factory status from ledger evidence and telemetry."""
    profile_stages = ctx.profile.stages()
    run = _pick_current_run(ctx)
    if run is None:
        return _empty_shape()

    run_id = run["id"]
    events = events_for_run(ctx, run_id)
    run_status, _ = derive_status(events)

    intent = _intent_from_run(ctx, run)
    workflow = run.get("workflow")

    in_flight_step = _find_in_flight_step(events)
    # step and stage are non-null ONLY when run_status is 'running'
    if run_status == "running" and in_flight_step:
        step = in_flight_step
        stage = _step_stage(in_flight_step, events, profile_stages)
    else:
        step = None
        stage = None

    previous = _build_previous(ctx, run_id, events, profile_stages)
    next_steps = _build_next(ctx, run, events, profile_stages, previous, in_flight_step)

    updated_at = _last_event_ts(events)

    return {
        "intent": intent,
        "run": run_id,
        "workflow": workflow,
        "run_status": run_status,
        "stage": stage,
        "step": step,
        "previous": previous,
        "next": next_steps,
        "updated_at": updated_at,
    }
