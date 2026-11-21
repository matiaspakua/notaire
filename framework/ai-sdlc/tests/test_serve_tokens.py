"""Several named serve tokens (#2384): each user authenticates with their own scope and is the actor."""
from __future__ import annotations

import json
import stat
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


class ServeTokensTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1",
        )
        self.assertEqual(0, add.returncode, output(add))

    def _serve(self):
        from aisdlc import serve as serve_mod
        from aisdlc.cli import load_context
        from aisdlc.serve_token import add_token, ensure_token

        ensure_token(self.repo.root, scope="owner")
        self.alice = add_token(self.repo.root, identity="alice", scope="write")
        self.bob = add_token(self.repo.root, identity="bob", scope="read")
        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = serve_mod.start_server(ctx, host="127.0.0.1", port=0, enable_write=True)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        return server.server_address[1]

    def _request(self, port, path, *, method="GET", token=None, csrf=None, body=None, if_match=None):
        headers = {}
        if token is not None:
            headers["Authorization"] = f"Bearer {token}"
        if csrf is not None:
            headers["X-AISDLC-CSRF"] = csrf
        if if_match is not None:
            headers["If-Match"] = if_match
        data = None
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            headers["Content-Type"] = "application/json"
        req = urllib.request.Request(f"http://127.0.0.1:{port}{path}", data=data, method=method, headers=headers)
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:
            return exc.code, json.loads(exc.read().decode("utf-8"))

    def _put_skill(self, port, token, csrf, summary):
        return self._request(
            port, "/factory/catalogs/skills/demo-skill", method="PUT", token=token.value, csrf=csrf,
            body={"id": "demo-skill", "summary": summary}, if_match="1",
        )

    def test_each_named_token_reads_with_its_own_scope_and_unknown_tokens_are_refused(self):
        port = self._serve()
        self.assertEqual(200, self._request(port, "/factory/capabilities", token=self.alice.value)[0])
        self.assertEqual(200, self._request(port, "/factory/capabilities", token=self.bob.value)[0])
        status, problem = self._request(port, "/factory/capabilities", token="not-a-token")
        self.assertEqual(401, status)
        self.assertEqual("unauthorized", problem["code"])

    def test_a_write_records_the_matching_identity_as_the_actor(self):
        port = self._serve()
        status, payload = self._put_skill(port, self.alice, self.alice.value, "by alice")
        self.assertEqual(200, status, payload)
        self.assertEqual("ui:alice", payload["created_by"])

    def test_a_read_token_cannot_write_and_the_problem_names_the_scope(self):
        port = self._serve()
        status, problem = self._put_skill(port, self.bob, self.bob.value, "by bob")
        self.assertEqual(403, status)
        self.assertEqual("forbidden", problem["code"])
        self.assertIn("write", problem["detail"])

    def test_the_csrf_header_must_match_the_presented_token(self):
        port = self._serve()
        status, problem = self._put_skill(port, self.alice, self.bob.value, "mixed")
        self.assertEqual(403, status)
        self.assertEqual("csrf_required", problem["code"])

    def test_a_revoked_token_no_longer_authenticates(self):
        from aisdlc.serve_token import revoke_token

        port = self._serve()
        self.assertTrue(revoke_token(self.repo.root, "alice"))
        self.assertEqual(401, self._request(port, "/factory/capabilities", token=self.alice.value)[0])

    def test_the_token_file_is_private_and_listings_carry_no_secret(self):
        from aisdlc.serve_token import add_token, list_tokens, tokens_path

        add_token(self.repo.root, identity="carol", scope="read")
        mode = stat.S_IMODE(tokens_path(self.repo.root).stat().st_mode)
        self.assertEqual(0o600, mode)
        secret = add_token(self.repo.root, identity="carl", scope="write").value
        listed = list_tokens(self.repo.root)
        self.assertIn({"identity": "carol", "scope": "read"}, [{k: e[k] for k in ("identity", "scope")} for e in listed])
        self.assertTrue(all(set(e) <= {"identity", "scope", "source"} for e in listed))
        cli = self.repo.run("serve-token", "list", "--json")
        self.assertEqual(0, cli.returncode, output(cli))
        self.assertNotIn(secret, cli.stdout)
        self.assertTrue(all(set(e) <= {"identity", "scope", "source"} for e in json.loads(cli.stdout)))

    def test_token_files_are_ignored_by_git(self):
        import subprocess

        from aisdlc.serve_token import add_token, ensure_token

        ensure_token(self.repo.root, scope="owner")
        add_token(self.repo.root, identity="gina", scope="read")
        for name in ("serve.token", "serve.tokens"):
            ignored = subprocess.run(
                ["git", "check-ignore", "-q", f".aisdlc/{name}"], cwd=self.repo.root, check=False,
            )
            self.assertEqual(0, ignored.returncode, name)
        add_token(self.repo.root, identity="hana", scope="read")
        lines = (self.repo.root / ".aisdlc" / ".gitignore").read_text(encoding="utf-8").splitlines()
        self.assertEqual(1, lines.count("serve.tokens"))

    def test_identities_are_unique_and_validated(self):
        from aisdlc.errors import KitError
        from aisdlc.serve_token import add_token

        add_token(self.repo.root, identity="dana", scope="read")
        with self.assertRaises(KitError):
            add_token(self.repo.root, identity="dana", scope="write")
        with self.assertRaises(KitError):
            add_token(self.repo.root, identity="../evil", scope="read")

    def test_the_cli_adds_a_token_and_prints_the_secret_once(self):
        added = self.repo.run("serve-token", "add", "--identity", "erin", "--scope", "write", "--json")
        self.assertEqual(0, added.returncode, output(added))
        first = json.loads(added.stdout)
        self.assertEqual({"identity": "erin", "scope": "write"}, {k: first[k] for k in ("identity", "scope")})
        self.assertGreater(len(first["token"]), 20)
        listed = json.loads(self.repo.run("serve-token", "list", "--json").stdout)
        self.assertNotIn(first["token"], json.dumps(listed))


if __name__ == "__main__":
    unittest.main()
