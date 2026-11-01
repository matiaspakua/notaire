"""Workflow filename must be cleanup-actions.yml (#1981)."""

from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
WORKFLOWS = ROOT / ".github" / "workflows"
# Split so this test file is not itself an offender when scanning.
# Match the typo *filename* only — "cleanup-actions" contains the letters
# "leanup-actions" as a substring, so a bare token search false-positives.
TYPO_FILE = "lean" + "up-actions.yml"


class CleanupActionsWorkflowNameTest(unittest.TestCase):
    def test_cleanup_actions_yml_exists_and_typo_is_gone(self) -> None:
        self.assertTrue(
            (WORKFLOWS / "cleanup-actions.yml").is_file(),
            "expected .github/workflows/cleanup-actions.yml (#1981)",
        )
        self.assertFalse(
            (WORKFLOWS / TYPO_FILE).exists(),
            f"typo {TYPO_FILE} must be removed (#1981)",
        )

    def test_repo_references_use_cleanup_name(self) -> None:
        offenders: list[str] = []
        skip_dir_names = {".git", "node_modules", "target", ".next", "__pycache__"}
        for path in ROOT.rglob("*"):
            if not path.is_file():
                continue
            if any(part in skip_dir_names for part in path.parts):
                continue
            if path.resolve() == Path(__file__).resolve():
                continue
            # Active OpenSpec drafts may name the typo while describing the fix;
            # permanent tree must not.
            if "docs/openspec/changes/" in str(path).replace("\\", "/") and "/archive/" not in str(
                path
            ).replace("\\", "/"):
                continue
            try:
                text = path.read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError):
                continue
            # "cleanup-actions.yml" contains the typo string as a suffix of the
            # correct name; strip correct mentions before looking for leftovers.
            leftover = text.replace("cleanup-actions.yml", "")
            if TYPO_FILE in leftover:
                offenders.append(str(path.relative_to(ROOT)))
        self.assertEqual(
            offenders,
            [],
            f"references to {TYPO_FILE} remain: " + ", ".join(offenders),
        )


if __name__ == "__main__":
    unittest.main()
