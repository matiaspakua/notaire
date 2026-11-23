"""Bench results say which RepoLens produced them, so stale results can be detected."""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

import repolens

BENCH = Path(__file__).resolve().parents[1] / "bench"
sys.path.insert(0, str(BENCH))

import retrieval_bench  # noqa: E402


def test_retrieval_results_are_stamped_with_the_repolens_version(tmp_path: Path) -> None:
    subprocess.run(["git", "init", "-q", str(tmp_path)], check=True)
    subprocess.run(["git", "-C", str(tmp_path), "-c", "user.email=t@e", "-c", "user.name=t",
                    "commit", "-q", "--allow-empty", "-m", "root"], check=True)
    spec = {"name": "tiny", "clone": tmp_path, "tasks": [{"id": "a"}, {"id": "b"}]}

    meta = retrieval_bench.result_meta(spec, k=10, taskset="tiny")

    assert meta["repolens_version"] == repolens.__version__
    assert meta["tasks"] == 2
    assert meta["corpus"] == "tiny"
