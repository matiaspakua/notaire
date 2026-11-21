"""Coach: how this machine's agent sessions are run, measured locally and never the words (#2384).

Reads the session stores that coding harnesses keep under the user's home (Claude Code, Codex, OpenCode,
Antigravity) read-only, and turns them into counts and the anti-patterns of the AI engineering audit. Prompt
and response text stays in memory while a session is scored; the report carries counts, model and tool names
only. OpenCode's database also holds credentials, so only its session, message and part tables are queried.
"""
from __future__ import annotations

import json
import os
import re
import sqlite3
from collections import Counter
from collections.abc import Iterator
from dataclasses import dataclass, field
from datetime import UTC, datetime, timedelta
from pathlib import Path

SCHEMA = "aisdlc.coach/v1"
HARNESSES = ("claude-code", "codex", "opencode", "agy")
# Harnesses write their own messages into the user role: task notifications, command echoes, tool stdout.
INJECTED = re.compile(r"^\s*<[a-z][a-z0-9_-]*>")
SEVERITY_ORDER = ("high", "medium", "low")
THRESHOLDS = {
    "mega_session_turns": 30,
    "prompt_bloat_chars": 8000,
    "runaway_tool_streak": 40,
    "repeat_min_chars": 12,
    "plan_expected_turns": 10,
    "plan_expected_tools": 40,
}
PATTERNS = {
    "repeated_prompt": ("high", "When a step fails, send the error and a changed constraint instead of the same prompt."),
    "runaway_loop": ("high", "Give the agent a stopping rule and split the task into milestones it reports back on."),
    "unguarded_autonomy": ("high", "Run auto-approved agents in a sandbox or container, not on the host."),
    "mega_session": ("medium", "Start a fresh session when a sub-task is done; compact before context degrades."),
    "prompt_bloat": ("medium", "Reference files, line ranges or a grep instead of pasting logs and whole files."),
    "no_plan": ("medium", "Plan multi-step work first (/plan, an OpenSpec change or a short design note)."),
}


@dataclass
class Session:
    harness: str
    cwd: str | None = None
    prompts: list[str] = field(default_factory=list)
    stamps: list[datetime] = field(default_factory=list)
    tool_calls: int | None = 0
    streak: int = 0
    runaways: int = 0
    models: Counter = field(default_factory=Counter)
    tools: Counter = field(default_factory=Counter)
    planned: bool = False
    unguarded: bool = False

    def prompt(self, text: str, when: datetime | None) -> None:
        if INJECTED.match(text):
            self.planned = self.planned or _is_plan_prompt(text)
            return
        self.prompts.append(text)
        if when is not None:
            self.stamps.append(when)
        if _is_plan_prompt(text):
            self.planned = True
        self.streak = 0

    def tool(self, name: str) -> None:
        self.tool_calls = (self.tool_calls or 0) + 1
        self.tools[name] += 1
        self.streak += 1
        if self.streak == THRESHOLDS["runaway_tool_streak"]:
            self.runaways += 1


def _is_plan_prompt(text: str) -> bool:
    head = text.lstrip()[:80]
    return head.startswith("/plan") or "<command-name>/plan" in head


def _when(value: object) -> datetime | None:
    if isinstance(value, (int, float)):
        return datetime.fromtimestamp(value / 1000 if value > 1e11 else value, tz=UTC)
    if isinstance(value, str) and value:
        try:
            parsed = datetime.fromisoformat(value)
        except ValueError:
            return None
        return parsed if parsed.tzinfo else parsed.replace(tzinfo=UTC)
    return None


def _rows(path: Path) -> Iterator[dict]:
    try:
        with path.open(encoding="utf-8", errors="replace") as handle:
            for line in handle:
                try:
                    row = json.loads(line)
                except json.JSONDecodeError:
                    continue
                if isinstance(row, dict):
                    yield row
    except OSError:
        return


def _text_of(content: object) -> str | None:
    if isinstance(content, str):
        return content
    if not isinstance(content, list):
        return None
    if any(isinstance(item, dict) and item.get("type") == "tool_result" for item in content):
        return None
    texts = [item.get("text", "") for item in content if isinstance(item, dict) and item.get("type") == "text"]
    return "\n".join(texts) if texts else None


