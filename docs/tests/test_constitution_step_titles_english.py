#!/usr/bin/env python3
"""Constitution workflow step titles are English. The Gate 3 heading stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CONSTITUTION = ROOT / "CONSTITUTION.md"


class ConstitutionStepTitlesEnglishTest(unittest.TestCase):
    def test_workflow_step_titles_are_english(self):
        text = CONSTITUTION.read_text(encoding="utf-8")
        for live in (
            "Refine requirement",
            "Create branch (<type>/<issue-number>_<description>)",
            "Define acceptance criteria",
            "Design test cases",
            "Write unit tests (failing)",
            "Write integration tests (failing)",
            "Implement (make tests pass)",
            "Update existing tests",
            "Run tests (unit + integration)",
            "Full regression",
            "Create pull request",
            "Wait for green CI/CD",
            "Close the issue",
            "**2. Refine the requirement.**",
            "**6. Create the branch.**",
            "**8. Design test cases.**",
            "**11. Implement.**",
            "**14. Full regression.**",
            "**17.5 Run the full pipeline.**",
            "**18. Create the pull request.**",
            "**19. Wait for green CI/CD.**",
            "**24. Close the issue.**",
            "self-contained",
            "Actualizar documentación permanente",
            "Use Case (Caso de Uso)",
            'finding ("hallazgo")',
        ):
            self.assertIn(live, text, live)
        for retired in (
            "Refinar requerimiento",
            "Crear Branch",
            "Definir Acceptance Criteria",
            "Diseñar casos de prueba",
            "Escribir Unit Tests",
            "Escribir Integration Tests",
            "Implementar (make tests pass)",
            "Actualizar Tests existentes",
            "Ejecutar Tests",
            "Regression completa",
            "Crear Pull Request",
            "Esperar CI/CD verde",
            "Cerrar Issue",
            "Ejecutar el pipeline completo",
            "autocontenido",
        ):
            self.assertNotIn(retired, text, retired)


if __name__ == "__main__":
    unittest.main()
