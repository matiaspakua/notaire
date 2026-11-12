"""Catalogs CRUD with immutable revisions (#2139 / #2239 Phase A / #2254 A.3).

Entries live under ``[catalogs] dir`` / ``{kind}/{id}/``:
``current.json`` (pointer + latest payload) and ``versions/{n}.json`` (immutable).
Legacy flat ``{id}.json`` files remain readable; the next write migrates them.

List/show/history/validate only read files — they never execute commands or import
entry code. Digests are SHA-256 of the canonical JSON without the digest field.
Writes stamp ``schema_version``, ``created_at``, and server-derived ``created_by``.
"""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path

from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.evidence import now as evidence_now
from aisdlc.factory import redact

DEFAULT_SCHEMA_VERSION = 1

KINDS = (
    "skills",
    "mcp",
    "agents",
    "memory",
    "workflows",
    "providers",
    "integrations",
    "rules",
)


def _catalog_root(ctx: Context) -> Path | None:
    settings = getattr(ctx.profile, "catalogs_dir", None)
    if not settings:
        return None
    return ctx.path(settings)


def _require_root(ctx: Context) -> Path:
    root = _catalog_root(ctx)
    if root is None:
        raise KitError("no_catalogs", "no catalogs configured ([catalogs] dir)")
    return root


def _kind_dir(ctx: Context, kind: str) -> Path:
    if kind not in KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r} (expected {', '.join(KINDS)})")
    return _require_root(ctx) / kind


def _assert_id(entry_id: str) -> None:
    if not entry_id or "/" in entry_id or "\\" in entry_id or entry_id.startswith("."):
        raise KitError("invalid_id", f"invalid catalog id {entry_id!r}")


def _entity_dir(ctx: Context, kind: str, entry_id: str) -> Path:
    _assert_id(entry_id)
    return _kind_dir(ctx, kind) / entry_id


def _legacy_path(ctx: Context, kind: str, entry_id: str) -> Path:
    _assert_id(entry_id)
    return _kind_dir(ctx, kind) / f"{entry_id}.json"


def _current_path(ctx: Context, kind: str, entry_id: str) -> Path:
    return _entity_dir(ctx, kind, entry_id) / "current.json"


def _version_path(ctx: Context, kind: str, entry_id: str, version: int) -> Path:
    return _entity_dir(ctx, kind, entry_id) / "versions" / f"{version}.json"


def _digest(payload: dict) -> str:
    body = {k: v for k, v in sorted(payload.items()) if k != "digest"}
    blob = json.dumps(body, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(blob).hexdigest()


def compute_digest(payload: dict) -> str:
    """Public digest of a catalog payload (digest field ignored)."""
    return _digest(payload)


def _write_json(path: Path, payload: dict) -> dict:
    payload = dict(payload)
    payload["digest"] = _digest(payload)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return payload


def _read_json(path: Path) -> dict:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or "id" not in data:
        raise KitError("invalid_entry", f"catalog entry {path} must be a JSON object with id")
    return data


def _discover_skills_dir(ctx: Context) -> list[dict]:
    """Read-only overlay from ``[skills] dir`` folders that contain SKILL.md. Never executes."""
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


def _normalize_legacy(data: dict, kind: str) -> dict:
    payload = dict(data)
    payload.setdefault("kind", kind)
    payload.setdefault("version", 1)
    payload.setdefault("summary", payload.get("summary") or "")
    payload.setdefault("schema_version", DEFAULT_SCHEMA_VERSION)
    return payload


def _stamp_envelope(
    payload: dict,
    *,
    actor: str | None,
    schema_version: int | None,
) -> dict:
    body = dict(payload)
    body.pop("created_by", None)
    body.pop("created_at", None)
    body["schema_version"] = int(
        schema_version if schema_version is not None else body.get("schema_version") or DEFAULT_SCHEMA_VERSION
    )
    # AISDLC_CREATED_AT pins timestamps for golden CLI fixtures.
    body["created_at"] = os.environ.get("AISDLC_CREATED_AT") or evidence_now()
    body["created_by"] = actor or from_env(channel="cli")
    return body


def _exists(ctx: Context, kind: str, entry_id: str) -> bool:
    return _current_path(ctx, kind, entry_id).is_file() or _legacy_path(ctx, kind, entry_id).is_file()


def load_entry(ctx: Context, kind: str, entry_id: str) -> dict:
    """Load the current revision (directory layout or legacy flat file)."""
    if kind not in KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r} (expected {', '.join(KINDS)})")
    current = _current_path(ctx, kind, entry_id)
    if current.is_file():
        return _read_json(current)
    legacy = _legacy_path(ctx, kind, entry_id)
    if legacy.is_file():
        return _normalize_legacy(_read_json(legacy), kind)
    raise KitError("not_found", f"catalog entry {kind}/{entry_id} not found")


