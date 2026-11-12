"""File-backed factory Project entities (#2255 Phase A.4).

Projects bind a ``profile_ref`` and repositories. Repo ``mode`` defaults to
``dry_run`` (never live by accident). Not SaaS tenancy — files under
``.aisdlc/projects/{id}/current.json``.
"""
from __future__ import annotations

import json
from pathlib import Path

from aisdlc.actor import from_env
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.evidence import now as evidence_now

MODES = ("dry_run", "live")
PROJECTS_DIR = ".aisdlc/projects"


def _assert_id(value: str) -> str:
    if not value or "/" in value or "\\" in value or value.startswith("."):
        raise KitError("invalid_id", f"invalid project id {value!r}")
    return value


def _root(ctx: Context) -> Path:
    return ctx.path(PROJECTS_DIR)


def _path(ctx: Context, project_id: str) -> Path:
    return _root(ctx) / _assert_id(project_id) / "current.json"


def _normalize_repo(repo: dict) -> dict:
    if not isinstance(repo, dict):
        raise KitError("invalid_body", "repo must be an object")
    repo_id = str(repo.get("id") or "").strip()
    if not repo_id or "/" in repo_id or "\\" in repo_id:
        raise KitError("invalid_id", f"invalid repo id {repo_id!r}")
    mode = str(repo.get("mode") or "dry_run").strip().casefold()
    if mode not in MODES:
        raise KitError("invalid_body", f"repo mode must be dry_run|live, got {mode!r}")
    return {
        "id": repo_id,
        "forge": str(repo.get("forge") or "").strip(),
        "url": str(repo.get("url") or "").strip(),
        "default_branch": str(repo.get("default_branch") or "main").strip() or "main",
        "mode": mode,
    }


def _write(path: Path, payload: dict) -> dict:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return payload


def _read(path: Path) -> dict:
    if not path.is_file():
        raise KitError("not_found", f"project not found: {path.parent.name}")
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or not data.get("id"):
        raise KitError("invalid_entry", f"invalid project file {path}")
    return data


def create(
    ctx: Context,
    *,
    project_id: str,
    profile_ref: str,
    repos: list[dict] | None = None,
    actor: str | None = None,
) -> dict:
    path = _path(ctx, project_id)
    if path.is_file():
        raise KitError("already_exists", f"project {project_id!r} already exists")
    payload = {
        "id": _assert_id(project_id),
        "profile_ref": str(profile_ref or "").strip() or "aisdlc.toml",
        "repos": [_normalize_repo(r) for r in (repos or [])],
        "created_at": evidence_now(),
        "created_by": actor or from_env(channel="cli"),
    }
    return _write(path, payload)


def add_repo(
    ctx: Context,
    *,
    project_id: str,
    repo: dict,
    actor: str | None = None,
) -> dict:
    path = _path(ctx, project_id)
    payload = _read(path)
    normalized = _normalize_repo(repo)
    existing = {r["id"] for r in payload.get("repos") or []}
    if normalized["id"] in existing:
        raise KitError("already_exists", f"repo {normalized['id']!r} already on project")
    payload["repos"] = list(payload.get("repos") or []) + [normalized]
    payload["updated_at"] = evidence_now()
    payload["updated_by"] = actor or from_env(channel="cli")
    return _write(path, payload)


def show(ctx: Context, project_id: str) -> dict:
    return _read(_path(ctx, project_id))


def list_projects(ctx: Context) -> dict:
    root = _root(ctx)
    entries: list[dict] = []
    if root.is_dir():
        for child in sorted(root.iterdir()):
            current = child / "current.json"
            if current.is_file():
                try:
                    entries.append(_read(current))
                except KitError:
                    continue
    return {"kind": "projects", "entries": entries}
