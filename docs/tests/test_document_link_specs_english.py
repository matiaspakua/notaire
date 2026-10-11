#!/usr/bin/env python3
"""Document-link living specs name procedures and managements in English."""

import unittest
from pathlib import Path

SPECS = Path(__file__).resolve().parents[1] / "openspec" / "specs"


class DocumentLinkSpecsEnglishTest(unittest.TestCase):
    def test_document_procedure_link_prose_is_english(self):
        text = (SPECS / "document-procedure-link" / "spec.md").read_text(encoding="utf-8")
        self.assertTrue(text.startswith("# document-procedure-link Specification"))
        self.assertIn("owner CU04", text)
        self.assertIn("Documents screen", text)
        self.assertIn("#### Scenario: Create a document for a procedure", text)
        self.assertIn("#### Scenario: A management with a linked document", text)
        self.assertIn("#### Scenario: A management without documents", text)
        self.assertNotIn("trámite", text)
        self.assertNotIn("gestión", text)
        self.assertNotIn("Documentos screen", text)

    def test_required_docs_on_confirm_prose_is_english(self):
        text = (SPECS / "required-docs-on-confirm" / "spec.md").read_text(encoding="utf-8")
        self.assertTrue(text.startswith("# required-docs-on-confirm Specification"))
        self.assertIn("`ProcedureTemplate`", text)
        self.assertIn("POST /api/v1/managements/complete-case", text)
        self.assertIn("GET /api/v1/managements/{id}/case-summary", text)
        self.assertIn("CU03", text)
        self.assertIn("### Requirement: Confirming a procedure seeds its required documents", text)
        self.assertNotIn("trámite", text)
        self.assertNotIn("gestión", text)


if __name__ == "__main__":
    unittest.main()
