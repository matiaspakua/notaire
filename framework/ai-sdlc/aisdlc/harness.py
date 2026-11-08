"""Harness port: read agent-runtime adapters declared by the profile.

The kit never names an adopter path. Sources stay where discoverability already
lives. A permission listed in a source file is not a sandbox. Neither adapter is
a commercial Harness product.
"""
from __future__ import annotations

from pathlib import Path

from aisdlc.profile import Profile, ProfileError


def list_adapters(root: Path, profile: Profile) -> list[dict]:
    rows = []
    for adapter in profile.harness_adapters:
        path = _source_path(root, adapter.source)
        rows.append({
            "id": adapter.id,
            "runtime": adapter.runtime,
            "source": adapter.source,
            "readable": path is not None and path.is_file(),
            "sandbox": False,
            "commercial_harness_product": False,
        })
    return rows


def describe(root: Path, profile: Profile, adapter_id: str) -> dict:
    adapter = next((item for item in profile.harness_adapters if item.id == adapter_id), None)
    if adapter is None:
        raise ProfileError(f"unknown harness adapter {adapter_id!r}")
    path = _source_path(root, adapter.source)
    if path is None or not path.is_file():
        raise HarnessError(f"harness {adapter.id} source is missing: {adapter.source}")
    data = extract_fields(path.read_text(encoding="utf-8"))
    file_runtime = data.get("runtime")
    if isinstance(file_runtime, str) and file_runtime and file_runtime != adapter.runtime:
        raise HarnessError(
            f"harness {adapter.id} runtime {adapter.runtime!r} does not match source runtime {file_runtime!r}"
        )
    return {
        "id": adapter.id,
        "runtime": adapter.runtime,
        "source": adapter.source,
        "version": data.get("version"),
        "project": data.get("project"),
        "foreman_id": data.get("foreman_id"),
        "roles": sorted(data.get("roles") or []),
        "agent": data.get("agent"),
        "profile": data.get("profile"),
        "sandbox": False,
        "commercial_harness_product": False,
        "note": "A permission listed in the source is not a sandbox.",
    }


class HarnessError(Exception):
    """A harness source failed a closed check. Maps to exit code 1."""


def extract_fields(text: str) -> dict:
    """Pull the few adapter fields we need from a YAML-like source (stdlib only).

    Full YAML is not required: fleet manifests and local project adapters are
    indentation maps. Nested lists under roles are ignored; only role *ids* matter.
    """
    result: dict = {"roles": []}
    section: str | None = None
    section_indent = -1

    for raw in text.splitlines():
        if not raw.strip() or raw.lstrip().startswith("#"):
            continue
        indent = len(raw) - len(raw.lstrip(" "))
        stripped = raw.strip()
        if section is not None and indent <= section_indent and not stripped.startswith("-"):
            section = None
            section_indent = -1

        if indent == 0 and stripped.endswith(":") and " " not in stripped[:-1]:
            name = stripped[:-1]
            if name in ("foreman", "roles", "backend"):
                section = name
                section_indent = 0
            continue

        if indent == 0 and ":" in stripped and not stripped.startswith("-"):
            key, _, value = stripped.partition(":")
            key, value = key.strip(), value.strip()
            if key in ("runtime", "version", "project") and value:
                result[key] = _unquote(value)
            continue

        if section == "foreman" and indent > section_indent and stripped.startswith("id:"):
            result["foreman_id"] = _unquote(stripped.partition(":")[2].strip())
            continue

        if section == "backend" and indent > section_indent and ":" in stripped:
            key, _, value = stripped.partition(":")
            key, value = key.strip(), value.strip()
            if key in ("agent", "profile") and value:
                result[key] = _unquote(value)
            continue

        if section == "roles" and indent == 2 and stripped.endswith(":") and not stripped.startswith("-"):
            role = stripped[:-1].strip()
            if role and role not in result["roles"]:
                result["roles"].append(role)

    return result


def _unquote(value: str) -> str:
    if "#" in value and not (len(value) >= 2 and value[0] in ("'", '"')):
        value = value.split("#", 1)[0].rstrip()
    if len(value) >= 2 and value[0] == value[-1] and value[0] in ("'", '"'):
        return value[1:-1]
    return value


def _source_path(root: Path, source: str) -> Path | None:
    candidate = (root / source).resolve()
    root_resolved = root.resolve()
    if candidate != root_resolved and root_resolved not in candidate.parents:
        return None
    return candidate
