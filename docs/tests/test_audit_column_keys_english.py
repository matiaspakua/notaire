#!/usr/bin/env python3
"""Audit column keys and the procedure-template note are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class AuditColumnKeysEnglishTest(unittest.TestCase):
    def test_audit_column_keys_are_english(self):
        page = read("frontend/src/app/dashboard/audit/page.tsx")
        self.assertIn('key: "module"', page)
        self.assertIn('key: "detail"', page)
        self.assertIn('t("fields.modulo")', page)
        self.assertIn("r.module", page)
        self.assertIn("r.operationDetail", page)
        self.assertNotIn('key: "modulo"', page)
        self.assertNotIn('key: "detalle"', page)

    def test_procedure_template_note_is_english(self):
        text = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/"
            "ProcedureTemplateControllerTest.java"
        )
        self.assertIn('setNotes("Observation test")', text)
        self.assertNotIn("Obs test", text)


if __name__ == "__main__":
    unittest.main()
