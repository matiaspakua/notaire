"""Config-as-code: tree listing and file read with redaction (#2398).

Lists files from configured config roots (profile, policy, context files,
catalogs dir, skills dir, evidence dir) and serves individual file content
with secret redaction. Path traversal and symlink escapes are refused.
"""
from __future__ import annotations

import mimetypes
import re
from pathlib import Path
from typing import Any

from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import SECRET_VALUES

ID_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$")


def _media_type(path: Path) -> str:
    ct, _ = mimetypes.guess_type(str(path))
    if ct:
        return ct
    suffix = path.suffix.lower()
    table = {
        ".md": "text/markdown; charset=utf-8",
        ".toml": "text/toml; charset=utf-8",
        ".json": "application/json",
        ".yml": "text/yaml; charset=utf-8",
        ".yaml": "text/yaml; charset=utf-8",
        ".txt": "text/plain; charset=utf-8",
        ".py": "text/x-python; charset=utf-8",
        ".sh": "text/x-shellscript; charset=utf-8",
    }
    return table.get(suffix, "text/plain; charset=utf-8")


def _redact_text(text: str) -> tuple[str, bool]:
    """Return text with secrets replaced by *** and whether any were redacted."""
    redacted = False

    def _replace(m: re.Match) -> str:
        nonlocal redacted
        redacted = True
        return "***"

    out = SECRET_VALUES.sub(_replace, text)
    return out, redacted


def _assert_config_path(raw: str) -> str:
    """Validate a config file path — refuse traversal, absolute, encoded separators."""
    if not raw:
        raise KitError("invalid_id", "empty path")
    if raw.startswith(("/", "..")):
        raise KitError("invalid_id", f"invalid path {raw!r}")
    if "/" in raw:
        for segment in raw.split("/"):
            if not segment or segment == ".." or segment.startswith("."):
                raise KitError("invalid_id", f"invalid path segment {segment!r}")
    elif not ID_RE.fullmatch(raw):
        raise KitError("invalid_id", f"invalid id {raw!r}")
    if "\\" in raw:
        raise KitError("invalid_id", f"invalid path {raw!r}")
    return raw


def list_config_files(ctx: Context) -> list[dict[str, Any]]:
    """Return a tree of config-root files with metadata."""
    rows: list[dict[str, Any]] = []
    seen: set[str] = set()

    def _add(path: Path, kind: str) -> None:
        if not path.is_file():
            return
        real = path.resolve()
        key = str(real)
        if key in seen:
            return
        seen.add(key)
        try:
            size = path.stat().st_size
        except OSError:
            return
        rel = str(path.relative_to(ctx.root)) if path.is_relative_to(ctx.root) else str(path.name)
        rows.append({
            "path": rel,
            "kind": kind,
            "size": size,
            "media_type": _media_type(path),
        })

    profile = ctx.profile
    if profile.name:
        _add(ctx.profile_path, "profile")
    if getattr(profile, "policy", None):
        _add(ctx.path(str(profile.policy)), "policy")
    context_files = getattr(profile, "context_files", None) or getattr(profile, "context", None)
    if isinstance(context_files, list):
        for cf in context_files:
            _add(ctx.path(str(cf)), "context")
    elif isinstance(context_files, dict):
        for cf in context_files.get("files", []):
            _add(ctx.path(str(cf)), "context")
    catalogs_dir = getattr(profile, "catalogs_dir", None)
    if catalogs_dir:
        root = ctx.path(str(catalogs_dir))
        if root.is_dir():
            for p in sorted(root.rglob("*.json")):
                if p.is_file():
                    _add(p, "catalog")
    skills_dir = getattr(profile, "skills_dir", None)
    if skills_dir:
        root = ctx.path(str(skills_dir))
        if root.is_dir():
            for p in sorted(root.rglob("SKILL.md")):
                if p.is_file():
                    _add(p, "skill")
    evidence_dir = getattr(profile, "evidence_dir", None)
    if evidence_dir:
        root = ctx.path(str(evidence_dir))
        if root.is_dir():
            for p in sorted(root.rglob("*.jsonl")):
                if p.is_file():
                    _add(p, "evidence")

    rows.sort(key=lambda r: r["path"])
    return rows


def read_config_file(ctx: Context, raw_path: str) -> dict[str, Any]:
    """Read a config file with path confinement, symlink check, and redaction."""
    rel = _assert_config_path(raw_path)
    target = ctx.root / rel

    parts = rel.split("/")
    if len(parts) > 1:
        first_dir = ctx.root / parts[0]
        if not first_dir.is_dir():
            raise KitError("invalid_id", f"invalid path {rel!r}")

    if target.is_symlink():
        raise KitError("invalid_id", f"symlink not allowed: {rel}")

    resolved = target.resolve()
    root_resolved = ctx.root.resolve()
    if root_resolved not in resolved.parents and resolved != root_resolved:
        raise KitError("invalid_id", f"path escapes project root: {rel}")

    for part in parts[:-1]:
        segment = ctx.root / "/".join(parts[:parts.index(part) + 1]) if part else ctx.root
        if segment.is_symlink():
            link_target = segment.resolve()
            if root_resolved not in link_target.parents and link_target != root_resolved:
                raise KitError("invalid_id", f"symlink escapes project root: {rel}")

    if not resolved.is_file():
        raise KitError("not_found", f"config file not found: {rel}")

    raw_bytes = resolved.read_bytes()
    try:
        text = raw_bytes.decode("utf-8")
    except UnicodeDecodeError:
        raise KitError("invalid_id", f"non-utf-8 file: {rel}")

    redacted_text, was_redacted = _redact_text(text)

    return {
        "path": rel,
        "content": redacted_text,
        "redacted": was_redacted,
        "media_type": _media_type(resolved),
        "size": len(raw_bytes),
    }
