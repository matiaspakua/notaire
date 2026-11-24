#!/usr/bin/env python3
"""Run the process self-tests in parallel (#2502, CU76).

Each `test_*.py` file of the given directories runs as its own `python3 -m unittest discover -s <dir> -p <file>`
process, so imports and paths behave exactly as a serial `discover -s <dir>`. Up to os.cpu_count() files run at
once. Only failing files print their output.

    python3 workspace/sdlc/run_self_tests.py workspace/tests docs/tests ...
"""
from __future__ import annotations

import os
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path


def test_files(directories: list[str]) -> list[tuple[Path, str]]:
    return [(Path(d), f.name) for d in directories for f in sorted(Path(d).glob("test_*.py"))]


def run_one(directory: Path, name: str) -> tuple[str, int, str]:
    result = subprocess.run([sys.executable, "-m", "unittest", "discover", "-s", str(directory), "-p", name],
                            capture_output=True, text=True)
    return f"{directory}/{name}", result.returncode, result.stdout + result.stderr


def main(directories: list[str]) -> int:
    files = test_files(directories)
    start = time.monotonic()
    with ThreadPoolExecutor(max_workers=os.cpu_count() or 4) as pool:
        results = list(pool.map(lambda item: run_one(*item), files))
    failed = [(path, output) for path, code, output in results if code != 0]
    for path, output in failed:
        print(f"--- {path} ---\n{output}")
    print(f"self-tests: {len(files)} files, {len(failed)} failed, {time.monotonic() - start:.1f}s")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
