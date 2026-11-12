"""Environment-aware Isolator snapshot helper used by honesty tests (#2256 CI)."""
from __future__ import annotations

import unittest

import helpers


class IsolatorEnvHelperTest(unittest.TestCase):
    def tearDown(self) -> None:
        helpers._ISOLATOR_SNAPSHOT = None

    def test_sandbox_live_true_when_probe_ready(self):
        helpers._ISOLATOR_SNAPSHOT = {
            "status": "ready",
            "sandbox": True,
            "backend": "bwrap",
            "enforces_committer": True,
            "detail": "probe ok",
        }
        self.assertTrue(helpers.isolator_sandbox_live())
        self.assertEqual("ready", helpers.isolator_live_snapshot()["status"])

    def test_sandbox_live_false_when_no_evaluable(self):
        helpers._ISOLATOR_SNAPSHOT = {
            "status": "no-evaluable",
            "sandbox": False,
            "backend": "none",
            "enforces_committer": False,
            "detail": "no bwrap",
        }
        self.assertFalse(helpers.isolator_sandbox_live())


if __name__ == "__main__":
    unittest.main()
