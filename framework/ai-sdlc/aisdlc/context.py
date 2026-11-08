"""What every command needs: the project root, its profile, and how to run shell commands there."""
from __future__ import annotations

import os
import re
import shlex
import signal
import subprocess
import sys
import threading
import time
from collections import deque
from dataclasses import dataclass
from pathlib import Path

from aisdlc.profile import Profile
from aisdlc.repo import clean_env

KIT_BIN = Path(__file__).resolve().parents[1] / "bin" / "aisdlc"
TAIL_LINES = 40


@dataclass
class Context:
    root: Path
    profile: Profile
    profile_path: Path | None

    def path(self, rel: str) -> Path:
        return self.root / rel

    def expand(self, command: str, variables: dict[str, str]) -> str:
        """Fill known `{placeholders}` only, so shell braces such as awk '{print}' survive,
        and let a command start with `aisdlc` to call this kit."""
        values = {"root": str(self.root), **variables}
        if self.profile_path is not None:
            values["profile"] = str(self.profile_path)
        command = re.sub(r"\{([a-z_][a-z0-9_]*)\}", lambda m: values.get(m.group(1), m.group(0)), command)
        if command == "aisdlc" or command.startswith("aisdlc "):
            command = self.self_command() + command[len("aisdlc"):]
        return command

    def self_command(self) -> str:
        parts = [sys.executable, str(KIT_BIN), "--root", str(self.root)]
        if self.profile_path is not None:
            parts += ["--profile", str(self.profile_path)]
        return " ".join(shlex.quote(p) for p in parts)


@dataclass
class CommandResult:
    exit: int | None
    seconds: float
    tail: list[str]
    timed_out: bool = False


def run_shell(command: str, cwd: Path, timeout: int, stream: bool = False) -> CommandResult:
    """Run a shell command, optionally streaming it, keeping only the tail of its output for the
    evidence record. A timeout kills the whole process group, so children cannot hold the pipe."""
    started = time.monotonic()
    tail: deque[str] = deque(maxlen=TAIL_LINES)
    proc = subprocess.Popen(command, shell=True, cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                            text=True, errors="replace", start_new_session=True, env=clean_env())
    expired = threading.Event()

    def kill() -> None:
        expired.set()
        try:
            os.killpg(proc.pid, signal.SIGKILL)
        except ProcessLookupError:
            pass

    timer = threading.Timer(timeout, kill)
    timer.start()
    try:
        for line in proc.stdout:
            if stream:
                sys.stdout.write(line)
                sys.stdout.flush()
            tail.append(line.rstrip("\n"))
        proc.wait()
    finally:
        timer.cancel()
    seconds = time.monotonic() - started
    if expired.is_set():
        return CommandResult(None, seconds, list(tail), timed_out=True)
    return CommandResult(proc.returncode, seconds, list(tail))
