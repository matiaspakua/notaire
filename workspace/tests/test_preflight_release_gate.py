#!/usr/bin/env python3
"""#2493 (was #2001): the fast preflight is the pull-request gate; `--all` is the release gate.

GitHub does not re-run the product suites on a pull request, so the release is where every suite, the
server-backed ones included, must pass: `preflight.sh --all` before release-please is dispatched, then the tag CI.
"""

from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PREFLIGHT = ROOT / "workspace" / "sdlc" / "preflight.sh"
HOOK = ROOT / ".githooks" / "pre-push"
RELEASE = ROOT / "docs" / "300-development" / "RELEASE.md"


class PreflightReleaseGateTest(unittest.TestCase):
    def test_the_pre_push_hook_runs_the_pull_request_gate_by_default(self):
        text = HOOK.read_text(encoding="utf-8")
        self.assertIn("ARGS=(); MODE=pr", text)
        self.assertIn("PREFLIGHT_FULL", text)
        self.assertIn("PREFLIGHT_ALL", text)

    def test_preflight_names_the_release_gate(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("pull-request gate passed", text)
        self.assertIn("before a release: bash workspace/sdlc/preflight.sh --all", text)
        self.assertNotIn("not sufficient to open a pull request", text)

    def test_the_release_guide_requires_the_complete_preflight(self):
        self.assertIn("bash workspace/sdlc/preflight.sh --all", RELEASE.read_text(encoding="utf-8"))

    def test_preflight_records_its_own_pass_on_a_clean_tree(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("preflight-pass", text)
        self.assertIn("git status --porcelain", text)


if __name__ == "__main__":
    unittest.main()
