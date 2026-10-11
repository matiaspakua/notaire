#!/usr/bin/env python3
"""Workflow test fixtures use English identifiers and synthetic names."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class WorkflowFixturesEnglishTest(unittest.TestCase):
    def test_transition_integration_uses_english_names(self):
        text = read("integration/WorkflowTransitionIntegrationTest.java")
        self.assertIn("nodeIntermediate", text)
        self.assertIn("statusInProgress", text)
        self.assertIn('"Start"', text)
        self.assertIn('"In progress"', text)
        self.assertIn('"Finished"', text)
        self.assertIn("Transition from start to in progress", text)
        self.assertNotIn("nodeIntermedio", text)
        self.assertNotIn("statusProceso", text)
        self.assertNotIn('"Inicio"', text)
        self.assertNotIn('"En Proceso"', text)
        self.assertNotIn("inicio a proceso", text)

    def test_validation_fixtures_use_english_names(self):
        text = read("service/unit/WorkflowValidationServiceTest.java")
        self.assertIn("status1", text)
        self.assertIn('"Status1"', text)
        self.assertIn('"Status"', text)
        self.assertIn('"Start"', text)
        self.assertIn('"End"', text)
        self.assertIn('"Isolated"', text)
        self.assertNotIn("estado1", text)
        self.assertNotIn('"Estado1"', text)
        self.assertNotIn('"Inicio"', text)
        self.assertNotIn('"Aislado"', text)

    def test_controller_description_is_english(self):
        text = read("unit/WorkflowTransitionControllerTest.java")
        self.assertIn("Start → Review", text)
        self.assertNotIn("Inicio → Revisión", text)

    def test_query_status_fixture_is_english(self):
        text = read("unit/ManagementQueryServiceTest.java")
        self.assertIn('"In progress"', text)
        self.assertNotIn('"En Proceso"', text)

    def test_response_record_names_are_english(self):
        text = read("integration/ControllerResponseRecordsIntegrationTest.java")
        self.assertIn("Cost type", text)
        self.assertIn("Cost document", text)
        self.assertIn("Procedure type", text)
        self.assertNotIn("Tipo costos", text)
        self.assertNotIn("Doc costos", text)
        self.assertNotIn("Tipo tramite", text)
        self.assertIn("Cliente", text)


if __name__ == "__main__":
    unittest.main()
