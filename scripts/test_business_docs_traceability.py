#!/usr/bin/env python3
"""
Guards issue #956 / CU84: the business documents stay machine-traceable.

- every row of requerimientos.csv has a `#<n>` GitHub ID, four columns, and a unique ID;
- the Login requirement appears exactly once;
- every Use Case file carries the "Referencias Cruzadas" and "GitHub ID" rows of the standard template.

Run with: python3 scripts/test_business_docs_traceability.py
"""
import csv
import re
import unittest
from collections import Counter
from pathlib import Path

BUSINESS = Path(__file__).resolve().parents[1] / "docs" / "100-business"
REQUIREMENTS = BUSINESS / "101-requirements" / "requerimientos.csv"
USE_CASES = BUSINESS / "102-use-cases"
GITHUB_ID = re.compile(r"^#\d+$")


def requirement_rows():
    with open(REQUIREMENTS, encoding="utf-8", newline="") as handle:
        return list(csv.reader(handle))[1:]


class RequirementsCsvTest(unittest.TestCase):
    def test_every_row_has_four_columns_and_a_github_id(self):
        malformed = [row[:2] for row in requirement_rows() if len(row) != 4 or not GITHUB_ID.match(row[0])]
        self.assertEqual([], malformed)

    def test_github_ids_are_unique(self):
        duplicated = [i for i, n in Counter(row[0] for row in requirement_rows()).items() if n > 1]
        self.assertEqual([], duplicated)

    def test_login_requirement_appears_exactly_once(self):
        logins = [row for row in requirement_rows() if row[1].strip().lower() == "login al sistema"]
        self.assertEqual(1, len(logins))


class UseCaseTemplateTest(unittest.TestCase):
    def test_every_use_case_has_cross_references_and_github_ids(self):
        missing = []
        for path in sorted(USE_CASES.glob("*.md")):
            text = path.read_text(encoding="utf-8")
            if "**Referencias Cruzadas**" not in text or "**GitHub ID**" not in text:
                missing.append(path.name)
        self.assertEqual([], missing)


if __name__ == "__main__":
    unittest.main()
