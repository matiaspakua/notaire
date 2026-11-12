"""Grant write-set policy dry-run (#2194) — axiom 5 lite, not Isolator OS."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

BASE = """
[project]
name = "demo"
policy = "POLICY.md"
base = "HEAD~1"

[[gates]]
name = "plan check"
stage = "plan"
run = "true"
"""

GRANT = """
[[grants]]
id = "plan-build"
stage = "plan"
worktree = "."
write_set = ["docs/**", "framework/ai-sdlc/**"]
required_checks = ["plan check"]
"""


class GrantWriteSetCheckTest(unittest.TestCase):
    def test_no_grant_declared_is_no_evaluable(self):
        """#2194: no [[grants]] → grant check status no-evaluable; no sandbox claim."""
        repo = Repo(self, BASE)
        repo.write("docs/a.md", "ok\n")
        repo.commit("docs: in set")
        result = repo.run("grant", "check", "--base", repo.base, "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("no-evaluable", data["status"])
        self.assertIn("policy", data.get("note", "").lower())
        blob = json.dumps(data).lower()
        self.assertNotIn('"sandbox": true', blob.replace(" ", ""))
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')

    def test_empty_write_set_is_no_evaluable(self):
        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            worktree = "."
            write_set = []
            required_checks = ["plan check"]
            """,
        )
        repo.write("docs/a.md", "ok\n")
        repo.commit("docs: touch")
        result = repo.run("grant", "check", "--base", repo.base, "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("no-evaluable", data["status"])

    def test_paths_inside_write_set_pass(self):
        """#2194: every changed path matches Grant write_set → pass."""
        repo = Repo(self, BASE + GRANT)
        repo.write("docs/a.md", "inside\n")
        repo.write("framework/ai-sdlc/x.py", "x = 1\n")
        repo.commit("feat: inside write set")
        result = repo.run("grant", "check", "--base", repo.base, "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("pass", data["status"])
        self.assertEqual([], data.get("outside") or data.get("violations") or [])
        self.assertIn("policy", data.get("note", "").lower())
        blob = json.dumps(data).lower()
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')

    def test_path_outside_write_set_fails(self):
        """#2194: path outside write_set → fail naming the path."""
        repo = Repo(self, BASE + GRANT)
        repo.write("docs/a.md", "inside\n")
        repo.write("backend-api/src/Main.java", "class Main {}\n")
        repo.commit("feat: mixed paths")
        result = repo.run("grant", "check", "--base", repo.base, "--json")
        self.assertEqual(1, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("fail", data["status"])
        outside = data.get("outside") or [
            v.get("path") if isinstance(v, dict) else v for v in (data.get("violations") or [])
        ]
        self.assertTrue(any("backend-api" in str(p) for p in outside), outside)
        note = (data.get("note") or "").lower()
        self.assertIn("policy", note)
        self.assertTrue("not" in note and ("isolation" in note or "sandbox" in note), note)

    def test_doctor_denies_sandbox_for_grant_check(self):
        """#2194: grant INFO stays policy-only; Isolator honesty follows the live probe."""
        from helpers import isolator_live_snapshot, isolator_sandbox_live

        repo = Repo(self, BASE + GRANT)
        doctor = repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        combined = output(doctor).lower()
        self.assertIn("grant", combined)
        # Grants never claim to *be* a sandbox — policy wording stays honest either way.
        self.assertTrue(
            "policy" in combined or "not a sandbox" in combined,
            combined,
        )
        self.assertIn("isolator", combined)
        snap = isolator_live_snapshot()
        if isolator_sandbox_live():
            self.assertEqual("ready", snap["status"])
            self.assertIn("sandbox=true", combined)
            self.assertIn("ready", combined)
        else:
            self.assertEqual("no-evaluable", snap["status"])
            self.assertIn("no-evaluable", combined)
            self.assertNotIn("sandbox=true", combined)


if __name__ == "__main__":
    unittest.main()
