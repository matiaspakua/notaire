"""Notaire declares Spec Kit out of allow until dry plan is opted in (#2123, CU76)."""
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


class SpecKitAdoptionTest(unittest.TestCase):
    def test_config_validate_with_spec_kit_declared_out_of_allow(self):
        result = run("config", "validate")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_plugin_list_keeps_spec_kit_unavailable(self):
        result = run("plugin", "list", "--json")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        rows = {row["id"]: row for row in json.loads(result.stdout)}
        self.assertIn("github-spec-kit", rows)
        self.assertFalse(rows["github-spec-kit"]["available"])
        self.assertTrue(rows["openspec"]["available"])

    def test_dry_plan_blocked_until_allow(self):
        result = run("plugin", "plan", "--id", "github-spec-kit", "--var", "query=git")
        self.assertEqual(2, result.returncode)

    def test_called_api_record_exists(self):
        path = ROOT / "framework/ai-sdlc/plugins/spec-kit/CALLED-API.md"
        text = path.read_text(encoding="utf-8")
        self.assertIn("specify-cli==1.3.2", text)
        self.assertIn("specify extension search", text)
        self.assertIn("ExtensionCatalog.search", text)


if __name__ == "__main__":
    unittest.main()
