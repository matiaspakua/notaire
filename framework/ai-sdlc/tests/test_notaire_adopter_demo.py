"""Notaire sandbox adopter demo wiring (#2243 / CU76).

Adopter assertions against the live profile. Kit code stays brand-independent;
these prove the sandbox has an approved workflow aligned with Foreman stages.
"""
from __future__ import annotations

import json
import re
import subprocess
import sys
import unittest
from pathlib import Path

from helpers import BIN, ENV, KIT, output


def _notaire_root() -> Path | None:
    repo = KIT.parents[1]
    if (repo / ".aisdlc" / "aisdlc.toml").is_file():
        return repo
    return None


def _run(repo: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, str(BIN), "--profile", str(repo / ".aisdlc" / "aisdlc.toml"), *args],
        cwd=repo,
        env=ENV,
        capture_output=True,
        text=True,
    )


REF_RE = re.compile(r"^(?P<kind>[a-z]+):(?P<id>[^@]+)@(?P<version>\d+)$")


def _collect_pinned_ids(body: dict) -> tuple[set[str], set[str]]:
    agents: set[str] = set()
    skills: set[str] = set()
    for step in body.get("steps") or []:
        if not isinstance(step, dict):
            continue
        agent = step.get("agent")
        if isinstance(agent, str):
            match = REF_RE.fullmatch(agent.strip())
            if match and match.group("kind") == "agents":
                agents.add(match.group("id"))
        for item in step.get("skills") or []:
            if not isinstance(item, str):
                continue
            match = REF_RE.fullmatch(item.strip())
            if match and match.group("kind") == "skills":
                skills.add(match.group("id"))
    # v1 fallback
    for entry_id in body.get("agents") or []:
        if isinstance(entry_id, str):
            agents.add(entry_id)
    for entry_id in body.get("skills") or []:
        if isinstance(entry_id, str):
            skills.add(entry_id)
    return agents, skills


class NotaireAdopterDemoTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = _notaire_root()
        if self.repo is None:
            self.skipTest("not running inside sandbox adopter checkout")

    def test_notaire_has_approved_workflow_with_stage_refs(self):
        plan = _run(self.repo, "foreman", "plan", "--json")
        self.assertEqual(0, plan.returncode, output(plan))
        stages = json.loads(plan.stdout).get("stages") or []
        stage_agents: set[str] = set()
        stage_skills: set[str] = set()
        for stage in stages:
            stage_agents.update(stage.get("agents") or [])
            stage_skills.update(stage.get("skills") or [])
        self.assertTrue(stage_agents, "Foreman stages must declare agents")
        self.assertTrue(stage_skills, "Foreman stages must declare skills")

        listed = _run(self.repo, "workflow", "list", "--approved", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        entries = json.loads(listed.stdout).get("entries") or []
        self.assertGreaterEqual(len(entries), 1, "sandbox needs ≥1 approved workflow")

        matched = False
        for row in entries:
            shown = _run(self.repo, "workflow", "show", "--id", row["id"], "--json")
            self.assertEqual(0, shown.returncode, output(shown))
            payload = json.loads(shown.stdout)
            self.assertEqual("approved", payload.get("governance"))
            agents, skills = _collect_pinned_ids(payload.get("body") or {})
            self.assertTrue(agents, f"workflow {row['id']} needs agent pins")
            self.assertTrue(skills, f"workflow {row['id']} needs skill pins")
            if agents <= stage_agents and skills <= stage_skills:
                matched = True
                break
        self.assertTrue(
            matched,
            "no approved workflow pins only agents/skills used by Foreman stages",
        )

    def test_notaire_doctor_stays_clean_with_demo_catalogs(self):
        doctor = _run(self.repo, "doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        validate = _run(self.repo, "catalog", "validate")
        self.assertEqual(0, validate.returncode, output(validate))
        report = _run(self.repo, "report")
        self.assertEqual(0, report.returncode, output(report))


if __name__ == "__main__":
    unittest.main()
