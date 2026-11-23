#!/usr/bin/env python3
"""Guards #2414 (CU76): one stack-backed preflight at a time.

`stack_lock.sh acquire <pid>` records <pid> (the preflight run) as the holder and returns as soon
as it holds the lock; it fails naming the holder after PREFLIGHT_STACK_WAIT seconds. A lock whose
holder pid is dead is taken over. `release <pid>` only drops a lock that pid holds.

Run with: python3 workspace/tests/test_stack_lock.py
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import tempfile
import time
import unittest
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "workspace" / "sdlc" / "stack_lock.sh"
PREFLIGHT = ROOT / "workspace" / "sdlc" / "preflight.sh"


class StackLockTest(unittest.TestCase):
    def setUp(self):
        self.common = Path(tempfile.mkdtemp(prefix="stack-lock-"))
        self.addCleanup(shutil.rmtree, self.common, ignore_errors=True)
        self.holders: list[subprocess.Popen] = []
        self.addCleanup(self._stop_holders)

    def _stop_holders(self):
        for proc in self.holders:
            proc.kill()
            proc.wait()

    def holder(self) -> int:
        proc = subprocess.Popen(["sleep", "60"])
        self.holders.append(proc)
        return proc.pid

    def lock(self, command: str, pid: int, wait: str = "5") -> subprocess.CompletedProcess[str]:
        env = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}
        env.update(GIT_COMMON_DIR=str(self.common), PREFLIGHT_STACK_WAIT=wait, PREFLIGHT_STACK_POLL="1")
        return subprocess.run(["bash", str(SCRIPT), command, str(pid)], env=env, capture_output=True,
                              text=True, timeout=30)

    def holder_pid(self) -> str:
        return (self.common / "preflight-stack.lock" / "holder").read_text(encoding="utf-8").splitlines()[0]

    def test_acquire_returns_at_once_and_records_the_caller(self):
        pid = self.holder()
        started = time.monotonic()
        result = self.lock("acquire", pid)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertLess(time.monotonic() - started, 3)
        self.assertEqual(str(pid), self.holder_pid())

    def test_a_second_run_times_out_and_names_the_holder(self):
        first = self.holder()
        self.assertEqual(0, self.lock("acquire", first).returncode)
        second = self.lock("acquire", self.holder(), wait="2")
        self.assertNotEqual(0, second.returncode)
        self.assertIn(f"pid {first}", second.stdout + second.stderr)

    def test_a_lock_whose_holder_died_is_taken_over(self):
        dead = subprocess.Popen(["true"])
        dead.wait()
        self.assertEqual(0, self.lock("acquire", dead.pid).returncode)
        mine = self.holder()
        self.assertEqual(0, self.lock("acquire", mine).returncode)
        self.assertEqual(str(mine), self.holder_pid())

    def test_release_only_drops_the_callers_lock(self):
        owner = self.holder()
        self.assertEqual(0, self.lock("acquire", owner).returncode)
        self.lock("release", self.holder())
        self.assertEqual(str(owner), self.holder_pid())
        self.lock("release", owner)
        self.assertFalse((self.common / "preflight-stack.lock").exists())

    def test_only_one_of_two_concurrent_runs_gets_the_lock(self):
        pids = [self.holder(), self.holder()]
        with ThreadPoolExecutor(max_workers=2) as pool:
            results = list(pool.map(lambda pid: self.lock("acquire", pid, wait="2"), pids))
        self.assertEqual([0, 1], sorted(min(r.returncode, 1) for r in results))


class PreflightUsesTheLockTest(unittest.TestCase):
    def setUp(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        start = text.index('section "Server-backed suites"')
        self.section = text[start:text.index("PREFLIGHT SUMMARY", start)]

    def test_the_server_backed_section_holds_the_lock_for_this_run(self):
        self.assertRegex(self.section, r'stack_lock\.sh acquire "\$\$"')
        self.assertRegex(self.section, r'stack_lock\.sh release "\$\$"')
        self.assertNotRegex(self.section, r"stack_lock\.sh acquire[^\n]*&\s*$")

    def test_the_smoke_test_always_takes_the_stack_down(self):
        smoke = self.section[self.section.index("docker compose smoke test"):]
        self.assertTrue(re.search(r"\|\| _smoke_rc=\$\?\n\s*docker compose down", smoke))


if __name__ == "__main__":
    unittest.main()
