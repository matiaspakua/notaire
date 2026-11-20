"""Factory core and RepoLens core stay free of adopter names and of each other."""
from __future__ import annotations

import sys
import unittest

from helpers import KIT

sys.path.insert(0, str(KIT))
from aisdlc.boundaries import scan_factory, scan_repolens  # noqa: E402


class BoundaryScanTest(unittest.TestCase):
    def test_factory_core_has_no_adopter_marker_or_repolens_import(self):
        self.assertEqual([], scan_factory(KIT))

    def test_repolens_package_and_runners_have_no_adopter_marker_or_factory_import(self):
        self.assertEqual([], scan_repolens(KIT / "repolens"))


if __name__ == "__main__":
    unittest.main()
