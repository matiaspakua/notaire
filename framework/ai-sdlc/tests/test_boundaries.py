"""The kit core and its dashboard name no adopter; RepoLens is a separate system beside the kit (ADR-006)."""
from __future__ import annotations

import sys
import unittest

from helpers import KIT

sys.path.insert(0, str(KIT))
from aisdlc.boundaries import scan_dashboard, scan_factory  # noqa: E402


class BoundaryScanTest(unittest.TestCase):
    def test_factory_core_has_no_adopter_marker_or_repolens_import(self):
        self.assertEqual([], scan_factory(KIT))

    def test_the_dashboard_names_no_adopter(self):
        self.assertEqual([], scan_dashboard(KIT))

    def test_repolens_is_not_inside_the_kit(self):
        self.assertFalse((KIT / "repolens" / "pyproject.toml").exists())


if __name__ == "__main__":
    unittest.main()
