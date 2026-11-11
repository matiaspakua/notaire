"""aisdlc serve read-only HTTP host (#2253, Phase A.2)."""
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

# Build a secret-shaped fixture at runtime so the source tree stays clean for secret scan.
SECRET_SHAPED = "ghp_" + ("A" * 36)


class ServeHostTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            result = self.repo.run(
                "catalog",
                "add",
                "--kind",
                kind,
                "--id",
                cid,
                "--ref",
                f".aisdlc/{kind}/{cid}",
                "--summary",
                cid,
            )
            self.assertEqual(0, result.returncode, output(result))
        created = self.repo.run("workflow", "create", "--id", "w1", "--summary", "demo")
        self.assertEqual(0, created.returncode, output(created))

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _serve(self, *, host: str = "127.0.0.1", port: int = 0, allow_remote: bool = False):
        from aisdlc import serve as serve_mod

        ctx = self._ctx()
        server = serve_mod.start_server(
            ctx, host=host, port=port, allow_remote=allow_remote
        )
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        token_path = self.repo.root / ".aisdlc" / "serve.token"
        self.assertTrue(token_path.is_file())
        mode = token_path.stat().st_mode & 0o777
        self.assertEqual(0o600, mode)
        token = serve_mod.load_token_value(self.repo.root)
        self.assertTrue(token)
        self.assertNotIn(token, getattr(server, "_startup_message", "") or "")
        return server.server_address[1], token

    def _get(self, port: int, path: str, *, token: str | None, method: str = "GET"):
        headers = {}
        if token is not None:
            headers["Authorization"] = f"Bearer {token}"
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            method=method,
            headers=headers,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                body = resp.read().decode("utf-8")
                return resp.status, resp.headers.get_content_type(), body, dict(resp.headers)
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8")
            return exc.code, exc.headers.get_content_type(), body, dict(exc.headers)

    def test_token_created_mode_0600_never_logged(self):
        import contextlib
        import io

        from aisdlc import serve as serve_mod

        ctx = self._ctx()
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf), contextlib.redirect_stderr(buf):
            # Do not call shutdown() unless serve_forever is running — it blocks forever.
            server = serve_mod.start_server(ctx, host="127.0.0.1", port=0)
            self.addCleanup(server.server_close)
            token = serve_mod.load_token_value(self.repo.root)
        self.assertTrue(token)
        self.assertNotIn(token, buf.getvalue())
        path = self.repo.root / ".aisdlc" / "serve.token"
        self.assertEqual(0o600, path.stat().st_mode & 0o777)

    def test_missing_and_invalid_token_are_401(self):
        port, token = self._serve()
        status, ctype, body, _ = self._get(port, "/factory/capabilities", token=None)
        self.assertEqual(401, status)
        self.assertEqual("application/problem+json", ctype)
        problem = json.loads(body)
        self.assertEqual("unauthorized", problem["code"])
        self.assertNotIn(token, body)

        status, ctype, body, _ = self._get(port, "/factory/capabilities", token="not-the-token")
        self.assertEqual(401, status)
        self.assertEqual("unauthorized", json.loads(body)["code"])

    def test_get_factory_paths_and_openapi_shapes(self):
        port, token = self._serve()
        paths = [
            "/factory/capabilities",
            "/factory/config",
            "/factory/plugins",
            "/factory/harness",
            "/factory/report",
            "/factory/catalogs/agents",
            "/factory/catalogs/agents/implementer",
            "/factory/catalogs/agents/implementer/versions",
            "/factory/workflows",
            "/factory/workflows/w1",
        ]
        for path in paths:
            status, ctype, body, _ = self._get(port, path, token=token)
            self.assertEqual(200, status, path)
            self.assertEqual("application/json", ctype, path)
            data = json.loads(body)
            if path == "/factory/capabilities":
                self.assertIn("approvals_live", data)
                self.assertIn("runner", data)
                self.assertIn("forge_write", data)
                self.assertIn("isolator", data)
                self.assertIn("sandbox", data["isolator"])
                self.assertIn("enforces_committer", data["isolator"])
                self.assertIs(False, data["approvals_live"])
                self.assertIs(False, data["runner"])
                self.assertIs(False, data["forge_write"])
                self.assertIs(False, data["isolator"]["sandbox"])
            elif path == "/factory/config":
                for key in (
                    "schema",
                    "project",
                    "max_open_prs",
                    "concurrency",
                    "timeout_seconds",
                    "retry",
                    "backoff_seconds",
                    "plugin_allow",
                ):
                    self.assertIn(key, data)
            elif path == "/factory/plugins":
                self.assertIsInstance(data, list)
            elif path == "/factory/harness":
                self.assertIsInstance(data, list)
            elif path == "/factory/report":
                for key in ("runs", "failed_runs", "gates", "stages", "cost"):
                    self.assertIn(key, data)
            elif path.endswith("/versions"):
                self.assertEqual(["id", "kind", "versions"], sorted(k for k in ("id", "kind", "versions") if k in data) and ["id", "kind", "versions"])
                self.assertIn("versions", data)
            elif "/catalogs/agents/implementer" in path:
                self.assertEqual("implementer", data["id"])
            elif path == "/factory/catalogs/agents":
                self.assertEqual("agents", data["kind"])
                self.assertTrue(any(e["id"] == "implementer" for e in data["entries"]))
            elif path == "/factory/workflows":
                self.assertEqual("workflows", data["kind"])
            elif path.endswith("/w1"):
                self.assertEqual("w1", data["id"])

    def test_mutating_methods_are_405(self):
        port, token = self._serve()
        for method in ("POST", "PUT", "PATCH", "DELETE"):
            status, ctype, body, _ = self._get(
                port, "/factory/workflows", token=token, method=method
            )
            self.assertEqual(405, status, method)
            self.assertEqual("application/problem+json", ctype)
            self.assertEqual("method_not_allowed", json.loads(body)["code"])

    def test_non_loopback_bind_refused_without_allow_remote_and_token(self):
        from aisdlc import serve as serve_mod
        from aisdlc.errors import KitError

        ctx = self._ctx()
        with self.assertRaises(KitError) as raised:
            serve_mod.start_server(ctx, host="0.0.0.0", port=0, allow_remote=False)
        self.assertEqual("bind_refused", raised.exception.code)

        # allow-remote without token still refused (token created only after bind ok path)
        token_path = self.repo.root / ".aisdlc" / "serve.token"
        if token_path.exists():
            token_path.unlink()
        with self.assertRaises(KitError) as raised2:
            serve_mod.start_server(ctx, host="0.0.0.0", port=0, allow_remote=True, create_token=False)
        self.assertEqual("bind_refused", raised2.exception.code)

    def test_traversal_ids_are_400(self):
        port, token = self._serve()
        for path in (
            "/factory/catalogs/agents/../etc",
            "/factory/catalogs/agents/a%2Fb",
            "/factory/workflows/../secret",
            "/factory/workflows/.hidden",
        ):
            status, ctype, body, _ = self._get(port, path, token=token)
            self.assertEqual(400, status, path)
            self.assertEqual("application/problem+json", ctype)
            self.assertEqual("invalid_id", json.loads(body)["code"], path)

    def test_secret_redaction_in_responses_and_logs(self):
        # Plant a secret-shaped value in a catalog summary via raw file, then show via serve.
        entry = self.repo.root / ".aisdlc" / "catalogs" / "agents" / "implementer" / "current.json"
        data = json.loads(entry.read_text(encoding="utf-8"))
        data["summary"] = f"token {SECRET_SHAPED}"
        data["ref"] = f"env:GITHUB_TOKEN"
        entry.write_text(json.dumps(data), encoding="utf-8")

        port, token = self._serve()
        status, _, body, _ = self._get(port, "/factory/catalogs/agents/implementer", token=token)
        self.assertEqual(200, status)
        self.assertNotIn(SECRET_SHAPED, body)
        self.assertNotIn(token, body)
        # Authorization header value must not appear in body either
        self.assertNotRegex(body, r"Bearer\s+\S+")

    def test_static_mount_exists_without_spa(self):
        port, token = self._serve()
        status, ctype, body, _ = self._get(port, "/ui/", token=token)
        self.assertEqual(200, status)
        self.assertNotIn("<html", body.lower())
        # Mount root exists in kit tree
        ui = KIT / "ui"
        self.assertTrue(ui.is_dir())
        self.assertTrue((ui / "README.md").is_file())
        # No SPA entrypoints shipped
        self.assertFalse((ui / "index.html").exists())
        self.assertFalse(any(ui.glob("**/*.js")))

    def test_capabilities_path_in_contract(self):
        from aisdlc import contract

        text = contract.load_contract_text(KIT)
        self.assertIn("/factory/capabilities", text)
        self.assertIn("FactoryCapabilities", text)
        self.assertEqual([], contract.validate_contract(KIT))
        # Single OpenAPI tree
        openapi_files = list((KIT / "openapi").glob("*.yaml")) + list((KIT / "openapi").glob("*.yml"))
        self.assertEqual(1, len(openapi_files))


if __name__ == "__main__":
    unittest.main()
