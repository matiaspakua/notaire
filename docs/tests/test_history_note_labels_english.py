#!/usr/bin/env python3
"""The data dictionary names the seeded history notes."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Reason or attestation for the status change. Stored notes "
    "`Apertura de la gestión`, `Trámite de compraventa en curso`, "
    "`Documentación completa recibida`, and `Trámite de donación en curso` stay."
)


class HistoryNoteLabelsEnglishTest(unittest.TestCase):
    def test_history_notes_name_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Reason or attestation for the status change |",
            dictionary,
        )
        self.assertNotIn(
            "Text,,,x,Reason or attestation for the status change\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
