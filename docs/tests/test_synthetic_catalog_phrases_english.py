#!/usr/bin/env python3
"""Synthetic catalog phrases in tests are English. Stored labels and visible UI stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class SyntheticCatalogPhrasesEnglishTest(unittest.TestCase):
    def test_notes_and_headings_are_english(self):
        substitution = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/SubstitutionEntityTest.java"
        )
        self.assertIn("Sick leave", substitution)
        self.assertNotIn("Licencia", substitution)
        self.assertNotIn("enfermedad", substitution)

        columns = read("testing/e2e/tests/TS-0116-managements-columns.spec.ts")
        self.assertIn("Heading E2E", columns)
        self.assertIn('name: "Carátula"', columns)
        self.assertIn('name: "Inicio"', columns)
        self.assertIn('name: "Trámites"', columns)
        self.assertIn('name: "Tipo de Trámite"', columns)
        self.assertNotIn("Carátula E2E", columns)

        helpers = read("testing/e2e/tests/setup/api-helpers.ts")
        self.assertIn("Folio type E2E", helpers)
        self.assertIn("Activated from Playwright", helpers)
        self.assertIn("Archived from Playwright", helpers)
        self.assertIn("detalle:", helpers)
        self.assertIn('"Entidad Externa"', helpers)
        self.assertIn('"Cliente"', helpers)
        self.assertIn('"Soltero"', helpers)
        self.assertIn('"Masculino"', helpers)
        self.assertIn('"Efectivo"', helpers)
        self.assertIn('"Nuevo"', helpers)
        self.assertNotIn("Tipo Folio", helpers)
        self.assertNotIn("Activada", helpers)
        self.assertNotIn("Archivada", helpers)
        self.assertNotIn("desde Playwright", helpers)

    def test_document_and_procedure_phrases_are_english(self):
        movement = read("testing/e2e/tests/TS-0033-external-entity-documents-feature.spec.ts")
        self.assertIn("Withdrawn from the registry", movement)
        self.assertIn("detailDialog", movement)
        self.assertIn("Certificado de Dominio", movement)
        self.assertIn("movimiento registrado", movement)
        self.assertIn("no hay documentos", movement)
        self.assertNotIn("Retirado", movement)
        self.assertNotIn("detalleDialog", movement)

        procedure = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/CoreBusinessControllersIntegrationTest.java"
        )
        self.assertIn("Donation procedure", procedure)
        self.assertIn('"Donacion"', procedure)
        self.assertNotIn("de donacion", procedure)

        documents = read("testing/e2e/tests/TS-0134-document-type-expiration-config.spec.ts")
        self.assertIn("Power Expires", documents)
        self.assertIn("Identity card Expires", documents)
        self.assertIn("Contract Expires", documents)
        self.assertIn("Property Registry", documents)
        self.assertIn("without due days", documents)
        self.assertIn("existing type with returned", documents)
        self.assertIn("vence/diasVencimiento/quienEntrega", documents)
        self.assertIn('"Escribano"', documents)
        self.assertIn('"días"', documents)
        self.assertIn('"creado"', documents)
        self.assertNotIn("Vence", documents)
        self.assertNotIn("Cedula", documents)
        self.assertNotIn("Contrato", documents)
        self.assertNotIn("Registro de la Propiedad", documents)
        self.assertNotIn("Poder", documents)
        self.assertNotIn("without dias", documents)
        self.assertNotIn("existing tipo", documents)


if __name__ == "__main__":
    unittest.main()
