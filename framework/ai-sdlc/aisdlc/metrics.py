"""Factory delivery metrics: lead time (issue→PR green) and stage durations (#2138).

Event pair (documented, fixed):
  start = issue.created_at
  end   = pull_request.checks_green_at  (earliest linked PR whose required checks are green)

Merge is optional and is not required for this metric. Prefer TelemetryPort emission when
bound; otherwise JSON from `aisdlc metrics` / `aisdlc report`. Prompts, diffs and secrets
never appear on metric attributes.
"""
from __future__ import annotations

import datetime as dt
import json
import statistics
from pathlib import Path
from typing import Any

from aisdlc.telemetry import TelemetryPort, redact_attributes

EVENT_PAIR = {
    "start": "issue.created_at",
    "end": "pull_request.checks_green_at",
}


def redact_metric_attributes(attributes: dict[str, Any]) -> dict[str, Any]:
    return redact_attributes(attributes)


def parse_ts(value: str) -> dt.datetime:
    text = value.strip()
    if text.endswith("Z"):
        text = text[:-1] + "+00:00"
    return dt.datetime.fromisoformat(text)


def lead_time_from_sample(document: dict[str, Any]) -> dict[str, Any]:
    """Compute lead time from a forge sample/fixture (no live API call)."""
    sample = document.get("sample") or document
    issue = sample.get("issue") or {}
    issue_number = issue.get("number")
    created = issue.get("created_at")
    base = {
        "status": "no-evaluable",
        "issue": issue_number,
        "pull_request": None,
        "seconds": None,
        "start_event": EVENT_PAIR["start"],
        "end_event": EVENT_PAIR["end"],
        "start_at": created,
        "end_at": None,
    }
    if not created or issue_number is None:
        return base
    start = parse_ts(str(created))
    candidates: list[tuple[dt.datetime, int, str]] = []
    for pr in sample.get("pull_requests") or []:
        linked = pr.get("linked_issues") or []
        green = pr.get("checks_green_at")
        number = pr.get("number")
        if number is None or not green:
            continue
        if issue_number not in linked and str(issue_number) not in {str(x) for x in linked}:
            continue
        candidates.append((parse_ts(str(green)), int(number), str(green)))
    if not candidates:
        return base
    end_at, pr_number, end_raw = min(candidates, key=lambda item: item[0])
    seconds = (end_at - start).total_seconds()
    if seconds < 0:
        return {**base, "status": "no-evaluable", "detail": "checks_green_at before issue.created_at"}
    return {
        "status": "ok",
        "issue": int(issue_number),
        "pull_request": pr_number,
        "seconds": float(seconds),
        "start_event": EVENT_PAIR["start"],
        "end_event": EVENT_PAIR["end"],
        "start_at": created,
        "end_at": end_raw,
    }


def load_lead_time_fixture(path: Path) -> dict[str, Any]:
    return lead_time_from_sample(json.loads(path.read_text(encoding="utf-8")))


def stage_durations(runs: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Per-stage duration from evidence ledger runs (sum of gate seconds per run)."""
    buckets: dict[str, list[float]] = {}
    ok_counts: dict[str, int] = {}
    for run in runs:
        stage = str(run.get("stage") or "")
        if not stage:
            continue
        total = sum(float(g.get("seconds") or 0) for g in run.get("gates") or [])
        buckets.setdefault(stage, []).append(total)
        if run.get("ok"):
            ok_counts[stage] = ok_counts.get(stage, 0) + 1
    stages = []
    for stage in sorted(buckets):
        values = buckets[stage]
        stages.append(
            {
                "stage": stage,
                "runs": len(values),
                "ok_runs": ok_counts.get(stage, 0),
                "median_seconds": round(statistics.median(values), 2),
                "total_seconds": round(sum(values), 2),
            }
        )
    return stages


def emit_lead_time(port: TelemetryPort, result: dict[str, Any]) -> None:
    attrs = redact_metric_attributes(
        {
            "aisdlc.issue": result.get("issue"),
            "aisdlc.pull_request": result.get("pull_request"),
            "aisdlc.lead_time_seconds": result.get("seconds") or 0.0,
            "aisdlc.lead_time_status": result.get("status"),
            "aisdlc.start_event": result.get("start_event"),
            "aisdlc.end_event": result.get("end_event"),
        }
    )
    port.emit("aisdlc.lead_time", attrs)


def emit_stage_durations(port: TelemetryPort, stages: list[dict[str, Any]]) -> None:
    for stage in stages:
        port.emit(
            "aisdlc.stage.duration",
            redact_metric_attributes(
                {
                    "aisdlc.stage": stage["stage"],
                    "aisdlc.duration_seconds": stage["median_seconds"],
                    "aisdlc.runs": stage["runs"],
                }
            ),
        )
