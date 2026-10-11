#!/usr/bin/env python3
"""Living spec prose names the live English contracts. Spec ids stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPECS = ROOT / "docs/openspec/specs"


def read(spec_id: str) -> str:
    return (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")


class LivingSpecDeadNamesEnglishTest(unittest.TestCase):
    def test_current_status_specs_name_management(self):
        tie_break = read("estado-actual-tie-break")
        tie_test = read("estado-actual-tie-test")
        self.assertTrue(tie_break.startswith("# estado-actual-tie-break Specification"))
        self.assertTrue(tie_test.startswith("# estado-actual-tie-test Specification"))
        self.assertIn("a management has two history rows", tie_break)
        self.assertIn("a management has history rows", tie_break)
        self.assertNotIn("gestion", tie_break)
        self.assertIn("`current-status` is requested", tie_test)
        self.assertNotIn("`estado-actual`", tie_test)
        self.assertIn("/api/v1/managements/{id}/current-status", tie_break)

    def test_bruno_controller_list_is_english(self):
        text = read("bruno-zero-coverage-controllers")
        self.assertTrue(text.startswith("# bruno-zero-coverage-controllers Specification"))
        self.assertIn("Management,", text)
        self.assertIn("AuxiliaryProtocol,", text)
        self.assertIn("Report,", text)
        self.assertNotIn("Gestion,", text)
        self.assertNotIn("ProtocoloAuxiliar", text)
        self.assertNotIn("Reporte,", text)

    def test_procedure_type_and_person_prose_is_english(self):
        reliability = read("e2e-test-reliability")
        person = read("persona-validation-error-toast")
        toasts = read("api-error-toasts")
        self.assertIn("procedure-type fixtures", reliability)
        self.assertIn("procedure-type rows", reliability)
        self.assertNotIn("tipo-tramite", reliability)
        self.assertTrue(person.startswith("# persona-validation-error-toast Specification"))
        self.assertIn("people screen", person)
        self.assertIn("When person create or update fails", person)
        self.assertIn("person create/update fails", person)
        self.assertIn("opens the new-person dialog", person)
        self.assertIn("TS-0015-people-clients-workflow.spec.ts", person)
        self.assertNotIn("TS-0015-personas-clientes-workflow.spec.ts", person)
        archive = (
            ROOT
            / "docs/openspec/changes/archive/2026-10-03-fix-945-persona-validation-toast"
            / "specs/persona-validation-error-toast/spec.md"
        ).read_text(encoding="utf-8")
        self.assertIn("TS-0015-personas-clientes-workflow.spec.ts", archive)
        self.assertIn("DNI", person)
        self.assertNotIn("persona create", person)
        self.assertNotIn("Personas", person)
        self.assertNotIn("Nueva Person", person)
        self.assertIn("`name: must not be blank`", toasts)
        self.assertNotIn("nombre:", toasts)


if __name__ == "__main__":
    unittest.main()
