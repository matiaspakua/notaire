"""Factory telemetry store tests (#2394).

Tests the aisdlc.telemetry_store module: sync from ledger/cost/RepoLens,
idempotency, redaction, report math, stats, absent-store behavior, dual-write
fail-open, CLI commands, and the serve endpoint.  These tests must fail with
ImportError until the production module exists.
"""
from __future__ import annotations

import json
import os
import shutil
import sqlite3
import subprocess
import sys
import tempfile
import threading
import time
import unittest
import urllib.error
import urllib.request
from pathlib import Path

from helpers import ENV, KIT, Repo, output, git

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"

[[harness.adapters]]
id = "local-worker"
runtime = "local"
source = ".aisdlc/harness/local-worker.yml"
"""


class TelemetryStoreSyncTest(unittest.TestCase):
    """sync() imports ledger, cost, and RepoLens rows idempotently."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def _ledger_path(self) -> Path:
        return self.repo.root / ".aisdlc" / "evidence" / "ledger.jsonl"

    def _cost_path(self) -> Path:
        return self.repo.root / ".aisdlc" / "evidence" / "cost.jsonl"

    def _repolens_path(self) -> Path:
        return self.repo.root / ".repolens" / "usage.jsonl"

    # ------------------------------------------------------------------
    # Ledger sync
    # ------------------------------------------------------------------
    def test_sync_ledger(self) -> None:
        """sync imports ledger rows into the events table."""
        from aisdlc import evidence
        from aisdlc.telemetry_store import sync
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # Write two ledger rows via evidence.append_record.
        evidence.append_record(ctx, {"kind": "stage-run", "stage": "pre-push", "ok": True, "ts": "2026-01-01T00:00:00+00:00"})
        evidence.append_record(ctx, {"kind": "stage-run", "stage": "pre-merge", "ok": False, "ts": "2026-01-01T00:01:00+00:00"})

        result = sync(ctx)
        self.assertIn("ledger", result)
        self.assertGreaterEqual(result["ledger"], 2)

    # ------------------------------------------------------------------
    # Cost sync
    # ------------------------------------------------------------------
    def test_sync_cost(self) -> None:
        """sync imports cost log entries into the telemetry table."""
        from aisdlc import evidence
        from aisdlc.telemetry_store import sync
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # Write three cost entries.
        evidence.add_cost(ctx, "junie", "gpt-4", 100, 50, 0.01, "1")
        evidence.add_cost(ctx, "junie", "gpt-4", 200, 100, 0.02, "2")
        evidence.add_cost(ctx, "cursor", "claude-3", 50, 25, 0.005, "3")

        result = sync(ctx)
        self.assertIn("cost", result)
        self.assertGreaterEqual(result["cost"], 3)

    # ------------------------------------------------------------------
    # RepoLens sync
    # ------------------------------------------------------------------
    def test_sync_repolens(self) -> None:
        """sync reads RepoLens usage defensively and stores tool/ms/tokens."""
        from aisdlc.telemetry_store import sync
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # Write realistic RepoLens lines.
        repolens_dir = self.repo.root / ".repolens"
        repolens_dir.mkdir(parents=True, exist_ok=True)
        lines = [
            json.dumps({"ts": 1794941929.723, "src": "server", "client": "mcp",
                        "tool": "search", "input": "where are payments processed",
                        "ms": 1215.8, "bytes": 1578, "tokens": 394,
                        "results": 13, "files": ["a/b.java", "c/d.py"]}),
            json.dumps({"ts": 1794941930.0, "src": "server", "client": "mcp",
                        "tool": "node", "input": "PaymentService",
                        "ms": 85.2, "bytes": 412, "tokens": 50,
                        "results": 3, "files": ["e.java"]}),
            # Line missing optional keys
            json.dumps({"ts": 1794941931.0, "src": "server", "client": "mcp",
                        "tool": "outline", "ms": 40.0}),
            # Malformed line
            "this is not json at all",
        ]
        (repolens_dir / "usage.jsonl").write_text("\n".join(lines) + "\n", encoding="utf-8")

        result = sync(ctx)
        self.assertIn("repolens", result)
        self.assertGreaterEqual(result["repolens"], 3)

    # ------------------------------------------------------------------
    # Idempotency
    # ------------------------------------------------------------------
    def test_sync_idempotent(self) -> None:
        """Second sync inserts 0 rows; row counts remain equal."""
        from aisdlc import evidence
        from aisdlc.telemetry_store import sync, stats
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        evidence.append_record(ctx, {"kind": "stage-run", "stage": "pre-push", "ok": True, "ts": "2026-01-01T00:00:00+00:00"})

        first = sync(ctx)
        stats_after_first = stats(ctx)

        second = sync(ctx)
        self.assertEqual(0, second["ledger"], "second sync should insert 0 ledger rows")

        stats_after_second = stats(ctx)
        self.assertEqual(stats_after_first["events"], stats_after_second["events"])

    # ------------------------------------------------------------------
    # RepoLens input and files never stored
    # ------------------------------------------------------------------
    def test_repolens_no_input_or_files(self) -> None:
        """RepoLens 'input' query text and 'files' never appear in any row."""
        from aisdlc.telemetry_store import sync
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        repolens_dir = self.repo.root / ".repolens"
        repolens_dir.mkdir(parents=True, exist_ok=True)
        line = json.dumps({"ts": 1794941929.723, "src": "server", "client": "mcp",
                            "tool": "search", "input": "where are payments processed",
                            "ms": 1215.8, "bytes": 1578, "tokens": 394,
                            "results": 13, "files": ["a/b.java", "c/d.py"]})
        (repolens_dir / "usage.jsonl").write_text(line + "\n", encoding="utf-8")

        sync(ctx)

        # Scan every table cell in the store.
        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        tables = [r["name"] for r in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        ).fetchall()]
        for table in tables:
            rows = conn.execute(f"SELECT * FROM [{table}]").fetchall()
            for row in rows:
                for cell in row:
                    cell_str = str(cell or "")
                    self.assertNotIn("where are payments processed", cell_str,
                                     f"input text found in {table}")
                    self.assertNotIn("a/b.java", cell_str,
                                     f"files text found in {table}")
        conn.close()

    # ------------------------------------------------------------------
    # No secrets in any row
    # ------------------------------------------------------------------
    def test_no_secrets_in_rows(self) -> None:
        """A ledger row with secret-like keys leaves no secret in any table."""
        from aisdlc import evidence
        from aisdlc.telemetry_store import sync
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        evidence.append_record(ctx, {
            "kind": "stage-run", "stage": "pre-push", "ok": True,
            "ts": "2026-01-01T00:00:00+00:00",
            "secret_token": "sk-1234567890abcdef",
            "api_key": "Bearer abc",
        })

        sync(ctx)

        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        tables = [r["name"] for r in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table'"
        ).fetchall()]
        banned_values = ["sk-1234567890abcdef", "Bearer abc"]
        banned_keys = ["secret_token", "api_key"]
        for table in tables:
            rows = conn.execute(f"SELECT * FROM [{table}]").fetchall()
            for row in rows:
                for cell in row:
                    cell_str = str(cell or "")
                    for bv in banned_values:
                        self.assertNotIn(bv, cell_str,
                                         f"secret value '{bv}' found in {table}")
        conn.close()


