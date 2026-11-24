#!/usr/bin/env python3
"""Which preflight suite groups a branch touches (#2413, CU76).

Prints the groups as space-separated words, `all` when everything must run, or `none`.

    python3 workspace/sdlc/preflight_affected.py --base origin/main
"""
from __future__ import annotations

import argparse
import subprocess
import sys

# Build files select the groups they can break (#2497): compose files, a root Dockerfile and the stack scripts select
# only the server-backed suites, the root POM also the backend build. No path selects every group; `--all` before a
# release runs them all (#2493).
GROUPS = {
    "backend": ("backend-api/", "contracts/", "pom.xml"),
    "frontend": ("frontend/",),
    "testing": ("testing/",),
    "pages": ("github-page/", "docs/", "CHANGELOG.md", "release/", "workspace/release/"),
    "stack": ("backend-api/", "frontend/", "testing/", "infra/", "contracts/", "workspace/stack/", "docker-compose",
              "Dockerfile", "pom.xml"),
    "kit": ("framework/ai-sdlc/", "local-ai/", ".aisdlc/"),
    "boundaries": ("framework/",),
}
ALL = frozenset(GROUPS)


def groups(paths: list[str] | None) -> frozenset[str] | set[str]:
    if paths is None:
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
