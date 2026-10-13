#!/usr/bin/env python3
"""Blocking product version coherence gate (CU76, #1591).

Stdlib only: reads the working tree and reuses workspace/release/product_release.py.
Never calls gh, the network, or subprocess.
"""

from __future__ import annotations

import argparse
import importlib.util
import sys
from pathlib import Path


def repo_root_from_here() -> Path:
    return Path(__file__).resolve().parents[2]


def load_product_release():
    module_path = repo_root_from_here() / "workspace" / "release" / "product_release.py"
    spec = importlib.util.spec_from_file_location("product_release", module_path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {module_path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=None, help="repository root (default: auto)")
    parser.add_argument("--tag", default=None, help="git tag to validate (vX.Y.Z or legacy notaire-v*)")
    parser.add_argument(
        "--packaged",
        type=Path,
        default=None,
        help="path to filtered backend-api/target/classes/application.properties",
    )
    args = parser.parse_args(argv)

    root = (args.root or repo_root_from_here()).resolve()
    mod = load_product_release()
    errors = mod.coherence_errors(root, tag=args.tag, packaged=args.packaged)
    if errors:
        for error in errors:
            print(f"✗ {error}", file=sys.stderr)
        return 1

    snapshot = mod.collect(root)
    print(f"✓ product version coherent {snapshot['version']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
