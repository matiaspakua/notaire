"""Catalog import and materialize for AI harness configurations."""
from __future__ import annotations

import hashlib
import json
from enum import Enum
from pathlib import Path
from typing import NamedTuple

from aisdlc import catalogs
from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.evidence import append_record
from aisdlc.evidence import now as evidence_now
from aisdlc.skills import frontmatter
from aisdlc.telemetry import get_telemetry


class LayoutMode(Enum):
    dir = "dir"
    file = "file"


class LayoutSpec(NamedTuple):
    mode: LayoutMode
    template: str


LAYOUTS: dict[str, dict[str, LayoutSpec]] = {
    "claude-code": {
        "skills": LayoutSpec(LayoutMode.dir, ".claude/skills/{id}"),
        "agents": LayoutSpec(LayoutMode.file, ".claude/agents/{id}.md"),
        "rules": LayoutSpec(LayoutMode.file, ".claude/rules/{id}.md"),
    },
    "cursor": {
        "rules": LayoutSpec(LayoutMode.file, ".cursor/rules/{id}.mdc"),
    },
    "codex": {
        "skills": LayoutSpec(LayoutMode.dir, ".codex/skills/{id}"),
    },
    "junie": {
        "rules": LayoutSpec(LayoutMode.file, ".junie/guidelines/{id}.md"),
    },
    "kiro": {
        "rules": LayoutSpec(LayoutMode.file, ".kiro/steering/{id}.md"),
    },
    "agy": {
        "skills": LayoutSpec(LayoutMode.dir, ".agents/skills/{id}"),
    },
}


def _source_root_for(harness: str, kind: str) -> Path:
    template = LAYOUTS[harness][kind].template
    return Path(template.rsplit("/", 1)[0])


def _entry_suffix_for(harness: str, kind: str) -> str:
    spec = LAYOUTS[harness][kind]
    template = spec.template
    if spec.mode is LayoutMode.file:
        full = template.replace("{id}", "x")
        return Path(full).suffix
    return ""


