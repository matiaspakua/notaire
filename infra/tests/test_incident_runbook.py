"""The incident runbook names commands that exist and does not claim a finished drill (#288)."""

from __future__ import annotations

import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
RUNBOOK = REPO / "infra" / "docs" / "INCIDENT-RUNBOOK.md"
OPERATION = REPO / "infra" / "docs" / "OPERATION.md"


class IncidentRunbookTest(unittest.TestCase):
    def test_runbook_uses_real_commands_and_does_not_claim_a_drill(self) -> None:
        self.assertTrue(RUNBOOK.is_file(), "infra/docs/INCIDENT-RUNBOOK.md is missing")
        text = RUNBOOK.read_text(encoding="utf-8")
        for required in (
            "bash infra/scripts/check-infra.sh",
            "curl http://localhost:8080/actuator/health",
            "bash infra/scripts/verify-signals.sh --with-grafana-panels",
            "docker logs notary-backend",
            "docker logs notary-postgres",
            "## Scenarios",
            "## Escalation",
            "## Communication",
            "## Post-incident",
            "## Recovery checklist",
        ):
            self.assertIn(required, text, required)
        self.assertIn("no on-call rota is configured", text.lower())
        self.assertNotIn("drill completed", text.lower())
        self.assertNotIn("team trained", text.lower())
        self.assertIn("INCIDENT-RUNBOOK.md", OPERATION.read_text(encoding="utf-8"))
        self.assertTrue((REPO / "infra" / "scripts" / "check-infra.sh").is_file())
        self.assertTrue((REPO / "infra" / "scripts" / "verify-signals.sh").is_file())


if __name__ == "__main__":
    unittest.main()
