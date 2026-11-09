"""Per-stage profile knobs: agents/skills/MCP/model/effort (#2182)."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc.profile import ProfileError, load_profile  # noqa: E402

BASE = """
[project]
name = "demo"
policy = "POLICY.md"

[[gates]]
name = "plan check"
stage = "plan"
run = "true"

[[gates]]
name = "push check"
stage = "pre-push"
run = "true"

[skills]
dir = "skills"

[catalogs]
dir = ".aisdlc/catalogs"
"""


def _skill(repo: Repo, name: str = "openspec-propose") -> None:
    folder = repo.root / "skills" / name
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "SKILL.md").write_text(
        f"---\nname: {name}\ndescription: Demo skill\n---\n# {name}\n",
        encoding="utf-8",
    )


class StageProfilesTest(unittest.TestCase):
    def _with_skills_dir(self, repo: Repo) -> None:
        (repo.root / "skills").mkdir(exist_ok=True)

    def test_omitted_stage_knobs_are_noop(self):
        repo = Repo(self, BASE)
        self._with_skills_dir(repo)
        validate = repo.run("config", "validate")
        self.assertEqual(0, validate.returncode, output(validate))
        doctor = repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        listed = repo.run("stage", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        data = json.loads(listed.stdout)
        stages = data["stages"] if isinstance(data, dict) else data
        self.assertEqual([], stages)

    def test_parse_agents_skills_mcp_model_effort(self):
        repo = Repo(
            self,
            BASE
            + """
            [[stages]]
            id = "plan"
            agents = ["openspec-planner"]
            skills = ["openspec-propose"]
            mcp = ["docs-reader"]
            model = "claude-sonnet-5-5-high"
            effort = { tokens = 100000, time_seconds = 1800, questions = 10, review_lines = 400 }
            """,
        )
        _skill(repo)
        add_agent = repo.run(
            "catalog", "add", "--kind", "agents", "--id", "openspec-planner",
            "--ref", "agents/openspec-planner.md", "--summary", "Planner role",
        )
        self.assertEqual(0, add_agent.returncode, output(add_agent))
        add_mcp = repo.run(
            "catalog", "add", "--kind", "mcp", "--id", "docs-reader",
            "--ref", "mcp://docs", "--summary", "read-only docs",
        )
        self.assertEqual(0, add_mcp.returncode, output(add_mcp))

        shown = repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        view = json.loads(shown.stdout)
        self.assertIn("stages", view)
        self.assertEqual(1, len(view["stages"]))
        stage = view["stages"][0]
        self.assertEqual("plan", stage["id"])
        self.assertEqual(["openspec-planner"], stage["agents"])
        self.assertEqual(["openspec-propose"], stage["skills"])
        self.assertEqual(["docs-reader"], stage["mcp"])
        self.assertEqual("claude-sonnet-5-5-high", stage["model"])
        self.assertEqual(100000, stage["effort"]["tokens"])
        self.assertEqual(1800, stage["effort"]["time_seconds"])
        self.assertEqual(10, stage["effort"]["questions"])
        self.assertEqual(400, stage["effort"]["review_lines"])

        listed = repo.run("stage", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        payload = json.loads(listed.stdout)
        stages = payload["stages"] if isinstance(payload, dict) else payload
        self.assertEqual("plan", stages[0]["id"])
        self.assertEqual(["openspec-planner"], stages[0]["agents"])

    def test_unknown_stage_id_fails_load(self):
        text = (
            '[project]\nname = "demo"\npolicy = "POLICY.md"\n\n'
            '[[gates]]\nname = "plan check"\nstage = "plan"\nrun = "true"\n\n'
            '[[stages]]\nid = "no-such-stage"\nagents = ["x"]\n'
        )
        with self.assertRaises(ProfileError) as ctx:
            load_profile(text=text)
        self.assertIn("no-such-stage", str(ctx.exception))

    def test_unknown_catalog_id_fails_validate(self):
        repo = Repo(
            self,
            BASE
            + """
            [[stages]]
            id = "plan"
            agents = ["missing-agent"]
            """,
        )
        result = repo.run("config", "validate")
        self.assertNotEqual(0, result.returncode, output(result))
        self.assertIn("missing-agent", output(result))

    def test_unknown_id_fails_doctor(self):
        repo = Repo(
            self,
            BASE
            + """
            [[stages]]
            id = "plan"
            mcp = ["ghost-mcp"]
            """,
        )
        self._with_skills_dir(repo)
        result = repo.run("doctor")
        self.assertNotEqual(0, result.returncode, output(result))
        self.assertIn("ghost-mcp", output(result))
        self.assertIn("ERROR", output(result))

    def test_skills_dir_overlay_resolves_skill_id(self):
        repo = Repo(
            self,
            BASE
            + """
            [[stages]]
            id = "plan"
            skills = ["openspec-propose"]
            model = "claude-sonnet-5-5-high"
            """,
        )
        _skill(repo, "openspec-propose")
        result = repo.run("config", "validate")
        self.assertEqual(0, result.returncode, output(result))
        listed = repo.run("stage", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        stages = json.loads(listed.stdout)["stages"]
        self.assertEqual(["openspec-propose"], stages[0]["skills"])
        self.assertEqual("claude-sonnet-5-5-high", stages[0]["model"])

    def test_missing_catalogs_dir_with_agent_ref_fails(self):
        profile = """
        [project]
        name = "demo"
        policy = "POLICY.md"

        [[gates]]
        name = "plan check"
        stage = "plan"
        run = "true"

        [skills]
        dir = "skills"

        [[stages]]
        id = "plan"
        agents = ["openspec-planner"]
        """
        repo = Repo(self, profile)
        result = repo.run("config", "validate")
        self.assertNotEqual(0, result.returncode, output(result))
        self.assertTrue(
            "openspec-planner" in output(result) or "catalog" in output(result).lower(),
            output(result),
        )

    def test_stage_list_json_never_executes(self):
        repo = Repo(
            self,
            BASE
            + """
            [[stages]]
            id = "plan"
            mcp = ["hook"]
            skills = ["openspec-propose"]
            """,
        )
        _skill(repo)
        add = repo.run(
            "catalog", "add", "--kind", "mcp", "--id", "hook",
            "--ref", "mcp://unused",
            "--summary", "command=rm -rf / (must not run)",
        )
        self.assertEqual(0, add.returncode, output(add))
        marker = repo.root / "MUST_NOT_EXIST"
        listed = repo.run("stage", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        self.assertFalse(marker.exists())
        self.assertIn("hook", listed.stdout)

    def test_config_show_redacted_surfaces_stages(self):
        repo = Repo(
            self,
            BASE
            + """
            [[stages]]
            id = "pre-push"
            skills = ["openspec-propose"]
            effort = { tokens = 50000 }
            """,
        )
        _skill(repo)
        shown = repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        view = json.loads(shown.stdout)
        self.assertEqual("pre-push", view["stages"][0]["id"])
        self.assertEqual(50000, view["stages"][0]["effort"]["tokens"])


if __name__ == "__main__":
    unittest.main()
