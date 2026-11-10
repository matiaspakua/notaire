"""Lead-time operators pass Issue number, not PR number (#2211)."""
from __future__ import annotations

import subprocess
import sys
import unittest
from pathlib import Path

from helpers import BIN, ENV, Repo, output


class MetricsLeadTimeHintTest(unittest.TestCase):
    def test_lead_time_issue_help_says_issue_not_pr(self):
        """#2211: --issue help states forge Issue number, not pull-request."""
        result = subprocess.run(
            [sys.executable, str(BIN), "metrics", "lead-time", "--help"],
            cwd=Path(__file__).resolve().parents[1],
            env=ENV,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, output(result))
        help_text = (result.stdout + result.stderr).lower()
        self.assertIn("issue", help_text)
        self.assertTrue(
            "not a pull-request" in help_text
            or "not a pr" in help_text
            or "not pull request" in help_text
            or ("issue number" in help_text and "not" in help_text and "pr" in help_text),
            help_text,
        )

    def test_doctor_info_mentions_issue_not_pr_for_lead_time(self):
        """#2211: doctor INFO reminds operators lead-time --issue is Issue-keyed."""
        repo = Repo(
            self,
            """
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
            """,
        )
        result = repo.run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result).lower()
        self.assertIn("lead-time", combined)
        self.assertTrue(
            ("issue" in combined and "pr" in combined and "not" in combined)
            or ("issue number" in combined and "not" in combined),
            combined,
        )


if __name__ == "__main__":
    unittest.main()
