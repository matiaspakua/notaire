#!/usr/bin/env python3
"""Unit tests for the pure rendering helpers of scripts/generate_data_dictionary.py (#1222 / CU76)."""
import sys
import unittest
from pathlib import Path

_SCRIPTS = str(Path(__file__).resolve().parent)
sys.path.insert(0, _SCRIPTS)
try:
    import generate_data_dictionary as gen
finally:
    sys.path.remove(_SCRIPTS)


def column(**overrides):
    base = {"data_type": "text", "length": None, "precision": None, "scale": None, "nullable": False,
            "default": None, "generated": False}
    return {**base, **overrides}


class RenderingTest(unittest.TestCase):
    def test_generated_integer_is_a_serial(self):
        self.assertEqual("SERIAL (INT)", gen.sql_type(column(data_type="integer", generated=True)))

    def test_varchar_keeps_its_length(self):
        self.assertEqual("VARCHAR(20)", gen.sql_type(column(data_type="character varying", length=20)))

    def test_numeric_keeps_precision_and_scale(self):
        self.assertEqual("NUMERIC(19,2)", gen.sql_type(column(data_type="numeric", precision=19, scale=2)))

    def test_default_drops_the_type_cast(self):
        self.assertEqual("'NORMAL'", gen.default_text(column(default="'NORMAL'::character varying")))

    def test_nullable_column_without_default_shows_null(self):
        self.assertEqual("NULL", gen.default_text(column(nullable=True)))

    def test_mandatory_column_without_default_shows_a_dash(self):
        self.assertEqual("—", gen.default_text(column()))

    def test_primary_key_and_version_come_first(self):
        self.assertEqual(["id", "version", "name"], gen.ordered_columns({"name": 1, "version": 1, "id": 1}, ["id"]))

    def test_missing_description_is_marked_todo(self):
        row = gen.column_row("t", "c", column(), {}, {}, {})
        self.assertTrue(row.endswith("| TODO |"))


if __name__ == "__main__":
    unittest.main()
