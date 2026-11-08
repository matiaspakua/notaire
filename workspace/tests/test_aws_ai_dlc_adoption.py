"""Notaire keeps AWS AI-DLC as an unavailable boundary plugin (#2124, CU76)."""
from __future__ import annotations

import json
import subprocess
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
AISDLC = [sys.executable, "framework/ai-sdlc/bin/aisdlc", "--profile", ".aisdlc/aisdlc.toml"]


def run(*args: str) -> subprocess.CompletedProcess:
    return subprocess.run([*AISDLC, *args], cwd=ROOT, capture_output=True, text=True)


class AwsAiDlcAdoptionTest(unittest.TestCase):
    def test_config_validate_and_unavailable(self):
        self.assertEqual(0, run("config", "validate").returncode)
        listed = run("plugin", "list", "--json")
        self.assertEqual(0, listed.returncode, listed.stdout + listed.stderr)
        rows = {row["id"]: row for row in json.loads(listed.stdout)}
        self.assertIn("aws-ai-dlc", rows)
        self.assertFalse(rows["aws-ai-dlc"]["available"])
        self.assertEqual("boundary", rows["aws-ai-dlc"]["status"])

    def test_design_and_manifest_forbid_auto_trust(self):
        design = (ROOT / "framework/ai-sdlc/plugins/aws-ai-dlc/DESIGN.md").read_text(encoding="utf-8")
        self.assertIn("owns zero", design)
        self.assertIn("Construction", design)
        manifest = (ROOT / "framework/ai-sdlc/plugins/aws-ai-dlc/plugin.toml").read_text(encoding="utf-8")
        self.assertIn("auto_trust_hooks = false", manifest)

    def test_plan_blocked(self):
        self.assertEqual(2, run("plugin", "plan", "--id", "aws-ai-dlc").returncode)


if __name__ == "__main__":
    unittest.main()
