"""Factory config, plugin manifests and the migration inventory.

Business rules live here so a later UI can call them. The CLI only parses
arguments and prints. Inspecting config never executes a plugin command.
"""
from __future__ import annotations

import json
import os
import re
import tomllib
from pathlib import Path

from aisdlc.profile import PLUGIN_KINDS, Profile, ProfileError

SCHEMA = 1
CLASSES = ("core", "project-profile", "harness-adapter", "method-plugin", "compatibility-shim", "stay")
STATUSES = ("migrated", "planned", "stay")
SECRET_KEYS = re.compile(r"(token|secret|password|passwd|api_key|authorization|credential)", re.I)
SECRET_VALUES = re.compile(r"(ghp_[A-Za-z0-9]{8,}|github_pat_\w+|sk-[A-Za-z0-9]|AKIA[0-9A-Z]{8,}|Bearer\s+\S+)")
ENV_LIMIT = "AISDLC_MAX_OPEN_PRS"


def resolve_max_open_prs(profile: Profile, env: dict[str, str] | None = None, cli: int | None = None) -> int:
    """Effective cap. The profile value is a ceiling: env and flags may lower it, not raise it.

    The environment variable is a number, not a secret. A non-numeric value is ignored.
    """
    ceiling = profile.max_open_prs
    requested = ceiling
    if cli is not None:
        requested = cli
    else:
        raw = (env if env is not None else os.environ).get(ENV_LIMIT)
        if raw is not None and raw.isdigit():
            requested = int(raw)
    if requested > ceiling:
        raise ProfileError(
            f"cannot raise max_open_prs above the profile ceiling {ceiling} (asked for {requested})"
        )
    return requested


def pr_cap_decision(open_count: int, limit: int) -> str:
    """Local decision only. This does not take a distributed lock."""
    if open_count < 0 or limit < 0:
        raise ProfileError("open pull-request count and the cap must be non-negative")
    return "allow" if open_count < limit else "block"


def redact(key: str, value: object) -> object:
    if isinstance(value, dict):
        return {k: redact(k, v) for k, v in value.items()}
    if isinstance(value, list):
        return [redact(key, item) for item in value]
    if not isinstance(value, str):
        return value
    if value.startswith("env:"):
        return value
    if SECRET_KEYS.search(key) or SECRET_VALUES.search(value):
        return "***"
    return value


def sidecar_is_stale(design: Path, sidecar: Path) -> bool:
    """True when the design document is newer than the generated sidecar."""
    if not design.is_file() or not sidecar.is_file():
        return False
    return sidecar.stat().st_mtime < design.stat().st_mtime


def validate_factory(root: Path, profile: Profile) -> list[str]:
    errors: list[str] = []
    allow = set(profile.plugin_allow)
    for plugin in profile.plugins:
        if plugin.trusted and plugin.id not in allow:
            errors.append(f"plugin {plugin.id} is trusted but not in [plugins] allow")
        path = _manifest_path(root, plugin.manifest)
        if path is None:
            errors.append(f"plugin {plugin.id} manifest escapes the project: {plugin.manifest}")
            continue
        if not path.is_file():
            errors.append(f"plugin {plugin.id} manifest is missing: {plugin.manifest}")
            continue
        errors.extend(_manifest_errors(plugin.id, path))
    if profile.inventory:
        errors.extend(check_inventory(root / profile.inventory))
    return errors


def list_plugins(root: Path, profile: Profile) -> list[dict]:
    """Read manifests. Does not import or run plugin code."""
    rows = []
    allow = set(profile.plugin_allow)
    for plugin in profile.plugins:
        available = plugin.trusted and plugin.id in allow
        path = _manifest_path(root, plugin.manifest)
        manifest = _read_manifest(path) if path and path.is_file() else {}
        rows.append({
            "id": plugin.id,
            "kind": plugin.kind,
            "version": plugin.version,
            "available": available,
            "reason": None if available else "not in the trusted allowlist",
            "capabilities": list(manifest.get("capabilities", [])) if available else [],
            "status": manifest.get("status", "declared"),
        })
    return rows


