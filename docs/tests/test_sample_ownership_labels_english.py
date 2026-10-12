#!/usr/bin/env python3
"""The data dictionary names the seeded sample ownership and empty notes."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
NOTES = (
    "Functional definition and conditions. Stored notes `Inicio Gestion`, "
    "`Post-signing: testimony generated from signed deed`, "
    "`Post-signing: testimony submitted for inscription`, and "
    "`Post-signing: testimony withdrawn after inscription cycle` stay. "
    "Seeded notes stay empty for `En Tramite`, `Documentacion Completa`, "
    "`Archivada`, `Gestion Modificada`, `Gestion con Escritura Firmada`, "
    "`Gestion con Escritura Sin Firmar`, `Gestion con Escritura Anulada`, "
    "`Gestion con Escritura No Paso`, and `Gestion con Escritura Inscripta`."
)
NOTARY = "Notary in charge. The seeded managements stay with `Garcia`."
HISTORY = (
    "Deed management that changed status. Seeded history stays on managements "
    "`1001` and `1002`."
)
GROUP = (
    "Deed management that groups it (null for auxiliary work). Seeded "
    "procedures stay on managements `1001` and `1002`."
)
PARENT = (
    "Parent workflow (ON DELETE CASCADE). The seeded rows stay on "
    "`Workflow de Gestión Estándar`."
)
LIVE = (NOTES, NOTARY, HISTORY, GROUP, PARENT)


class SampleOwnershipLabelsEnglishTest(unittest.TestCase):
    def test_sample_ownership_names_the_seeded_links(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in LIVE:
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| Notary in charge |", dictionary)
        self.assertNotIn("| Deed management that changed status |", dictionary)
        self.assertNotIn(
            "| Deed management that groups it (null for auxiliary work) |",
            dictionary,
        )
        self.assertNotIn("| Parent workflow (ON DELETE CASCADE) |", dictionary)
        self.assertEqual(dictionary.count(PARENT), 2)
        self.assertEqual(entities.count(PARENT), 2)
        self.assertNotIn("Integer,,x,x,Notary in charge\n", entities)
        self.assertNotIn(
            "Integer,,x,x,Deed management that changed status\n",
            entities,
        )
        self.assertNotIn(
            "Integer,,x,x,Deed management that groups it (null for auxiliary work)\n",
            entities,
        )
        self.assertNotIn(
            "Integer,,x,,Parent workflow (ON DELETE CASCADE)\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
