#!/usr/bin/env python3
"""The data dictionary names the seeded folio-type labels."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Name. Stored values `De documento`, `De actuacion`, `De certificacion`, "
    "`Protocolo Principal`, `Protocolo Auxiliar`, and `Testimonio` stay."
)
RETIRED = (
    "Name. Stored values `Protocolo Principal`, `Protocolo Auxiliar`, and `Testimonio` stay."
)


class FolioTypeSeedLabelsEnglishTest(unittest.TestCase):
    def test_folio_type_names_the_seeded_labels(self):
        for path in (DICTIONARY, ENTITIES):
            text = path.read_text(encoding="utf-8")
            self.assertIn(LIVE, text, path.name)
            self.assertNotIn(RETIRED, text, path.name)


if __name__ == "__main__":
    unittest.main()