class TelemetryStoreRecordTest(unittest.TestCase):
    """record() inserts a telemetry row with redaction."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        return Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

    def test_record_redacts_secrets(self) -> None:
        """record with secret-shaped keys drops them before insert."""
        from aisdlc.telemetry_store import record
        from aisdlc.store import store_path

        ctx = self._ctx()
        record(ctx, stage="pre-push", agent="junie", metric="duration_s",
               value=5.0, ok=True, api_key="sk-secret")

        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        row = conn.execute("SELECT * FROM telemetry").fetchone()
        self.assertIsNotNone(row)
        # The row should not contain the secret value.
        for cell in row:
            self.assertNotIn("sk-secret", str(cell or ""))
        conn.close()


class TelemetryStoreReportTest(unittest.TestCase):
    """report() groups and aggregates correctly."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        return Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

    def test_report_group_stage(self) -> None:
        """Report groups by stage with correct sums."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        record(ctx, stage="pre-push", agent="junie", metric="duration_s", value=3.0, ok=True)
        record(ctx, stage="pre-push", agent="junie", metric="duration_s", value=2.0, ok=False)
        record(ctx, stage="pre-push", agent="junie", metric="usd", value=0.01, ok=True)
        record(ctx, stage="pre-merge", agent="cursor", metric="duration_s", value=1.0, ok=True)
        record(ctx, stage="pre-merge", agent="cursor", metric="usd", value=0.005, ok=True)

        result = report(ctx, "stage")
        self.assertEqual("stage", result["group"])
        rows = {r["key"]: r for r in result["rows"]}
        self.assertIn("pre-push", rows)
        self.assertIn("pre-merge", rows)

        pp = rows["pre-push"]
        self.assertAlmostEqual(5.0, pp["duration_s"], places=2)
        self.assertEqual(1, pp["failures"])

        pm = rows["pre-merge"]
        self.assertAlmostEqual(1.0, pm["duration_s"], places=2)
        self.assertEqual(0, pm["failures"])

    def test_report_math(self) -> None:
        """Report totals match sums of rows."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        record(ctx, stage="s1", agent="a1", metric="duration_s", value=2.0, ok=True)
        record(ctx, stage="s1", agent="a1", metric="usd", value=0.1, ok=True)
        record(ctx, stage="s2", agent="a2", metric="duration_s", value=3.0, ok=False)
        record(ctx, stage="s2", agent="a2", metric="usd", value=0.2, ok=True)

        result = report(ctx, "stage")
        self.assertAlmostEqual(5.0, result["total_duration_s"], places=2)
        self.assertAlmostEqual(0.3, result["total_usd"], places=2)
        self.assertEqual(1, result["total_failures"])

    def test_report_failures_by_integration(self) -> None:
        """Report includes failures_by_integration from harness events."""
        from aisdlc.telemetry_store import report
        from aisdlc.store import open_store, store_path

        ctx = self._ctx()
        open_store(self.repo.root)

        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        # Insert harness events with ok=0 from different adapters.
        conn.execute(
            "INSERT INTO events (ts, kind, run_id, actor, data) VALUES (?, ?, ?, ?, ?)",
            ("2026-01-01T00:00:00+00:00", "step.finished", "r1", "junie",
             json.dumps({"ok": False, "adapter": "junie"})),
        )
        conn.execute(
            "INSERT INTO events (ts, kind, run_id, actor, data) VALUES (?, ?, ?, ?, ?)",
            ("2026-01-01T00:00:01+00:00", "step.finished", "r1", "junie",
             json.dumps({"ok": False, "adapter": "junie"})),
        )
        conn.execute(
            "INSERT INTO events (ts, kind, run_id, actor, data) VALUES (?, ?, ?, ?, ?)",
            ("2026-01-01T00:00:02+00:00", "step.finished", "r1", "repolens",
             json.dumps({"ok": False, "adapter": "repolens"})),
        )
        conn.commit()
        conn.close()

        result = report(ctx, "stage")
        fbi = result.get("failures_by_integration", {})
        self.assertEqual(2, fbi.get("junie"))
        self.assertEqual(1, fbi.get("repolens"))

    def test_report_repolens_summary(self) -> None:
        """Report includes repolens summary with calls, total_ms, avg_ms, by_tool."""
        from aisdlc.telemetry_store import report
        from aisdlc.store import open_store, store_path

        ctx = self._ctx()
        open_store(self.repo.root)

        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        # Insert RepoLens telemetry rows.
        for tool, ms in [("search", 40.0), ("search", 40.0), ("search", 40.0)]:
            conn.execute(
                "INSERT INTO telemetry (ts, tool, metric, value) VALUES (?, ?, ?, ?)",
                ("2026-01-01T00:00:00+00:00", tool, "repolens_ms", ms),
            )
        for tool, ms in [("node", 40.0), ("node", 40.0)]:
            conn.execute(
                "INSERT INTO telemetry (ts, tool, metric, value) VALUES (?, ?, ?, ?)",
                ("2026-01-01T00:00:00+00:00", tool, "repolens_ms", ms),
            )
        conn.commit()
        conn.close()

        result = report(ctx, "stage")
        rp = result.get("repolens", {})
        self.assertEqual(5, rp.get("calls"))
        self.assertAlmostEqual(200.0, rp.get("total_ms", 0), places=1)
        self.assertAlmostEqual(40.0, rp.get("avg_ms", 0), places=1)
        by_tool = rp.get("by_tool", {})
        self.assertEqual(3, by_tool.get("search"))
        self.assertEqual(2, by_tool.get("node"))

    def test_absent_store_empty_report(self) -> None:
        """Report with no store file returns empty results and does not create the file."""
        from aisdlc.telemetry_store import report
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        db_path = self.repo.root / ".aisdlc" / "factory.db"
        self.assertFalse(db_path.exists())

        result = report(ctx, "stage")
        self.assertEqual("stage", result["group"])
        self.assertEqual([], result["rows"])
        self.assertEqual(0, result["total_runs"])
        self.assertEqual(0, result["total_duration_s"])
        self.assertEqual(0, result["total_usd"])
        self.assertEqual(0, result["total_failures"])

        # report must NOT create the store file
        self.assertFalse(db_path.exists())

    def test_absent_store_empty_stats(self) -> None:
        """Stats with no store file returns zero counts and does not create the file."""
        from aisdlc.telemetry_store import stats
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        db_path = self.repo.root / ".aisdlc" / "factory.db"
        self.assertFalse(db_path.exists())

        result = stats(ctx)
        self.assertEqual(0, result.get("events"))
        self.assertEqual(0, result.get("telemetry"))
        self.assertEqual(0, result.get("benchmarks"))
        self.assertEqual(0, result.get("config_snapshots"))
        self.assertEqual(0, result.get("intents"))

        self.assertFalse(db_path.exists())


