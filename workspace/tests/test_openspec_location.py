#!/usr/bin/env python3
"""
Guards issue #1292 (ADR-026): OpenSpec lives under docs/ (the knowledge base) and the CLI resolves
its root from there, because it hard-codes the folder name `openspec`.

Also guards #2218: agents must discover notaire-sdlc from docs/ (or via seed --create), not from
the repository root. A root `openspec/` folder remains forbidden.

Run with: python3 workspace/tests/test_openspec_location.py
"""
import json
import shutil
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
OPENSPEC = REPO_ROOT / "docs" / "openspec"
SEED = REPO_ROOT / "workspace" / "sdlc" / "seed-openspec-change.sh"
ADAPTATIONS = OPENSPEC / "NOTAIRE-ADAPTATIONS.md"
AGENT_WORKFLOW_SKILL = REPO_ROOT / ".claude" / "skills" / "ai-agent-workflow" / "SKILL.md"
OPENSPEC_PROPOSE_SKILL = REPO_ROOT / ".claude" / "skills" / "openspec-propose" / "SKILL.md"
AGENT_WORKFLOW_RULE = REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md"


class OpenSpecLocationTest(unittest.TestCase):
    def test_openspec_lives_under_docs(self):
        for name in ("config.yaml", "schemas", "specs", "changes"):
            self.assertTrue((OPENSPEC / name).exists(), f"docs/openspec/{name} missing")

    def test_no_openspec_folder_at_the_repository_root(self):
        self.assertFalse((REPO_ROOT / "openspec").exists(), "docs/openspec/ moved to docs/openspec/")

    @unittest.skipUnless(shutil.which("openspec"), "openspec CLI not installed")
    def test_cli_resolves_the_root_from_docs(self):
        out = subprocess.run(["openspec", "list", "--specs"], cwd=REPO_ROOT / "docs",
                             capture_output=True, text=True, check=True).stdout
        self.assertIn("login-lockout-e2e", out)

    @unittest.skipUnless(shutil.which("openspec"), "openspec CLI not installed")
    def test_schemas_lists_notaire_sdlc_from_docs_cwd(self):
        """#2218 — project schema is discoverable when cwd is docs/."""
        raw = subprocess.run(
            ["openspec", "schemas", "--json"],
            cwd=REPO_ROOT / "docs",
            capture_output=True,
            text=True,
            check=True,
        ).stdout
        schemas = json.loads(raw)
        notaire = next((s for s in schemas if s.get("name") == "notaire-sdlc"), None)
        self.assertIsNotNone(notaire, "notaire-sdlc missing from openspec schemas --json (cwd=docs)")
        self.assertEqual(notaire.get("source"), "project")

    @unittest.skipUnless(shutil.which("openspec"), "openspec CLI not installed")
    def test_schemas_from_repo_root_do_not_list_notaire_sdlc(self):
        """Honest friction: root cwd has no openspec/ folder, so only packaged schemas appear."""
        raw = subprocess.run(
            ["openspec", "schemas", "--json"],
            cwd=REPO_ROOT,
            capture_output=True,
            text=True,
            check=True,
        ).stdout
        names = {s.get("name") for s in json.loads(raw)}
        self.assertNotIn(
            "notaire-sdlc",
            names,
            "Root cwd must not invent notaire-sdlc without an openspec/ folder (ADR-026)",
        )

    def test_seed_create_runs_openspec_from_docs(self):
        text = SEED.read_text(encoding="utf-8")
        self.assertIn('cd "$REPO_ROOT/docs"', text)
        self.assertIn("openspec new change", text)
        self.assertIn("--schema notaire-sdlc", text)

    def test_agent_docs_point_at_working_scaffold_path(self):
        """#2218 acceptance: workflow + openspec skill name the working path."""
        adaptations = ADAPTATIONS.read_text(encoding="utf-8")
        workflow_skill = AGENT_WORKFLOW_SKILL.read_text(encoding="utf-8")
        propose_skill = OPENSPEC_PROPOSE_SKILL.read_text(encoding="utf-8")
        workflow_rule = AGENT_WORKFLOW_RULE.read_text(encoding="utf-8")

        self.assertIn("seed-openspec-change.sh", adaptations)
        self.assertIn("cd docs", adaptations)
        self.assertIn("notaire-sdlc", adaptations)

        self.assertIn("seed-openspec-change.sh", workflow_skill)
        self.assertIn("--create", workflow_skill)
        self.assertIn("cd docs", workflow_skill)

        self.assertIn("cd docs", propose_skill)
        self.assertIn("notaire-sdlc", propose_skill)

        self.assertIn("seed-openspec-change.sh", workflow_rule)
        self.assertIn("--create", workflow_rule)


if __name__ == "__main__":
    unittest.main()
