#!/usr/bin/env python3
"""Guards #1977: the interactive diagrams are accessible, motion-safe and fed by the repository.

Every diagram lives in github-page/components/diagrams, uses the shared stepper (reader-driven,
no autoplay, reduced motion honoured), and is placed on the page it explains. Counts reach the
diagrams from lib/tech-facts.ts, never typed into the component.

Run with: python3 workspace/tests/test_pages_diagrams.py
"""
from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
PAGE = REPO / "github-page"
DIAGRAMS = PAGE / "components" / "diagrams"
COMPONENTS = ("FlowDiagram", "HexagonMap", "TestPyramid", "FlywayTimeline", "PipelineMap")
PLACEMENT = {
    "app/docs/hexagonal/page.tsx": ("<HexagonMap", "requestFlow()"),
    "app/docs/database/page.tsx": ("<FlywayTimeline",),
    "app/docs/api/page.tsx": ("contractFlow(",),
    "app/docs/testing/page.tsx": ("<TestPyramid",),
    "app/docs/stack/page.tsx": ("<PipelineMap", "workflowTriggers()"),
    "app/ai-sdlc/page.tsx": ("factoryFlow()",),
    "app/page.tsx": ("factoryFlow()",),
}


def read(rel: str | Path) -> str:
    return (PAGE / rel if isinstance(rel, str) else rel).read_text(encoding="utf-8")


class DiagramsTest(unittest.TestCase):
    def test_every_diagram_uses_the_shared_stepper(self):
        for name in COMPONENTS:
            text = read(DIAGRAMS / f"{name}.tsx")
            self.assertIn('"use client"', text, name)
            self.assertIn("useStepper(", text, f"{name} steps with the shared hook")
            self.assertIn("<StepControls", text, f"{name} has keyboard controls and a list fallback")
            self.assertIn("transition(s.reduced", text, f"{name} drops motion when reduced")

    def test_stepper_never_autoplays_and_honours_reduced_motion(self):
        text = read(DIAGRAMS / "useStepper.ts")
        self.assertIn("useReducedMotion", text)
        self.assertIn("useState(false)", text, "playing starts false: no autoplay")
        self.assertRegex(text, r"duration:\s*0\b")

    def test_controls_are_keyboard_and_screen_reader_friendly(self):
        text = read(DIAGRAMS / "StepControls.tsx")
        for needle in ('aria-live="polite"', "ArrowRight", "ArrowLeft", 'aria-current={i === s.index ? "step"', "aria-pressed"):
            self.assertIn(needle, text)

    def test_svg_diagrams_have_a_title_and_description(self):
        for name in ("FlowDiagram", "HexagonMap", "TestPyramid", "FlywayTimeline"):
            text = read(DIAGRAMS / f"{name}.tsx")
            self.assertIn('role="img"', text, name)
            self.assertIn("<title", text, name)
            self.assertIn("<desc", text, name)

    def test_diagrams_do_not_type_counts(self):
        for name in ("HexagonMap", "TestPyramid", "PipelineMap"):
            text = read(DIAGRAMS / f"{name}.tsx")
            typed = re.search(
                r"\b\d+ (test classes|test files|requests|specs|bounded contexts|use-case services|in-port|out-port|web adapter|outbound adapter|workflows?)\b",
                text,
            )
            self.assertIsNone(typed, f"{name} hard-codes a count: {typed and typed.group(0)}")
            self.assertIn("facts.", text if name != "PipelineMap" else text.replace("workflows.length", "facts."), name)

    def test_each_page_shows_its_diagram(self):
        for rel, needles in PLACEMENT.items():
            text = read(rel)
            for needle in needles:
                self.assertIn(needle, text, f"{rel} should use {needle}")

    def test_workflow_triggers_are_read_from_the_on_block(self):
        self.assertIn("export function workflowTriggers()", read("lib/tech-facts.ts"))

    def test_flow_data_cites_real_files(self):
        text = read("lib/diagram-flows.ts")
        java = "backend-api/src/main/java/com/licensis/notaire"
        paths = [p.replace("${JAVA}", java) for p in re.findall(r'src\(`?"?([^`")]+)`?"?\)', text)]
        self.assertGreaterEqual(len(paths), 8)
        for rel in paths:
            self.assertTrue((REPO / rel.rstrip("/")).exists(), f"diagram-flows cites missing {rel}")


if __name__ == "__main__":
    unittest.main()
