"""Intent model service — one entry point for CLI, API, MCP (#2393, #2395).

Persisted as versioned JSON files under ``.aisdlc/intents/<id>/``.
A spec file is Markdown with Title / Description / Acceptance criteria / References.
Reference intents (github:#N, jira:KEY-1, url:https://…, file:path) only store
the reference — no network call is made.
"""
from __future__ import annotations

import json
import os
import re
import secrets
import tempfile
from pathlib import Path

from aisdlc import evidence
from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.evidence import now as evidence_now
from aisdlc.factory import SECRET_VALUES

INTENTS_DIR = ".aisdlc/intents"
VALID_SYSTEMS = ("github", "jira", "url", "file")

# Intent id pattern — same shape as serve.ID_RE
_ID_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$")


# ---------------------------------------------------------------------------
# Id validation
# ---------------------------------------------------------------------------

def _assert_id(value: str) -> str:
    """Validate an intent id — rejects path traversal and invalid characters."""
    if not value or not _ID_RE.fullmatch(value):
        raise KitError("invalid_id", f"invalid intent id {value!r}")
    return value


# ---------------------------------------------------------------------------
# Spec parser
# ---------------------------------------------------------------------------

def parse_spec(markdown: str) -> dict:
    """Parse a Markdown spec into {title, text, acceptance[], references[]}.

    Format:
      - ``# Title`` or first ``## Title`` section heading
      - Description: text before / under ``## Description``
      - ``## Acceptance criteria``: list items or checkboxes
      - ``## References``: ``[label](url)`` with optional ``(kind)`` tag
    """
    lines = markdown.splitlines()

    # --- Title ---
    title = ""
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("# ") and not stripped.startswith("## "):
            title = stripped[2:].strip()
            break
    if not title:
        # Fallback: first ## Title or first non-empty line
        for line in lines:
            stripped = line.strip()
            if stripped.startswith("## Title"):
                title = stripped[8:].strip()
                break
            elif stripped:
                title = stripped
                break
    if not title:
        title = "Untitled"

    # --- Sections ---
    sections = _split_sections(lines)
    description_lines: list[str] = []
    acceptance: list[str] = []
    references: list[dict] = []

    # Gather description: text before any ## heading, plus ## Description body
    desc_section = sections.get("## Description", [])
    if desc_section:
        description_lines = [line.strip() for line in desc_section if line.strip()]
    else:
        # Text before the first ## heading
        for line in lines:
            if line.strip().startswith("## "):
                break
            if line.strip():
                description_lines.append(line.strip())

    # Acceptance criteria
    for line in sections.get("## Acceptance criteria", []):
        stripped = line.strip()
        if stripped.startswith(('- [ ]', '- [x]', '- ')):
            item = re.sub(r"^- *\[[ xX]?\] ?", "", stripped)
            if item.strip():
                acceptance.append(item.strip())

    # References
    for line in sections.get("## References", []):
        stripped = line.strip()
        if not stripped.startswith("- "):
            continue
        rest = stripped[2:].strip()
        m = re.match(r"\[([^\]]+)\]\(([^)]+)\)\s*(?:\(([^)]+)\))?", rest)
        if m:
            label = m.group(1).strip()
            url = m.group(2).strip()
            kind_raw = (m.group(3) or "").strip().lower()
            kind = _infer_ref_kind(kind_raw, label, url)
            references.append({"label": label, "url": url, "kind": kind})

    text = "\n".join(description_lines)
    return {
        "title": title,
        "text": text,
        "acceptance": acceptance,
        "references": references,
    }


def _split_sections(lines: list[str]) -> dict[str, list[str]]:
    """Group lines under ``## Heading`` markers."""
    sections: dict[str, list[str]] = {}
    current: str | None = None
    for line in lines:
        m = re.match(r"^##\s+(.+)$", line.strip())
        if m:
            current = "## " + m.group(1).strip()
            sections.setdefault(current, [])
        elif current is not None:
            sections[current].append(line)
    return sections


def _infer_ref_kind(kind_raw: str, label: str, url: str) -> str:
    if kind_raw in ("architecture", "security", "adr", "standard", "other"):
        return kind_raw
    lower = (label + " " + url).lower()
    if "adr" in lower or "architecture" in lower:
        return "architecture"
    if "threat" in lower or "security" in lower:
        return "security"
    if "standard" in lower or "style" in lower or "guide" in lower:
        return "standard"
    return "other"


# ---------------------------------------------------------------------------
# Storage helpers
# ---------------------------------------------------------------------------

def _intents_root(ctx: Context) -> Path:
    return ctx.path(INTENTS_DIR)


def _intent_dir(ctx: Context, intent_id: str) -> Path:
    return _intents_root(ctx) / intent_id


def _current_path(ctx: Context, intent_id: str) -> Path:
    return _intent_dir(ctx, intent_id) / "current.json"


def _version_path(ctx: Context, intent_id: str, version: int) -> Path:
    return _intent_dir(ctx, intent_id) / "versions" / f"{version}.json"


def _next_version(ctx: Context, intent_id: str) -> int:
    versions_dir = _intent_dir(ctx, intent_id) / "versions"
    if not versions_dir.is_dir():
        return 1
    existing = [int(p.stem) for p in versions_dir.glob("*.json") if p.stem.isdigit()]
    return max(existing, default=0) + 1


def _new_intent_id() -> str:
    return f"int-{secrets.token_hex(6)}"


