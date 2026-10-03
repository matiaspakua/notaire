#!/usr/bin/env python3
"""Assert frontend ESLint is a blocking CI gate (#1048 / CU76)."""

from __future__ import annotations

import os
import subprocess
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
FRONTEND_DIR = REPO_ROOT / "frontend"
FRONTEND_CI = REPO_ROOT / ".github/workflows/frontend-ci.yml"
PREFLIGHT = REPO_ROOT / "scripts/preflight.sh"
ESLINT_CONFIG = REPO_ROOT / "frontend/eslint.config.mjs"
PACKAGE_JSON = REPO_ROOT / "frontend/package.json"
ESLINT_BIN = FRONTEND_DIR / "node_modules" / ".bin" / "eslint"


def load_workflow(path: Path) -> dict:
    with path.open(encoding="utf-8") as handle:
        workflow = yaml.safe_load(handle)
    # PyYAML (YAML 1.1) parses the top-level `on:` key as boolean True.
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def find_step(job: dict, name: str) -> dict | None:
    for step in job.get("steps", []):
        if step.get("name") == name:
            return step
    return None


class FrontendEslintBlockingTest(unittest.TestCase):
    """ESLint step in frontend-ci.yml must fail the job (no continue-on-error)."""

    def setUp(self) -> None:
        self.assertTrue(FRONTEND_CI.is_file(), FRONTEND_CI)
        self.workflow = load_workflow(FRONTEND_CI)
        self.typecheck = self.workflow.get("jobs", {}).get("typecheck", {})
        self.eslint_step = find_step(self.typecheck, "ESLint")
        self.raw = FRONTEND_CI.read_text(encoding="utf-8")

    def test_eslint_step_exists_and_runs_npm_lint(self) -> None:
        self.assertIsNotNone(self.eslint_step, "ESLint step missing from typecheck job")
        self.assertEqual(self.eslint_step.get("run"), "npm run lint")

    def test_eslint_step_is_not_continue_on_error(self) -> None:
        self.assertIsNotNone(self.eslint_step, "ESLint step missing from typecheck job")
        self.assertNotEqual(
            self.eslint_step.get("continue-on-error"),
            True,
            "ESLint step must not set continue-on-error: true (#1048)",
        )
        # Explicit false is redundant; preferred form omits the key entirely.
        self.assertNotIn(
            "continue-on-error",
            self.eslint_step,
            "ESLint step should omit continue-on-error (fail-closed default)",
        )

    def test_obsolete_issue_701_advisory_comment_removed(self) -> None:
        self.assertNotRegex(
            self.raw,
            r"until\s+#701|#701.*continue-on-error|Report-only until #701",
            "Obsolete #701 advisory/report-only comment must be removed",
        )

    def test_test_reporter_continue_on_error_untouched(self) -> None:
        """Unrelated advisory publish step stays continue-on-error (out of scope)."""
        unit = self.workflow.get("jobs", {}).get("unit-tests", {})
        reporter = find_step(unit, "Publish test results")
        self.assertIsNotNone(reporter)
        self.assertIs(reporter.get("continue-on-error"), True)


class PreflightEslintBlockingMapTest(unittest.TestCase):
    """preflight.sh MAP must document CI ESLint as blocking, not advisory #701."""

    def setUp(self) -> None:
        self.assertTrue(PREFLIGHT.is_file(), PREFLIGHT)
        self.content = PREFLIGHT.read_text(encoding="utf-8")

    def test_map_does_not_call_ci_eslint_advisory(self) -> None:
        self.assertNotIn("advisory in CI — #701", self.content)
        self.assertNotRegex(self.content, r"frontend eslint.*advisory")

    def test_map_documents_blocking_eslint_in_ci(self) -> None:
        map_line = next(
            (
                line
                for line in self.content.splitlines()
                if line.startswith("frontend eslint")
            ),
            "",
        )
        self.assertTrue(map_line, "frontend eslint MAP row missing")
        self.assertIn("frontend-ci.yml", map_line)
        self.assertNotIn("#701", map_line)
        # Blocking in both places — no "advisory" qualifier.
        self.assertNotIn("advisory", map_line.lower())

    def test_preflight_and_package_share_max_warnings_zero(self) -> None:
        self.assertIn("eslint src --max-warnings=0", self.content)
        package = PACKAGE_JSON.read_text(encoding="utf-8")
        self.assertRegex(package, r'"lint"\s*:\s*"eslint src --max-warnings=0"')


class JsxA11yEnabledTest(unittest.TestCase):
    """jsx-a11y must be active via eslint-config-next core-web-vitals."""

    def test_eslint_config_extends_next_core_web_vitals(self) -> None:
        self.assertTrue(ESLINT_CONFIG.is_file(), ESLINT_CONFIG)
        text = ESLINT_CONFIG.read_text(encoding="utf-8")
        self.assertIn("eslint-config-next/core-web-vitals", text)
        self.assertRegex(text, r"nextConfig|core-web-vitals")

    def test_a11y_violation_fails_blocking_lint(self) -> None:
        """A known jsx-a11y violation under src/ must make eslint exit non-zero."""
        if not ESLINT_BIN.is_file():
            self.skipTest("frontend node_modules not installed")

        fixture = (
            'export default function BadImage() {\n'
            '  return <img src="/x.png" />;\n'
            "}\n"
        )
        target = FRONTEND_DIR / "src" / "__eslint_a11y_fixture__.tsx"
        try:
            target.write_text(fixture, encoding="utf-8")
            env = os.environ.copy()
            # Keep eslint deterministic in CI/local.
            result = subprocess.run(
                [str(ESLINT_BIN), "src/__eslint_a11y_fixture__.tsx", "--max-warnings=0"],
                cwd=FRONTEND_DIR,
                capture_output=True,
                text=True,
                env=env,
                check=False,
            )
            self.assertNotEqual(
                result.returncode,
                0,
                "jsx-a11y violation should fail blocking lint:\n"
                f"stdout={result.stdout}\nstderr={result.stderr}",
            )
            combined = (result.stdout or "") + (result.stderr or "")
            self.assertRegex(
                combined,
                r"jsx-a11y/alt-text|alt",
                "expected alt-text / jsx-a11y finding in eslint output",
            )
        finally:
            if target.exists():
                target.unlink()


if __name__ == "__main__":
    unittest.main()
