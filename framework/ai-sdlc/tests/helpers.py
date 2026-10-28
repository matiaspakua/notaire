"""Test fixtures: a throwaway git repository and a black-box runner for the `aisdlc` CLI.

The tests never touch the network and need only git and Python, which is the point:
the kit must run on a project with no particular stack.
"""
from __future__ import annotations

import json
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


def git(root: Path, *args: str) -> str:
    out = subprocess.run(["git", *args], cwd=root, check=True, capture_output=True, text=True)
    return out.stdout.strip()


class Repo:
    """A temporary git repository with an `aisdlc.toml` profile at its root."""

    def __init__(self, test: unittest.TestCase, profile: str = "") -> None:
        self.root = Path(tempfile.mkdtemp(prefix="aisdlc-"))
        test.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        git(self.root, "init", "-q", "-b", "main")
        git(self.root, "config", "user.email", "test@example.com")
        git(self.root, "config", "user.name", "Test")
        git(self.root, "config", "commit.gpgsign", "false")
        self.write("POLICY.md", POLICY)
        if profile:
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
