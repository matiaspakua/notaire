"""Catalog import and materialize for AI harness configurations (#2399 / CU76).

Provides:
- LAYOUTS: static mapping of harness -> {kind -> (mode, path_template)}
- import_harness: scan harness dir, create/update catalog entries with files payload
- materialize_harness: write catalog entries into harness layout, write manifest
- check_harness: verify disk matches catalog without writing

Evidence records: catalog.import / catalog.materialize
Telemetry events: aisdlc.catalog.import / aisdlc.catalog.materialize
"""
from __future__ import annotations

import hashlib
import json
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

# ---------------------------------------------------------------------------
# Layout table
# ---------------------------------------------------------------------------

class LayoutSpec(NamedTuple):
    mode: str           # "dir" or "file"
    template: str       # path template relative to dest, with {id} placeholder


# LAYOUTS[harness][kind] = LayoutSpec
# Kinds "prompts" and "hooks" are intentionally absent (catalog-only, no harness layout).
LAYOUTS: dict[str, dict[str, LayoutSpec]] = {
    "claude-code": {
        "skills": LayoutSpec("dir", ".claude/skills/{id}"),
        "agents": LayoutSpec("file", ".claude/agents/{id}.md"),
        "rules":  LayoutSpec("file", ".claude/rules/{id}.md"),
    },
    "cursor": {
        "rules":  LayoutSpec("file", ".cursor/rules/{id}.mdc"),
    },
    "codex": {
        "skills": LayoutSpec("dir", ".codex/skills/{id}"),
    },
    "junie": {
        "rules":  LayoutSpec("file", ".junie/guidelines/{id}.md"),
    },
    "kiro": {
        "rules":  LayoutSpec("file", ".kiro/steering/{id}.md"),
    },
    "agy": {
        "skills": LayoutSpec("dir", ".agents/skills/{id}"),
    },
}

