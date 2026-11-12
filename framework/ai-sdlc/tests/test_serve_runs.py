"""Serve HTTP for projects/runs + cancel 501 honesty (#2255)."""
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


class ServeRunsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
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

    def test_http_get_post_projects_and_runs(self):
        port, token = self._serve(enable_write=True)
        status, ctype, body = self._request(
            port,
            "/factory/projects",
            method="POST",
            token=token,
            body={
                "id": "alpha",
                "profile_ref": "aisdlc.toml",
                "repos": [
                    {
                        "id": "notaire",
                        "forge": "github",
                        "url": "https://github.com/example/notaire",
                        "default_branch": "main",
                    }
                ],
            },
            headers={"X-AISDLC-CSRF": token},
        )
        self.assertEqual(200, status, body)
        self.assertEqual("application/json", ctype)
        project = json.loads(body)
        self.assertEqual("dry_run", project["repos"][0]["mode"])

        status, _, body = self._request(port, "/factory/projects", method="GET", token=token)
        self.assertEqual(200, status)
        self.assertIn("alpha", [row["id"] for row in json.loads(body)["entries"]])

        status, _, body = self._request(
            port,
            "/factory/runs",
            method="POST",
            token=token,
            body={"project": "alpha", "workflow": "feature-delivery"},
            headers={"X-AISDLC-CSRF": token},
        )
        self.assertEqual(200, status, body)
        run = json.loads(body)
        self.assertIn(run["status"], ("requested", "queued"))
        run_id = run["id"]

        status, _, body = self._request(port, f"/factory/runs/{run_id}", method="GET", token=token)
        self.assertEqual(200, status)
        self.assertEqual(run_id, json.loads(body)["id"])

        status, _, body = self._request(
            port, f"/factory/runs/{run_id}/events", method="GET", token=token,
        )
        self.assertEqual(200, status)
        events = json.loads(body)["events"]
        self.assertTrue(any(ev.get("kind") == "run.request" for ev in events))

    def test_cancel_returns_501_with_capability_reason(self):
        port, token = self._serve(enable_write=True)
        self._request(
            port,
            "/factory/projects",
            method="POST",
            token=token,
            body={"id": "alpha", "profile_ref": "aisdlc.toml", "repos": []},
            headers={"X-AISDLC-CSRF": token},
        )
        status, _, body = self._request(
            port,
            "/factory/runs",
            method="POST",
            token=token,
            body={"project": "alpha", "workflow": "feature-delivery"},
            headers={"X-AISDLC-CSRF": token},
        )
        self.assertEqual(200, status, body)
        run_id = json.loads(body)["id"]

        status, ctype, body = self._request(
            port,
            f"/factory/runs/{run_id}/cancel",
            method="POST",
            token=token,
            body={},
            headers={"X-AISDLC-CSRF": token},
        )
        self.assertEqual(501, status)
        self.assertEqual("application/problem+json", ctype)
        problem = json.loads(body)
        self.assertIn(problem["code"], ("not_implemented", "runner_unavailable"))
        self.assertIn("runner", problem["detail"].casefold())


if __name__ == "__main__":
    unittest.main()
