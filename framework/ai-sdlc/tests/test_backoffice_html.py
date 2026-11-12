"""Kit backoffice HTML shell (#2240 Phase B) — right nav, governance, redaction."""
from __future__ import annotations

import json
import re
import sys
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class BackofficeHtmlTest(unittest.TestCase):
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

    def test_shell_has_right_nav_and_kit_brand(self):
        out = self.repo.root / "backoffice.html"
        result = self.repo.run("backoffice", "--html", str(out))
        self.assertEqual(0, result.returncode, output(result))
        self.assertTrue(out.is_file())
        html = out.read_text(encoding="utf-8")
        self.assertIn("<!DOCTYPE html>", html)
        self.assertIn("AI SDLC factory", html)
        self.assertIn('id="module-nav"', html)
        self.assertIn('class="module-nav right"', html)
        self.assertIn("Workflows", html)
        self.assertIn("Agents", html)
        self.assertIn("Skills", html)
        # Kit-neutral: no Notaire product chrome markers.
        self.assertNotIn("Notaire", html)
        self.assertNotIn("notaire", html.lower())
        self.assertNotIn("/dashboard/administration", html)

    def test_main_can_shrink_so_the_page_does_not_scroll_sideways_at_360px(self):
        out = self.repo.root / "runs.html"
        result = self.repo.run("backoffice", "--html", str(out), "--module", "runs")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        main_rule = re.search(r"\bmain\s*\{([^}]*)\}", html)
        self.assertIsNotNone(main_rule)
        self.assertRegex(main_rule.group(1), r"min-width:\s*0")
        for pre in re.findall(r"<pre\b[^>]*>", html):
            self.assertIn('tabindex="0"', pre, "a scrollable JSON block must be keyboard focusable")
            self.assertIn("aria-label=", pre)

    def test_list_and_detail_for_agents_and_skills(self):
        out = self.repo.root / "agents.html"
        result = self.repo.run("backoffice", "--html", str(out), "--module", "agents")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn("implementer", html)
        self.assertIn('data-module="agents"', html)

        detail = self.repo.root / "agent-detail.html"
        result = self.repo.run(
            "backoffice", "--html", str(detail), "--module", "agents", "--id", "implementer"
        )
        self.assertEqual(0, result.returncode, output(result))
        detail_html = detail.read_text(encoding="utf-8")
        self.assertIn("implementer", detail_html)
        self.assertIn("Version history", detail_html)

        skills_out = self.repo.root / "skills.html"
        result = self.repo.run("backoffice", "--html", str(skills_out), "--module", "skills")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("java", skills_out.read_text(encoding="utf-8"))

    def test_workflow_allowed_transitions_only(self):
        created = self.repo.run(
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
        self.assertEqual(0, created.returncode, output(created))

        out = self.repo.root / "workflow.html"
        result = self.repo.run(
            "backoffice",
            "--html",
            str(out),
            "--module",
            "workflows",
            "--id",
            "feature-delivery",
        )
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-governance="draft"', html)
        self.assertIn('data-transition="define"', html)
        self.assertNotIn('data-transition="approve"', html)
        self.assertIn("draft", html)

    def test_history_listed_on_detail_after_edit(self):
        self.repo.run(
            "catalog", "add", "--kind", "agents", "--id", "reviewer",
            "--ref", ".aisdlc/agents/reviewer", "--summary", "v1",
        )
        self.repo.run(
            "catalog", "update", "--kind", "agents", "--id", "reviewer", "--summary", "v2"
        )
        out = self.repo.root / "reviewer.html"
        result = self.repo.run(
            "backoffice", "--html", str(out), "--module", "agents", "--id", "reviewer"
        )
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-version="1"', html)
        self.assertIn('data-version="2"', html)
        self.assertIn("Version history", html)

    def test_secret_refs_redacted_in_html(self):
        # Attach a secret-shaped field via providers kind then show agents module
        # with a crafted agent that carries credential metadata through update path.
        # Agents store ref/summary; put secret in summary-like extra via providers
        # and ensure backoffice agents page never echoes plaintext when we also
        # render a providers credential on a skill/agent show that was poisoned.
        add = self.repo.run(
            "catalog",
            "add",
            "--kind",
            "agents",
            "--id",
            "secret-agent",
            "--ref",
            "env:AGENT_TOKEN",
            "--summary",
            "ok summary",
        )
        self.assertEqual(0, add.returncode, output(add))
        # Write a credential field into current.json the way a bad operator might,
        # then ensure show/HTML redacts it.
        from aisdlc.catalogs import load_entry, write_new_revision
        from aisdlc.cli import load_context

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        data = load_entry(ctx, "agents", "secret-agent")
        data["api_key"] = "sk-PLAINTEXT-SECRET-VALUE"
        data["password"] = "hunter2-secret"
        write_new_revision(ctx, "agents", "secret-agent", data)

        out = self.repo.root / "secret.html"
        result = self.repo.run(
            "backoffice", "--html", str(out), "--module", "agents", "--id", "secret-agent"
        )
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertNotIn("sk-PLAINTEXT-SECRET-VALUE", html)
        self.assertNotIn("hunter2-secret", html)
        self.assertIn("***", html)

    def test_create_form_surfaces_present(self):
        out = self.repo.root / "create.html"
        result = self.repo.run("backoffice", "--html", str(out), "--module", "skills")
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn('data-action="create"', html)
        self.assertIn('name="id"', html)
        self.assertIn('name="summary"', html)
