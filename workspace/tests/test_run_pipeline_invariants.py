#!/usr/bin/env python3
"""
Guards workspace/sdlc/run_pipeline.sh for issue #856:

Goal: run_pipeline.sh is the single, mandatory, dashboarded pre-PR gate
(CONSTITUTION.md Gate 4 precondition) — it composes the existing gates rather
than duplicating them, brings the Docker stack up itself, adds a markdown-lint
pass that exists nowhere else in the repo, and produces one HTML dashboard
plus a plain-text log with a non-zero exit code on any blocking failure.

Plain stdlib unittest, consistent with this project's other one-off
script/config invariant checks (see workspace/tests/test_ci_workflow_invariants.py).
Run with: python3 workspace/tests/test_run_pipeline_invariants.py
"""
import os
import stat
import subprocess
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SCRIPT_PATH = os.path.join(REPO_ROOT, "workspace", "sdlc", "run_pipeline.sh")
GITIGNORE_PATH = os.path.join(REPO_ROOT, ".gitignore")


def load_script():
    with open(SCRIPT_PATH, encoding="utf-8") as f:
        return f.read()


class RunPipelineExistsTest(unittest.TestCase):
    def test_script_exists(self):
        self.assertTrue(os.path.isfile(SCRIPT_PATH), f"{SCRIPT_PATH} must exist")

    def test_script_is_executable(self):
        mode = os.stat(SCRIPT_PATH).st_mode
        self.assertTrue(mode & stat.S_IXUSR, "run_pipeline.sh must be executable (chmod +x)")

    def test_script_has_bash_strict_mode(self):
        content = load_script()
        self.assertRegex(content, r"set -\w*u\w*o?\s*pipefail|set -e", "must fail fast on errors")


class RunPipelineComposesExistingGatesTest(unittest.TestCase):
    """It must delegate to the existing gates, not reimplement them (DRY)."""

    def setUp(self):
        self.content = load_script()

    def test_composes_sdlc_plan_validation(self):
        self.assertIn("validate-sdlc-plan.sh", self.content)

    def test_composes_preflight_full(self):
        self.assertIn("preflight.sh", self.content)
        self.assertIn("--full", self.content)

    def test_starts_docker_stack_via_start_sh(self):
        self.assertIn("start.sh", self.content)


class RunPipelineNewGatesTest(unittest.TestCase):
    """Gates run_pipeline.sh adds on top of preflight.sh."""

    def setUp(self):
        self.content = load_script()

    def test_has_markdown_lint_step(self):
        self.assertRegex(self.content.lower(), r"markdownlint|markdown.lint")

    def test_documents_markdown_lint_as_local_only(self):
        # Honesty convention from preflight.sh --list: a gate with no CI
        # counterpart must say so in the usage header.
        self.assertRegex(self.content, r"no CI job|local.only|not.*mirrored.*CI|CI job: none")


class RunPipelineDashboardTest(unittest.TestCase):
    def setUp(self):
        self.content = load_script()

    def test_generates_html_dashboard(self):
        self.assertIn(".html", self.content)

    def test_writes_plain_text_log(self):
        self.assertRegex(self.content, r"\.log\b")

    def test_reports_directory_is_gitignored(self):
        with open(GITIGNORE_PATH, encoding="utf-8") as f:
            lines = {line.strip() for line in f}
        self.assertTrue(
            {"reports/", "/reports/", "reports"} & lines,
            "reports/ output dir must be git-ignored",
        )


class RunPipelineExitCodeTest(unittest.TestCase):
    def test_exits_nonzero_on_failure(self):
        content = load_script()
        self.assertIn("exit 1", content)


class RunPipelinePrunesOldRunsTest(unittest.TestCase):
    def test_removes_previous_runs_before_creating_the_new_one(self):
        content = load_script()
        prune = content.index('find "$REPO_ROOT/reports/pipeline"')
        create = content.index('mkdir -p "$REPORT_DIR"')
        self.assertLess(prune, create)


class RunPipelineModeContractsTest(unittest.TestCase):
    """Requested modes must not silently weaken the quality evidence."""

    def setUp(self):
        self.content = load_script()

    def test_with_infra_requires_sonarqube_quality_gate(self):
        self.assertRegex(
            self.content,
            r"--with-infra\) WITH_INFRA=1; REQUIRE_SONAR=1",
            "--with-infra must make the requested SonarQube quality gate blocking",
        )

    def test_fast_mode_skips_server_backed_phases(self):
        self.assertRegex(
            self.content,
            r'if \[ "\$FAST_MODE" -eq 1 \]; then\s+'
            r'phase_skip "docker stack up"',
            "--fast must not start the application Docker stack",
        )
        self.assertRegex(
            self.content,
            r'phase_skip "security dast baseline" "--fast"',
            "--fast must not run live DAST",
        )

    def test_conflicting_sonarqube_flags_fail_with_usage_status(self):
        self.assertRegex(
            self.content,
            r'--skip-sonar cannot be combined with --with-infra or --require-sonar',
        )
        self.assertRegex(
            self.content,
            r'--skip-sonar cannot be combined[\s\S]{0,300}exit 2',
            "conflicting quality-gate flags must fail before executing phases",
        )

    def test_help_lists_every_option(self):
        result = subprocess.run(
            ["bash", SCRIPT_PATH, "--help"], capture_output=True, text=True, check=False
        )
        self.assertEqual(result.returncode, 0)
        for option in ("--with-infra", "--require-sonar", "--skip-sonar", "--fast", "--fix"):
            self.assertIn(option, result.stdout, f"--help must document {option}")


if __name__ == "__main__":
    unittest.main()
