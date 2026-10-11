#!/usr/bin/env python3
"""Bruno collection names and the documents-due scenario use the live English paths."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class ReportCollectionNamesEnglishTest(unittest.TestCase):
    def test_current_status_collection_is_english(self):
        path = ROOT / "backend-api/api-test/managements/05-current-status.yml"
        self.assertTrue(path.is_file(), path)
        self.assertFalse((ROOT / "backend-api/api-test/managements/05-estado-actual.yml").exists())
        text = path.read_text(encoding="utf-8")
        self.assertIn("/api/v1/managements/{{mgmt_id}}/current-status", text)
        self.assertIn("current-status", text)
        self.assertNotIn("estado-actual", text)

    def test_report_pdf_collections_are_english(self):
        due = read("backend-api/api-test/report-pdfs/11-documents-due-unknown-404.yml")
        debt = read("backend-api/api-test/report-pdfs/12-document-debt.yml")
        missing = read("backend-api/api-test/report-pdfs/13-document-debt-unknown-404.yml")
        self.assertFalse(
            (ROOT / "backend-api/api-test/report-pdfs/11-documentos-por-vencer-unknown-404.yml").exists()
        )
        self.assertFalse(
            (ROOT / "backend-api/api-test/report-pdfs/12-consultar-deuda-documentos.yml").exists()
        )
        self.assertFalse(
            (ROOT / "backend-api/api-test/report-pdfs/13-consultar-deuda-documentos-unknown-404.yml").exists()
        )
        self.assertIn("name: Report - documents due unknown id 404", due)
        self.assertIn("{idSubmittedDocument}", due)
        self.assertIn("/api/v1/reports/documents-due/999999999", due)
        self.assertNotIn("documentos por vencer", due)
        self.assertNotIn("idDocumentoPresentado", due)
        self.assertIn("name: Report - document debt PDF", debt)
        self.assertIn("/api/v1/reports/document-debt?numberManagement=1001", debt)
        self.assertNotIn("consultar deuda", debt)
        self.assertIn("name: Report - document debt unknown number 404", missing)
        self.assertNotIn("consultar deuda", missing)

    def test_documents_due_scenario_is_english(self):
        spec = read("docs/openspec/specs/reportes-404/spec.md")
        controller = read(
            "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/report/ReportController.java"
        )
        self.assertTrue(spec.startswith("# reportes-404 Specification"))
        self.assertIn("#### Scenario: Unknown documents-due id", spec)
        self.assertIn("no documents-due 99999 exists", spec)
        self.assertIn("/api/v1/reports/documents-due/99999", spec)
        self.assertNotIn("documentos-por-vencer", spec)
        self.assertIn("documentos_vencer.pdf", controller)
        self.assertIn("deuda_documentos_", controller)


if __name__ == "__main__":
    unittest.main()
