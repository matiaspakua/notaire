#!/usr/bin/env python3
"""
Guards issue #1042 (CU76): CD must publish the CI-tested SHA, not tip of main.

Under workflow_run, github.sha is the tip of the default branch at CD schedule
time — not workflow_run.head_sha. This suite asserts:

1. build-and-publish checkout pins workflow_run.head_sha (fallback github.sha)
2. An explicit publish SHA step wires tags to that SHA (not tip type=sha alone)
3. Immutable SHA-tagged image is pushed before latest is moved to that digest
4. build-and-publish skips when workflow_run.conclusion != success

Run with: python3 scripts/test_cd_pin_tested_sha.py
"""

from __future__ import annotations

import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CD_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "cd.yml")

SUCCESS_IF = (
    "github.event_name != 'workflow_run' || "
    "github.event.workflow_run.conclusion == 'success'"
)


def load_workflow(path: str) -> dict:
    with open(path, encoding="utf-8") as f:
        workflow = yaml.safe_load(f)
    # PyYAML (YAML 1.1) parses the top-level `on:` key as boolean True.
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def job_steps(workflow: dict, job_id: str) -> list:
    job = workflow.get("jobs", {}).get(job_id, {})
    return job.get("steps", []) or []


def step_name(step: dict) -> str:
    return (step.get("name") or "").strip()


def checkout_steps(steps: list) -> list:
    return [
        s
        for s in steps
        if isinstance(s, dict) and str(s.get("uses", "")).startswith("actions/checkout")
    ]


def build_push_steps(steps: list) -> list:
    return [
        s
        for s in steps
        if isinstance(s, dict)
        and str(s.get("uses", "")).startswith("docker/build-push-action")
    ]


def metadata_steps(steps: list) -> list:
    return [
        s
        for s in steps
        if isinstance(s, dict)
        and str(s.get("uses", "")).startswith("docker/metadata-action")
    ]


def step_index(steps: list, predicate) -> int:
    for i, step in enumerate(steps):
        if predicate(step):
            return i
    return -1


class CdPinTestedShaTest(unittest.TestCase):
    """cd.yml build-and-publish pins CI-tested SHA and orders latest after SHA."""

    def setUp(self):
        self.assertTrue(os.path.isfile(CD_WORKFLOW), CD_WORKFLOW)
        self.cd = load_workflow(CD_WORKFLOW)
        self.job = self.cd.get("jobs", {}).get("build-and-publish", {})
        self.steps = job_steps(self.cd, "build-and-publish")
        self.assertTrue(self.job, "missing build-and-publish job")
        self.assertTrue(self.steps, "build-and-publish has no steps")

    def test_skips_publish_when_ci_not_success(self):
        condition = " ".join(str(self.job.get("if", "")).split())
        self.assertIn(
            "github.event.workflow_run.conclusion == 'success'",
            condition,
            "build-and-publish must gate on workflow_run conclusion == success",
        )
        self.assertIn(
            "github.event_name != 'workflow_run'",
            condition,
            "build-and-publish must still allow tag/dispatch without workflow_run",
        )
        # Normalize spaces for exact contract match from #1042 AC / design.md
        self.assertEqual(condition, SUCCESS_IF)

    def test_checkout_pins_workflow_run_head_sha(self):
        checkouts = checkout_steps(self.steps)
        self.assertTrue(checkouts, "build-and-publish must checkout the repo")
        checkout = checkouts[0]
        ref = str((checkout.get("with") or {}).get("ref", ""))
        self.assertIn(
            "workflow_run.head_sha",
            ref,
            "checkout with.ref must resolve workflow_run.head_sha on workflow_run",
        )
        self.assertIn(
            "github.sha",
            ref,
            "checkout with.ref must fall back to github.sha for tag/dispatch",
        )

    def test_resolve_publish_sha_step_uses_head_sha_on_workflow_run(self):
        idx = step_index(
            self.steps,
            lambda s: "publish" in step_name(s).lower()
            and "sha" in step_name(s).lower(),
        )
        self.assertGreaterEqual(
            idx,
            0,
            "missing Resolve publish SHA (or equivalent) step before metadata/push",
        )
        step = self.steps[idx]
        run = str(step.get("run", ""))
        self.assertIn("workflow_run", run)
        self.assertIn("head_sha", run)
        self.assertIn("github.sha", run)
        step_id = step.get("id")
        self.assertTrue(step_id, "publish SHA step must set an id for outputs")

        # Must appear before the first immutable build-push
        build_idx = step_index(
            self.steps,
            lambda s: isinstance(s, dict)
            and str(s.get("uses", "")).startswith("docker/build-push-action"),
        )
        self.assertGreater(build_idx, idx, "publish SHA must be resolved before push")

    def test_immutable_tags_include_explicit_publish_sha(self):
        # Prefer metadata tags that reference publish_sha outputs; also accept
        # a raw tag expression on the first build-push that embeds that output.
        meta = metadata_steps(self.steps)
        self.assertTrue(meta, "expected docker/metadata-action for image tags")

        first_meta = meta[0]
        tags = str((first_meta.get("with") or {}).get("tags", ""))
        # First metadata block must NOT move latest (that is a later step).
        self.assertNotIn(
            "value=latest",
            tags,
            "immutable metadata must omit type=raw latest; latest is a later step",
        )
        self.assertTrue(
            "publish_sha" in tags or "steps.publish_sha" in tags or "raw,value=" in tags,
            "immutable tags must include an explicit raw/publish SHA (not tip type=sha alone)",
        )
        # Explicitly require publish_sha wiring so tip github.sha type=sha cannot hide the bug.
        self.assertIn(
            "publish_sha",
            tags,
            "immutable tags must reference steps.publish_sha (or id publish_sha) outputs",
        )

    def test_latest_is_gated_after_sha_tagged_push(self):
        builds = build_push_steps(self.steps)
        self.assertTrue(builds, "expected docker/build-push-action push step(s)")

        first_build = builds[0]
        first_tags = str((first_build.get("with") or {}).get("tags", ""))
        # First push must not apply latest via its tags input.
        self.assertNotRegex(
            first_tags,
            r"(^|[\s,/:])latest($|[\s,]|@)",
            "first image push must not include the mutable latest tag",
        )
        self.assertNotIn("value=latest", first_tags)

        first_build_idx = step_index(
            self.steps,
            lambda s: s is first_build,
        )

        # A later step must apply latest (metadata, build-push, or imagetools).
        later = self.steps[first_build_idx + 1 :]
        latest_idx = step_index(
            later,
            lambda s: "latest" in step_name(s).lower()
            or "latest" in str((s.get("with") or {}).get("tags", "")).lower()
            or (
                "imagetools" in str(s.get("run", "")).lower()
                and "latest" in str(s.get("run", "")).lower()
            )
            or (
                "latest" in str(s.get("run", "")).lower()
                and (
                    "docker" in str(s.get("run", "")).lower()
                    or "tag" in str(s.get("run", "")).lower()
                )
            ),
        )
        self.assertGreaterEqual(
            latest_idx,
            0,
            "after SHA push, a later step must tag/push latest to the same digest",
        )

        # If a second metadata-action exists for latest, it must come after first build.
        if len(meta := metadata_steps(self.steps)) >= 2:
            second_meta_idx = step_index(self.steps, lambda s: s is meta[1])
            self.assertGreater(
                second_meta_idx,
                first_build_idx,
                "latest metadata must come after the immutable SHA push",
            )


if __name__ == "__main__":
    unittest.main()
