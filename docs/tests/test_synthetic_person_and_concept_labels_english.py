#!/usr/bin/env python3
"""Synthetic person and concept labels in tests are English. Visible UI stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class SyntheticPersonAndConceptLabelsEnglishTest(unittest.TestCase):
    def test_stubbed_person_names_are_english(self):
        referenced = read("testing/e2e/tests/TS-0105-delete-in-use-reason.spec.ts")
        self.assertIn('firstName: "Referenced"', referenced)
        self.assertIn("No se puede eliminar", referenced)
        self.assertIn("Error al eliminar la persona", referenced)
        self.assertNotIn("Referenciada", referenced)

        mobile = read("testing/e2e/tests/TS-0109-mobile-lists.spec.ts")
        self.assertIn('firstName: "Mobile"', mobile)
        self.assertIn("mobile.ts0109@notaire.test", mobile)
        self.assertIn('"INICIADA"', mobile)
        self.assertIn('"EFECTIVO"', mobile)
        self.assertNotIn("Movil", mobile)
        self.assertNotIn("movil.ts0109", mobile)

        people = read("frontend/src/tests/unit/people-identification-type.test.tsx")
        self.assertIn('firstName: "Company"', people)
        self.assertIn('"DNI"', people)
        self.assertIn('"CUIT"', people)
        self.assertIn('"Pasaporte"', people)
        self.assertNotIn("Empresa", people)

    def test_concept_and_missing_search_labels_are_english(self):
        concepts = read("testing/e2e/tests/TS-0020-reports-admin-workflow.spec.ts")
        self.assertIn('"New Concept Test"', concepts)
        self.assertIn('"nombre"', concepts)
        self.assertIn("Nuevo concepto", concepts)
        self.assertNotIn("Nuevo Concept Test", concepts)

        search = read("testing/e2e/tests/TS-0015-people-clients-workflow.spec.ts")
        self.assertIn("'ZZZZDoesNotExist999XY'", search)
        self.assertIn("'no hay'", search)
        self.assertNotIn("NoExiste", search)


if __name__ == "__main__":
    unittest.main()
