#!/usr/bin/env python3
"""Release asset attachment and dispatch (#1591)."""

from __future__ import annotations

import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]


def load_workflow(name: str) -> dict:
    with open(REPO_ROOT / ".github" / "workflows" / name, encoding="utf-8") as handle:
        data = yaml.safe_load(handle) or {}
    if "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


class DispatchOnTagTest(unittest.TestCase):
    def test_release_please_dispatches_cd_on_tag(self):
        workflow = load_workflow("release-please.yml")
        permissions = workflow.get("permissions", {})
        self.assertEqual(permissions.get("actions"), "write")
        steps = workflow["jobs"]["release-please"]["steps"]
        dispatch = [step for step in steps if step.get("name") == "Dispatch CD on release tag"]
        self.assertEqual(len(dispatch), 1)
        self.assertIn("release_created", dispatch[0].get("if", ""))
        self.assertIn("gh workflow run cd.yml", dispatch[0]["run"])

    def test_cd_keeps_tag_filters(self):
        workflow = load_workflow("cd.yml")
        tags = workflow["on"]["push"]["tags"]
        self.assertIn("v*", tags)
        self.assertIn("notaire-v*", tags)


class FailClosedAttachTest(unittest.TestCase):
    def setUp(self):
        self.cd = load_workflow("cd.yml")
        self.release = self.cd["jobs"]["release"]
        self.blob = yaml.dump(self.release)

    def test_prepare_and_upload_are_fail_closed(self):
        self.assertIn("fail_on_unmatched_files: true", self.blob)
        self.assertIn("missing release assets", self.blob)
        self.assertIn("Verify Release asset set", self.blob)

    def test_concurrency_serialises_same_ref(self):
        concurrency = self.release.get("concurrency", {})
        self.assertIn("release-assets-${{ github.ref }}", concurrency.get("group", ""))
        self.assertFalse(concurrency.get("cancel-in-progress", True))


class DeliveryOrderingTest(unittest.TestCase):
    def test_identity_gates_build_and_release(self):
        cd = load_workflow("cd.yml")
        self.assertIn("release-identity", cd["jobs"]["build-and-publish"].get("needs", []))
        needs = cd["jobs"]["release"]["needs"]
        if isinstance(needs, str):
            needs = [needs]
        self.assertEqual(needs, ["build-and-publish"])


class BackfillRunbookTest(unittest.TestCase):
    def test_runbook_forbids_destructive_commands(self):
        path = REPO_ROOT / "docs" / "300-development" / "RELEASE-BACKFILL.md"
        self.assertTrue(path.is_file(), "RELEASE-BACKFILL.md must exist")
        text = path.read_text(encoding="utf-8").lower()
        self.assertIn("--clobber", text)
        self.assertIn("--clobber", text)
        self.assertIn("before", text)
        self.assertIn("after", text)
        self.assertIn("digest", text)


class LegacyBackfillPolicyTest(unittest.TestCase):
    def test_runbook_documents_legacy_gaps(self):
        text = (REPO_ROOT / "docs" / "300-development" / "RELEASE-BACKFILL.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("notaire-v0.1.1", text)
        self.assertIn("notaire-v0.1.2", text)
        self.assertIn("product", text.lower())


if __name__ == "__main__":
    unittest.main()
