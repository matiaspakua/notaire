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

RELEASE_ASSETS = (
    "sbom-backend.cdx.json",
    "sbom-frontend.cdx.json",
    "sbom-database.cdx.json",
    "sbom-notaire.cdx.json",
    "product-release.json",
)

SEMVER_RE = re.compile(r"^\d+\.\d+\.\d+$")
V_TAG_RE = re.compile(r"^v(\d+\.\d+\.\d+)$")
LEGACY_TAG_RE = re.compile(r"^notaire-v(\d+\.\d+\.\d+)$")
GHCR_IMAGE_RE = re.compile(
    r"ghcr\.io/matiaspakua/notaire/(backend|frontend):([^\s#]+)"
)
RELEASE_PLEASE_MARKER = "# x-release-please-version"
GHCR_PREFIX = "ghcr.io/matiaspakua/notaire/"
IDENTITY_COMPOSE_FILES = ("docker-compose.yml", "docker-compose.prod.yml")
STAGING_KUSTOMIZE = "infra/deploy/kustomize/overlays/staging/kustomization.yaml"
RELEASE_PLEASE_IDENTITY_PATHS = frozenset(
    {
        "docker-compose.yml",
        "docker-compose.prod.yml",
        STAGING_KUSTOMIZE,
    }
)
UNFILTERED_MAVEN_TOKEN = "@project.version@"

TRAIN_V10 = "2026-11 · Release blockers"
TRAIN_V11 = "2027-01 · Quality & traceability"

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

    0.x and 1.0.x advance the release-blocker gate. They do not close it.
    1.1.x advances the quality gate. Any other minor fails closed so a new
    release-gate mapping is an explicit decision, not an accident.
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


def tag_is_valid(tag: str, version: str | None = None) -> tuple[bool, str]:
    """Return (ok, message). Legacy tags are shape-only; vX.Y.Z must match version when given."""
    if tag in LEGACY_COMPONENT_TAGS.values():
        match = LEGACY_TAG_RE.match(tag)
        if not match:
            return False, f"tag {tag} is not a valid legacy release tag"
        legacy_version = match.group(1)
        if legacy_version not in LEGACY_COMPONENT_TAGS:
            return False, f"legacy tag {tag} is not in LEGACY_COMPONENT_TAGS"
        return True, ""

    match = V_TAG_RE.match(tag)
    if not match:
        return False, f"tag {tag} must be vX.Y.Z or a listed legacy notaire-v* tag"
    semver = match.group(1)
    if not SEMVER_RE.fullmatch(semver):
        return False, f"tag {tag} does not use a three-part semver"
    if version is not None and semver != version:
        return False, f"tag {tag} != tree version {version}"
    return True, ""


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
    if "SNAPSHOT" in backend["version"].upper():
        errors.append(
            f"root pom.xml version {backend['version']} is a snapshot; "
            "release tags require a release semver"
        )
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


def _attach_asset_names_from_cd(text: str) -> set[str]:
    marker = "name: Attach SBOMs to Release"
    start = text.find(marker)
    if start < 0:
        return set()
    block = text[start:]
    files_idx = block.find("files:")
    if files_idx < 0:
        return set()
    lines = block[files_idx:].splitlines()[1:]
    names: set[str] = set()
    for line in lines:
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if not stripped.startswith("release-assets/"):
            break
        names.add(Path(stripped).name)
    return names


def check_sbom_snapshot(snapshot: dict) -> list[str]:
    errors: list[str] = []
    version = snapshot["version"]
    schema = snapshot["components"]["database"]["schemaVersion"]
    backend = snapshot["components"]["backend"]["version"]
    frontend = snapshot["components"]["frontend"]["version"]
    product = product_sbom(snapshot)
    database = database_sbom(snapshot)
    if product["metadata"]["component"]["version"] != version:
        errors.append("generated product SBOM version does not match the manifest")
    component_versions = {item["bom-ref"]: item["version"] for item in product["components"]}
    if component_versions.get("backend") != backend:
        errors.append("generated product SBOM backend version drift")
    if component_versions.get("frontend") != frontend:
        errors.append("generated product SBOM frontend version drift")
    if component_versions.get("database") != schema:
        errors.append("generated product SBOM database schema drift")
    if len(database["components"]) != snapshot["components"]["database"]["migrationCount"]:
        errors.append("generated database SBOM migration count drift")
    return errors


