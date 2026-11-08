#!/usr/bin/env python3
"""Guards #1958 (CU76): Notaire is the first adopter of the stack-agnostic AI SDLC framework.

The kit lives in framework/ai-sdlc and knows nothing about Notaire; Notaire's profile
(.aisdlc/aisdlc.toml) maps the framework stages onto the existing scripts. Two rules that
were prose only are now mechanical: baselines only shrink against main, and the skills
catalog stays unique, described, and generic where it claims to be.

Run with: python3 workspace/tests/test_aisdlc_adoption.py
"""
from __future__ import annotations

import subprocess
import sys
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
KIT = REPO_ROOT / "framework" / "ai-sdlc"
AISDLC = KIT / "bin" / "aisdlc"
PROFILE = REPO_ROOT / ".aisdlc" / "aisdlc.toml"
PREFLIGHT = REPO_ROOT / "workspace" / "sdlc" / "preflight.sh"
PROCESS = REPO_ROOT / ".github" / "workflows" / "sdlc-process.yml"
BASELINES = (
    "backend-api/src/test/resources/architecture/hexagon-baseline.txt",
    "backend-api/src/test/resources/architecture/usecase-cycle-baseline.txt",
    "backend-api/src/test/resources/architecture/controller-loose-returns.txt",
    "backend-api/src/test/resources/architecture/entity-dto-mapping.txt",
    "frontend/eslint-suppressions.json",
)


def aisdlc(*args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, str(AISDLC), "--profile", str(PROFILE), *args],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
    )


def process_steps(job: str) -> list[dict]:
    workflow = yaml.safe_load(PROCESS.read_text(encoding="utf-8"))
    return workflow["jobs"][job]["steps"]


class NotaireProfileTest(unittest.TestCase):
    def test_profile_passes_doctor(self):
        result = aisdlc("doctor")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_profile_ratchets_every_architecture_baseline(self):
        text = PROFILE.read_text(encoding="utf-8")
        for baseline in BASELINES:
            self.assertIn(f'file = "{baseline}"', text)

    def test_skills_catalog_passes_the_lint(self):
        result = aisdlc("skills")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_new_frontend_skills_are_committed_and_cataloged(self):
        catalog = (REPO_ROOT / ".claude" / "skills" / "README.md").read_text(encoding="utf-8")
        for name in ("ui-design", "motion-design"):
            self.assertTrue((REPO_ROOT / ".claude" / "skills" / name / "SKILL.md").is_file(), name)
            self.assertIn(f"`{name}`", catalog)


class WiringTest(unittest.TestCase):
    def test_framework_is_a_registered_module(self):
        modules = yaml.safe_load((REPO_ROOT / "workspace" / "modules.yaml").read_text(encoding="utf-8"))["modules"]
        self.assertIn("ai-sdlc-framework", modules)
        self.assertEqual("framework/ai-sdlc", modules["ai-sdlc-framework"]["path"])

    def test_preflight_runs_ratchet_skills_and_kit_tests(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("aisdlc --profile .aisdlc/aisdlc.toml ratchet --base origin/main", text)
        self.assertIn("aisdlc --profile .aisdlc/aisdlc.toml skills", text)
        self.assertIn("python3 -m unittest discover -s framework/ai-sdlc/tests", text)

    def test_process_checks_run_ratchet_skills_and_kit_tests(self):
        runs = "\n".join(step.get("run", "") for step in process_steps("process-checks"))
        self.assertIn('ratchet --base "$BASE_SHA"', runs)
        self.assertIn("aisdlc --profile .aisdlc/aisdlc.toml skills", runs)
        self.assertIn("python3 -m unittest discover -s framework/ai-sdlc/tests", runs)

    def test_main_health_runs_kit_tests_and_skills_lint(self):
        runs = "\n".join(step.get("run", "") for step in process_steps("main-health"))
        self.assertIn("aisdlc --profile .aisdlc/aisdlc.toml skills", runs)
        self.assertIn("python3 -m unittest discover -s framework/ai-sdlc/tests", runs)

    def test_evidence_ledger_is_not_committed(self):
        self.assertIn(".aisdlc/evidence/", (REPO_ROOT / ".gitignore").read_text(encoding="utf-8"))


class ReportTest(unittest.TestCase):
    REPORT = REPO_ROOT / "docs" / "300-development" / "306-ai-sdlc-framework" / "ai_sdlc_framework.md"

    def test_report_exists_and_separates_evidence_from_inference(self):
        text = self.REPORT.read_text(encoding="utf-8")
        for heading in ("Evidence", "Inference", "Open questions", "Iteration 1", "Iteration 2", "Iteration 3"):
            self.assertIn(heading, text)
        self.assertGreaterEqual(text.count("```mermaid"), 5)

    def test_report_svg_assets_exist_and_are_accessible(self):
        for svg in sorted((self.REPORT.parent / "assets").glob("*.svg")):
            with self.subTest(svg.name):
                text = svg.read_text(encoding="utf-8")
                self.assertIn("<title", text)
                self.assertIn('role="img"', text)
                self.assertIn(f"assets/{svg.name}", self.REPORT.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
