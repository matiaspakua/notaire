"""Failing HTTP serve tests for the intent entry point (#2395).

Copies the harness pattern of test_serve_runs.py and test_serve_tokens.py.
Expected to fail with 404 since serve.py has no /factory/intents routes yet.
"""
from __future__ import annotations

import json
import sys
import textwrap
import threading
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
"""

SPEC_FIXTURE = textwrap.dedent("""\
    # Add rate limiting to factory API

    The factory API currently has no rate limiting.

    ## Acceptance criteria

    - [ ] New endpoint returns 429 after 60 requests in a minute
    - [ ] Rate limit header is included in every response

    ## References

    - [ADR-030 hexagonal](docs/200-architecture/202-ADR/ADR-030.md) (architecture)
""")


class IntentServeTest(unittest.TestCase):
    """HTTP endpoints: POST/GET /factory/intents, POST /factory/runs with intent."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        # Set up catalogs and workflow for POST /factory/runs tests
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
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

    def _serve(self, *, enable_write: bool = True, scope: str = "owner"):
        from aisdlc import serve as serve_mod
        from aisdlc.serve_token import add_token, ensure_token

        ensure_token(self.repo.root, scope=scope)
        self.write_token = add_token(self.repo.root, identity="writer", scope="write")
        self.read_token = add_token(self.repo.root, identity="reader", scope="read")
        server = serve_mod.start_server(
            self._ctx(), host="127.0.0.1", port=0, allow_remote=False, enable_write=enable_write,
        )
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        return server.server_address[1]

    def _request(
        self,
        port: int,
        path: str,
        *,
        method: str,
        token: str | None = None,
        body: dict | None = None,
        csrf: str | None = None,
    ):
        headers = {}
        if token is not None:
            headers["Authorization"] = f"Bearer {token}"
        if csrf is not None:
            headers["X-AISDLC-CSRF"] = csrf
        data = None
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            headers.setdefault("Content-Type", "application/json")
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            data=data,
            method=method,
            headers=headers,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:
            return exc.code, json.loads(exc.read().decode("utf-8"))

    # --- Scenario: POST /factory/intents creates intent with write scope, CSRF, and actor ---
    def test_post_intent(self):
        port = self._serve(enable_write=True)

        # Write token + CSRF → 201
        status, intent = self._request(
            port,
            "/factory/intents",
            method="POST",
            token=self.write_token.value,
            csrf=self.write_token.value,
            body={"text": "Refactor auth"},
        )
        self.assertEqual(201, status, json.dumps(intent, default=str))
        self.assertEqual("prompt", intent["kind"])
        self.assertTrue(intent["id"].startswith("int-"))
        self.assertEqual("ui:writer", intent["created_by"])

        # Read-only token → 403
        status, problem = self._request(
            port,
            "/factory/intents",
            method="POST",
            token=self.read_token.value,
            csrf=self.read_token.value,
            body={"text": "Should fail"},
        )
        self.assertEqual(403, status)
        self.assertEqual("forbidden", problem["code"])

        # Missing CSRF → refused
        status, problem = self._request(
            port,
            "/factory/intents",
            method="POST",
            token=self.write_token.value,
            body={"text": "No CSRF"},
        )
        self.assertEqual(403, status)
        self.assertEqual("csrf_required", problem["code"])

    # --- Scenario: POST /factory/intents with spec ---
    def test_post_intent_with_spec(self):
        port = self._serve(enable_write=True)
        status, intent = self._request(
            port,
            "/factory/intents",
            method="POST",
            token=self.write_token.value,
            csrf=self.write_token.value,
            body={"spec": SPEC_FIXTURE},
        )
        self.assertEqual(201, status, json.dumps(intent, default=str))
        self.assertEqual("spec", intent["kind"])
        self.assertEqual("Add rate limiting to factory API", intent["title"])

    # --- Scenario: GET /factory/intents returns intent list ---
    def test_get_intents(self):
        port = self._serve(enable_write=True)
        # Create one intent first
        self._request(
            port, "/factory/intents", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"text": "List me"},
        )
        status, data = self._request(
            port, "/factory/intents", method="GET", token=self.read_token.value,
        )
        self.assertEqual(200, status)
        entries = data.get("entries", data) if isinstance(data, dict) else data
        self.assertGreaterEqual(len(entries), 1)

    # --- Scenario: GET /factory/intents/{id} returns intent details ---
    def test_get_intent_by_id(self):
        port = self._serve(enable_write=True)
        status, created = self._request(
            port, "/factory/intents", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"text": "Get me by id"},
        )
        self.assertEqual(201, status)
        intent_id = created["id"]

        status, fetched = self._request(
            port, f"/factory/intents/{intent_id}", method="GET", token=self.read_token.value,
        )
        self.assertEqual(200, status)
        self.assertEqual(intent_id, fetched["id"])
        self.assertEqual("Get me by id", fetched["title"])

    # --- Scenario: POST /factory/runs accepts valid intent id and appends to run_ids ---
    def test_post_runs_with_intent(self):
        port = self._serve(enable_write=True)
        # Create a project first
        self._request(
            port, "/factory/projects", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"id": "alpha", "profile_ref": "aisdlc.toml", "repos": []},
        )
        # Create an intent
        status, intent = self._request(
            port, "/factory/intents", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"text": "Run intent"},
        )
        self.assertEqual(201, status)
        intent_id = intent["id"]

        # Start a run with the intent
        status, run = self._request(
            port, "/factory/runs", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"project": "alpha", "workflow": "feature-delivery", "intent": intent_id},
        )
        self.assertEqual(200, status, json.dumps(run, default=str))
        self.assertEqual(intent_id, run.get("intent"))

        # Intent's run_ids should contain the run id
        status, updated_intent = self._request(
            port, f"/factory/intents/{intent_id}", method="GET", token=self.read_token.value,
        )
        self.assertEqual(200, status)
        self.assertIn(run["id"], updated_intent["run_ids"])

    # --- Scenario: POST /factory/runs rejects nonexistent intent id ---
    def test_post_runs_unknown_intent(self):
        port = self._serve(enable_write=True)
        # Create a project first
        self._request(
            port, "/factory/projects", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"id": "alpha", "profile_ref": "aisdlc.toml", "repos": []},
        )
        status, problem = self._request(
            port, "/factory/runs", method="POST",
            token=self.write_token.value, csrf=self.write_token.value,
            body={"project": "alpha", "workflow": "feature-delivery", "intent": "int-nonexistent"},
        )
        self.assertEqual(404, status)
        self.assertEqual("not_found", problem["code"])


if __name__ == "__main__":
    unittest.main()
