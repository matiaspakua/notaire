#!/usr/bin/env python3
"""Substitution lookup parameters use the English name substituted."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FILES = (
    "backend-api/src/main/java/com/licensis/notaire/repository/SubstitutionRepository.java",
    "backend-api/src/main/java/com/licensis/notaire/application/port/out/substitution/SubstitutionRepositoryPort.java",
    "backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/substitution/SubstitutionPersistenceAdapter.java",
    "backend-api/src/main/java/com/licensis/notaire/application/usecase/substitution/SubstitutionService.java",
)


class SubstitutedParameterNamesEnglishTest(unittest.TestCase):
    def test_substituted_parameters_are_english(self):
        for relative in FILES:
            text = (ROOT / relative).read_text(encoding="utf-8")
            self.assertIn("Person substituted", text, relative)
            self.assertIn("Integer substitutedId", text, relative)
            self.assertIn("findByFkIdSubstituted", text, relative)
            self.assertNotIn("suplantado", text, relative)


if __name__ == "__main__":
    unittest.main()
