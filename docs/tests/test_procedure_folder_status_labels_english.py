#!/usr/bin/env python3
"""The data dictionary names the live procedure-folder statuses."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Folder status. Stored labels `Activa`, `Espera`, and `Archivada` stay."
)


class ProcedureFolderStatusLabelsEnglishTest(unittest.TestCase):
    def test_procedure_folder_status_names_the_live_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Folder status |", dictionary)
        self.assertNotIn("Text,,,,Folder status", entities)


if __name__ == "__main__":
    unittest.main()
