#!/usr/bin/env python3
"""
Guards #2067 / CU76: every ADR file is listed in the ADR index with a status.

The November 2026 documentation audit found ADR-030 (the accepted hexagonal architecture) missing from
docs/200-architecture/202-ADR/README.md while the Pages architecture page, which reads the folder,
listed it. The index is hand-written, so this test keeps it whole.

Run with: python3 docs/tests/test_adr_index_complete.py
"""
import re
import unittest
from pathlib import Path

ADR_DIR = Path(__file__).resolve().parents[1] / "200-architecture" / "202-ADR"
STATUSES = ("Accepted", "Proposed", "Superseded", "Deprecated", "Rejected")


class AdrIndexTest(unittest.TestCase):
    def test_every_adr_file_has_an_index_row_with_a_status(self):
        index = (ADR_DIR / "README.md").read_text(encoding="utf-8")
        for path in sorted(ADR_DIR.glob("ADR-*.md")):
            row = next((line for line in index.splitlines() if f"]({path.name})" in line), None)
            self.assertIsNotNone(row, f"{path.name} is not listed in 202-ADR/README.md")
            self.assertTrue(any(s in row for s in STATUSES), f"{path.name} index row has no status")

    def test_index_rows_point_at_files_that_exist(self):
        index = (ADR_DIR / "README.md").read_text(encoding="utf-8")
        for name in re.findall(r"\]\((ADR-[^)]+\.md)\)", index):
            self.assertTrue((ADR_DIR / name).is_file(), f"index links missing {name}")


if __name__ == "__main__":
    unittest.main()