def _write_intent(ctx: Context, intent: dict) -> dict:
    """Persist intent as current.json + versioned file."""
    intent_id = intent["id"]
    version = _next_version(ctx, intent_id)
    intent["version"] = version

    payload_text = json.dumps(intent, indent=2, sort_keys=True) + "\n"

    # Write version file
    vpath = _version_path(ctx, intent_id, version)
    vpath.parent.mkdir(parents=True, exist_ok=True)
    vpath.write_text(payload_text, encoding="utf-8")

    # Write current.json atomically (temp file + os.replace)
    cpath = _current_path(ctx, intent_id)
    cpath.parent.mkdir(parents=True, exist_ok=True)
    dir_path = cpath.parent
    fd, tmp_path = tempfile.mkstemp(dir=str(dir_path), suffix=".tmp")
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as f:
            f.write(payload_text)
        os.replace(tmp_path, str(cpath))
    except BaseException:
        os.unlink(tmp_path)
        raise

    return intent


def _read_intent(ctx: Context, intent_id: str) -> dict:
    _assert_id(intent_id)
    cpath = _current_path(ctx, intent_id)
    if not cpath.is_file():
        raise KitError("not_found", f"intent not found: {intent_id}")
    data = json.loads(cpath.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or not data.get("id"):
        raise KitError("invalid_entry", f"invalid intent file {cpath}")
    return data


# ---------------------------------------------------------------------------
# Secrets check
# ---------------------------------------------------------------------------

def _check_secrets(*texts: str) -> None:
    """Raise KitError if any text matches factory SECRET_VALUES patterns."""
    for text in texts:
        if text and SECRET_VALUES.search(text):
            raise KitError("literal_secret", "intent text contains a value that looks like a secret")


# ---------------------------------------------------------------------------
# Ledger event (no text)
# ---------------------------------------------------------------------------

def _emit_ledger_event(ctx: Context, intent: dict) -> None:
    """Append a ledger event with id, kind, source, actor — never the text."""
    evidence.append_record(
        ctx,
        {
            "kind": "intent.submit",
            "intent_id": intent["id"],
            "intent_kind": intent["kind"],
            "source": intent.get("source", {}),
            "actor": intent.get("created_by", ""),
        },
    )


# ---------------------------------------------------------------------------
# Store mirror (best-effort, silent no-op)
# ---------------------------------------------------------------------------

def _mirror_to_store(ctx: Context, intent: dict) -> None:
    """Try to mirror to aisdlc.store if available; silent no-op otherwise."""
    try:
        from aisdlc import store

        if hasattr(store, "mirror_intent"):
            store.mirror_intent(ctx, intent)
    except (ImportError, RuntimeError):
        pass


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def submit(
    ctx: Context,
    *,
    text: str | None = None,
    ref: str | None = None,
    spec: str | None = None,
    title: str | None = None,
    actor: str | None = None,
    source_ref: str | None = None,
) -> dict:
    """Create an intent (prompt | reference | spec).

    Exactly one of ``text``, ``ref``, ``spec`` must be provided.
    ``source_ref`` is used only for spec intents submitted via CLI ``--file``.
    """
    actor = actor or from_env(channel="cli")
    created_at = evidence_now()

    if title is not None:
        _check_secrets(title)

    if text is not None:
        _check_secrets(text)
        intent = {
            "id": _new_intent_id(),
            "kind": "prompt",
            "title": title or text,
            "text": text,
            "source": {"system": "", "ref": ""},
            "acceptance": [],
            "references": [],
            "status": "open",
            "created_by": actor,
            "created_at": created_at,
            "run_ids": [],
        }
    elif ref is not None:
        # Parse system:ref
        if ":" not in ref:
            raise KitError("invalid_body", f"reference must be system:id, got {ref!r}")
        system, ref_id = ref.split(":", 1)
        if system not in VALID_SYSTEMS:
            raise KitError(
                "invalid_body",
                f"unknown reference system {system!r}; allowed: {', '.join(VALID_SYSTEMS)}",
            )
        _check_secrets(ref_id)
        intent = {
            "id": _new_intent_id(),
            "kind": "reference",
            "title": title or ref,
            "text": "",
            "source": {"system": system, "ref": ref_id},
            "acceptance": [],
            "references": [],
            "status": "open",
            "created_by": actor,
            "created_at": created_at,
            "run_ids": [],
        }
    elif spec is not None:
        _check_secrets(spec)
        parsed = parse_spec(spec)
        intent = {
            "id": _new_intent_id(),
            "kind": "spec",
            "title": title or parsed["title"],
            "text": parsed["text"],
            "source": {"system": "file", "ref": source_ref or ""},
            "acceptance": parsed["acceptance"],
            "references": parsed["references"],
            "status": "open",
            "created_by": actor,
            "created_at": created_at,
            "run_ids": [],
        }
    else:
        raise KitError("invalid_body", "submit requires one of: text, ref, spec")

    _write_intent(ctx, intent)
    _emit_ledger_event(ctx, intent)
    _mirror_to_store(ctx, intent)
    return intent


def list_intents(ctx: Context) -> dict:
    """Return all intents."""
    root = _intents_root(ctx)
    entries: list[dict] = []
    if root.is_dir():
        for child in sorted(root.iterdir()):
            current = child / "current.json"
            if current.is_file():
                try:
                    entries.append(json.loads(current.read_text(encoding="utf-8")))
                except (json.JSONDecodeError, OSError):
                    continue
    return {"kind": "intents", "entries": entries}


def show_intent(ctx: Context, intent_id: str) -> dict:
    """Return a single intent by id."""
    return _read_intent(ctx, intent_id)


def attach_run(ctx: Context, intent_id: str, run_id: str) -> dict:
    """Append a run_id to an intent's run_ids list."""
    intent = _read_intent(ctx, intent_id)
    if run_id not in intent["run_ids"]:
        intent["run_ids"].append(run_id)
    _write_intent(ctx, intent)
    return intent
