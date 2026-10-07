#!/usr/bin/env python3
"""The Foreman's view of the system: reads workspace/modules.yaml (issue #1292, ADR-026).

  python3 workspace/modules.py list                     modules, dependencies first
  python3 workspace/modules.py affected <path>...       modules a change touches, plus their dependents
  python3 workspace/modules.py verify <module>|--all [--dry-run]
"""
import argparse
import subprocess
import sys
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[1]
MANIFEST = REPO_ROOT / "workspace" / "modules.yaml"


def load_modules():
    return yaml.safe_load(MANIFEST.read_text(encoding="utf-8"))["modules"]


def ordered(modules):
    result, seen = [], set()

    def visit(name):
        if name in seen:
            return
        seen.add(name)
        for dependency in modules[name]["depends_on"]:
            visit(dependency)
        result.append(name)

    for name in modules:
        visit(name)
    return result


def owned_paths(module):
    return [module["path"], *module.get("extra_paths", [])]


def touched(modules, paths):
    return {
        name for name, module in modules.items()
        for path in paths for owned in owned_paths(module)
        if path == owned or path.startswith(owned + "/")
    }


def affected(modules, paths):
    result = touched(modules, paths)
    grew = True
    while grew:
        grew = False
        for name, module in modules.items():
            if name not in result and result & set(module["depends_on"]):
                result.add(name)
                grew = True
    return [name for name in ordered(modules) if name in result]


def verify(modules, names, dry_run):
    for name in names:
        command = modules[name]["verify"]
        print(command)
        if not dry_run and subprocess.run(command, shell=True, cwd=REPO_ROOT).returncode != 0:
            print(f"verify failed: {name}", file=sys.stderr)
            return 1
    return 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("list")
    sub.add_parser("affected").add_argument("paths", nargs="+")
    check = sub.add_parser("verify")
    check.add_argument("module", nargs="?")
    check.add_argument("--all", action="store_true")
    check.add_argument("--dry-run", action="store_true")
    args = parser.parse_args(argv)
    modules = load_modules()
    if args.command == "list":
        print("\n".join(ordered(modules)))
    elif args.command == "affected":
        print("\n".join(affected(modules, args.paths)))
    else:
        if not args.all and args.module not in modules:
            print(f"unknown module: {args.module}", file=sys.stderr)
            return 2
        names = ordered(modules) if args.all else [args.module]
        return verify(modules, names, args.dry_run)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
