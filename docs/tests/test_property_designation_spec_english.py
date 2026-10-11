#!/usr/bin/env python3
"""The property-designation delta names the dialog and field in English."""

import unittest
from pathlib import Path

SPEC = (
    Path(__file__).resolve().parents[1]
    / "openspec"
    / "changes"
    / "fix-655-property-cadastral-designation-required"
    / "specs"
    / "properties"
    / "spec.md"
)


class PropertyDesignationSpecEnglishTest(unittest.TestCase):
    def test_properties_delta_names_the_dialog_in_english(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn("# properties — delta", text)
        self.assertIn("POST /api/v1/properties", text)
        self.assertIn("PUT /api/v1/properties/{id}", text)
        self.assertIn("`cadastralDesignation`", text)
        self.assertIn("The Properties dialog", text)
        self.assertIn("cadastral designation is blank", text)
        self.assertIn("leaves the designation blank", text)
        for retired in ("inmuebles", "Inmuebles", "nomenclatura"):
            self.assertNotIn(retired, text, retired)


if __name__ == "__main__":
    unittest.main()
