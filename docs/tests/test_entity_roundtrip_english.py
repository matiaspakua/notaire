#!/usr/bin/env python3
"""Entity status round-trips and nested test names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "backend-api/src/test/java/com/licensis/notaire/unit"


def read(name: str) -> str:
    return (JAVA / name).read_text(encoding="utf-8")


class EntityRoundtripEnglishTest(unittest.TestCase):
    def test_status_roundtrips_are_english(self):
        budget = read("BudgetEntityTest.java")
        self.assertIn('"pending"', budget)
        self.assertIn('"approved"', budget)
        self.assertNotIn('"pendiente"', budget)
        self.assertNotIn('"aprobado"', budget)
        self.assertIn("Compraventa", budget)
        self.assertIn("Prepare budget", budget)
        self.assertIn("class PrepareBudgetTests", budget)
        self.assertNotIn("Preparar", budget)

        deed = read("DeedEntityTest.java")
        self.assertIn('"signed"', deed)
        self.assertIn('"unsigned"', deed)
        self.assertIn('"voided"', deed)
        self.assertIn('"not_passed"', deed)
        self.assertNotIn('"firmada"', deed)
        self.assertNotIn('"no_firmada"', deed)
        self.assertNotIn('"anulada"', deed)
        self.assertNotIn('"no_paso"', deed)
        self.assertIn('"FIRMADA"', deed)
        self.assertIn("setProcedures(null)", deed)
        self.assertNotIn("setTramites", deed)
        self.assertNotIn("getTramites", deed)
        self.assertIn("Prepare deed", deed)
        self.assertIn("class PrepareDeedTests", deed)
        self.assertIn("unsigned status", deed)
        self.assertNotIn("Preparar", deed)
        self.assertNotIn("SIN_FIRMAR", deed)

    def test_nested_attribute_classes_are_english(self):
        names = (
            "BudgetEntityTest.java",
            "DeedEntityTest.java",
            "ProcedureEntityTest.java",
            "PaymentEntityTest.java",
            "DeedManagementEntityTest.java",
            "EntitiesBasicTest.java",
        )
        for name in names:
            source = read(name)
            self.assertNotIn("SetAtributos", source, name)
            self.assertNotIn("setAtributos", source, name)
        self.assertIn("class SetAttributesTests", read("DeedEntityTest.java"))
        self.assertIn("class SetAttributesTests", read("ProcedureEntityTest.java"))
        self.assertIn("class SetAttributesTests", read("PaymentEntityTest.java"))
        self.assertIn("class SetAttributesTests", read("DeedManagementEntityTest.java"))
        self.assertIn("void getDtoAndSetAttributes(", read("EntitiesBasicTest.java"))


if __name__ == "__main__":
    unittest.main()
