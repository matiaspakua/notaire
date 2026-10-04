#!/usr/bin/env python3
"""
Guards issue #1222 / CU76: the Diccionario de Datos describes the schema Flyway builds.

The committed ERD artifacts are generated from that schema (scripts/generate_erd.py) and guarded
by test_erd_current_schema.py, so comparing the dictionary with them detects drift without a
database. Regenerate the dictionary with scripts/generate_data_dictionary.py.

Run with: python3 scripts/test_data_dictionary_sync.py
"""
import csv
import re
import unittest
from collections import OrderedDict
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
DATA_MODEL = REPO_ROOT / "docs" / "200-architecture" / "205-data-model"
DICTIONARY = DATA_MODEL / "Diccionario de Datos.md"
CSV_FILE = DATA_MODEL / "ERD" / "Modelo Relacional Escribania - Entidades.csv"
FULL_PUML = DATA_MODEL / "ERD" / "ERD-Escribania_completo.puml"

SECTION_HEADING = re.compile(r"^###\s+\d+\.\s+`(\w+)`")
INDEX_ROW = re.compile(r"^\|\s*\d+\s*\|\s*\[(\w+)\]")
COLUMN_ROW = re.compile(r"^\|\s*`(\w+)`\s*\|(.*)\|\s*$")
MATRIX_ROW = re.compile(r"^\|\s*`(\w+)`\s*\|\s*`(\w+)`\s*\|\s*`(\w+)`\s*\|\s*`(\w+)`\s*\|")
RELATIONSHIP = re.compile(r'^(\w+)\s+\S+--\S+\s+(\w+)\s*:\s*"(\w+)"')


def split_sections(text):
    sections, current = {}, None
    for line in text.splitlines():
        match = re.match(r"^##\s+(\d+)\.", line)
        if match:
            current = int(match.group(1))
        sections.setdefault(current, []).append(line)
    return sections


def dictionary_index(lines):
    return [m.group(1) for m in map(INDEX_ROW.match, lines) if m]


def dictionary_columns(lines):
    tables, table = OrderedDict(), None
    for line in lines:
        heading = SECTION_HEADING.match(line)
        if heading:
            table = heading.group(1)
            tables[table] = OrderedDict()
            continue
        row = COLUMN_ROW.match(line)
        if table and row:
            cells = [c.strip() for c in row.group(2).split("|")]
            tables[table][row.group(1)] = {"pk": cells[1] == "Sí", "fk": cells[2] == "Sí",
                                           "not_null": cells[3] == "Sí", "description": cells[-1]}
    return tables


def dictionary_matrix(lines):
    return {m.groups()[:2] + (m.group(3),) for m in map(MATRIX_ROW.match, lines) if m}


def schema_columns():
    tables = OrderedDict()
    with open(CSV_FILE, encoding="utf-8", newline="") as handle:
        for row in csv.DictReader(handle):
            tables.setdefault(row["Nombre Entidad"], OrderedDict())[row["Atributo"]] = {
                "pk": row["PK"] == "x", "fk": row["FK"] == "x", "not_null": row["NULL"] != "x"}
    return tables


def schema_relationships():
    found = set()
    for line in FULL_PUML.read_text(encoding="utf-8").splitlines():
        match = RELATIONSHIP.match(line)
        if match:
            parent, child, column = match.groups()
            found.add((child, column, parent))
    return found


class DataDictionarySyncTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        sections = split_sections(DICTIONARY.read_text(encoding="utf-8"))
        cls.index = dictionary_index(sections[2])
        cls.documented = dictionary_columns(sections[4])
        cls.matrix = dictionary_matrix(sections[5])
        cls.schema = schema_columns()

    def test_every_schema_table_has_a_section_and_vice_versa(self):
        self.assertEqual(set(self.schema), set(self.documented))

    def test_index_lists_every_table_once(self):
        self.assertEqual(sorted(self.index), sorted(self.schema))

    def test_columns_match_the_schema_for_every_table(self):
        drift = {t: sorted(set(cols) ^ set(self.documented.get(t, {})))
                 for t, cols in self.schema.items() if set(cols) != set(self.documented.get(t, {}))}
        self.assertEqual({}, drift)

    def test_key_and_nullability_flags_match_the_schema(self):
        mismatches = [f"{table}.{column}" for table, cols in self.schema.items()
                      for column, flags in cols.items() if column in self.documented.get(table, {})
                      and any(self.documented[table][column][k] != flags[k] for k in ("pk", "fk", "not_null"))]
        self.assertEqual([], mismatches)

    def test_referential_matrix_equals_the_schema_foreign_keys(self):
        self.assertEqual(schema_relationships(), self.matrix)

    def test_no_description_is_left_as_todo(self):
        todo = [f"{t}.{c}" for t, cols in self.documented.items() for c, info in cols.items()
                if not info["description"] or "TODO" in info["description"]]
        self.assertEqual([], todo)


if __name__ == "__main__":
    unittest.main()
