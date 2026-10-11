#!/usr/bin/env python3
"""English comments quote Spanish field labels instead of using them as prose."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
EDIT = ROOT / "testing/e2e/tests/TS-0126-management-full-edit.spec.ts"
CONCEPTS = ROOT / "testing/e2e/tests/TS-0020-reports-admin-workflow.spec.ts"


class FieldLabelCommentsEnglishTest(unittest.TestCase):
    def test_management_edit_comment_names_the_number_field(self):
        text = EDIT.read_text(encoding="utf-8")
        self.assertIn('only the number ("Número")', text)
        self.assertIn('"Editar"', text)
        self.assertIn("CU53 - Modificar gestión", text)
        self.assertNotIn("only Número", text)

    def test_concept_form_comment_quotes_the_visible_labels(self):
        text = CONCEPTS.read_text(encoding="utf-8")
        self.assertIn(
            'the form shows "Nombre", "Descripción", and "Valor base ($)" (no "porcentaje" field)',
            text,
        )
        self.assertNotIn("Name, Descripción", text)
        self.assertIn("nuevo concepto", text)
        self.assertIn("Nuevo concepto", text)


if __name__ == "__main__":
    unittest.main()
