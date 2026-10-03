#!/usr/bin/env python3
"""
Guards issue #1041 / CU76: CI bots must not commit generated reports into main.

Invariants:
- ci.yml publish-reports, cd.yml publish-report, playwright-e2e.yml
  coverage-report, and pr-validation.yml must not git-commit/push
  docs/wiki/cicd-reports (or configure CI Bot for that purpose).
- Those report-publish jobs must not request contents: write (CD release may).
- Reports remain discoverable via upload-artifact and/or GITHUB_STEP_SUMMARY.
- docs/wiki/cicd-reports/ is gitignored and untracked.

Run with: python3 scripts/test_no_bot_report_commits.py
"""
from __future__ import annotations

import os
import subprocess
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WORKFLOWS_DIR = os.path.join(REPO_ROOT, ".github", "workflows")
GITIGNORE_PATH = os.path.join(REPO_ROOT, ".gitignore")

REPORT_JOBS = (
    ("ci.yml", "publish-reports"),
    ("cd.yml", "publish-report"),
    ("playwright-e2e.yml", "coverage-report"),
)

WORKFLOWS_NO_REPORT_COMMITS = (
    "ci.yml",
    "cd.yml",
    "playwright-e2e.yml",
    "pr-validation.yml",
)

CICD_REPORTS_IGNORE = "docs/wiki/cicd-reports/"
CICD_REPORTS_PREFIX = "docs/wiki/cicd-reports/"


def load_workflow(workflow_file: str) -> dict:
    with open(os.path.join(WORKFLOWS_DIR, workflow_file), encoding="utf-8") as f:
        workflow = yaml.safe_load(f)
    # PyYAML (YAML 1.1) parses the top-level `on:` key as boolean True.
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def iter_run_scripts(job: dict) -> list[str]:
    scripts: list[str] = []
    for step in job.get("steps") or []:
        if not isinstance(step, dict):
            continue
        run = step.get("run")
        if isinstance(run, str):
            scripts.append(run)
    return scripts


def job_run_blob(job: dict) -> str:
    return "\n".join(iter_run_scripts(job))


def job_has_upload_artifact(job: dict) -> bool:
    for step in job.get("steps") or []:
        if not isinstance(step, dict):
            continue
        uses = step.get("uses") or ""
        if uses.startswith("actions/upload-artifact@"):
            return True
    return False


def job_writes_step_summary(job: dict) -> bool:
    blob = job_run_blob(job)
    return "GITHUB_STEP_SUMMARY" in blob


def job_contents_permission(job: dict) -> str | None:
    permissions = job.get("permissions") or {}
    if not isinstance(permissions, dict):
        return None
    contents = permissions.get("contents")
    return contents if isinstance(contents, str) else None


def git_tracked_under(prefix: str) -> list[str]:
    result = subprocess.run(
        ["git", "-C", REPO_ROOT, "ls-files", "--", prefix],
        check=True,
        capture_output=True,
        text=True,
    )
    return [line for line in result.stdout.splitlines() if line.strip()]


class NoBotReportCommitsTest(unittest.TestCase):
    """Workflows never commit generated CI/CD/E2E reports into the repo."""

    def test_report_jobs_have_no_git_commit_or_push_of_cicd_reports(self):
        for workflow_file, job_name in REPORT_JOBS:
            with self.subTest(workflow=workflow_file, job=job_name):
                job = load_workflow(workflow_file)["jobs"][job_name]
                blob = job_run_blob(job)
                self.assertNotIn(
                    "git commit",
                    blob,
                    f"{workflow_file}:{job_name} must not git commit reports",
                )
                self.assertNotIn(
                    "git push",
                    blob,
                    f"{workflow_file}:{job_name} must not git push reports",
                )
                self.assertNotIn(
                    "CI Bot",
                    blob,
                    f"{workflow_file}:{job_name} must not configure CI Bot for report commits",
                )
                self.assertNotIn(
                    "docs/wiki/cicd-reports",
                    blob,
                    f"{workflow_file}:{job_name} must not stage docs/wiki/cicd-reports",
                )

    def test_pr_validation_has_no_wiki_report_commits(self):
        workflow = load_workflow("pr-validation.yml")
        for job_name, job in workflow.get("jobs", {}).items():
            with self.subTest(job=job_name):
                blob = job_run_blob(job)
                self.assertNotIn("docs/wiki/cicd-reports", blob)
                if "git commit" in blob:
                    self.fail(
                        f"pr-validation.yml:{job_name} must not git commit "
                        "(wiki/CI reports stay as artifacts / PR comments)"
                    )

    def test_all_listed_workflows_lack_ci_bot_report_commit_pattern(self):
        for workflow_file in WORKFLOWS_NO_REPORT_COMMITS:
            with self.subTest(workflow=workflow_file):
                workflow = load_workflow(workflow_file)
                for job_name, job in workflow.get("jobs", {}).items():
                    blob = job_run_blob(job)
                    if "CI Bot" in blob and "docs/wiki/cicd-reports" in blob:
                        self.fail(
                            f"{workflow_file}:{job_name} still configures CI Bot "
                            "for docs/wiki/cicd-reports commits"
                        )
                    if "git commit" in blob and "docs/wiki/cicd-reports" in blob:
                        self.fail(
                            f"{workflow_file}:{job_name} still git-commits "
                            "docs/wiki/cicd-reports"
                        )


class ReportDiscoverabilityTest(unittest.TestCase):
    """Reports remain available via artifact and/or job summary."""

    def test_report_jobs_publish_artifact_or_step_summary(self):
        for workflow_file, job_name in REPORT_JOBS:
            with self.subTest(workflow=workflow_file, job=job_name):
                job = load_workflow(workflow_file)["jobs"][job_name]
                has_channel = job_has_upload_artifact(job) or job_writes_step_summary(job)
                # CI/CD already upload in generate-reports / generate-report;
                # the publish job itself must still surface summary or re-upload.
                self.assertTrue(
                    has_channel,
                    f"{workflow_file}:{job_name} must upload-artifact or write "
                    "$GITHUB_STEP_SUMMARY (no git commits)",
                )


class ReportLeastPrivilegeAndIgnoreTest(unittest.TestCase):
    """Ignore/untrack cicd-reports; drop contents:write on report jobs."""

    def test_report_jobs_do_not_request_contents_write(self):
        for workflow_file, job_name in REPORT_JOBS:
            with self.subTest(workflow=workflow_file, job=job_name):
                job = load_workflow(workflow_file)["jobs"][job_name]
                contents = job_contents_permission(job)
                self.assertNotEqual(
                    contents,
                    "write",
                    f"{workflow_file}:{job_name} must not set contents: write",
                )

    def test_cd_release_may_retain_contents_write(self):
        release = load_workflow("cd.yml")["jobs"]["release"]
        self.assertEqual(job_contents_permission(release), "write")

    def test_gitignore_contains_cicd_reports_path(self):
        with open(GITIGNORE_PATH, encoding="utf-8") as f:
            gitignore = f.read()
        self.assertIn(
            CICD_REPORTS_IGNORE,
            gitignore,
            ".gitignore must list docs/wiki/cicd-reports/",
        )

    def test_no_tracked_files_under_cicd_reports(self):
        tracked = git_tracked_under(CICD_REPORTS_PREFIX)
        self.assertEqual(
            tracked,
            [],
            f"expected no tracked files under {CICD_REPORTS_PREFIX}, found {len(tracked)}",
        )


if __name__ == "__main__":
    unittest.main()
