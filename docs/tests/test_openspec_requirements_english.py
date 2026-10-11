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
            "protocolo-auxiliar",
            "pagos-parciales-cuotas",
            "carpetas-de-tramite",
            "descuentos-recargos-presupuesto",
            "persona-validacion-duplicados",
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
        auxiliary = (SPECS / "protocolo-auxiliar" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("Protocolo Auxiliar", auxiliary)
        self.assertIn("Protocolo Principal", auxiliary)
        self.assertIn("`isAuxiliary`", auxiliary)
        self.assertIn("GET /api/v1/auxiliary-protocol/available-folios", auxiliary)
        self.assertIn("`idFolio`", auxiliary)
        self.assertIn("CU81", auxiliary)
        payments = (SPECS / "pagos-parciales-cuotas" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`NoPayments`", payments)
        self.assertIn("`PARTIAL`", payments)
        self.assertIn("`PAID`", payments)
        self.assertIn("GET /api/v1/payments/budget/{id}/status", payments)
        self.assertIn("`amount`", payments)
        self.assertIn("CU15", payments)
        self.assertIn("CU47", payments)
        folders = (SPECS / "carpetas-de-tramite" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"Activa"', folders)
        self.assertIn('"Espera"', folders)
        self.assertIn('"Archivada"', folders)
        self.assertIn("`confirmado`", folders)
        self.assertIn("CU85", folders)
        self.assertIn("CU16", folders)
        adjustments = (SPECS / "descuentos-recargos-presupuesto" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"NORMAL"', adjustments)
        self.assertIn('"DESCUENTO"', adjustments)
        self.assertIn('"RECARGO"', adjustments)
        self.assertIn("`item_type`", adjustments)
        self.assertIn("`reason`", adjustments)
        self.assertIn("CU45", adjustments)
        self.assertIn("CU71", adjustments)
        duplicates = (SPECS / "persona-validacion-duplicados" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`identificationNumber`", duplicates)
        self.assertIn("fk_id_identification_type", duplicates)
        self.assertIn("existingPersonId", duplicates)
        self.assertIn("CU17", duplicates)
        self.assertIn("#799", duplicates)
        self.assertIn("#835", duplicates)
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
        later = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "protocolo-auxiliar",
                "pagos-parciales-cuotas",
                "carpetas-de-tramite",
                "descuentos-recargos-presupuesto",
                "persona-validacion-duplicados",
            )
        )
        for retired in (
            "El sistema SHALL",
            "numeroIdentificacion",
            "fk_id_tipo_identificacion",
        ):
            self.assertNotIn(retired, later, retired)


if __name__ == "__main__":
    unittest.main()
