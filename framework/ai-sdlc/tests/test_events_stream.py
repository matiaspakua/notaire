"""Failing tests for SSE event stream (aisdlc.events_stream module) — #2397.

Tests pure functions with injected sleep/clock/should_stop/write — no real waiting.
These tests must fail with ImportError until aisdlc.events_stream exists.
"""
from __future__ import annotations

import json
import sys
import unittest
from pathlib import Path

from helpers import KIT, Repo

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"

[[gates]]
stage = "pre-push"
name = "lint"
run = "true"

[[harness.adapters]]
id = "local-worker"
runtime = "local"
source = ".aisdlc/harness/local-worker.yml"
"""


class FormatEventTest(unittest.TestCase):
    """format_event produces correct SSE framing bytes."""

    def test_format_event_produces_exact_bytes(self) -> None:
        from aisdlc.events_stream import format_event

        row = {"kind": "step.started", "run_id": "R1", "step": "implement"}
        frame = format_event(3, row)
        expected = b"id: 3\nevent: step.started\ndata: "
        self.assertTrue(frame.startswith(expected))
        self.assertTrue(frame.endswith(b"\n\n"))
        # The data portion should be compact JSON
        data_part = frame.split(b"data: ")[1].rstrip(b"\n")
        data = json.loads(data_part)
        self.assertEqual("R1", data["run_id"])
        self.assertEqual("implement", data["step"])

    def test_format_event_sanitizes_kind(self) -> None:
        """A kind containing newline cannot inject a line."""
        from aisdlc.events_stream import format_event

        row = {"kind": "bad\nkind", "data": "value"}
        frame = format_event(1, row)
        # The kind must be sanitized — no raw newline in the event: line
        lines = frame.split(b"\n")
        event_line = None
        for line in lines:
            if line.startswith(b"event:"):
                event_line = line
                break
        self.assertIsNotNone(event_line)
        # Sanitized kind should not contain newline
        self.assertIn(b"event: bad_kind", frame)

    def test_format_event_stage_run_kind(self) -> None:
        """Rows with gates+stage and no kind get kind 'stage-run'."""
        from aisdlc.events_stream import format_event

        row = {
            "stage": "pre-push",
            "ok": True,
            "gates": [{"name": "lint", "status": "pass"}],
        }
        frame = format_event(2, row)
        self.assertIn(b"event: stage-run", frame)

    def test_format_event_ledger_default_kind(self) -> None:
        """Rows without kind and without gates get kind 'ledger'."""
        from aisdlc.events_stream import format_event

        row = {"some": "data"}
        frame = format_event(1, row)
        self.assertIn(b"event: ledger", frame)

    def test_format_event_redacts_secrets(self) -> None:
        """Secret values in ledger rows are redacted in SSE frames."""
        from aisdlc.events_stream import format_event

        row = {
            "kind": "step.started",
            "token": "Bearer abc123SECRET",
        }
        frame = format_event(1, row)
        # The secret value should be redacted
        self.assertNotIn(b"Bearer abc123SECRET", frame)
        self.assertIn(b"***", frame)


class LedgerRowsTest(unittest.TestCase):
    """ledger_rows reads the ledger file with 1-based line numbers."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _ledger_path(self) -> Path:
        return self.repo.root / ".aisdlc" / "evidence" / "ledger.jsonl"

    def test_ledger_rows_returns_all_rows(self) -> None:
        from aisdlc.events_stream import ledger_rows

        ctx = self._ctx()
        ledger = self._ledger_path()
        # Write three JSON lines directly
        lines = [
            json.dumps({"kind": "run.request", "run_id": "R1"}),
            json.dumps({"kind": "step.started", "run_id": "R1", "step": "s1"}),
            json.dumps({"kind": "step.finished", "run_id": "R1", "step": "s1"}),
        ]
        ledger.write_text("\n".join(lines) + "\n", encoding="utf-8")

        rows = ledger_rows(ctx)
        self.assertEqual(3, len(rows))
        self.assertEqual(1, rows[0][0])  # 1-based line number
        self.assertEqual(2, rows[1][0])
        self.assertEqual(3, rows[2][0])
        self.assertEqual("R1", rows[0][1]["run_id"])

    def test_ledger_rows_after_filter(self) -> None:
        """ledger_rows(ctx, after=2) returns only rows from line 3 onward."""
        from aisdlc.events_stream import ledger_rows

        ctx = self._ctx()
        ledger = self._ledger_path()
        lines = [
            json.dumps({"kind": "run.request", "run_id": "R1"}),
            json.dumps({"kind": "step.started", "run_id": "R1", "step": "s1"}),
            json.dumps({"kind": "step.finished", "run_id": "R1", "step": "s1"}),
        ]
        ledger.write_text("\n".join(lines) + "\n", encoding="utf-8")

        rows = ledger_rows(ctx, after=2)
        self.assertEqual(1, len(rows))
        self.assertEqual(3, rows[0][0])

    def test_ledger_rows_skips_partial_trailing_line(self) -> None:
        """A partial last line (no trailing newline) is skipped."""
        from aisdlc.events_stream import ledger_rows

        ctx = self._ctx()
        ledger = self._ledger_path()
        # Two complete lines + one partial line
        content = (
            json.dumps({"kind": "run.request", "run_id": "R1"}) + "\n"
            + json.dumps({"kind": "step.started", "run_id": "R1"}) + "\n"
            + '{"kind": "partial"'  # incomplete JSON, no newline
        )
        ledger.write_text(content, encoding="utf-8")

        rows = ledger_rows(ctx)
        self.assertEqual(2, len(rows))
        self.assertEqual(1, rows[0][0])
        self.assertEqual(2, rows[1][0])

    def test_ledger_rows_picks_up_completed_trailing_line(self) -> None:
        """A previously partial line, once completed, is picked up."""
        from aisdlc.events_stream import ledger_rows

        ctx = self._ctx()
        ledger = self._ledger_path()
        # One complete line + one partial line
        content = (
            json.dumps({"kind": "run.request", "run_id": "R1"}) + "\n"
            + '{"kind": "step.started"'  # incomplete
        )
        ledger.write_text(content, encoding="utf-8")

        rows1 = ledger_rows(ctx)
        self.assertEqual(1, len(rows1))

        # Now complete the partial line
        ledger.write_text(
            json.dumps({"kind": "run.request", "run_id": "R1"}) + "\n"
            + json.dumps({"kind": "step.started", "run_id": "R1"}) + "\n",
            encoding="utf-8",
        )

        rows2 = ledger_rows(ctx, after=1)
        self.assertEqual(1, len(rows2))
        self.assertEqual(2, rows2[0][0])

    def test_ledger_rows_skips_blank_lines(self) -> None:
        """Blank lines are skipped but still count as line numbers."""
        from aisdlc.events_stream import ledger_rows

        ctx = self._ctx()
        ledger = self._ledger_path()
        content = (
            json.dumps({"kind": "run.request", "run_id": "R1"}) + "\n"
            + "\n"  # blank line (line 2)
            + json.dumps({"kind": "step.started", "run_id": "R1"}) + "\n"
        )
        ledger.write_text(content, encoding="utf-8")

        rows = ledger_rows(ctx)
        self.assertEqual(2, len(rows))
        self.assertEqual(1, rows[0][0])
        self.assertEqual(3, rows[1][0])


