#!/usr/bin/env python3
"""The data dictionary names the seeded document-type fees and validity."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
RETURNED = (
    "`true` when the original must be returned to the client. Seeded flags stay: "
    "`true` for `Libre Deuda Municipal`, `Informe de Dominio`, and "
    "`Certificado Catastral`, and `false` for `Fotocopia DNI`."
)
EXPIRES = (
    "`true` when the certificate expires. Seeded flags stay: `true` for "
    "`Libre Deuda Municipal`, `Informe de Dominio`, and `Certificado Catastral`, "
    "and `false` for `Fotocopia DNI`."
)
DUE_DAYS = (
    "Legal validity in days. Seeded day counts stay: `30` for "
    "`Libre Deuda Municipal`, `60` for `Informe de Dominio`, `90` for "
    "`Certificado Catastral`, and NULL for `Fotocopia DNI`."
)
FEE = (
    "Standard fee. Seeded fees stay: `0` for `Libre Deuda Municipal` and "
    "`Fotocopia DNI`, `150` for `Informe de Dominio`, and `100` for "
    "`Certificado Catastral`."
)


class DocumentTypeFeeLabelsEnglishTest(unittest.TestCase):
    def test_document_type_fees_name_the_seeded_values(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in (RETURNED, EXPIRES, DUE_DAYS, FEE):
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn(
            "| `true` when the original must be returned to the client |",
            dictionary,
        )
        self.assertNotIn("| `true` when the certificate expires |", dictionary)
        self.assertNotIn("| Legal validity in days |", dictionary)
        self.assertNotIn("| Standard fee |", dictionary)
        self.assertNotIn(
            "Boolean,,,,`true` when the original must be returned to the client\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,`true` when the certificate expires\n",
            entities,
        )
        self.assertNotIn("Integer,,,x,Legal validity in days\n", entities)
        self.assertNotIn("Real,,,x,Standard fee\n", entities)


if __name__ == "__main__":
    unittest.main()
