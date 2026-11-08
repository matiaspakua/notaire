"""Factory config, plugins and inventory. No network and no adopter paths."""
from __future__ import annotations

import os
import subprocess
import sys
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc.factory import (  # noqa: E402
    pr_cap_decision,
    redact,
    resolve_max_open_prs,
    sidecar_is_stale,
)
from aisdlc.profile import Profile, ProfileError  # noqa: E402

PROFILE = """
[factory]
max_open_prs = 3
concurrency = 1
timeout_seconds = 30
retry = 1
backoff_seconds = 0.5
inventory = ".aisdlc/migration-inventory.toml"

[plugins]
allow = ["openspec", "example-method"]

[[plugins.entries]]
id = "openspec"
kind = "method"
version = "1.0.0"
manifest = "plugins/openspec/plugin.toml"
trusted = true

[[plugins.entries]]
id = "example-method"
kind = "method"
version = "1.0.0"
manifest = "plugins/example-method/plugin.toml"
trusted = true

[[plugins.entries]]
id = "rejected"
kind = "method"
version = "1.0.0"
manifest = "plugins/rejected/plugin.toml"
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
source = "adapters/local"
destination = "tools/factory"
class = "harness-adapter"
status = "planned"
note = "not moved"

[[rows]]
source = "product/api.yaml"
destination = "product/api.yaml"
class = "stay"
status = "stay"
note = "product contract"
"""


class FactoryRulesTest(unittest.TestCase):
    def test_cap_cannot_be_raised_and_blocks_at_the_ceiling(self):
        profile = Profile(name="demo", max_open_prs=3)
        self.assertEqual(3, resolve_max_open_prs(profile, env={}))
        self.assertEqual(1, resolve_max_open_prs(profile, env={"AISDLC_MAX_OPEN_PRS": "1"}))
        self.assertEqual(2, resolve_max_open_prs(profile, env={"AISDLC_MAX_OPEN_PRS": "9"}, cli=2))
        with self.assertRaises(ProfileError):
            resolve_max_open_prs(profile, cli=4)
        self.assertEqual("allow", pr_cap_decision(2, 3))
        self.assertEqual("block", pr_cap_decision(3, 3))

    def test_redaction_keeps_env_references_and_hides_token_shapes(self):
        shown = redact("note", {"token": "ghp_abcdefghij", "tracker": "env:GH_TOKEN", "name": "demo"})
        self.assertEqual("***", shown["token"])
        self.assertEqual("env:GH_TOKEN", shown["tracker"])
        self.assertEqual("demo", shown["name"])

    def test_sidecar_is_stale_only_when_the_design_file_is_newer(self):
        root = Path(self.id().replace(".", "_"))
        # Use the temp repo so cleanup is automatic.
        repo = Repo(self)
        design = repo.write("DESIGN.md", "name: demo\n")
        sidecar = repo.write(".factory/design.json", "{}\n")
        os.utime(sidecar, (1_000, 1_000))
        os.utime(design, (2_000, 2_000))
        self.assertTrue(sidecar_is_stale(design, sidecar))
        os.utime(sidecar, (3_000, 3_000))
        self.assertFalse(sidecar_is_stale(design, sidecar))
        self.assertFalse(root.exists())


class FactoryCliTest(unittest.TestCase):
    def setUp(self):
        self.repo = Repo(self, PROFILE)
        for name in ("openspec", "example-method"):
            source = KIT / "plugins" / name / "plugin.toml"
            self.repo.write(f"plugins/{name}/plugin.toml", source.read_text(encoding="utf-8"))
        self.repo.write(
            "plugins/rejected/plugin.toml",
            'id = "rejected"\nkind = "method"\nversion = "1.0.0"\ncapabilities = ["plan"]\n',
        )
        self.repo.write(".aisdlc/migration-inventory.toml", INVENTORY)
        self.repo.commit("chore: factory profile")

    def test_validate_show_and_plugins_from_another_cwd(self):
        validated = subprocess.run(
            [sys.executable, str(KIT / "bin" / "aisdlc"), "--root", str(self.repo.root), "config", "validate"],
            cwd=Path("/tmp"),
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, validated.returncode, output(validated))
        shown = self.repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        self.assertIn('"max_open_prs": 3', shown.stdout)
        self.assertNotIn("ghp_", shown.stdout)
        listed = self.repo.run("plugin", "list")
        self.assertIn("openspec", listed.stdout)
        self.assertIn("example-method", listed.stdout)
        self.assertIn("available=False", listed.stdout)
        plan = self.repo.run("plugin", "plan", "--id", "openspec", "--var", "change=demo-change")
        self.assertEqual(0, plan.returncode, output(plan))
        self.assertEqual("openspec validate demo-change --strict\n", plan.stdout)
        inventory = self.repo.run("inventory", "check")
        self.assertEqual(0, inventory.returncode, output(inventory))

    def test_partial_plugin_failure_is_not_success(self):
        self.repo.write("plugins/openspec/plugin.toml", 'id = "wrong"\nkind = "method"\ncapabilities = ["plan"]\n')
        result = self.repo.run("config", "validate")
        self.assertEqual(1, result.returncode)
        self.assertIn("openspec", output(result))
        self.assertNotIn("factory config valid", output(result))

    def test_show_without_redacted_is_refused(self):
        result = self.repo.run("config", "show")
        self.assertEqual(2, result.returncode)

    def test_path_traversal_is_rejected_at_load(self):
        result = self.repo.run("config", "validate")
        # The loaded profile is valid. A traversal manifest cannot be loaded.
        bad = Repo(self, PROFILE.replace(
            'manifest = "plugins/openspec/plugin.toml"',
            'manifest = "../secrets/plugin.toml"',
        ))
        loaded = bad.run("config", "validate")
        self.assertEqual(2, loaded.returncode)
        self.assertIn("relative path", output(loaded))
        self.assertEqual(0, result.returncode)


if __name__ == "__main__":
    unittest.main()
