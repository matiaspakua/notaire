"""`repolens setup` and `repolens doctor`: wire repolens into a repository and its agent harnesses, then check
the wiring. Standard library only (it must run before the dependencies are installed).

Every writer is idempotent and merges into existing files (other servers, hooks and keys are kept):

  claude     .mcp.json (server), .claude/settings.json (enable the server, allow its read-only tools, and the
             PostToolUse usage hook)
  codex      .codex/config.toml  [mcp_servers.repolens]
  opencode   opencode.json "mcp" (path from `[harness] opencode` in repolens.toml, default ./opencode.json)
  cursor     .cursor/mcp.json
  git        post-checkout / post-merge hooks: refresh the index in the background after a branch switch or a
             pull (in core.hooksPath when set, else .git/hooks)
  gitignore  `.repolens/` (store, usage log, lock) is never committed

The server is started through a command that works from any worktree of the repository: when repolens lives
inside the repository its launcher is resolved with `git rev-parse --show-toplevel`; otherwise `repolens` from
PATH (pipx / pip install).
"""

from __future__ import annotations

import importlib.util
import json
import os
import shutil
import sqlite3
import subprocess
import sys
import time
import tomllib
from pathlib import Path

from .paths import CONFIG_NAME, STATE_DIR, find_config, git_root, home, project_root, usage_log

TARGETS = ("claude", "codex", "opencode", "cursor", "git", "gitignore")
HOOK_MATCHER = "Read|Grep|Glob|mcp__repolens__.*"
DEPENDENCIES = ("numpy", "scipy", "tree_sitter", "tree_sitter_language_pack", "yaml", "fastembed", "mcp")
MODULE_DIR = Path(__file__).resolve().parent.parent


# -- how a harness starts repolens ---------------------------------------------------------------------------
def launcher(root: Path) -> tuple[str, list[str]] | None:
    """(command, leading args) for the launcher; None when repolens is installed on PATH instead."""
    script = MODULE_DIR / "bin" / "repolens"
    try:
        rel = script.relative_to(root).as_posix()
    except ValueError:
        return None
    return "bash", ["-c", f'exec "$(git rev-parse --show-toplevel)/{rel}" "$@"', "repolens"]


def command(root: Path, *args: str) -> tuple[str, list[str]]:
    found = launcher(root)
    if found is None:
        return "repolens", list(args)
    cmd, lead = found
    return cmd, [*lead, *args]


def shell_command(root: Path, *args: str) -> str:
    """The same command as one shell line (git hooks, Claude Code hooks)."""
    found = launcher(root)
    if found is None:
        return " ".join(["repolens", *args])
    rel = (MODULE_DIR / "bin" / "repolens").relative_to(root).as_posix()
    return f'bash "$(git rev-parse --show-toplevel)/{rel}" ' + " ".join(args)


# -- writers ------------------------------------------------------------------------------------------------
def _load_json(path: Path) -> dict:
    if not path.exists():
        return {}
    text = path.read_text(encoding="utf-8").strip()
    return json.loads(text) if text else {}


def _write_json(path: Path, data: dict, dry_run: bool) -> bool:
    new = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
    if path.exists() and path.read_text(encoding="utf-8") == new:
        return False
    if not dry_run:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(new, encoding="utf-8")
    return True


def _server(root: Path) -> dict:
    cmd, args = command(root, "mcp")
    return {"command": cmd, "args": args}


def setup_claude(root: Path, dry_run: bool) -> list[str]:
    changed = []
    mcp_path = root / ".mcp.json"
    data = _load_json(mcp_path)
    data.setdefault("mcpServers", {})["repolens"] = {"type": "stdio", **_server(root)}
    if _write_json(mcp_path, data, dry_run):
        changed.append(".mcp.json")
    settings_path = root / ".claude" / "settings.json"
    settings = _load_json(settings_path)
    enabled = settings.setdefault("enabledMcpjsonServers", [])
    if "repolens" not in enabled:
        enabled.append("repolens")
    allow = settings.setdefault("permissions", {}).setdefault("allow", [])
    if "mcp__repolens" not in allow:
        allow.append("mcp__repolens")
    post = settings.setdefault("hooks", {}).setdefault("PostToolUse", [])
    hook_cmd = shell_command(root, "hook")
    entry = next((h for h in post if h.get("matcher") == HOOK_MATCHER), None)
    wanted = {"matcher": HOOK_MATCHER, "hooks": [{"type": "command", "command": hook_cmd, "timeout": 5}]}
    if entry is None:
        post.append(wanted)
    else:
        entry.update(wanted)
    if _write_json(settings_path, settings, dry_run):
        changed.append(".claude/settings.json")
    return changed


