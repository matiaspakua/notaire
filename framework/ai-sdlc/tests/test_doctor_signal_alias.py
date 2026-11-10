"""Doctor accepts intentional `signal` local alias (#2211 / #2159)."""
from __future__ import annotations

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
"""


class DoctorSignalAliasTest(unittest.TestCase):
    def test_doctor_accepts_signal_local_alias(self):
        """#2211: signal is a documented local alias — no non-canonical WARN."""
        repo = Repo(
            self,
            BASE
            + """
            [[gates]]
            name = "ratchet signal"
            stage = "signal"
            run = "true"
            """,
        )
        result = repo.run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result)
        self.assertNotRegex(
            combined,
            r"WARN\s+stage 'signal' is not canonical",
            combined,
        )
        self.assertNotIn("stage 'signal' is not canonical", combined.lower())

    def test_unknown_custom_stage_still_warns(self):
        """#2211: other non-canonical stages still WARN."""
        repo = Repo(
            self,
            BASE
            + """
            [[gates]]
            name = "custom ops"
            stage = "custom-ops"
            run = "true"
            """,
        )
        result = repo.run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result)
        self.assertRegex(
            combined,
            r"WARN\s+stage 'custom-ops' is not canonical",
            combined,
        )


if __name__ == "__main__":
    unittest.main()
