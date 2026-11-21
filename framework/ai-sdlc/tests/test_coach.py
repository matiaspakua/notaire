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

    def _coach(self, *args: str) -> dict:
        result = self.repo.run("coach", "--home", str(self.home), "--json", *args)
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn(SENTINEL, result.stdout)
        self.assertNotIn("must-never-be-read", result.stdout)
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
        self.assertEqual({"claude-opus-5-5": 48}, self._harness(report, "claude-code")["models"])
        self.assertEqual({"shell": 2}, self._harness(report, "codex")["tools"])
        self.assertIsNone(self._harness(report, "agy")["tool_calls"])
        self.assertEqual(sum(report["hours"]), report["totals"]["user_turns"])

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
        self.assertEqual(6, report["totals"]["sessions"])
        self.assertGreater(report["totals"]["healthy_sessions"], 0)
        self.assertLess(report["totals"]["healthy_sessions"], report["totals"]["sessions"])

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