def _sha256_hex(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def _read_files(source: Path) -> tuple[dict[str, str], list[str]]:
    files: dict[str, str] = {}
    skipped: list[str] = []
    if not source.exists():
        return files, skipped
    if source.is_file():
        raw = source.read_bytes()
        try:
            text = raw.decode("utf-8", errors="strict")
        except UnicodeDecodeError:
            skipped.append(source.name)
            return files, skipped
        files[source.name] = text
    else:
        for path in sorted(source.rglob("*")):
            if not path.is_file():
                continue
            rel = path.relative_to(source).as_posix()
            raw = path.read_bytes()
            try:
                text = raw.decode("utf-8", errors="strict")
            except UnicodeDecodeError:
                skipped.append(rel)
                continue
            files[rel] = text
    return files, skipped


def _extract_summary(kind: str, files: dict[str, str]) -> str:
    if kind == "skills" and "SKILL.md" in files:
        meta = frontmatter(files["SKILL.md"])
        return meta.get("description") or ""
    if kind == "agents":
        for content in files.values():
            meta = frontmatter(content)
            desc = meta.get("description")
            if desc:
                return desc
    return ""


def _files_digest(files: dict[str, str]) -> str:
    blob = json.dumps(files, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(blob).hexdigest()


def _dest_path(dest: Path, harness: str, kind: str, entry_id: str, filename: str) -> Path:
    spec = LAYOUTS[harness][kind]
    base = spec.template.format(id=entry_id)
    if spec.mode is LayoutMode.dir:
        return dest / base / filename
    return dest / base


def _dest_rel(harness: str, kind: str, entry_id: str, filename: str) -> str:
    spec = LAYOUTS[harness][kind]
    base = spec.template.format(id=entry_id)
    if spec.mode is LayoutMode.dir:
        return f"{base}/{filename}"
    return base


MANIFEST_NAME = ".aisdlc-materialized.json"


def _scan_dir_kind(harness: str, kind: str, source_root: Path) -> list[tuple[str, Path]]:
    return sorted(
        (p.name, p) for p in source_root.iterdir()
        if p.is_dir() and not p.name.startswith(".")
    )


def _scan_file_kind(harness: str, kind: str, source_root: Path) -> list[tuple[str, Path]]:
    suffix = _entry_suffix_for(harness, kind)
    return sorted(
        (p.stem, p) for p in source_root.iterdir()
        if p.is_file() and not p.name.startswith(".") and p.suffix == suffix
    )


def _record_import(ctx: Context, harness: str, total_entries: int, total_files: int, actor: str) -> str:
    digest = _sha256_hex(
        json.dumps({"harness": harness, "entries": total_entries, "files": total_files},
                    sort_keys=True).encode()
    )
    append_record(ctx, {
        "kind": "catalog.import",
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": digest,
        "actor": actor,
        "ts": evidence_now(),
    })
    get_telemetry().emit("aisdlc.catalog.import", {
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": digest,
    })
    return digest


def import_harness(
    ctx: Context,
    harness: str,
    from_dir: Path,
    *,
    kind_filter: str | None = None,
    actor: str | None = None,
) -> int:
    if harness not in LAYOUTS:
        raise KitError("unknown_harness", f"unknown harness {harness!r}; expected {sorted(LAYOUTS)}")
    if actor is None:
        actor = from_env(channel="cli")

    kinds = [kind_filter] if kind_filter else list(LAYOUTS[harness].keys())
    total_entries = 0
    total_files = 0
    had_error = False

    for kind in kinds:
        if kind not in LAYOUTS[harness]:
            continue
        source_root = from_dir / _source_root_for(harness, kind)
        if not source_root.exists():
            continue

        spec = LAYOUTS[harness][kind]
        pairs = _scan_dir_kind(harness, kind, source_root) if spec.mode is LayoutMode.dir \
            else _scan_file_kind(harness, kind, source_root)

        for entry_id, entry_path in pairs:
            result = _import_entry(ctx, harness, kind, entry_id, entry_path, actor)
            if result is None:
                had_error = True
            else:
                total_entries += 1
                total_files += result

    _record_import(ctx, harness, total_entries, total_files, actor)
    return 1 if had_error else 0


def _import_entry(
    ctx: Context,
    harness: str,
    kind: str,
    entry_id: str,
    source: Path,
    actor: str,
) -> int | None:
    files, skipped = _read_files(source)
    for s in skipped:
        print(f"  skip (non-UTF-8): {s}")
    if not files:
        return 0

    try:
        catalogs.guard_files(files)
    except KitError as exc:
        print(f"  reject ({exc.code}): {entry_id} — {exc.detail}")
        return None

    summary = _extract_summary(kind, files)
    file_count = len(files)

    if catalogs.has_entry(ctx, kind, entry_id):
        current = catalogs.load_entry(ctx, kind, entry_id)
        existing_files = current.get("files")
        if existing_files == files:
            print(f"  unchanged: {kind}/{entry_id}")
            return file_count
        payload = dict(current)
        payload["summary"] = summary or current.get("summary") or ""
        catalogs.write_new_revision(ctx, kind, entry_id, payload, actor=actor, files=files)
        print(f"  updated: {kind}/{entry_id}@v{current.get('version', 1) + 1}")
    else:
        catalogs.add(ctx, kind, entry_id, ref=None, summary=summary, actor=actor, files=files)
        print(f"  imported: {kind}/{entry_id}@v1")

    return file_count


def _collect_entries(ctx: Context, harness: str, kinds: list[str]) -> list[dict]:
    layout = LAYOUTS[harness]
    entries: list[dict] = []
    for kind in kinds:
        if kind not in layout:
            continue
        try:
            block = catalogs.list_entries(ctx, kind)
        except KitError:
            continue
        for meta in block.get("entries", []):
            if meta.get("source") == "skills-dir" or meta.get("error"):
                continue
            try:
                entry = catalogs.load_entry(ctx, kind, meta["id"])
            except KitError:
                continue
            if entry.get("files"):
                entries.append(entry)
    entries.sort(key=lambda e: (e["kind"], e["id"]))
    return entries


def _write_entries(dest: Path, harness: str, entries: list[dict]) -> tuple[int, list[dict]]:
    total_files = 0
    manifest_entries: list[dict] = []
    for entry in entries:
        kind = entry["kind"]
        eid = entry["id"]
        hashes: dict[str, str] = {}
        for fname, text in entry["files"].items():
            raw = text.encode("utf-8")
            dp = _dest_path(dest, harness, kind, eid, fname)
            dp.parent.mkdir(parents=True, exist_ok=True)
            dp.write_bytes(raw)
            hashes[_dest_rel(harness, kind, eid, fname)] = _sha256_hex(raw)
            total_files += 1
        manifest_entries.append({
            "kind": kind,
            "id": eid,
            "version": entry.get("version", 1),
            "digest": entry.get("digest", ""),
            "files": hashes,
        })
    return total_files, manifest_entries


def _write_manifest(dest: Path, harness: str, manifest_entries: list[dict]) -> dict:
    manifest = {"schema": 1, "harness": harness, "entries": manifest_entries}
    dest.mkdir(parents=True, exist_ok=True)
    (dest / MANIFEST_NAME).write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    return manifest


def _record_materialize(
    ctx: Context, harness: str, total_entries: int, total_files: int,
    digest: str, actor: str,
) -> None:
    append_record(ctx, {
        "kind": "catalog.materialize",
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": digest,
        "actor": actor,
        "ts": evidence_now(),
    })
    get_telemetry().emit("aisdlc.catalog.materialize", {
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": digest,
    })


def materialize_harness(
    ctx: Context,
    harness: str,
    dest: Path,
    *,
    kind_filter: str | None = None,
    check: bool = False,
    actor: str | None = None,
) -> int:
    if harness not in LAYOUTS:
        raise KitError("unknown_harness", f"unknown harness {harness!r}; expected {sorted(LAYOUTS)}")
    if actor is None:
        actor = from_env(channel="cli")

    kinds = [kind_filter] if kind_filter else list(LAYOUTS[harness].keys())
    entries = _collect_entries(ctx, harness, kinds)

    if check:
        return _check_harness(harness, dest, entries)

    total_files, manifest_entries = _write_entries(dest, harness, entries)
    manifest = _write_manifest(dest, harness, manifest_entries)
    digest = _sha256_hex(json.dumps(manifest, sort_keys=True).encode())
    _record_materialize(ctx, harness, len(entries), total_files, digest, actor)
    return 0


def _check_harness(harness: str, dest: Path, entries: list[dict]) -> int:
    drift: list[str] = []
    manifest_path = dest / MANIFEST_NAME
    if manifest_path.is_file():
        try:
            prior = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError):
            prior = {}
        current_ids = {(e["kind"], e["id"]) for e in entries}
        for pe in prior.get("entries", []):
            pk = (pe.get("kind"), pe.get("id"))
            if pk not in current_ids:
                for rp in pe.get("files", {}):
                    if (dest / rp).is_file():
                        drift.append(f"stale: {rp}")
                        break
                else:
                    drift.append(f"stale: {pe.get('kind')}/{pe.get('id')}")
    else:
        drift.append(f"missing manifest: {MANIFEST_NAME}")

    for entry in entries:
        kind = entry["kind"]
        eid = entry["id"]
        for fname, text in entry["files"].items():
            expected = text.encode("utf-8")
            dp = _dest_path(dest, harness, kind, eid, fname)
            if not dp.exists():
                drift.append(f"missing: {_dest_rel(harness, kind, eid, fname)}")
            elif dp.read_bytes() != expected:
                drift.append(f"modified: {_dest_rel(harness, kind, eid, fname)}")

    for line in drift:
        print(line)
    return 1 if drift else 0