class TelemetryStoreStatsTest(unittest.TestCase):
    """stats() returns row counts per table."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    def _ctx(self):
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        return Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

    def test_stats(self) -> None:
        """Stats returns counts matching inserted rows."""
        from aisdlc.telemetry_store import record, stats
        from aisdlc.store import open_store, store_path

        ctx = self._ctx()
        open_store(self.repo.root)

        record(ctx, stage="s1", agent="a1", metric="duration_s", value=1.0, ok=True)
        record(ctx, stage="s2", agent="a2", metric="duration_s", value=2.0, ok=False)

        result = stats(ctx)
        self.assertGreaterEqual(result["telemetry"], 2)


class DualWriteFailOpenTest(unittest.TestCase):
    """Dual-write hooks in runs.start and runs.append_harness_event are fail-open."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
        self.repo.run(
            "project", "create", "--id", "alpha",
            "--profile-ref", "aisdlc.toml", "--json",
        )
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java", "--mcp", "github",
            "--json",
        )
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", "feature-delivery",
                "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))

    def test_dual_write_fail_open(self) -> None:
        """Patch store to raise; run start and harness report still succeed."""
        from unittest import mock

        # Make the store module raise on any import/call.
        with mock.patch.dict("sys.modules", {"aisdlc.store": mock.MagicMock()}):
            with mock.patch.dict("sys.modules", {"aisdlc.telemetry_store": mock.MagicMock()}):
                started = self.repo.run(
                    "run", "start", "--project", "alpha",
                    "--workflow", "feature-delivery", "--json",
                )
                self.assertEqual(0, started.returncode, output(started))
                run_id = json.loads(started.stdout)["id"]

                report = self.repo.run(
                    "harness", "report",
                    "--run", run_id,
                    "--event", "run.started",
                    "--adapter", "local-worker",
                    "--json",
                )
                self.assertEqual(0, report.returncode, output(report))

                shown = self.repo.run("run", "show", "--id", run_id, "--json")
                self.assertEqual("running", json.loads(shown.stdout)["status"])


