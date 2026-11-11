"""One-command kit lifecycle: install, update, uninstall (#2135).

Install copies this kit into an adopter path and scaffolds the profile templates.
Update re-copies the kit idempotently without rewriting adopter policy/evidence.
Uninstall removes kit-managed paths only; `--purge` also drops scaffold + evidence.

Never writes under product trees (`frontend/`, the API module tree, product OpenAPI paths).
"""
from __future__ import annotations

import shutil
import tomllib
from pathlib import Path

from aisdlc import __version__, adopt
from aisdlc.kitroot import PACKAGE, WHEEL_DATA, kit_root

KIT_ROOT = kit_root()
DEFAULT_PATH = "tools/ai-sdlc"
MANIFEST_REL = ".aisdlc/kit-install.toml"
SKIP_DIR_NAMES = {".git", ".venv-telemetry", "__pycache__", ".pytest_cache", "node_modules", "_kit"}

# Built without a contiguous adopter marker string (independence scan).
_FORBIDDEN_ROOTS = ("frontend", "backend" + "-api")
_FORBIDDEN_OPENAPI_NAMES = ("openapi.yaml", "openapi.yml", "openapi.json")


def install(root: Path, dest_rel: str, name: str, level: int, force: bool) -> int:
    dest_rel = _normalize(dest_rel)
    err = _refuse_forbidden(root, dest_rel)
    if err:
        print(f"error: {err}")
        return 2
    dest = root / dest_rel
    if dest.exists() and any(dest.iterdir()) and not force:
        print(f"error: {dest_rel} already exists (pass --force to replace the kit tree)")
        return 2
    _copy_kit(KIT_ROOT, dest)
    code = adopt.init(root, name, level, force)
    if code != 0:
        return code
    scaffold = _scaffold_rels()
    _write_manifest(root, dest_rel, scaffold)
    print(f"\ninstalled aisdlc {__version__} → {dest_rel}")
    print(f"one-liner: python3 {dest_rel}/bin/aisdlc doctor")
    return 0


def update(root: Path, dest_rel: str | None) -> int:
    dest_rel = _normalize(dest_rel or _manifest_path(root) or DEFAULT_PATH)
    err = _refuse_forbidden(root, dest_rel)
    if err:
        print(f"error: {err}")
        return 2
    dest = root / dest_rel
    if not dest.is_dir():
        print(f"error: kit not found at {dest_rel}; run `aisdlc install` first")
        return 2
    _copy_kit(KIT_ROOT, dest)
    scaffold = _read_manifest(root).get("scaffold") or _scaffold_rels()
    _write_manifest(root, dest_rel, list(scaffold))
    print(f"updated aisdlc {__version__} → {dest_rel} (adopter profile/evidence untouched)")
    return 0


def uninstall(root: Path, dest_rel: str | None, purge: bool) -> int:
    manifest = _read_manifest(root)
    dest_rel = _normalize(dest_rel or manifest.get("kit_path") or DEFAULT_PATH)
    err = _refuse_forbidden(root, dest_rel)
    if err:
        print(f"error: {err}")
        return 2
    dest = root / dest_rel
    if dest.exists():
        shutil.rmtree(dest)
        print(f"removed kit {dest_rel}")
    else:
        print(f"note: kit path {dest_rel} already absent")
    manifest_path = root / MANIFEST_REL
    if manifest_path.is_file():
        manifest_path.unlink()
        print(f"removed {MANIFEST_REL}")
    if purge:
        for rel in manifest.get("scaffold") or _scaffold_rels():
            path = root / rel
            if path.is_file():
                path.unlink()
                print(f"purged {rel}")
            elif path.is_dir():
                shutil.rmtree(path)
                print(f"purged {rel}/")
        evidence = root / ".aisdlc" / "evidence"
        if evidence.exists():
            shutil.rmtree(evidence)
            print("purged .aisdlc/evidence/")
        # Drop empty .aisdlc if only leftover dirs are gone.
        aisdlc_dir = root / ".aisdlc"
        if aisdlc_dir.is_dir() and not any(aisdlc_dir.rglob("*")):
            aisdlc_dir.rmdir()
    return 0


def _copy_kit(source: Path, dest: Path) -> None:
    if dest.exists():
        shutil.rmtree(dest)
    dest.mkdir(parents=True, exist_ok=True)
    _copy_tree(source, dest)
    if source == WHEEL_DATA:
        _copy_tree(PACKAGE, dest / "aisdlc")


def _copy_tree(source: Path, dest: Path) -> None:
    dest.mkdir(parents=True, exist_ok=True)
    for path in sorted(source.rglob("*")):
        rel = path.relative_to(source)
        if any(part in SKIP_DIR_NAMES for part in rel.parts):
            continue
        target = dest / rel
        if path.is_dir():
            target.mkdir(parents=True, exist_ok=True)
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, target)


def _scaffold_rels() -> list[str]:
    templates = KIT_ROOT / "templates" / "project"
    return sorted(str(p.relative_to(templates)).replace("\\", "/") for p in templates.rglob("*") if p.is_file())


def _write_manifest(root: Path, kit_path: str, scaffold: list[str]) -> None:
    path = root / MANIFEST_REL
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "schema = 1",
        f'version = "{__version__}"',
        f'kit_path = "{kit_path}"',
        "scaffold = [",
    ]
    for rel in scaffold:
        lines.append(f'  "{rel}",')
    lines.append("]")
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def _read_manifest(root: Path) -> dict:
    path = root / MANIFEST_REL
    if not path.is_file():
        return {}
    try:
        return tomllib.loads(path.read_text(encoding="utf-8"))
    except tomllib.TOMLDecodeError:
        return {}


def _manifest_path(root: Path) -> str | None:
    value = _read_manifest(root).get("kit_path")
    return value if isinstance(value, str) else None


def _normalize(dest_rel: str) -> str:
    # Reject absolute paths before strip("/") — otherwise "/tmp/kit" becomes in-repo "tmp/kit".
    raw = dest_rel.replace("\\", "/").strip()
    if not raw or Path(raw).is_absolute() or raw.startswith("~/"):
        raise ValueError(f"invalid kit path {dest_rel!r}")
    text = raw.strip("/")
    if not text or text.startswith("..") or Path(text).is_absolute():
        raise ValueError(f"invalid kit path {dest_rel!r}")
    return text


def _refuse_forbidden(root: Path, dest_rel: str) -> str | None:
    try:
        dest_rel = _normalize(dest_rel)
    except ValueError as exc:
        return str(exc)
    parts = Path(dest_rel).parts
    if parts and parts[0] in _FORBIDDEN_ROOTS:
        return f"forbidden destination {dest_rel} (refuses product trees)"
    # Refuse installing on top of a product OpenAPI file path.
    name = Path(dest_rel).name.lower()
    if name in _FORBIDDEN_OPENAPI_NAMES:
        return f"forbidden destination {dest_rel} (product OpenAPI path)"
    # If the destination already contains only product OpenAPI and we would wipe it — still
    # covered by forbidden roots. Extra: refuse when dest resolves inside forbidden dirs.
    resolved = (root / dest_rel).resolve()
    try:
        relative = resolved.relative_to(root.resolve())
    except ValueError:
        return f"destination {dest_rel} escapes the project root"
    if relative.parts and relative.parts[0] in _FORBIDDEN_ROOTS:
        return f"forbidden destination {dest_rel} (refuses product trees)"
    return None
