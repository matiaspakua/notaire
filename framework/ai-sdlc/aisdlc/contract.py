"""Factory control OpenAPI 3.1.0 contract (#2136).

The document under `openapi/factory-control.openapi.yaml` describes CLI JSON payloads
and the ``aisdlc serve`` HTTP adapter (#2253). The kit does not import the adopter's
product OpenAPI. Breaking changes are checked with oasdiff when installed; accepted
breaks live in `openapi/accepted-breaking-changes.d/`.
"""
from __future__ import annotations

import re
from pathlib import Path

from aisdlc.kitroot import kit_root

KIT_ROOT = kit_root()
CONTRACT_REL = "openapi/factory-control.openapi.yaml"
ACCEPT_DIR_REL = "openapi/accepted-breaking-changes.d"
REQUIRED_PATHS = (
    "/factory/capabilities",
    "/factory/config",
    "/factory/plugins",
    "/factory/harness",
    "/factory/report",
    "/factory/catalogs/{kind}",
    "/factory/workflows",
)
SCHEMA_MARKERS = (
    "FactoryEntity",
    "EntityVersion",
    "Workflow",
    "WorkflowGovernanceState",
    "FactoryCapabilities",
)
CONFIG_REQUIRED = (
    "schema",
    "project",
    "max_open_prs",
    "concurrency",
    "timeout_seconds",
    "retry",
    "backoff_seconds",
    "plugin_allow",
)
PRODUCT_MARKERS = re.compile(
    r"(backend" + r"-api|frontend/src|product OpenAPI|/v3/api-docs)",
    re.I,
)


def contract_path(kit_root: Path | None = None) -> Path:
    return (kit_root or KIT_ROOT) / CONTRACT_REL


def accept_dir(kit_root: Path | None = None) -> Path:
    return (kit_root or KIT_ROOT) / ACCEPT_DIR_REL


def load_contract_text(kit_root: Path | None = None) -> str:
    path = contract_path(kit_root)
    if not path.is_file():
        raise FileNotFoundError(f"factory contract missing: {path}")
    return path.read_text(encoding="utf-8")


def validate_contract(kit_root: Path | None = None) -> list[str]:
    """Structural checks without requiring PyYAML or a network."""
    errors: list[str] = []
    text = load_contract_text(kit_root)
    if not re.search(r"(?m)^openapi:\s*[\"']?3\.1\.0[\"']?\s*$", text):
        errors.append("openapi version must be 3.1.0 (not 3.2)")
    if PRODUCT_MARKERS.search(text):
        errors.append("contract must not reference product OpenAPI / adopter trees")
    for path in REQUIRED_PATHS:
        if path not in text:
            errors.append(f"missing path {path}")
    for key in CONFIG_REQUIRED:
        if f"{key}:" not in text and f"{key} :" not in text:
            # required listed under FactoryConfig properties
            if key not in text:
                errors.append(f"FactoryConfig should document property {key}")
    if "CatalogList" not in text:
        errors.append("catalogs schema CatalogList is required for plugin-friendly extension")
    for marker in SCHEMA_MARKERS:
        if marker not in text:
            errors.append(f"missing schema {marker} (#2239)")
    return errors


def config_keys_match_contract(config: dict, kit_root: Path | None = None) -> list[str]:
    """CLI `config show --json` keys must cover the contract's required FactoryConfig fields."""
    text = load_contract_text(kit_root)
    missing = [key for key in CONFIG_REQUIRED if key not in config]
    extras_ok = True  # additionalProperties allowed
    del extras_ok
    if "FactoryConfig" not in text:
        missing.append("FactoryConfig schema absent")
    return [f"config payload missing required key {k}" for k in missing]


def check(kit_root: Path | None = None) -> int:
    errors = validate_contract(kit_root)
    if errors:
        print("factory contract invalid:")
        for error in errors:
            print(f"  - {error}")
        return 1
    print(f"factory contract OK · OpenAPI 3.1.0 · {contract_path(kit_root).relative_to(kit_root or KIT_ROOT)}")
    print(f"accepted breaks dir: {accept_dir(kit_root).relative_to(kit_root or KIT_ROOT)}")
    return 0
