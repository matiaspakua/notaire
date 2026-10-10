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
  coverage-report job produces the report on non-PR events as a workflow
  artifact + $GITHUB_STEP_SUMMARY (never git-commits into docs/wiki/ —
  issue #1041), so the Playwright/Bruno coverage record is retained for main.

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see workspace/tests/test_report_job_needs_dependencies.py).
Run with: python3 workspace/tests/test_ci_workflow_invariants.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
WORKFLOWS_DIR = os.path.join(REPO_ROOT, ".github", "workflows")

CI_WORKFLOW = "ci.yml"
PAGE_WORKFLOW = "deploy-github-page.yml"
PLAYWRIGHT_WORKFLOW = "playwright-e2e.yml"
FRONTEND_WORKFLOW = "frontend-ci.yml"
OPENAPI_WORKFLOW = "openapi-contract.yml"

SUITE_AGGREGATORS = {
    CI_WORKFLOW: ("suite-ci", "CI"),
    FRONTEND_WORKFLOW: ("suite-frontend-ci", "Frontend CI"),
    PLAYWRIGHT_WORKFLOW: ("suite-playwright-e2e", "Playwright E2E"),
}

PATH_FILTER_WORKFLOWS = (
    CI_WORKFLOW,
    FRONTEND_WORKFLOW,
    PLAYWRIGHT_WORKFLOW,
    OPENAPI_WORKFLOW,
)


def _aggregator_run_script(workflow_file, job_id):
    wf = load_workflow(workflow_file)
    job = wf.get("jobs", {}).get(job_id, {})
    for step in job.get("steps", []) or []:
        run = step.get("run")
        if isinstance(run, str) and "result" in run:
            return run
    return ""


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



class PathScopedCiInvariantsTest(unittest.TestCase):
    """#1257 — path filters + aggregators accept intentional skips."""

    def test_path_filter_workflows_have_changes_job(self):
        for name in PATH_FILTER_WORKFLOWS:
            with self.subTest(workflow=name):
                jobs = load_workflow(name).get("jobs", {})
                self.assertIn("changes", jobs)
                self.assertEqual(jobs["changes"].get("name"), "Path filter")

    def test_suite_aggregator_names_unchanged(self):
        for workflow_file, (job_id, display_name) in SUITE_AGGREGATORS.items():
            with self.subTest(workflow=workflow_file):
                job = load_workflow(workflow_file).get("jobs", {}).get(job_id, {})
                self.assertEqual(job.get("name"), display_name)

    def test_suite_aggregators_accept_skipped(self):
        for workflow_file, (job_id, _) in SUITE_AGGREGATORS.items():
            with self.subTest(workflow=workflow_file):
                script = _aggregator_run_script(workflow_file, job_id)
                self.assertIn("skipped", script)
                self.assertIn("success|skipped", script.replace(" ", ""))

    def test_ci_build_gated_on_backend_or_ci_filter(self):
        build = load_workflow(CI_WORKFLOW).get("jobs", {}).get("build", {})
        job_if = build.get("if", "")
        self.assertIn("changes.outputs.backend", job_if)
        self.assertIn("changes.outputs.ci", job_if)

    def test_frontend_roots_gated_on_frontend_or_ci_filter(self):
        jobs = load_workflow(FRONTEND_WORKFLOW).get("jobs", {})
        for job_id in ("typecheck", "unit-tests"):
            with self.subTest(job=job_id):
                job_if = jobs[job_id].get("if", "")
                self.assertIn("changes.outputs.frontend", job_if)
                self.assertIn("changes.outputs.ci", job_if)

    def test_playwright_roots_gated_on_product_filter(self):
        jobs = load_workflow(PLAYWRIGHT_WORKFLOW).get("jobs", {})
        for job_id in ("backend-build", "frontend-build"):
            with self.subTest(job=job_id):
                job_if = jobs[job_id].get("if", "")
                self.assertIn("changes.outputs.product", job_if)

    def test_workflows_do_not_use_on_paths_that_starve_main(self):
        """Job-level if is OK; workflow-level on.paths would drop required checks on main."""
        for name in PATH_FILTER_WORKFLOWS:
            with self.subTest(workflow=name):
                on = load_workflow(name).get("on", {})
                for event in ("pull_request", "push"):
                    cfg = on.get(event)
                    if isinstance(cfg, dict):
                        self.assertNotIn("paths", cfg)




class PlaywrightShardInvariantsTest(unittest.TestCase):
    """#1258 — three-shard E2E matrix + fail-closed merge job."""

    def setUp(self):
        self.pw = load_workflow(PLAYWRIGHT_WORKFLOW)

    def test_e2e_tests_job_has_three_shards(self):
        job = self.pw.get("jobs", {}).get("e2e-tests", {})
        matrix = (job.get("strategy") or {}).get("matrix") or {}
        self.assertEqual(matrix.get("shard"), [1, 2, 3])

    def test_e2e_run_step_passes_shard_flag(self):
        steps = self.pw.get("jobs", {}).get("e2e-tests", {}).get("steps") or []
        run = next((s.get("run", "") for s in steps if "playwright test" in s.get("run", "")), "")
        self.assertIn("--shard=", run)

    def test_merge_reports_job_needs_e2e_tests(self):
        job = self.pw.get("jobs", {}).get("e2e-merge-reports", {})
        self.assertIsNotNone(job)
        needs = job.get("needs") or []
        self.assertIn("e2e-tests", needs)

    def test_merge_job_keeps_heavy_ci_check_name(self):
        """check-heavy-ci.sh looks for exact name UI E2E Tests (Playwright)."""
        job = self.pw.get("jobs", {}).get("e2e-merge-reports", {})
        self.assertEqual(job.get("name"), "UI E2E Tests (Playwright)")

    def test_suite_aggregator_needs_merge_not_raw_matrix(self):
        needs = self.pw.get("jobs", {}).get("suite-playwright-e2e", {}).get("needs") or []
        self.assertIn("e2e-merge-reports", needs)
        self.assertNotIn("e2e-tests", needs)

    def test_merge_job_fails_closed_when_shards_red(self):
        steps = self.pw.get("jobs", {}).get("e2e-merge-reports", {}).get("steps") or []
        script = "\n".join(s.get("run", "") for s in steps if isinstance(s.get("run"), str))
        self.assertIn("needs.e2e-tests.result", script)


if __name__ == "__main__":
    unittest.main()
