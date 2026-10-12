#!/usr/bin/env python3
"""The data dictionary names the seeded sample foreign keys."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
IDENTIFICATION = "Primary identification type. The seeded sample stays `DNI`."
PERSON = "Linked person. The seeded account stays linked to the person `Garcia`."
MANAGEMENT = (
    "Current operational status. Seeded samples stay `Documentacion Completa` "
    "for management `1001` and `En Tramite` for management `1002`."
)
HISTORY = (
    "Status reached. Seeded samples stay `Iniciada`, `En Tramite`, and "
    "`Documentacion Completa`."
)
PROCEDURE = (
    "Procedure type. Seeded samples stay `Compraventa` for procedure `1` and "
    "`Donacion` for procedure `2`."
)
PROCEDURE_ROW = (
    "| `fk_id_procedure_type` | INTEGER | No | Yes | No | NULL | "
    "`procedure_types(id)` | Procedure type |"
)
LIVE = (IDENTIFICATION, PERSON, MANAGEMENT, HISTORY, PROCEDURE)


class SampleLinkLabelsEnglishTest(unittest.TestCase):
    def test_sample_links_name_the_seeded_targets(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in LIVE:
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| Primary identification type |", dictionary)
        self.assertNotIn("| Linked person |", dictionary)
        self.assertNotIn("| Current operational status |", dictionary)
        self.assertNotIn("| Status reached |", dictionary)
        self.assertNotIn(PROCEDURE_ROW, dictionary)
        self.assertIn("| Procedure type |", dictionary)
        self.assertNotIn(
            "Integer,,x,x,Primary identification type\n",
            entities,
        )
        self.assertNotIn("Integer,,x,x,Linked person\n", entities)
        self.assertNotIn("Integer,,x,x,Current operational status\n", entities)
        self.assertNotIn("Integer,,x,x,Status reached\n", entities)
        self.assertNotIn(
            "procedures,Strong,fk_id_procedure_type,FK,yes,Integer,,x,x,Procedure type\n",
            entities,
        )
        self.assertIn(
            "budget_templates,Associative,fk_id_procedure_type,PK,no,Integer,x,x,,Procedure type\n",
            entities,
        )
        self.assertIn(
            "procedure_templates,Associative,fk_id_procedure_type,PK,no,Integer,x,x,,Procedure type\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
