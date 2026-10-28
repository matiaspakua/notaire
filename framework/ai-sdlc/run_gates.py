#!/usr/bin/env python3
"""Stack-agnostic AI SDLC gate runner (#1958).

Loads a named profile from `profiles/<name>/profile.yaml` and either lists or
runs the configured gate commands relative to the repository root (cwd).

USAGE
  python3 framework/ai-sdlc/run_gates.py --profile notaire --list
  python3 framework/ai-sdlc/run_gates.py --profile notaire --dry-run
  python3 framework/ai-sdlc/run_gates.py --profile notaire

Profiles own all product-specific paths and commands. This runner must stay
free of product tree hardcodes so other adopters can copy the kit.
"""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

try:
    import yaml
except ImportError:  # pragma: no cover - stdlib fallback
    yaml = None  # type: ignore

KIT_ROOT = Path(__file__).resolve().parent
PROFILES = KIT_ROOT / "profiles"


def _load_yaml(path: Path) -> dict:
    text = path.read_text(encoding="utf-8")
    if yaml is not None:
        data = yaml.safe_load(text)
        if not isinstance(data, dict):
            raise SystemExit(f"profile must be a mapping: {path}")
        return data
    # Minimal YAML subset: key: value and nested gates list with id/command
    data: dict = {"gates": []}
    current_gate: dict | None = None
    for raw in text.splitlines():
        line = raw.split("#", 1)[0].rstrip()
        if not line.strip():
            continue
        if line.startswith(" ") or line.startswith("\t"):
            indented = line.strip()
            if indented.startswith("- "):
                current_gate = {}
                data["gates"].append(current_gate)
                indented = indented[2:].strip()
                if indented and ":" in indented:
                    k, v = indented.split(":", 1)
                    current_gate[k.strip()] = v.strip().strip("'\"")
            elif current_gate is not None and ":" in indented:
                k, v = indented.split(":", 1)
                current_gate[k.strip()] = v.strip().strip("'\"")
            continue
        if ":" in line:
            k, v = line.split(":", 1)
            key = k.strip()
            val = v.strip().strip("'\"")
            if key == "gates":
                continue
            data[key] = val
    return data


def load_profile(name: str) -> dict:
    path = PROFILES / name / "profile.yaml"
    if not path.is_file():
        known = sorted(p.name for p in PROFILES.iterdir() if p.is_dir()) if PROFILES.is_dir() else []
        raise SystemExit(
            f"unknown profile {name!r}: not found at {path}\n"
            f"known profiles: {', '.join(known) or '(none)'}"
        )
    profile = _load_yaml(path)
    profile["_path"] = str(path)
    profile.setdefault("name", name)
    profile.setdefault("gates", [])
    return profile


def list_gates(profile: dict) -> None:
    print(f"profile: {profile.get('name')}")
    print(f"source:  {profile.get('_path')}")
    gates = profile.get("gates") or []
    if not gates:
        print("gates:   (none)")
        return
    print(f"gates:   {len(gates)}")
    for gate in gates:
        gid = gate.get("id", "?")
        cmd = gate.get("command", "")
        print(f"  - {gid}: {cmd}")


def run_gates(profile: dict, *, dry_run: bool, cwd: Path) -> int:
    failed = 0
    for gate in profile.get("gates") or []:
        gid = gate.get("id", "?")
        cmd = gate.get("command")
        if not cmd:
            print(f"✗ {gid}: missing command", file=sys.stderr)
            failed += 1
            continue
        print(f"→ {gid}: {cmd}")
        if dry_run:
            continue
        result = subprocess.run(cmd, shell=True, cwd=cwd)
        if result.returncode != 0:
            print(f"✗ {gid} failed (exit {result.returncode})", file=sys.stderr)
            failed += 1
        else:
            print(f"✓ {gid}")
    return 1 if failed else 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--profile", required=True, help="profile name under profiles/")
    parser.add_argument("--list", action="store_true", help="list gates without running")
    parser.add_argument("--dry-run", action="store_true", help="print gates then exit 0")
    parser.add_argument(
        "--cwd",
        type=Path,
        default=Path.cwd(),
        help="repository root used as command cwd (default: process cwd)",
    )
    args = parser.parse_args(argv)
    try:
        profile = load_profile(args.profile)
    except SystemExit as exc:
        print(str(exc), file=sys.stderr)
        return 2
    if args.list or args.dry_run:
        list_gates(profile)
        if args.list:
            return 0
        return run_gates(profile, dry_run=True, cwd=args.cwd.resolve())
    return run_gates(profile, dry_run=False, cwd=args.cwd.resolve())


if __name__ == "__main__":
    sys.exit(main())