class ParseLastEventIdTest(unittest.TestCase):
    """parse_last_event_id handles precedence and validation."""

    def test_parse_last_event_id_from_header(self) -> None:
        from aisdlc.events_stream import parse_last_event_id

        result = parse_last_event_id("5", None)
        self.assertEqual(5, result)

    def test_parse_last_event_id_header_wins_over_query(self) -> None:
        from aisdlc.events_stream import parse_last_event_id

        result = parse_last_event_id("5", "10")
        self.assertEqual(5, result)

    def test_parse_last_event_id_from_query(self) -> None:
        from aisdlc.events_stream import parse_last_event_id

        result = parse_last_event_id(None, "7")
        self.assertEqual(7, result)

    def test_parse_last_event_id_defaults_to_zero(self) -> None:
        from aisdlc.events_stream import parse_last_event_id

        result = parse_last_event_id(None, None)
        self.assertEqual(0, result)

    def test_parse_last_event_id_invalid_raises(self) -> None:
        from aisdlc.events_stream import parse_last_event_id

        from aisdlc.errors import KitError

        with self.assertRaises(KitError) as cm:
            parse_last_event_id("abc", None)
        self.assertEqual("invalid_body", cm.exception.code)

    def test_parse_last_event_id_negative_raises(self) -> None:
        from aisdlc.events_stream import parse_last_event_id

        from aisdlc.errors import KitError

        with self.assertRaises(KitError) as cm:
            parse_last_event_id("-1", None)
        self.assertEqual("invalid_body", cm.exception.code)