def method_plan(root: Path, profile: Profile, plugin_id: str, variables: dict[str, str]) -> str:
    """Return the plugin's plan command with placeholders filled. Does not run it."""
    plugin = next((item for item in profile.plugins if item.id == plugin_id), None)
    if plugin is None or not plugin.trusted or plugin.id not in set(profile.plugin_allow):
        raise ProfileError(f"plugin {plugin_id} is not an available trusted plugin")
    path = _manifest_path(root, plugin.manifest)
    if path is None or not path.is_file():
        raise ProfileError(f"plugin {plugin_id} has no readable manifest")
    command = _read_manifest(path).get("command", [])
    if not isinstance(command, list) or not command or not all(isinstance(part, str) for part in command):
        raise ProfileError(f"plugin {plugin_id} command must be an argv list")
    filled: list[str] = []
    for part in command:
        for key, value in variables.items():
            if not re.fullmatch(r"[A-Za-z0-9_.-]+", value):
                raise ProfileError(f"variable {key} must be a single token")
            part = part.replace("{" + key + "}", value)
        if "{" in part or not re.fullmatch(r"[A-Za-z0-9_./:-]+", part):
            raise ProfileError(f"plugin {plugin_id} command part is not a single argv token")
        filled.append(part)
    return " ".join(filled)


def check_inventory(path: Path) -> list[str]:
    if not path.is_file():
        return [f"migration inventory is missing: {path}"]
    try:
        data = tomllib.loads(path.read_text(encoding="utf-8"))
    except tomllib.TOMLDecodeError as exc:
        return [f"migration inventory is not valid TOML: {exc}"]
    errors: list[str] = []
    if data.get("schema") != SCHEMA:
        errors.append(f"migration inventory schema must be {SCHEMA}")
    if not data.get("canonical_root"):
        errors.append("migration inventory needs canonical_root")
    rows = data.get("rows", [])
    if not rows:
        errors.append("migration inventory has no rows")
    seen: set[str] = set()
    migrated = planned = 0
    for index, row in enumerate(rows, 1):
        source = row.get("source", "")
        label = f"row #{index} ({source or '?'})"
        if not source or not row.get("destination"):
            errors.append(f"{label} needs source and destination")
        if row.get("class") not in CLASSES:
            errors.append(f"{label} class must be one of {', '.join(CLASSES)}")
        status = row.get("status")
        if status not in STATUSES:
            errors.append(f"{label} status must be one of {', '.join(STATUSES)}")
        elif status == "migrated":
            migrated += 1
        elif status == "planned":
            planned += 1
        if source in seen:
            errors.append(f"{label} duplicates source {source}")
        seen.add(source)
        blob = json.dumps(row)
        if SECRET_VALUES.search(blob):
            errors.append(f"{label} contains a secret-shaped value")
    if rows and planned == 0:
        errors.append("migration inventory claims nothing is left planned")
    if rows and migrated == 0:
        errors.append("migration inventory has no migrated row")
    return errors


def config_view(profile: Profile, *, limit: int) -> dict:
    return redact("", {
        "schema": SCHEMA,
        "project": profile.name,
        "max_open_prs": limit,
        "concurrency": profile.concurrency,
        "timeout_seconds": profile.timeout_seconds,
        "retry": profile.retry,
        "backoff_seconds": profile.backoff_seconds,
        "plugin_allow": list(profile.plugin_allow),
        "inventory": profile.inventory,
        "distributed_claim_lock": "not implemented",
    })


def _manifest_path(root: Path, manifest: str) -> Path | None:
    candidate = (root / manifest).resolve()
    root_resolved = root.resolve()
    if candidate != root_resolved and root_resolved not in candidate.parents:
        return None
    return candidate


def _read_manifest(path: Path | None) -> dict:
    if path is None:
        return {}
    try:
        return tomllib.loads(path.read_text(encoding="utf-8"))
    except (OSError, tomllib.TOMLDecodeError):
        return {}


def _manifest_errors(plugin_id: str, path: Path) -> list[str]:
    data = _read_manifest(path)
    errors = []
    if data.get("id") != plugin_id:
        errors.append(f"plugin {plugin_id} manifest id is {data.get('id')!r}")
    if data.get("kind") not in PLUGIN_KINDS:
        errors.append(f"plugin {plugin_id} manifest kind is invalid")
    capabilities = data.get("capabilities", [])
    if not isinstance(capabilities, list) or not capabilities:
        errors.append(f"plugin {plugin_id} manifest needs capabilities")
    command = data.get("command")
    if command is not None and (
        not isinstance(command, list) or not all(isinstance(part, str) for part in command)
    ):
        errors.append(f"plugin {plugin_id} command must be an argv list of strings")
    return errors
