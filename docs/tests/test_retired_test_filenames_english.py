#!/usr/bin/env python3
"""Open change notes name the live test files."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class RetiredTestFilenamesEnglishTest(unittest.TestCase):
    def test_copies_change_names_the_live_playwright_file(self):
        folder = CHANGES / "fix-655-required-ids-copy-draft-reentry"
        combined = "\n".join(
            (folder / name).read_text(encoding="utf-8")
            for name in ("proposal.md", "design.md", "tasks.md", "traceability.md")
        )
        self.assertIn("TS-0132-copies-required-fields.spec.ts", combined)
        self.assertNotIn("copias-required-fields", combined)
        self.assertIn("### Módulos afectados", (folder / "proposal.md").read_text(encoding="utf-8"))

    def test_people_pagination_names_the_live_unit_test(self):
        folder = CHANGES / "fix-596-people-pagination"
        design = (folder / "design.md").read_text(encoding="utf-8")
        ledger = (folder / "traceability.md").read_text(encoding="utf-8")
        combined = design + "\n" + ledger
        self.assertIn("people-pagination.test.tsx", combined)
        self.assertIn("TS-0015-people-clients-workflow.spec.ts", design)
        self.assertNotIn("use-personas-pagination", combined)
        self.assertNotIn("personas workflow", design)
        self.assertIn("## Riesgos / Trade-offs", design)


if __name__ == "__main__":
    unittest.main()