def check_cd_release_assets(root: Path) -> list[str]:
    errors: list[str] = []
    path = root / ".github" / "workflows" / "cd.yml"
    if not path.is_file():
        return [".github/workflows/cd.yml is missing"]
    text = _read(path)
    for asset in RELEASE_ASSETS:
        if asset not in text:
            errors.append(f"cd.yml does not reference release asset {asset}")
    attached = _attach_asset_names_from_cd(text)
    if attached and attached != set(RELEASE_ASSETS):
        missing = set(RELEASE_ASSETS) - attached
        extra = attached - set(RELEASE_ASSETS)
        if missing:
            errors.append(f"cd.yml attach list missing assets: {', '.join(sorted(missing))}")
        if extra:
            errors.append(f"cd.yml attach list has unexpected assets: {', '.join(sorted(extra))}")
    if "fail_on_unmatched_files: true" not in text:
        errors.append("cd.yml release upload must set fail_on_unmatched_files: true")
    if "group: release-assets-${{ github.ref }}" not in text:
        errors.append("cd.yml release job must declare concurrency group release-assets-${{ github.ref }}")
    return errors


def _image_tag_is_placeholder(tag: str) -> bool:
    lower = tag.lower()
    if lower == "latest":
        return True
    if "placeholder" in lower:
        return True
    if lower.startswith("sha-") or re.fullmatch(r"[0-9a-f]{7,40}", lower):
        return True
    return False


def check_image_identity(root: Path, version: str) -> list[str]:
    errors: list[str] = []
    for relative in IDENTITY_COMPOSE_FILES:
        path = root / relative
        if not path.is_file():
            errors.append(f"{relative} is missing")
            continue
        text = _read(path)
        for match in GHCR_IMAGE_RE.finditer(text):
            component, tag = match.group(1), match.group(2)
            if _image_tag_is_placeholder(tag):
                errors.append(
                    f"{relative} {component} image tag {tag} is not the released semver"
                )
            elif tag != version:
                errors.append(
                    f"{relative} {component} image tag {tag} != product version {version}"
                )
        for component in ("backend", "frontend"):
            needle = f"{GHCR_PREFIX}{component}:"
            if needle not in text:
                errors.append(f"{relative} missing ghcr image line for {component}")

    overlay = root / STAGING_KUSTOMIZE
    if not overlay.is_file():
        errors.append(f"{STAGING_KUSTOMIZE} is missing")
    else:
        text = _read(overlay)
        for line in text.splitlines():
            if "newTag:" in line:
                tag = line.split("newTag:", 1)[1].split("#", 1)[0].strip()
                if _image_tag_is_placeholder(tag):
                    errors.append(f"staging overlay newTag {tag} is not the released semver")
                elif tag != version:
                    errors.append(f"staging overlay newTag {tag} != product version {version}")
        if "app.kubernetes.io/version" not in text:
            errors.append("staging overlay must set app.kubernetes.io/version")
        elif version not in text:
            errors.append(f"staging overlay must reference product version {version}")

    config_path = root / "release-please-config.json"
    if config_path.is_file():
        package = json.loads(_read(config_path)).get("packages", {}).get(".", {})
        paths = {entry.get("path") for entry in package.get("extra-files", []) if entry.get("path")}
        missing = RELEASE_PLEASE_IDENTITY_PATHS - paths
        if missing:
            errors.append(
                "release-please extra-files missing identity paths: "
                + ", ".join(sorted(missing))
            )
    return errors


def check_packaged_version(packaged: Path, record_version: str) -> list[str]:
    errors: list[str] = []
    if not packaged.is_file():
        return [f"packaged application.properties not found at {packaged}"]
    app_version: str | None = None
    for line in _read(packaged).splitlines():
        stripped = line.strip()
        if stripped.startswith("app.version="):
            app_version = stripped.split("=", 1)[1].strip()
            break
    if app_version is None:
        return ["packaged application.properties has no app.version"]
    if app_version == UNFILTERED_MAVEN_TOKEN:
        return ["packaged app.version is the unfiltered @project.version@ token"]
    if app_version != record_version:
        return [
            f"packaged app.version {app_version} != release record version {record_version}"
        ]
    return errors


def check_tag_argument(tag: str, tree_version: str) -> list[str]:
    ok, message = tag_is_valid(tag, tree_version)
    if ok:
        if tag in LEGACY_COMPONENT_TAGS.values():
            return []
        return []
    return [message]


def coherence_errors(
    root: Path,
    *,
    tag: str | None = None,
    packaged: Path | None = None,
) -> list[str]:
    errors = list(check(root))
    try:
        snapshot = collect(root)
    except ValueError:
        return errors
    errors.extend(check_sbom_snapshot(snapshot))
    errors.extend(check_cd_release_assets(root))
    errors.extend(check_image_identity(root, snapshot["version"]))
    if tag:
        errors.extend(check_tag_argument(tag, snapshot["version"]))
    if packaged is not None:
        errors.extend(check_packaged_version(packaged, snapshot["version"]))
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
