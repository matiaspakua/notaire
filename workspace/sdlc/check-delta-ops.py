#!/usr/bin/env python3
"""Check that each delta operation of an OpenSpec change matches the main spec (#1623, CU76).

`openspec validate --strict` accepts `## MODIFIED Requirements` for a capability
that does not exist yet; `openspec archive` then aborts, after the merge, when
nobody is looking. This runs at Gate 1 instead:

  ADDED              the requirement must not exist in the main spec yet
  MODIFIED/REMOVED   the main spec and the requirement must exist
  RENAMED            the `FROM:` requirement must exist

USAGE  python3 workspace/sdlc/check-delta-ops.py <change-dir>
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

SECTION = re.compile(r"^## (ADDED|MODIFIED|REMOVED|RENAMED) Requirements\s*$", re.M)
REQUIREMENT = re.compile(r"^### Requirement: (.+?)\s*$", re.M)
RENAMED_FROM = re.compile(r"FROM:\s*`?###\s*Requirement:\s*(.+?)`?\s*$", re.M)


def main_requirements(spec: Path) -> set[str] | None:
    if not spec.is_file():
        return None
    return set(REQUIREMENT.findall(spec.read_text(encoding="utf-8")))


def sections(text: str) -> list[tuple[str, str]]:
    parts = SECTION.split(text)
    return list(zip(parts[1::2], parts[2::2]))


def find_problems(change_dir: Path) -> list[str]:
    specs_root = change_dir.parents[1] / "specs"
    problems: list[str] = []
    for delta in sorted((change_dir / "specs").rglob("spec.md")):
        capability = delta.parent.relative_to(change_dir / "specs").as_posix()
        existing = main_requirements(specs_root / capability / "spec.md")
        for operation, body in sections(delta.read_text(encoding="utf-8")):
            names = RENAMED_FROM.findall(body) if operation == "RENAMED" else REQUIREMENT.findall(body)
            for name in names:
                if operation == "ADDED":
                    if existing and name in existing:
                        problems.append(f"{capability}: ADDED '{name}' already exists in the main spec — use MODIFIED")
                elif existing is None:
                    problems.append(f"{capability}: {operation} '{name}' targets a spec that does not exist — use ADDED")
                elif name not in existing:
                    problems.append(f"{capability}: {operation} '{name}' is not in the main spec — use ADDED or fix the name")
    return problems


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print(__doc__.strip().splitlines()[-1], file=sys.stderr)
        return 2
    problems = find_problems(Path(argv[1]).resolve())
    for problem in problems:
        print(problem)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
