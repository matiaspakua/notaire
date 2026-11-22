"""Server-Sent Events for ledger/store event streaming (#2397).

Plain functions only — no HTTP code beyond formatting.
Injectable sleep/clock so tests need no real waiting.
"""
from __future__ import annotations

import json
import re
import time
from collections.abc import Callable

from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import redact

KEEPALIVE = b": keep-alive\n\n"

_KIND_SANITIZER = re.compile(r"[^A-Za-z0-9._-]")


def _is_stage_run(row: dict) -> bool:
    """Classify a ledger row as a stage run (gates+stage, no kind)."""
    return "gates" in row and "stage" in row and "kind" not in row


def _emit(write: Callable[[bytes], None], frame: bytes) -> bool:
    """Write a frame; return False if the client disconnected."""
    try:
        write(frame)
        return True
    except (BrokenPipeError, ConnectionResetError, OSError):
        return False


def ledger_rows(ctx: Context, after: int = 0) -> list[tuple[int, dict]]:
    """Read ledger file and return (1-based line number, parsed row) for lines > after."""
    from aisdlc.evidence import LEDGER

    ledger = ctx.path(ctx.profile.evidence_dir) / LEDGER
    if not ledger.is_file():
        return []
    text = ledger.read_text(encoding="utf-8")
    raw_lines = text.split("\n")
    results: list[tuple[int, dict]] = []
    total = len(raw_lines)
    for idx, raw in enumerate(raw_lines, 1):
        if idx <= after:
            continue
        # Skip the last line if it has no trailing newline (partial)
        if idx == total and not text.endswith("\n"):
            continue
        stripped = raw.strip()
        if not stripped:
            continue
        try:
            row = json.loads(raw)
        except json.JSONDecodeError:
            continue
        if not isinstance(row, dict):
            continue
        results.append((idx, row))
    return results


def format_event(event_id: int, row: dict) -> bytes:
    """Format a single SSE frame: id, event kind, redacted JSON data."""
    kind = row.get("kind")
    if kind is None:
        kind = "stage-run" if _is_stage_run(row) else "ledger"
    kind = _KIND_SANITIZER.sub("_", str(kind))
    clean = redact("", row)
    data = json.dumps(clean, sort_keys=True, separators=(",", ":"))
    frame = f"id: {event_id}\nevent: {kind}\ndata: {data}\n\n"
    return frame.encode("utf-8")


def parse_last_event_id(
    header: str | None, query_after: str | None,
) -> int:
    """Parse Last-Event-ID from header or query param. Non-negative int."""
    raw = header
    if raw is None:
        raw = query_after
    if raw is None or raw == "":
        return 0
    try:
        val = int(raw)
    except ValueError as exc:
        raise KitError("invalid_body", f"invalid Last-Event-ID: {raw!r}") from exc
    if val < 0:
        raise KitError("invalid_body", f"negative Last-Event-ID: {val}")
    return val


def stream(
    ctx: Context,
    write: Callable[[bytes], None],
    after: int,
    *,
    poll_seconds: float = 1.0,
    keepalive_seconds: float = 15.0,
    should_stop: Callable[[], bool] = lambda: False,
    sleep: Callable[[float], None] = time.sleep,
    clock: Callable[[], float] = time.monotonic,
) -> None:
    """Replay rows after 'after', then poll for new rows with keep-alive."""
    last_id = after
    last_write = clock()

    rows = ledger_rows(ctx, after=after)
    for eid, row in rows:
        frame = format_event(eid, row)
        if not _emit(write, frame):
            return
        last_id = eid
        last_write = clock()

    while not should_stop():
        sleep(poll_seconds)
        new_rows = ledger_rows(ctx, after=last_id)
        if new_rows:
            for eid, row in new_rows:
                frame = format_event(eid, row)
                if not _emit(write, frame):
                    return
                last_id = eid
            last_write = clock()
        elif clock() - last_write >= keepalive_seconds:
            if not _emit(write, KEEPALIVE):
                return
            last_write = clock()
