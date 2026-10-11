#!/usr/bin/env python3
"""Auxiliary numbering names, concept fixtures, and leftover comments are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class NumberingNamesEnglishTest(unittest.TestCase):
    def test_auxiliary_numbering_names_are_english(self):
        names = (
            "backend-api/src/main/java/com/licensis/notaire/repository/FolioRepository.java",
            "backend-api/src/main/java/com/licensis/notaire/application/port/out/folio/FolioRepositoryPort.java",
            "backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/folio/FolioPersistenceAdapter.java",
            "backend-api/src/main/java/com/licensis/notaire/application/usecase/notebook/AuxiliaryProtocolService.java",
            "backend-api/src/test/java/com/licensis/notaire/service/unit/AuxiliaryProtocolServiceTest.java",
        )
        for relative in names:
            source = read(relative)
            self.assertNotIn("findMaxNumberDeedAuxiliary", source, relative)
            self.assertNotIn("calculateNextNumberAuxiliary", source, relative)
        self.assertIn("findMaxAuxiliaryDeedNumber", read(names[0]))
        service = read(names[3])
        self.assertIn("calculateNextAuxiliaryNumber", service)
        self.assertIn("findMaxAuxiliaryDeedNumber", service)

        movement = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/TestimonyMovementServiceTest.java"
        )
        self.assertIn("shouldRejectWithdrawalWhenNotInscribed", movement)
        self.assertIn("not inscribed", movement)
        self.assertNotIn("NotInscripto", movement)
        self.assertIn("registered", movement)

    def test_concept_fixtures_and_comments_are_english(self):
        concepts = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/ConceptReferentialIntegrityTest.java"
        )
        self.assertIn("ConceptFree_InUse", concepts)
        self.assertIn("ConceptUsed_InUse", concepts)
        self.assertIn("ConceptEditBlocked", concepts)
        self.assertIn("ConceptEditAllowed", concepts)
        self.assertIn("ConceptDeleteBlocked", concepts)
        self.assertNotIn("Concepto", concepts)

        entities = read("backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java")
        self.assertIn("idPerson", entities)
        self.assertNotIn("idPersona", entities)

        cascade = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/ConceptDeleteCascadeIntegrationTest.java"
        )
        self.assertIn("HistoryDeleteIntegrationTest", cascade)
        self.assertNotIn("HistorialDelete", cascade)

        people = read("backend-api/src/test/java/com/licensis/notaire/unit/SimpleControllersTest.java")
        self.assertIn("missing identification type", people)
        self.assertNotIn("tipo identification", people)

        workflow = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/BusinessWorkflowIntegrationTest.java"
        )
        self.assertIn("type=1", workflow)
        self.assertNotIn("tipo=1", workflow)

        broken = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/PreviouslyBrokenEndpointsIntegrationTest.java"
        )
        self.assertIn("folio types", broken)
        self.assertNotIn("tipo-folio", broken)


if __name__ == "__main__":
    unittest.main()
