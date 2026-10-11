#!/usr/bin/env python3
"""The compensation diagram uses current English table and column names.

Run with: python3 docs/tests/test_compensation_diagram.py
"""
import csv
import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DATA_MODEL = REPO_ROOT / "docs" / "200-architecture" / "205-data-model"
CSV_FILE = DATA_MODEL / "diagrama-compensacion" / "Diagrama de Compensacion.csv"
PUML_FILE = DATA_MODEL / "diagrama-compensacion" / "Diagrama-Compensacion.puml"
DICTIONARY = DATA_MODEL / "Diccionario de Datos.md"

HEADER = ["Id", "Parent", "Child", "Foreign key", "Insert", "Update", "Delete", "Business rule"]
ACTIONS = {"Restrict", "Cascade", "Null"}
LEGACY_CHILD = "identifications"
MATRIX_ROW = re.compile(r"^\|\s*`(\w+)`\s*\|\s*`([^`]+)`\s*\|\s*`(\w+)`\s*\|")
SPANISH = re.compile(
    r"Impedir|Cascada|Insertar|Entidad_Padre|Personas|Trámite|Tramite|Gestión|Gestion|"
    r"Presupuesto|Escritura|Usuario|Suplencia|Identificacion|Regla_Negocio|node_type"
)


def dictionary_tables_and_columns():
    tables, columns = set(), set()
    for line in DICTIONARY.read_text(encoding="utf-8").splitlines():
        match = MATRIX_ROW.match(line)
        if match:
            child, column, parent = match.group(1), match.group(2).strip(), match.group(3)
            tables.update((child, parent))
            columns.add(column)
    return tables, columns


class CompensationDiagramTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with CSV_FILE.open(encoding="utf-8", newline="") as handle:
            reader = csv.reader(handle)
            cls.header = next(reader)
            cls.rows = list(reader)
        cls.tables, cls.columns = dictionary_tables_and_columns()
        cls.puml = PUML_FILE.read_text(encoding="utf-8")

    def test_header_is_english(self):
        self.assertEqual(HEADER, self.header)

    def test_actions_and_names_match_the_current_schema(self):
        problems = []
        for row in self.rows:
            parent, child, foreign_key, insert, update, delete, rule = row[1:]
            if insert not in ACTIONS or update not in ACTIONS or delete not in ACTIONS:
                problems.append(f"action {row[0]}: {insert}/{update}/{delete}")
            for name in (parent, child):
                if name != LEGACY_CHILD and name not in self.tables:
                    problems.append(f"entity {row[0]}: {name}")
            for column in (part.strip() for part in foreign_key.split("/")):
                if column not in self.columns:
                    problems.append(f"column {row[0]}: {column}")
            if child == LEGACY_CHILD and "Flyway does not create" not in rule:
                problems.append(f"legacy note {row[0]}")
            if SPANISH.search(rule) or SPANISH.search(foreign_key):
                problems.append(f"spanish {row[0]}: {rule}")
        self.assertEqual([], problems)

    def test_diagram_source_is_english(self):
        allowed = self.puml.replace("No Pasó", "")
        self.assertIsNone(SPANISH.search(allowed))
        self.assertNotIn("Impedir", self.puml)


if __name__ == "__main__":
    unittest.main()
