#!/usr/bin/env python3
"""Guards live Owner umbrella tracker after keyword-parse closures of #1197/#1443."""
from __future__ import annotations

import os
import re
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
ADR_024 = os.path.join(
    REPO_ROOT, "docs", "200-architecture", "202-ADR", "ADR-024-repository-topology.md"
)
PLAN = os.path.join(REPO_ROOT, "docs", "300-development", "REPO-SPLIT-PLAN.md")
PAGES_ARCH = os.path.join(
    REPO_ROOT, "github-page", "app", "docs", "architecture", "page.tsx"
)
PAGES_MODULES = os.path.join(REPO_ROOT, "github-page", "app", "docs", "modules", "page.tsx")
PAGES_DOCS_HOME = os.path.join(REPO_ROOT, "github-page", "app", "docs", "page.tsx")
MODULE_OWNERSHIP = os.path.join(
    REPO_ROOT, "docs", "300-development", "MODULE-OWNERSHIP.md"
)
LIVE = "1445"


class Adr024OwnerTrackerTest(unittest.TestCase):
    def test_adr024_points_at_live_owner_tracker(self):
        with open(ADR_024, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn(f"#{LIVE}", text, f"ADR-024 must reference live Owner tracker #{LIVE}")
        self.assertRegex(text, r"(?i)keyword parsing|auto-closed|were closed by")

    def test_repo_split_plan_umbrella_points_at_live_tracker(self):
        with open(PLAN, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn(f"#{LIVE}", text)
        self.assertRegex(text, rf"(?i)live Owner tracker.*#{LIVE}|#{LIVE} \(replaces")

    def test_pages_architecture_surfaces_live_owner_umbrella(self):
        with open(PAGES_ARCH, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("ADR-024-repository-topology.md", text)
        self.assertIn(
            f"#{LIVE}",
            text,
            f"Pages Architecture must name live Owner umbrella #{LIVE} next to ADR-024",
        )

    def test_pages_architecture_lists_owner_decision_pack(self):
        """Public Architecture page names every blocking Owner decision for sync."""
        with open(PAGES_ARCH, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("Owner decisions pending", text)
        for num in (LIVE, "1438", "1226"):
            self.assertIn(
                f"#{num}",
                text,
                f"Pages Architecture must list Owner decision #{num}",
            )
            self.assertIn(
                f"https://github.com/matiaspakua/notaire/issues/{num}",
                text,
                f"Pages Architecture must deep-link issue #{num}",
            )

    def test_pages_architecture_surfaces_phase0_done_and_metrics(self):
        """Owner pack must show Phase 0 done and link the metrics baseline."""
        with open(PAGES_ARCH, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("P0.1–P0.5 Done", text)
        self.assertIn("REPO-METRICS-BASELINE.md", text)
        self.assertIn("6/17", text)

    def test_adr024_records_current_path_scoping_baseline(self):
        with open(ADR_024, encoding="utf-8") as fh:
            text = fh.read()
        self.assertRegex(text, r"6/17|6 of 17")
        self.assertIn("dorny/paths-filter", text)

    def test_pages_architecture_links_enterprise_assessment_reports(self):
        """Enterprise readiness reports must be discoverable from Architecture Pages."""
        with open(PAGES_ARCH, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("ASSESSMENT-2026-10.md", text)
        self.assertIn("DOCUMENTATION-AUDIT-2026-10.md", text)

    def test_module_ownership_topology_parent_is_live_tracker(self):
        """MODULE-OWNERSHIP must not present closed #1197 as the live Owner tracker."""
        with open(MODULE_OWNERSHIP, encoding="utf-8") as fh:
            text = fh.read()
        self.assertRegex(
            text,
            rf"(?i)Topology parent:.*#{LIVE}",
            f"MODULE-OWNERSHIP Topology parent must name live tracker #{LIVE}",
        )
        self.assertIn(
            f"https://github.com/matiaspakua/notaire/issues/{LIVE}",
            text,
        )

    def test_pages_modules_surfaces_live_owner_tracker(self):
        """Modules Pages must name live Owner umbrella for sibling-agent sync."""
        with open(PAGES_MODULES, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn(
            f"#{LIVE}",
            text,
            f"Modules Pages must name live Owner tracker #{LIVE}",
        )
        self.assertIn(
            f"https://github.com/matiaspakua/notaire/issues/{LIVE}",
            text,
        )

    def test_pages_docs_home_module_card_names_live_tracker(self):
        """Docs home module-ownership card must point agents at #1445."""
        with open(PAGES_DOCS_HOME, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn(f"#{LIVE}", text)


if __name__ == "__main__":
    unittest.main()
