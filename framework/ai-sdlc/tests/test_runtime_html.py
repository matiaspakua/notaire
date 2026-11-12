"""Phase D runtime UI HTML (#2242) — Projects / Start / Monitor."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""

NAV_LABELS = ("Projects", "Start run", "Monitor")


class RuntimeHtmlTest(unittest.TestCase):
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
        self.repo.run(
            "workflow",
            "create",
            "--id",
            "draft-only",
            "--summary",
            "Still draft",
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

    def test_runtime_shell_has_projects_start_monitor_and_kit_brand(self):
        out = self.repo.root / "runtime.html"
        result = self.repo.run("runtime", "--html", str(out), "--module", "projects")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn("AI SDLC factory", html)
        self.assertNotIn("Notaire", html)
        for label in NAV_LABELS:
            self.assertIn(label, html)
        self.assertIn('id="module-nav"', html)
        self.assertIn("module-nav right", html)
        self.assertIn('data-surface="runtime"', html)

    def test_approved_only_workflow_picker(self):
        self._approve("feature-delivery")
        out = self.repo.root / "start.html"
        result = self.repo.run("runtime", "--html", str(out), "--module", "start")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-module="start"', html)
        self.assertIn('value="feature-delivery"', html)
        self.assertNotIn('value="draft-only"', html)
        self.assertIn("approved", html.casefold())

    def test_monitor_empty_no_adapter_copy_and_cancel_disabled(self):
        self._approve()
        self.repo.run(
            "project",
            "create",
            "--id",
            "alpha",
            "--profile-ref",
            "aisdlc.toml",
            "--json",
        )
        started = self.repo.run(
            "run",
            "start",
            "--project",
            "alpha",
            "--workflow",
            "feature-delivery",
            "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        out = self.repo.root / "monitor.html"
        result = self.repo.run(
            "runtime", "--html", str(out), "--module", "monitor", "--id", run_id,
        )
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-module="monitor"', html)
        self.assertIn(run_id, html)
        # No harness event → honest empty / not running.
        self.assertNotRegex(html, r'data-status="running"')
        self.assertTrue(
            'data-status="queued"' in html or 'data-status="requested"' in html,
            html,
        )
        self.assertIn("no adapter", html.casefold())
        self.assertIn('data-supported="false"', html)
        self.assertIn("cancel", html.casefold())

    def test_no_commit_or_approve_affordances(self):
        out = self.repo.root / "runtime.html"
        for module in ("projects", "start", "monitor"):
            result = self.repo.run("runtime", "--html", str(out), "--module", module)
            self.assertEqual(0, result.returncode, output(result))
            html = out.read_text(encoding="utf-8").casefold()
            self.assertNotIn('name="action" value="approve"', html)
            self.assertNotIn('name="action" value="commit"', html)
            self.assertNotIn("git commit", html)
            self.assertNotIn("owner_override", html)


if __name__ == "__main__":
    unittest.main()
