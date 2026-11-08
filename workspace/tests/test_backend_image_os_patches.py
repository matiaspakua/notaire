#!/usr/bin/env python3
"""
Guards issue #1971 (CU76): the backend runtime images upgrade their Alpine
packages at build time, so OS fixes published after the pinned base image
(for example brotli-libs CVE-2026-111497, CRITICAL) do not fail the
`Build Docker Image` Trivy gate in ci.yml.

Run with: python3 workspace/tests/test_backend_image_os_patches.py
"""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DOCKERFILES = (
    REPO_ROOT / "backend-api" / "Dockerfile",
    REPO_ROOT / "backend-api" / "Dockerfile.slim",
)
FROM = re.compile(r"^FROM\s+\S+", re.MULTILINE)
APK_UPGRADE = re.compile(r"^RUN\s+apk\s+upgrade\s+--no-cache\b", re.MULTILINE)


def runtime_stage(text: str) -> str:
    starts = [m.start() for m in FROM.finditer(text)]
    return text[starts[-1]:]


class BackendImageOsPatchesTest(unittest.TestCase):
    def test_runtime_stage_upgrades_alpine_packages(self) -> None:
        for dockerfile in DOCKERFILES:
            with self.subTest(dockerfile=dockerfile.name):
                stage = runtime_stage(dockerfile.read_text(encoding="utf-8"))
                self.assertRegex(stage, APK_UPGRADE)

    def test_upgrade_runs_before_switching_to_non_root(self) -> None:
        for dockerfile in DOCKERFILES:
            with self.subTest(dockerfile=dockerfile.name):
                stage = runtime_stage(dockerfile.read_text(encoding="utf-8"))
                upgrade = APK_UPGRADE.search(stage)
                self.assertIsNotNone(upgrade)
                user = re.search(r"^USER\s+", stage, re.MULTILINE)
                if user:
                    self.assertLess(upgrade.start(), user.start())


if __name__ == "__main__":
    unittest.main()
