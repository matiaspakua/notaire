#!/usr/bin/env python3
"""
Guards the GitHub Actions workflow layout for issue #778:

Goal: (1) CI/quality/test workflows run on every PR and are mandatory on main;
(2) the GitHub Page is always deployed on every main merge, gated on CI
success; (3) Playwright E2E tests run on main and produce a report.

Invariants enforced here:
- ci.yml runs on every PR into main and every push to main, and is the single
  CI gate; it must NOT contain a deploy-pages job nor hold pages/id-token
  permissions (the page deploy lives only in deploy-github-page.yml).
- deploy-github-page.yml triggers on workflow_run of the CI workflow on main
  (completed) and on workflow_dispatch; its deploy job only runs after the CI
  workflow concludes successfully (or on manual dispatch), and it holds the
  pages/id-token permissions + github-pages environment.
- playwright-e2e.yml runs on PR/main push/schedule/manual and its
  coverage-report job produces the report on non-PR events (commits to
  docs/wiki/ on main pushes), so the Playwright/Bruno report is recorded for
  main.

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see scripts/test_report_job_needs_dependencies.py).
Run with: python3 scripts/test_ci_workflow_invariants.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WORKFLOWS_DIR = os.path.join(REPO_ROOT, ".github", "workflows")

CI_WORKFLOW = "ci.yml"
PAGE_WORKFLOW = "deploy-github-page.yml"
PLAYWRIGHT_WORKFLOW = "playwright-e2e.yml"

# The page deploy workflow must watch this CI workflow (its `name:`).
CI_WORKFLOW_NAME = "CI - Build, Test & Security"


def load_workflow(workflow_file):
    with open(os.path.join(WORKFLOWS_DIR, workflow_file), encoding="utf-8") as f:
        workflow = yaml.safe_load(f)
    # PyYAML (YAML 1.1) parses the top-level `on:` key as boolean True.
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def trigger_types(workflow):
    return set(workflow.get("on", {}).keys())


def branches_for(workflow, event_type):
    event = workflow.get("on", {}).get(event_type, {})
    if isinstance(event, str):
        return []
    if isinstance(event, list):
        return event
    return event.get("branches", [])


class CiWorkflowInvariantsTest(unittest.TestCase):
    """ci.yml runs on PR + main and is the single CI gate (no page deploy)."""

    def setUp(self):
        self.ci = load_workflow(CI_WORKFLOW)

    def test_ci_workflow_runs_on_pull_request_to_main(self):
        self.assertIn("main", branches_for(self.ci, "pull_request"))

    def test_ci_workflow_runs_on_push_to_main(self):
        self.assertIn("main", branches_for(self.ci, "push"))

    def test_ci_workflow_has_no_deploy_pages_job(self):
        self.assertNotIn("deploy-pages", self.ci.get("jobs", {}))

    def test_ci_workflow_does_not_hold_pages_permissions(self):
        permissions = self.ci.get("permissions", {})
        self.assertNotIn("pages", permissions)

    def test_ci_workflow_does_not_hold_id_token_permissions(self):
        permissions = self.ci.get("permissions", {})
        self.assertNotIn("id-token", permissions)

    def test_ci_workflow_has_quality_gate_jobs(self):
        jobs = self.ci.get("jobs", {})
        for job in ("build", "unit-tests", "integration-tests", "coverage", "security", "quality"):
            self.assertIn(job, jobs)


class PageDeployWorkflowInvariantsTest(unittest.TestCase):
    """deploy-github-page.yml auto-deploys on main after CI success."""

    def setUp(self):
        self.page = load_workflow(PAGE_WORKFLOW)

    def test_page_deploy_workflow_has_workflow_dispatch_trigger(self):
        self.assertIn("workflow_dispatch", trigger_types(self.page))

    def test_page_deploy_workflow_watches_ci_workflow_on_main(self):
        run = self.page.get("on", {}).get("workflow_run", {})
        self.assertIn(CI_WORKFLOW_NAME, run.get("workflows", []))
        self.assertIn("main", run.get("branches", []))
        self.assertIn("completed", run.get("types", []))

    def test_page_deploy_job_gated_on_ci_success(self):
        deploy = self.page.get("jobs", {}).get("deploy", {})
        self.assertIsNotNone(deploy)
        job_if = deploy.get("if", "")
        self.assertIn("workflow_run.conclusion", job_if)
        self.assertIn("success", job_if)

    def test_page_deploy_workflow_holds_pages_permissions(self):
        permissions = self.page.get("permissions", {})
        self.assertIn("pages", permissions)
        self.assertIn("id-token", permissions)

    def test_page_deploy_uses_github_pages_environment(self):
        deploy = self.page.get("jobs", {}).get("deploy", {})
        self.assertEqual(deploy.get("environment", {}).get("name"), "github-pages")


class PlaywrightWorkflowInvariantsTest(unittest.TestCase):
    """playwright-e2e.yml runs on PR/main and records the report for main."""

    def setUp(self):
        self.pw = load_workflow(PLAYWRIGHT_WORKFLOW)

    def test_playwright_runs_on_pull_request_to_main(self):
        self.assertIn("main", branches_for(self.pw, "pull_request"))

    def test_playwright_runs_on_push_to_main(self):
        self.assertIn("main", branches_for(self.pw, "push"))

    def test_playwright_has_coverage_report_job_skipping_pr(self):
        job = self.pw.get("jobs", {}).get("coverage-report")
        self.assertIsNotNone(job)
        self.assertIn("'pull_request'", job.get("if", ""))


if __name__ == "__main__":
    unittest.main()