class StreamTest(unittest.TestCase):
    """stream() replays, polls, keep-alive, and handles broken pipes."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _ledger_path(self) -> Path:
        return self.repo.root / ".aisdlc" / "evidence" / "ledger.jsonl"

    def test_stream_replays_existing_rows(self) -> None:
        from aisdlc.events_stream import stream

        ctx = self._ctx()
        ledger = self._ledger_path()
        lines = [
            json.dumps({"kind": "run.request", "run_id": "R1"}),
            json.dumps({"kind": "step.started", "run_id": "R1", "step": "s1"}),
        ]
        ledger.write_text("\n".join(lines) + "\n", encoding="utf-8")

        written: list[bytes] = []

        def write(data: bytes) -> None:
            written.append(data)

        def should_stop() -> bool:
            return True  # stop immediately after replay

        stream(ctx, write, after=0, should_stop=should_stop)
        self.assertTrue(len(written) >= 2)
        # Check that id: 1 and id: 2 are present
        all_data = b"".join(written)
        self.assertIn(b"id: 1", all_data)
        self.assertIn(b"id: 2", all_data)

    def test_stream_emits_new_row_after_fake_sleep(self) -> None:
        """A row appended during a fake sleep is emitted with the next id."""
        from aisdlc.events_stream import stream

        ctx = self._ctx()
        ledger = self._ledger_path()
        # Start with one row
        ledger.write_text(
            json.dumps({"kind": "run.request", "run_id": "R1"}) + "\n",
            encoding="utf-8",
        )

        written: list[bytes] = []

        def write(data: bytes) -> None:
            written.append(data)

        call_count = 0

        def fake_sleep(_seconds: float) -> None:
            nonlocal call_count
            call_count += 1
            if call_count == 1:
                # Append a new row during the first "sleep"
                with open(ledger, "a", encoding="utf-8") as f:
                    f.write(
                        json.dumps({"kind": "step.started", "run_id": "R1"}) + "\n"
                    )

        def should_stop() -> bool:
            return call_count >= 2  # stop after 2 polls

        stream(
            ctx, write, after=0,
            poll_seconds=1.0, keepalive_seconds=15.0,
            should_stop=should_stop, sleep=fake_sleep, clock=lambda: 0,
        )
        all_data = b"".join(written)
        self.assertIn(b"id: 1", all_data)
        self.assertIn(b"id: 2", all_data)

    def test_stream_keepalive_when_no_rows(self) -> None:
        """Keep-alive b': keep-alive\n\n' is written when clock advances 15s."""
        from aisdlc.events_stream import stream

        ctx = self._ctx()
        ledger = self._ledger_path()
        # Empty ledger
        ledger.write_text("", encoding="utf-8")

        written: list[bytes] = []

        def write(data: bytes) -> None:
            written.append(data)

        clock_time = 0.0

        def fake_sleep(_seconds: float) -> None:
            nonlocal clock_time
            clock_time += 1.0

        def fake_clock() -> float:
            return clock_time

        call_count = 0

        def should_stop() -> bool:
            nonlocal call_count
            call_count += 1
            return call_count >= 20  # enough polls to trigger keepalive

        stream(
            ctx, write, after=0,
            poll_seconds=1.0, keepalive_seconds=15.0,
            should_stop=should_stop, sleep=fake_sleep, clock=fake_clock,
        )
        all_data = b"".join(written)
        self.assertIn(b": keep-alive\n\n", all_data)

    def test_stream_broken_pipe_ends_cleanly(self) -> None:
        """BrokenPipeError from write ends stream without raising."""
        from aisdlc.events_stream import stream

        ctx = self._ctx()
        ledger = self._ledger_path()
        ledger.write_text(
            json.dumps({"kind": "run.request", "run_id": "R1"}) + "\n",
            encoding="utf-8",
        )

        call_count = 0

        def write(data: bytes) -> None:
            nonlocal call_count
            call_count += 1
            if call_count == 2:
                raise BrokenPipeError()

        def should_stop() -> bool:
            return False  # would loop forever without BrokenPipeError

        # Should not raise
        stream(
            ctx, write, after=0,
            poll_seconds=1.0, keepalive_seconds=15.0,
            should_stop=should_stop,
            sleep=lambda s: None,
            clock=lambda: 0,
        )
        # Stream should have stopped after BrokenPipeError
        self.assertLessEqual(call_count, 2)


if __name__ == "__main__":
    unittest.main()
