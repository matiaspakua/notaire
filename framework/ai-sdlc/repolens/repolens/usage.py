"""Usage telemetry: one JSON line per event in `<state>/usage.jsonl`. Standard library only.

Two sources write here:
  server   the MCP server, for every tool call (any harness): tool, latency, size, files returned
  harness  a harness hook (Claude Code PostToolUse) for navigation tools: Read, Grep, Glob and the
           repolens tools, with the session id, so `report` can tell whether agents opened the files
           repolens had returned, and how often they fell back to grep

Recording never raises: telemetry must not break an agent's tool call. Everything stays on this machine.
"""

from __future__ import annotations

import json
import os
import re
import sys
import time
from pathlib import Path

ROTATE_BYTES = 20_000_000
MAX_FILES = 40
_PATH_LINE = re.compile(r"(?<![\w/.-])((?:[\w@.+-]+/)*[\w@+-][\w@.+-]*\.[A-Za-z0-9]{1,8})(?::\d+)")
_RESULT_LINE = re.compile(r"^\s*\d+\.\s+\[\w+\]\s+(\S+?):\d", re.M)
NAVIGATION_TOOLS = {"Read": "read", "Grep": "grep", "Glob": "glob", "LS": "glob", "NotebookRead": "read"}


def record(log: Path, event: dict) -> None:
    """Append one event (with a timestamp). Missing or unwritable state folders are ignored."""
    try:
        if not log.parent.is_dir():
            return
        if log.exists() and log.stat().st_size > ROTATE_BYTES:
            log.replace(log.with_suffix(".jsonl.1"))
        line = json.dumps({"ts": round(time.time(), 3), **event}, separators=(",", ":"), ensure_ascii=False)
        with open(log, "a", encoding="utf-8") as fh:
            fh.write(line + "\n")
    except (OSError, TypeError, ValueError):
        pass


def read(log: Path, since: float = 0.0) -> list[dict]:
    events: list[dict] = []
    for path in (log.with_suffix(".jsonl.1"), log):
        if not path.exists():
            continue
        with open(path, encoding="utf-8", errors="replace") as fh:
            for line in fh:
                try:
                    event = json.loads(line)
                except ValueError:
                    continue
                if isinstance(event, dict) and event.get("ts", 0) >= since:
                    events.append(event)
    return events


def files_in(text: str) -> list[str]:
    """Repository paths a rendered repolens answer points to (`path:line`), in order, without duplicates."""
    found = _RESULT_LINE.findall(text) or _PATH_LINE.findall(text)
    return list(dict.fromkeys(found))[:MAX_FILES]


def relative(path: str, root: Path) -> str:
    """A tool's path argument made repository-relative (agents pass absolute paths)."""
    if not path:
        return ""
    candidate = Path(path)
    if candidate.is_absolute():
        try:
            return candidate.resolve().relative_to(root.resolve()).as_posix()
        except (ValueError, OSError):
            return candidate.as_posix()
    return candidate.as_posix().removeprefix("./")


def harness_event(payload: dict, root: Path, harness: str = "claude-code") -> dict | None:
    """Turn a PostToolUse hook payload into a usage event; None for tools that are not navigation."""
    tool = str(payload.get("tool_name") or "")
    params = payload.get("tool_input") or {}
    response = payload.get("tool_response")
    text = response if isinstance(response, str) else json.dumps(response, ensure_ascii=False, default=str)
    event: dict = {"src": "harness", "harness": harness, "session": str(payload.get("session_id") or "")[:64],
                   "bytes": len(text.encode("utf-8")) if text else 0}
    if tool.startswith("mcp__repolens__"):
        event.update(tool="repolens:" + tool.rsplit("__", 1)[-1], files=files_in(text),
                     input=str(params.get("query") or params.get("ref") or params.get("file_path") or "")[:200])
        return event
    kind = NAVIGATION_TOOLS.get(tool)
    if kind is None:
        return None
    event["tool"] = kind
    if kind == "read":
        event["files"] = [relative(str(params.get("file_path") or params.get("notebook_path") or ""), root)]
    elif kind == "grep":
        event["input"] = str(params.get("pattern") or "")[:200]
        event["files"] = [p for p in (relative(str(params.get("path") or ""), root),) if p]
    else:
        event["input"] = str(params.get("pattern") or params.get("path") or "")[:200]
    return event


def hook_main(stdin=None, harness: str = "claude-code") -> int:
    """`repolens hook`: read one hook payload from stdin and record it. Always exits 0, prints nothing."""
    try:
        from .paths import state_dir, usage_log

        payload = json.load(stdin or sys.stdin)
        root = Path(payload.get("cwd") or os.getcwd())
        state = state_dir(root)
        event = harness_event(payload, state.parent, harness)
        if event is not None:
            record(usage_log(state), event)
    except Exception:  # noqa: BLE001, S110 - a telemetry hook must never fail or slow the agent's tool call
        pass
    return 0
