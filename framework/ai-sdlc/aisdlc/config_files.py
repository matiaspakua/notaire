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

    # JSON: parse, redact keys, dump back.
    if "json" in media_type:
        try:
            obj = json.loads(text)
            redacted_obj = factory_redact("", obj)
            # Detect if anything changed
            was_redacted = _json_changed(text, redacted_obj)
            return json.dumps(redacted_obj, indent=2) + "\n", was_redacted
        except (json.JSONDecodeError, TypeError):
            pass  # fall through to text-based redaction

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


def _json_changed(original: str, redacted_obj: Any) -> bool:
    """Check if redaction changed anything in the JSON."""
    redacted_str = json.dumps(redacted_obj, indent=2, sort_keys=True)
    try:
        original_obj = json.loads(original)
        original_str = json.dumps(original_obj, indent=2, sort_keys=True)
    except (json.JSONDecodeError, TypeError):
        return True
    return redacted_str != original_str


def _redact_key_value_line(line: str) -> tuple[str, bool]:
    """Redact a key = value or key: value line if the key matches SECRET_KEYS."""
    # Match 'key = value' or 'key: value' patterns
    m = re.match(r'^(\s*\w+)\s*[=:]\s*(.*)$', line)
    if m:
        key = m.group(1).strip()
        value = m.group(2).strip()
        # Strip quotes
        if (value.startswith('"') and value.endswith('"')) or \
           (value.startswith("'") and value.endswith("'")):
            value_inner = value[1:-1]
        else:
            value_inner = value
        # Skip env:NAME references
        if value_inner.startswith("env:"):
            return line, False
        if SECRET_KEYS.search(key):
            # Preserve the quote style
            if value.startswith('"'):
                return f'{m.group(1)} = "***"', True
            elif value.startswith("'"):
                return f"{m.group(1)} = '***'", True
            else:
                return f'{m.group(1)} = "***"', True
    return line, False


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


def _config_entries(ctx: Context):
    """Yield (path, kind, read_only) for every config-root file.

    Single source of truth for both listing and read-confinement.
    Uses the real profile fields directly (no getattr / dict fallbacks).
    Every yielded file has its resolved path inside the repository root.
    """
    root_resolved = ctx.root.resolve()
    profile = ctx.profile

    def _yield_file(path: Path, kind: str, *, read_only: bool = False) -> None:
        if not path.is_file():
            return
        resolved = path.resolve()
        if root_resolved not in resolved.parents and resolved != root_resolved:
            return  # resolved target outside repo root — skip
        yield (path, kind, read_only)

    # Profile file
    if ctx.profile_path.is_file():
        for entry in _yield_file(ctx.profile_path, "profile"):
            yield entry

    # Policy
    if profile.policy:
        for entry in _yield_file(ctx.path(profile.policy), "policy"):
            yield entry

    # Context files (tuple of str)
    for cf in profile.context_files:
        for entry in _yield_file(ctx.path(cf), "context"):
            yield entry

    # Catalogs dir
    if profile.catalogs_dir:
        catalogs_root = ctx.path(profile.catalogs_dir)
        if catalogs_root.is_dir():
            for p in sorted(catalogs_root.rglob("*.json")):
                for entry in _yield_file(p, "catalog"):
                    yield entry

    # Projects dir (.aisdlc/projects)
    projects_dir = ctx.path(".aisdlc/projects")
    if projects_dir.is_dir():
        for p in sorted(projects_dir.rglob("*.json")):
            for entry in _yield_file(p, "project"):
                yield entry

    # Skills dir — read_only rows
    if profile.skills_dir:
        skills_root = ctx.path(profile.skills_dir)
        if skills_root.is_dir():
            for p in sorted(skills_root.rglob("SKILL.md")):
                for entry in _yield_file(p, "skill", read_only=True):
                    yield entry


def _build_config_root_set(ctx: Context) -> set[Path]:
    """Build the set of resolved config-root paths for containment checks,
    derived from the same enumeration as _config_entries."""
    roots: set[Path] = set()
    root_resolved = ctx.root.resolve()
    profile = ctx.profile

    if ctx.profile_path.is_file():
        resolved = ctx.profile_path.resolve()
        if root_resolved in resolved.parents or resolved == root_resolved:
            roots.add(resolved)

    if profile.policy:
        p = ctx.path(profile.policy)
        if p.is_file():
            resolved = p.resolve()
            if root_resolved in resolved.parents or resolved == root_resolved:
                roots.add(resolved)

    for cf in profile.context_files:
        p = ctx.path(cf)
        if p.is_file():
            resolved = p.resolve()
            if root_resolved in resolved.parents or resolved == root_resolved:
                roots.add(resolved)

    if profile.catalogs_dir:
        p = ctx.path(profile.catalogs_dir)
        if p.is_dir():
            roots.add(p.resolve())

    projects_dir = ctx.path(".aisdlc/projects")
    if projects_dir.is_dir():
        roots.add(projects_dir.resolve())

    if profile.skills_dir:
        p = ctx.path(profile.skills_dir)
        if p.is_dir():
            roots.add(p.resolve())

    return roots


def _inside_config_roots(resolved: Path, roots: set[Path]) -> bool:
    """Check if resolved path is inside one of the config roots."""
    for root in roots:
        if resolved == root or root in resolved.parents:
            return True
    return False


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
    # Symlink must stay inside repo root
    if resolved != target and (root_resolved not in resolved.parents and resolved != root_resolved):
        raise KitError("invalid_id", f"symlink escapes project root: {rel}")

    # Must be inside a config root
    config_roots = _build_config_root_set(ctx)
    if not _inside_config_roots(resolved, config_roots):
        raise KitError("not_found", f"config file not found: {rel}")

    if not resolved.is_file():
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
