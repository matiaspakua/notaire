#!/usr/bin/env python3
"""
Guards issue #1943 (CU78): the frontend lockfile resolves a patched katex.

katex arrives only via the docs linter:
markdownlint-cli2@0.24.0 -> markdownlint@0.42.0 -> micromark-extension-math@3.1.0
(katex@^0.16.0). No upstream release accepts the patched katex yet, so
frontend/package.json forces it with npm `overrides` (same pattern as the
smol-toml override, #1046).

Advisory range: >= 0.11.0 < 0.18.2; first patched 0.18.2.

Run with: python3 security/tests/test_katex_override.py
"""

from __future__ import annotations

import json
import re
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
FRONTEND = REPO_ROOT / "frontend"
FRONTEND_PACKAGE_JSON = FRONTEND / "package.json"
FRONTEND_LOCKFILE = FRONTEND / "package-lock.json"
MARKDOWNLINT_BIN = FRONTEND / "node_modules" / ".bin" / "markdownlint-cli2"

MIN_KATEX = (0, 18, 2)


def parse_semver(version: str) -> tuple[int, int, int]:
    match = re.match(r"^v?(\d+)\.(\d+)\.(\d+)", version.strip())
    if not match:
        raise ValueError(f"unparseable semver: {version!r}")
    return int(match.group(1)), int(match.group(2)), int(match.group(3))


def override_floor(value: object) -> tuple[int, int, int] | None:
    """Lowest version an override range admits (exact, ^, ~ or >=)."""
    if not isinstance(value, str):
        return None
    text = value.strip()
    for prefix in (">=", "^", "~", "="):
        if text.startswith(prefix):
            text = text[len(prefix):].lstrip()
            break
    try:
        return parse_semver(text)
    except ValueError:
        return None


def lockfile_versions(lock: dict, name: str) -> list[str]:
    versions: list[str] = []
    for key, meta in (lock.get("packages") or {}).items():
        if not isinstance(meta, dict):
            continue
        if key == f"node_modules/{name}" or key.endswith(f"/node_modules/{name}"):
            ver = meta.get("version")
            if isinstance(ver, str):
                versions.append(ver)
    return versions


class KatexOverrideTest(unittest.TestCase):
    def test_override_floor_helper(self):
        self.assertEqual(override_floor("^0.18.2"), (0, 18, 2))
        self.assertEqual(override_floor(">=0.18.2"), (0, 18, 2))
        self.assertEqual(override_floor("0.16.47"), (0, 16, 47))
        self.assertIsNone(override_floor({"nested": "x"}))

    def test_package_json_declares_patched_katex_override(self):
        data = json.loads(FRONTEND_PACKAGE_JSON.read_text(encoding="utf-8"))
        overrides = data.get("overrides") or {}
        floor = override_floor(overrides.get("katex"))
        self.assertIsNotNone(
            floor,
            "frontend/package.json must declare overrides.katex (#1943)",
        )
        self.assertGreaterEqual(
            floor,
            MIN_KATEX,
            f"overrides.katex must force >=0.18.2 (#1943); got {overrides.get('katex')!r}",
        )

    def test_lockfile_resolves_patched_katex(self):
        lock = json.loads(FRONTEND_LOCKFILE.read_text(encoding="utf-8"))
        versions = lockfile_versions(lock, "katex")
        self.assertTrue(
            versions, "frontend/package-lock.json must contain a katex entry (#1943)"
        )
        for version in versions:
            self.assertGreaterEqual(
                parse_semver(version),
                MIN_KATEX,
                f"katex@{version} is inside the advisory range >=0.11.0 <0.18.2 (#1943)",
            )

    @unittest.skipUnless(MARKDOWNLINT_BIN.exists(), "frontend/node_modules not installed")
    def test_markdownlint_cli2_runs_on_math_markdown(self):
        with tempfile.TemporaryDirectory() as tmp:
            md = Path(tmp) / "math.md"
            md.write_text(
                "# Math\n\nInline $a^2 + b^2 = c^2$ and a block:\n\n$$\nE = mc^2\n$$\n",
                encoding="utf-8",
            )
            result = subprocess.run(
                [str(MARKDOWNLINT_BIN), "--no-globs", str(md)],
                cwd=tmp,
                capture_output=True,
                text=True,
                timeout=120,
            )
        output = result.stdout + result.stderr
        self.assertEqual(result.returncode, 0, output)
        self.assertNotIn("Cannot find module", output)


if __name__ == "__main__":
    unittest.main()
