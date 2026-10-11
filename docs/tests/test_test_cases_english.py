#!/usr/bin/env python3
"""Live test-case writeups use English filenames and prose.

Run with: python3 docs/tests/test_test_cases_english.py
"""
import re
import unittest
from pathlib import Path

CASES = Path(__file__).resolve().parents[2] / "docs" / "300-development" / "303-testing" / "test-cases"
REQUIRED = [
    "TC-001-prepare-budget-success.md",
    "TC-002-process-budget-request-success.md",
    "TC-003-print-budget-success.md",
    "TC-004-register-client-documents-success.md",
    "TC-005-prepare-deed-success.md",
    "TC-006-sign-deed-success.md",
    "TC-007-generate-testimony-success.md",
    "TC-008-verify-testimony-success.md",
    "TC-009-register-document-debts-success.md",
]
SPANISH = re.compile(
    r"[áéíóúñÁÉÍÓÚÑ]|Título|Descripción|Escenario|Atributo|presupuesto|escritura|testimonio|trámite|tramite|escribano",
    re.IGNORECASE,
)


class TestCasesEnglishTest(unittest.TestCase):
    def test_english_filenames_replace_the_spanish_ones(self):
        names = sorted(path.name for path in CASES.glob("*.md"))
        self.assertEqual(REQUIRED, names)

    def test_prose_is_english(self):
        problems = []
        for path in CASES.glob("*.md"):
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                if SPANISH.search(line):
                    problems.append(f"{path.name}:{number}: {line[:120]}")
        self.assertEqual([], problems)


if __name__ == "__main__":
    unittest.main()