# Maps harness -> {kind -> source_root_relative_to_from_dir}
_SOURCE_ROOTS: dict[str, dict[str, str]] = {
    "claude-code": {
        "skills": ".claude/skills",
        "agents": ".claude/agents",
        "rules":  ".claude/rules",
    },
    "cursor": {
        "rules":  ".cursor/rules",
    },
    "codex": {
        "skills": ".codex/skills",
    },
    "junie": {
        "rules":  ".junie/guidelines",
    },
    "kiro": {
        "rules":  ".kiro/steering",
    },
    "agy": {
        "skills": ".agents/skills",
    },
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _sha256_hex(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def _read_files_from_dir(source_dir: Path, entry_name: str, kind: str) -> tuple[dict[str, str], list[str]]:
    """Read all files under source_dir as UTF-8.

    Returns (files_dict, skipped_list). Non-UTF-8 files are skipped and reported.
    For dir-mode (skills): files are stored relative to the entry folder.
    For file-mode (agents/rules): only the single file, stored by basename.
    """
    files: dict[str, str] = {}
    skipped: list[str] = []
    if not source_dir.exists():
        return files, skipped
    if source_dir.is_file():
        # file-mode entry
        raw = source_dir.read_bytes()
        try:
            text = raw.decode("utf-8", errors="strict")
        except UnicodeDecodeError:
            skipped.append(source_dir.name)
            return files, skipped
        files[source_dir.name] = text
    else:
        # dir-mode: recurse into folder
        for path in sorted(source_dir.rglob("*")):
            if not path.is_file():
                continue
            rel = path.relative_to(source_dir).as_posix()
            raw = path.read_bytes()
            try:
                text = raw.decode("utf-8", errors="strict")
            except UnicodeDecodeError:
                skipped.append(rel)
                continue
            files[rel] = text
    return files, skipped


def _extract_summary(kind: str, files: dict[str, str], entry_id: str) -> str:
    """Extract summary from SKILL.md frontmatter 'description:' or agent file frontmatter."""
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
    """Stable digest of a files mapping (sorted by key, SHA-256 of JSON)."""
    blob = json.dumps(files, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(blob).hexdigest()


def _dest_path_for_entry(dest: Path, harness: str, kind: str, entry_id: str, filename: str) -> Path:
    """Compute the destination path for a single file within a materialized entry."""
    spec = LAYOUTS[harness][kind]
    base = spec.template.format(id=entry_id)
    if spec.mode == "dir":
        return dest / base / filename
    else:
        # file mode: the template IS the full path; filename is ignored
        return dest / base


def _dest_prefix_for_entry(harness: str, kind: str, entry_id: str, filename: str) -> str:
    """Dest-relative path string for manifest (no leading slash)."""
    spec = LAYOUTS[harness][kind]
    base = spec.template.format(id=entry_id)
    if spec.mode == "dir":
        return f"{base}/{filename}"
    else:
        return base


# ---------------------------------------------------------------------------
# Import
# ---------------------------------------------------------------------------

def import_harness(
    ctx: Context,
    harness: str,
    from_dir: Path,
    *,
    kind_filter: str | None = None,
    actor: str | None = None,
) -> int:
    """Import AI configuration from harness layout into catalog entries.

    Returns 0 on success, 1 if any entry was rejected (secret) or had an error.
    """
    if harness not in LAYOUTS:
        raise KitError("unknown_harness", f"unknown harness {harness!r}; expected {sorted(LAYOUTS)}")
    if actor is None:
        actor = from_env(channel="cli")

    source_roots = _SOURCE_ROOTS[harness]
    kinds_to_scan = [kind_filter] if kind_filter else list(source_roots.keys())

    total_entries = 0
    total_files = 0
    had_error = False
    imported_digest = ""

    for kind in kinds_to_scan:
        if kind not in source_roots:
            continue
        source_root = from_dir / source_roots[kind]
        if not source_root.exists():
            continue

        spec = LAYOUTS[harness][kind]
        if spec.mode == "dir":
            # Each subdirectory is one entry
            entry_dirs = sorted(p for p in source_root.iterdir() if p.is_dir() and not p.name.startswith("."))
            for entry_dir in entry_dirs:
                result = _import_entry(ctx, harness, kind, entry_dir.name, entry_dir, actor)
                if result is None:
                    had_error = True
                else:
                    total_entries += 1
                    total_files += result
        else:
            # Each file is one entry (strip extension for id)
            entry_files = sorted(p for p in source_root.iterdir() if p.is_file() and not p.name.startswith("."))
            for entry_file in entry_files:
                entry_id = entry_file.stem
                result = _import_entry(ctx, harness, kind, entry_id, entry_file, actor)
                if result is None:
                    had_error = True
                else:
                    total_entries += 1
                    total_files += result

    # Build a digest for the import event
    imported_digest = _sha256_hex(
        json.dumps({"harness": harness, "entries": total_entries, "files": total_files},
                   sort_keys=True).encode()
    )

    # Evidence
    append_record(ctx, {
        "kind": "catalog.import",
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": imported_digest,
        "actor": actor,
        "ts": evidence_now(),
    })

    # Telemetry
    get_telemetry().emit("aisdlc.catalog.import", {
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": imported_digest,
    })

    return 1 if had_error else 0


def _import_entry(
    ctx: Context,
    harness: str,
    kind: str,
    entry_id: str,
    source: Path,
    actor: str,
) -> int | None:
    """Import one entry. Returns count of files imported, or None on rejection."""
    files, skipped = _read_files_from_dir(source, entry_id, kind)

    for s in skipped:
        print(f"  skip (non-UTF-8): {s}")

    if not files:
        return 0

    # Secret check
    try:
        catalogs.guard_files(files)
    except KitError as exc:
        # Print path info (from exc) but never the secret
        print(f"  reject ({exc.code}): {entry_id} — {exc.detail}")
        return None

    summary = _extract_summary(kind, files, entry_id)
    file_count = len(files)

    # Idempotency check
    if catalogs._exists(ctx, kind, entry_id):
        try:
            current = catalogs.load_entry(ctx, kind, entry_id)
        except KitError:
            current = None
        if current is not None:
            existing_files = current.get("files")
            if existing_files == files:
                print(f"  unchanged: {kind}/{entry_id}")
                return file_count
            # Changed: write new revision
            payload = dict(current)
            payload["summary"] = summary or current.get("summary") or ""
            catalogs.write_new_revision(ctx, kind, entry_id, payload, actor=actor, files=files)
            print(f"  updated: {kind}/{entry_id}@v{current.get('version', 1) + 1}")
        else:
            # Exists but can't load - add fresh
            catalogs.add(ctx, kind, entry_id, ref=None, summary=summary, actor=actor, files=files)
            print(f"  imported: {kind}/{entry_id}@v1")
    else:
        catalogs.add(ctx, kind, entry_id, ref=None, summary=summary, actor=actor, files=files)
        print(f"  imported: {kind}/{entry_id}@v1")

    return file_count


# ---------------------------------------------------------------------------
# Materialize
# ---------------------------------------------------------------------------

MANIFEST_NAME = ".aisdlc-materialized.json"


def materialize_harness(
    ctx: Context,
    harness: str,
    dest: Path,
    *,
    kind_filter: str | None = None,
    check: bool = False,
    actor: str | None = None,
) -> int:
    """Materialize catalog entries into harness layout under dest.

    With check=True: writes nothing, reports drift, exits 1 on drift else 0.
    Without check: writes files and manifest, records evidence and telemetry.
    """
    if harness not in LAYOUTS:
        raise KitError("unknown_harness", f"unknown harness {harness!r}; expected {sorted(LAYOUTS)}")
    if actor is None:
        actor = from_env(channel="cli")

    layout = LAYOUTS[harness]
    kinds_to_mat = [kind_filter] if kind_filter else list(layout.keys())

    # Collect all catalog entries that have files for this harness
    entries_data: list[dict] = []
    for kind in kinds_to_mat:
        if kind not in layout:
            continue
        try:
            block = catalogs.list_entries(ctx, kind)
        except KitError:
            continue
        for entry_meta in block.get("entries", []):
            if entry_meta.get("source") == "skills-dir":
                continue
            if entry_meta.get("error"):
                continue
            try:
                entry = catalogs.load_entry(ctx, kind, entry_meta["id"])
            except KitError:
                continue
            if "files" not in entry or not entry["files"]:
                continue
            entries_data.append(entry)

    # Sort by kind then id
    entries_data.sort(key=lambda e: (e["kind"], e["id"]))

    if check:
        return _check_harness(harness, dest, entries_data)

    # --- write mode ---
    total_files = 0
    manifest_entries: list[dict] = []

    for entry in entries_data:
        kind = entry["kind"]
        entry_id = entry["id"]
        files: dict[str, str] = entry["files"]
        spec = layout[kind]
        file_hashes: dict[str, str] = {}

        for filename, text in files.items():
            raw = text.encode("utf-8")
            dest_path = _dest_path_for_entry(dest, harness, kind, entry_id, filename)
            dest_path.parent.mkdir(parents=True, exist_ok=True)
            dest_path.write_bytes(raw)
            rel = _dest_prefix_for_entry(harness, kind, entry_id, filename)
            file_hashes[rel] = _sha256_hex(raw)
            total_files += 1
            _ = spec  # used above implicitly

        manifest_entries.append({
            "kind": kind,
            "id": entry_id,
            "version": entry.get("version", 1),
            "digest": entry.get("digest", ""),
            "files": file_hashes,
        })

    manifest = {
        "schema": 1,
        "harness": harness,
        "entries": manifest_entries,
    }
    manifest_path = dest / MANIFEST_NAME
    dest.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")

    total_entries = len(entries_data)
    mat_digest = _sha256_hex(json.dumps(manifest, sort_keys=True).encode())

    # Evidence
    append_record(ctx, {
        "kind": "catalog.materialize",
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": mat_digest,
        "actor": actor,
        "ts": evidence_now(),
    })

    # Telemetry
    get_telemetry().emit("aisdlc.catalog.materialize", {
        "harness": harness,
        "entries": total_entries,
        "files": total_files,
        "digest": mat_digest,
    })

    return 0


def _check_harness(harness: str, dest: Path, entries_data: list[dict]) -> int:
    """Compare disk state against expected catalog output. Returns 1 on any drift, else 0."""
    layout = LAYOUTS[harness]
    drift: list[str] = []

    # Check manifest
    manifest_path = dest / MANIFEST_NAME
    if not manifest_path.is_file():
        drift.append(f"missing manifest: {MANIFEST_NAME}")
        # Still check individual files
    else:
        # Load prior manifest to detect stale entries
        try:
            prior_manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError):
            prior_manifest = {}

        current_ids = {(e["kind"], e["id"]) for e in entries_data}
        for prior_entry in prior_manifest.get("entries", []):
            pk = (prior_entry.get("kind"), prior_entry.get("id"))
            if pk not in current_ids:
                # Stale entry - check if files still on disk
                stale_files = prior_entry.get("files", {})
                for rel_path in stale_files:
                    disk_path = dest / rel_path
                    if disk_path.is_file():
                        drift.append(f"stale: {rel_path}")
                        break
                else:
                    drift.append(f"stale: {prior_entry.get('kind')}/{prior_entry.get('id')}")

    for entry in entries_data:
        kind = entry["kind"]
        entry_id = entry["id"]
        files: dict[str, str] = entry["files"]

        for filename, text in files.items():
            expected_bytes = text.encode("utf-8")
            dest_path = _dest_path_for_entry(dest, harness, kind, entry_id, filename)
            if not dest_path.exists():
                rel = _dest_prefix_for_entry(harness, kind, entry_id, filename)
                drift.append(f"missing: {rel}")
            else:
                actual = dest_path.read_bytes()
                if actual != expected_bytes:
                    rel = _dest_prefix_for_entry(harness, kind, entry_id, filename)
                    drift.append(f"modified: {rel}")
            _ = layout  # referenced indirectly via _dest_path_for_entry

    for line in drift:
        print(line)

    return 1 if drift else 0
