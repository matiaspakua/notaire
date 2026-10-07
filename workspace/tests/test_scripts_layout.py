#!/usr/bin/env python3
"""
Guards issue #1307 (CU76, ADR-026): scripts/ is organized by module. Every moved script exists at
its new path, is gone from the old one, and no tracked file still points at the old path.

Run with: python3 workspace/tests/test_scripts_layout.py
"""
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]

MOVED = {
    "scripts/start.sh": "workspace/stack/start.sh",
    "scripts/stop.sh": "workspace/stack/stop.sh",
    "scripts/logs.sh": "workspace/stack/logs.sh",
    "scripts/start-all.sh": "workspace/stack/start-all.sh",
    "scripts/setup-pgadmin.sh": "workspace/stack/setup-pgadmin.sh",
}
HISTORY = (
    "CHANGELOG.md", "deprecated/", "docs/000-archive/", "docs/openspec/changes/archive/",
    "workspace/tests/test_scripts_layout.py",
)


def tracked_text_files():
    out = subprocess.run(["git", "ls-files", "-z"], cwd=REPO_ROOT, capture_output=True, check=True).stdout
    for name in out.decode().split("\0"):
        path = REPO_ROOT / name
        if name and not name.startswith(HISTORY) and path.is_file():
            try:
                yield name, path.read_text(encoding="utf-8")
            except UnicodeDecodeError:
                continue


class ScriptsLayoutTest(unittest.TestCase):
    def test_moved_scripts_exist_at_their_new_path(self):
        missing = [new for new in MOVED.values() if not (REPO_ROOT / new).is_file()]
        self.assertEqual([], missing)

    def test_moved_scripts_are_gone_from_the_old_path(self):
        left = [old for old in MOVED if (REPO_ROOT / old).exists()]
        self.assertEqual([], left)

    def test_no_tracked_file_references_an_old_path(self):
        stale = [f"{name}: {old}" for name, text in tracked_text_files() for old in MOVED if old in text]
        self.assertEqual([], stale)


if __name__ == "__main__":
    unittest.main()
