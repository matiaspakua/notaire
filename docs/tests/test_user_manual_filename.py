#!/usr/bin/env python3
"""The historical user-manual path stays Manual de Usuario."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FILES = (
    ROOT / "docs/tools/fetch-user-manual.sh",
    ROOT / "docs/200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md",
    ROOT / "docs/openspec/specs/repo-hygiene/spec.md",
    ROOT / "workspace/tests/test_repo_hygiene.py",
)
PDF = "Manual de Usuario Notaire.doc.pdf"
FOLDER = "C_Manual de Usuario"
ASSET = "Manual-de-Usuario-Notaire.doc.pdf"


class UserManualFilenameTest(unittest.TestCase):
    def test_historical_manual_filename_stays_usuario(self):
        for path in FILES:
            text = path.read_text(encoding="utf-8")
            self.assertIn(PDF, text, path.name)
            self.assertNotIn("UserAccount", text, path.name)
        fetch = FILES[0].read_text(encoding="utf-8")
        adr = FILES[1].read_text(encoding="utf-8")
        self.assertIn(FOLDER, fetch)
        self.assertIn(ASSET, fetch)
        self.assertIn(FOLDER, adr)
        self.assertIn(ASSET, adr)
        ignored = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn("C_Manual\\ de\\ Usuario", ignored)
        self.assertNotIn("UserAccount", ignored)


if __name__ == "__main__":
    unittest.main()