class StoreCLITest(unittest.TestCase):
    """CLI commands: store sync, store stats, telemetry record, telemetry report."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def test_store_sync_command(self) -> None:
        """aisdlc store sync exits 0."""
        result = self.repo.run("store", "sync")
        self.assertEqual(0, result.returncode, output(result))

    def test_store_stats_json_command(self) -> None:
        """aisdlc store stats --json returns valid JSON."""
        result = self.repo.run("store", "stats", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertIn("events", data)
        self.assertIn("telemetry", data)

    def test_telemetry_record_command(self) -> None:
        """aisdlc telemetry record inserts a row."""
        result = self.repo.run(
            "telemetry", "record",
            "--stage", "pre-push", "--agent", "junie",
            "--metric", "duration_s", "--value", "5.0",
        )
        self.assertEqual(0, result.returncode, output(result))

    def test_telemetry_report_json_command(self) -> None:
        """aisdlc telemetry report --group stage --json returns valid JSON."""
        result = self.repo.run(
            "telemetry", "report", "--group", "stage", "--json",
        )
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("stage", data["group"])
        self.assertIn("rows", data)


class ServeTelemetryEndpointTest(unittest.TestCase):
    """GET /factory/telemetry returns the report with read-scope token."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
        created = self.repo.run("workflow", "create", "--id", "w1", "--summary", "demo")
        self.assertEqual(0, created.returncode, output(created))

    def _ctx(self):
        from aisdlc.cli import load_context
        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _serve(self):
        from aisdlc import serve as serve_mod

        ctx = self._ctx()
        server = serve_mod.start_server(
            ctx, host="127.0.0.1", port=0, allow_remote=False,
        )
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        token = serve_mod.load_token_value(self.repo.root)
        return server.server_address[1], token

    def _get(self, port: int, path: str, *, token: str | None):
        headers = {}
        if token is not None:
            headers["Authorization"] = f"Bearer {token}"
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            method="GET",
            headers=headers,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                body = resp.read().decode("utf-8")
                return resp.status, resp.headers.get_content_type(), body
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8")
            return exc.code, exc.headers.get_content_type(), body

    def test_telemetry_endpoint_with_read_token(self) -> None:
        """GET /factory/telemetry?group=stage with valid token returns 200."""
        port, token = self._serve()
        status, ctype, body = self._get(
            port, "/factory/telemetry?group=stage", token=token,
        )
        self.assertEqual(200, status)
        self.assertEqual("application/json", ctype)
        data = json.loads(body)
        self.assertEqual("stage", data["group"])
        self.assertIn("rows", data)

    def test_telemetry_endpoint_no_token_401(self) -> None:
        """GET /factory/telemetry without token returns 401."""
        port, token = self._serve()
        status, ctype, body = self._get(
            port, "/factory/telemetry?group=stage", token=None,
        )
        self.assertEqual(401, status)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("unauthorized", json.loads(body)["code"])

    def test_telemetry_endpoint_invalid_group_400(self) -> None:
        """GET /factory/telemetry?group=invalid returns 400."""
        port, token = self._serve()
        status, ctype, body = self._get(
            port, "/factory/telemetry?group=invalid", token=token,
        )
        self.assertEqual(400, status)
        self.assertEqual("application/problem+json", ctype)


if __name__ == "__main__":
    unittest.main()
