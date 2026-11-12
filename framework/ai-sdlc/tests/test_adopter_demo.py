"""Phase E kit-neutral adopter demo seed (#2243 / CU76)."""
from __future__ import annotations

import json
import re
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

ADOPTER_MARKERS = re.compile("|".join(["notai" + "re", "backend" + "-api", "workspace" + "/sdlc"]), re.I)

PROFILE = """
[project]
name = "sample-adopter"
policy = "POLICY.md"
level = 2

[catalogs]
dir = ".aisdlc/catalogs"

[evidence]
dir = ".aisdlc/evidence"

[[gates]]
name = "policy present"
stage = "plan"
run = "test -f POLICY.md"

[[stages]]
id = "plan"
agents = ["planner", "foreman"]
skills = ["propose", "steward"]
"""


class AdopterDemoTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")

    def test_demo_seed_creates_approved_workflow_on_temp_project(self):
        seeded = self.repo.run("demo", "seed", "--json")
        self.assertEqual(0, seeded.returncode, output(seeded))
        payload = json.loads(seeded.stdout)
        self.assertEqual("approved", payload.get("governance"))
        wid = payload.get("id")
        self.assertTrue(wid)

        listed = self.repo.run("workflow", "list", "--approved", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        ids = {row["id"] for row in json.loads(listed.stdout)["entries"]}
        self.assertIn(wid, ids)

    def test_from_stages_pins_foreman_agent_and_skill_ids(self):
        seeded = self.repo.run(
            "demo", "seed", "--from-stages", "--workflow", "stage-demo", "--json",
        )
        self.assertEqual(0, seeded.returncode, output(seeded))
        shown = self.repo.run("workflow", "show", "--id", "stage-demo", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        body = json.loads(shown.stdout)["body"]
        blob = json.dumps(body)
        # Pinned refs must come from [[stages]]; migrate pins the first agent + all skills.
        self.assertIn("agents:planner@", blob)
        self.assertIn("skills:propose@", blob)
        self.assertIn("skills:steward@", blob)
        self.assertTrue(
            "mcp" not in body
            or body.get("mcp") in ([], None)
            or all(
                (not isinstance(step, dict)) or not (step.get("mcp") or [])
                for step in (body.get("steps") or [])
            ),
            "demo workflow must not declare vanity MCP",
        )

    def test_doctor_catalog_validate_and_report_green_after_seed(self):
        seeded = self.repo.run("demo", "seed", "--from-stages", "--json")
        self.assertEqual(0, seeded.returncode, output(seeded))

        doctor = self.repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))

        validate = self.repo.run("catalog", "validate")
        self.assertEqual(0, validate.returncode, output(validate))

        report = self.repo.run("report")
        self.assertEqual(0, report.returncode, output(report))

    def test_kit_independence_holds_after_demo_implementation(self):
        hits = []
        for folder in ("aisdlc", "bin", "templates"):
            for path in sorted((KIT / folder).rglob("*")):
                if not path.is_file() or "__pycache__" in path.parts:
                    continue
                for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                    if ADOPTER_MARKERS.search(line):
                        hits.append(f"{path.relative_to(KIT)}:{number}: {line.strip()}")
        self.assertEqual([], hits)
        # Demo module must exist once implemented (Gate 2 fails until then).
        self.assertTrue((KIT / "aisdlc" / "demo.py").is_file())


if __name__ == "__main__":
    unittest.main()
