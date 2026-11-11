"""Backoffice serve adapter (#2240) — mutations via Phase A ports only."""
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


class BackofficeServeTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            result = self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
            self.assertEqual(0, result.returncode, output(result))

    def _serve(self, port: int = 0):
        from aisdlc.backoffice import start_server
        from aisdlc.cli import load_context

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = start_server(ctx, host="127.0.0.1", port=port)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        return server.server_address[1]

    def test_create_and_edit_allocate_phase_a_versions(self):
        port = self._serve()
        # Create agent via POST form.
        body = urllib.parse.urlencode(
            {
                "module": "agents",
                "action": "create",
                "id": "reviewer",
                "ref": ".aisdlc/agents/reviewer",
                "summary": "v1 reviewer",
            }
        ).encode()
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}/mutate",
            data=body,
            method="POST",
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )
        with urllib.request.urlopen(req, timeout=5) as resp:
            self.assertIn(resp.status, (200, 302))

        hist = self.repo.run("catalog", "history", "--kind", "agents", "--id", "reviewer", "--json")
        self.assertEqual(0, hist.returncode, output(hist))
        versions = [row["version"] for row in json.loads(hist.stdout)["versions"]]
        self.assertEqual([1], versions)

        version_path = (
            self.repo.root / ".aisdlc" / "catalogs" / "agents" / "reviewer" / "versions" / "1.json"
        )
        # After first create, version file exists once we update.
        body2 = urllib.parse.urlencode(
            {
                "module": "agents",
                "action": "edit",
                "id": "reviewer",
                "summary": "v2 reviewer",
            }
        ).encode()
        req2 = urllib.request.Request(
            f"http://127.0.0.1:{port}/mutate",
            data=body2,
            method="POST",
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )
        with urllib.request.urlopen(req2, timeout=5) as resp:
            self.assertIn(resp.status, (200, 302))

        self.assertTrue(version_path.is_file())
        v1_bytes = version_path.read_bytes()

        hist2 = self.repo.run("catalog", "history", "--kind", "agents", "--id", "reviewer", "--json")
        self.assertEqual(0, hist2.returncode, output(hist2))
        versions2 = [row["version"] for row in json.loads(hist2.stdout)["versions"]]
        self.assertEqual([1, 2], versions2)
        self.assertEqual(v1_bytes, version_path.read_bytes())

    def test_illegal_approve_via_serve_fails_closed(self):
        created = self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java", "--mcp", "github", "--json",
        )
        self.assertEqual(0, created.returncode, output(created))

        port = self._serve()
        body = urllib.parse.urlencode(
            {
                "module": "workflows",
                "action": "transition",
                "id": "feature-delivery",
                "transition": "approve",
                "owner_override": "1",
                "reason": "should-not-matter",
            }
        ).encode()
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}/mutate",
            data=body,
            method="POST",
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                # Success HTTP with error page is ok; must not change state.
                page = resp.read().decode("utf-8")
                self.assertTrue("error" in page.lower() or resp.status >= 400)
        except urllib.error.HTTPError as exc:
            self.assertGreaterEqual(exc.code, 400)

        shown = self.repo.run("workflow", "show", "--id", "feature-delivery", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        self.assertEqual("draft", json.loads(shown.stdout)["governance"])

    def test_get_index_serves_shell(self):
        port = self._serve()
        with urllib.request.urlopen(f"http://127.0.0.1:{port}/", timeout=5) as resp:
            html = resp.read().decode("utf-8")
        self.assertIn("AI SDLC factory", html)
        self.assertIn('class="module-nav right"', html)
