"""Guards #2514: security/ is a git submodule of matiaspakua/notaire-security."""

from __future__ import annotations

import configparser
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GITMODULES = ROOT / ".gitmodules"
SECURITY = ROOT / "security"


class SecuritySubmoduleTest(unittest.TestCase):
    def test_gitmodules_points_at_notaire_security(self) -> None:
        self.assertTrue(GITMODULES.is_file(), ".gitmodules must exist")
        parser = configparser.ConfigParser()
        parser.read(GITMODULES)
        paths = {parser[s].get("path"): parser[s].get("url") for s in parser.sections()}
        self.assertIn("security", paths)
        self.assertIn("matiaspakua/notaire-security", paths["security"])

    def test_security_is_gitlink_not_vendored_tree(self) -> None:
        out = subprocess.check_output(
            ["git", "ls-files", "-s", "security"],
            cwd=ROOT,
            text=True,
        ).strip()
        self.assertTrue(out, "security must be recorded in the git index")
        mode = out.split()[0]
        self.assertEqual("160000", mode, f"security must be a submodule gitlink, got: {out}")

    def test_module_verify_path_exists(self) -> None:
        self.assertTrue((SECURITY / "verify.sh").is_file())
        self.assertTrue((SECURITY / "docs" / "SECURITY-FLEET-CONTRACT.md").is_file())
        self.assertTrue((SECURITY / "docs" / "CYBERSECURITY-PROGRAM.md").is_file())


if __name__ == "__main__":
    unittest.main()
