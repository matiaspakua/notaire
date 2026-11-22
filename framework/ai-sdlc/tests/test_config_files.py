"""Config-as-code: tree listing, file read with redaction, path confinement, catalog delete (#2398).

Gate 2 (RED) — failing tests only; production code in config_files.py, serve.py, catalogs.py, cli.py.
"""
from __future__ import annotations

import json
import os
import sys
import threading
import unittest
import urllib.error
import urllib.request
from pathlib import Path

from helpers import ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))

# Build a secret-shaped fixture at runtime so the source tree stays clean for secret scan.
SECRET_TOKEN = "ghp_" + ("X" * 36)

PROFILE_WITH_CONTEXT = """
[project]
name = "demo"
policy = "POLICY.md"

[skills]
dir = "skills"

[catalogs]
dir = ".aisdlc/catalogs"

[context]
files = ["CONTEXT.md"]
"""


class ConfigFilesTreeTest(unittest.TestCase):
    """GET /factory/config/files — tree listing of config roots."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE_WITH_CONTEXT)
        self.repo.write("POLICY.md", "# Policy\n")
        self.repo.write("CONTEXT.md", "# Context\nAlways loaded.\n")
        (self.repo.root / "skills").mkdir(exist_ok=True)
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1",
        )
        self.assertEqual(0, add.returncode, output(add))
        created = self.repo.run("workflow", "create", "--id", "w1", "--summary", "demo")
        self.assertEqual(0, created.returncode, output(created))

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
        method: str = "GET",
        token: str | None,
        headers: dict | None = None,
        body: dict | None = None,
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

    # ------------------------------------------------------------------ tree listing

    def test_tree_listing_returns_config_roots(self):
        port, token = self._serve()
        status, ctype, body = self._request(port, "/factory/config/files", token=token)
        self.assertEqual(200, status, body)
        self.assertEqual("application/json", ctype)
        rows = json.loads(body)
        self.assertIsInstance(rows, list)
        # Every row has the required fields
        for row in rows:
            self.assertIn("path", row)
            self.assertIn("kind", row)
            self.assertIn("size", row)
            self.assertIn("media_type", row)
        # Profile file should be present
        kinds = {r["kind"] for r in rows}
        self.assertIn("profile", kinds)

    def test_tree_listing_requires_read_scope(self):
        port, _ = self._serve()
        status, ctype, body = self._request(port, "/factory/config/files", token=None)
        self.assertEqual(401, status)
        self.assertEqual("application/problem+json", ctype)


class ConfigFileReadTest(unittest.TestCase):
    """GET /factory/config/files/{path} — file content with redaction."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE_WITH_CONTEXT)
        self.repo.write("POLICY.md", "# Policy\n")
        self.repo.write("CONTEXT.md", "# Context\n")
        # Write a config file containing a secret-shaped value
        self.repo.write(
            "secrets.toml",
            f'token = "{SECRET_TOKEN}"\nother = "safe"\n',
        )
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
        method: str = "GET",
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
                return resp.status, resp.headers.get_content_type(), resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, exc.headers.get_content_type(), exc.read().decode("utf-8")

    # ------------------------------------------------------------------ file read

    def test_file_read_returns_redacted_content(self):
        port, token = self._serve()
        status, ctype, body = self._request(
            port, "/factory/config/files/secrets.toml", token=token
        )
        self.assertEqual(200, status, body)
        payload = json.loads(body)
        self.assertIn("content", payload)
        self.assertTrue(payload.get("redacted", False))
        self.assertNotIn(SECRET_TOKEN, payload["content"])
        # env:NAME references must stay visible
        self.assertIn("other", payload["content"])

    def test_file_read_no_secrets(self):
        port, token = self._serve()
        status, _, body = self._request(
            port, "/factory/config/files/POLICY.md", token=token
        )
        self.assertEqual(200, status, body)
        payload = json.loads(body)
        self.assertFalse(payload.get("redacted", False))
        self.assertIn("Policy", payload["content"])

    def test_file_read_requires_read_scope(self):
        port, _ = self._serve()
        status, ctype, _ = self._request(
            port, "/factory/config/files/POLICY.md", token=None
        )
        self.assertEqual(401, status)
        self.assertEqual("application/problem+json", ctype)

    def test_file_read_media_type_by_extension(self):
        port, token = self._serve()
        status, _, body = self._request(
            port, "/factory/config/files/POLICY.md", token=token
        )
        self.assertEqual(200, status, body)
        payload = json.loads(body)
        self.assertIn("media_type", payload)
        self.assertIn("markdown", payload["media_type"].lower())


