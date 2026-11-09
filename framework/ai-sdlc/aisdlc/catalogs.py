"""Catalogs CRUD: skills, MCP, agents, memory as versioned config-as-code (#2139).

Entries are JSON files under `[catalogs] dir` / `{kind}/{id}.json`. List and validate only
read files — they never execute commands or import entry code. Digests (SHA-256 of the
canonical JSON without the digest field) are refreshed on add/update.
"""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

from aisdlc.context import Context
from aisdlc.profile import ProfileError

KINDS = ("skills", "mcp", "agents", "memory")


def _catalog_root(ctx: Context) -> Path | None:
    settings = getattr(ctx.profile, "catalogs_dir", None)
    if not settings:
        return None
    return ctx.path(settings)


def _require_root(ctx: Context) -> Path:
    root = _catalog_root(ctx)
    if root is None:
        raise ProfileError("no catalogs configured ([catalogs] dir)")
    return root


def _kind_dir(ctx: Context, kind: str) -> Path:
    if kind not in KINDS:
        raise ProfileError(f"unknown catalog kind {kind!r} (expected {', '.join(KINDS)})")
    return _require_root(ctx) / kind


def _entry_path(ctx: Context, kind: str, entry_id: str) -> Path:
    if not entry_id or "/" in entry_id or "\\" in entry_id or entry_id.startswith("."):
        raise ProfileError(f"invalid catalog id {entry_id!r}")
    return _kind_dir(ctx, kind) / f"{entry_id}.json"


def _digest(payload: dict) -> str:
    body = {k: v for k, v in sorted(payload.items()) if k != "digest"}
    blob = json.dumps(body, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(blob).hexdigest()


def _write(path: Path, payload: dict) -> dict:
    payload = dict(payload)
    payload["digest"] = _digest(payload)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return payload


def _read(path: Path) -> dict:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or "id" not in data:
        raise ProfileError(f"catalog entry {path} must be a JSON object with id")
    return data


def _discover_skills_dir(ctx: Context) -> list[dict]:
    """Read-only overlay from `[skills] dir` folders that contain SKILL.md. Never executes."""
    if not ctx.profile.skills_dir:
        return []
    catalog = ctx.path(ctx.profile.skills_dir)
    if not catalog.is_dir():
        return []
    entries = []
    for folder in sorted(p for p in catalog.iterdir() if p.is_dir() and not p.name.startswith(".")):
        skill_md = folder / "SKILL.md"
        if not skill_md.is_file():
            continue
        try:
            from aisdlc.skills import frontmatter

            meta = frontmatter(skill_md.read_text(encoding="utf-8"))
        except OSError:
            meta = {}
        rel = f"{ctx.profile.skills_dir.rstrip('/')}/{folder.name}"
        entries.append(
            {
                "id": meta.get("name") or folder.name,
                "kind": "skills",
                "ref": rel,
                "summary": meta.get("description") or "",
                "digest": None,
                "source": "skills-dir",
            }
        )
    return entries


def list_entries(ctx: Context, kind: str | None = None) -> dict:
    kinds = (kind,) if kind else KINDS
    if kind and kind not in KINDS:
        raise ProfileError(f"unknown catalog kind {kind!r} (expected {', '.join(KINDS)})")
    root = _catalog_root(ctx)
    result: dict = {}
    for item in kinds:
        entries = []
        seen: set[str] = set()
        folder = (root / item) if root is not None else None
        if folder is not None and folder.is_dir():
            for path in sorted(folder.glob("*.json")):
                try:
                    data = _read(path)
                except (OSError, json.JSONDecodeError, ProfileError):
                    entries.append({"id": path.stem, "error": "unreadable", "source": "catalog"})
                    continue
                entry_id = data["id"]
                seen.add(entry_id)
                entries.append(
                    {
                        "id": entry_id,
                        "kind": item,
                        "ref": data.get("ref"),
                        "summary": data.get("summary"),
                        "digest": data.get("digest"),
                        "source": "catalog",
                    }
                )
        if item == "skills":
            for discovered in _discover_skills_dir(ctx):
                if discovered["id"] in seen:
                    continue
                entries.append(discovered)
        result[item] = {"kind": item, "entries": entries}
    if kind:
        return result[kind]
    return result


def add(ctx: Context, kind: str, entry_id: str, *, ref: str | None, summary: str | None) -> int:
    path = _entry_path(ctx, kind, entry_id)
    if path.exists():
        print(f"error: catalog entry {kind}/{entry_id} already exists (use update)")
        return 1
    payload = {"id": entry_id, "kind": kind, "ref": ref, "summary": summary or ""}
    _write(path, payload)
    print(f"catalog add: {kind}/{entry_id}")
    return 0


def update(ctx: Context, kind: str, entry_id: str, *, ref: str | None, summary: str | None) -> int:
    path = _entry_path(ctx, kind, entry_id)
    if not path.is_file():
        print(f"error: catalog entry {kind}/{entry_id} not found")
        return 1
    data = _read(path)
    if ref is not None:
        data["ref"] = ref
    if summary is not None:
        data["summary"] = summary
    _write(path, data)
    print(f"catalog update: {kind}/{entry_id}")
    return 0


def remove(ctx: Context, kind: str, entry_id: str) -> int:
    path = _entry_path(ctx, kind, entry_id)
    if not path.is_file():
        print(f"error: catalog entry {kind}/{entry_id} not found")
        return 1
    path.unlink()
    print(f"catalog remove: {kind}/{entry_id}")
    return 0


def validate(ctx: Context, kind: str | None = None) -> int:
    """Read-only integrity: digests match; skills refs exist when kind=skills. Never executes."""
    try:
        listed = list_entries(ctx, kind)
    except ProfileError as exc:
        print(f"error: {exc}")
        return 2
    rows = [listed] if kind else list(listed.values())
    errors: list[str] = []
    for block in rows:
        k = block["kind"]
        for entry in block["entries"]:
            if entry.get("source") == "skills-dir":
                # Virtual discover rows — list-only; never require a digest file.
                continue
            if entry.get("error"):
                errors.append(f"{k}/{entry['id']}: {entry['error']}")
                continue
            path = _entry_path(ctx, k, entry["id"])
            data = _read(path)
            expected = _digest(data)
            if data.get("digest") != expected:
                errors.append(f"{k}/{entry['id']}: digest mismatch (file edited outside catalog CLI)")
            if k == "skills" and data.get("ref"):
                ref_path = ctx.path(str(data["ref"]))
                if not (ref_path / "SKILL.md").is_file() and not ref_path.is_file():
                    errors.append(f"{k}/{entry['id']}: ref {data['ref']} not found")
    for error in errors:
        print(f"  FAIL  {error}")
    total = sum(len(block["entries"]) for block in rows)
    print(f"catalogs: {total} entr{'y' if total == 1 else 'ies'} — {'FAILED' if errors else 'PASSED'}")
    return 1 if errors else 0
