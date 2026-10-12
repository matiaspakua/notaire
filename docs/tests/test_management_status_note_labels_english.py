#!/usr/bin/env python3
"""The data dictionary names the seeded management-status notes."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Functional definition and conditions. Stored notes `Inicio Gestion`, "
    "`Post-signing: testimony generated from signed deed`, "
    "`Post-signing: testimony submitted for inscription`, and "
    "`Post-signing: testimony withdrawn after inscription cycle` stay."
)


class ManagementStatusNoteLabelsEnglishTest(unittest.TestCase):
    def test_management_status_notes_name_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Functional definition and conditions |", dictionary)
        self.assertNotIn(
            "Text,,,x,Functional definition and conditions\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
