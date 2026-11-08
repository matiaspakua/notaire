#!/usr/bin/env python3
"""Synthetic transition fixtures, notebook locals, and the substitution comment are English."""

import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


def lacks(source: str, token: str) -> bool:
    return re.search(r"\b" + re.escape(token) + r"\b", source) is None


class TransitionFixtureWordsEnglishTest(unittest.TestCase):
    def test_transition_fixtures_are_english(self):
        source = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/management/TransitionManagementUseCaseTest.java"
        )
        self.assertIn('"Initial"', source)
        self.assertIn("intermediateStatus", source)
        self.assertIn('"Intermediate"', source)
        self.assertIn("unreachableStatus", source)
        self.assertIn('"Unreachable"', source)
        self.assertTrue(lacks(source, "Inicial"))
        self.assertTrue(lacks(source, "Intermedio"))
        self.assertTrue(lacks(source, "Inalcanzable"))
        self.assertTrue(lacks(source, "statusIntermedio"))
        self.assertTrue(lacks(source, "statusInasequible"))

    def test_history_note_is_english(self):
        source = read(
            "backend-api/src/test/java/com/licensis/notaire/application/usecase/history/HistoryMapperTest.java"
        )
        self.assertIn('"Initial status change"', source)
        self.assertNotIn("Cambio de status", source)
        self.assertIn('"En proceso"', source)

    def test_notebook_locals_and_substitution_comment_are_english(self):
        notebooks = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/NotebookControllerTest.java"
        )
        self.assertIn("firstBatch", notebooks)
        self.assertIn("secondBatch", notebooks)
        self.assertIn("withReassignedFolio", notebooks)
        self.assertIn("discontinuous", notebooks)
        self.assertTrue(lacks(notebooks, "primerLote"))
        self.assertTrue(lacks(notebooks, "segundoLote"))
        self.assertTrue(lacks(notebooks, "conFolioReasignado"))
        self.assertTrue(lacks(notebooks, "discontinuos"))
        self.assertIn('"New"', notebooks)
        self.assertIn('"Errose"', notebooks)

        # #576 deleted SubstitutionJpaController; the named query lives on the entity.
        self.assertFalse(
            (
                ROOT
                / "backend-api/src/main/java/com/licensis/notaire/jpa/SubstitutionJpaController.java"
            ).exists(),
            "SubstitutionJpaController must stay deleted after #576",
        )
        substitution_entity = read(
            "backend-api/src/main/java/com/licensis/notaire/business/Substitution.java"
        )
        self.assertIn("Substitution.findSubstitutionsByYear", substitution_entity)
        self.assertTrue(lacks(substitution_entity, "unaSubstitution"))


if __name__ == "__main__":
    unittest.main()
