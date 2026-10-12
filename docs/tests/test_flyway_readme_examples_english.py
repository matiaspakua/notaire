#!/usr/bin/env python3
"""Flyway guide examples use the live English schema. Applied migrations stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
README = ROOT / "backend-api/src/main/resources/db/migration/README.md"
V1 = ROOT / "backend-api/src/main/resources/db/migration/V1__initial_schema.sql"
V26 = ROOT / "backend-api/src/main/resources/db/migration/V26__rename_leaf_reference_tables_to_english.sql"


class FlywayReadmeExamplesEnglishTest(unittest.TestCase):
    def test_guide_examples_use_english_identifiers(self):
        text = README.read_text(encoding="utf-8")
        self.assertIn("REFERENCES users(id)", text)
        self.assertIn("ALTER TABLE users ADD COLUMN phone VARCHAR(20);", text)
        self.assertIn("ALTER TABLE users ADD COLUMN mobile_phone VARCHAR(20);", text)
        self.assertIn(
            "INSERT INTO procedure_types (version, id, name, enabled, is_archived, is_registered, associates_properties)",
            text,
        )
        self.assertIn("FROM procedure_types WHERE name = 'New Type'", text)
        self.assertIn("procedure_types_id_seq", text)
        for retired in (
            "usuarios",
            "id_usuario",
            "telefono",
            "celular",
            "tipos_de_tramite",
            "habilitado",
            "se_archiva",
            "se_inscribe",
            "asocia_inmuebles",
        ):
            self.assertNotIn(retired, text, retired)

    def test_applied_migrations_keep_the_spanish_schema(self):
        v1 = V1.read_text(encoding="utf-8")
        self.assertIn("CREATE TABLE usuarios", v1)
        self.assertIn("id_usuario SERIAL PRIMARY KEY", v1)
        v26 = V26.read_text(encoding="utf-8")
        self.assertIn("tipos_de_tramite -> procedure_types", v26)
        self.assertIn("habilitado TO enabled", v26)
        self.assertIn("asocia_inmuebles TO associates_properties", v26)


if __name__ == "__main__":
    unittest.main()
