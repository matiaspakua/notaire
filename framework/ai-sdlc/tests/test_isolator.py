"""IsolatorPort honesty: no-evaluable without OS backend, never a fake sandbox (#2173/#2198)."""
from __future__ import annotations

import json
import os
import sys
import unittest
from unittest import mock

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))


PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"
level = 1
"""


class IsolatorPortTest(unittest.TestCase):
    def test_isolator_status_is_no_evaluable_and_not_sandbox(self):
        """#2173/#2198: without an OS backend, Isolator stays no-evaluable; sandbox=false."""
        from aisdlc.isolator import isolator_status  # noqa: E402

        with mock.patch.dict(os.environ, {"AISDLC_BWRAP": ""}):
            payload = isolator_status()
        self.assertEqual("no-evaluable", payload["status"])
        self.assertIs(False, payload["sandbox"])
        self.assertEqual("none", payload.get("backend"))
        self.assertIn("committer", (payload.get("detail") or "").lower())
        blob = json.dumps(payload).lower()
        self.assertNotIn("sandbox=true", blob)
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')

    def test_doctor_reports_isolator_no_evaluable_without_failing(self):
        """#2173: doctor surfaces Isolator no-evaluable; does not fail for it alone."""
        repo = Repo(self, PROFILE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        with mock.patch.dict(os.environ, {"AISDLC_BWRAP": ""}):
            # doctor runs in-process via repo.run — ENV copy may omit AISDLC_BWRAP;
            # force via helpers.ENV for this process tree.
            from helpers import ENV

            ENV["AISDLC_BWRAP"] = ""
            try:
                result = repo.run("doctor")
            finally:
                ENV.pop("AISDLC_BWRAP", None)
        combined = output(result).lower()
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("isolator", combined)
        self.assertIn("no-evaluable", combined)
        self.assertNotIn("sandbox=true", combined)


if __name__ == "__main__":
    unittest.main()
