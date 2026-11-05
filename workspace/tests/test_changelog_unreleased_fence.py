"""CHANGELOG Unreleased fence helper is importable and executable (#1693)."""

from __future__ import annotations

import ast
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "workspace" / "sdlc" / "check-changelog-unreleased-fence.py"


class ChangelogUnreleasedFenceTest(unittest.TestCase):
    def test_script_exists_and_parses(self) -> None:
        self.assertTrue(SCRIPT.is_file())
        source = SCRIPT.read_text(encoding="utf-8")
        ast.parse(source)
        self.assertIn("CHANGELOG.md", source)
        self.assertIn("Unreleased", source)


if __name__ == "__main__":
    unittest.main()
