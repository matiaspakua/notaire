"""Coach: how this machine's agent sessions are run, measured locally and never the words (#2384).

Reads the session stores that coding harnesses keep under the user's home (Claude Code, Codex, OpenCode,
Antigravity, Junie CLI) read-only, and turns them into counts and the anti-patterns of the AI engineering
audit. Prompt and response text stays in memory while a session is scored; the report carries counts, model
and tool names only. OpenCode's database also holds credentials, so only its session, message and part tables
are queried. Junie's EnvironmentVariablesUpdatedEvent carries secrets and is never read.
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
HARNESSES = ("claude-code", "codex", "opencode", "agy", "junie")
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


def junie(home: Path) -> Iterator[Session]:
    """Read Junie CLI sessions: index.jsonl → events.jsonl → history.jsonl.

    Never reads EnvironmentVariablesUpdatedEvent (secrets). Counts one tool call
    per unique stepId across ToolBlockUpdatedEvent and TerminalBlockUpdatedEvent.
    """
    index_path = home / ".junie" / "sessions" / "index.jsonl"
    if not index_path.is_file():
        return

    # Build a map of sessionId → session metadata
    sessions: dict[str, Session] = {}
    for row in _rows(index_path):
        session_id = str(row.get("sessionId") or "")
        if not session_id:
            continue
        session = Session("junie", cwd=row.get("projectDir"), tool_calls=None)
        created = _when(row.get("createdAt"))
        if created is not None:
            session.stamps.append(created)
        sessions[session_id] = session

    if not sessions:
        return

    # Read events for each session
    sessions_dir = home / ".junie" / "sessions"
    task_start_counts: dict[str, int] = {}
    task_start_times: dict[str, list[datetime]] = {}
    for session_id, session in sessions.items():
        events_path = sessions_dir / session_id / "events.jsonl"
        if not events_path.is_file():
            continue
        step_ids: set[str] = set()
        task_start_counts[session_id] = 0
        task_start_times[session_id] = []
        for row in _rows(events_path):
            kind = row.get("kind")
            # Count TaskStartedEvent for task-mode sessions (junie --task)
            if kind == "TaskStartedEvent":
                task_start_counts[session_id] = task_start_counts.get(session_id, 0) + 1
                ts = _when(row.get("timestampMs"))
                if ts is not None:
                    task_start_times.setdefault(session_id, []).append(ts)
            if kind == "SessionA2uxEvent":
                event = row.get("event") if isinstance(row.get("event"), dict) else {}
                agent_event = event.get("agentEvent") if isinstance(event.get("agentEvent"), dict) else {}
                agent_kind = agent_event.get("kind")
                # Skip EnvironmentVariablesUpdatedEvent — contains secrets
                if agent_kind == "EnvironmentVariablesUpdatedEvent":
                    continue
                # Count tool calls by unique stepId
                if agent_kind in ("ToolBlockUpdatedEvent", "TerminalBlockUpdatedEvent"):
                    step_id = agent_event.get("stepId")
                    if step_id:
                        step_ids.add(str(step_id))
                # Model from LlmResponseMetadataEvent — real shape is a list of
                # {"model": str, "cost": float, "inputTokens": int, ...}
                if agent_kind == "LlmResponseMetadataEvent":
                    model_usage = agent_event.get("modelUsage")
                    if isinstance(model_usage, list):
                        for item in model_usage:
                            if isinstance(item, dict):
                                model_name = item.get("model")
                                if isinstance(model_name, str) and model_name:
                                    session.models[model_name] += 1
                    elif isinstance(model_usage, str) and model_usage:
                        session.models[model_usage] += 1
        session.tool_calls = len(step_ids)

    # Match prompts from history.jsonl by project path and time window.
    # Each prompt is assigned to exactly one session: the one whose time window
    # is closest (prefer createdAt <= prompt <= updatedAt; else nearest createdAt
    # within 1 day).
    history_path = home / ".junie" / "history.jsonl"
    if history_path.is_file():
        # Re-read index to get createdAt/updatedAt per session for window matching
        session_windows: dict[str, tuple[datetime | None, datetime | None]] = {}
        for row in _rows(index_path):
            sid = str(row.get("sessionId") or "")
            if sid in sessions:
                created = _when(row.get("createdAt"))
                updated = _when(row.get("updatedAt"))
                session_windows[sid] = (created, updated)

        for row in _rows(history_path):
            project_path = row.get("projectPath")
            if not project_path:
                continue
            prompt_text = row.get("text")
            if not isinstance(prompt_text, str):
                continue
            prompt_when = _when(row.get("timestamp"))

            # Find the best matching session for this prompt
            best_sid: str | None = None
            best_score = float("inf")
            for sid, session in sessions.items():
                if session.cwd != str(project_path):
                    continue
                created, updated = session_windows.get(sid, (None, None))
                if prompt_when is None:
                    # Without a timestamp, pick the session with closest createdAt
                    if created is not None:
                        score = abs((prompt_when or created) - created).total_seconds()
                    else:
                        score = float("inf")
                else:
                    if created is not None and updated is not None:
                        if created <= prompt_when <= updated:
                            # Prompt falls within this session's window — best match
                            score = 0
                        else:
                            # Distance to nearest window edge
                            score = min(abs((prompt_when - created).total_seconds()),
                                        abs((prompt_when - updated).total_seconds()))
                    elif created is not None:
                        score = abs((prompt_when - created).total_seconds())
                    else:
                        score = float("inf")
                # Only consider sessions within 1 day
                if score > 86400:
                    continue
                if score < best_score:
                    best_score = score
                    best_sid = sid

            if best_sid is not None:
                sessions[best_sid].prompt(prompt_text, prompt_when)

    # For task-mode sessions (junie --task) with no history prompts, count each
    # TaskStartedEvent as one user turn using an empty prompt string.
    for session_id, session in sessions.items():
        count = task_start_counts.get(session_id, 0)
        if count and not session.prompts:
            times = task_start_times.get(session_id, [])
            for i in range(count):
                ts = times[i] if i < len(times) else None
                session.prompt("", ts)

    yield from (s for s in sessions.values() if s.prompts)


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


READERS = {"claude-code": claude_code, "codex": codex, "opencode": opencode, "agy": agy, "junie": junie}


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
