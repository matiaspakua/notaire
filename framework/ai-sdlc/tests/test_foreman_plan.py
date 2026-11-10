"""Foreman stage-knob routing (#2196) — dispatch plan only, never executes."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

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

STAGE = """
[[stages]]
id = "plan"
agents = ["openspec-planner"]
skills = ["openspec-propose"]
mcp = ["docs-reader"]
model = "claude-sonnet-5-5-high"
effort = { tokens = 100000, time_seconds = 1800, questions = 10, review_lines = 400 }
"""


def _skill(repo: Repo, name: str = "openspec-propose") -> None:
    folder = repo.root / "skills" / name
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "SKILL.md").write_text(
        f"---\nname: {name}\ndescription: Demo skill\n---\n# {name}\n",
        encoding="utf-8",
    )


def _catalogs(repo: Repo) -> None:
    add_agent = repo.run(
        "catalog",
        "add",
        "--kind",
        "agents",
        "--id",
        "openspec-planner",
        "--ref",
        "agents/openspec-planner.md",
        "--summary",
        "Planner role",
    )
    assert add_agent.returncode == 0, output(add_agent)
    add_mcp = repo.run(
        "catalog",
        "add",
        "--kind",
        "mcp",
        "--id",
        "docs-reader",
        "--ref",
        "mcp://docs",
        "--summary",
        "read-only docs",
    )
    assert add_mcp.returncode == 0, output(add_mcp)


class ForemanPlanTest(unittest.TestCase):
    def test_omitted_stage_knobs_yield_empty_plan(self):
        """#2196: no [[stages]] → empty plan; executes/sandbox false."""
        repo = Repo(self, BASE)
        (repo.root / "skills").mkdir(exist_ok=True)
        result = repo.run("foreman", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual([], data["stages"])
        self.assertEqual(0, data.get("mapped_count", len(data["stages"])))
        self.assertIs(False, data["executes"])
        self.assertIs(False, data["sandbox"])
        note = (data.get("note") or "").lower()
        self.assertTrue(
            "dispatch" in note or "plan" in note,
            note,
        )
        self.assertTrue(
            "not" in note and ("isolator" in note or "sandbox" in note or "execute" in note),
            note,
        )

    def test_mapped_stage_appears_in_plan_json(self):
        """#2196: declared plan knobs appear in foreman plan --json."""
        repo = Repo(self, BASE + STAGE)
        _skill(repo)
        _catalogs(repo)
        result = repo.run("foreman", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertGreaterEqual(data.get("mapped_count", 0), 1)
        self.assertEqual(1, len(data["stages"]))
        stage = data["stages"][0]
        self.assertEqual("plan", stage["id"])
        self.assertEqual(["openspec-planner"], stage["agents"])
        self.assertEqual(["openspec-propose"], stage["skills"])
        self.assertEqual(["docs-reader"], stage["mcp"])
        self.assertEqual("claude-sonnet-5-5-high", stage["model"])
        self.assertEqual(100000, stage["effort"]["tokens"])
        self.assertIs(False, data["executes"])
        self.assertIs(False, data["sandbox"])

    def test_stage_filter_narrows_the_plan(self):
        repo = Repo(
            self,
            BASE
            + STAGE
            + """
            [[stages]]
            id = "pre-push"
            agents = ["openspec-planner"]
            skills = ["openspec-propose"]
            model = "claude-sonnet-5-5-medium"
            """,
        )
        _skill(repo)
        _catalogs(repo)
        result = repo.run("foreman", "plan", "--stage", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertTrue(data["stages"])
        self.assertTrue(all(s["id"] == "plan" for s in data["stages"]))

    def test_unknown_stage_filter_fails_closed(self):
        repo = Repo(self, BASE + STAGE)
        _skill(repo)
        _catalogs(repo)
        result = repo.run("foreman", "plan", "--stage", "not-a-gate", "--json")
        self.assertEqual(2, result.returncode, output(result))
        combined = output(result).lower()
        self.assertIn("not-a-gate", combined)

    def test_plan_never_executes_agents_or_mcp(self):
        repo = Repo(self, BASE + STAGE)
        _skill(repo)
        _catalogs(repo)
        result = repo.run("foreman", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertIs(False, data["executes"])
        self.assertIs(False, data["sandbox"])
        blob = json.dumps(data).lower()
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')
        note = (data.get("note") or "").lower()
        self.assertTrue("not" in note and ("execute" in note or "isolator" in note or "sandbox" in note), note)

    def test_route_is_alias_of_plan(self):
        repo = Repo(self, BASE + STAGE)
        _skill(repo)
        _catalogs(repo)
        plan = repo.run("foreman", "plan", "--json")
        route = repo.run("foreman", "route", "--json")
        self.assertEqual(0, plan.returncode, output(plan))
        self.assertEqual(0, route.returncode, output(route))
        plan_data = json.loads(plan.stdout)
        route_data = json.loads(route.stdout)
        self.assertEqual(plan_data["stages"], route_data["stages"])
        self.assertEqual(plan_data["executes"], route_data["executes"])
        self.assertEqual(plan_data["sandbox"], route_data["sandbox"])


if __name__ == "__main__":
    unittest.main()
