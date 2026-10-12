#!/usr/bin/env python3
"""The data dictionary names the seeded workflow graph and sample files."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
ACTIVE = "`true` when the workflow is enabled. The seeded workflow stays `true`."
ASSIGNED = (
    "Workflow assigned to the procedure type (V8). The seeded workflow stays "
    "assigned to `Compraventa`, `Donacion`, and `Hipoteca`. `Sucesion` and "
    "`Poder` stay unassigned."
)
NODE = (
    "Management status this node represents. The live seeded graph stays: "
    "`Iniciada` as `INITIAL`; `En Tramite`, `Documentacion Completa`, "
    "`Gestion con Escritura Sin Firmar`, `Gestion con Escritura Firmada`, "
    "`Testimonio Generado`, and `Testimonio Ingresado a Inscripcion` as "
    "`INTERMEDIATE`; `Archivada` and `Testimonio Retirado` as `FINAL`."
)
NUMBER = (
    "Yearly serial number of the management. Seeded numbers `1001` and `1002` stay."
)
OPENED = (
    "Date the file was opened. Seeded dates `2026-05-02` and `2026-05-20` stay."
)
PROCEDURE = (
    "Procedure serial number (optional since V13). Seeded numbers `1` and `2` stay."
)
EVENT = (
    "Date of the status change. Seeded dates `2026-05-02`, `2026-05-09`, "
    "`2026-05-28`, `2026-05-20`, and `2026-06-01` stay."
)
LIVE = (ACTIVE, ASSIGNED, NODE, NUMBER, OPENED, PROCEDURE, EVENT)


class WorkflowSampleLabelsEnglishTest(unittest.TestCase):
    def test_workflow_sample_names_the_seeded_graph(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in LIVE:
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| `true` when the workflow is enabled |", dictionary)
        self.assertNotIn(
            "| Workflow assigned to the procedure type (V8) |",
            dictionary,
        )
        self.assertNotIn("| Management status this node represents |", dictionary)
        self.assertNotIn("| Yearly serial number of the management |", dictionary)
        self.assertNotIn("| Date the file was opened |", dictionary)
        self.assertNotIn(
            "| Procedure serial number (optional since V13) |",
            dictionary,
        )
        self.assertNotIn("| Date of the status change |", dictionary)
        self.assertNotIn("Boolean,,,,`true` when the workflow is enabled\n", entities)
        self.assertNotIn(
            "Integer,,x,x,Workflow assigned to the procedure type (V8)\n",
            entities,
        )
        self.assertNotIn(
            "Integer,,x,,Management status this node represents\n",
            entities,
        )
        self.assertNotIn(
            "Integer,,,,Yearly serial number of the management\n",
            entities,
        )
        self.assertNotIn("Date,,,,Date the file was opened\n", entities)
        self.assertNotIn(
            "Integer,,,x,Procedure serial number (optional since V13)\n",
            entities,
        )
        self.assertNotIn("Date,,,,Date of the status change\n", entities)
        self.assertNotIn("`Gestion con Escritura Inscripta` as `FINAL`", dictionary)
        self.assertNotIn("`Gestion con Escritura Inscripta` as `FINAL`", entities)


if __name__ == "__main__":
    unittest.main()
