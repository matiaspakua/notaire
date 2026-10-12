#!/usr/bin/env python3
"""The data dictionary names the seeded document-type labels."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Name of the document or certificate. Stored values `Libre Deuda Municipal`, "
    "`Informe de Dominio`, `Certificado Catastral`, and `Fotocopia DNI` stay."
)


class DocumentTypeSeedLabelsEnglishTest(unittest.TestCase):
    def test_document_type_names_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Name of the document or certificate |", dictionary)
        self.assertNotIn("Text,,,,Name of the document or certificate\n", entities)


if __name__ == "__main__":
    unittest.main()
