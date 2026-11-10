"""Extra HarnessPort stubs: Claude Code / Codex / ACP — inventory only (#2188)."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

STUB_IDS = ("claude-code", "openai-codex", "agent-client-protocol")

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[factory]
max_open_prs = 3
inventory = ".aisdlc/migration-inventory.toml"

[plugins]
allow = ["openspec"]

[[plugins.entries]]
id = "openspec"
kind = "method"
version = "1.0.0"
manifest = "plugins/openspec/plugin.toml"
trusted = true

[[plugins.entries]]
id = "claude-code"
kind = "harness"
version = "1.0.0"
manifest = "plugins/claude-code/plugin.toml"
trusted = false

[[plugins.entries]]
id = "openai-codex"
kind = "harness"
version = "1.0.0"
manifest = "plugins/openai-codex/plugin.toml"
trusted = false

[[plugins.entries]]
id = "agent-client-protocol"
kind = "harness"
version = "1.0.0"
manifest = "plugins/acp/plugin.toml"
trusted = false

[[harness.adapters]]
id = "cursor-cloud"
runtime = "cursor-cloud"
source = "fleet/manifest.yaml"

[[harness.adapters]]
id = "local-foreman"
runtime = "local-omlx"
source = "local/project.yml"
"""

INVENTORY = """
schema = 1
canonical_root = "tools/factory"

[[rows]]
source = "tools/factory"
destination = "tools/factory"
class = "core"
status = "migrated"
note = "canonical root"

[[rows]]
source = "plugins/planned/other.toml"
destination = "plugins/planned/other.toml"
class = "method-plugin"
status = "planned"
note = "still planned"
"""

FLEET = """
version: 1
runtime: cursor-cloud
foreman:
  id: cloud-foreman
roles:
  planner:
    agent_file: agents/planner.md
"""

LOCAL = """
project: demo
backend:
  profile: omlx
  agent: codex
"""


def _copy_stub_plugins(repo: Repo) -> None:
    for name in ("openspec", "claude-code", "openai-codex", "acp"):
        source = KIT / "plugins" / name
        if not source.is_dir():
            continue
        for path in source.iterdir():
            if path.is_file():
                repo.write(f"plugins/{name}/{path.name}", path.read_text(encoding="utf-8"))


class ExtraHarnessStubsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        _copy_stub_plugins(self.repo)
        self.repo.write("fleet/manifest.yaml", FLEET)
        self.repo.write("local/project.yml", LOCAL)
        self.repo.write(".aisdlc/migration-inventory.toml", INVENTORY)
        self.repo.commit("chore: extra harness stub fixtures")

    def test_kit_ships_three_harness_stub_manifests(self):
        for plugin_dir, plugin_id, runtime in (
            ("claude-code", "claude-code", "claude-code"),
            ("openai-codex", "openai-codex", "openai-codex"),
            ("acp", "agent-client-protocol", "agent-client-protocol"),
        ):
            text = (KIT / "plugins" / plugin_dir / "plugin.toml").read_text(encoding="utf-8")
            self.assertIn(f'id = "{plugin_id}"', text)
            self.assertIn('kind = "harness"', text)
            self.assertIn(f'runtime = "{runtime}"', text)
            adapter = KIT / "plugins" / plugin_dir / "adapter.yml"
            self.assertTrue(adapter.is_file(), adapter)
            body = adapter.read_text(encoding="utf-8")
            self.assertIn(f"runtime: {runtime}", body)
            self.assertIn("sandbox: false", body)

    def test_harness_list_surfaces_stubs_with_sandbox_false(self):
        result = self.repo.run("harness", "list", "--json")
        self.assertEqual(0, result.returncode, output(result))
        rows = {row["id"]: row for row in json.loads(result.stdout)}
        self.assertEqual("cursor-cloud", rows["cursor-cloud"]["runtime"])
        self.assertEqual("local-omlx", rows["local-foreman"]["runtime"])
        for stub_id in STUB_IDS:
            self.assertIn(stub_id, rows)
            self.assertIs(False, rows[stub_id]["sandbox"])
            self.assertIs(False, rows[stub_id]["commercial_harness_product"])
            self.assertTrue(rows[stub_id]["stub"])
            self.assertIs(False, rows[stub_id]["available"])
            self.assertTrue(rows[stub_id]["readable"])

    def test_plugin_list_shows_stubs_unavailable_until_allow(self):
        result = self.repo.run("plugin", "list", "--json")
        self.assertEqual(0, result.returncode, output(result))
        rows = {row["id"]: row for row in json.loads(result.stdout)}
        for stub_id in STUB_IDS:
            self.assertEqual("harness", rows[stub_id]["kind"])
            self.assertIs(False, rows[stub_id]["available"])

    def test_config_validate_passes_while_stubs_are_out_of_allow(self):
        result = self.repo.run("config", "validate")
        self.assertEqual(0, result.returncode, output(result))

    def test_show_reads_claude_code_adapter_in_place(self):
        result = self.repo.run("harness", "show", "--id", "claude-code", "--json")
        self.assertEqual(0, result.returncode, output(result))
        view = json.loads(result.stdout)
        self.assertEqual("claude-code", view["runtime"])
        self.assertIs(False, view["sandbox"])
        self.assertTrue((self.repo.root / "plugins/claude-code/adapter.yml").is_file())

    def test_dry_plan_refused_while_out_of_allow(self):
        result = self.repo.run("plugin", "plan", "--id", "claude-code")
        self.assertEqual(2, result.returncode)

    def test_dry_plan_prints_describe_argv_when_allowed(self):
        allowed = PROFILE.replace(
            'allow = ["openspec"]',
            'allow = ["openspec", "claude-code"]',
        ).replace(
            'id = "claude-code"\nkind = "harness"\nversion = "1.0.0"\nmanifest = "plugins/claude-code/plugin.toml"\ntrusted = false',
            'id = "claude-code"\nkind = "harness"\nversion = "1.0.0"\nmanifest = "plugins/claude-code/plugin.toml"\ntrusted = true',
            1,
        )
        repo = Repo(self, allowed)
        _copy_stub_plugins(repo)
        repo.write("fleet/manifest.yaml", FLEET)
        repo.write("local/project.yml", LOCAL)
        repo.write(".aisdlc/migration-inventory.toml", INVENTORY)
        repo.commit("chore: allow claude-code stub")
        result = repo.run("plugin", "plan", "--id", "claude-code")
        self.assertEqual(0, result.returncode, output(result))
        self.assertEqual("aisdlc-harness-describe claude-code\n", result.stdout)
        listed = repo.run("harness", "list", "--json")
        rows = {row["id"]: row for row in json.loads(listed.stdout)}
        self.assertIs(True, rows["claude-code"]["available"])
        self.assertIs(False, rows["claude-code"]["sandbox"])


if __name__ == "__main__":
    unittest.main()
