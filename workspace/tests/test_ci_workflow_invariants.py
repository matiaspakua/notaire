#!/usr/bin/env python3
"""
Guards the GitHub Actions workflow layout (#778, retargeted by #2001):

Goal: (1) product CI/quality/test workflows run on a version tag (v* /
notaire-v*) and on workflow_dispatch, not on pull_request or branch push;
(2) the GitHub Page deploys on every push to main (and manual dispatch),
without waiting for CI; (3) Playwright E2E has no weekday cron and still
records its report as a workflow artifact on a non-PR event.

Day-to-day proof is bash workspace/sdlc/preflight.sh --full. There is no
GitHub re-run of that suite on a pull request.

Plain stdlib unittest. Run with:
python3 workspace/tests/test_ci_workflow_invariants.py
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


RELEASE_TAGS = ("v*", "notaire-v*")

# Heavy product workflows: tags + manual dispatch only (#2001).
RELEASE_ONLY_WORKFLOWS = (
    CI_WORKFLOW,
    FRONTEND_WORKFLOW,
    PLAYWRIGHT_WORKFLOW,
    OPENAPI_WORKFLOW,
    "pr-validation.yml",
    "pages-ci.yml",
    "database-vv.yml",
    "sdlc-process.yml",
    "test-coverage-report.yml",
)


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


def assert_release_only(test, workflow):
    on = workflow.get("on", {})
    test.assertNotIn("pull_request", on)
    test.assertNotIn("workflow_run", on)
    test.assertNotIn("schedule", on)
    test.assertIn("workflow_dispatch", on)
    push = on.get("push")
    test.assertIsInstance(push, dict)
    test.assertNotIn("branches", push)
    test.assertNotIn("paths", push)
    tags = push.get("tags") or []
    for tag in RELEASE_TAGS:
        test.assertIn(tag, tags)


class CiWorkflowInvariantsTest(unittest.TestCase):
    """ci.yml is the CI gate on a version tag (no page deploy)."""

    def setUp(self):
        self.ci = load_workflow(CI_WORKFLOW)

    def test_ci_workflow_is_release_only(self):
        assert_release_only(self, self.ci)

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
    """deploy-github-page.yml deploys on push to main, not after CI."""

    def setUp(self):
        self.page = load_workflow(PAGE_WORKFLOW)

    def test_page_deploy_workflow_has_workflow_dispatch_trigger(self):
        self.assertIn("workflow_dispatch", trigger_types(self.page))

    def test_page_deploy_runs_on_push_to_main_without_ci_workflow_run(self):
        on = self.page.get("on", {})
        self.assertNotIn("workflow_run", on)
        self.assertIn("main", branches_for(self.page, "push"))

    def test_page_deploy_job_is_not_gated_on_ci_workflow_run(self):
        deploy = self.page.get("jobs", {}).get("deploy", {})
        self.assertIsNotNone(deploy)
        job_if = deploy.get("if") or ""
        self.assertNotIn("workflow_run", job_if)

    def test_page_deploy_workflow_holds_pages_permissions(self):
        permissions = self.page.get("permissions", {})
        self.assertIn("pages", permissions)
        self.assertIn("id-token", permissions)

    def test_page_deploy_uses_github_pages_environment(self):
        deploy = self.page.get("jobs", {}).get("deploy", {})
        self.assertEqual(deploy.get("environment", {}).get("name"), "github-pages")


class PlaywrightWorkflowInvariantsTest(unittest.TestCase):
    """playwright-e2e.yml runs on a version tag and records the report."""

    def setUp(self):
        self.pw = load_workflow(PLAYWRIGHT_WORKFLOW)

    def test_playwright_is_release_only_without_weekday_cron(self):
        assert_release_only(self, self.pw)
        raw_path = os.path.join(WORKFLOWS_DIR, PLAYWRIGHT_WORKFLOW)
        with open(raw_path, encoding="utf-8") as handle:
            raw = handle.read()
        self.assertNotIn("0 6 * * 1-5", raw)

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

    def test_workflows_do_not_use_on_paths_that_suppress_tags(self):
        """Tag pushes ignore paths filters; a paths key would skip the release run."""
        for name in PATH_FILTER_WORKFLOWS:
            with self.subTest(workflow=name):
                on = load_workflow(name).get("on", {})
                self.assertNotIn("pull_request", on)
                push = on.get("push")
                if isinstance(push, dict):
                    self.assertNotIn("paths", push)


class ReleaseOnlyWorkflowTriggersTest(unittest.TestCase):
    """#2001 — product CI is tag + workflow_dispatch, CodeQL keeps Monday."""

    def test_heavy_workflows_are_release_only(self):
        for name in RELEASE_ONLY_WORKFLOWS:
            with self.subTest(workflow=name):
                assert_release_only(self, load_workflow(name))

    def test_codeql_keeps_monday_and_drops_pull_request(self):
        codeql = load_workflow("codeql.yml")
        on = codeql.get("on", {})
        self.assertNotIn("pull_request", on)
        self.assertIn("workflow_dispatch", on)
        push = on.get("push") or {}
        self.assertNotIn("branches", push)
        for tag in RELEASE_TAGS:
            self.assertIn(tag, push.get("tags") or [])
        crons = [item.get("cron") for item in (on.get("schedule") or [])]
        self.assertIn("30 6 * * 1", crons)

    def test_release_please_still_runs_on_main(self):
        release = load_workflow("release-please.yml")
        self.assertIn("main", branches_for(release, "push"))
        self.assertIn("workflow_dispatch", trigger_types(release))

    def test_operational_schedules_remain(self):
        for name, cron in (
            ("cleanup-actions.yml", None),
            ("dast-zap.yml", None),
            ("performance-test.yml", None),
            ("backup-restore-smoke.yml", None),
        ):
            with self.subTest(workflow=name):
                on = load_workflow(name).get("on", {})
                self.assertIn("schedule", on)
                self.assertNotIn("pull_request", on)




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
