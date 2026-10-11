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
            "copia-validacion-testimonio-inscripto",
            "documento-presentado-herencia-vencimiento",
            "costos-documentos-presupuesto",
            "numeracion-escrituras",
            "folio-vinculacion-escritura",
            "testimonio-generacion-verificacion",
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
        copy = (SPECS / "copia-validacion-testimonio-inscripto" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`registered = true`", copy)
        self.assertIn("CU87", copy)
        submitted = (SPECS / "documento-presentado-herencia-vencimiento" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`expires`", submitted)
        self.assertIn("`dueDays`", submitted)
        self.assertIn("`deliveredBy`", submitted)
        self.assertIn("`dateEntry`", submitted)
        self.assertIn("`dateDue`", submitted)
        self.assertIn("CU42", submitted)
        costs = (SPECS / "costos-documentos-presupuesto" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`amountToPay`", costs)
        numbering = (SPECS / "numeracion-escrituras" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("Protocolo Auxiliar", numbering)
        self.assertIn("Protocolo Principal", numbering)
        self.assertIn("no pasó", numbering)
        self.assertIn("CU86", numbering)
        folio = (SPECS / "folio-vinculacion-escritura" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`deedId`", folio)
        self.assertIn('"Utilizado"', folio)
        self.assertIn("GET /api/v1/folios/{id}", folio)
        testimony = (SPECS / "testimonio-generacion-verificacion" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"Firmada"', testimony)
        self.assertIn("Escribano", testimony)
        self.assertIn("CU07", testimony)
        combined = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "escritura-firma",
                "presupuesto-plantilla",
                "presupuesto-catalogo-items",
                "pago-recibo",
                "persona-credencial-escribano",
                "copia-validacion-testimonio-inscripto",
                "documento-presentado-herencia-vencimiento",
                "costos-documentos-presupuesto",
                "numeracion-escrituras",
                "folio-vinculacion-escritura",
                "testimonio-generacion-verificacion",
            )
        )
        for retired in (
            "El sistema SHALL",
            "tipo de trámite",
            "`Presupuesto`",
            "idPago",
            "registroEscribano",
            "inscripta",
            "vence",
            "diasVencimiento",
            "quienEntrega",
            "fechaVencimiento",
            "fechaIngreso",
            "importeAPagar",
            "escrituraId",
        ):
            self.assertNotIn(retired, combined, retired)


if __name__ == "__main__":
    unittest.main()