class ConfigFilePathTraversalTest(unittest.TestCase):
    """Path traversal, absolute path, encoded separators, symlink escape."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE_WITH_CONTEXT)
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
        token: str | None,
    ):
        hdrs = {}
        if token is not None:
            hdrs["Authorization"] = f"Bearer {token}"
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            headers=hdrs,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, exc.read().decode("utf-8")

    def test_traversal_with_dotdot_refused(self):
        port, token = self._serve()
        status, body = self._request(
            port, "/factory/config/files/..%2F..%2Fetc%2Fpasswd", token=token
        )
        self.assertEqual(400, status, body)
        self.assertEqual("invalid_id", json.loads(body)["code"])

    def test_absolute_path_refused(self):
        port, token = self._serve()
        status, body = self._request(
            port, "/factory/config/files/etc/passwd", token=token
        )
        self.assertEqual(400, status, body)
        self.assertEqual("invalid_id", json.loads(body)["code"])

    def test_encoded_separator_percent2f_refused(self):
        port, token = self._serve()
        status, body = self._request(
            port, "/factory/config/files/.aisdlc%2F..%2Fetc", token=token
        )
        self.assertEqual(400, status, body)
        self.assertEqual("invalid_id", json.loads(body)["code"])

    def test_encoded_separator_percent5c_refused(self):
        port, token = self._serve()
        status, body = self._request(
            port, "/factory/config/files/.aisdlc%5C..%5Cetc", token=token
        )
        self.assertEqual(400, status, body)
        self.assertEqual("invalid_id", json.loads(body)["code"])

    def test_symlink_escape_refused(self):
        # Create a symlink inside the repo pointing outside
        outside = self.repo.root / "outside"
        outside.mkdir(parents=True, exist_ok=True)
        target = outside / "secret.txt"
        target.write_text("top-secret", encoding="utf-8")
        skills_dir = self.repo.root / "skills"
        skills_dir.mkdir(parents=True, exist_ok=True)
        link = skills_dir / "evil-link"
        link.symlink_to(target)

        port, token = self._serve()
        status, body = self._request(
            port, "/factory/config/files/skills/evil-link", token=token
        )
        self.assertEqual(400, status, body)
        self.assertEqual("invalid_id", json.loads(body)["code"])


class CatalogDeleteHttpTest(unittest.TestCase):
    """DELETE /factory/catalogs/{kind}/{id} — optimistic concurrency, CSRF, evidence."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE_WITH_CONTEXT)
        self.repo.write("POLICY.md", "# Policy\n")
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1",
        )
        self.assertEqual(0, add.returncode, output(add))

    def _ctx(self):
        from aisdlc.cli import load_context
        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def _serve(self, *, enable_write: bool = True, scope: str = "write"):
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
        method: str = "DELETE",
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
                return resp.status, resp.headers.get_content_type(), resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, exc.headers.get_content_type(), exc.read().decode("utf-8")

    # ------------------------------------------------------------------ delete

    def test_delete_with_matching_if_match_succeeds(self):
        port, token = self._serve()
        status, ctype, body = self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(200, status, body)
        self.assertEqual("application/json", ctype)
        payload = json.loads(body)
        self.assertEqual("demo-skill", payload.get("id"))
        # Entry must be gone
        entry = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo-skill" / "current.json"
        self.assertFalse(entry.exists())

    def test_delete_with_stale_if_match_returns_409(self):
        port, token = self._serve()
        status, ctype, body = self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"If-Match": "99", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(409, status, body)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("version_conflict", json.loads(body)["code"])
        # Entry must still exist
        entry = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo-skill" / "current.json"
        self.assertTrue(entry.exists())

    def test_delete_without_if_match_returns_428(self):
        port, token = self._serve()
        status, ctype, body = self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"X-AISDLC-CSRF": token},
        )
        self.assertEqual(428, status, body)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("if_match_required", json.loads(body)["code"])

    def test_delete_requires_write_scope(self):
        port, token = self._serve(scope="read")
        status, ctype, body = self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(403, status, body)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("forbidden", json.loads(body)["code"])

    def test_delete_requires_csrf(self):
        port, token = self._serve()
        status, ctype, body = self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"If-Match": "1"},
        )
        self.assertEqual(403, status, body)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("csrf_required", json.loads(body)["code"])

    def test_delete_unknown_entry_returns_404(self):
        port, token = self._serve()
        status, ctype, body = self._request(
            port, "/factory/catalogs/skills/nonexistent",
            token=token,
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(404, status, body)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("not_found", json.loads(body)["code"])

    def test_delete_appends_evidence_record(self):
        port, token = self._serve()
        before = self.repo.ledger()
        self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        after = self.repo.ledger()
        self.assertGreater(len(after), len(before))
        # Last record should be a catalog.delete
        last = after[-1]
        self.assertEqual("catalog.delete", last.get("kind"))
        self.assertEqual("demo-skill", last.get("id"))
        # Evidence chain must still verify
        from aisdlc.cli import load_context
        from aisdlc import evidence as ev
        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        self.assertEqual(0, ev.verify(ctx))


class CatalogDeleteOverHttpReadOnlyTest(unittest.TestCase):
    """DELETE over HTTP without --write returns 405."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE_WITH_CONTEXT)
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
        method: str = "DELETE",
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
                return resp.status, resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, exc.read().decode("utf-8")

    def test_delete_without_write_flag_returns_405(self):
        port, token = self._serve(enable_write=False)
        status, body = self._request(
            port, "/factory/catalogs/skills/demo-skill",
            token=token,
            headers={"If-Match": "1", "X-AISDLC-CSRF": token},
        )
        self.assertEqual(405, status, body)
        self.assertEqual("method_not_allowed", json.loads(body)["code"])


class CliConfigFilesTest(unittest.TestCase):
    """CLI parity: aisdlc config files, aisdlc config show, aisdlc catalog remove --if-match."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE_WITH_CONTEXT)
        self.repo.write("POLICY.md", "# Policy\n")
        self.repo.write("CONTEXT.md", "# Context\n")
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1",
        )
        self.assertEqual(0, add.returncode, output(add))

    def test_cli_config_files_lists_tree(self):
        result = self.repo.run("config", "files", "--json")
        self.assertEqual(0, result.returncode, output(result))
        rows = json.loads(result.stdout)
        self.assertIsInstance(rows, list)
        for row in rows:
            self.assertIn("path", row)
            self.assertIn("kind", row)

    def test_cli_config_show_displays_file(self):
        result = self.repo.run("config", "show", "POLICY.md")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("Policy", result.stdout)

    def test_cli_config_show_redacts_secrets(self):
        self.repo.write(
            "secrets.toml",
            f'token = "{SECRET_TOKEN}"\n',
        )
        result = self.repo.run("config", "show", "secrets.toml")
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn(SECRET_TOKEN, result.stdout)
        self.assertIn("***", result.stdout)

    def test_cli_catalog_remove_with_if_match(self):
        result = self.repo.run(
            "catalog", "remove", "--kind", "skills", "--id", "demo-skill",
            "--if-match", "1",
        )
        self.assertEqual(0, result.returncode, output(result))
        entry = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo-skill" / "current.json"
        self.assertFalse(entry.exists())

    def test_cli_catalog_remove_stale_if_match_nonzero(self):
        result = self.repo.run(
            "catalog", "remove", "--kind", "skills", "--id", "demo-skill",
            "--if-match", "99",
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("version_conflict", result.stderr + result.stdout)
        # Entry must still exist
        entry = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo-skill" / "current.json"
        self.assertTrue(entry.exists())


if __name__ == "__main__":
    unittest.main()
