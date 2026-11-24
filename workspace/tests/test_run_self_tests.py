#!/usr/bin/env python3
"""Guards #2502 (CU76): the process self-tests run one file per process, in parallel, with the same results.

Run with: python3 workspace/tests/test_run_self_tests.py
"""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
import time
import unittest
from pathlib import Path

RUNNER = Path(__file__).resolve().parents[2] / "workspace" / "sdlc" / "run_self_tests.py"
SLOW = "import time, unittest\n\nclass T(unittest.TestCase):\n    def test_slow(self):\n        time.sleep(1)\n"
PASS = "import unittest\n\nclass T(unittest.TestCase):\n    def test_ok(self):\n        self.assertTrue(True)\n"
FAIL = "import unittest\n\nclass T(unittest.TestCase):\n    def test_broken(self):\n        self.assertEqual(1, 2)\n"


class RunSelfTestsTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="selftests-"))
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)

    def suite(self, name: str, files: dict[str, str]) -> Path:
        folder = self.root / name
        folder.mkdir()
        for file, body in files.items():
            (folder / file).write_text(body, encoding="utf-8")
        return folder

    def run_runner(self, *dirs: Path):
        return subprocess.run([sys.executable, str(RUNNER), *map(str, dirs)], capture_output=True, text=True)

    def test_a_failing_file_fails_the_run_and_is_named(self):
        folder = self.suite("t", {"test_ok.py": PASS, "test_broken.py": FAIL})
        result = self.run_runner(folder)
        self.assertEqual(1, result.returncode, result.stdout + result.stderr)
        self.assertIn("test_broken.py", result.stdout)
        self.assertIn("AssertionError", result.stdout)
        self.assertNotIn("test_ok.py ---", result.stdout)

    def test_passing_files_run_concurrently(self):
        folder = self.suite("t", {f"test_slow{i}.py": SLOW for i in range(4)})
        start = time.monotonic()
        result = self.run_runner(folder)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertLess(time.monotonic() - start, 3.5, "four 1-second files must not run one after the other")
        self.assertIn("4 files", result.stdout)

    def test_several_directories_are_collected(self):
        result = self.run_runner(self.suite("a", {"test_a.py": PASS}), self.suite("b", {"test_b.py": PASS}))
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn("2 files", result.stdout)


if __name__ == "__main__":
    unittest.main()
