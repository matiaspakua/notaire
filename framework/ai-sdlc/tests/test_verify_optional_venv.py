"""verify.sh optional OTel venv must degrade — never hard-fail the stdlib suite (#2180)."""
from __future__ import annotations

import os
import shutil
import subprocess
import unittest
from pathlib import Path

from helpers import ENV, KIT

VERIFY = KIT / "verify.sh"
OPTIONAL_VENV = KIT / ".venv-telemetry"


class VerifyOptionalVenvTest(unittest.TestCase):
    def tearDown(self) -> None:
        # Do not leave forced-fail leftovers for the next test or a human verify run.
        if OPTIONAL_VENV.exists():
            shutil.rmtree(OPTIONAL_VENV, ignore_errors=True)

    def test_forced_venv_failure_degrades_and_cleans_half_venv(self) -> None:
        """AISDLC_TEST_FORCE_VENV_FAIL simulates ensurepip-missing hosts."""
        if OPTIONAL_VENV.exists():
            shutil.rmtree(OPTIONAL_VENV, ignore_errors=True)

        env = dict(ENV)
        env["AISDLC_TEST_FORCE_VENV_FAIL"] = "1"
        # Only exercise bootstrap + a trivial python -c, not the full 194-test suite.
        env["AISDLC_TEST_VERIFY_CMD"] = "python3 -c 'print(\"stdlib-ok\")'"

        proc = subprocess.run(
            ["bash", str(VERIFY)],
            cwd=KIT.parents[1],  # repo root (verify.sh cds there)
            env=env,
            capture_output=True,
            text=True,
        )
        combined = (proc.stdout or "") + (proc.stderr or "")
        self.assertEqual(
            0,
            proc.returncode,
            msg=f"verify must degrade on optional venv failure\n{combined}",
        )
        self.assertRegex(
            combined,
            r"(?i)warn|optional|otel|telemetry|venv",
            msg=f"expected a WARN about optional venv\n{combined}",
        )
        self.assertIn("stdlib-ok", combined)
        self.assertFalse(
            OPTIONAL_VENV.exists(),
            msg="half-created .venv-telemetry must be removed after degrade",
        )


if __name__ == "__main__":
    unittest.main()
