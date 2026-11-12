"""Harness port: read agent-runtime adapters declared by the profile.

The kit never names an adopter path. Sources stay where discoverability already
lives. A permission listed in a source file is not a sandbox. Neither adapter is
a commercial Harness product.

Harness-kind plugins (#2188) are inventory stubs: list/show/plan never launch a
remote agent. sandbox is always false until a real Isolator exists.
"""
from __future__ import annotations

import tomllib
from pathlib import Path

from aisdlc.profile import Profile, ProfileError


def list_adapters(root: Path, profile: Profile) -> list[dict]:
    rows: list[dict] = []
    seen: set[str] = set()
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
        seen.add(adapter.id)
    allow = set(profile.plugin_allow)
    for plugin in profile.plugins:
        if plugin.kind != "harness" or plugin.id in seen:
            continue
        source, runtime = _stub_source_and_runtime(root, plugin.manifest, plugin.id)
        path = _source_path(root, source) if source else None
        rows.append({
            "id": plugin.id,
            "runtime": runtime or plugin.id,
            "source": source or plugin.manifest,
            "readable": path is not None and path.is_file(),
            "sandbox": False,
            "commercial_harness_product": False,
            "stub": True,
            "available": plugin.trusted and plugin.id in allow,
        })
        seen.add(plugin.id)
    return rows


def describe(root: Path, profile: Profile, adapter_id: str) -> dict:
    adapter = next((item for item in profile.harness_adapters if item.id == adapter_id), None)
    stub = False
    source = ""
    runtime = ""
    if adapter is not None:
        source = adapter.source
        runtime = adapter.runtime
    else:
        plugin = next(
            (item for item in profile.plugins if item.kind == "harness" and item.id == adapter_id),
            None,
        )
        if plugin is None:
            raise ProfileError(f"unknown harness adapter {adapter_id!r}")
        source, runtime = _stub_source_and_runtime(root, plugin.manifest, plugin.id)
        if not source or not runtime:
            raise HarnessError(f"harness {adapter_id} stub source is missing: {plugin.manifest}")
        stub = True

    path = _source_path(root, source)
    if path is None or not path.is_file():
        raise HarnessError(f"harness {adapter_id} source is missing: {source}")
    data = extract_fields(path.read_text(encoding="utf-8"))
    file_runtime = data.get("runtime")
    if isinstance(file_runtime, str) and file_runtime and file_runtime != runtime:
        raise HarnessError(
            f"harness {adapter_id} runtime {runtime!r} does not match source runtime {file_runtime!r}"
        )
    view = {
        "id": adapter_id,
        "runtime": runtime,
        "source": source,
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
    if stub:
        view["stub"] = True
    return view


class HarnessError(Exception):
    """A harness source failed a closed check. Maps to exit code 1."""


def report_event(
    ctx,
    *,
    run_id: str,
    event: str,
    adapter: str = "local-worker",
    step: str | None = None,
    step_status: str | None = None,
    ok: bool | None = None,
    budget_overrun: bool = False,
    cancelled: bool = False,
    blocked: bool = False,
) -> dict:
    """Local-worker reference adapter: append a harness run event (#2255).

    Never launches agents. Hash-chain append only — see docs/HARNESS-REPORTING.md.
    """
    from aisdlc import runs

    return runs.append_harness_event(
        ctx,
        run_id=run_id,
        event=event,
        adapter=adapter,
        step=step,
        step_status=step_status,
        ok=ok,
        budget_overrun=budget_overrun,
        cancelled=cancelled,
        blocked=blocked,
    )


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


def _stub_source_and_runtime(root: Path, manifest: str, plugin_id: str) -> tuple[str, str]:
    """Resolve adapter.yml path (relative to root) and runtime from a harness plugin."""
    manifest_path = _source_path(root, manifest)
    if manifest_path is None or not manifest_path.is_file():
        return "", ""
    try:
        data = tomllib.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, tomllib.TOMLDecodeError):
        return "", ""
    runtime = str(data.get("runtime") or plugin_id)
    adapter_name = str(data.get("adapter") or "adapter.yml")
    adapter_rel = str((Path(manifest).parent / adapter_name).as_posix())
    return adapter_rel, runtime


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
