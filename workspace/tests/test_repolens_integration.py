#!/usr/bin/env python3
"""Guards #2298 (CU76): repolens is wired into every agent harness and into the AI SDLC factory profile, and
the committed wiring is exactly what `repolens setup` generates (no drift).

Run with: python3 -m unittest workspace.tests.test_repolens_integration
"""
import json
import os
import subprocess
import tomllib
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LAUNCHER = ROOT / "framework" / "repolens" / "bin" / "repolens"
LAUNCH = 'exec "$(git rev-parse --show-toplevel)/framework/repolens/bin/repolens" "$@"'
EXPECTED = ["bash", "-c", LAUNCH, "repolens", "mcp"]


def load_json(rel: str) -> dict:
    return json.loads((ROOT / rel).read_text(encoding="utf-8"))


class RepolensHarnessWiringTest(unittest.TestCase):
    def test_launcher_is_executable(self):
        self.assertTrue(os.access(LAUNCHER, os.X_OK))

    def test_every_harness_starts_the_same_server(self):
        claude = load_json(".mcp.json")["mcpServers"]["repolens"]
        cursor = load_json(".cursor/mcp.json")["mcpServers"]["repolens"]
        junie = load_json(".junie/mcp/mcp.json")["mcpServers"]["repolens"]
        opencode = load_json("local-ai/opencode/opencode.json")["mcp"]["repolens"]
        codex = tomllib.loads((ROOT / ".codex" / "config.toml").read_text(encoding="utf-8"))["mcp_servers"]["repolens"]
        for name, server in (("claude", claude), ("cursor", cursor), ("junie", junie), ("codex", codex)):
            self.assertEqual(EXPECTED, [server["command"], *server["args"]], name)
        self.assertEqual(EXPECTED, opencode["command"])
        self.assertTrue(opencode["enabled"])

    def test_claude_code_enables_the_server_and_records_usage(self):
        settings = load_json(".claude/settings.json")
        self.assertIn("repolens", settings["enabledMcpjsonServers"])
        self.assertIn("mcp__repolens", settings["permissions"]["allow"])
        hooks = [h for h in settings["hooks"]["PostToolUse"] if "repolens" in h["matcher"]]
        self.assertEqual(1, len(hooks))
        self.assertTrue(hooks[0]["hooks"][0]["command"].endswith("bin/repolens\" hook"))
        self.assertIn("Read", hooks[0]["matcher"])

    def test_git_hooks_refresh_in_the_background_and_never_fail(self):
        for name in ("post-checkout", "post-merge"):
            hook = ROOT / ".githooks" / name
            self.assertTrue(os.access(hook, os.X_OK), name)
            text = hook.read_text(encoding="utf-8")
            self.assertIn("refresh --background", text)
            self.assertIn("|| true", text)

    def test_committed_wiring_matches_the_generator(self):
        out = subprocess.run(["bash", str(LAUNCHER), "setup", "--dry-run"], cwd=ROOT, capture_output=True,
                             text=True, timeout=60)
        self.assertEqual(0, out.returncode, out.stderr)
        # Cursor Cloud remaps core.hooksPath to a host mirror under
        # ~/.cursor/agent-hooks/ that is not committed wiring. Drift there is
        # fixed with `repolens setup --only git` on the machine; it is not a
        # repository defect. Assert only on in-repo paths (.githooks is checked
        # separately above).
        drift = [
            line
            for line in out.stdout.splitlines()
            if "would update" in line and "/.cursor/agent-hooks/" not in line
        ]
        self.assertEqual([], drift, "run `bash framework/repolens/bin/repolens setup` and commit")


class RepolensFactoryProfileTest(unittest.TestCase):
    def setUp(self):
        self.profile = tomllib.loads((ROOT / ".aisdlc" / "aisdlc.toml").read_text(encoding="utf-8"))

    def test_navigating_stages_list_repolens(self):
        stages = {s["id"]: s for s in self.profile["stages"]}
        for stage in ("plan", "red", "pre-pr"):
            self.assertIn("repolens", stages[stage].get("mcp", []), stage)

    def test_catalog_entry_points_at_the_module(self):
        entry = load_json(".aisdlc/catalogs/mcp/repolens/current.json")
        self.assertEqual("framework/repolens", entry["ref"])
        self.assertTrue((ROOT / entry["ref"] / "MODULE.md").is_file())

    def test_usage_report_gate_is_advisory(self):
        gate = next(g for g in self.profile["gates"] if g["name"] == "repolens usage report")
        self.assertEqual("signal", gate["stage"])
        self.assertIs(False, gate["required"])
        self.assertIn("report --gate", gate["run"])

    def test_agents_are_told_and_the_store_is_not_committed(self):
        self.assertIn("repolens", (ROOT / "AGENTS.md").read_text(encoding="utf-8"))
        self.assertIn(".repolens/", (ROOT / ".gitignore").read_text(encoding="utf-8").splitlines())
        profile = tomllib.loads((ROOT / "repolens.toml").read_text(encoding="utf-8"))
        self.assertEqual(".", profile["source"][0]["path"])


if __name__ == "__main__":
    unittest.main()
