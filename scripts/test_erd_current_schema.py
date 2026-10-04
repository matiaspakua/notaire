#!/usr/bin/env python3
"""
Guards issue #1021 / CU76: the ERD artifacts describe the English schema Flyway builds.

The retired Spanish table names are derived from the `ALTER TABLE ... RENAME TO` statements
of the migrations themselves, so the list cannot go stale. The three artifacts (PlantUML
source, rendered SVG, relational CSV) must also agree on the set of entities.

Run with: python3 scripts/test_erd_current_schema.py
"""
import csv
import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
MIGRATIONS = REPO_ROOT / "backend-api" / "src" / "main" / "resources" / "db" / "migration"
ERD_DIR = REPO_ROOT / "docs" / "200-architecture" / "205-data-model" / "ERD"
FULL_PUML = ERD_DIR / "ERD-Escribania_completo.puml"
SIMPLE_PUML = ERD_DIR / "ERD.puml"
FULL_SVG = ERD_DIR / "ERD_Escribania_completo.svg"
SIMPLE_SVG = ERD_DIR / "ERD_Escribania.svg"
CSV_FILE = ERD_DIR / "Modelo Relacional Escribania - Entidades.csv"

RENAME = re.compile(r"ALTER\s+TABLE\s+(\w+)\s+RENAME\s+TO\s+(\w+)", re.IGNORECASE)
PUML_ENTITY = re.compile(r'^\s*entity\s+"[^"]*"\s+as\s+(\w+)', re.MULTILINE)
SVG_ENTITY = re.compile(r"<!--(?:entity|class) (\w+)-->")


def renamed_tables():
    old_to_new = {}
    for migration in sorted(MIGRATIONS.glob("V*.sql")):
        for old, new in RENAME.findall(migration.read_text(encoding="utf-8")):
            old_to_new[old.lower()] = new.lower()
    return old_to_new


def puml_entities(path):
    return set(PUML_ENTITY.findall(path.read_text(encoding="utf-8")))


def svg_entities(path):
    return set(SVG_ENTITY.findall(path.read_text(encoding="utf-8")))


def csv_entities():
    with open(CSV_FILE, encoding="utf-8", newline="") as handle:
        return {row[0] for row in csv.reader(handle) if row and row[0] != "Nombre Entidad"}


class ErdUsesEnglishSchemaTest(unittest.TestCase):
    def test_migrations_define_renames(self):
        self.assertGreaterEqual(len(renamed_tables()), 20)

    def test_puml_sources_use_no_retired_table_name(self):
        retired = set(renamed_tables())
        for path in (FULL_PUML, SIMPLE_PUML):
            with self.subTest(file=path.name):
                self.assertEqual([], sorted(puml_entities(path) & retired))

    def test_rendered_svgs_use_no_retired_table_name(self):
        retired = set(renamed_tables())
        for path in (FULL_SVG, SIMPLE_SVG):
            with self.subTest(file=path.name):
                self.assertEqual([], sorted(svg_entities(path) & retired))

    def test_csv_uses_no_retired_table_name(self):
        self.assertEqual([], sorted(csv_entities() & set(renamed_tables())))

    def test_renamed_tables_appear_under_their_new_name(self):
        new_names = set(renamed_tables().values()) - {"users"}
        self.assertEqual([], sorted(new_names - puml_entities(FULL_PUML)))


class ErdArtifactsAgreeTest(unittest.TestCase):
    def test_full_puml_svg_and_csv_describe_the_same_entities(self):
        self.assertEqual(puml_entities(FULL_PUML), svg_entities(FULL_SVG))
        self.assertEqual(puml_entities(FULL_PUML), csv_entities())

    def test_simple_puml_and_svg_describe_the_same_entities(self):
        self.assertEqual(puml_entities(SIMPLE_PUML), svg_entities(SIMPLE_SVG))

    def test_simple_diagram_is_a_subset_of_the_full_one(self):
        self.assertEqual(set(), puml_entities(SIMPLE_PUML) - puml_entities(FULL_PUML))


if __name__ == "__main__":
    unittest.main()