def _commit_revision(ctx: Context, kind: str, entry_id: str, payload: dict) -> dict:
    """Write immutable version N and advance current. Never rewrites prior version files."""
    version = int(payload.get("version") or 1)
    payload = dict(payload)
    payload["id"] = entry_id
    payload["kind"] = kind
    payload["version"] = version
    version_file = _version_path(ctx, kind, entry_id, version)
    if version_file.is_file():
        raise KitError("revision_exists", f"catalog revision {kind}/{entry_id}@{version} already exists")
    written = _write_json(version_file, payload)
    _write_json(_current_path(ctx, kind, entry_id), written)
    legacy = _legacy_path(ctx, kind, entry_id)
    if legacy.is_file():
        legacy.unlink()
    return written


def list_entries(ctx: Context, kind: str | None = None) -> dict:
    kinds = (kind,) if kind else KINDS
    if kind and kind not in KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r} (expected {', '.join(KINDS)})")
    root = _catalog_root(ctx)
    result: dict = {}
    for item in kinds:
        entries = []
        seen: set[str] = set()
        folder = (root / item) if root is not None else None
        if folder is not None and folder.is_dir():
            for path in sorted(folder.iterdir()):
                entry_id = None
                try:
                    if path.is_dir() and (path / "current.json").is_file():
                        data = _read_json(path / "current.json")
                        entry_id = data["id"]
                    elif path.is_file() and path.suffix == ".json":
                        data = _normalize_legacy(_read_json(path), item)
                        entry_id = data["id"]
                    else:
                        continue
                except (OSError, json.JSONDecodeError, KitError, KeyError):
                    entries.append({"id": path.stem, "error": "unreadable", "source": "catalog"})
                    continue
                seen.add(entry_id)
                entries.append(
                    {
                        "id": entry_id,
                        "kind": item,
                        "ref": data.get("ref"),
                        "summary": data.get("summary"),
                        "digest": data.get("digest"),
                        "version": data.get("version", 1),
                        "governance": data.get("governance"),
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


def show(ctx: Context, kind: str, entry_id: str, *, redacted: bool = True) -> dict:
    data = load_entry(ctx, kind, entry_id)
    return redact("", data) if redacted else data


def history(ctx: Context, kind: str, entry_id: str) -> dict:
    """List immutable revisions for an entity. Never executes."""
    if not _exists(ctx, kind, entry_id):
        raise KitError("not_found", f"catalog entry {kind}/{entry_id} not found")
    versions: list[dict] = []
    versions_dir = _entity_dir(ctx, kind, entry_id) / "versions"
    if versions_dir.is_dir():
        for path in sorted(versions_dir.glob("*.json"), key=lambda p: int(p.stem)):
            data = _read_json(path)
            versions.append(
                {
                    "version": data.get("version", int(path.stem)),
                    "digest": data.get("digest"),
                    "summary": data.get("summary"),
                    "governance": data.get("governance"),
                }
            )
    else:
        # Legacy flat file: single synthetic revision.
        data = load_entry(ctx, kind, entry_id)
        versions.append(
            {
                "version": data.get("version", 1),
                "digest": data.get("digest"),
                "summary": data.get("summary"),
                "governance": data.get("governance"),
            }
        )
    return {"id": entry_id, "kind": kind, "versions": versions}


def add(
    ctx: Context,
    kind: str,
    entry_id: str,
    *,
    ref: str | None,
    summary: str | None,
    extra: dict | None = None,
    actor: str | None = None,
    schema_version: int | None = None,
) -> dict:
    if _exists(ctx, kind, entry_id):
        raise KitError("already_exists", f"catalog entry {kind}/{entry_id} already exists (use update)")
    payload: dict = {
        "id": entry_id,
        "kind": kind,
        "ref": ref,
        "summary": summary or "",
        "version": 1,
    }
    for key, value in (extra or {}).items():
        if key not in {"id", "kind", "version", "digest", "created_by", "created_at"}:
            payload[key] = value
    payload = _stamp_envelope(payload, actor=actor, schema_version=schema_version)
    return _commit_revision(ctx, kind, entry_id, payload)


def update(
    ctx: Context,
    kind: str,
    entry_id: str,
    *,
    ref: str | None,
    summary: str | None,
    expected_version: int | None = None,
    actor: str | None = None,
) -> dict:
    try:
        data = load_entry(ctx, kind, entry_id)
    except KitError as exc:
        raise KitError("not_found", f"catalog entry {kind}/{entry_id} not found") from exc
    payload = dict(data)
    if ref is not None:
        payload["ref"] = ref
    if summary is not None:
        payload["summary"] = summary
    return write_new_revision(
        ctx,
        kind,
        entry_id,
        payload,
        expected_version=expected_version,
        actor=actor,
    )


def write_new_revision(
    ctx: Context,
    kind: str,
    entry_id: str,
    payload: dict,
    *,
    expected_version: int | None = None,
    actor: str | None = None,
    schema_version: int | None = None,
) -> dict:
    """Library helper: persist payload as the next immutable revision."""
    data = load_entry(ctx, kind, entry_id)
    prior = int(data.get("version") or 1)
    if expected_version is not None and int(expected_version) != prior:
        raise KitError(
            "version_conflict",
            f"version conflict for {kind}/{entry_id}: expected {expected_version}, current {prior}",
        )
    prior_path = _version_path(ctx, kind, entry_id, prior)
    if not prior_path.is_file():
        seed = dict(data)
        seed["version"] = prior
        seed.pop("digest", None)
        _write_json(prior_path, seed)
    next_version = prior + 1
    body = dict(payload)
    body["id"] = entry_id
    body["kind"] = kind
    body["version"] = next_version
    body.pop("digest", None)
    if schema_version is None and body.get("schema_version") is None:
        schema_version = int(data.get("schema_version") or DEFAULT_SCHEMA_VERSION)
    body = _stamp_envelope(body, actor=actor, schema_version=schema_version)
    return _commit_revision(ctx, kind, entry_id, body)


def load_revision(ctx: Context, kind: str, entry_id: str, version: int) -> dict:
    """Load an immutable revision by version number."""
    path = _version_path(ctx, kind, entry_id, int(version))
    if path.is_file():
        return _read_json(path)
    current = load_entry(ctx, kind, entry_id)
    if int(current.get("version") or 1) == int(version):
        return current
    raise KitError("not_found", f"catalog revision {kind}/{entry_id}@{version} not found")


def remove(ctx: Context, kind: str, entry_id: str) -> int:
    current = _current_path(ctx, kind, entry_id)
    legacy = _legacy_path(ctx, kind, entry_id)
    entity = _entity_dir(ctx, kind, entry_id)
    if not current.is_file() and not legacy.is_file() and not entity.is_dir():
        print(f"error: catalog entry {kind}/{entry_id} not found")
        return 1
    if legacy.is_file():
        legacy.unlink()
    if entity.is_dir():
        import shutil

        shutil.rmtree(entity)
    print(f"catalog remove: {kind}/{entry_id}")
    return 0


def validate(ctx: Context, kind: str | None = None) -> int:
    """Read-only integrity: digests match; skills refs exist when kind=skills. Never executes."""
    try:
        listed = list_entries(ctx, kind)
    except KitError as exc:
        print(f"error: {exc}")
        return 2
    rows = [listed] if kind else list(listed.values())
    errors: list[str] = []
    for block in rows:
        k = block["kind"]
        for entry in block["entries"]:
            if entry.get("source") == "skills-dir":
                continue
            if entry.get("error"):
                errors.append(f"{k}/{entry['id']}: {entry['error']}")
                continue
            data = load_entry(ctx, k, entry["id"])
            expected = _digest(data)
            if data.get("digest") != expected:
                errors.append(f"{k}/{entry['id']}: digest mismatch (file edited outside catalog CLI)")
            if k == "skills" and data.get("ref"):
                ref_path = ctx.path(str(data["ref"]))
                if not (ref_path / "SKILL.md").is_file() and not ref_path.is_file():
                    errors.append(f"{k}/{entry['id']}: ref {data['ref']} not found")
            # Version files must remain byte-stable vs their own digest.
            versions_dir = _entity_dir(ctx, k, entry["id"]) / "versions"
            if versions_dir.is_dir():
                for path in versions_dir.glob("*.json"):
                    rev = _read_json(path)
                    if rev.get("digest") != _digest(rev):
                        errors.append(f"{k}/{entry['id']}@{path.stem}: revision digest mismatch")
    for error in errors:
        print(f"  FAIL  {error}")
    total = sum(len(block["entries"]) for block in rows)
    print(f"catalogs: {total} entr{'y' if total == 1 else 'ies'} — {'FAILED' if errors else 'PASSED'}")
    return 1 if errors else 0


def current_subject_path(ctx: Context, kind: str, entry_id: str) -> Path:
    """Path used as ApprovalPort subject for the current revision."""
    path = _current_path(ctx, kind, entry_id)
    if path.is_file():
        return path
    legacy = _legacy_path(ctx, kind, entry_id)
    if legacy.is_file():
        return legacy
    raise KitError("not_found", f"catalog entry {kind}/{entry_id} not found")