def setup_cursor(root: Path, dry_run: bool) -> list[str]:
    path = root / ".cursor" / "mcp.json"
    data = _load_json(path)
    data.setdefault("mcpServers", {})["repolens"] = _server(root)
    return [".cursor/mcp.json"] if _write_json(path, data, dry_run) else []


def setup_codex(root: Path, dry_run: bool) -> list[str]:
    path = root / ".codex" / "config.toml"
    text = path.read_text(encoding="utf-8") if path.exists() else ""
    cmd, args = command(root, "mcp")
    block = (f"[mcp_servers.repolens]\ncommand = {json.dumps(cmd)}\n"
             f"args = [{', '.join(json.dumps(a) for a in args)}]\n")
    if "[mcp_servers.repolens]" in text:
        start = text.index("[mcp_servers.repolens]")
        nxt = text.find("\n[", start + 1)
        current = text[start: nxt + 1 if nxt != -1 else len(text)]
        if current.strip() == block.strip():
            return []
        text = text[:start] + block + (text[nxt + 1:] if nxt != -1 else "")
    else:
        text = (text.rstrip("\n") + "\n\n" if text.strip() else "") + block
    if not dry_run:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
    return [".codex/config.toml"]


def setup_opencode(root: Path, dry_run: bool) -> list[str]:
    path = root / _harness_setting(root, "opencode", "opencode.json")
    data = _load_json(path)
    cmd, args = command(root, "mcp")
    data.setdefault("mcp", {})["repolens"] = {"type": "local", "command": [cmd, *args], "enabled": True}
    return [path.relative_to(root).as_posix()] if _write_json(path, data, dry_run) else []


def setup_git(root: Path, dry_run: bool) -> list[str]:
    hooks_dir = _hooks_dir(root)
    if hooks_dir is None:
        return []
    line = shell_command(root, "refresh", "--background") + " >/dev/null 2>&1 || true"
    changed = []
    for name in ("post-checkout", "post-merge"):
        path = hooks_dir / name
        text = path.read_text(encoding="utf-8") if path.exists() else ""
        if "repolens" in text:
            continue
        header = "" if text else ("#!/usr/bin/env bash\n# Keep the repolens index of this worktree current after a "
                                  "branch switch or a pull.\n# Never blocks git: runs in the background and never "
                                  "fails.\n")
        new = (text.rstrip("\n") + "\n" if text else header) + line + "\n"
        if not dry_run:
            hooks_dir.mkdir(parents=True, exist_ok=True)
            path.write_text(new, encoding="utf-8")
            path.chmod(0o755)
        changed.append(path.relative_to(root).as_posix() if path.is_relative_to(root) else str(path))
    return changed


def setup_gitignore(root: Path, dry_run: bool) -> list[str]:
    path = root / ".gitignore"
    text = path.read_text(encoding="utf-8") if path.exists() else ""
    if any(line.strip() in (f"{STATE_DIR}/", STATE_DIR, f"/{STATE_DIR}/") for line in text.splitlines()):
        return []
    if not dry_run:
        path.write_text((text.rstrip("\n") + "\n" if text else "") + f"# repolens store, usage log and lock\n"
                        f"{STATE_DIR}/\n", encoding="utf-8")
    return [".gitignore"]


WRITERS = {"claude": setup_claude, "codex": setup_codex, "opencode": setup_opencode, "cursor": setup_cursor,
           "git": setup_git, "gitignore": setup_gitignore}


def setup(targets: list[str], dry_run: bool = False, start: Path | None = None) -> dict[str, list[str]]:
    root = project_root(start)
    return {target: WRITERS[target](root, dry_run) for target in targets}


def install_dependencies(log=print) -> int:
    """Create the shared environment `$REPOLENS_HOME/venv` with repolens's dependencies (all worktrees and
    repositories use it; the launcher prefers it). The package itself is not installed: each worktree runs
    its own checkout of the code."""
    venv = home() / "venv"
    python = venv / ("Scripts/python.exe" if os.name == "nt" else "bin/python")
    if not python.exists():
        log(f"creating {venv}")
        subprocess.run([sys.executable, "-m", "venv", str(venv)], check=True)
    meta = tomllib.loads((MODULE_DIR / "pyproject.toml").read_text(encoding="utf-8"))["project"]
    packages = list(meta["dependencies"]) + list(meta["optional-dependencies"]["all"])
    log(f"installing {len(packages)} packages into {venv}")
    return subprocess.run([str(python), "-m", "pip", "install", "--quiet", "--upgrade", *packages]).returncode


