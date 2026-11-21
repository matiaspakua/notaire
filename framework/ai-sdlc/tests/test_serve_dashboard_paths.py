"""Contract paths a dashboard needs (#2384): who am I, create a catalog entry, edit its fields; no literal secrets."""
from __future__ import annotations

import json
import sys
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


class DashboardPathsTest(unittest.TestCase):
    def setUp(self) -> None:
        from aisdlc import serve as serve_mod
        from aisdlc.cli import load_context
        from aisdlc.serve_token import add_token, ensure_token

        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        added = self.repo.run("catalog", "add", "--kind", "providers", "--id", "anthropic", "--ref", "providers/a",
                              "--summary", "v1")
        self.assertEqual(0, added.returncode, output(added))
        ensure_token(self.repo.root, scope="owner")
        self.alice = add_token(self.repo.root, identity="alice", scope="write")
        self.bob = add_token(self.repo.root, identity="bob", scope="read")
        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = serve_mod.start_server(ctx, host="127.0.0.1", port=0, enable_write=True)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        self.port = server.server_address[1]

    def _call(self, path, token, *, method="GET", body=None, if_match=None):
        headers = {"Authorization": f"Bearer {token.value}"}
        data = None
        if method != "GET":
            headers["X-AISDLC-CSRF"] = token.value
        if if_match is not None:
            headers["If-Match"] = if_match
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            headers["Content-Type"] = "application/json"
        req = urllib.request.Request(f"http://127.0.0.1:{self.port}{path}", data=data, method=method, headers=headers)
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:
            return exc.code, json.loads(exc.read().decode("utf-8"))

    def test_the_session_path_names_the_caller_and_scope(self):
        status, me = self._call("/factory/session", self.alice)
        self.assertEqual(200, status, me)
        self.assertEqual({"identity": "alice", "scope": "write", "actor": "ui:alice", "write_enabled": True}, me)
        self.assertEqual("read", self._call("/factory/session", self.bob)[1]["scope"])

    def test_a_writer_creates_a_provider_with_an_env_credential(self):
        body = {"id": "openai", "ref": "providers/openai", "summary": "OpenAI",
                "fields": {"model": "gpt-5", "api_key": "env:OPENAI_API_KEY", "endpoint": "https://api.openai.com"}}
        status, created = self._call("/factory/catalogs/providers", self.alice, method="POST", body=body)
        self.assertEqual(200, status, created)
        self.assertEqual(1, created["version"])
        self.assertEqual("ui:alice", created["created_by"])
        shown = self._call("/factory/catalogs/providers/openai", self.bob)[1]
        self.assertEqual("env:OPENAI_API_KEY", shown["api_key"])
        self.assertEqual("gpt-5", shown["model"])

    def test_a_literal_secret_is_refused(self):
        for fields in ({"api_key": "plain-value"}, {"note": "Bearer abc.def"}, {"api_key": "env:lower case"}):
            body = {"id": "leaky", "summary": "x", "fields": fields}
            status, problem = self._call("/factory/catalogs/providers", self.alice, method="POST", body=body)
            self.assertEqual(400, status, fields)
            self.assertEqual("literal_secret", problem["code"])
            self.assertNotIn("plain-value", json.dumps(problem))
        self.assertEqual(404, self._call("/factory/catalogs/providers/leaky", self.bob)[0])

    def test_a_reader_cannot_create(self):
        status, problem = self._call("/factory/catalogs/providers", self.bob, method="POST",
                                     body={"id": "x", "summary": "x"})
        self.assertEqual(403, status)
        self.assertEqual("forbidden", problem["code"])

    def test_put_updates_fields_with_if_match_and_keeps_the_rule(self):
        status, updated = self._call("/factory/catalogs/providers/anthropic", self.alice, method="PUT",
                                     body={"fields": {"model": "claude-opus-5-5", "api_key": "env:ANTHROPIC_API_KEY"}},
                                     if_match="1")
        self.assertEqual(200, status, updated)
        self.assertEqual(2, updated["version"])
        self.assertEqual("claude-opus-5-5", updated["model"])
        status, problem = self._call("/factory/catalogs/providers/anthropic", self.alice, method="PUT",
                                     body={"fields": {"api_key": "sk-abcdef123"}}, if_match="2")
        self.assertEqual(400, status)
        self.assertEqual("literal_secret", problem["code"])

    def test_the_contract_lists_the_new_paths(self):
        from aisdlc import contract

        text = contract.load_contract_text(KIT)
        self.assertIn("/factory/session", text)
        self.assertIn("createCatalogEntry", text)
        self.assertIn("literal_secret", text)
        self.assertEqual([], contract.validate_contract(KIT))


if __name__ == "__main__":
    unittest.main()
