"""Owner shell scripts under workspace/sdlc/owner/ (#1878)."""

from __future__ import annotations

import os
import shutil
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OWNER = ROOT / "workspace" / "sdlc" / "owner"
WORKFLOW = ROOT / ".github" / "workflows" / "sdlc-process.yml"


class OwnerScriptsTests(unittest.TestCase):
    def test_scripts_exist_and_default_to_preview(self):
        scripts = sorted(OWNER.glob("*.sh"))
        self.assertGreaterEqual(len(scripts), 3)
        for path in scripts:
            text = path.read_text(encoding="utf-8")
            self.assertIn("set -euo pipefail", text)
            # Preview is default: --apply is opt-in.
            self.assertRegex(text, r"--apply")

    def test_bash_n_passes(self):
        for path in sorted(OWNER.glob("*.sh")):
            result = subprocess.run(
                ["bash", "-n", str(path)],
                check=False,
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.returncode, 0, result.stderr)

    def test_shellcheck_when_installed(self):
        if shutil.which("shellcheck") is None:
            self.skipTest("shellcheck not installed")
        for path in sorted(OWNER.glob("*.sh")):
            result = subprocess.run(
                ["shellcheck", "-x", str(path)],
                check=False,
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_process_checks_run_bash_n(self):
        text = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("workspace/sdlc/owner", text)
        self.assertIn("bash -n", text)


if __name__ == "__main__":
    unittest.main()
