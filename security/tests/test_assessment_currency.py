#!/usr/bin/env python3
"""Security program docs match the tree after release-only CI (CU78, #2003).

Run with: python3 -m unittest security.tests.test_assessment_currency
"""

from __future__ import annotations

import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SEC = REPO / "docs" / "200-architecture" / "206-security"
ASSESSMENT = SEC / "ASSESSMENT-2026-11-05.md"
PROGRAM = SEC / "CYBERSECURITY-PROGRAM.md"
TOOLS = SEC / "SECURITY-TOOLS.md"
OWASP = SEC / "OWASP-TOP10.md"
README = SEC / "README.md"
SKILL = REPO / ".claude" / "skills" / "notaire-cybersec" / "SKILL.md"
FACTORY = REPO / "docs" / "300-development" / "304-ai-sdlc-cloud" / "SECURITY-GATES.md"

STALE = (
    "module grants not yet enforced",
    "image scan report-only",
    "image scan is report-only",
    "Trivy findings stay report-only",
)


class AssessmentCurrencyTest(unittest.TestCase):
    def test_assessment_cites_the_release_only_controls(self):
        text = ASSESSMENT.read_text(encoding="utf-8")
        for needle in (
            "2026-11-05",
            "#2001",
            "#2003",
            "RoleModuleAuthorizationFilter",
            "security/run_gates.py",
            "codeql.yml",
            "dast-zap.yml",
            "workflow_dispatch",
            "not a pentest",
            "does not run on pull_request",
        ):
            self.assertIn(needle, text, needle)

    def test_program_docs_drop_stale_control_claims(self):
        for path in (PROGRAM, OWASP, README):
            text = path.read_text(encoding="utf-8")
            for phrase in STALE:
                self.assertNotIn(phrase, text, f"{path.name}: {phrase}")
        program = PROGRAM.read_text(encoding="utf-8")
        self.assertIn("#1683", program)
        self.assertIn("#2010", program)
        tools = TOOLS.read_text(encoding="utf-8")
        self.assertIn("OWASP Threat Dragon", tools)
        self.assertIn("pytm", tools)
        self.assertIn("preflight.sh --full", tools)

    def test_agent_guidance_is_separate_from_the_product_assessment(self):
        skill = SKILL.read_text(encoding="utf-8")
        factory = FACTORY.read_text(encoding="utf-8")
        self.assertIn("name: notaire-cybersec", skill)
        self.assertIn("security/run_gates.py", skill)
        self.assertIn("#2001", skill)
        self.assertIn("ASSESSMENT-2026-11-05.md", factory)
        self.assertIn("not the product threat model", factory.lower())


if __name__ == "__main__":
    unittest.main()
