"""Notaire adopts the harness port against real fleet and local adapter files (#2122, CU76)."""
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


class HarnessAdoptionTest(unittest.TestCase):
    def test_list_separates_cursor_cloud_from_local_foreman(self):
        result = run("harness", "list", "--json")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        rows = {row["id"]: row for row in json.loads(result.stdout)}
        self.assertEqual("cursor-cloud", rows["cursor-cloud"]["runtime"])
        self.assertEqual("local-omlx", rows["local-foreman"]["runtime"])
        self.assertTrue(rows["cursor-cloud"]["readable"])
        self.assertTrue(rows["local-foreman"]["readable"])
        for row in rows.values():
            self.assertIs(False, row["sandbox"])
            self.assertIs(False, row["commercial_harness_product"])

    def test_show_reads_fleet_manifest_in_place(self):
        result = run("harness", "show", "--id", "cursor-cloud", "--json")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        view = json.loads(result.stdout)
        self.assertEqual("cloud-foreman", view["foreman_id"])
        self.assertIn("openspec-planner", view["roles"])
        self.assertIs(False, view["sandbox"])
        self.assertTrue((ROOT / "docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml").is_file())

    def test_show_reads_local_project_yml_in_place(self):
        result = run("harness", "show", "--id", "local-foreman", "--json")
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        view = json.loads(result.stdout)
        self.assertEqual("local-omlx", view["runtime"])
        self.assertEqual("codex", view["agent"])
        self.assertEqual("omlx", view["profile"])
        self.assertNotEqual(view["runtime"], "cursor-cloud")

    def test_extra_stubs_surface_out_of_allow_with_sandbox_false(self):
        """#2188: Claude Code / Codex / ACP stubs are inventory-only (Spec Kit opt-in)."""
        listed = run("harness", "list", "--json")
        self.assertEqual(0, listed.returncode, listed.stdout + listed.stderr)
        rows = {row["id"]: row for row in json.loads(listed.stdout)}
        for stub_id, runtime in (
            ("claude-code", "claude-code"),
            ("openai-codex", "openai-codex"),
            ("agent-client-protocol", "agent-client-protocol"),
        ):
            self.assertEqual(runtime, rows[stub_id]["runtime"])
            self.assertIs(False, rows[stub_id]["sandbox"])
            self.assertIs(False, rows[stub_id]["commercial_harness_product"])
            self.assertTrue(rows[stub_id]["stub"])
            self.assertIs(False, rows[stub_id]["available"])
        plugins = run("plugin", "list", "--json")
        self.assertEqual(0, plugins.returncode, plugins.stdout + plugins.stderr)
        plugin_rows = {row["id"]: row for row in json.loads(plugins.stdout)}
        for stub_id in ("claude-code", "openai-codex", "agent-client-protocol"):
            self.assertEqual("harness", plugin_rows[stub_id]["kind"])
            self.assertIs(False, plugin_rows[stub_id]["available"])
        validate = run("config", "validate")
        self.assertEqual(0, validate.returncode, validate.stdout + validate.stderr)
        doctor = run("doctor")
        self.assertEqual(0, doctor.returncode, doctor.stdout + doctor.stderr)


if __name__ == "__main__":
    unittest.main()
