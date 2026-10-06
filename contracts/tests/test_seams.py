#!/usr/bin/env python3
"""
Guards issue #1292 (CU76, ADR-026): every seam between modules is listed in contracts/seams.yaml
and each contract value still appears in the files that rely on it, so a rename on one side fails
here instead of in production.

Run with: python3 contracts/tests/test_seams.py
"""
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
SEAMS = REPO_ROOT / "contracts" / "seams.yaml"
MODULES = REPO_ROOT / "workspace" / "modules.yaml"
REQUIRED_KEYS = ("provider", "consumers", "values", "evidence")


def load(path):
    return yaml.safe_load(path.read_text(encoding="utf-8"))


class SeamsTest(unittest.TestCase):
    def test_seams_file_exists(self):
        self.assertTrue(SEAMS.is_file(), "contracts/seams.yaml lists every seam between modules")

    def test_every_seam_declares_the_required_keys(self):
        for name, seam in load(SEAMS)["seams"].items():
            for key in REQUIRED_KEYS:
                self.assertTrue(seam.get(key), f"{name} must declare a non-empty '{key}'")

    def test_provider_and_consumers_are_manifest_modules(self):
        modules = load(MODULES)["modules"]
        for name, seam in load(SEAMS)["seams"].items():
            for module in [seam["provider"], *seam["consumers"]]:
                self.assertIn(module, modules, f"{name} names unknown module {module}")

    def test_provider_is_not_its_own_consumer(self):
        for name, seam in load(SEAMS)["seams"].items():
            self.assertNotIn(seam["provider"], seam["consumers"], f"{name}: provider consumes itself")

    def test_every_contract_value_appears_in_every_evidence_file(self):
        for name, seam in load(SEAMS)["seams"].items():
            for path in seam["evidence"]:
                evidence = REPO_ROOT / path
                self.assertTrue(evidence.is_file(), f"{name}: evidence file {path} is missing")
                text = evidence.read_text(encoding="utf-8")
                for value in seam["values"]:
                    self.assertIn(value, text, f"{name}: '{value}' no longer appears in {path}")


if __name__ == "__main__":
    unittest.main()
