"""Guards #2514 docs option 3: threat models stay; program docs stub to the fleet."""

from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SEC = ROOT / "docs" / "200-architecture" / "206-security"
PROGRAM_STUBS = (
    "CYBERSECURITY-PROGRAM.md",
    "ASSESSMENT-2026-11-05.md",
    "SECURITY-TOOLS.md",
    "MONITORING.md",
    "HTTPS-TLS-RUNBOOK.md",
    "INSTALLATION-ISOLATION-CHECKLIST.md",
)


class SecurityDocsSeamTest(unittest.TestCase):
    def test_threat_model_docs_remain_in_product(self) -> None:
        for relative in (
            "THREAT-MODEL-SYSTEM.md",
            "THREAT-MODEL-AUTH-AUDIT.md",
            "threat-model",
            "diagrams",
        ):
            path = SEC / relative
            self.assertTrue(path.exists(), relative)

    def test_program_docs_are_stubs_to_notaire_security(self) -> None:
        for name in PROGRAM_STUBS:
            text = (SEC / name).read_text(encoding="utf-8")
            self.assertIn("matiaspakua/notaire-security", text, name)
            self.assertIn("Moved", text, name)

    def test_readme_records_the_fleet_split(self) -> None:
        readme = (SEC / "README.md").read_text(encoding="utf-8")
        self.assertIn("notaire-security", readme)
        self.assertIn("Fleet split", readme)


if __name__ == "__main__":
    unittest.main()
