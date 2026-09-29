#!/usr/bin/env python3
"""Run a command with a hard time limit: watchdog.py SECONDS CMD [ARG...]

The command runs in its own process group. On timeout the whole group gets TERM, then KILL
after WATCHDOG_GRACE seconds (default 10), and the watchdog exits 124. TERM/INT sent to the
watchdog are passed to the group, so stopping the foreman stops the worker.
A perl alarm did not stop `codex exec`: SIGALRM reaches only the exec'd process.
"""
import os
import signal
import subprocess
import sys

TIMED_OUT = 124


def _stop(proc, grace):
    # KILL follows even when the leader died on TERM: its children may ignore TERM
    for sig in (signal.SIGTERM, signal.SIGKILL):
        try:
            os.killpg(proc.pid, sig)
        except ProcessLookupError:
            return
        try:
            proc.wait(timeout=grace)
        except subprocess.TimeoutExpired:
            pass
    proc.wait()


def main(argv):
    if len(argv) < 2:
        print(__doc__, file=sys.stderr)
        return 2
    seconds, cmd = float(argv[0]), argv[1:]
    grace = float(os.environ.get("WATCHDOG_GRACE", "10"))
    proc = subprocess.Popen(cmd, start_new_session=True)

    def forward(signum, _frame):
        _stop(proc, grace)
        sys.exit(128 + signum)

    signal.signal(signal.SIGTERM, forward)
    signal.signal(signal.SIGINT, forward)
    try:
        rc = proc.wait(timeout=seconds)
    except subprocess.TimeoutExpired:
        print("watchdog: %s exceeded %ss, stopping it" % (cmd[0], argv[0]), file=sys.stderr)
        _stop(proc, grace)
        return TIMED_OUT
    return 128 - rc if rc < 0 else rc


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
