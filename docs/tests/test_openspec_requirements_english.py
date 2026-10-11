#!/usr/bin/env python3
"""Living OpenSpec requirement prose is English. Spec ids and stored labels stay."""

import unittest
from pathlib import Path

SPECS = (
    Path(__file__).resolve().parents[1]
    / "openspec"
    / "specs"
)


class OpenSpecRequirementsEnglishTest(unittest.TestCase):
    def test_spec_ids_stay(self):
        for spec_id in (
            "escritura-firma",
            "presupuesto-plantilla",
            "presupuesto-catalogo-items",
            "pago-recibo",
            "persona-credencial-escribano",
        ):
            path = SPECS / spec_id / "spec.md"
            text = path.read_text(encoding="utf-8")
            self.assertTrue(text.startswith(f"# {spec_id} Specification"), spec_id)

    def test_requirement_prose_is_english_and_keeps_stored_labels(self):
        firma = (SPECS / "escritura-firma" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("### Requirement: Sign a deed only when it is Sin Firmar and has a folio", firma)
        self.assertIn('"Sin Firmar"', firma)
        self.assertIn('"Firmada"', firma)
        self.assertIn("Escribano", firma)
        self.assertIn("CU06", firma)
        plantilla = (SPECS / "presupuesto-plantilla" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", plantilla)
        self.assertIn("CU39", plantilla)
        catalog = (SPECS / "presupuesto-catalogo-items" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("CU71", catalog)
        receipt = (SPECS / "pago-recibo" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("{idPayment}", receipt)
        self.assertIn("recibo_pago_", receipt)
        self.assertIn("`amount`", receipt)
        credential = (SPECS / "persona-credencial-escribano" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`notaryRegistrationNumber`", credential)
        self.assertIn("CU48", credential)
        self.assertIn("CU51", credential)
        combined = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "escritura-firma",
                "presupuesto-plantilla",
                "presupuesto-catalogo-items",
                "pago-recibo",
                "persona-credencial-escribano",
            )
        )
        for retired in (
            "El sistema SHALL",
            "tipo de trámite",
            "`Presupuesto`",
            "idPago",
            "registroEscribano",
        ):
            self.assertNotIn(retired, combined, retired)


if __name__ == "__main__":
    unittest.main()
