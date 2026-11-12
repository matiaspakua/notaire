"""Test fixtures: a throwaway git repository and a black-box runner for the `aisdlc` CLI.

The tests never touch the network and need only git and Python, which is the point:
the kit must run on a project with no particular stack.
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import tempfile
import textwrap
import unittest
from pathlib import Path

KIT = Path(__file__).resolve().parents[1]
BIN = KIT / "bin" / "aisdlc"

POLICY = "# Policy\n\nGates are absolute.\n"
# A pre-push hook exports GIT_DIR; inherited, `git init` here would rewrite the real repository.
ENV = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}

_ISOLATOR_SNAPSHOT: dict | None = None


def isolator_live_snapshot() -> dict:
    """One live Isolator probe for environment-aware honesty assertions.

    GitHub-hosted runners often have ``bwrap`` (sandbox=true / status=ready); local
    and some cloud VMs do not (no-evaluable / sandbox=false). Tests must assert the
    honest probe result, never force a fake sandbox=false when containment works.
    """
    global _ISOLATOR_SNAPSHOT
    if _ISOLATOR_SNAPSHOT is None:
        sys.path.insert(0, str(KIT))
        from aisdlc.isolator import isolator_status  # noqa: E402

        _ISOLATOR_SNAPSHOT = isolator_status()
    return dict(_ISOLATOR_SNAPSHOT)


def isolator_sandbox_live() -> bool:
    """True when the live containment probe reports an Isolator sandbox."""
    snap = isolator_live_snapshot()
    return bool(snap.get("sandbox")) and snap.get("status") == "ready"


def git(root: Path, *args: str) -> str:
    out = subprocess.run(["git", *args], cwd=root, check=True, capture_output=True, text=True, env=ENV)
    return out.stdout.strip()


class Repo:
    """A temporary git repository with an `aisdlc.toml` profile at its root."""

    def __init__(self, test: unittest.TestCase, profile: str | None = None) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="aisdlc-"))
        test.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        git(self.root, "init", "-q", "-b", "main")
        git(self.root, "config", "user.email", "test@example.com")
        git(self.root, "config", "user.name", "Test")
        git(self.root, "config", "commit.gpgsign", "false")
        self.write("POLICY.md", POLICY)
        if profile is not None:
            self.profile(profile)
        self.base = self.commit("chore: initial commit")

    def write(self, rel: str, text: str) -> Path:
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(textwrap.dedent(text), encoding="utf-8")
        return path

    def remove(self, rel: str) -> None:
        (self.root / rel).unlink()

    def profile(self, body: str) -> None:
        header = '[project]\nname = "demo"\npolicy = "POLICY.md"\n'
        body = textwrap.dedent(body)
        if "[project]" in body:
            header = ""
        self.write("aisdlc.toml", header + body)

    def commit(self, message: str) -> str:
        git(self.root, "add", "-A")
        git(self.root, "commit", "-q", "--allow-empty", "-m", message)
        return git(self.root, "rev-parse", "HEAD")

    def run(self, *args: str) -> subprocess.CompletedProcess:
        return subprocess.run(
            [sys.executable, str(BIN), "--root", str(self.root), *args],
            cwd=self.root,
            env=ENV,
            capture_output=True,
            text=True,
        )

    def ledger(self) -> list[dict]:
        path = self.root / ".aisdlc" / "evidence" / "ledger.jsonl"
        if not path.is_file():
            return []
        return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def output(result: subprocess.CompletedProcess) -> str:
    return result.stdout + result.stderr