# -- doctor -------------------------------------------------------------------------------------------------
def doctor(start: Path | None = None) -> tuple[int, list[str]]:
    """Check what an agent needs: dependencies, config, a fresh store, harness wiring, telemetry."""
    root = project_root(start)
    lines: list[str] = []
    failures = 0

    def check(ok: bool | None, text: str) -> None:
        nonlocal failures
        mark = {True: "ok  ", False: "FAIL", None: "warn"}[ok]
        failures += ok is False
        lines.append(f"[{mark}] {text}")

    missing = [m for m in DEPENDENCIES if importlib.util.find_spec(m) is None]
    check(not missing, "dependencies" + (f" missing: {', '.join(missing)} -> repolens setup --install" if missing
                                         else f" importable ({sys.executable})"))
    config = find_config(start)
    check(True if config else None, f"config {config}" if config else f"no {CONFIG_NAME}: indexing {root} with "
          "defaults")
    db = _store_path(root, config)
    if db.exists():
        con = sqlite3.connect(f"file:{db}?mode=ro", uri=True)
        meta = dict(con.execute("SELECT key, value FROM meta WHERE key IN ('last_index', 'embedder')"))
        files = con.execute("SELECT COUNT(*) FROM files").fetchone()[0]
        vectors = con.execute("SELECT COUNT(*) FROM vectors").fetchone()[0]
        con.close()
        age = time.time() - float(meta.get("last_index") or 0)
        check(age < 86400 or None, f"store {db}: {files} files, {vectors} vectors ({meta.get('embedder')}), "
              f"indexed {age / 60:.0f} min ago")
    else:
        check(None, f"no store yet at {db}: the MCP server builds it on first start (or run `repolens index`)")
    for target, probe in _wiring(root).items():
        check(probe or None, f"{target}: " + ("registered" if probe else f"not registered -> repolens setup "
                                                                       f"--only {target}"))
    cache = home() / "vectors"
    caches = sorted(cache.glob("*.db")) if cache.is_dir() else []
    check(True, f"vector cache {cache}: " + (", ".join(f"{p.stem} {p.stat().st_size / 1e6:.0f} MB" for p in caches)
                                             or "empty (filled by the first full embedding)"))
    log = usage_log(db.parent)
    check(True if log.exists() else None, f"usage log {log}" + ("" if log.exists() else " (no calls yet)"))
    return (1 if failures else 0), lines


def _wiring(root: Path) -> dict[str, bool]:
    def has(path: Path, needle: str) -> bool:
        return path.exists() and needle in path.read_text(encoding="utf-8", errors="replace")

    settings = root / ".claude" / "settings.json"
    hooks_dir = _hooks_dir(root)
    return {
        "claude": has(root / ".mcp.json", '"repolens"') and has(settings, "repolens"),
        "codex": has(root / ".codex" / "config.toml", "[mcp_servers.repolens]"),
        "opencode": has(root / _harness_setting(root, "opencode", "opencode.json"), '"repolens"'),
        "cursor": has(root / ".cursor" / "mcp.json", '"repolens"'),
        "git": hooks_dir is not None and has(hooks_dir / "post-checkout", "repolens"),
        "gitignore": has(root / ".gitignore", f"{STATE_DIR}/"),
    }


def _store_path(root: Path, config: Path | None) -> Path:
    if config is not None:
        db = tomllib.loads(config.read_text(encoding="utf-8")).get("index", {}).get("db")
        if db:
            path = Path(os.path.expanduser(db))
            return path if path.is_absolute() else (config.parent / path).resolve()
    return root / STATE_DIR / "graph.db"


def _harness_setting(root: Path, key: str, default: str) -> str:
    config = find_config(root)
    if config is None:
        return default
    return str(tomllib.loads(config.read_text(encoding="utf-8")).get("harness", {}).get(key, default))


def _hooks_dir(root: Path) -> Path | None:
    if git_root(root) is None or shutil.which("git") is None:
        return None
    out = subprocess.run(["git", "-C", str(root), "config", "--get", "core.hooksPath"], capture_output=True,
                         text=True).stdout.strip()
    if out:
        return (root / out).resolve()
    common = subprocess.run(["git", "-C", str(root), "rev-parse", "--git-common-dir"], capture_output=True,
                            text=True).stdout.strip()
    return (root / common / "hooks").resolve() if common else None
