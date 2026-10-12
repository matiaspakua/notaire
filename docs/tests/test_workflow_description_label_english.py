#!/usr/bin/env python3
"""The data dictionary names the seeded workflow description."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Functional description of the process. The seeded value "
    "`Ciclo de vida estándar de una gestión de escritura` stays."
)


class WorkflowDescriptionLabelEnglishTest(unittest.TestCase):
    def test_workflow_description_names_the_seeded_label(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Functional description of the process |", dictionary)
        self.assertNotIn(
            "Text,,,x,Functional description of the process\n",
            entities,
        )
        self.assertIn("`Workflow de Gestión Estándar`", dictionary)


if __name__ == "__main__":
    unittest.main()
