import os
import subprocess
import sys
import tempfile
import time
import unittest

import helpers

WATCHDOG = os.path.join(helpers.BIN, "watchdog.py")
# ignores TERM and starts a child that ignores it too; the child writes its pid
STUBBORN = """
import signal, subprocess, sys, time
signal.signal(signal.SIGTERM, signal.SIG_IGN)
child = subprocess.Popen([sys.executable, "-c",
    "import signal, time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(60)"])
open(sys.argv[1], "w").write(str(child.pid))
time.sleep(60)
"""


def _alive(pid):
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    return True


class WatchdogTest(unittest.TestCase):
    def _run(self, seconds, *cmd):
        env = dict(os.environ, WATCHDOG_GRACE="1")
        return subprocess.run([sys.executable, WATCHDOG, str(seconds), *cmd], env=env, timeout=30)

    def test_exit_status_of_a_worker_that_finishes_in_time(self):
        self.assertEqual(self._run(10, sys.executable, "-c", "raise SystemExit(3)").returncode, 3)

    def test_worker_ignoring_term_is_killed_with_its_child(self):
        pidfile = os.path.join(tempfile.mkdtemp(), "child.pid")
        start = time.monotonic()
        rc = self._run(2, sys.executable, "-c", STUBBORN, pidfile).returncode
        self.assertEqual(rc, 124)
        self.assertLess(time.monotonic() - start, 15)
        time.sleep(0.5)
        self.assertFalse(_alive(int(open(pidfile).read())))


if __name__ == "__main__":
    unittest.main()
