#!/usr/bin/env python3
"""
Guards against a recurring GitHub Actions bug (issue #697): a job reads
`${{ needs.<job>.result }}` for a job that isn't listed in its own `needs:`
array. GitHub Actions only populates the `needs` context for jobs a job
directly depends on, even if that job is reachable transitively through
another dependency — so the expression silently resolves to an empty
string instead of the job's status.

Both `publish-report` (pr-validation.yml) and `generate-reports` (ci.yml)
hit this: they read `needs.validate-pr.result` / `needs.build.result` etc.
without depending on those jobs, so the `: "${VAR:?REQUIRED}"` guards in
scripts/generate-pr-validation-report.sh and scripts/generate-markdown-report.sh
failed on every run.

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see scripts/test_performance_test_assets.py).
Run with: python3 scripts/test_report_job_needs_dependencies.py
"""
import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WORKFLOWS_DIR = os.path.join(REPO_ROOT, ".github", "workflows")

NEEDS_REF_PATTERN = re.compile(r"needs\.([A-Za-z0-9_-]+)\.")


def load_jobs(workflow_file):
    with open(os.path.join(WORKFLOWS_DIR, workflow_file), encoding="utf-8") as f:
        workflow = yaml.safe_load(f)
    return workflow["jobs"]


def declared_needs(job):
    needs = job.get("needs", [])
    if isinstance(needs, str):
        needs = [needs]
    return set(needs)


def referenced_needs(job):
    return set(NEEDS_REF_PATTERN.findall(yaml.dump(job)))


class ReportJobNeedsDependenciesTest(unittest.TestCase):
    """Every needs.<job>.result reference must be backed by that job in needs:."""

    def assertNoDanglingNeedsReferences(self, workflow_file):
        jobs = load_jobs(workflow_file)
        for job_name, job in jobs.items():
            missing = referenced_needs(job) - declared_needs(job)
            self.assertFalse(
                missing,
                f"{workflow_file} job '{job_name}' reads needs.{{{','.join(sorted(missing))}}}.result "
                f"but does not list {sorted(missing)} in its own needs: {sorted(declared_needs(job))}",
            )

    def test_pr_validation_workflow_has_no_dangling_needs_references(self):
        self.assertNoDanglingNeedsReferences("pr-validation.yml")

    def test_ci_workflow_has_no_dangling_needs_references(self):
        self.assertNoDanglingNeedsReferences("ci.yml")

    def test_publish_report_depends_on_every_job_it_reports_on(self):
        jobs = load_jobs("pr-validation.yml")
        publish_report = jobs["publish-report"]
        self.assertEqual(
            declared_needs(publish_report),
            {"validate-pr", "quick-build", "dependency-analysis", "lint", "branch-protection", "generate-report"},
        )

    def test_generate_reports_depends_on_every_job_it_reports_on(self):
        jobs = load_jobs("ci.yml")
        generate_reports = jobs["generate-reports"]
        self.assertEqual(
            declared_needs(generate_reports),
            {"build", "unit-tests", "integration-tests", "coverage", "security", "docker-build", "quality"},
        )


if __name__ == "__main__":
    unittest.main()
