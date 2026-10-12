#!/usr/bin/env python3
"""Product release record for Notaire (CU76, #1571).

A release is one semver tag with three components:

- backend — Maven version, equal to the release-please manifest
- frontend — npm version, equal to the same manifest
- database — highest Flyway migration id, recorded beside the tag

Delivery milestones are trains, not tags. ``train_for`` is the only map from
a product version to a train title in ``MILESTONES.md``.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import uuid
from pathlib import Path

# Tags cut before include-component-in-tag was turned off.
LEGACY_COMPONENT_TAGS = {
    "0.1.1": "notaire-v0.1.1",
    "0.1.2": "notaire-v0.1.2",
}

TRAIN_V10 = "v1.0 — Production Ready"
TRAIN_V11 = "v1.1 — Hardening"

RELEASE_HEADING = re.compile(
    r"^## \[(\d+\.\d+\.\d+)\]\([^)]*\) \((\d{4}-\d{2}-\d{2})\)\s*$",
    re.MULTILINE,
)
MIGRATION_FILE = re.compile(r"^V(\d+(?:\.\d+)*)__.+\.sql$")
POM_VERSION = re.compile(
    r"<artifactId>\s*notaire-root\s*</artifactId>\s*<version>\s*([^<\s]+)\s*</version>",
    re.MULTILINE,
)

CYCLONEDX_SPEC = "1.6"
SBOM_NAMESPACE = uuid.UUID("6ba7b811-9dad-11d1-80b4-00c04fd430c8")  # NAMESPACE_URL


def repo_root_from_here() -> Path:
    return Path(__file__).resolve().parents[2]


def train_for(version: str) -> str:
    """Map a product semver to a delivery-train title.

    0.x and 1.0.x advance the v1.0 train. They do not close it.
    1.1.x advances the hardening train. Any other minor fails closed so a
    new train is an explicit decision, not an accident.
    """
    major_s, minor_s, *_rest = version.split(".")
    major, minor = int(major_s), int(minor_s)
    if major < 1 or (major == 1 and minor == 0):
        return TRAIN_V10
    if major == 1 and minor == 1:
        return TRAIN_V11
    raise ValueError(
        f"no delivery train mapped for {version}; add the train in "
        "docs/300-development/305-project-management/RELEASES.md and train_for()"
    )


def tag_for(version: str) -> str:
    return LEGACY_COMPONENT_TAGS.get(version, f"v{version}")


def _read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def manifest_version(root: Path) -> str:
    data = json.loads(_read(root / ".release-please-manifest.json"))
    version = data.get(".")
    if not isinstance(version, str) or not version:
        raise ValueError("release-please manifest is missing the root component version")
    return version


def maven_version(root: Path) -> str:
    match = POM_VERSION.search(_read(root / "pom.xml"))
    if not match:
        raise ValueError("root pom.xml has no notaire-root version")
    return match.group(1)


def backend_parent_version(root: Path) -> str:
    match = POM_VERSION.search(_read(root / "backend-api" / "pom.xml"))
    if not match:
        raise ValueError("backend-api/pom.xml has no parent notaire-root version")
    return match.group(1)


def frontend_version(root: Path) -> str:
    data = json.loads(_read(root / "frontend" / "package.json"))
    version = data.get("version")
    if not isinstance(version, str) or not version:
        raise ValueError("frontend/package.json has no version")
    return version


def flyway_migrations(root: Path) -> list[tuple[tuple[int, ...], str, str]]:
    """Return (numeric version, version string, filename) sorted ascending."""
    directory = root / "backend-api" / "src" / "main" / "resources" / "db" / "migration"
    found: list[tuple[tuple[int, ...], str, str]] = []
    for path in sorted(directory.glob("V*.sql")):
        match = MIGRATION_FILE.match(path.name)
        if not match:
            continue
        version = match.group(1)
        numeric = tuple(int(part) for part in version.split("."))
        found.append((numeric, version, path.name))
    found.sort(key=lambda item: item[0])
    return found


def changelog_releases(root: Path) -> list[dict[str, str]]:
    """Release-please headings only. The legacy ``1.0.0-SNAPSHOT`` block is not a release."""
    text = _read(root / "CHANGELOG.md")
    releases = [
        {"version": match.group(1), "date": match.group(2)}
        for match in RELEASE_HEADING.finditer(text)
    ]
    return releases


def collect(root: Path) -> dict:
    migrations = flyway_migrations(root)
    if not migrations:
        raise ValueError("no Flyway versioned migrations found")
    schema = migrations[-1][1]
    version = manifest_version(root)
    return {
        "name": "notaire",
        "version": version,
        "train": train_for(version),
        "tag": tag_for(version),
        "components": {
            "backend": {
                "version": maven_version(root),
                "parentVersion": backend_parent_version(root),
                "coordinates": "com.licensis:notaire-root",
            },
            "frontend": {
                "version": frontend_version(root),
                "package": "notaire-frontend",
            },
            "database": {
                "engine": "flyway",
                "schemaVersion": schema,
                "migrationCount": len(migrations),
            },
        },
        "migrations": [
            {"version": version_s, "file": filename} for _numeric, version_s, filename in migrations
        ],
        "releases": changelog_releases(root),
    }


def product_release_document(snapshot: dict) -> dict:
    """Committed contract. The train is derived, not stored, so a release-please
    version bump cannot leave a stale train title on main.
    """
    backend = snapshot["components"]["backend"]["version"]
    frontend = snapshot["components"]["frontend"]["version"]
    database = snapshot["components"]["database"]
    return {
        "name": "notaire",
        "version": snapshot["version"],
        "components": {
            "backend": {
                "version": backend,
                "coordinates": "com.licensis:notaire-root",
            },
            "frontend": {
                "version": frontend,
                "package": "notaire-frontend",
            },
            "database": {
                "engine": "flyway",
                "schemaVersion": database["schemaVersion"],
                "migrationCount": database["migrationCount"],
            },
        },
    }


def pages_history(snapshot: dict) -> dict:
    current = snapshot["version"]
    releases = []
    for release in snapshot["releases"]:
        version = release["version"]
        releases.append(
            {
                "version": version,
                "date": release["date"],
                "tag": tag_for(version),
                "train": train_for(version),
                "url": f"https://github.com/matiaspakua/notaire/releases/tag/{tag_for(version)}",
            }
        )
    database = snapshot["components"]["database"]
    return {
        "productVersion": current,
        "train": snapshot["train"],
        "tag": snapshot["tag"],
        "components": {
            "backend": snapshot["components"]["backend"]["version"],
            "frontend": snapshot["components"]["frontend"]["version"],
            "databaseSchema": database["schemaVersion"],
        },
        "releases": releases,
    }


def _serial(name: str, version: str, schema: str) -> str:
    token = uuid.uuid5(SBOM_NAMESPACE, f"notaire:{name}:{version}:{schema}")
    return f"urn:uuid:{token}"


def _property(name: str, value: str) -> dict[str, str]:
    return {"name": name, "value": value}


def product_sbom(snapshot: dict) -> dict:
    version = snapshot["version"]
    schema = snapshot["components"]["database"]["schemaVersion"]
    backend = snapshot["components"]["backend"]["version"]
    frontend = snapshot["components"]["frontend"]["version"]
    return {
        "bomFormat": "CycloneDX",
        "specVersion": CYCLONEDX_SPEC,
        "serialNumber": _serial("sbom-notaire", version, schema),
        "version": 1,
        "metadata": {
            "component": {
                "type": "application",
                "bom-ref": "notaire",
                "name": "notaire",
                "version": version,
                "description": (
                    "Notaire product composition: backend, frontend, and database schema. "
                    "Image dependency SBOMs are separate release assets."
                ),
            },
            "properties": [
                _property("notaire:release-train", snapshot["train"]),
                _property("notaire:git-tag", snapshot["tag"]),
                _property("notaire:image-sbom-backend", "sbom-backend.cdx.json"),
                _property("notaire:image-sbom-frontend", "sbom-frontend.cdx.json"),
                _property("notaire:database-sbom", "sbom-database.cdx.json"),
            ],
        },
        "components": [
            {
                "type": "application",
                "bom-ref": "backend",
                "name": "notaire-backend",
                "version": backend,
                "purl": f"pkg:maven/com.licensis/notaire-root@{backend}",
            },
            {
                "type": "application",
                "bom-ref": "frontend",
                "name": "notaire-frontend",
                "version": frontend,
                "purl": f"pkg:npm/notaire-frontend@{frontend}",
            },
            {
                "type": "data",
                "bom-ref": "database",
                "name": "notaire-database",
                "version": schema,
                "purl": f"pkg:generic/notaire-database@{schema}",
                "description": "Flyway schema version. Not equal to the product semver.",
            },
        ],
    }


def database_sbom(snapshot: dict) -> dict:
    version = snapshot["version"]
    schema = snapshot["components"]["database"]["schemaVersion"]
    return {
        "bomFormat": "CycloneDX",
        "specVersion": CYCLONEDX_SPEC,
        "serialNumber": _serial("sbom-database", version, schema),
        "version": 1,
        "metadata": {
            "component": {
                "type": "data",
                "bom-ref": "notaire-database",
                "name": "notaire-database",
                "version": schema,
                "purl": f"pkg:generic/notaire-database@{schema}",
                "description": "Versioned Flyway migrations shipped with this product cut.",
            },
            "properties": [
                _property("notaire:product-version", version),
                _property("notaire:release-train", snapshot["train"]),
            ],
        },
        "components": [
            {
                "type": "file",
                "name": item["file"],
                "version": item["version"],
            }
            for item in snapshot["migrations"]
        ],
    }


def _dump(payload: dict) -> str:
    return json.dumps(payload, indent=2, ensure_ascii=False) + "\n"


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(_dump(payload), encoding="utf-8")


def check(root: Path) -> list[str]:
    """Return human-readable errors. Empty means the committed record matches the tree."""
    errors: list[str] = []
    try:
        snapshot = collect(root)
    except ValueError as exc:
        return [str(exc)]

    version = snapshot["version"]
    backend = snapshot["components"]["backend"]
    frontend = snapshot["components"]["frontend"]["version"]
    if backend["version"] != version:
        errors.append(
            f"root pom.xml version {backend['version']} != manifest {version} "
            "(release-please only rewrites a Maven version that already equals the last release)"
        )
    if backend["parentVersion"] != version:
        errors.append(
            f"backend-api parent version {backend['parentVersion']} != manifest {version}"
        )
    if frontend != version:
        errors.append(f"frontend/package.json version {frontend} != manifest {version}")

    releases = snapshot["releases"]
    if not releases:
        errors.append("CHANGELOG.md has no release-please version heading")
    elif releases[0]["version"] != version:
        errors.append(
            f"latest CHANGELOG release {releases[0]['version']} != manifest {version}"
        )

    document_path = root / "release" / "product-release.json"
    if not document_path.is_file():
        errors.append("release/product-release.json is missing")
    else:
        actual = json.loads(_read(document_path))
        expected = product_release_document(snapshot)
        if actual != expected:
            errors.append(
                "release/product-release.json does not match pom, package.json, "
                "manifest, and Flyway migrations; run "
                "python3 workspace/release/product_release.py --write"
            )

    config = json.loads(_read(root / "release-please-config.json"))
    package = config.get("packages", {}).get(".", {})
    if package.get("include-component-in-tag") is not False:
        errors.append(
            "release-please-config.json must set include-component-in-tag to false "
            "so tags are vX.Y.Z (legacy notaire-v* tags never matched CD's v* filter)"
        )
    extra = json.dumps(package.get("extra-files", []))
    for needle in (
        "pom.xml",
        "backend-api/pom.xml",
        "frontend/package.json",
        "release/product-release.json",
    ):
        if needle not in extra:
            errors.append(f"release-please extra-files missing {needle}")

    return errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=None)
    parser.add_argument("--check", action="store_true", help="fail when the committed record is stale")
    parser.add_argument("--write", action="store_true", help="write release/product-release.json")
    parser.add_argument("--write-pages", type=Path, default=None, help="write the Pages history JSON")
    parser.add_argument("--write-sbom", type=Path, default=None, help="directory for product and database SBOMs")
    args = parser.parse_args(argv)

    root = (args.root or repo_root_from_here()).resolve()
    if not args.check and not args.write and args.write_pages is None and args.write_sbom is None:
        args.check = True

    snapshot = collect(root)
    if args.write:
        write_json(root / "release" / "product-release.json", product_release_document(snapshot))
    if args.write_pages is not None:
        destination = args.write_pages
        if not destination.is_absolute():
            destination = Path.cwd() / destination
        write_json(destination, pages_history(snapshot))
    if args.write_sbom is not None:
        directory = args.write_sbom
        if not directory.is_absolute():
            directory = Path.cwd() / directory
        write_json(directory / "sbom-notaire.cdx.json", product_sbom(snapshot))
        write_json(directory / "sbom-database.cdx.json", database_sbom(snapshot))

    if args.check or args.write:
        errors = check(root)
        if errors:
            for error in errors:
                print(f"✗ {error}", file=sys.stderr)
            return 1
        print(f"✓ product release {snapshot['version']} ({snapshot['train']})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
