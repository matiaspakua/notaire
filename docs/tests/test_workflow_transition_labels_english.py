#!/usr/bin/env python3
"""The data dictionary names the live seeded workflow-transition descriptions."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Description of the action that advances the flow. Stored values "
    "`Se asignan trámites y comienza la gestión`, "
    "`Toda la documentación requerida fue presentada`, "
    "`Se redacta la escritura`, `Las partes firman la escritura`, "
    "`La gestión se archiva sin escritura`, "
    "`Testimony generated after deed signing`, "
    "`Testimony submitted for inscription`, and "
    "`Testimony withdrawn after inscription cycle` stay."
)


class WorkflowTransitionLabelEnglishTest(unittest.TestCase):
    def test_transition_description_names_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Description of the action that advances the flow |",
            dictionary,
        )
        self.assertNotIn(
            "Text,,,x,Description of the action that advances the flow\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
