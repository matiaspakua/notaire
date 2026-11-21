"""Coach (#2384): local harness session habits, measured on this machine and never the words."""
from __future__ import annotations

import json
import os
import sqlite3
import sys
import threading
import unittest
import urllib.error
import urllib.request
from datetime import UTC, datetime, timedelta
from pathlib import Path
from unittest import mock

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

SENTINEL = "zebra-sentinel-prompt please refactor the ledger writer"
NOW = datetime.now(UTC)


def _iso(delta_minutes: int = 0, days_ago: int = 0) -> str:
    moment = NOW - timedelta(days=days_ago) + timedelta(minutes=delta_minutes)
    return moment.isoformat().replace("+00:00", "Z")


def _jsonl(path: Path, rows: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("".join(json.dumps(row) + "\n" for row in rows), encoding="utf-8")


def claude_session(home: Path, name: str, cwd: Path, *, days_ago: int = 0, prompts: list[str],
                   tool_streak: int = 0, mode: str = "default") -> None:
    rows: list[dict] = []
    minute = 0
    for prompt in prompts:
        rows.append({"type": "user", "cwd": str(cwd), "sessionId": name, "permissionMode": mode,
                     "timestamp": _iso(minute, days_ago), "message": {"role": "user", "content": prompt}})
        minute += 1
    for index in range(tool_streak):
        rows.append({"type": "assistant", "cwd": str(cwd), "sessionId": name, "timestamp": _iso(minute, days_ago),
                     "message": {"role": "assistant", "model": "claude-opus-5-5",
                                 "content": [{"type": "tool_use", "id": f"t{index}", "name": "Bash", "input": {}}]}})
        rows.append({"type": "user", "cwd": str(cwd), "sessionId": name, "timestamp": _iso(minute, days_ago),
                     "message": {"role": "user", "content": [{"type": "tool_result", "tool_use_id": f"t{index}",
                                                              "content": "ok"}]}})
        minute += 1
    _jsonl(home / ".claude" / "projects" / "-demo" / f"{name}.jsonl", rows)


def codex_session(home: Path, cwd: Path, prompts: list[str], tools: int) -> None:
    rows: list[dict] = [
        {"timestamp": _iso(), "type": "session_meta", "payload": {"id": "c1", "cwd": str(cwd), "timestamp": _iso()}},
        {"timestamp": _iso(), "type": "turn_context",
         "payload": {"model": "gpt-5", "sandbox_policy": {"type": "danger-full-access"}}},
    ]
    for prompt in prompts:
        rows.append({"timestamp": _iso(), "type": "event_msg", "payload": {"type": "user_message", "message": prompt}})
    for _ in range(tools):
        rows.append({"timestamp": _iso(), "type": "response_item",
                     "payload": {"type": "function_call", "name": "shell", "arguments": "{}"}})
    _jsonl(home / ".codex" / "sessions" / "2026" / "11" / "21" / "rollout-c1.jsonl", rows)


def opencode_db(home: Path, cwd: Path) -> None:
    path = home / ".local" / "share" / "opencode" / "opencode.db"
    path.parent.mkdir(parents=True, exist_ok=True)
    millis = int(NOW.timestamp() * 1000)
    with sqlite3.connect(path) as db:
        db.execute("CREATE TABLE session (id TEXT, project_id TEXT, directory TEXT, title TEXT, time_created INTEGER)")
        db.execute("CREATE TABLE message (id TEXT, session_id TEXT, time_created INTEGER, data TEXT)")
        db.execute("CREATE TABLE part (id TEXT, message_id TEXT, session_id TEXT, time_created INTEGER, data TEXT)")
        db.execute("CREATE TABLE account (id TEXT, access_token TEXT)")
        db.execute("INSERT INTO account VALUES ('a', 'must-never-be-read')")
        db.execute("INSERT INTO session VALUES ('o1', 'p', ?, 'title', ?)", (str(cwd), millis))
        db.execute("INSERT INTO message VALUES ('m1', 'o1', ?, ?)",
                   (millis, json.dumps({"role": "user", "agent": "plan", "model": {"modelID": "big-pickle"}})))
        db.execute("INSERT INTO part VALUES ('p1', 'm1', 'o1', ?, ?)",
                   (millis, json.dumps({"type": "text", "text": "plan the importer"})))
        db.execute("INSERT INTO message VALUES ('m2', 'o1', ?, ?)",
                   (millis + 1, json.dumps({"role": "assistant", "modelID": "big-pickle"})))
        db.execute("INSERT INTO part VALUES ('p2', 'm2', 'o1', ?, ?)",
                   (millis + 1, json.dumps({"type": "tool", "tool": "bash"})))


def agy_history(home: Path, cwd: Path) -> None:
    rows = [{"display": "build the parser", "timestamp": int(NOW.timestamp() * 1000), "workspace": str(cwd),
             "conversationId": "g1", "type": "user"}]
    _jsonl(home / ".gemini" / "antigravity-cli" / "history.jsonl", rows)


JUNIE_SENTINEL_PROMPT = "junie-sentinel-do-not-emit-this-prompt-text"
JUNIE_SENTINEL_SECRET = "SECRET_ENV_VALUE_DO_NOT_READ"


def junie_session(home: Path, cwd: Path, *, session_id: str = "junie-s1", days_ago: int = 0,
                  tool_step_ids: list[str] | None = None, model: str = "qwen-3-27b",
                  prompt_text: str = "implement the coach reader") -> None:
    """Create a synthetic Junie store: index.jsonl, events.jsonl, and history.jsonl."""
    ts_ms = int((NOW - timedelta(days=days_ago)).timestamp() * 1000)
    iso = (NOW - timedelta(days=days_ago)).isoformat()

    # Session index
    index_rows = [{
        "sessionId": session_id,
        "projectDir": str(cwd),
        "taskName": "coach reader task",
        "createdAt": iso,
        "lastPromptAt": iso,
        "updatedAt": iso,
    }]
    _jsonl(home / ".junie" / "sessions" / "index.jsonl", index_rows)

    # Events for the session
    events: list[dict] = [
        {"kind": "TaskStartedEvent", "timestampMs": ts_ms},
    ]
    # Add an EnvironmentVariablesUpdatedEvent with a sentinel secret (must NEVER be read)
    events.append({
        "kind": "SessionA2uxEvent",
        "timestampMs": ts_ms + 1,
        "event": {
            "agentEvent": {
                "kind": "EnvironmentVariablesUpdatedEvent",
                "env": {"SECRET_KEY": JUNIE_SENTINEL_SECRET},
            }
        },
    })
    # Tool block events (one per stepId)
    if tool_step_ids:
        for step_id in tool_step_ids:
            events.append({
                "kind": "SessionA2uxEvent",
                "timestampMs": ts_ms + 2,
                "event": {
                    "agentEvent": {
                        "kind": "ToolBlockUpdatedEvent",
                        "toolType": "file_edit",
                        "status": "completed",
                        "stepId": step_id,
                        "text": "",
                        "details": {},
                    }
                },
            })
    # Terminal block event
    events.append({
        "kind": "SessionA2uxEvent",
        "timestampMs": ts_ms + 3,
        "event": {
            "agentEvent": {
                "kind": "TerminalBlockUpdatedEvent",
                "status": "completed",
                "stepId": "term-1",
                "text": "echo hello",
            }
        },
    })
    # Model usage event
    events.append({
        "kind": "SessionA2uxEvent",
        "timestampMs": ts_ms + 4,
        "event": {
            "agentEvent": {
                "kind": "LlmResponseMetadataEvent",
                "modelUsage": model,
            }
        },
    })
    events_path = home / ".junie" / "sessions" / session_id / "events.jsonl"
    _jsonl(events_path, events)

    # History (prompts)
    history_rows = [
        {
            "projectPath": str(cwd),
            "text": prompt_text,
            "timestamp": iso,
        },
    ]
    _jsonl(home / ".junie" / "history.jsonl", history_rows)


class CoachTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        self.repo.write("POLICY.md", "# Policy\n")
        self.home = self.repo.root.parent / f"{self.repo.root.name}-home"
        self.home.mkdir()
        elsewhere = self.home / "elsewhere"
        long_prompts = [f"step {n}: implement the next part of the importer carefully" for n in range(35)]
        claude_session(self.home, "mega", self.repo.root, prompts=[SENTINEL, SENTINEL, "x" * 9000, *long_prompts],
                       tool_streak=45, mode="bypassPermissions")
        claude_session(self.home, "tidy", self.repo.root, prompts=["/plan add an export command"], tool_streak=3)
        claude_session(self.home, "old", self.repo.root, prompts=["an old prompt from long ago"], days_ago=60)
        claude_session(self.home, "other", elsewhere, prompts=["work on another repository"])
        codex_session(self.home, self.repo.root, ["fix the flaky test", "fix the flaky test"], tools=2)
        opencode_db(self.home, self.repo.root)
        agy_history(self.home, self.repo.root)
        junie_session(self.home, self.repo.root, prompt_text=JUNIE_SENTINEL_PROMPT,
                      tool_step_ids=["step-1", "step-2"])

    def _coach(self, *args: str) -> dict:
        result = self.repo.run("coach", "--home", str(self.home), "--json", *args)
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn(SENTINEL, result.stdout)
        self.assertNotIn("must-never-be-read", result.stdout)
        self.assertNotIn(JUNIE_SENTINEL_PROMPT, result.stdout)
        self.assertNotIn(JUNIE_SENTINEL_SECRET, result.stdout)
        return json.loads(result.stdout)

    @staticmethod
    def _harness(report: dict, name: str) -> dict:
        return next(row for row in report["harnesses"] if row["harness"] == name)

    @staticmethod
    def _pattern(report: dict, pattern: str) -> dict:
        return next(row for row in report["anti_patterns"] if row["id"] == pattern)

    def test_the_report_counts_each_harness_of_this_project_only(self):
        report = self._coach()
        self.assertEqual("aisdlc.coach/v1", report["schema"])
        self.assertEqual(3, self._harness(report, "claude-code")["sessions"])
        self.assertEqual(1, self._harness(report, "codex")["sessions"])
        self.assertEqual(1, self._harness(report, "opencode")["sessions"])
        self.assertEqual(1, self._harness(report, "agy")["sessions"])
        self.assertEqual(1, self._harness(report, "junie")["sessions"])
        self.assertEqual({"claude-opus-5-5": 48}, self._harness(report, "claude-code")["models"])
        self.assertEqual({"shell": 2}, self._harness(report, "codex")["tools"])
        self.assertIsNone(self._harness(report, "agy")["tool_calls"])
        self.assertEqual(sum(report["hours"]), report["totals"]["user_turns"])
        # Junie specifics
        junie_row = self._harness(report, "junie")
        self.assertEqual(1, junie_row["user_turns"])
        self.assertEqual(3, junie_row["tool_calls"])  # step-1, step-2, term-1
        self.assertEqual({"qwen-3-27b": 1}, junie_row["models"])

    def test_all_projects_and_days_widen_or_narrow_the_window(self):
        self.assertEqual(4, self._harness(self._coach("--all-projects"), "claude-code")["sessions"])
        self.assertEqual(2, self._harness(self._coach("--days", "7"), "claude-code")["sessions"])

    def test_the_audit_anti_patterns_are_detected_with_a_recommendation(self):
        report = self._coach()
        self.assertEqual(1, self._pattern(report, "mega_session")["occurrences"])
        self.assertEqual(2, self._pattern(report, "repeated_prompt")["occurrences"])
        self.assertEqual(1, self._pattern(report, "prompt_bloat")["occurrences"])
        self.assertEqual(1, self._pattern(report, "runaway_loop")["occurrences"])
        self.assertEqual(2, self._pattern(report, "unguarded_autonomy")["sessions"])
        self.assertEqual(1, self._pattern(report, "no_plan")["sessions"])
        for row in report["anti_patterns"]:
            self.assertIn(row["severity"], {"high", "medium", "low"})
            self.assertTrue(row["recommendation"])
        severities = [row["severity"] for row in report["anti_patterns"]]
        self.assertEqual(sorted(severities, key=["high", "medium", "low"].index), severities)

    def test_a_planned_session_is_healthy(self):
        report = self._coach()
        self.assertEqual(7, report["totals"]["sessions"])
        self.assertGreater(report["totals"]["healthy_sessions"], 0)
        self.assertLess(report["totals"]["healthy_sessions"], report["totals"]["sessions"])

    def test_junie_session_does_not_leak_secrets_or_prompt_text(self):
        report = self._coach()
        junie_row = self._harness(report, "junie")
        self.assertEqual(1, junie_row["sessions"])
        # Prompt text must not appear (already checked in _coach, but verify structure)
        self.assertEqual(1, junie_row["user_turns"])
        # Tool calls: 2 tool blocks (step-1, step-2) + 1 terminal block (term-1) = 3 unique stepIds
        self.assertEqual(3, junie_row["tool_calls"])
        # Model from LlmResponseMetadataEvent
        self.assertEqual({"qwen-3-27b": 1}, junie_row["models"])

    def test_junie_days_filter_excludes_old_sessions(self):
        home = self.repo.root.parent / f"{self.repo.root.name}-junie-days"
        home.mkdir()
        junie_session(home, self.repo.root, session_id="recent", days_ago=0)
        junie_session(home, self.repo.root, session_id="old", days_ago=60)
        result = self.repo.run("coach", "--home", str(home), "--days", "7", "--json")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(result.stdout)
        self.assertEqual(1, self._harness(report, "junie")["sessions"])

    def test_junie_project_filter_excludes_other_projects(self):
        home = self.repo.root.parent / f"{self.repo.root.name}-junie-proj"
        home.mkdir()
        other_project = home / "other-project"
        other_project.mkdir()
        junie_session(home, self.repo.root, session_id="in-project")
        junie_session(home, other_project, session_id="out-project")
        result = self.repo.run("coach", "--home", str(home), "--json")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(result.stdout)
        self.assertEqual(1, self._harness(report, "junie")["sessions"])

    def test_junie_missing_store_returns_empty_report(self):
        empty = self.repo.root.parent / f"{self.repo.root.name}-no-junie"
        empty.mkdir()
        result = self.repo.run("coach", "--home", str(empty), "--json")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(result.stdout)
        junie_row = self._harness(report, "junie")
        self.assertEqual(0, junie_row["sessions"])

    def test_harness_injected_messages_are_not_counted_as_prompts(self):
        home = self.repo.root.parent / f"{self.repo.root.name}-injected"
        injected = [
            "<task-notification> <task-id>b1</task-id> background task finished </task-notification>",
            "<task-notification> <task-id>b1</task-id> background task finished </task-notification>",
            "<local-command-stdout>model set</local-command-stdout>",
            "<command-name>/model</command-name>",
        ]
        claude_session(home, "noisy", self.repo.root, prompts=["refactor the importer module", *injected])
        path = home / ".claude" / "projects" / "-demo" / "noisy.jsonl"
        summary = {"type": "user", "cwd": str(self.repo.root), "isCompactSummary": True, "timestamp": _iso(),
                   "message": {"role": "user", "content": "This session is being continued from a previous one."}}
        with path.open("a", encoding="utf-8") as handle:
            handle.write(json.dumps(summary) + "\n")
            handle.write(json.dumps(summary) + "\n")
        result = self.repo.run("coach", "--home", str(home), "--json")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(result.stdout)
        self.assertEqual(1, report["totals"]["user_turns"])
        self.assertEqual([], report["anti_patterns"])

    def test_missing_stores_give_an_empty_report_not_an_error(self):
        empty = self.repo.root.parent / f"{self.repo.root.name}-empty"
        empty.mkdir()
        result = self.repo.run("coach", "--home", str(empty), "--json")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(result.stdout)
        self.assertEqual(0, report["totals"]["sessions"])
        self.assertEqual([], report["anti_patterns"])

    def test_the_text_report_names_the_patterns(self):
        result = self.repo.run("coach", "--home", str(self.home))
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("runaway_loop", result.stdout)
        self.assertNotIn(SENTINEL, result.stdout)

    def test_serve_returns_the_cli_report_to_the_owner_only(self):
        from aisdlc import serve as serve_mod
        from aisdlc.cli import load_context
        from aisdlc.serve_token import add_token, ensure_token

        owner = ensure_token(self.repo.root, scope="owner")
        reader = add_token(self.repo.root, identity="reader", scope="read")
        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        with mock.patch.dict(os.environ, {"AISDLC_COACH_HOME": str(self.home)}):
            server = serve_mod.start_server(ctx, host="127.0.0.1", port=0)
            threading.Thread(target=server.serve_forever, daemon=True).start()
            self.addCleanup(server.shutdown)
            self.addCleanup(server.server_close)
            port = server.server_address[1]
            status, served = self._get(port, "/factory/coach", owner.value)
            denied, problem = self._get(port, "/factory/coach", reader.value)
        self.assertEqual(200, status, served)
        self.assertEqual(self._coach(), served)
        self.assertEqual(403, denied)
        self.assertIn("owner", problem["detail"])

    @staticmethod
    def _get(port: int, path: str, token: str) -> tuple[int, dict]:
        req = urllib.request.Request(f"http://127.0.0.1:{port}{path}", headers={"Authorization": f"Bearer {token}"})
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                return resp.status, json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:
            return exc.code, json.loads(exc.read().decode("utf-8"))

    def test_the_contract_describes_the_coach_path(self):
        from aisdlc import contract

        text = contract.load_contract_text(KIT)
        self.assertIn("/factory/coach", text)
        self.assertIn("CoachReport", text)
        self.assertEqual([], contract.validate_contract(KIT))


if __name__ == "__main__":
    unittest.main()
