"""Phase C backoffice modules + runs monitor (#2241)."""
from __future__ import annotations

import hashlib
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

NAV_LABELS = (
    "Workflows",
    "Agents",
    "Skills",
    "Tools / MCP",
    "Providers",
    "Knowledge",
    "Rules / Gates",
    "Integrations",
    "Runs",
)


def _digest(line: str | None) -> str:
    return "genesis" if line is None else hashlib.sha256(line.encode("utf-8")).hexdigest()


def _seed_evidence(repo: Repo) -> None:
    ledger_dir = repo.root / ".aisdlc" / "evidence"
    ledger_dir.mkdir(parents=True, exist_ok=True)
    prev = None
    lines = []
    for i, ok in enumerate((True, False, True)):
        record = {
            "ts": f"2026-11-11T1{i}:00:00+00:00",
            "stage": "plan" if ok else "green",
            "ok": ok,
            "gates": [{"name": "gate-a", "status": "pass" if ok else "fail", "seconds": 1.0 + i}],
            "prev": _digest(prev),
        }
        line = json.dumps(record, sort_keys=True)
        lines.append(line)
        prev = line
    (ledger_dir / "ledger.jsonl").write_text("\n".join(lines) + "\n", encoding="utf-8")

    cost_prev = None
    cost_lines = []
    cost = {
        "ts": "2026-11-11T12:00:00+00:00",
        "agent": "implementer",
        "model": "test-model",
        "input": 100,
        "output": 50,
        "usd": 0.01,
        "prev": _digest(cost_prev),
    }
    cost_line = json.dumps(cost, sort_keys=True)
    cost_lines.append(cost_line)
    (ledger_dir / "cost.jsonl").write_text("\n".join(cost_lines) + "\n", encoding="utf-8")


class BackofficePhaseCTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (
            ("mcp", "github"),
            ("providers", "openai"),
            ("memory", "context-pack"),
            ("rules", "intake-lint"),
            ("integrations", "forge-github"),
        ):
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

    def test_nav_lists_phase_c_modules_and_kit_brand(self):
        out = self.repo.root / "backoffice.html"
        result = self.repo.run("backoffice", "--html", str(out))
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn("AI SDLC factory", html)
        self.assertIn('class="module-nav right"', html)
        for label in NAV_LABELS:
            self.assertIn(label, html)
        self.assertNotIn("Notaire", html)
        self.assertNotIn("/dashboard/administration", html)

    def test_list_and_detail_for_mcp_and_providers(self):
        mcp_out = self.repo.root / "mcp.html"
        result = self.repo.run("backoffice", "--html", str(mcp_out), "--module", "mcp")
        self.assertEqual(0, result.returncode, output(result))
        mcp_html = mcp_out.read_text(encoding="utf-8")
        self.assertIn("github", mcp_html)
        self.assertIn('data-module="mcp"', mcp_html)
        self.assertIn("Tools / MCP", mcp_html)

        detail = self.repo.root / "mcp-detail.html"
        result = self.repo.run(
            "backoffice", "--html", str(detail), "--module", "mcp", "--id", "github"
        )
        self.assertEqual(0, result.returncode, output(result))
        detail_html = detail.read_text(encoding="utf-8")
        self.assertIn("Version history", detail_html)
        self.assertIn("github", detail_html)

        prov = self.repo.root / "providers.html"
        result = self.repo.run("backoffice", "--html", str(prov), "--module", "providers")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("openai", prov.read_text(encoding="utf-8"))

    def test_create_and_edit_allocate_versions_for_knowledge(self):
        from aisdlc.backoffice import start_server
        from aisdlc.cli import load_context

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = start_server(ctx, host="127.0.0.1", port=0)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        port = server.server_address[1]

        body = urllib.parse.urlencode(
            {
                "module": "memory",
                "action": "create",
                "id": "team-notes",
                "ref": ".aisdlc/memory/team-notes",
                "summary": "v1 notes",
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

        body2 = urllib.parse.urlencode(
            {
                "module": "memory",
                "action": "edit",
                "id": "team-notes",
                "summary": "v2 notes",
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

        version_path = (
            self.repo.root / ".aisdlc" / "catalogs" / "memory" / "team-notes" / "versions" / "1.json"
        )
        self.assertTrue(version_path.is_file())
        v1_bytes = version_path.read_bytes()

        hist = self.repo.run(
            "catalog", "history", "--kind", "memory", "--id", "team-notes", "--json"
        )
        self.assertEqual(0, hist.returncode, output(hist))
        versions = [row["version"] for row in json.loads(hist.stdout)["versions"]]
        self.assertEqual([1, 2], versions)
        self.assertEqual(v1_bytes, version_path.read_bytes())

    def test_provider_secrets_redacted(self):
        from aisdlc.catalogs import load_entry, write_new_revision
        from aisdlc.cli import load_context

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        data = load_entry(ctx, "providers", "openai")
        data["api_key"] = "sk-PHASE-C-PLAINTEXT"
        data["password"] = "provider-secret-hunter2"
        write_new_revision(ctx, "providers", "openai", data)

        out = self.repo.root / "providers-secret.html"
        result = self.repo.run(
            "backoffice", "--html", str(out), "--module", "providers", "--id", "openai"
        )
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertNotIn("sk-PHASE-C-PLAINTEXT", html)
        self.assertNotIn("provider-secret-hunter2", html)
        self.assertIn("***", html)

    def test_runs_monitor_shows_evidence_summary(self):
        _seed_evidence(self.repo)
        out = self.repo.root / "runs.html"
        result = self.repo.run("backoffice", "--html", str(out), "--module", "runs")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-module="runs"', html)
        self.assertIn('data-view="runs-monitor"', html)
        self.assertIn("3", html)  # run count from seeded ledger
        self.assertIn("test-model", html)
        self.assertIn("0.01", html)
        self.assertIn('aria-live="polite"', html)

    def test_cancel_and_retry_honestly_empty_when_unsupported(self):
        _seed_evidence(self.repo)
        out = self.repo.root / "runs-actions.html"
        result = self.repo.run("backoffice", "--html", str(out), "--module", "runs")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-action="cancel"', html)
        self.assertIn('data-action="retry"', html)
        self.assertIn('data-supported="false"', html)
        self.assertIn("unavailable", html.lower())
        self.assertNotIn('data-supported="true"', html)
        self.assertIn('data-action="cancel" data-supported="false" disabled', html)
        self.assertIn('data-action="retry" data-supported="false" disabled', html)

    def test_runs_mutations_rejected(self):
        from aisdlc.backoffice import start_server
        from aisdlc.cli import load_context

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = start_server(ctx, host="127.0.0.1", port=0)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        port = server.server_address[1]

        for action in ("create", "edit", "cancel", "retry"):
            body = urllib.parse.urlencode(
                {
                    "module": "runs",
                    "action": action,
                    "id": "run-1",
                    "summary": "should-fail",
                }
            ).encode()
            req = urllib.request.Request(
                f"http://127.0.0.1:{port}/mutate",
                data=body,
                method="POST",
                headers={
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Accept": "application/json",
                },
            )
            with self.assertRaises(urllib.error.HTTPError) as caught:
                urllib.request.urlopen(req, timeout=5)
            self.assertGreaterEqual(caught.exception.code, 400)

        # No parallel run store created.
        self.assertFalse((self.repo.root / ".aisdlc" / "runs").exists())

    def test_rules_and_integrations_modules_list(self):
        for module, eid in (("rules", "intake-lint"), ("integrations", "forge-github")):
            out = self.repo.root / f"{module}.html"
            result = self.repo.run("backoffice", "--html", str(out), "--module", module)
            self.assertEqual(0, result.returncode, output(result))
            html = out.read_text(encoding="utf-8")
            self.assertIn(eid, html)
            self.assertIn(f'data-module="{module}"', html)


if __name__ == "__main__":
    unittest.main()
