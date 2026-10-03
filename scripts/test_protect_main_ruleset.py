#!/usr/bin/env python3
"""
Guards issue #1040 / CU76+CU78: protect main with ruleset — PR-only, five
required check-run names, no force-push/delete, no durable bot bypass.

Invariants (in-repo artifacts; live GitHub assert is Gate 5):
- Desired-state JSON for ruleset protect-main (id 24128115) includes
  pull_request, required_status_checks for the five AC names, deletion,
  non_fast_forward, and empty bypass_actors.
- Workflow aggregator jobs exist with exact name: CI, Frontend CI,
  Playwright E2E, PR Validation; Code Lint remains the lint job name.
- .claude/rules/hooks.md no longer claims main is unprotected / 404.

Run with: python3 scripts/test_protect_main_ruleset.py
"""
from __future__ import annotations

import json
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WORKFLOWS_DIR = os.path.join(REPO_ROOT, ".github", "workflows")
DESIRED_JSON = os.path.join(REPO_ROOT, "scripts", "rulesets", "protect-main.desired.json")
HOOKS_MD = os.path.join(REPO_ROOT, ".claude", "rules", "hooks.md")

REQUIRED_CHECK_NAMES = (
    "CI",
    "Frontend CI",
    "Playwright E2E",
    "Code Lint",
    "PR Validation",
)

AGGREGATOR_SPECS = (
    ("ci.yml", "CI", ("build", "unit-tests", "integration-tests", "coverage", "quality", "security", "docker-build")),
    ("frontend-ci.yml", "Frontend CI", ("typecheck", "unit-tests", "build")),
    ("playwright-e2e.yml", "Playwright E2E", ("api-tests", "e2e-tests")),
    ("pr-validation.yml", "PR Validation", ("validate-pr", "lint", "sdlc-plan", "quick-build", "branch-naming", "dependency-analysis")),
)

GAP_PHRASES = (
    "not currently configured",
    "404 Branch not protected",
)


def load_workflow(workflow_file: str) -> dict:
    with open(os.path.join(WORKFLOWS_DIR, workflow_file), encoding="utf-8") as f:
        workflow = yaml.safe_load(f)
    # PyYAML (YAML 1.1) parses the top-level `on:` key as boolean True.
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def job_by_name(workflow: dict, display_name: str) -> tuple[str, dict] | None:
    for job_id, job in (workflow.get("jobs") or {}).items():
        if not isinstance(job, dict):
            continue
        if job.get("name") == display_name:
            return job_id, job
    return None


def as_needs_list(needs) -> list[str]:
    if needs is None:
        return []
    if isinstance(needs, str):
        return [needs]
    if isinstance(needs, list):
        return list(needs)
    return []


class ProtectMainDesiredStateTest(unittest.TestCase):
    """Desired ruleset payload matches #1040 AC (extend id 24128115)."""

    def setUp(self):
        self.assertTrue(
            os.path.isfile(DESIRED_JSON),
            f"missing desired-state file: {DESIRED_JSON}",
        )
        with open(DESIRED_JSON, encoding="utf-8") as f:
            self.desired = json.load(f)

    def test_targets_existing_protect_main_ruleset_id(self):
        self.assertEqual(self.desired.get("id"), 24128115)
        self.assertEqual(self.desired.get("name"), "protect-main")
        self.assertEqual(self.desired.get("enforcement"), "active")

    def test_targets_default_branch(self):
        include = (
            self.desired.get("conditions", {})
            .get("ref_name", {})
            .get("include", [])
        )
        self.assertIn("~DEFAULT_BRANCH", include)

    def test_rule_types_include_pr_checks_deletion_non_ff(self):
        types = {rule.get("type") for rule in self.desired.get("rules", [])}
        self.assertIn("pull_request", types)
        self.assertIn("required_status_checks", types)
        self.assertIn("deletion", types)
        self.assertIn("non_fast_forward", types)

    def test_required_status_checks_are_exact_ac_names(self):
        rsc = next(
            r for r in self.desired["rules"] if r.get("type") == "required_status_checks"
        )
        params = rsc.get("parameters") or {}
        checks = params.get("required_status_checks") or []
        contexts = sorted(c.get("context") for c in checks if isinstance(c, dict))
        self.assertEqual(contexts, sorted(REQUIRED_CHECK_NAMES))
        self.assertTrue(
            params.get("strict_required_status_checks_policy") is True
            or params.get("strict") is True,
            "expected strict required-status-checks policy",
        )

    def test_pull_request_rule_does_not_require_approving_reviews(self):
        pr = next(r for r in self.desired["rules"] if r.get("type") == "pull_request")
        params = pr.get("parameters") or {}
        self.assertEqual(params.get("required_approving_review_count", 0), 0)

    def test_bypass_actors_empty_after_1041(self):
        bypass = self.desired.get("bypass_actors")
        self.assertTrue(bypass in (None, []), f"bypass_actors must be empty, got {bypass!r}")


class AggregatorJobsTest(unittest.TestCase):
    """Thin suite aggregators publish the exact ruleset check-run names."""

    def test_code_lint_job_name_unchanged(self):
        workflow = load_workflow("pr-validation.yml")
        found = job_by_name(workflow, "Code Lint")
        self.assertIsNotNone(found, "Code Lint job must remain in pr-validation.yml")
        job_id, _ = found
        self.assertEqual(job_id, "lint")

    def test_aggregator_jobs_exist_with_exact_names_and_needs(self):
        for workflow_file, display_name, required_needs in AGGREGATOR_SPECS:
            with self.subTest(workflow=workflow_file, name=display_name):
                workflow = load_workflow(workflow_file)
                found = job_by_name(workflow, display_name)
                self.assertIsNotNone(
                    found,
                    f"{workflow_file} must define a job with name: {display_name!r}",
                )
                _, job = found
                needs = set(as_needs_list(job.get("needs")))
                missing = set(required_needs) - needs
                self.assertFalse(
                    missing,
                    f"{display_name} in {workflow_file} missing needs: {sorted(missing)}",
                )


class HooksDocumentationTest(unittest.TestCase):
    """hooks.md must describe protect-main, not the 2026-09-22 gap."""

    def setUp(self):
        with open(HOOKS_MD, encoding="utf-8") as f:
            self.text = f.read()

    def test_hooks_md_does_not_claim_unprotected_gap(self):
        for phrase in GAP_PHRASES:
            with self.subTest(phrase=phrase):
                self.assertNotIn(phrase, self.text)

    def test_hooks_md_references_protect_main_ruleset(self):
        self.assertIn("protect-main", self.text)
        lowered = self.text.lower()
        self.assertTrue(
            "defense in depth" in lowered or "defense-in-depth" in lowered,
            "hooks.md must describe the Claude push guard as defense-in-depth",
        )


if __name__ == "__main__":
    unittest.main()
