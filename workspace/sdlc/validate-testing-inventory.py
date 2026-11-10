#!/usr/bin/env python3
"""
Validate testing discovery and Playwright TS-nnnn naming (#1511 / #2117 / CU76).

Exit non-zero when:
- any testing/e2e/tests/*.spec.ts lacks the TS-nnnn- prefix
- backend, frontend, or Bruno test trees are missing / unreadable

Prints discovered suite counts. Does NOT diff a committed count table (#2117).
"""
from __future__ import annotations

import importlib.util
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
E2E_TESTS = REPO / "testing" / "e2e" / "tests"
BACKEND_TEST = REPO / "backend-api" / "src" / "test"
FRONTEND_SRC = REPO / "frontend" / "src"
BRUNO = REPO / "backend-api" / "api-test"
GENERATE = Path(__file__).resolve().parent / "generate-testing-inventory.py"
TS_NAME = re.compile(r"^TS-\d{4}-.+\.spec\.ts$")


def _load_generate():
    spec = importlib.util.spec_from_file_location("generate_testing_inventory", GENERATE)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load {GENERATE}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def main() -> int:
    errors: list[str] = []

    if not E2E_TESTS.is_dir():
        errors.append(f"missing E2E tests dir: {E2E_TESTS}")
    else:
        offenders = sorted(
            path.name for path in E2E_TESTS.glob("*.spec.ts") if not TS_NAME.match(path.name)
        )
        if offenders:
            errors.append(
                "Playwright specs must match TS-nnnn-*.spec.ts; offenders: "
                + ", ".join(offenders)
            )

    if not BACKEND_TEST.is_dir():
        errors.append(f"missing readable backend test tree: {BACKEND_TEST}")
    if not FRONTEND_SRC.is_dir():
        errors.append(f"missing readable frontend src tree: {FRONTEND_SRC}")
    if not BRUNO.is_dir():
        errors.append(f"missing readable Bruno tree: {BRUNO}")

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        return 1

    gen = _load_generate()
    counts = gen.suite_counts()
    print(
        "testing inventory OK — "
        f"Playwright {counts['playwright']}, "
        f"backend {counts['backend']}, "
        f"Vitest {counts['vitest']}, "
        f"Bruno {counts['bruno']}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
