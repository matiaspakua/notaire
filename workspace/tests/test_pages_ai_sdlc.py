#!/usr/bin/env python3
"""Guards #1934: the Pages "AI Factory" section explains the AI SDLC, apart from the product docs.

The section must stay grounded in the repository: every path it cites through
``src("…")`` must exist, every skill/agent/workflow it names must be in its
catalog, and counts are read from the repo at build time (no hard-coded totals).

Run with: python3 workspace/tests/test_pages_ai_sdlc.py
"""
from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
PAGE = REPO / "github-page"
SECTION = PAGE / "app" / "ai-sdlc"
CONTENT = PAGE / "lib" / "ai-sdlc-content.ts"
FACTS = PAGE / "lib" / "ai-sdlc-facts.ts"
HISTORY = PAGE / "lib" / "ai-sdlc-history.json"
HISTORY_GENERATOR = REPO / "workspace" / "ci" / "generate-ai-sdlc-history.py"
GATE_FLOW = PAGE / "components" / "GateFlow.tsx"

SUBPAGES = (
    "lifecycle",
    "guardrails",
    "quality-gates",
    "openspec",
    "agents",
    "governance",
    "lessons",
    "playbook",
    "case-study",
    "framework",
)
DIAGRAMS = (
    "FACTORY_VS_PRODUCT",
    "LIFECYCLE_FLOW",
    "GUARDRAIL_LAYERS",
    "QUALITY_GATE_PIPELINE",
    "ROLES_CHART",
    "OPENSPEC_FLOW",
    "ISSUE_STATE_MACHINE",
    "PR_SEQUENCE",
    "FRAMEWORK_ARCHITECTURE",
    "LEARNING_LOOP",
    "ADOPTION_LEVELS",
)
SRC = re.compile(r'src\(\s*"([^"]+)"\s*\)')
SKILL = re.compile(r'skill\(\s*"([^"]+)"\s*\)')
AGENT = re.compile(r'agent\(\s*"([^"]+)"\s*\)')
WORKFLOW = re.compile(r'workflow\(\s*"([^"]+)"\s*\)')


def section_sources() -> list[Path]:
    return [CONTENT, FACTS, GATE_FLOW, *sorted(SECTION.rglob("*.tsx"))]


def read_all() -> str:
    return "\n".join(p.read_text(encoding="utf-8") for p in section_sources() if p.is_file())


class AiSdlcSectionStructureTest(unittest.TestCase):
    def test_section_is_a_top_level_route_apart_from_product_docs(self):
        self.assertTrue((SECTION / "page.tsx").is_file(), "missing app/ai-sdlc/page.tsx")
        self.assertTrue((SECTION / "layout.tsx").is_file(), "missing app/ai-sdlc/layout.tsx")
        self.assertFalse((PAGE / "app" / "docs" / "ai-sdlc").exists(), "AI SDLC must not live under /docs/")

    def test_every_subpage_exists(self):
        for name in SUBPAGES:
            self.assertTrue((SECTION / name / "page.tsx").is_file(), f"missing app/ai-sdlc/{name}/page.tsx")

    def test_story_nav_and_docs_tab_link_the_factory(self):
        for path in (PAGE / "components" / "Nav.tsx", PAGE / "components" / "DocsChrome.tsx"):
            self.assertIn("/ai-sdlc/", path.read_text(encoding="utf-8"), path.name)

    def test_content_declares_every_required_diagram_as_mermaid(self):
        text = CONTENT.read_text(encoding="utf-8")
        for name in DIAGRAMS:
            match = re.search(rf"export const {name} = `\s*([\w-]+)", text)
            self.assertIsNotNone(match, f"diagram {name} missing")
            self.assertIn(
                match.group(1),
                {"flowchart", "stateDiagram-v2", "sequenceDiagram", "graph"},
                f"{name} is not a Mermaid diagram",
            )

    def test_every_diagram_is_rendered_by_a_page(self):
        ui = (PAGE / "components" / "FactoryUI.tsx").read_text(encoding="utf-8")
        self.assertIn("<MermaidDiagram", ui, "the section's Diagram must render through MermaidDiagram")
        pages = "\n".join(p.read_text(encoding="utf-8") for p in SECTION.rglob("page.tsx"))
        for name in DIAGRAMS:
            self.assertRegex(pages, rf"<Diagram chart=\{{{name}\}}", f"diagram {name} is declared but no page renders it")


class AiSdlcSectionGroundingTest(unittest.TestCase):
    def test_every_cited_path_exists(self):
        cited = sorted(set(SRC.findall(read_all())))
        self.assertGreater(len(cited), 30, "the section should cite its sources")
        missing = [c for c in cited if not (REPO / c.rstrip("/")).exists()]
        self.assertEqual([], missing, "cited paths that do not exist")

    def test_every_named_skill_agent_and_workflow_exists(self):
        text = read_all()
        missing = [s for s in SKILL.findall(text) if not (REPO / ".claude" / "skills" / s / "SKILL.md").is_file()]
        missing += [a for a in AGENT.findall(text) if not (REPO / ".claude" / "agents" / f"{a}.md").is_file()]
        missing += [w for w in WORKFLOW.findall(text) if not (REPO / ".github" / "workflows" / w).is_file()]
        self.assertEqual([], missing)

    def test_counts_are_read_from_the_repository_at_build_time(self):
        facts = FACTS.read_text(encoding="utf-8")
        self.assertIn('from "node:fs"', facts)
        for root in (".claude/skills", ".claude/agents", ".github/workflows", "docs/openspec/changes/archive"):
            self.assertIn(root, facts, f"facts must count {root} from the repo")


class AiSdlcCaseStudyTest(unittest.TestCase):
    """#1958: the case study and the reusable framework, grounded in data and the repository."""

    def test_history_snapshot_records_its_provenance(self):
        import json

        data = json.loads(HISTORY.read_text(encoding="utf-8"))
        self.assertIn("generated_at", data)
        self.assertIn("generate-ai-sdlc-history.py", data["generated_by"])
        self.assertTrue(HISTORY_GENERATOR.is_file())
        months = data["months"]
        self.assertGreaterEqual(len(months), 6)
        for row in months:
            for key in ("month", "commits", "merged_prs", "process_prs", "bug_issues"):
                self.assertIn(key, row)

    def test_case_study_reads_history_from_the_snapshot(self):
        page = (SECTION / "case-study" / "page.tsx").read_text(encoding="utf-8")
        self.assertIn("ai-sdlc-history.json", read_all() + page)
        self.assertIn("issue(", page, "the page must link the issues it cites")
        chapters = CONTENT.read_text(encoding="utf-8").split("export const CASE_CHAPTERS", 1)[1].split("\n];", 1)[0]
        self.assertGreaterEqual(len(re.findall(r"refs: \[\d+", chapters)), 6, "chapters must cite real issues")

    def test_framework_page_cites_the_kit_and_the_report(self):
        page = (SECTION / "framework" / "page.tsx").read_text(encoding="utf-8")
        self.assertIn('src("framework/ai-sdlc/")', page)
        self.assertIn('src("docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md")', page)

    def test_gate_flow_animation_respects_reduced_motion(self):
        text = GATE_FLOW.read_text(encoding="utf-8")
        self.assertIn("prefers-reduced-motion", text)
        self.assertIn('role="img"', text)


if __name__ == "__main__":
    unittest.main()
