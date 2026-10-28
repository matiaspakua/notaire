#!/usr/bin/env python3
"""Guards #1661 (CU76): an OpenSpec change is scaffolded and completed from a spec file."""

from __future__ import annotations

import importlib.util
import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCAFFOLD = REPO_ROOT / "workspace" / "sdlc" / "scaffold-openspec-change.py"
HYGIENE = REPO_ROOT / "workspace" / "sdlc" / "check-openspec-archive-hygiene.py"
MARKDOWNLINT = REPO_ROOT / "frontend" / "node_modules" / ".bin" / "markdownlint-cli2"
TEMPLATES = REPO_ROOT / "docs" / "openspec" / "schemas" / "notaire-sdlc" / "templates"
NAME = "feat-9999-example-change"

SPEC = {
    "name": NAME,
    "issue": 9999,
    "uc": "CU76 — Quality Assurance and Testing Infrastructure",
    "branch": "feat/9999_example-change",
    "title": "Example change",
    "why": "Why this exists.",
    "what": "- One thing changes.",
    "rule": "An example rule holds",
    "rule_src": "#9999",
    "cap": "example-capability",
    "cap_desc": "an example.",
    "modules": {"workspace": "example tool"},
    "docs": [["docs/example.md", "mention the tool"]],
    "oos": "- Nothing else.",
    "spec": (
        "<!-- Governed by CONSTITUTION.md. CU76, issue #9999. -->\n\n"
        "## Purpose\n\nAn example capability used only by the scaffold unit test.\n\n"
        "## ADDED Requirements\n\n"
        "### Requirement: Example holds\n\nThe example MUST hold.\n\n"
        "#### Scenario: first\n\n- **WHEN** a\n- **THEN** b\n\n"
        "#### Scenario: second\n\n- **WHEN** c\n- **THEN** d\n\n"
        "#### Scenario: third\n\n- **WHEN** e\n- **THEN** f\n"
    ),
    "context": "Some context.",
    "goals": "a goal.",
    "nongoals": "a non-goal.",
    "decisions": "1. **A decision.** Because.",
    "risks": "- [A risk] → mitigated.",
    "testing": "`ExampleTest` covers every scenario.",
    "regression": "Run the suite.",
    "tasks": ["Do the first thing", "Do the second thing"],
    "test": "ExampleTest",
}


def load_hygiene():
    spec = importlib.util.spec_from_file_location("hygiene", HYGIENE)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class ScaffoldOpenspecChangeTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="scaffold-"))
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        shutil.copytree(REPO_ROOT / "workspace" / "sdlc", self.root / "workspace" / "sdlc")
        openspec = self.root / "docs" / "openspec"
        openspec.mkdir(parents=True)
        shutil.copytree(REPO_ROOT / "docs" / "openspec" / "schemas", openspec / "schemas")
        shutil.copytree(REPO_ROOT / "docs" / "openspec" / "specs", openspec / "specs")
        shutil.copy(REPO_ROOT / "docs" / "openspec" / "config.yaml", openspec / "config.yaml")
        (openspec / "changes").mkdir()
        self.spec_file = self.root / "spec.json"
        self.spec_file.write_text(json.dumps(SPEC), encoding="utf-8")
        self.change = openspec / "changes" / NAME
        self.env = {**os.environ, "NOTAIRE_REPO_ROOT": str(self.root)}
        self.offline_env = {k: v for k, v in self.env.items() if k not in ("GH_TOKEN", "GITHUB_TOKEN")}
        self.offline_env["GH_CONFIG_DIR"] = str(self.root / "no-gh-config")

    def run_tool(self, *args: str) -> subprocess.CompletedProcess:
        return subprocess.run(
            ["python3", str(SCAFFOLD), *args], capture_output=True, text=True, env=self.env, timeout=60
        )

    def scaffold(self):
        result = self.run_tool("scaffold", str(self.spec_file))
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_missing_folder_is_created(self):
        self.assertFalse(self.change.exists())
        self.scaffold()
        self.assertIn("notaire-sdlc", (self.change / ".openspec.yaml").read_text(encoding="utf-8"))

    def test_scaffolded_change_passes_plan_validation(self):
        self.scaffold()
        validator = self.root / "workspace" / "sdlc" / "validate-sdlc-plan.sh"
        result = subprocess.run(
            ["bash", str(validator), NAME], capture_output=True, text=True, env=self.offline_env, timeout=120
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_scenarios_become_coverage_rows(self):
        self.scaffold()
        traceability = (self.change / "traceability.md").read_text(encoding="utf-8")
        for scenario in ("first", "second", "third"):
            self.assertIn(f"| {scenario} | ExampleTest | pending |", traceability)

    def test_given_tasks_land_in_the_implementation_group(self):
        self.scaffold()
        tasks = (self.change / "tasks.md").read_text(encoding="utf-8")
        self.assertIn("- [ ] 4.1 Do the first thing", tasks)
        self.assertIn("- [ ] 4.2 Do the second thing", tasks)

    def test_complete_ticks_the_implementation_groups(self):
        self.scaffold()
        result = self.run_tool("complete", NAME, "--test", "ExampleTest")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        tasks = (self.change / "tasks.md").read_text(encoding="utf-8")
        self.assertEqual([], load_hygiene().unchecked_impl_tasks(tasks))
        traceability = (self.change / "traceability.md").read_text(encoding="utf-8")
        self.assertIn("| first | ExampleTest | passing |", traceability)
        self.assertIn("- [ ] 10.", tasks)

    def test_scaffolded_change_passes_markdownlint(self):
        """#1689: fresh scaffold must not invent markdownlint debt."""
        if not MARKDOWNLINT.is_file():
            self.skipTest("frontend/node_modules/.bin/markdownlint-cli2 not installed")
        self.scaffold()
        files = sorted(str(path) for path in self.change.rglob("*.md"))
        result = subprocess.run(
            [str(MARKDOWNLINT), "--no-globs", *files],
            capture_output=True,
            text=True,
            timeout=60,
        )
        self.assertEqual(
            result.returncode,
            0,
            result.stdout + result.stderr,
        )

    def test_schema_templates_pass_markdownlint(self):
        """#1689: seed templates must lint clean before agents fill them."""
        if not MARKDOWNLINT.is_file():
            self.skipTest("frontend/node_modules/.bin/markdownlint-cli2 not installed")
        files = sorted(str(path) for path in TEMPLATES.glob("*.md"))
        result = subprocess.run(
            [str(MARKDOWNLINT), "--no-globs", *files],
            capture_output=True,
            text=True,
            timeout=60,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()
