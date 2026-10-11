#!/usr/bin/env python3
"""Date parameters, note fixtures, and leftover prose are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class DateAndNoteWordsEnglishTest(unittest.TestCase):
    def test_date_parameters_are_english(self):
        source = read(
            "backend-api/src/main/java/com/licensis/notaire/service/ValidationAdministrator.java"
        )
        self.assertIn("Date startDate, Date endDate", source)
        self.assertIn("endDate.after(startDate)", source)
        self.assertIn("Date today", source)
        self.assertIn("today.before(year)", source)
        self.assertNotIn("dateDesde", source)
        self.assertNotIn("dateHasta", source)
        self.assertNotIn("Date actual", source)
        self.assertIn("'¿'", source)
        self.assertIn("'¡'", source)

    def test_note_fixtures_say_notes(self):
        for relative in (
            "backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java",
            "backend-api/src/test/java/com/licensis/notaire/unit/ProcedureEntityTest.java",
            "backend-api/src/test/java/com/licensis/notaire/integration/HistoryDeleteIntegrationTest.java",
        ):
            source = read(relative)
            self.assertIn('"notes"', source, relative)
            self.assertNotIn('"obs"', source, relative)

    def test_substitution_comments_and_tracker_title_are_english(self):
        substitutions = read("testing/e2e/tests/TS-0017-substitutions-workflow.spec.ts")
        self.assertIn("notary and substitute", substitutions)
        self.assertIn("start and end", substitutions)
        self.assertIn("substitute ID", substitutions)
        self.assertIn('"suplencias"', substitutions)
        self.assertIn("Suplencias", substitutions)
        self.assertIn("nueva suplencia", substitutions)
        self.assertIn("Nueva suplencia", substitutions)
        self.assertIn("registrada", substitutions)
        self.assertNotIn("suplente", substitutions)
        self.assertNotIn("Desde", substitutions)
        self.assertNotIn("Hasta", substitutions)

        tracker = read("testing/e2e/tests/TS-0138-workflow-tracker.spec.ts")
        self.assertIn("degrades without post-signing nodes", tracker)
        self.assertIn("Testimonio Generado", tracker)
        self.assertIn("Gestion con Escritura Firmada", tracker)
        self.assertNotIn("post-firma", tracker)


if __name__ == "__main__":
    unittest.main()
