#!/usr/bin/env python3
"""Which preflight suite groups a branch touches (#2413, CU76).

Prints the groups as space-separated words, `all` when everything must run, or `none`.

    python3 workspace/sdlc/preflight_affected.py --base origin/main
"""
from __future__ import annotations

import argparse
import subprocess
import sys

GROUPS = {
    "backend": ("backend-api/", "contracts/"),
    "frontend": ("frontend/",),
    "testing": ("testing/",),
    "pages": ("github-page/", "docs/", "CHANGELOG.md", "release/", "workspace/release/"),
    "stack": ("backend-api/", "frontend/", "testing/", "infra/", "contracts/"),
    "kit": ("framework/ai-sdlc/", "local-ai/", ".aisdlc/"),
    "boundaries": ("framework/",),
}
ALL = frozenset(GROUPS)

# Shared infrastructure every suite depends on: a change here runs everything.
FORCE_ALL = ("workspace/sdlc/preflight.sh", "workspace/sdlc/preflight_affected.py", ".githooks/",
             "workspace/stack/", "pom.xml")


def _forces_all(path: str) -> bool:
    name = path.rsplit("/", 1)[-1]
    return (path.startswith(FORCE_ALL) or name.startswith("docker-compose") or name.startswith("Dockerfile"))


def groups(paths: list[str] | None) -> frozenset[str] | set[str]:
    if paths is None or any(_forces_all(p) for p in paths):
        return ALL
    return {name for name, prefixes in GROUPS.items() if any(p.startswith(prefixes) for p in paths)}


def render(selected: frozenset[str] | set[str]) -> str:
    if selected == ALL:
        return "all"
    return " ".join(sorted(selected)) or "none"


def changed_paths(base: str) -> list[str] | None:
    result = subprocess.run(["git", "diff", "--name-only", f"{base}...HEAD"], capture_output=True, text=True)
    if result.returncode != 0:
        return None
    return [line for line in result.stdout.splitlines() if line]


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base", default="origin/main")
    args = parser.parse_args(argv)
    print(render(groups(changed_paths(args.base))))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
