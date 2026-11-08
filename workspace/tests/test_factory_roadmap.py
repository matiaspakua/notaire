"""FACTORY-ROADMAP.md mirrors the multi-session factory master plan (#2140)."""

from __future__ import annotations

import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
ROADMAP = (
    REPO_ROOT
    / "docs"
    / "300-development"
    / "306-ai-sdlc-framework"
    / "FACTORY-ROADMAP.md"
)
FOUNDATION = ROADMAP.with_name("FACTORY-FOUNDATION.md")


class FactoryRoadmapTest(unittest.TestCase):
    def test_roadmap_exists(self) -> None:
        self.assertTrue(ROADMAP.is_file(), f"missing {ROADMAP.relative_to(REPO_ROOT)}")

    def test_roadmap_mirrors_master_plan_spine(self) -> None:
        text = ROADMAP.read_text(encoding="utf-8")
        lowered = text.lower()
        for needle in (
            "# ai sdlc factory roadmap",
            "vision",
            "architecture",
            "foreman",
            "observability",
            "catalogs",
            "self-improvement",
            "harness",
            "phased backlog",
            "delivery rules",
            "anti-patterns",
            "config-driven",
            "lead-time",
            "opentelemetry",
            "openfeature",
            "openapi",
            "cli",
            "install",
            "plugin",
            "notaire",
            "tdd",
            "kiss",
            "dry",
            "srp",
            "boy scout",
            "#2125",
            "#2134",
            "#2135",
            "#2136",
            "#2137",
            "#2138",
            "#2139",
            "#2140",
            "#2141",
            "#2142",
        ):
            with self.subTest(needle=needle):
                self.assertIn(needle, lowered)

    def test_foundation_points_at_roadmap(self) -> None:
        text = FOUNDATION.read_text(encoding="utf-8")
        self.assertIn("FACTORY-ROADMAP.md", text)


if __name__ == "__main__":
    unittest.main()
