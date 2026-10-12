#!/usr/bin/env python3
"""The data dictionary names the live registration-draft statuses."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Draft status in the registration flow. Stored labels "
    "`Generada`, `Presentado para inscripción`, `Observado`, and `Inscripto` stay."
)


class RegistrationDraftStatusLabelsEnglishTest(unittest.TestCase):
    def test_registration_draft_status_names_the_live_labels(self):
        for path in (DICTIONARY, ENTITIES):
            text = path.read_text(encoding="utf-8")
            self.assertIn(LIVE, text, path.name)
            self.assertNotIn(
                "Draft status in the registration flow. Stored labels stay.",
                text,
                path.name,
            )
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        self.assertIn("`Inscripta`", dictionary)
        self.assertIn("Presentado para inscripción", dictionary)


if __name__ == "__main__":
    unittest.main()
