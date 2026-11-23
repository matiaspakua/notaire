"""Usage telemetry: one JSON line per event in `<state>/usage.jsonl`. Standard library only.

Two sources write here:
  server   the MCP server, for every tool call (any harness): tool, latency, size, files returned
  harness  a harness hook (Claude Code PostToolUse) for navigation tools: Read, Grep, Glob, the
           repolens tools and the shell commands that run either (`repolens search`, `rg`, `git grep`,
           `find`), with the session id, so `report` can tell whether agents opened the files repolens had
           returned, and how often they fell back to grep. Reads outside the repository are not navigation

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
_RESULT_LINE = re.compile(r"^\s*\d+\.\s+(?:\[\w+\]\s+)?(\S+?):\d", re.M)
NAVIGATION_TOOLS = {"Read": "read", "Grep": "grep", "Glob": "glob", "LS": "glob", "NotebookRead": "read"}
_SHELL_START = r"(?:^|[;&|(]\s*|\bxargs\s+|\bthen\s+|\bdo\s+)"
_SHELL_REPOLENS = re.compile(r"(?:^|[\s/;&|(\"'])repolens[\"']?\s+"
                             r"(search|node|impact|path|outline|snippet|neighbors|orphans)\b")
_SHELL_GREP = re.compile(_SHELL_START + r"(?:rg|grep|egrep|fgrep|ag|ack|git\s+grep)\b")
_SHELL_GLOB = re.compile(_SHELL_START + r"(?:find|fd|git\s+ls-files)\b")


def cli_session() -> str:
    """Session id for a CLI query. Empty when neither variable is set.

    ``REPOLENS_SESSION`` wins. Otherwise the Cursor cloud agent conversation id
    is used, so CLI searches from one run share a session in ``repolens report``.
    """
    for key in ("REPOLENS_SESSION", "CURSOR_CONVERSATION_ID"):
        value = os.environ.get(key, "").strip()
        if value:
            return value[:64]
    return ""


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


def response_text(response) -> str:
    """What a tool returned to the model: a shell's stdout, an MCP tool's text blocks, or the raw value."""
    if isinstance(response, str):
        return response
    if isinstance(response, dict) and isinstance(response.get("stdout"), str):
        return response["stdout"]
    blocks = response.get("content") if isinstance(response, dict) else response
    if isinstance(blocks, list) and blocks and all(isinstance(b, dict) and "text" in b for b in blocks):
        return "\n".join(str(b["text"]) for b in blocks)
    return json.dumps(response, ensure_ascii=False, default=str) if response is not None else ""


def shell_event(command: str, text: str) -> dict | None:
    """A shell command that navigates: a repolens query (with the files it printed), a text search, a file
    listing. Anything else is not navigation. A command that does both counts as the repolens call."""
    query = _SHELL_REPOLENS.search(command)
    if query:
        return {"tool": "repolens:" + query.group(1), "files": files_in(text), "input": command[-200:]}
    if _SHELL_GREP.search(command):
        return {"tool": "grep", "input": command[:200]}
    if _SHELL_GLOB.search(command):
        return {"tool": "glob", "input": command[:200]}
    return None


def outside(path: str) -> bool:
    """A path `relative` could not place inside the repository (a scratchpad, a temp file)."""
    return path.startswith("/")


def harness_event(payload: dict, root: Path, harness: str = "claude-code") -> dict | None:
    """Turn a PostToolUse hook payload into a usage event; None for tools that are not navigation."""
    tool = str(payload.get("tool_name") or "")
    params = payload.get("tool_input") or {}
    text = response_text(payload.get("tool_response"))
    event: dict = {"src": "harness", "harness": harness, "session": str(payload.get("session_id") or "")[:64],
                   "bytes": len(text.encode("utf-8")) if text else 0}
    if tool == "Bash":
        shell = shell_event(str(params.get("command") or ""), text)
        return {**event, **shell} if shell else None
    if tool.startswith("mcp__repolens__"):
        event.update(tool="repolens:" + tool.rsplit("__", 1)[-1], files=files_in(text),
                     input=str(params.get("query") or params.get("ref") or params.get("file_path") or "")[:200])
        return event
    kind = NAVIGATION_TOOLS.get(tool)
    if kind is None:
        return None
    event["tool"] = kind
    if kind == "read":
        path = relative(str(params.get("file_path") or params.get("notebook_path") or ""), root)
        if outside(path):
            return None
        event["files"] = [path]
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
