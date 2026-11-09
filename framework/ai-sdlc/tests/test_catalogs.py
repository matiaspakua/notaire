"""Catalogs CRUD for skills, MCP, agents, memory (#2139)."""
from __future__ import annotations

import json
import sys
import textwrap
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[skills]
dir = "skills"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class CatalogCrudTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        (self.repo.root / "skills" / "demo-skill").mkdir(parents=True)
        (self.repo.root / "skills" / "demo-skill" / "SKILL.md").write_text(
            "---\nname: demo-skill\ndescription: Demo\n---\n# Demo\n",
            encoding="utf-8",
        )

    def test_add_list_update_remove_and_validate(self):
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo-skill", "--summary", "Demo skill",
        )
        self.assertEqual(0, add.returncode, output(add))
        entry = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo-skill.json"
        self.assertTrue(entry.is_file())
        data = json.loads(entry.read_text(encoding="utf-8"))
        self.assertEqual("demo-skill", data["id"])
        self.assertIn("digest", data)
        digest = data["digest"]

        listed = self.repo.run("catalog", "list", "--kind", "skills", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        rows = json.loads(listed.stdout)
        self.assertEqual(1, len(rows["entries"]))
        self.assertEqual("demo-skill", rows["entries"][0]["id"])

        upd = self.repo.run(
            "catalog", "update", "--kind", "skills", "--id", "demo-skill",
            "--summary", "Updated summary",
        )
        self.assertEqual(0, upd.returncode, output(upd))
        data = json.loads(entry.read_text(encoding="utf-8"))
        self.assertEqual("Updated summary", data["summary"])
        self.assertNotEqual(digest, data["digest"])

        val = self.repo.run("catalog", "validate")
        self.assertEqual(0, val.returncode, output(val))

        rem = self.repo.run("catalog", "remove", "--kind", "skills", "--id", "demo-skill")
        self.assertEqual(0, rem.returncode, output(rem))
        self.assertFalse(entry.exists())

    def test_list_validate_never_execute_manifests(self):
        add = self.repo.run(
            "catalog", "add", "--kind", "mcp", "--id", "hook",
            "--ref", "mcp://unused", "--summary", "command=rm -rf / (must not run)",
        )
        self.assertEqual(0, add.returncode, output(add))
        listed = self.repo.run("catalog", "list", "--kind", "mcp", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        self.assertIn("hook", listed.stdout)
        # validate reads digests only — never interprets summary/command text as code
        val = self.repo.run("catalog", "validate", "--kind", "mcp")
        self.assertEqual(0, val.returncode, output(val))
        self.assertTrue((self.repo.root / ".aisdlc" / "catalogs" / "mcp" / "hook.json").is_file())

    def test_unknown_kind_rejected(self):
        result = self.repo.run("catalog", "list", "--kind", "widgets")
        self.assertEqual(2, result.returncode)

    def test_agents_and_memory_kinds(self):
        for kind, cid in (("agents", "foreman"), ("memory", "policy-pack")):
            add = self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}.md", "--summary", f"{kind} entry",
            )
            self.assertEqual(0, add.returncode, output(add))
        all_kinds = self.repo.run("catalog", "list", "--json")
        self.assertEqual(0, all_kinds.returncode, output(all_kinds))
        payload = json.loads(all_kinds.stdout)
        self.assertIn("agents", payload)
        self.assertIn("memory", payload)


class CatalogSkillsDiscoverTest(unittest.TestCase):
    def test_list_surfaces_skills_dir_when_catalog_json_empty(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            [skills]
            dir = ".claude/skills"
            [catalogs]
            dir = ".aisdlc/catalogs"
            """,
        )
        skill = repo.root / ".claude" / "skills" / "demo-skill"
        skill.mkdir(parents=True)
        (skill / "SKILL.md").write_text(
            "---\nname: demo-skill\ndescription: Demo skill for catalog discover\n---\n# Demo\n",
            encoding="utf-8",
        )
        listed = repo.run("catalog", "list", "--kind", "skills", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        data = json.loads(listed.stdout)
        ids = {e["id"] for e in data["entries"]}
        self.assertIn("demo-skill", ids)
        entry = next(e for e in data["entries"] if e["id"] == "demo-skill")
        self.assertEqual("skills-dir", entry.get("source"))
        # validate ignores virtual skills-dir rows (no digest file)
        self.assertEqual(0, repo.run("catalog", "validate", "--kind", "skills").returncode)


class CatalogNotaireSmokeTest(unittest.TestCase):
    def test_notaire_skills_remain_discoverable_via_catalog_list(self):
        repo = KIT.parents[1]
        profile = repo / ".aisdlc" / "aisdlc.toml"
        if not profile.is_file():
            self.skipTest("not Notaire checkout")
        result = __import__("subprocess").run(
            [sys.executable, str(KIT / "bin" / "aisdlc"), "--profile", str(profile), "catalog", "list", "--json"],
            cwd=repo,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        data = json.loads(result.stdout)
        self.assertIn("skills", data)
        # Notaire keeps skills under .claude/skills — list must surface them without seeding JSON.
        self.assertGreater(len(data["skills"]["entries"]), 0, result.stdout)


if __name__ == "__main__":
    unittest.main()
