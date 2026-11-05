#!/usr/bin/env python3
"""The public portfolio cadence matches workflow triggers (CU78, #2003).

Run with: python3 -m unittest security.tests.test_public_security_cadence
"""

from __future__ import annotations

import json
import unittest
from pathlib import Path

import yaml

REPO = Path(__file__).resolve().parents[2]
CADENCE = REPO / "github-page" / "lib" / "security-cadence.json"
CONTENT = REPO / "github-page" / "lib" / "security-content.ts"
WORKFLOWS = REPO / ".github" / "workflows"
SECRET_MARKERS = ("ghp_", "BEGIN PRIVATE KEY", "AKIA", "sk_live_")


def load_workflow(name: str) -> dict:
    data = yaml.safe_load((WORKFLOWS / name).read_text(encoding="utf-8"))
    if "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


class PublicCadenceTest(unittest.TestCase):
    def setUp(self):
        self.data = json.loads(CADENCE.read_text(encoding="utf-8"))

    def test_snapshot_is_dated_and_not_a_live_scan(self):
        self.assertEqual("2026-11-05", self.data["assessed"])
        self.assertIn("not a live scan", self.data["disclaimer"].lower())
        blob = CADENCE.read_text(encoding="utf-8") + CONTENT.read_text(encoding="utf-8")
        for marker in SECRET_MARKERS:
            self.assertNotIn(marker, blob)

    def test_triggers_match_the_workflow_files(self):
        seen = set()
        for entry in self.data["entries"]:
            seen.add(entry["id"])
            trigger = entry["trigger"]
            workflow = entry.get("workflow")
            if trigger == "platform":
                self.assertIsNone(workflow)
                continue
            if trigger == "local":
                self.assertTrue(entry.get("local"))
                self.assertIsNone(workflow)
                continue
            on = load_workflow(workflow).get("on", {})
            self.assertNotIn("pull_request", on, entry["id"])
            push = on.get("push") or {}
            tags = push.get("tags") or []
            branches = push.get("branches") or []
            if trigger == "release":
                self.assertIn("v*", tags, entry["id"])
                self.assertIn("notaire-v*", tags, entry["id"])
                self.assertIn("workflow_dispatch", on, entry["id"])
                self.assertNotIn("schedule", on, entry["id"])
            elif trigger == "release-schedule":
                self.assertIn("v*", tags, entry["id"])
                self.assertIn("notaire-v*", tags, entry["id"])
                self.assertTrue(on.get("schedule"), entry["id"])
                self.assertIn("workflow_dispatch", on, entry["id"])
            elif trigger == "schedule":
                self.assertTrue(on.get("schedule"), entry["id"])
                self.assertNotIn("tags", push, entry["id"])
                self.assertIn("workflow_dispatch", on, entry["id"])
            elif trigger == "main":
                self.assertIn("main", branches, entry["id"])
                self.assertIn("workflow_dispatch", on, entry["id"])
            else:
                self.fail(f"unknown trigger {trigger} on {entry['id']}")
        for required in ("secrets", "semgrep", "trivy-fs", "trivy-image", "codeql", "zap", "dast-baseline", "cosign"):
            self.assertIn(required, seen)


if __name__ == "__main__":
    unittest.main()
