#!/usr/bin/env python3
"""Guards #2086 (CU76): factory routing matches ADR-030 and the local Cursor handoff.

A source inventory is not a rendered UI audit. The oMLX harness is not the
local Cursor machine.
"""

from __future__ import annotations

import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
CLOUD = REPO / "docs" / "300-development" / "304-ai-sdlc-cloud"
JAVA = REPO / ".claude" / "agents" / "java-architect.md"
BACKEND = REPO / ".claude" / "agents" / "backend-implementer.md"
FRONT = REPO / ".claude" / "agents" / "frontend-design.md"
FOREMAN = REPO / ".claude" / "agents" / "cloud-foreman.md"
MANIFEST = CLOUD / "fleet-manifest.yaml"
HANDOFF = CLOUD / "LOCAL-CURSOR-HANDOFF.md"
FORMS = CLOUD / "FORMS-AUDIT-PLAN.md"
README = CLOUD / "README.md"
SKILLS = REPO / ".claude" / "skills" / "README.md"


class FactoryPhase1RoutingTest(unittest.TestCase):
    def test_architect_follows_adr_030(self):
        text = JAVA.read_text(encoding="utf-8")
        self.assertIn("ADR-030", text)
        self.assertIn("hexagonal-architecture.md", text)
        self.assertNotIn("use for new code", text.lower())
        self.assertNotIn("Controller → Service → Repository", text)

    def test_backend_implementer_does_not_send_new_code_to_legacy_layers(self):
        text = BACKEND.read_text(encoding="utf-8")
        self.assertIn("ADR-030", text)
        self.assertNotIn("new persistence in `repository`", text)
        self.assertNotIn("repository/service/api layers", text)

    def test_manifest_routes_hex_forms_and_security(self):
        text = MANIFEST.read_text(encoding="utf-8")
        self.assertIn("impeccable", text)
        self.assertIn("notaire-cybersec", text)
        self.assertIn("ports and adapters", text.lower())

    def test_frontend_and_skills_name_impeccable_provenance(self):
        front = FRONT.read_text(encoding="utf-8")
        skills = SKILLS.read_text(encoding="utf-8")
        for text in (front, skills):
            self.assertIn("impeccable", text)
            self.assertIn("4.8.1", text)
            self.assertIn("Apache-2.0", text)
        self.assertIn("rendered", front.lower())

    def test_handoff_names_the_local_cursor_worker(self):
        text = HANDOFF.read_text(encoding="utf-8")
        for needle in (
            "agent login",
            "agent worker start",
            "https://cursor.com/docs/cloud-agent/self-hosted/my-machines",
            "#1693",
            "#577",
            "#580",
            "#2086",
            "local-ai/sdlc/foreman.sh",
            "no claim marker",
            "Do **not** start a private worker",
            "Cursor Cloud",
        ):
            self.assertIn(needle, text)

    def test_forms_plan_is_a_code_inventory(self):
        text = FORMS.read_text(encoding="utf-8")
        lowered = text.lower()
        self.assertIn("code inspection", lowered)
        self.assertIn("rendered audit did not run", lowered)
        self.assertIn("form-patterns.tsx", text)
        self.assertIn("people/page.tsx", text)
        self.assertIn("pbakaus/impeccable", text)
        self.assertIn("#2087", text)
        self.assertIn("#2088", text)
        self.assertNotIn("??/20", text)

    def test_indexes_point_at_the_new_docs(self):
        readme = README.read_text(encoding="utf-8")
        foreman = FOREMAN.read_text(encoding="utf-8")
        self.assertIn("LOCAL-CURSOR-HANDOFF.md", readme)
        self.assertIn("FORMS-AUDIT-PLAN.md", readme)
        self.assertIn("LOCAL-CURSOR-HANDOFF.md", foreman)


if __name__ == "__main__":
    unittest.main()
