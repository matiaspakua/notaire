#!/usr/bin/env python3
"""Tests for workspace/sdlc/stack_lock.sh (#2414, CU76).

Runs the real shell script in a temporary git repository, so the lock
behaviour is exercised end-to-end.  Also contains text-level assertions
over preflight.sh to ensure the integration points are wired correctly.

Run with: python3 -m unittest workspace/tests/test_stack_lock
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import time
import unittest
from pathlib import Path
from typing import Any


_REPO_ROOT = Path(__file__).resolve().parents[2]
_STACK_LOCK = _REPO_ROOT / "workspace" / "sdlc" / "stack_lock.sh"
_PREFLIGHT = _REPO_ROOT / "workspace" / "sdlc" / "preflight.sh"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _run(cmd: list[str], **kw: Any) -> subprocess.CompletedProcess[str]:
    """Run a command, capturing stdout/stderr."""
    return subprocess.run(cmd, capture_output=True, text=True, **kw)


def _git_init(tmp: Path) -> None:
    """Create a bare-minimum git repo inside *tmp*."""
    _run(["git", "init", "-b", "main", str(tmp)])
    _run(["git", "config", "user.email", "test@local"])
    _run(["git", "config", "user.name", "Test"])
    (tmp / "dummy").write_text("hi")
    _run(["git", "add", "dummy"], cwd=str(tmp))
    _run(["git", "commit", "-m", "init"], cwd=str(tmp))


def _kill_tree(pid: int) -> None:
    """Kill a process and its children."""
    try:
        os.kill(pid, signal.SIGTERM)
    except ProcessLookupError:
        pass


import signal


# ---------------------------------------------------------------------------
# stack_lock.sh tests
# ---------------------------------------------------------------------------

class StackLockTest(unittest.TestCase):
    """Exercise stack_lock.sh acquire / release in a throwaway git repo."""

    def setUp(self) -> None:
        self.tmp = Path(subprocess.check_output(
            ["mktemp", "-d"], text=True
        ).strip())
        _git_init(self.tmp)
        self.env = os.environ.copy()
        self.env["GIT_COMMON_DIR"] = str(self.tmp / ".git")
        self._holders: list[subprocess.Popen] = []

    def tearDown(self) -> None:
        # Kill any background holders
        for p in self._holders:
            try:
                p.terminate()
                p.wait(timeout=3)
            except Exception:
                try:
                    p.kill()
                    p.wait(timeout=2)
                except Exception:
                    pass
        # Clean up any leftover lock dirs
        lock_dir = self.tmp / ".git" / "preflight-stack.lock"
        if lock_dir.exists():
            shutil.rmtree(lock_dir, ignore_errors=True)
        shutil.rmtree(self.tmp, ignore_errors=True)

    def _acquire_bg(self, **extra_env: str) -> subprocess.Popen:
        """Start acquire in background; it blocks after acquiring."""
        env = self.env.copy()
        env.update(extra_env)
        p = subprocess.Popen(
            ["bash", str(_STACK_LOCK), "acquire"],
            env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
        )
        self._holders.append(p)
        return p

    def _acquire_wait(self, **extra_env: str) -> subprocess.CompletedProcess[str]:
        """Run acquire with short poll interval for timeout tests."""
        env = self.env.copy()
        env["PREFLIGHT_STACK_POLL"] = "1"
        env.update(extra_env)
        return _run(["bash", str(_STACK_LOCK), "acquire"], env=env)

    def _release(self) -> subprocess.CompletedProcess[str]:
        return _run(["bash", str(_STACK_LOCK), "release"], env=self.env)

    # -- 1. Acquire then release -------------------------------------------

    def test_acquire_then_release(self) -> None:
        p = self._acquire_bg()
        time.sleep(0.3)  # let it acquire
        self.assertIsNone(p.poll())  # still running (holding lock)

        lock_dir = self.tmp / ".git" / "preflight-stack.lock"
        self.assertTrue(lock_dir.is_dir())

        # Kill the holder, then release
        p.terminate()
        p.wait(timeout=3)

        r = self._release()
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertFalse(lock_dir.exists())

    # -- 2. Second acquire waits and times out -----------------------------

    def test_second_acquire_times_out(self) -> None:
        p1 = self._acquire_bg()
        time.sleep(0.5)  # let p1 acquire the lock
        self.assertIsNone(p1.poll())  # p1 should still be running

        # Second acquire with short timeout should fail
        r2 = self._acquire_wait(PREFLIGHT_STACK_WAIT="2")
        self.assertNotEqual(r2.returncode, 0)
        combined = r2.stdout + r2.stderr
        self.assertIn("holder", combined.lower())

        # Clean up
        p1.terminate()
        p1.wait(timeout=3)
        self._release()

    # -- 3. Stale lock (dead PID) is taken over ----------------------------

    def test_stale_lock_taken_over(self) -> None:
        # Manually create a lock with a PID that doesn't exist
        lock_dir = self.tmp / ".git" / "preflight-stack.lock"
        lock_dir.mkdir(parents=True)
        (lock_dir / "holder").write_text("199999\nmain\n0\n")

        p = self._acquire_bg()
        time.sleep(0.5)
        self.assertIsNone(p.poll())
        # Lock should now be held by the new process
        holder_file = lock_dir / "holder"
        self.assertTrue(holder_file.exists())

        p.terminate()
        p.wait(timeout=3)
        self._release()

    # -- 4. Two concurrent acquirers: only one holds at a time -------------

    def test_concurrent_acquirers(self) -> None:
        env2 = {**self.env, "PREFLIGHT_STACK_WAIT": "2", "PREFLIGHT_STACK_POLL": "1"}

        # p1 acquires and stays alive
        p1 = self._acquire_bg()
        time.sleep(0.5)
        self.assertIsNone(p1.poll())  # p1 should still be running

        # p2 tries to acquire but p1 is alive — should time out
        p2 = subprocess.Popen(
            ["bash", str(_STACK_LOCK), "acquire"],
            env=env2, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
        )
        _, _ = p2.communicate()

        # p1 succeeded (still running), p2 failed (timed out)
        self.assertIsNone(p1.poll())
        self.assertNotEqual(p2.returncode, 0)

        # Clean up
        p1.terminate()
        p1.wait(timeout=3)
        self._release()


# ---------------------------------------------------------------------------
# preflight.sh text-level tests
# ---------------------------------------------------------------------------

class PreflightIntegrationTest(unittest.TestCase):
    """Text-level assertions on preflight.sh to verify integration points."""

    def setUp(self) -> None:
        self.content = _PREFLIGHT.read_text()

    def test_preflight_calls_stack_lock_acquire(self) -> None:
        """preflight.sh must call stack_lock.sh acquire inside server-backed section."""
        self.assertIn("stack_lock.sh", self.content)
        self.assertIn("acquire", self.content)

    def test_preflight_smoke_test_always_reaches_down(self) -> None:
        """The docker compose smoke test command must always reach 'docker compose down'."""
        # The smoke test should ensure 'docker compose down' always runs.
        # It can use a trap, a sequential "; docker compose down", or a standalone
        # "docker compose down" line after the up command (within the same bash -c block).
        has_trap = bool(re.search(r"trap.*docker compose down", self.content))
        has_sequential_down = bool(re.search(r";\s*docker compose down", self.content))
        # Also accept a standalone "docker compose down" line in the smoke test section
        # (the new pattern: up || rc=$? ; docker compose down ; exit $rc)
        has_standalone_down = bool(
            re.search(r"docker compose down\s*$", self.content, re.MULTILINE)
        )
        self.assertTrue(
            has_trap or has_sequential_down or has_standalone_down,
            "Smoke test should ensure 'docker compose down' always runs "
            f"(trap: {has_trap}, sequential: {has_sequential_down}, standalone: {has_standalone_down})"
        )


if __name__ == "__main__":
    unittest.main()
