"""Phase D runtime serve adapter (#2242) — project/repo/start via ports."""
from __future__ import annotations

import json
import sys
import threading
import unittest
import urllib.error
import urllib.parse
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


class RuntimeServeTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
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
        self.repo.run(
            "workflow",
            "create",
            "--id",
            "feature-delivery",
            "--summary",
            "Feature delivery",
            "--agent",
            "implementer",
            "--skill",
            "java",
            "--mcp",
            "github",
            "--json",
        )

    def _approve(self, wid: str = "feature-delivery") -> None:
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", wid, "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))

    def _serve(self, port: int = 0):
        from aisdlc.cli import load_context
        from aisdlc.runtime import start_server

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = start_server(ctx, host="127.0.0.1", port=port)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        return server.server_address[1]

    def _post(self, port: int, fields: dict) -> tuple[int, str]:
        body = urllib.parse.urlencode(fields).encode()
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}/mutate",
            data=body,
            method="POST",
            headers={
                "Content-Type": "application/x-www-form-urlencoded",
                "Accept": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, resp.read().decode("utf-8")
        except urllib.error.HTTPError as exc:
            return exc.code, exc.read().decode("utf-8")

    def test_create_project_binds_profile_ref(self):
        port = self._serve()
        status, _ = self._post(
            port,
            {
                "module": "projects",
                "action": "create",
                "id": "alpha",
                "profile_ref": "aisdlc.toml",
            },
        )
        self.assertIn(status, (200, 302), _)
        shown = self.repo.run("project", "show", "--id", "alpha", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        payload = json.loads(shown.stdout)
        self.assertEqual("alpha", payload["id"])
        self.assertEqual("aisdlc.toml", payload["profile_ref"])

    def test_bind_repo_defaults_dry_run(self):
        port = self._serve()
        self._post(
            port,
            {
                "module": "projects",
                "action": "create",
                "id": "alpha",
                "profile_ref": "aisdlc.toml",
            },
        )
        status, _ = self._post(
            port,
            {
                "module": "projects",
                "action": "add_repo",
                "id": "alpha",
                "repo_id": "notaire",
                "forge": "github",
                "url": "https://github.com/example/notaire",
                "default_branch": "main",
            },
        )
        self.assertIn(status, (200, 302), _)
        shown = self.repo.run("project", "show", "--id", "alpha", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        repos = json.loads(shown.stdout)["repos"]
        self.assertEqual(1, len(repos))
        self.assertEqual("dry_run", repos[0]["mode"])
        self.assertEqual("github", repos[0]["forge"])

    def test_start_run_shows_queued_not_running(self):
        self._approve()
        port = self._serve()
        self._post(
            port,
            {
                "module": "projects",
                "action": "create",
                "id": "alpha",
                "profile_ref": "aisdlc.toml",
            },
        )
        status, body = self._post(
            port,
            {
                "module": "start",
                "action": "start",
                "project": "alpha",
                "workflow": "feature-delivery",
            },
        )
        self.assertIn(status, (200, 302), body)
        listed = self.repo.run("run", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        entries = json.loads(listed.stdout)["entries"]
        self.assertEqual(1, len(entries))
        self.assertIn(entries[0]["status"], ("queued", "requested"))
        self.assertNotEqual("running", entries[0]["status"])

        # Monitor HTML still not running without harness.
        out = self.repo.root / "monitor.html"
        html_result = self.repo.run(
            "runtime",
            "--html",
            str(out),
            "--module",
            "monitor",
            "--id",
            entries[0]["id"],
        )
        self.assertEqual(0, html_result.returncode, output(html_result))
        html = out.read_text(encoding="utf-8")
        self.assertNotIn('data-status="running"', html)

    def test_unapproved_start_refused(self):
        port = self._serve()
        self._post(
            port,
            {
                "module": "projects",
                "action": "create",
                "id": "alpha",
                "profile_ref": "aisdlc.toml",
            },
        )
        status, body = self._post(
            port,
            {
                "module": "start",
                "action": "start",
                "project": "alpha",
                "workflow": "feature-delivery",
            },
        )
        self.assertEqual(400, status)
        self.assertIn("workflow_not_approved", body)
        listed = self.repo.run("run", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        self.assertEqual([], json.loads(listed.stdout)["entries"])


if __name__ == "__main__":
    unittest.main()
