"""Spec Kit method plugin: dry plan argv; stays unavailable until allow (#2123)."""
from __future__ import annotations

import unittest
from pathlib import Path

from helpers import KIT, Repo, output

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
id = "github-spec-kit"
kind = "method"
version = "1.0.0"
manifest = "plugins/spec-kit/plugin.toml"
trusted = false
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


class SpecKitPluginTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for name in ("openspec", "spec-kit"):
            source = KIT / "plugins" / name / "plugin.toml"
            self.repo.write(f"plugins/{name}/plugin.toml", source.read_text(encoding="utf-8"))
        self.repo.write(".aisdlc/migration-inventory.toml", INVENTORY)
        self.repo.commit("chore: spec-kit plugin fixtures")

    def test_manifest_records_pin_and_extensions_api(self):
        text = (KIT / "plugins" / "spec-kit" / "plugin.toml").read_text(encoding="utf-8")
        self.assertIn('id = "github-spec-kit"', text)
        self.assertIn("specify-cli==1.3.2", text)
        self.assertIn("specify extension search", text)
        called = (KIT / "plugins" / "spec-kit" / "CALLED-API.md").read_text(encoding="utf-8")
        self.assertIn("1.3.2", called)
        self.assertIn("ExtensionCatalog.search", called)
        self.assertNotIn("API is absent", called.lower())

    def test_config_validate_passes_while_spec_kit_is_out_of_allow(self):
        result = self.repo.run("config", "validate")
        self.assertEqual(0, result.returncode, output(result))

    def test_plugin_list_shows_spec_kit_unavailable_until_allow(self):
        result = self.repo.run("plugin", "list", "--json")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("github-spec-kit", result.stdout)
        self.assertIn('"available": false', result.stdout)

    def test_dry_plan_refused_while_out_of_allow(self):
        result = self.repo.run("plugin", "plan", "--id", "github-spec-kit", "--var", "query=git")
        self.assertEqual(2, result.returncode)

    def test_dry_plan_prints_extension_search_when_allowed(self):
        allowed = PROFILE.replace('allow = ["openspec"]', 'allow = ["openspec", "github-spec-kit"]').replace(
            "trusted = false", "trusted = true"
        )
        repo = Repo(self, allowed)
        for name in ("openspec", "spec-kit"):
            source = KIT / "plugins" / name / "plugin.toml"
            repo.write(f"plugins/{name}/plugin.toml", source.read_text(encoding="utf-8"))
        repo.write(".aisdlc/migration-inventory.toml", INVENTORY)
        repo.commit("chore: allow spec-kit")
        result = repo.run("plugin", "plan", "--id", "github-spec-kit", "--var", "query=git")
        self.assertEqual(0, result.returncode, output(result))
        self.assertEqual("specify extension search git\n", result.stdout)
        listed = repo.run("plugin", "list")
        self.assertIn("github-spec-kit", listed.stdout)
        self.assertIn("available=True", listed.stdout)

    def test_planned_stub_is_gone(self):
        self.assertFalse((KIT / "plugins" / "planned" / "spec-kit.toml").is_file())
        self.assertTrue((KIT / "plugins" / "spec-kit" / "plugin.toml").is_file())


if __name__ == "__main__":
    unittest.main()
