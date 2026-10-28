"""The kit is independent of any adopter: no adopter path in its code or templates (#1958)."""
from __future__ import annotations

import re
import unittest

from helpers import KIT

ADOPTER_MARKERS = re.compile("|".join(["notai" + "re", "backend" + "-api", "workspace" + "/sdlc"]), re.I)
SCANNED = ("aisdlc", "bin", "templates")


class KitIndependenceTest(unittest.TestCase):
    def test_no_adopter_path_in_code_or_templates(self):
        hits = []
        for folder in SCANNED:
            for path in sorted((KIT / folder).rglob("*")):
                if not path.is_file() or "__pycache__" in path.parts:
                    continue
                for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                    if ADOPTER_MARKERS.search(line):
                        hits.append(f"{path.relative_to(KIT)}:{number}: {line.strip()}")
        self.assertEqual([], hits)

    def test_kit_uses_only_the_standard_library(self):
        allowed_local = {"aisdlc"}
        imports = re.compile(r"^\s*(?:from|import)\s+([a-zA-Z_][\w]*)", re.M)
        import sys

        stdlib = set(sys.stdlib_module_names) | {"__future__"}
        for path in sorted((KIT / "aisdlc").rglob("*.py")):
            for module in imports.findall(path.read_text(encoding="utf-8")):
                self.assertIn(module, stdlib | allowed_local, f"{path.name} imports {module}")


if __name__ == "__main__":
    unittest.main()
