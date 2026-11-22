"""Failing HTTP and CLI tests for /factory/status and /factory/events — #2397.

Tests the serve endpoints and CLI command.  These tests must fail with
404 / ImportError until the routes and aisdlc.status / aisdlc.events_stream exist.
"""
from __future__ import annotations

import http.client
import json
import sys
import threading
import time
import unittest
import urllib.error
import urllib.request

from helpers import KIT, Repo, output

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


class ServeStatusEventsTest(unittest.TestCase):
    """HTTP tests for GET /factory/status and GET /factory/events."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _serve(self, *, enable_write: bool = False, scope: str = "read"):
        from aisdlc import serve as serve_mod
        from aisdlc.serve_token import ensure_token

        ctx = self._ctx()
        path = self.repo.root / ".aisdlc" / "serve.token"
        if path.is_file():
            path.unlink()
        ensure_token(self.repo.root, scope=scope)
        server = serve_mod.start_server(
            ctx, host="127.0.0.1", port=0, allow_remote=False,
            enable_write=enable_write,
        )
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        token = serve_mod.load_token_value(self.repo.root)
        return server.server_address[1], token

    def _request(
        self,
        port: int,
        path: str,
        *,
        method: str,
        token: str | None,
        headers: dict | None = None,
    ):
        hdrs = dict(headers or {})
        if token is not None:
            hdrs["Authorization"] = f"Bearer {token}"
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            method=method,
            headers=hdrs,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, dict(resp.headers), resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, dict(exc.headers), exc.read().decode("utf-8")

    # ------------------------------------------------------------------
    # GET /factory/status
    # ------------------------------------------------------------------

    def test_status_401_without_token(self) -> None:
        """GET /factory/status returns 401 without authentication."""
        port, _ = self._serve()
        status, _, _ = self._request(
            port, "/factory/status", method="GET", token=None,
        )
        self.assertEqual(401, status)

    def test_status_200_with_token_empty_shape(self) -> None:
        """GET /factory/status returns 200 JSON with empty shape when no runs."""
        port, token = self._serve()
        status, _, body = self._request(
            port, "/factory/status", method="GET", token=token,
        )
        self.assertEqual(200, status)
        data = json.loads(body)
        self.assertIsNone(data["intent"])
        self.assertIsNone(data["run"])
        self.assertEqual([], data["previous"])
        self.assertEqual([], data["next"])

    def test_status_200_with_run(self) -> None:
        """GET /factory/status returns the current run after a run is started."""
        self.repo.run(
            "project", "create", "--id", "alpha",
            "--profile-ref", "aisdlc.toml",
            "--repo-id", "notaire",
            "--forge", "github",
            "--url", "https://github.com/example/notaire",
            "--default-branch", "main",
            "--json",
        )
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java",
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

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        port, token = self._serve()
        status, _, body = self._request(
            port, "/factory/status", method="GET", token=token,
        )
        self.assertEqual(200, status)
        data = json.loads(body)
        self.assertEqual(run_id, data["run"])

    # ------------------------------------------------------------------
    # GET /factory/events
    # ------------------------------------------------------------------

    def test_events_401_without_token(self) -> None:
        """GET /factory/events returns 401 without authentication."""
        port, _ = self._serve()
        status, _, _ = self._request(
            port, "/factory/events", method="GET", token=None,
        )
        self.assertEqual(401, status)

    def test_events_200_with_token(self) -> None:
        """GET /factory/events returns 200 with SSE headers."""
        port, token = self._serve()
        conn = http.client.HTTPConnection("127.0.0.1", port, timeout=3)
        conn.request(
            "GET", "/factory/events",
            headers={"Authorization": f"Bearer {token}"},
        )
        resp = conn.getresponse()
        self.assertEqual(200, resp.status)
        ct = resp.getheader("Content-Type", "")
        self.assertTrue(
            ct.startswith("text/event-stream"),
            f"Content-Type should start with text/event-stream, got: {ct}",
        )
        self.assertIsNone(resp.getheader("Content-Length"))
        conn.close()

    def test_events_replay_with_last_event_id(self) -> None:
        """Last-Event-ID: 2 returns only rows from line 3 onward."""
        from aisdlc import evidence

        ctx = self._ctx()
        # Write three ledger rows
        evidence.append_record(ctx, {
            "kind": "run.request", "run_id": "R1",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.started", "run_id": "R1", "step": "s1",
            "ts": "2026-11-22T10:00:01+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished", "run_id": "R1", "step": "s1",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:02+00:00",
        })

        port, token = self._serve()
        conn = http.client.HTTPConnection("127.0.0.1", port, timeout=3)
        conn.request(
            "GET", "/factory/events",
            headers={
                "Authorization": f"Bearer {token}",
                "Last-Event-ID": "2",
            },
        )
        resp = conn.getresponse()
        self.assertEqual(200, resp.status)
        # Read a chunk — should only contain id: 3
        chunk = resp.read(4096).decode("utf-8", errors="replace")
        conn.close()
        self.assertIn("id: 3", chunk)
        self.assertNotIn("id: 1", chunk)
        self.assertNotIn("id: 2", chunk)

    def test_events_replay_with_query_after(self) -> None:
        """?after=2 returns only rows from line 3 onward."""
        from aisdlc import evidence

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.request", "run_id": "R1",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.started", "run_id": "R1", "step": "s1",
            "ts": "2026-11-22T10:00:01+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished", "run_id": "R1", "step": "s1",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:02+00:00",
        })

        port, token = self._serve()
        conn = http.client.HTTPConnection("127.0.0.1", port, timeout=3)
        conn.request(
            "GET", "/factory/events?after=2",
            headers={"Authorization": f"Bearer {token}"},
        )
        resp = conn.getresponse()
        self.assertEqual(200, resp.status)
        chunk = resp.read(4096).decode("utf-8", errors="replace")
        conn.close()
        self.assertIn("id: 3", chunk)
        self.assertNotIn("id: 1", chunk)

    def test_events_invalid_last_event_id_returns_400(self) -> None:
        """Invalid Last-Event-ID ('abc') returns 400 application/problem+json."""
        port, token = self._serve()
        conn = http.client.HTTPConnection("127.0.0.1", port, timeout=5)
        conn.request(
            "GET", "/factory/events",
            headers={
                "Authorization": f"Bearer {token}",
                "Last-Event-ID": "abc",
            },
        )
        resp = conn.getresponse()
        self.assertEqual(400, resp.status)
        ct = resp.getheader("Content-Type", "")
        self.assertIn("problem+json", ct)
        conn.close()

    def test_events_live_append_arrives(self) -> None:
        """A ledger row appended while SSE is open arrives on the connection."""
        from aisdlc import evidence

        ctx = self._ctx()
        # Write one initial row
        evidence.append_record(ctx, {
            "kind": "run.request", "run_id": "R1",
            "ts": "2026-11-22T10:00:00+00:00",
        })

        port, token = self._serve()
        conn = http.client.HTTPConnection("127.0.0.1", port, timeout=5)
        conn.request(
            "GET", "/factory/events",
            headers={"Authorization": f"Bearer {token}"},
        )
        resp = conn.getresponse()
        self.assertEqual(200, resp.status)

        # Read the first frame (id: 1)
        first_chunk = resp.read(4096).decode("utf-8", errors="replace")
        self.assertIn("id: 1", first_chunk)

        # Append a new row while connection is open
        evidence.append_record(ctx, {
            "kind": "step.started", "run_id": "R1", "step": "s1",
            "ts": "2026-11-22T10:00:01+00:00",
        })

        # Wait for the server to poll and emit the new row
        all_data = first_chunk
        deadline = time.monotonic() + 5
        while time.monotonic() < deadline:
            try:
                more = resp.read(4096).decode("utf-8", errors="replace")
                if more:
                    all_data += more
                    if "id: 2" in all_data:
                        break
                time.sleep(0.2)
            except (OSError, TimeoutError):
                break
        conn.close()
        self.assertIn("id: 2", all_data)

    def test_sse_and_status_not_blocked(self) -> None:
        """While one SSE connection is open, GET /factory/status still answers."""
        from aisdlc import evidence

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.request", "run_id": "R1",
            "ts": "2026-11-22T10:00:00+00:00",
        })

        port, token = self._serve()

        # Open an SSE connection (keep it alive)
        sse_conn = http.client.HTTPConnection("127.0.0.1", port, timeout=5)
        sse_conn.request(
            "GET", "/factory/events",
            headers={"Authorization": f"Bearer {token}"},
        )
        sse_resp = sse_conn.getresponse()
        self.assertEqual(200, sse_resp.status)
        # Read a bit to establish the connection
        sse_resp.read(256)

        # Now make a status request on a separate connection
        status_conn = http.client.HTTPConnection("127.0.0.1", port, timeout=5)
        status_conn.request(
            "GET", "/factory/status",
            headers={"Authorization": f"Bearer {token}"},
        )
        status_resp = status_conn.getresponse()
        self.assertEqual(200, status_resp.status)
        data = json.loads(status_resp.read().decode("utf-8"))
        self.assertIn("run", data)
        status_conn.close()

        # Clean up SSE connection
        sse_conn.close()

    def test_events_close_and_reconnect(self) -> None:
        """Closing the client does not prevent a later request."""
        port, token = self._serve()

        # First connection
        conn1 = http.client.HTTPConnection("127.0.0.1", port, timeout=3)
        conn1.request(
            "GET", "/factory/events",
            headers={"Authorization": f"Bearer {token}"},
        )
        resp1 = conn1.getresponse()
        self.assertEqual(200, resp1.status)
        conn1.close()

        # Second connection should work
        conn2 = http.client.HTTPConnection("127.0.0.1", port, timeout=3)
        conn2.request(
            "GET", "/factory/events",
            headers={"Authorization": f"Bearer {token}"},
        )
        resp2 = conn2.getresponse()
        self.assertEqual(200, resp2.status)
        conn2.close()


class CliStatusTest(unittest.TestCase):
    """CLI black-box test: aisdlc status --json."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def test_status_json_no_run(self) -> None:
        """aisdlc status --json returns empty shape with exit 0."""
        result = self.repo.run("status", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertIsNone(data["run"])
        self.assertEqual([], data["previous"])
        self.assertEqual([], data["next"])


if __name__ == "__main__":
    unittest.main()
