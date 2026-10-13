#!/usr/bin/env python3
"""Guards #1665 (CU76): the Docker-free native stack matches what CI runs."""

from __future__ import annotations

import re
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "stack" / "native.sh"
WORKFLOW = REPO_ROOT / ".github" / "workflows" / "playwright-e2e.yml"


def workflow_value(pattern: str) -> str:
    return re.search(pattern, WORKFLOW.read_text(encoding="utf-8")).group(1)


class NativeStackTest(unittest.TestCase):
    def setUp(self):
        self.script = SCRIPT.read_text(encoding="utf-8")

    def test_backend_values_equal_the_ci_workflow(self):
        expected = [
            workflow_value(r"JWT_SECRET: (\S+)"),
            workflow_value(r"ACTUATOR_USER: (\S+)"),
            workflow_value(r"ACTUATOR_PASSWORD: (\S+)"),
            workflow_value(r"--spring\.datasource\.url=(\S+) \\"),
            workflow_value(r"--spring\.datasource\.username=(\S+) \\"),
            workflow_value(r"--spring\.datasource\.password=(\S+) \\"),
            workflow_value(r"BACKEND_URL: (\S+)"),
        ]
        for value in expected:
            self.assertIn(value, self.script)

    def test_usage_lists_every_command(self):
        result = subprocess.run(["bash", str(SCRIPT), "--help"], capture_output=True, text=True, timeout=30)
        self.assertEqual(result.returncode, 0, result.stderr)
        for command in ("up", "down", "status", "env"):
            self.assertRegex(result.stdout, rf"\b{command}\b")

    def test_script_is_valid_bash(self):
        result = subprocess.run(["bash", "-n", str(SCRIPT)], capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_status_of_a_stopped_stack_is_non_zero_without_a_trace(self):
        with tempfile.TemporaryDirectory() as empty:
            result = subprocess.run(
                ["bash", str(SCRIPT), "status"],
                capture_output=True,
                text=True,
                env={"PATH": "/usr/bin:/bin", "HOME": empty, "NOTAIRE_NATIVE_DIR": empty},
                timeout=30,
            )
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn("Traceback", result.stderr)
        self.assertNotIn("unbound variable", result.stderr)


if __name__ == "__main__":
    unittest.main()
