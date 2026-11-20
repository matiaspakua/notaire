#!/usr/bin/env python3
"""Factory and RepoLens extraction boundaries.

Static scan (always): adopter markers, parent-index escapes, and imports across
the factory core and the RepoLens package/runners.

Copy check: each tree is copied outside the repository and runs its own tests
plus a CLI help check. RepoLens tests run when pytest is importable.
"""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
KIT = ROOT / "framework" / "ai-sdlc"
REPOLENS = KIT / "repolens"
sys.path.insert(0, str(KIT))

from aisdlc.boundaries import scan_factory, scan_repolens  # noqa: E402


def _fail(title: str, hits: list[str]) -> int:
    print(f"FAIL {title}")
    for hit in hits:
        print(f"  {hit}")
    return 1


def _copy(src: Path, dst: Path, exclude: set[str]) -> None:
    def ignore(directory: str, names: list[str]) -> set[str]:
        skipped = {"__pycache__", ".pytest_cache", ".ruff_cache", ".venv", ".venv-telemetry", "dist", "build"}
        if Path(directory) == src:
            skipped |= exclude
        return {name for name in names if name in skipped or name.endswith(".egg-info")}

    shutil.copytree(src, dst, ignore=ignore, symlinks=False)


def _run(argv: list[str], cwd: Path) -> subprocess.CompletedProcess[str]:
    return subprocess.run(argv, cwd=cwd, text=True, capture_output=True, check=False)


def main() -> int:
    failed = 0
    factory_hits = scan_factory(KIT)
    repolens_hits = scan_repolens(REPOLENS)
    if factory_hits:
        failed |= _fail("factory core", factory_hits)
    else:
        print("factory core: no adopter marker, no repolens import, no parent escape")
    if repolens_hits:
        failed |= _fail("repolens core", repolens_hits)
    else:
        print("repolens core: no adopter marker, no aisdlc import, no parent escape")
    if failed:
        return failed

    with tempfile.TemporaryDirectory(prefix="aisdlc-extract-") as tmp:
        base = Path(tmp)
        factory_copy = base / "ai-sdlc"
        repolens_copy = base / "repolens"
        _copy(KIT, factory_copy, {"repolens"})
        _copy(REPOLENS, repolens_copy, set())
        print(f"copied factory without repolens to {factory_copy}")
        suite = _run([sys.executable, "-m", "unittest", "discover", "-s", "tests", "-q"], factory_copy)
        if suite.returncode != 0:
            sys.stdout.write(suite.stdout[-4000:])
            sys.stderr.write(suite.stderr[-4000:])
            print("FAIL factory copy tests")
            return 1
        print("factory copy: unittest ok")
        help_run = _run([sys.executable, "bin/aisdlc", "--help"], factory_copy)
        if help_run.returncode != 0:
            print(help_run.stderr)
            print("FAIL factory copy CLI")
            return 1
        print("factory copy: aisdlc --help ok")

        probe = _run([sys.executable, "-c", "import pytest"], repolens_copy)
        if probe.returncode != 0:
            print("repolens copy tests skipped: pytest is not importable")
        else:
            tests = _run([sys.executable, "-m", "pytest", "-q", "-p", "no:cacheprovider", "tests"], repolens_copy)
            if tests.returncode != 0:
                sys.stdout.write(tests.stdout[-4000:])
                sys.stderr.write(tests.stderr[-4000:])
                print("FAIL repolens copy tests")
                return 1
            print("repolens copy: pytest ok")
        cli = _run([sys.executable, "-m", "repolens", "--help"], repolens_copy)
        if cli.returncode != 0:
            print(cli.stderr)
            print("FAIL repolens copy CLI")
            return 1
        print("repolens copy: python -m repolens --help ok")
    print("extraction boundaries: ok")
    return 0


if __name__ == "__main__":
    sys.exit(main())