def claude_code(home: Path) -> Iterator[Session]:
    for path in sorted((home / ".claude" / "projects").glob("*/*.jsonl")):
        session = Session("claude-code")
        for row in _rows(path):
            if row.get("isSidechain") or row.get("isMeta") or row.get("isCompactSummary"):
                continue
            session.cwd = session.cwd or row.get("cwd")
            mode = row.get("permissionMode")
            session.planned = session.planned or mode == "plan"
            session.unguarded = session.unguarded or mode == "bypassPermissions"
            message = row.get("message") if isinstance(row.get("message"), dict) else {}
            if row.get("type") == "user":
                text = _text_of(message.get("content"))
                if text is not None:
                    session.prompt(text, _when(row.get("timestamp")))
            elif row.get("type") == "assistant":
                _claude_assistant(session, message)
        if session.prompts:
            yield session


def _claude_assistant(session: Session, message: dict) -> None:
    content = message.get("content")
    if not isinstance(content, list):
        return
    for item in content:
        if isinstance(item, dict) and item.get("type") == "tool_use":
            name = str(item.get("name") or "tool")
            session.planned = session.planned or name == "ExitPlanMode"
            session.tool(name)
            if message.get("model"):
                session.models[str(message["model"])] += 1


def codex(home: Path) -> Iterator[Session]:
    for path in sorted((home / ".codex" / "sessions").glob("**/rollout-*.jsonl")):
        session = Session("codex")
        for row in _rows(path):
            payload = row.get("payload") if isinstance(row.get("payload"), dict) else {}
            kind, inner = row.get("type"), payload.get("type")
            if kind == "session_meta":
                session.cwd = payload.get("cwd")
            elif kind == "turn_context":
                _codex_context(session, payload)
            elif kind == "event_msg" and inner == "user_message":
                session.prompt(str(payload.get("message") or ""), _when(row.get("timestamp")))
            elif kind == "response_item" and inner in {"function_call", "custom_tool_call", "local_shell_call"}:
                session.tool(str(payload.get("name") or inner))
        if session.prompts:
            yield session


def _codex_context(session: Session, payload: dict) -> None:
    if payload.get("model"):
        session.models[str(payload["model"])] += 1
    policy = payload.get("sandbox_policy")
    mode = policy.get("type") or policy.get("mode") if isinstance(policy, dict) else policy
    session.unguarded = session.unguarded or mode == "danger-full-access"


def opencode(home: Path) -> Iterator[Session]:
    path = home / ".local" / "share" / "opencode" / "opencode.db"
    if not path.is_file():
        return
    try:
        db = sqlite3.connect(f"{path.as_uri()}?mode=ro", uri=True)
    except sqlite3.Error:
        return
    try:
        yield from _opencode_sessions(db)
    except sqlite3.Error:
        return
    finally:
        db.close()


def _opencode_sessions(db: sqlite3.Connection) -> Iterator[Session]:
    sessions = {sid: Session("opencode", cwd=cwd) for sid, cwd in db.execute("SELECT id, directory FROM session")}
    roles: dict[str, str] = {}
    for mid, sid, data in db.execute("SELECT id, session_id, data FROM message ORDER BY time_created, id"):
        message = _json(data)
        roles[mid] = str(message.get("role") or "")
        session = sessions.get(sid)
        if session is None:
            continue
        session.planned = session.planned or message.get("agent") == "plan"
        model = message.get("modelID") or (message.get("model") or {}).get("modelID")
        if message.get("role") == "assistant" and model:
            session.models[str(model)] += 1
    query = "SELECT message_id, session_id, time_created, data FROM part ORDER BY time_created, id"
    for mid, sid, created, data in db.execute(query):
        session, part = sessions.get(sid), _json(data)
        if session is None:
            continue
        if part.get("type") == "text" and roles.get(mid) == "user":
            session.prompt(str(part.get("text") or ""), _when(created))
        elif part.get("type") == "tool":
            session.tool(str(part.get("tool") or "tool"))
    yield from (session for session in sessions.values() if session.prompts)


def _json(raw: object) -> dict:
    try:
        value = json.loads(raw) if isinstance(raw, (str, bytes)) else {}
    except json.JSONDecodeError:
        return {}
    return value if isinstance(value, dict) else {}


def agy(home: Path) -> Iterator[Session]:
    sessions: dict[str, Session] = {}
    for row in _rows(home / ".gemini" / "antigravity-cli" / "history.jsonl"):
        conversation = str(row.get("conversationId") or "")
        if not conversation:
            continue
        session = sessions.setdefault(conversation, Session("agy", cwd=row.get("workspace"), tool_calls=None))
        session.prompt(str(row.get("display") or ""), _when(row.get("timestamp")))
    yield from sessions.values()


def default_home() -> Path:
    """AISDLC_COACH_HOME points the coach at another home (tests, a mounted backup); else the user's."""
    return Path(os.environ.get("AISDLC_COACH_HOME") or Path.home())


READERS = {"claude-code": claude_code, "codex": codex, "opencode": opencode, "agy": agy}


