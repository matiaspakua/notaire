"""aisdlc serve --write If-Match PUT, CSRF, write scope (#2254)."""
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


class ServeWritesTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1",
        )
        self.assertEqual(0, add.returncode, output(add))

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _serve(self, *, enable_write: bool = False, scope: str = "owner"):
        from aisdlc import serve as serve_mod
        from aisdlc.serve_token import ensure_token

        ctx = self._ctx()
        path = self.repo.root / ".aisdlc" / "serve.token"
        if path.is_file():
            path.unlink()
        ensure_token(self.repo.root, scope=scope)
        server = serve_mod.start_server(
            ctx, host="127.0.0.1", port=0, allow_remote=False, enable_write=enable_write,
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
        body: dict | None = None,
        headers: dict | None = None,
    ):
        hdrs = dict(headers or {})
        if token is not None:
            hdrs["Authorization"] = f"Bearer {token}"
        data = None
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            hdrs.setdefault("Content-Type", "application/json")
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            data=data,
            method=method,
            headers=hdrs,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, resp.headers.get_content_type(), resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, exc.headers.get_content_type(), exc.read().decode("utf-8")

    def test_put_without_write_remains_405(self):
        port, token = self._serve(enable_write=False)
        status, ctype, body = self._request(
            port,
            "/factory/catalogs/skills/demo-skill",
            method="PUT",
            token=token,
            body={"id": "demo-skill", "summary": "nope"},
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(405, status)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("method_not_allowed", json.loads(body)["code"])

    def test_put_with_if_match_succeeds_under_write(self):
        port, token = self._serve(enable_write=True, scope="write")
        status, ctype, body = self._request(
            port,
            "/factory/catalogs/skills/demo-skill",
            method="PUT",
            token=token,
            body={"id": "demo-skill", "summary": "v2-http"},
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(200, status, body)
        self.assertEqual("application/json", ctype)
        payload = json.loads(body)
        self.assertEqual(2, payload["version"])
        self.assertEqual("v2-http", payload["summary"])
        self.assertTrue(str(payload.get("created_by", "")).startswith("ui:"))

    def test_missing_csrf_or_read_scope_refused(self):
        port, token = self._serve(enable_write=True, scope="write")
        status, _, body = self._request(
            port,
            "/factory/catalogs/skills/demo-skill",
            method="PUT",
            token=token,
            body={"id": "demo-skill", "summary": "x"},
            headers={"If-Match": "1"},
        )
        self.assertIn(status, (403, 400))
        self.assertIn(json.loads(body)["code"], {"csrf_required", "forbidden"})

        # Downgrade token to read and retry with CSRF.
        path = self.repo.root / ".aisdlc" / "serve.token"
        path.write_text(f"token={token}\nscope=read\n", encoding="utf-8")
        status, _, body = self._request(
            port,
            "/factory/catalogs/skills/demo-skill",
            method="PUT",
            token=token,
            body={"id": "demo-skill", "summary": "x"},
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(403, status)
        self.assertEqual("forbidden", json.loads(body)["code"])


if __name__ == "__main__":
    unittest.main()
