#!/usr/bin/env python3
"""
Validate testing inventory and Playwright TS-nnnn naming (#1511 / CU76).

Exit non-zero when:
- any testing/e2e/tests/*.spec.ts lacks the TS-nnnn- prefix
- INVENTORY.md is missing or stale vs disk
"""
from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
E2E_TESTS = REPO / "testing" / "e2e" / "tests"
INVENTORY = REPO / "docs" / "300-development" / "303-testing" / "INVENTORY.md"
GENERATE = Path(__file__).resolve().parent / "generate-testing-inventory.py"
TS_NAME = re.compile(r"^TS-\d{4}-.+\.spec\.ts$")


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

    if not INVENTORY.is_file():
        errors.append(f"missing {INVENTORY.relative_to(REPO)} — run generate-testing-inventory.py")
    else:
        check = subprocess.run(
            ["python3", str(GENERATE), "--check"],
            cwd=REPO,
            capture_output=True,
            text=True,
            check=False,
        )
        if check.returncode != 0:
            msg = (check.stderr or check.stdout or "inventory stale").strip()
            errors.append(msg)

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        return 1

    print("testing inventory OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