def _within(cwd: str | None, root: Path) -> bool:
    if not cwd:
        return False
    try:
        Path(cwd).resolve().relative_to(root.resolve())
    except (OSError, ValueError):
        return False
    return True


def _patterns(session: Session) -> Counter:
    found: Counter = Counter()
    turns = len(session.prompts)
    if turns > THRESHOLDS["mega_session_turns"]:
        found["mega_session"] += 1
    seen: set[str] = set()
    for text in session.prompts:
        normal = " ".join(text.casefold().split())
        if len(normal) >= THRESHOLDS["repeat_min_chars"] and normal in seen:
            found["repeated_prompt"] += 1
        seen.add(normal)
        if len(text) > THRESHOLDS["prompt_bloat_chars"]:
            found["prompt_bloat"] += 1
    if session.runaways:
        found["runaway_loop"] += session.runaways
    if session.unguarded:
        found["unguarded_autonomy"] += 1
    large = turns >= THRESHOLDS["plan_expected_turns"] or (session.tool_calls or 0) >= THRESHOLDS["plan_expected_tools"]
    if large and not session.planned:
        found["no_plan"] += 1
    return found


def report(home: Path, *, root: Path | None = None, days: int | None = None) -> dict:
    """Score every session of `home`; `root` keeps the sessions run inside that project only."""
    since = datetime.now(UTC) - timedelta(days=days) if days else None
    rows, hours, totals = [], [0] * 24, Counter()
    occurrences: Counter = Counter()
    affected: Counter = Counter()
    for name in HARNESSES:
        sessions = [s for s in READERS[name](home) if _keep(s, root, since)]
        rows.append(_harness_row(name, sessions))
        for session in sessions:
            found = _patterns(session)
            occurrences.update(found)
            affected.update(found.keys())
            totals["sessions"] += 1
            totals["healthy_sessions"] += 0 if found else 1
            totals["user_turns"] += len(session.prompts)
            totals["tool_calls"] += session.tool_calls or 0
            for stamp in session.stamps:
                hours[stamp.astimezone().hour] += 1
    return {
        "schema": SCHEMA,
        "scope": {"project_only": root is not None, "days": days},
        "thresholds": dict(THRESHOLDS),
        "totals": {key: totals[key] for key in ("sessions", "healthy_sessions", "user_turns", "tool_calls")},
        "harnesses": rows,
        "anti_patterns": _anti_patterns(occurrences, affected),
        "hours": hours,
    }


def _keep(session: Session, root: Path | None, since: datetime | None) -> bool:
    if root is not None and not _within(session.cwd, root):
        return False
    return since is None or any(stamp >= since for stamp in session.stamps)


def _harness_row(name: str, sessions: list[Session]) -> dict:
    stamps = sorted(stamp for session in sessions for stamp in session.stamps)
    measured = name != "agy"
    return {
        "harness": name,
        "sessions": len(sessions),
        "user_turns": sum(len(s.prompts) for s in sessions),
        "tool_calls": sum(s.tool_calls or 0 for s in sessions) if measured else None,
        "models": dict(sum((s.models for s in sessions), Counter()).most_common(5)),
        "tools": dict(sum((s.tools for s in sessions), Counter()).most_common(8)),
        "first": stamps[0].isoformat() if stamps else None,
        "last": stamps[-1].isoformat() if stamps else None,
    }


def _anti_patterns(occurrences: Counter, affected: Counter) -> list[dict]:
    rows = [
        {"id": pid, "severity": PATTERNS[pid][0], "occurrences": count, "sessions": affected[pid],
         "recommendation": PATTERNS[pid][1]}
        for pid, count in occurrences.items()
    ]
    return sorted(rows, key=lambda r: (SEVERITY_ORDER.index(r["severity"]), -r["occurrences"], r["id"]))


def render(data: dict) -> str:
    totals = data["totals"]
    lines = [(
        f"coach: {totals['sessions']} sessions, {totals['user_turns']} prompts, "
        f"{totals['tool_calls']} tool calls, {totals['healthy_sessions']} without a finding"
    )]
    for row in data["harnesses"]:
        if row["sessions"]:
            tools = "n/a" if row["tool_calls"] is None else row["tool_calls"]
            lines.append(f"  {row['harness']:<12} {row['sessions']:>4} sessions {row['user_turns']:>6} prompts "
                         f"{tools:>6} tools")
    for item in data["anti_patterns"]:
        lines.append(f"  [{item['severity']}] {item['id']}: {item['occurrences']} in {item['sessions']} sessions. "
                     f"{item['recommendation']}")
    return "\n".join(lines)
