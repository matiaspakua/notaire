"""Grant profile schema: axiom 5 data toward Isolator — validate/list/show only (#2192)."""
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
"""


class GrantProfileTest(unittest.TestCase):
    def test_omitted_grants_are_noop(self):
        """#2192: no [[grants]] → validate/doctor succeed; grant list empty."""
        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        validate = repo.run("config", "validate")
        self.assertEqual(0, validate.returncode, output(validate))
        doctor = repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        listed = repo.run("grant", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        data = json.loads(listed.stdout)
        grants = data["grants"] if isinstance(data, dict) else data
        self.assertEqual([], grants)

    def test_parse_grant_fields(self):
        """#2192: load worktree/write_set/required_checks/budget/stage; surface in show."""
        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            stage = "plan"
            worktree = "."
            write_set = ["docs/**", "framework/ai-sdlc/**"]
            required_checks = ["plan check"]
            budget = { tokens = 100000, time_seconds = 1800, questions = 5, review_lines = 400 }
            """,
        )
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")

        shown = repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        view = json.loads(shown.stdout)
        self.assertIn("grants", view)
        self.assertEqual(1, len(view["grants"]))
        grant = view["grants"][0]
        self.assertEqual("plan-build", grant["id"])
        self.assertEqual("plan", grant["stage"])
        self.assertEqual(".", grant["worktree"])
        self.assertEqual(["docs/**", "framework/ai-sdlc/**"], grant["write_set"])
        self.assertEqual(["plan check"], grant["required_checks"])
        self.assertEqual(100000, grant["budget"]["tokens"])
        self.assertEqual(1800, grant["budget"]["time_seconds"])
        self.assertEqual(5, grant["budget"]["questions"])
        self.assertEqual(400, grant["budget"]["review_lines"])

        listed = repo.run("grant", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        payload = json.loads(listed.stdout)
        grants = payload["grants"] if isinstance(payload, dict) else payload
        self.assertEqual("plan-build", grants[0]["id"])

        detail = repo.run("grant", "show", "--id", "plan-build", "--json")
        self.assertEqual(0, detail.returncode, output(detail))
        self.assertEqual("plan-build", json.loads(detail.stdout)["id"])

    def test_unknown_stage_on_grant_fails_load(self):
        text = (
            '[project]\nname = "demo"\npolicy = "POLICY.md"\n\n'
            '[[gates]]\nname = "plan check"\nstage = "plan"\nrun = "true"\n\n'
            '[[grants]]\nid = "bad"\nstage = "no-such-stage"\nworktree = "."\n'
        )
        with self.assertRaises(ProfileError) as ctx:
            load_profile(text=text)
        self.assertIn("no-such-stage", str(ctx.exception))

    def test_unknown_required_check_fails_validate(self):
        """#2192: required_checks unknown against gates → fail closed."""
        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            worktree = "."
            required_checks = ["missing check"]
            """,
        )
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = repo.run("config", "validate")
        # load may already fail; either load exit 2 or validate non-zero is fail-closed
        if result.returncode == 0:
            # if load succeeded somehow, doctor/validate must still fail
            self.fail("expected fail-closed for unknown required_checks")
        combined = output(result)
        self.assertIn("missing check", combined)

    def test_grant_list_json_never_claims_sandbox(self):
        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            worktree = "."
            write_set = ["docs/**"]
            required_checks = ["plan check"]
            """,
        )
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        listed = repo.run("grant", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        blob = listed.stdout.lower()
        self.assertNotIn('"sandbox": true', blob.replace(" ", ""))
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')

        from helpers import isolator_live_snapshot, isolator_sandbox_live

        payload = isolator_live_snapshot()
        # Declaring grants must not invent Isolator truth — match the live probe.
        if isolator_sandbox_live():
            self.assertEqual("ready", payload["status"])
            self.assertIs(True, payload["sandbox"])
        else:
            self.assertEqual("no-evaluable", payload["status"])
            self.assertIs(False, payload["sandbox"])

    def test_isolator_honesty_unchanged_when_grants_present(self):
        """Grants do not fake Isolator; doctor Isolator line matches the live probe."""
        from helpers import isolator_live_snapshot, isolator_sandbox_live

        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            stage = "plan"
            worktree = "."
            write_set = ["docs/**"]
            required_checks = ["plan check"]
            """,
        )
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        doctor = repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        combined = output(doctor).lower()
        self.assertIn("isolator", combined)
        self.assertIn("grant", combined)
        self.assertTrue("not a sandbox" in combined or "policy" in combined, combined)
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
