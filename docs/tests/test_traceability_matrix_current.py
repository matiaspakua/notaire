#!/usr/bin/env python3
"""
Guards #2067 / CU76: the requirement <-> use-case matrix is regenerated, never hand-edited.

The November 2026 documentation audit found the committed matrix covered CU01-CU82 while CU83-CU88
existed. This test regenerates it in memory and fails when the committed file differs.

Run with: python3 docs/tests/test_traceability_matrix_current.py
"""
import importlib.util
import unittest
from pathlib import Path

TOOL = Path(__file__).resolve().parents[1] / "tools" / "generate_traceability_matrix.py"
spec = importlib.util.spec_from_file_location("generate_traceability_matrix", TOOL)
matrix = importlib.util.module_from_spec(spec)
spec.loader.exec_module(matrix)


class TraceabilityMatrixTest(unittest.TestCase):
    def test_committed_matrix_matches_the_use_cases(self):
        self.assertEqual(
            matrix.MATRIX.read_text(encoding="utf-8"),
            matrix.render(),
            "stale matrix: run python3 docs/tools/generate_traceability_matrix.py",
        )

    def test_every_use_case_file_is_a_row(self):
        files = sorted(p.name.split(" ")[0] for p in matrix.USE_CASES.glob("CU*.md"))
        text = matrix.MATRIX.read_text(encoding="utf-8")
        for code in files:
            self.assertIn(f"| {code} ", text, f"{code} missing from the matrix")

    def test_no_reference_points_at_a_missing_requirement(self):
        catalog = matrix.requirements()
        for code, _title, rf, _rnf in matrix.use_cases():
            for n in rf:
                self.assertIn(n, catalog, f"{code} cites RF #{n}, which is not in requerimientos.csv")


if __name__ == "__main__":
    unittest.main()
