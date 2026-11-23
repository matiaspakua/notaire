#!/usr/bin/env python3
"""
Guards issue #2457: Notaire runs its AI SDLC only through the aisdlc kit. Other factory products the Owner
experiments with live elsewhere, so no tracked file names them. Released and migration history (CHANGELOG
and UPGRADE files), the archive and OpenSpec change records keep what was true when written.

Run with: python3 workspace/tests/test_no_external_factory.py
"""
import re
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]

# Built from parts so this guard does not match itself.
NAMES = re.compile("|".join(["agentic" + "[-_]os", "factory" + "[-_]control", "Factory" + " Control"]))
HISTORY = ("docs/000-archive/", "docs/openspec/changes/")
HISTORY_FILES = ("CHANGELOG.md", "UPGRADE.md")


def offending_lines():
    found = subprocess.run(
        ["git", "grep", "-I", "-n", "-E", NAMES.pattern],
        cwd=REPO_ROOT, capture_output=True, text=True, check=False,
    ).stdout.splitlines()
    return [line for line in found
            if not line.startswith(HISTORY) and not line.split(":", 1)[0].endswith(HISTORY_FILES)]


class NoExternalFactoryTest(unittest.TestCase):
    def test_no_tracked_file_names_an_external_factory_product(self):
        self.assertEqual([], offending_lines())


if __name__ == "__main__":
    unittest.main()
