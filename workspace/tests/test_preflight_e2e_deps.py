#!/usr/bin/env python3
"""A fresh worktree's gate installs the e2e suite before Playwright; the types check cleans up (#2442)."""
from __future__ import annotations

import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PREFLIGHT = ROOT / "workspace" / "sdlc" / "preflight.sh"
FRONTEND_PACKAGE = ROOT / "frontend" / "package.json"


class PreflightE2eDepsTest(unittest.TestCase):
    def test_e2e_dependencies_are_installed_by_one_helper(self) -> None:
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("ensure_e2e_deps()", text)
        helper = text[text.index("ensure_e2e_deps()"):]
        helper = helper[: helper.index("\n}\n")]
        self.assertIn("testing/e2e/node_modules", helper)
        self.assertIn("npm ci", helper)

    def test_server_backed_playwright_installs_the_suite_first(self) -> None:
        text = PREFLIGHT.read_text(encoding="utf-8")
        section = text[text.index('touches stack && [ "$STACK_READY" = "1" ]'):text.index('run "playwright e2e"')]
        self.assertIn("ensure_e2e_deps", section)

    def test_e2e_lint_uses_the_same_helper(self) -> None:
        text = PREFLIGHT.read_text(encoding="utf-8")
        section = text[:text.index('run "e2e typecheck"')]
        self.assertIn("ensure_e2e_deps\n", section[section.rindex("if ! touches testing"):])


class OpenApiTypesCheckTest(unittest.TestCase):
    def run_check(self, generated: str) -> tuple[int, Path]:
        script = json.loads(FRONTEND_PACKAGE.read_text(encoding="utf-8"))["scripts"]["openapi:types:check"]
        tmp = Path(tempfile.mkdtemp())
        self.addCleanup(lambda: subprocess.run(["rm", "-rf", str(tmp)], check=False))
        (tmp / "frontend" / "src" / "types").mkdir(parents=True)
        (tmp / "frontend" / "src" / "types" / "api.generated.ts").write_text("committed\n", encoding="utf-8")
        bin_dir = tmp / "bin"
        bin_dir.mkdir()
        stub = bin_dir / "openapi-typescript"
        stub.write_text(f'#!/bin/sh\nwhile [ "$1" != "-o" ]; do shift; done\nprintf "{generated}\\n" > "$2"\n',
                        encoding="utf-8")
        stub.chmod(0o755)
        env = {**os.environ, "PATH": f"{bin_dir}{os.pathsep}{os.environ['PATH']}"}
        result = subprocess.run(["sh", "-c", script], cwd=tmp / "frontend", env=env, capture_output=True, text=True)
        return result.returncode, tmp / "frontend" / "src" / "types" / "api.generated.check.ts"

    def test_stale_types_fail_and_leave_no_file(self) -> None:
        code, leftover = self.run_check("changed")
        self.assertNotEqual(0, code)
        self.assertFalse(leftover.exists(), "a failed check must not leave the temporary file behind")

    def test_current_types_pass_and_leave_no_file(self) -> None:
        code, leftover = self.run_check("committed")
        self.assertEqual(0, code)
        self.assertFalse(leftover.exists())


if __name__ == "__main__":
    unittest.main()
