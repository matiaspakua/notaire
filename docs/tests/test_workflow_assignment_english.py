#!/usr/bin/env python3
"""Workflow assignment copy names the active workflow in English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

CONTROLLER = (
    "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/"
    "procedure/ProcedureTypeController.java"
)
OPENAPI = "backend-api/openapi/openapi.yaml"
GENERATED = "frontend/src/types/api.generated.ts"
ASSIGNMENT_TEST = (
    "backend-api/src/test/java/com/licensis/notaire/unit/"
    "ProcedureTypeWorkflowAssignmentTest.java"
)

SCHEMA = "Active workflow to assign, or null to unassign the current one"
BAD_REQUEST = (
    "workflowDefinitionId is required (an integer, or null to unassign)"
)
CONFLICT = "The selected workflow is not active"
SUMMARY = "Assign or unassign a workflow on a procedure type"


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class WorkflowAssignmentEnglishTest(unittest.TestCase):
    def test_contract_names_the_selected_workflow(self):
        for relative in (CONTROLLER, OPENAPI, GENERATED):
            source = read(relative)
            self.assertIn(SCHEMA, source, relative)
            self.assertIn(BAD_REQUEST, source, relative)
            self.assertIn(CONFLICT, source, relative)
            self.assertIn(SUMMARY, source, relative)
            self.assertNotIn("seleccionado", source, relative)
            self.assertNotIn("entero", source, relative)
            self.assertNotIn("Workflow active a assign", source, relative)
            self.assertNotIn("workflow a procedure type", source, relative)

    def test_inactive_fixture_is_english(self):
        source = read(ASSIGNMENT_TEST)
        self.assertIn("Inactive workflow", source)
        self.assertNotIn("Inactivo", source)


if __name__ == "__main__":
    unittest.main()
