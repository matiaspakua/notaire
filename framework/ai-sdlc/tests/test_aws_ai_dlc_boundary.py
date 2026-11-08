"""AWS AI-DLC method boundary: design present, hooks not auto-trusted, unavailable (#2124)."""
from __future__ import annotations

import unittest

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
id = "aws-ai-dlc"
kind = "method"
version = "1.0.0"
manifest = "plugins/aws-ai-dlc/plugin.toml"
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


class AwsAiDlcBoundaryTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write(
            "plugins/openspec/plugin.toml",
            (KIT / "plugins/openspec/plugin.toml").read_text(encoding="utf-8"),
        )
        for name in ("plugin.toml", "DESIGN.md"):
            self.repo.write(
                f"plugins/aws-ai-dlc/{name}",
                (KIT / "plugins/aws-ai-dlc" / name).read_text(encoding="utf-8"),
            )
        self.repo.write(".aisdlc/migration-inventory.toml", INVENTORY)
        self.repo.commit("chore: aws-ai-dlc fixtures")

    def test_design_names_phase_ownership_and_forbids_second_orchestrator(self):
        design = (KIT / "plugins/aws-ai-dlc/DESIGN.md").read_text(encoding="utf-8")
        for needle in (
            "Initialization",
            "Ideation",
            "Inception",
            "Construction",
            "Operation",
            "owns zero",
            "auto-trust",
        ):
            self.assertIn(needle, design)
        manifest = (KIT / "plugins/aws-ai-dlc/plugin.toml").read_text(encoding="utf-8")
        self.assertIn("auto_trust_hooks = false", manifest)
        self.assertIn('status = "boundary"', manifest)
        self.assertFalse((KIT / "plugins/planned/aws-ai-dlc.toml").is_file())

    def test_config_validate_passes_while_unavailable(self):
        result = self.repo.run("config", "validate")
        self.assertEqual(0, result.returncode, output(result))

    def test_plugin_list_keeps_aws_ai_dlc_unavailable(self):
        result = self.repo.run("plugin", "list", "--json")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("aws-ai-dlc", result.stdout)
        self.assertIn('"available": false', result.stdout)
        self.assertIn("boundary", result.stdout)

    def test_plan_refused_while_out_of_allow(self):
        result = self.repo.run("plugin", "plan", "--id", "aws-ai-dlc")
        self.assertEqual(2, result.returncode)

    def test_auto_trust_hooks_true_fails_config_validate(self):
        self.repo.write(
            "plugins/aws-ai-dlc/plugin.toml",
            'id = "aws-ai-dlc"\nkind = "method"\nversion = "1.0.0"\n'
            'status = "boundary"\ncapabilities = ["boundary"]\nauto_trust_hooks = true\n',
        )
        result = self.repo.run("config", "validate")
        self.assertEqual(1, result.returncode)
        self.assertIn("auto_trust_hooks", output(result))


if __name__ == "__main__":
    unittest.main()
