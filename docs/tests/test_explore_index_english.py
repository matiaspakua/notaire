#!/usr/bin/env python3
"""explore.md prose is English. The filename, change slugs, and finding identifiers stay."""

import unittest
from pathlib import Path

EXPLORE = Path(__file__).resolve().parents[1] / "openspec" / "explore.md"


class ExploreIndexEnglishTest(unittest.TestCase):
    def test_prose_is_english_and_keeps_the_record(self):
        self.assertTrue(EXPLORE.is_file(), EXPLORE)
        text = EXPLORE.read_text(encoding="utf-8")
        for live in (
            "# Notaire — Business finding index",
            "finding↔Issue",
            "`folio-vinculacion-escritura`",
            "[#838]",
            "`gestion-workflow-reingreso-testimonio`",
            "`CU84 - Login.md`",
            "`gestion-archive-deuda-check`",
            "`GestionArchiveDebtService`",
            "`importeAPagar`",
            "`ManagementHistory.fecha`",
            '"Efectivo"',
            '"observaciones"',
            "`fix/957_silent-delete-persistable-fix`",
            "`main@5996b3d`",
            "No change",
            "Proposed",
            "Orphan/invalid",
            "Archived",
        ):
            self.assertIn(live, text, live)
        for retired in (
            "Índice de Hallazgos",
            "Pendientes (abiertos)",
            "hallazgo de negocio",
            "Resueltos (mergeados",
            "Estado OpenSpec",
            "Sin change",
            "Huérfano",
        ):
            self.assertNotIn(retired, text, retired)


if __name__ == "__main__":
    unittest.main()
