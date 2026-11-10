"""ApprovalPort stub: design-only, no-evaluable, never live approvals (#2190)."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))


PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"
level = 1
"""


class ApprovalPortTest(unittest.TestCase):
    def test_approval_status_is_no_evaluable_and_not_live(self):
        """#2190: without a custody runtime, ApprovalPort stays no-evaluable; not live."""
        from aisdlc.approval import approval_status  # noqa: E402

        payload = approval_status()
        self.assertEqual("no-evaluable", payload["status"])
        self.assertIs(False, payload["approvals_live"])
        self.assertIs(False, payload["agent_can_approve"])
        detail = (payload.get("detail") or "").lower()
        self.assertTrue(
            "ssh" in detail or "dsse" in detail or "token" in detail,
            detail,
        )
        blob = json.dumps(payload).lower()
        self.assertNotIn("approvals_live\": true", blob.replace(" ", ""))
        self.assertNotRegex(blob, r'"approvals_live"\s*:\s*true')
        self.assertNotRegex(blob, r'"agent_can_approve"\s*:\s*true')

    def test_doctor_reports_approval_no_evaluable_without_failing(self):
        """#2190: doctor surfaces ApprovalPort no-evaluable; does not fail for it alone."""
        repo = Repo(self, PROFILE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = repo.run("doctor")
        combined = output(result).lower()
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("approval", combined)
        self.assertIn("no-evaluable", combined)
        self.assertNotIn("approvals_live=true", combined)
        self.assertNotIn("agent_can_approve=true", combined)

    def test_approval_design_forbids_agent_self_approval(self):
        """#2190: stub/ADR surface forbids agent self-approval (policy tip)."""
        from aisdlc.approval import approval_status  # noqa: E402

        payload = approval_status()
        detail = (payload.get("detail") or "").lower()
        self.assertIn("self-approv", detail)
        self.assertIs(False, payload["agent_can_approve"])


if __name__ == "__main__":
    unittest.main()
