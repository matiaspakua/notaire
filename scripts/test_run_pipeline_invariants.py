#!/usr/bin/env python3
"""
Guards scripts/run_pipeline.sh for issue #856:

Goal: run_pipeline.sh is the single, mandatory, dashboarded pre-PR gate
(CONSTITUTION.md Gate 4 precondition) — it composes the existing gates rather
than duplicating them, brings the Docker stack up itself, adds a markdown-lint
pass that exists nowhere else in the repo, and produces one HTML dashboard
plus a plain-text log with a non-zero exit code on any blocking failure.

Plain stdlib unittest, consistent with this project's other one-off
script/config invariant checks (see scripts/test_ci_workflow_invariants.py).
Run with: python3 scripts/test_run_pipeline_invariants.py
"""
import os
import stat
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCRIPT_PATH = os.path.join(REPO_ROOT, "scripts", "run_pipeline.sh")
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


if __name__ == "__main__":
    unittest.main()
