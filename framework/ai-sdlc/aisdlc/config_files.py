"""Config-as-code: tree listing and file read with redaction (#2398).

Lists files from the profile's config roots (profile file, policy, context
files, catalogs dir, projects dir, skills dir) and serves individual file
content with secret redaction.  Paths are confined to those roots — a file
inside the repository but outside every config root is refused.
Symlinks whose resolved target leaves the repository root are skipped.
"""
from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import SECRET_KEYS, SECRET_VALUES
from aisdlc.factory import redact as factory_redact

# Explicit extension → media type table (no stdlib mimetypes dependency).
_MEDIA: dict[str, str] = {
    ".toml": "text/toml; charset=utf-8",
    ".json": "application/json",
    ".yaml": "text/yaml; charset=utf-8",
    ".yml": "text/yaml; charset=utf-8",
    ".md": "text/markdown; charset=utf-8",
    ".txt": "text/plain; charset=utf-8",
}


def _media_type(path: Path) -> str:
    return _MEDIA.get(path.suffix.lower(), "text/plain; charset=utf-8")


def _redact_text(text: str, media_type: str) -> tuple[str, bool]:
    """Return text with secrets replaced by *** and whether any were redacted.

    For JSON content, parse and apply factory_redact (key-aware).
    For TOML/YAML/INI-like lines, redact key = value / key: value pairs
    whose key matches SECRET_KEYS.  Always substitute SECRET_VALUES tokens.
    """
    was_redacted = False

    if "json" in media_type:
        try:
            obj = json.loads(text)
            redacted_obj = factory_redact("", obj)
            was_redacted = redacted_obj != obj
            return json.dumps(redacted_obj, indent=2) + "\n", was_redacted
        except (json.JSONDecodeError, TypeError):
            pass

    lines = text.split("\n")
    out_lines: list[str] = []
    for line in lines:
        new_line, changed = _redact_key_value_line(line)
        if changed:
            was_redacted = True
        new_line = SECRET_VALUES.sub("***", new_line)
        if new_line != line and not changed:
            was_redacted = True
        out_lines.append(new_line)
    return "\n".join(out_lines), was_redacted


def _redact_key_value_line(line: str) -> tuple[str, bool]:
    m = re.match(r"^(\s*\w+)\s*[=:]\s*(.*)$", line)
    if not m:
        return line, False
    key = m.group(1).strip()
    value = m.group(2).strip()
    quote = value[0] if value and value[0] in ('"', "'") else None
    inner = value[1:-1] if quote and value.endswith(quote) else value
    if inner.startswith("env:"):
        return line, False
    if not SECRET_KEYS.search(key):
        return line, False
    q = quote if quote else '"'
    return f"{m.group(1)} = {q}***{q}", True


def _guard_path(raw: str) -> str:
    """Reuse serve._guard_path semantics: reject traversal and encoded separators.

    Allows ``.aisdlc`` as a segment since it is the standard config root prefix.
    """
    if not raw:
        raise KitError("invalid_id", "empty path")
    if "%2f" in raw.casefold() or "%5c" in raw.casefold():
        raise KitError("invalid_id", "encoded path separator refused")
    if raw.startswith(("/", "..")):
        raise KitError("invalid_id", f"invalid path {raw!r}")
    if "\\" in raw:
        raise KitError("invalid_id", f"invalid path {raw!r}")
    for part in raw.split("/"):
        if not part:
            continue
        if part == "..":
            raise KitError("invalid_id", f"invalid path segment {part!r}")
        if part.startswith(".") and part != ".aisdlc":
            raise KitError("invalid_id", f"invalid path segment {part!r}")
    return raw


def _inside_root(path: Path, root_resolved: Path) -> bool:
    """Return True when *path* resolves inside *root_resolved*."""
    resolved = path.resolve()
    return resolved == root_resolved or root_resolved in resolved.parents


def _config_entries(ctx: Context) -> list[tuple[Path, str, bool]]:
    """Return every config-root file as (path, kind, read_only)."""
    root_resolved = ctx.root.resolve()
    profile = ctx.profile

    table: list[tuple[Path | None, str, str, bool]] = []
    table.append((ctx.profile_path, "", "profile", False))
    if profile.policy:
        table.append((ctx.path(profile.policy), "", "policy", False))
    for cf in profile.context_files:
        table.append((ctx.path(cf), "", "context", False))
    if profile.catalogs_dir:
        table.append((ctx.path(profile.catalogs_dir), "*.json", "catalog", False))
    table.append((ctx.path(".aisdlc/projects"), "*.json", "project", False))
    if profile.skills_dir:
        table.append((ctx.path(profile.skills_dir), "SKILL.md", "skill", True))

    entries: list[tuple[Path, str, bool]] = []
    for entry_path, glob_pat, kind, read_only in table:
        if glob_pat:
            if not entry_path.is_dir():
                continue
            for p in sorted(entry_path.rglob(glob_pat)):
                if p.is_file() and _inside_root(p, root_resolved):
                    entries.append((p, kind, read_only))
        else:
            if entry_path.is_file() and _inside_root(entry_path, root_resolved):
                entries.append((entry_path, kind, read_only))
    return entries


def list_config_files(ctx: Context) -> list[dict[str, Any]]:
    """Return a tree of config-root files with metadata."""
    rows: list[dict[str, Any]] = []
    seen: set[str] = set()

    for path, kind, read_only in _config_entries(ctx):
        real = path.resolve()
        key = str(real)
        if key in seen:
            continue
        seen.add(key)
        try:
            size = path.stat().st_size
        except OSError:
            continue
        rel = (str(path.relative_to(ctx.root))
               if path.is_relative_to(ctx.root)
               else str(path.name))
        row: dict[str, Any] = {
            "path": rel,
            "kind": kind,
            "size": size,
            "media_type": _media_type(path),
            "read_only": read_only,
        }
        rows.append(row)

    rows.sort(key=lambda r: r["path"])
    return rows


def read_config_file(ctx: Context, raw_path: str) -> dict[str, Any]:
    """Read a config file confined to config roots, with symlink and redaction checks."""
    rel = _guard_path(raw_path)
    target = ctx.root / rel
    resolved = target.resolve()

    root_resolved = ctx.root.resolve()
    if root_resolved not in resolved.parents and resolved != root_resolved:
        raise KitError("invalid_id", f"symlink escapes project root: {rel}")

    config_set = {p.resolve() for p, _kind, _ro in _config_entries(ctx)}
    if resolved not in config_set:
        raise KitError("not_found", f"config file not found: {rel}")

    raw_bytes = resolved.read_bytes()
    try:
        text = raw_bytes.decode("utf-8")
    except UnicodeDecodeError:
        raise KitError("invalid_id", f"non-utf-8 file: {rel}")

    media = _media_type(resolved)
    redacted_text, was_redacted = _redact_text(text, media)

    return {
        "path": rel,
        "content": redacted_text,
        "redacted": was_redacted,
        "media_type": media,
        "size": len(raw_bytes),
    }
