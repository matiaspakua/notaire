#!/usr/bin/env python3
"""Living testing docs and the cadastral check use English practice names."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class TestingPracticeNamesEnglishTest(unittest.TestCase):
    def test_master_plan_and_process_names_are_english(self):
        plan = read("docs/300-development/303-testing/TEST-PLAN.md")
        self.assertIn("### 3.3 Identification (artifact IDs)", plan)
        self.assertIn("test artifacts (identification).", plan)
        self.assertIn("budgets, managements, payments", plan)
        self.assertNotIn("identificación", plan)
        self.assertNotIn("Identificación", plan)
        self.assertNotIn("gestiones", plan)

        process = read("docs/300-development/303-testing/PROCESS-ARCHITECTURE.md")
        self.assertIn("| Organization / knowledge base |", process)
        self.assertIn("| Identification |", process)
        self.assertIn("| Traceability |", process)
        self.assertIn("| Pyramid / fail-fast |", process)
        for retired in ("Organización", "Identificación", "Trazabilidad", "Pirámide"):
            self.assertNotIn(retired, process, retired)

    def test_adr_and_merge_gate_names_are_english(self):
        strategy = read("docs/200-architecture/202-ADR/ADR-006-testing-strategy.md")
        self.assertIn("pyramid / traceability", strategy)
        self.assertNotIn("pirámide", strategy)
        self.assertNotIn("trazabilidad", strategy)

        topology = read("docs/200-architecture/202-ADR/ADR-024-repository-topology.md")
        self.assertIn("managements/budgets/documents/dashboard", topology)
        self.assertNotIn("gestiones/", topology)

        gate = read("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md")
        self.assertIn("budget pickers", gate)
        self.assertNotIn("presupuesto pickers", gate)

    def test_missing_cadastral_check_is_english(self):
        spec = read("backend-api/api-test/properties/08-create-missing-cadastral.yml")
        self.assertIn("cadastral designation", spec)
        self.assertIn("Bruno without a cadastral designation", spec)
        self.assertNotIn("nomenclatura", spec)
        self.assertIn("cadastralDesignation", spec)


if __name__ == "__main__":
    unittest.main()
