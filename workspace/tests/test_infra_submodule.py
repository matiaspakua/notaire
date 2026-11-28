"""Guards #2512: infra/ is a git submodule of matiaspakua/infrastructure."""

from __future__ import annotations

import configparser
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GITMODULES = ROOT / ".gitmodules"
INFRA = ROOT / "infra"


class InfraSubmoduleTest(unittest.TestCase):
    def test_gitmodules_points_at_infrastructure_product(self) -> None:
        self.assertTrue(GITMODULES.is_file(), ".gitmodules must exist")
        parser = configparser.ConfigParser()
        parser.read(GITMODULES)
        paths = {parser[s].get("path"): parser[s].get("url") for s in parser.sections()}
        self.assertIn("infra", paths)
        self.assertIn("matiaspakua/infrastructure", paths["infra"])

    def test_infra_is_gitlink_not_vendored_tree(self) -> None:
        # gitlink mode 160000 in the index
        import subprocess

        out = subprocess.check_output(
            ["git", "ls-files", "-s", "infra"],
            cwd=ROOT,
            text=True,
        ).strip()
        self.assertTrue(out, "infra must be recorded in the git index")
        mode = out.split()[0]
        self.assertEqual("160000", mode, f"infra must be a submodule gitlink, got: {out}")

    def test_module_verify_path_exists(self) -> None:
        self.assertTrue((INFRA / "verify.sh").is_file())
        self.assertTrue((INFRA / "docs" / "SYSTEM-CONTRACT.md").is_file())


if __name__ == "__main__":
    unittest.main()
