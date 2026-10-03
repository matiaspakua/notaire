#!/usr/bin/env python3
"""Validate CU-API-MATRIX.csv against live English REST controllers (#1064, CU76).

Checks:
  1. Every non-N/A Controller value is a @RestController under adapter.in.web
  2. Required resource base paths appear in the Endpoint column
  3. Bruno_Test is N/A, MISSING, a folder root (…/), or a .yml path — never DONE/OK
  4. Bruno_Test=MISSING rows cite #953 in Notas and/or GitHub_Issue

Exit 0 on success; non-zero with a human-readable report on failure.
"""

from __future__ import annotations

import argparse
import csv
import re
import sys
from pathlib import Path

REQUIRED_BASES = (
    "/carpetas",
    "/cuadernos",
    "/minutas-inscripcion",
    "/plantilla-costos-documento",
    "/protocolo-auxiliar",
    "/roles",
    "/tipo-identificacion",
    "/tramites",
)

FORBIDDEN_BRUNO_STATUS = frozenset({"DONE", "OK", "PASS", "PASSED", "FAIL", "FAILED"})
BRUNO_PATH_RE = re.compile(r"^[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*\.yml$")
BRUNO_FOLDER_RE = re.compile(r"^[A-Za-z0-9._-]+(?:/[A-Za-z0-9._-]+)*/$")
CLASS_RE = re.compile(r"\b(?:public\s+)?class\s+(\w+Controller)\b")
MAPPING_RE = re.compile(
    r'@RequestMapping\s*(?:\(\s*(?:value\s*=\s*)?["\']([^"\']+)["\'])'
)
REST_RE = re.compile(r"@RestController\b")


def discover_controllers(root: Path) -> dict[str, str]:
    """Return {ClassName: requestMapping} for adapter.in.web REST controllers."""
    web = root / "backend-api" / "src" / "main" / "java" / "com" / "licensis" / "notaire" / "adapter" / "in" / "web"
    found: dict[str, str] = {}
    if not web.is_dir():
        return found
    for path in sorted(web.rglob("*Controller.java")):
        text = path.read_text(encoding="utf-8")
        if not REST_RE.search(text):
            continue
        class_match = CLASS_RE.search(text)
        if not class_match:
            continue
        mapping_match = MAPPING_RE.search(text)
        mapping = mapping_match.group(1) if mapping_match else ""
        found[class_match.group(1)] = mapping
    return found


def load_matrix(matrix: Path) -> list[dict[str, str]]:
    with matrix.open(newline="", encoding="utf-8") as fh:
        return list(csv.DictReader(fh))


def validate(root: Path, matrix_path: Path) -> list[str]:
    errors: list[str] = []
    if not matrix_path.is_file():
        return [f"matrix file not found: {matrix_path}"]

    controllers = discover_controllers(root)
    if not controllers:
        errors.append(f"no REST controllers discovered under {root}/backend-api/.../adapter/in/web")

    rows = load_matrix(matrix_path)
    if not rows:
        errors.append(f"matrix has no data rows: {matrix_path}")
        return errors

    endpoints_blob = " ".join((row.get("Endpoint") or "") for row in rows)

    for name in sorted(controllers):
        # Every live controller class must appear at least once (or be unreachable
        # only if we later add an allowlist — for now require presence).
        if not any((row.get("Controller") or "") == name for row in rows):
            errors.append(f"live controller missing from matrix: {name}")

    for index, row in enumerate(rows, start=2):
        cu = row.get("CU_ID") or f"row {index}"
        controller = (row.get("Controller") or "").strip()
        bruno = (row.get("Bruno_Test") or "").strip()
        notes = row.get("Notas") or ""
        issue = row.get("GitHub_Issue") or ""

        if controller and controller != "N/A" and controller not in controllers:
            errors.append(
                f"{cu}: stale or unknown Controller '{controller}' "
                f"(expected a live adapter.in.web REST controller or N/A)"
            )

        if bruno in FORBIDDEN_BRUNO_STATUS:
            errors.append(
                f"{cu}: Bruno_Test must not be status word '{bruno}' "
                f"(use a path, MISSING, or N/A)"
            )
        elif bruno not in {"N/A", "MISSING"} and bruno:
            if not (BRUNO_PATH_RE.fullmatch(bruno) or BRUNO_FOLDER_RE.fullmatch(bruno)):
                errors.append(
                    f"{cu}: Bruno_Test '{bruno}' is not N/A, MISSING, "
                    f"a folder root ending in '/', or a .yml path"
                )

        if bruno == "MISSING" and "#953" not in notes and "#953" not in issue:
            errors.append(
                f"{cu}: Bruno_Test=MISSING must cite #953 in Notas or GitHub_Issue"
            )

    for base in REQUIRED_BASES:
        if base not in endpoints_blob:
            errors.append(f"missing required resource base: {base}")

    return errors


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="repository root (default: parent of scripts/)",
    )
    parser.add_argument(
        "--matrix",
        type=Path,
        default=None,
        help="path to CU-API-MATRIX.csv (default: docs/.../CU-API-MATRIX.csv under --root)",
    )
    args = parser.parse_args(argv)
    root = args.root.resolve()
    matrix = (
        args.matrix.resolve()
        if args.matrix is not None
        else root / "docs" / "300-development" / "303-testing" / "CU-API-MATRIX.csv"
    )

    errors = validate(root, matrix)
    if errors:
        print("CU-API-MATRIX validation FAILED:", file=sys.stderr)
        for err in errors:
            print(f"  - {err}", file=sys.stderr)
        return 1

    print(
        f"CU-API-MATRIX validation OK ({matrix.relative_to(root) if matrix.is_relative_to(root) else matrix})"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
