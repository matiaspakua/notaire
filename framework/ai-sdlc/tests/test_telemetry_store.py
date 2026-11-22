"""Factory telemetry store tests (#2394).

Tests the aisdlc.telemetry_store module: sync from ledger/cost/RepoLens,
idempotency, redaction, report math, stats, absent-store behavior, dual-write
fail-open, CLI commands, and the serve endpoint.  These tests must fail with
ImportError until the production module exists.
"""
from __future__ import annotations

import json
import sqlite3
import sys
import threading
import unittest
import urllib.error
import urllib.request
from pathlib import Path

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

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
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import sync

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # Write two REAL stage-run ledger rows (no 'kind' key, has 'gates' and 'stage').
        evidence.append_record(ctx, {
            "stage": "pre-push", "ok": True, "ts": "2026-01-01T00:00:00+00:00",
            "gates": [{"exit": 0, "expect": "pass", "name": "gate1", "required": True,
                        "seconds": 1.5, "status": "pass", "tail": [], "timed_out": False}],
        })
        evidence.append_record(ctx, {
            "stage": "pre-merge", "ok": False, "ts": "2026-01-01T00:01:00+00:00",
            "gates": [{"exit": 1, "expect": "pass", "name": "gate2", "required": True,
                        "seconds": 2.0, "status": "fail", "tail": [], "timed_out": False}],
        })

        # sync is idempotent — mirror already wrote, so sync inserts 0 new.
        result = sync(ctx)
        self.assertIn("ledger", result)
        self.assertEqual(0, result["ledger"], "mirror already imported; sync inserts 0")

        # But the events table should have the rows from mirror.
        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        rows = conn.execute("SELECT kind FROM events WHERE kind='stage-run'").fetchall()
        self.assertGreaterEqual(len(rows), 2)

        # Telemetry should have duration_s derived from gate seconds.
        telem = conn.execute(
            "SELECT stage, metric, value FROM telemetry WHERE metric='duration_s'"
        ).fetchall()
        conn.close()
        self.assertTrue(len(telem) >= 2)
        stage_durations = {r[0]: r[2] for r in telem}
        self.assertAlmostEqual(1.5, stage_durations.get("pre-push", 0), places=1)
        self.assertAlmostEqual(2.0, stage_durations.get("pre-merge", 0), places=1)

    # ------------------------------------------------------------------
    # Cost sync
    # ------------------------------------------------------------------
    def test_sync_cost(self) -> None:
        """sync imports cost log entries into the telemetry table."""
        from aisdlc import evidence
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import sync

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # Write three cost entries (mirror writes them).
        evidence.add_cost(ctx, "junie", "gpt-4", 100, 50, 0.01, "1")
        evidence.add_cost(ctx, "junie", "gpt-4", 200, 100, 0.02, "2")
        evidence.add_cost(ctx, "cursor", "claude-3", 50, 25, 0.005, "3")

        # sync is idempotent — mirror already wrote, so sync inserts 0 new.
        result = sync(ctx)
        self.assertIn("cost", result)
        self.assertEqual(0, result["cost"], "mirror already imported; sync inserts 0")

        # But the telemetry table should have the cost rows from mirror.
        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        rows = conn.execute("SELECT agent, metric FROM telemetry WHERE metric='usd'").fetchall()
        conn.close()
        self.assertGreaterEqual(len(rows), 3)

    def test_sync_cost_maps_to_telemetry_rows(self) -> None:
        """Cost sync maps cost.jsonl rows to telemetry rows with usd, tokens_in, tokens_out."""
        from aisdlc import evidence
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import sync

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        evidence.add_cost(ctx, "junie", "gpt-4", 100, 50, 0.01, "1")
        evidence.add_cost(ctx, "cursor", "claude-3", 200, 100, 0.02, "2")

        sync(ctx)

        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        rows = conn.execute(
            "SELECT agent, model, metric, value FROM telemetry ORDER BY metric"
        ).fetchall()
        conn.close()

        # Should have usd, tokens_in, tokens_out metrics.
        metrics = [r[2] for r in rows]
        self.assertIn("usd", metrics)
        self.assertIn("tokens_in", metrics)
        self.assertIn("tokens_out", metrics)
        # Agents and models should be populated.
        agents = [r[0] for r in rows]
        self.assertTrue(any(a == "junie" for a in agents))
        self.assertTrue(any(a == "cursor" for a in agents))

    # ------------------------------------------------------------------
    # RepoLens sync
    # ------------------------------------------------------------------
    def test_sync_repolens(self) -> None:
        """sync reads RepoLens usage defensively and stores duration_s/tokens_out."""
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import sync

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

        # Verify telemetry rows use duration_s metric with agent='repolens'
        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        telem = conn.execute(
            "SELECT agent, tool, metric, value FROM telemetry "
            "WHERE agent='repolens' ORDER BY tool, metric"
        ).fetchall()
        conn.close()
        # Should have duration_s rows (ms/1000)
        metrics = [r[2] for r in telem]
        self.assertIn("duration_s", metrics)
        # Agent should be repolens
        agents = [r[0] for r in telem]
        self.assertTrue(all(a == "repolens" for a in agents))

    # ------------------------------------------------------------------
    # Idempotency
    # ------------------------------------------------------------------
    def test_sync_idempotent(self) -> None:
        """Second sync inserts 0 rows; mirror + sync are idempotent with each other."""
        from aisdlc import evidence
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.telemetry_store import stats, sync

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # REAL stage-run shape: no 'kind', has 'gates' and 'stage'
        evidence.append_record(ctx, {
            "stage": "pre-push", "ok": True, "ts": "2026-01-01T00:00:00+00:00",
            "gates": [{"exit": 0, "expect": "pass", "name": "g1", "required": True,
                        "seconds": 1.0, "status": "pass", "tail": [], "timed_out": False}],
        })

        first = sync(ctx)
        self.assertEqual(0, first["ledger"], "mirror already imported; sync inserts 0")
        stats_after_first = stats(ctx)

        second = sync(ctx)
        self.assertEqual(0, second["ledger"], "second sync should insert 0 ledger rows")

        stats_after_second = stats(ctx)
        self.assertEqual(stats_after_first["events"], stats_after_second["events"])

        # Append one new ledger line and sync again — mirror imports it, sync inserts 0.
        evidence.append_record(ctx, {
            "stage": "pre-merge", "ok": True, "ts": "2026-01-01T00:02:00+00:00",
            "gates": [{"exit": 0, "expect": "pass", "name": "g2", "required": True,
                        "seconds": 0.5, "status": "pass", "tail": [], "timed_out": False}],
        })
        third = sync(ctx)
        self.assertEqual(0, third["ledger"], "mirror already imported; sync inserts 0")
        # But events count should have grown by 1 (from mirror)
        self.assertEqual(stats_after_second["events"] + 1, stats(ctx)["events"])

    def test_sync_repolens_malformed_and_missing_keys(self) -> None:
        """Sync does not raise on malformed JSON or missing optional keys;
        malformed lines are marked but not counted as imported rows."""
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.telemetry_store import sync

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        repolens_dir = self.repo.root / ".repolens"
        repolens_dir.mkdir(parents=True, exist_ok=True)
        lines = [
            # Valid line
            json.dumps({"ts": 1794941929.0, "src": "server", "client": "mcp",
                        "tool": "search", "ms": 100.0}),
            # Malformed JSON line (marked but not counted)
            "this is not json at all {{{",
            # Line missing optional keys (tokens, results)
            json.dumps({"ts": 1794941930.0, "src": "server", "client": "mcp",
                        "tool": "node", "ms": 50.0}),
            # Another valid line
            json.dumps({"ts": 1794941931.0, "src": "server", "client": "mcp",
                        "tool": "outline", "ms": 30.0, "tokens": 10}),
        ]
        (repolens_dir / "usage.jsonl").write_text("\n".join(lines) + "\n", encoding="utf-8")

        # Should not raise.
        result = sync(ctx)
        self.assertIn("repolens", result)
        # 3 valid lines counted; malformed line is marked but returns False
        self.assertEqual(3, result["repolens"])

        # Second sync should insert 0 (idempotent — malformed already marked)
        second = sync(ctx)
        self.assertEqual(0, second["repolens"])

    # ------------------------------------------------------------------
    # RepoLens input and files never stored
    # ------------------------------------------------------------------
    def test_repolens_no_input_or_files(self) -> None:
        """RepoLens 'input' query text and 'files' never appear in any row."""
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import sync

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
        tables = [r[0] for r in conn.execute(
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
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import sync

        ctx = Context(
            root=self.repo.root,
            profile=load_profile(find_profile(self.repo.root)),
            profile_path=find_profile(self.repo.root),
        )

        # REAL stage-run shape with secret-like keys
        evidence.append_record(ctx, {
            "stage": "pre-push", "ok": True,
            "ts": "2026-01-01T00:00:00+00:00",
            "gates": [{"exit": 0, "expect": "pass", "name": "g1", "required": True,
                        "seconds": 0.5, "status": "pass", "tail": [], "timed_out": False}],
            "secret_token": "sk-1234567890abcdef",
            "api_key": "Bearer abc",
        })

        sync(ctx)

        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        tables = [r[0] for r in conn.execute(
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
                    for bk in banned_keys:
                        self.assertNotIn(bk, cell_str,
                                         f"banned key '{bk}' found in {table}")
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
        from aisdlc.store import store_path
        from aisdlc.telemetry_store import record

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
        # Report rows no longer have repolens_calls/repolens_ms
        self.assertNotIn("repolens_calls", pp)
        self.assertNotIn("repolens_ms", pp)

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
        """Report includes failures_by_integration from telemetry ok=0 rows."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        # Record failures with ok=0 for different agents
        record(ctx, stage="s1", agent="junie", metric="duration_s", value=2.0, ok=False)
        record(ctx, stage="s1", agent="junie", metric="duration_s", value=1.0, ok=False)
        record(ctx, stage="s2", agent="repolens", metric="duration_s", value=0.5, ok=False)

        result = report(ctx, "stage")
        fbi = result.get("failures_by_integration", {})
        self.assertEqual(2, fbi.get("junie"))
        self.assertEqual(1, fbi.get("repolens"))

    def test_report_repolens_summary(self) -> None:
        """Report includes repolens summary from real sync of usage.jsonl."""
        from aisdlc.telemetry_store import report, sync

        ctx = self._ctx()

        # Write realistic .repolens/usage.jsonl.
        repolens_dir = self.repo.root / ".repolens"
        repolens_dir.mkdir(parents=True, exist_ok=True)
        lines = [
            json.dumps({"ts": 1794941929.723, "src": "server", "client": "mcp",
                        "tool": "search", "ms": 40.0, "tokens": 100, "results": 5}),
            json.dumps({"ts": 1794941930.0, "src": "server", "client": "mcp",
                        "tool": "search", "ms": 80.0, "tokens": 200, "results": 10}),
            json.dumps({"ts": 1794941931.0, "src": "server", "client": "mcp",
                        "tool": "search", "ms": 100.0, "tokens": 150, "results": 8}),
            json.dumps({"ts": 1794941932.0, "src": "server", "client": "mcp",
                        "tool": "node", "ms": 50.0, "tokens": 50, "results": 2}),
            json.dumps({"ts": 1794941933.0, "src": "server", "client": "mcp",
                        "tool": "node", "ms": 30.0, "tokens": 40, "results": 1}),
        ]
        (repolens_dir / "usage.jsonl").write_text("\n".join(lines) + "\n", encoding="utf-8")

        sync(ctx)
        result = report(ctx, "stage")
        rp = result.get("repolens", {})
        self.assertEqual(5, rp.get("calls"))
        # ms values stored as duration_s (ms/1000), total_ms = sum * 1000
        self.assertAlmostEqual(300.0, rp.get("total_ms", 0), places=1)
        self.assertAlmostEqual(60.0, rp.get("avg_ms", 0), places=1)
        by_tool = rp.get("by_tool", {})
        self.assertEqual(3, by_tool.get("search"))
        self.assertEqual(2, by_tool.get("node"))

    def test_report_group_intent(self) -> None:
        """Report groups by intent with correct aggregation."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        record(ctx, intent_id="i1", stage="s1", agent="junie", metric="duration_s", value=2.0, ok=True)
        record(ctx, intent_id="i1", stage="s1", agent="junie", metric="usd", value=0.01, ok=True)
        record(ctx, intent_id="i2", stage="s2", agent="cursor", metric="duration_s", value=3.0, ok=False)
        record(ctx, intent_id="i2", stage="s2", agent="cursor", metric="usd", value=0.02, ok=True)

        result = report(ctx, "intent")
        self.assertEqual("intent", result["group"])
        rows = {r["key"]: r for r in result["rows"]}
        self.assertIn("i1", rows)
        self.assertIn("i2", rows)
        self.assertAlmostEqual(2.0, rows["i1"]["duration_s"], places=2)
        self.assertEqual(1, rows["i2"]["failures"])

    def test_intent_id_propagates_from_run_request(self) -> None:
        """run.request with intent -> step.started/finished -> stage report grouped by intent
        shows the duration and runs=1."""
        from aisdlc.store import open_store
        from aisdlc.telemetry_store import import_line, report

        conn = open_store(self.repo.root)
        run_id = "run-001"

        # 1. run.request with intent
        import_line(conn, "ledger", json.dumps({
            "kind": "run.request", "run_id": run_id,
            "intent": "fix-login", "ts": "2026-01-01T00:00:00+00:00",
        }))
        # 2. step.started
        import_line(conn, "ledger", json.dumps({
            "kind": "step.started", "run_id": run_id,
            "step": "compile", "ts": "2026-01-01T00:01:00+00:00",
        }))
        # 3. step.finished (10 seconds later)
        import_line(conn, "ledger", json.dumps({
            "kind": "step.finished", "run_id": run_id,
            "step": "compile", "ts": "2026-01-01T00:01:10+00:00",
        }))
        conn.commit()
        conn.close()

        # All events and telemetry for this run should carry intent_id="fix-login"
        from aisdlc.store import store_path
        conn2 = sqlite3.connect(str(store_path(self.repo.root)))
        intent_events = conn2.execute(
            "SELECT COUNT(*) FROM events WHERE run_id=? AND intent_id='fix-login'",
            (run_id,),
        ).fetchone()[0]
        self.assertEqual(3, intent_events, "all 3 events should have intent_id")

        intent_telem = conn2.execute(
            "SELECT COUNT(*) FROM telemetry WHERE run_id=? AND intent_id='fix-login'",
            (run_id,),
        ).fetchone()[0]
        self.assertGreaterEqual(intent_telem, 1, "telemetry should have intent_id")
        conn2.close()

        # Report grouped by intent should show the duration
        ctx = self._ctx()
        result = report(ctx, "intent")
        rows = {r["key"]: r for r in result["rows"]}
        self.assertIn("fix-login", rows)
        fix_row = rows["fix-login"]
        self.assertGreater(fix_row["duration_s"], 0, "duration should be > 0")
        self.assertGreaterEqual(fix_row["runs"], 1)

    def test_stage_run_import_with_real_shape(self) -> None:
        """Import a REAL stage-run row (no 'kind', has 'gates' and 'stage') and
        verify duration_s is derived from gate seconds and tail is stripped."""
        from aisdlc.store import open_store
        from aisdlc.telemetry_store import import_line

        conn = open_store(self.repo.root)

        # REAL stage-run shape from ledger (no 'kind' key)
        real_row = {
            "base": "origin/main",
            "commit": "abc123",
            "gates": [
                {"exit": 0, "expect": "pass", "name": "baseline ratchet",
                 "required": True, "seconds": 0.165, "status": "pass",
                 "tail": ["some command output"], "timed_out": False},
                {"exit": 0, "expect": "pass", "name": "skills lint",
                 "required": True, "seconds": 0.076, "status": "pass",
                 "tail": [], "timed_out": False},
            ],
            "ok": True,
            "prev": "genesis",
            "stage": "signal",
            "ts": "2026-11-17T17:59:51+00:00",
        }
        imported = import_line(conn, "ledger", json.dumps(real_row))
        self.assertTrue(imported)
        conn.commit()

        # Verify events row has kind='stage-run'
        ev = conn.execute(
            "SELECT kind, stage, data FROM events WHERE kind='stage-run'"
        ).fetchone()
        self.assertIsNotNone(ev)
        self.assertEqual("signal", ev[1])
        # Verify tail is stripped from data
        data = json.loads(ev[2])
        for gate in data.get("gates", []):
            self.assertNotIn("tail", gate, "tail should be stripped from stored data")

        # Verify telemetry row has duration_s = sum of gate seconds
        telem = conn.execute(
            "SELECT stage, metric, value FROM telemetry WHERE metric='duration_s'"
        ).fetchone()
        self.assertIsNotNone(telem)
        self.assertEqual("signal", telem[0])
        expected_dur = 0.165 + 0.076
        self.assertAlmostEqual(expected_dur, telem[2], places=4)
        conn.close()

    def test_report_group_agent(self) -> None:
        """Report groups by agent with correct failure counts."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        record(ctx, stage="s1", agent="junie", metric="duration_s", value=2.0, ok=True)
        record(ctx, stage="s1", agent="junie", metric="duration_s", value=1.0, ok=False)
        record(ctx, stage="s1", agent="junie", metric="duration_s", value=3.0, ok=False)
        record(ctx, stage="s2", agent="cursor", metric="duration_s", value=1.0, ok=True)

        result = report(ctx, "agent")
        self.assertEqual("agent", result["group"])
        rows = {r["key"]: r for r in result["rows"]}
        self.assertIn("junie", rows)
        self.assertIn("cursor", rows)
        self.assertEqual(2, rows["junie"]["failures"])
        self.assertEqual(0, rows["cursor"]["failures"])

    def test_report_group_skill(self) -> None:
        """Report groups by skill with correct aggregation."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        record(ctx, stage="s1", agent="junie", skill="java", metric="duration_s", value=2.0, ok=True)
        record(ctx, stage="s1", agent="junie", skill="python", metric="duration_s", value=3.0, ok=False)

        result = report(ctx, "skill")
        self.assertEqual("skill", result["group"])
        rows = {r["key"]: r for r in result["rows"]}
        self.assertIn("java", rows)
        self.assertIn("python", rows)

    def test_report_group_tool(self) -> None:
        """Report groups by tool with correct aggregation."""
        from aisdlc.telemetry_store import record, report

        ctx = self._ctx()
        record(ctx, stage="s1", agent="junie", tool="repolens", metric="duration_s", value=0.5, ok=True)
        record(ctx, stage="s1", agent="junie", tool="worker", metric="duration_s", value=1.5, ok=True)

        result = report(ctx, "tool")
        self.assertEqual("tool", result["group"])
        rows = {r["key"]: r for r in result["rows"]}
        self.assertIn("repolens", rows)
        self.assertIn("worker", rows)

    def test_report_invalid_group_raises(self) -> None:
        """Report with invalid group raises KitError with code invalid_group."""
        from aisdlc.errors import KitError
        from aisdlc.telemetry_store import report

        ctx = self._ctx()
        with self.assertRaises(KitError) as cm:
            report(ctx, "invalid_group")
        self.assertEqual("invalid_group", cm.exception.code)

    def test_absent_store_empty_report(self) -> None:
        """Report with no store file returns empty results and does not create the file."""
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.telemetry_store import report

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
        from aisdlc.context import Context
        from aisdlc.profile import find_profile, load_profile
        from aisdlc.telemetry_store import stats

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
        from aisdlc.store import open_store
        from aisdlc.telemetry_store import record, stats

        ctx = self._ctx()
        open_store(self.repo.root)

        record(ctx, stage="s1", agent="a1", metric="duration_s", value=1.0, ok=True)
        record(ctx, stage="s2", agent="a2", metric="duration_s", value=2.0, ok=False)

        result = stats(ctx)
        self.assertGreaterEqual(result["telemetry"], 2)


class DualWriteFailOpenTest(unittest.TestCase):
    """Dual-write via evidence._append mirror is fail-open."""

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

    def _ctx(self):
        from aisdlc.cli import load_context
        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_mirror_fail_open_runs_start(self) -> None:
        """Patch mirror_line to raise; runs.start still succeeds."""
        from unittest import mock

        from aisdlc import runs

        with mock.patch(
            "aisdlc.telemetry_store.mirror_line",
            side_effect=RuntimeError("store locked"),
        ):
            result = runs.start(
                self._ctx(),
                project_id="alpha",
                workflow_id="feature-delivery",
            )
            self.assertIn("id", result)

    def test_mirror_fail_open_harness(self) -> None:
        """Patch mirror_line to raise; harness.report_event still succeeds."""
        from unittest import mock

        from aisdlc import harness, runs

        ctx = self._ctx()
        run = runs.start(ctx, project_id="alpha", workflow_id="feature-delivery")
        run_id = run["id"]

        with mock.patch(
            "aisdlc.telemetry_store.mirror_line",
            side_effect=RuntimeError("store locked"),
        ):
            result = harness.report_event(
                ctx,
                run_id=run_id,
                event="run.started",
                adapter="local-worker",
            )
            self.assertIn("id", result)

    def test_mirror_line_and_import_line_exist(self) -> None:
        """mirror_line and import_line are callables on aisdlc.telemetry_store."""
        from aisdlc.telemetry_store import import_line, mirror_line
        self.assertTrue(callable(mirror_line))
        self.assertTrue(callable(import_line))

    def test_mirror_creates_store_rows_without_sync(self) -> None:
        """After runs.start and harness events, store events table has rows
        WITHOUT calling sync() first — dual-write via mirror."""
        from aisdlc import harness, runs
        from aisdlc.store import store_path

        ctx = self._ctx()
        run = runs.start(ctx, project_id="alpha", workflow_id="feature-delivery")
        run_id = run["id"]

        harness.report_event(ctx, run_id=run_id, event="run.started", adapter="local-worker")
        harness.report_event(ctx, run_id=run_id, event="step.started", adapter="local-worker",
                             step="s1")
        harness.report_event(ctx, run_id=run_id, event="step.finished", adapter="local-worker",
                             step="s1", step_status="pass")
        harness.report_event(ctx, run_id=run_id, event="run.finished", adapter="local-worker",
                             ok=True)

        # Check events table WITHOUT calling sync() first.
        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        rows = conn.execute("SELECT kind, run_id, actor FROM events").fetchall()
        conn.close()

        kinds = [r[0] for r in rows]
        run_ids = [r[1] for r in rows]
        actors = [r[2] for r in rows]

        self.assertIn("run.request", kinds)
        self.assertIn("run.started", kinds)
        self.assertIn("step.started", kinds)
        self.assertIn("step.finished", kinds)
        self.assertIn("run.finished", kinds)
        self.assertTrue(all(rid == run_id for rid in run_ids if rid))
        self.assertTrue(any(a for a in actors))

    def test_sync_idempotent_with_mirror(self) -> None:
        """sync() inserts 0 for ledger lines already mirrored by dual-write."""
        from aisdlc import harness, runs
        from aisdlc.telemetry_store import sync

        ctx = self._ctx()
        run = runs.start(ctx, project_id="alpha", workflow_id="feature-delivery")
        run_id = run["id"]

        harness.report_event(ctx, run_id=run_id, event="run.started", adapter="local-worker")
        harness.report_event(ctx, run_id=run_id, event="run.finished", adapter="local-worker",
                             ok=True)

        # sync should insert 0 for lines already mirrored
        result = sync(ctx)
        self.assertEqual(0, result["ledger"],
                         "sync should insert 0 for already-mirrored ledger lines")

    def test_fail_event_in_store(self) -> None:
        """A harness event with step_status=fail is stored with step_status=fail in data."""
        from aisdlc import harness, runs
        from aisdlc.store import store_path

        ctx = self._ctx()
        run = runs.start(ctx, project_id="alpha", workflow_id="feature-delivery")
        run_id = run["id"]

        harness.report_event(ctx, run_id=run_id, event="run.started", adapter="local-worker")
        harness.report_event(ctx, run_id=run_id, event="step.finished", adapter="local-worker",
                             step="s1", step_status="fail", ok=False)
        harness.report_event(ctx, run_id=run_id, event="run.finished", adapter="local-worker",
                             ok=False)

        # Check without sync — mirror should have written the rows
        db_path = store_path(self.repo.root)
        conn = sqlite3.connect(str(db_path))
        rows = conn.execute(
            "SELECT kind, data FROM events WHERE kind='step.finished'"
        ).fetchall()
        conn.close()

        self.assertTrue(len(rows) >= 1)
        found_fail = False
        for row in rows:
            data = json.loads(row[1]) if row[1] else {}
            if not data.get("ok", True):
                found_fail = True
                break
        self.assertTrue(found_fail, "expected at least one step.finished with ok=false")


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
        port, _token = self._serve()
        status, ctype, body = self._get(
            port, "/factory/telemetry?group=stage", token=None,
        )
        self.assertEqual(401, status)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("unauthorized", json.loads(body)["code"])

    def test_telemetry_endpoint_invalid_group_400(self) -> None:
        """GET /factory/telemetry?group=invalid returns 400."""
        port, token = self._serve()
        status, ctype, _body = self._get(
            port, "/factory/telemetry?group=invalid", token=token,
        )
        self.assertEqual(400, status)
        self.assertEqual("application/problem+json", ctype)


if __name__ == "__main__":
    unittest.main()
