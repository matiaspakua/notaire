"""Shared helpers for packaging / conformance / second-adopter wheel installs (#2256)."""
from __future__ import annotations

import re
import shutil
import subprocess
import sys
import tempfile
import textwrap
import unittest
from pathlib import Path

from helpers import ENV, KIT, git, output

PROFILES = ("go-service", "java-maven", "node-web", "python-service")
FILL = re.compile(r"<!--.*?-->", re.S)


def fill_change(text: str) -> str:
    return FILL.sub("Filled in.", text)


def run(*cmd: str, cwd: Path | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, cwd=cwd, env=ENV, capture_output=True, text=True)


def venv_problem() -> str | None:
    with tempfile.TemporaryDirectory(prefix="aisdlc-venvprobe-") as tmp:
        venv = run(sys.executable, "-m", "venv", str(Path(tmp) / "v"))
    return None if venv.returncode == 0 else f"venv unavailable: {venv.stderr.strip()[:120]}"


def build_wheel(python: str, source: Path, out: Path) -> subprocess.CompletedProcess:
    return run(python, "-m", "pip", "wheel", "--no-deps", "--no-build-isolation", "-w", str(out), str(source))


def builder(work: Path, source: Path, out: Path) -> str:
    if build_wheel(sys.executable, source, out).returncode == 0:
        return sys.executable
    venv = work / "build-venv"
    run(sys.executable, "-m", "venv", str(venv))
    python = str(venv / "bin" / "python")
    fetched = run(python, "-m", "pip", "install", "-q", "setuptools", "wheel")
    if fetched.returncode != 0:
        raise unittest.SkipTest(f"no usable setuptools offline: {fetched.stderr.strip()[-120:]}")
    built = build_wheel(python, source, out)
    assert built.returncode == 0, output(built)
    return python


class WheelEnv:
    """Build the kit wheel once and expose an installed `aisdlc` console script."""

    def __init__(self, test: unittest.TestCase) -> None:
        problem = venv_problem()
        if problem:
            raise unittest.SkipTest(problem)
        self.work = Path(tempfile.mkdtemp(prefix="aisdlc-wheel-"))
        # setUpClass passes the TestCase class (addClassCleanup); instances use addCleanup.
        if isinstance(test, type):
            test.addClassCleanup(shutil.rmtree, self.work, ignore_errors=True)
        else:
            test.addCleanup(shutil.rmtree, self.work, ignore_errors=True)
        source = self.work / "src"
        shutil.copytree(
            KIT,
            source,
            ignore=shutil.ignore_patterns(
                "build", "dist", "*.egg-info", ".venv-telemetry", "__pycache__", "samples"
            ),
        )
        wheel_dir = self.work / "wheels"
        builder(self.work, source, wheel_dir)
        self.wheel = next(wheel_dir.glob("*.whl"))
        self.venv = self.work / "venv"
        assert run(sys.executable, "-m", "venv", str(self.venv)).returncode == 0
        installed = run(
            str(self.venv / "bin" / "python"),
            "-m",
            "pip",
            "install",
            "--no-deps",
            "--no-index",
            str(self.wheel),
        )
        assert installed.returncode == 0, output(installed)
        self.aisdlc = str(self.venv / "bin" / "aisdlc")
        self.bin = self.venv / "bin"

    def run(self, cwd: Path, *args: str) -> subprocess.CompletedProcess:
        merged = dict(ENV)
        merged["PATH"] = f"{self.bin}:{merged.get('PATH', '')}"
        return subprocess.run(
            [self.aisdlc, "--root", str(cwd), *args],
            cwd=cwd,
            env=merged,
            capture_output=True,
            text=True,
        )


def new_repo(test: unittest.TestCase, name: str = "adopter") -> Path:
    root = Path(tempfile.mkdtemp(prefix=f"aisdlc-{name}-"))
    test.addCleanup(shutil.rmtree, root, ignore_errors=True)
    git(root, "init", "-q", "-b", "main")
    git(root, "config", "user.email", "test@example.com")
    git(root, "config", "user.name", "Test")
    git(root, "config", "commit.gpgsign", "false")
    return root


def adapt_profile(stock: str, name: str) -> str:
    """Stock profile + offline overlays so doctor/plan/red/report can run without a stack."""
    text = stock
    text = re.sub(r'(?m)^policy\s*=\s*".*"', 'policy = "POLICY.md"', text, count=1)
    if "schema_version" not in text:
        text = re.sub(r"(?m)^(\[project\])", r"\1\nschema_version = 1", text, count=1)
    if "[tracker]" in text:
        text = re.sub(
            r"(?ms)^\[tracker\].*?(?=^\[|\Z)",
            '[tracker]\nverify = "true"\n\n',
            text,
            count=1,
        )
    else:
        text += '\n[tracker]\nverify = "true"\n'
    text = re.sub(r"(?ms)^\[context\].*?(?=^\[|\Z)", "", text)
    text = re.sub(r"(?ms)^\[skills\].*?(?=^\[|\Z)", "", text)
    text = re.sub(r"(?ms)^\[\[ratchets\]\].*?(?=^\[|\Z)", "", text)
    if 'stage = "red"' not in text:
        text += textwrap.dedent(
            """

            [[gates]]
            name = "new test fails"
            stage = "red"
            run = "false"
            expect = "fail"
            """
        )
    text = re.sub(
        r'(?m)^(run\s*=\s*")(?:pytest|mvn|npx)[^"]*(")',
        r"\1false\2",
        text,
    )
    assert f'name = "{name}"' in text or f"name = '{name}'" in text
    return text


def seed_change(wheel: WheelEnv, root: Path) -> None:
    created = wheel.run(root, "change", "new", "matrix-demo", "--issue", "1", "--title", "Matrix")
    assert created.returncode == 0, output(created)
    candidates = list(root.rglob("matrix-demo.md"))
    assert candidates, "change record missing"
    path = candidates[0]
    path.write_text(fill_change(path.read_text(encoding="utf-8")), encoding="utf-8")
