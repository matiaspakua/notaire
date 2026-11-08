"""Harness port: read adapters in place; never claim a sandbox (#2122)."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc.harness import describe, list_adapters  # noqa: E402
from aisdlc.profile import ProfileError, load_profile  # noqa: E402

FLEET = """
version: 1
runtime: cursor-cloud
excludes:
  - local-scratch/
foreman:
  id: cloud-foreman
  agent_file: agents/foreman.md
roles:
  planner:
    agent_file: agents/planner.md
  builder:
    agent_file: agents/builder.md
"""

LOCAL = """
project: demo
backend:
  profile: omlx
  agent: codex
paths:
  worktree: ../demo-worktree
"""

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[[harness.adapters]]
id = "cursor-cloud"
runtime = "cursor-cloud"
source = "fleet/manifest.yaml"

[[harness.adapters]]
id = "local-foreman"
runtime = "local-omlx"
source = "local/project.yml"
"""


class HarnessPortTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("fleet/manifest.yaml", FLEET)
        self.repo.write("local/project.yml", LOCAL)
        self.repo.commit("chore: harness fixtures")

    def test_list_reports_two_runtimes_and_never_a_sandbox(self):
        result = self.repo.run("harness", "list", "--json")
        self.assertEqual(0, result.returncode, output(result))
        rows = json.loads(result.stdout)
        self.assertEqual(["cursor-cloud", "local-foreman"], [row["id"] for row in rows])
        self.assertEqual(["cursor-cloud", "local-omlx"], [row["runtime"] for row in rows])
        for row in rows:
            self.assertIs(False, row["sandbox"])
            self.assertIs(False, row["commercial_harness_product"])
            self.assertTrue(row["readable"])

    def test_show_reads_fleet_roles_without_moving_the_file(self):
        result = self.repo.run("harness", "show", "--id", "cursor-cloud", "--json")
        self.assertEqual(0, result.returncode, output(result))
        view = json.loads(result.stdout)
        self.assertEqual("cursor-cloud", view["runtime"])
        self.assertEqual("cloud-foreman", view["foreman_id"])
        self.assertEqual(["builder", "planner"], sorted(view["roles"]))
        self.assertIs(False, view["sandbox"])
        self.assertTrue((self.repo.root / "fleet/manifest.yaml").is_file())

    def test_show_reads_local_foreman_as_a_different_adapter(self):
        result = self.repo.run("harness", "show", "--id", "local-foreman", "--json")
        self.assertEqual(0, result.returncode, output(result))
        view = json.loads(result.stdout)
        self.assertEqual("local-omlx", view["runtime"])
        self.assertEqual("codex", view["agent"])
        self.assertEqual("omlx", view["profile"])
        self.assertNotEqual(view["runtime"], "cursor-cloud")

    def test_missing_source_is_not_evaluable_as_success_on_show(self):
        (self.repo.root / "fleet/manifest.yaml").unlink()
        result = self.repo.run("harness", "show", "--id", "cursor-cloud")
        self.assertEqual(1, result.returncode)
        self.assertIn("missing", output(result).lower())

    def test_runtime_mismatch_fails_closed(self):
        self.repo.write("fleet/manifest.yaml", "version: 1\nruntime: other-cloud\n")
        result = self.repo.run("harness", "show", "--id", "cursor-cloud")
        self.assertEqual(1, result.returncode)
        self.assertIn("runtime", output(result).lower())

    def test_library_list_matches_cli(self):
        profile = load_profile(self.repo.root / "aisdlc.toml")
        rows = list_adapters(self.repo.root, profile)
        self.assertEqual(2, len(rows))
        described = describe(self.repo.root, profile, "cursor-cloud")
        self.assertEqual("cloud-foreman", described["foreman_id"])

    def test_unknown_id_is_usage_error(self):
        result = self.repo.run("harness", "show", "--id", "nope")
        self.assertEqual(2, result.returncode)

    def test_profile_rejects_escaping_source(self):
        with self.assertRaises(ProfileError):
            load_profile(
                text='[project]\nname="x"\n\n[[harness.adapters]]\nid="a"\nruntime="r"\nsource="../outside.yml"\n'
            )


if __name__ == "__main__":
    unittest.main()
