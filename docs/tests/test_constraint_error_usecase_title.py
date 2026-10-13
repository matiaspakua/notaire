#!/usr/bin/env python3
"""The constraint-error record cites CU07 as Testimonio, not the class name."""

import unittest

from openspec_changes import change

FILES = (
    change("fix-579-constraint-errors-400-testimony-person") / "proposal.md",
    change("fix-579-constraint-errors-400-testimony-person") / "traceability.md",
)


class ConstraintErrorUseCaseTitleTest(unittest.TestCase):
    def test_cu07_cites_testimonio(self):
        text = "\n".join(path.read_text(encoding="utf-8") for path in FILES)
        self.assertIn("CU07 – Testimonio", text)
        self.assertIn("CU08 – Movimientos de testimonio", text)
        self.assertIn("CU06 – Personas", text)
        self.assertNotIn("CU07 – Testimony", text)


if __name__ == "__main__":
    unittest.main()
