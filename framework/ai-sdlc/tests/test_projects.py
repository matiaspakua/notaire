"""Project create/list/show/add-repo with dry_run default (#2255)."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class ProjectsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")

    def test_project_create_defaults_dry_run(self):
        created = self.repo.run(
            "project", "create", "--id", "alpha",
            "--profile-ref", "aisdlc.toml",
            "--repo-id", "notaire",
            "--forge", "github",
            "--url", "https://github.com/example/notaire",
            "--default-branch", "main",
            "--json",
        )
        self.assertEqual(0, created.returncode, output(created))
        payload = json.loads(created.stdout)
        self.assertEqual("alpha", payload["id"])
        self.assertEqual("aisdlc.toml", payload["profile_ref"])
        self.assertEqual(1, len(payload["repos"]))
        self.assertEqual("dry_run", payload["repos"][0]["mode"])

        shown = self.repo.run("project", "show", "--id", "alpha", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        self.assertEqual("dry_run", json.loads(shown.stdout)["repos"][0]["mode"])

        listed = self.repo.run("project", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        ids = [row["id"] for row in json.loads(listed.stdout)["entries"]]
        self.assertIn("alpha", ids)

    def test_add_repo_defaults_dry_run(self):
        self.repo.run(
            "project", "create", "--id", "beta",
            "--profile-ref", "aisdlc.toml", "--json",
        )
        added = self.repo.run(
            "project", "add-repo", "--id", "beta",
            "--repo-id", "kit",
            "--forge", "github",
            "--url", "https://github.com/example/kit",
            "--default-branch", "main",
            "--json",
        )
        self.assertEqual(0, added.returncode, output(added))
        repos = json.loads(added.stdout)["repos"]
        self.assertEqual("dry_run", repos[-1]["mode"])


if __name__ == "__main__":
    unittest.main()
