"""Benchmark corpus: a clean `git clone --shared` of the repository HEAD.

This module (the benchmark and its answer key) is deleted from the clone's working tree, so neither the grep
arm nor the agents can read the expected answers. The clone keeps git history, so co-change edges exist.
"""

from __future__ import annotations

import re
import shutil
import subprocess
import sys
from pathlib import Path

import yaml

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))


def load_spec(path: Path) -> dict:
    """A task file names either a repository to clone (`repo`, `clone`, `profile`) or a prebuilt store
    (`config`: a repolens profile whose sources live next to it, e.g. the scale corpus of bench/scale_bench.py)."""
    spec = yaml.safe_load(path.read_text(encoding="utf-8"))
    corpus = spec["corpus"]
    base = path.resolve().parent
    if "config" in corpus:
        spec["config"] = (base / corpus["config"]).resolve()
        spec["clone"] = spec["config"].parent
        spec["prebuilt"] = True
        return spec
    spec["repo"] = (base / corpus["repo"]).resolve()
    spec["clone"] = (base / corpus["clone"]).resolve()
    spec["profile"] = (base / corpus["profile"]).resolve()
    spec["config"] = spec["clone"].parent / f"{spec['clone'].name}.toml"
    return spec


def ensure_corpus(spec: dict, refresh: bool = False, log=print) -> Path:
    """Clone (or fast-forward) the corpus and write its repolens config; returns the config path."""
    if spec.get("prebuilt"):
        if not spec["config"].exists():
            raise FileNotFoundError(f"{spec['config']} missing: build it first (bench/scale_bench.py)")
        return spec["config"]
    clone, repo = spec["clone"], spec["repo"]
    if not (clone / ".git").exists():
        clone.parent.mkdir(parents=True, exist_ok=True)
        log(f"cloning {repo} -> {clone}")
        subprocess.run(["git", "clone", "--quiet", "--shared", str(repo), str(clone)], check=True)
    elif refresh:
        subprocess.run(["git", "-C", str(clone), "pull", "--quiet", "--ff-only"], check=True)
    # This module holds the task files (questions and expected answers). Once it is committed, a clone of
    # HEAD contains it, so it is removed from the working tree: no arm may read the answer key.
    shutil.rmtree(clone / "framework" / "ai-sdlc" / "graph-db", ignore_errors=True)
    profile = spec["profile"].read_text(encoding="utf-8")
    profile = re.sub(r'(?m)^db\s*=.*$', f'db = "{clone.name}.db"', profile)
    profile = re.sub(r'(?ms)^\[\[source\]\].*?(?=^\[)', "", profile)
    profile += f'\n[[source]]\nname = "{spec.get("name", clone.name)}"\npath = "{clone.name}"\n'
    spec["config"].write_text(profile, encoding="utf-8")
    return spec["config"]


def head(spec: dict) -> str:
    if spec.get("prebuilt"):
        return ",".join(sorted(p.name for p in spec["clone"].iterdir() if (p / ".git").exists()))
    return subprocess.run(["git", "-C", str(spec["clone"]), "rev-parse", "--short", "HEAD"],
                          capture_output=True, text=True, check=True).stdout.strip()


def ensure_index(spec: dict, log=print) -> None:
    """Index the clone. A missing store is seeded from the profile's store (same project name), so
    unchanged files and their embeddings are reused instead of recomputed."""
    import sqlite3

    if spec.get("prebuilt"):
        return
    from repolens.config import load_config
    from repolens.service import KnowledgeGraph

    target = load_config(spec["config"]).db_path
    seed = load_config(spec["profile"]).db_path
    if not target.exists() and seed.exists():
        log(f"seeding {target.name} from {seed}")
        src, dst = sqlite3.connect(seed), sqlite3.connect(target)
        src.backup(dst)
        src.close()
        dst.close()
    kg = KnowledgeGraph.open(spec["config"])
    report = kg.index(log=log)
    log(f"index: {report['stats']} in {report['timings']['total_s']}s")
    kg.close()
