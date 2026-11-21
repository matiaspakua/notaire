"""Where things live. Standard library only: the harness hook, `report`, `setup` and `doctor` import this
without loading numpy or the embedding model.

  repository   the git work tree containing the working directory (each worktree is its own repository
               here, so an index never mixes branches)
  config       the nearest `repolens.toml` walking up from the working directory
  state        `<config or repository folder>/.repolens/`: the store, the usage log, the index lock
  home         `$REPOLENS_HOME` or `~/.cache/repolens`: what all repositories share (vector cache, venv,
               benchmark data)
"""

from __future__ import annotations

import os
from pathlib import Path

CONFIG_NAME = "repolens.toml"
STATE_DIR = ".repolens"


def home() -> Path:
    return Path(os.environ.get("REPOLENS_HOME") or Path.home() / ".cache" / "repolens").expanduser()


def git_root(start: Path | None = None) -> Path | None:
    """The work tree root (a worktree has a `.git` file, a clone a `.git` folder)."""
    current = (start or Path.cwd()).resolve()
    for folder in (current, *current.parents):
        if (folder / ".git").exists():
            return folder
    return None


def find_config(start: Path | None = None) -> Path | None:
    """The nearest repolens.toml from `start` up to (and including) its repository root."""
    current = (start or Path.cwd()).resolve()
    stop = git_root(current)
    for folder in (current, *current.parents):
        candidate = folder / CONFIG_NAME
        if candidate.is_file():
            return candidate
        if stop is not None and folder == stop:
            return None
    return None


def project_root(start: Path | None = None) -> Path:
    """The folder repolens indexes by default: the config's folder, else the repository, else `start`."""
    config = find_config(start)
    if config is not None:
        return config.parent
    return git_root(start) or (start or Path.cwd()).resolve()


def state_dir(start: Path | None = None) -> Path:
    return project_root(start) / STATE_DIR


def usage_log(state: Path) -> Path:
    return state / "usage.jsonl"
