#!/usr/bin/env python3
"""Guards #1970: one site shell, one design system and a findable IA on GitHub Pages.

The story, the Technical docs and the AI Factory share one header (with an accessible
mobile drawer and search), one footer and one set of design tokens; the site map in
``github-page/lib/site.ts`` drives navigation; and the Pages app is built, linted,
tested and link-checked on every pull request that touches it.

Run with: python3 workspace/tests/test_pages_site_shell.py
"""
from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
PAGE = REPO / "github-page"
SITE = PAGE / "components" / "site"
WORKFLOW = REPO / ".github" / "workflows" / "pages-ci.yml"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


class SiteShellTest(unittest.TestCase):
    def test_root_layout_renders_the_one_header_and_footer(self):
        layout = read(PAGE / "app" / "layout.tsx")
        self.assertIn("<SiteHeader", layout)
        self.assertIn("<SiteFooter", layout)
        self.assertIn('href="#main"', layout, "skip link to the main landmark")

    def test_sections_do_not_ship_their_own_headers(self):
        for name in ("Nav.tsx", "FactoryChrome.tsx", "DocsChrome.tsx", "CustomCursor.tsx"):
            self.assertFalse((PAGE / "components" / name).exists(), f"{name} must be replaced by components/site")
        home = read(PAGE / "app" / "page.tsx")
        self.assertNotIn("<Nav", home)
        self.assertNotIn("<Footer", home)

    def test_mobile_menu_is_an_accessible_dialog(self):
        header = read(SITE / "SiteHeader.tsx")
        for needle in ("aria-expanded", "aria-controls", 'aria-modal="true"', 'role="dialog"'):
            self.assertIn(needle, header)
        self.assertIn("useFocusTrap", header)
        trap = read(SITE / "useFocusTrap.ts")
        for needle in ('"Escape"', '"Tab"', "shiftKey"):
            self.assertIn(needle, trap)

    def test_site_map_has_the_four_top_level_entries(self):
        site = read(PAGE / "lib" / "site.ts")
        for label, href in (("AI Factory", "/ai-sdlc/"), ("Product", "/"), ("Technical docs", "/docs/"), ("Releases", "/docs/releases/")):
            self.assertRegex(site, rf'label: "{label}", href: "{re.escape(href)}"')

    def test_docs_and_factory_share_one_document_shell(self):
        for layout in ("docs", "ai-sdlc"):
            text = read(PAGE / "app" / layout / "layout.tsx")
            self.assertIn("<DocShell", text, f"app/{layout}/layout.tsx")
        shell = read(SITE / "DocShell.tsx")
        for part in ("<Breadcrumbs", "<Sidebar", "<PrevNext", "<ReadingProgress"):
            self.assertIn(part, shell)

    def test_design_tokens_are_defined_once(self):
        css = read(PAGE / "app" / "globals.css")
        self.assertIn("@theme", css)
        for token in ("--color-brand-600", "--color-factory-600", "--color-ink", "--color-surface", "--font-display"):
            self.assertIn(token, css)
        self.assertNotIn("cursor: none", css, "the system cursor stays visible")

    def test_ux_assessment_is_written_and_prioritized(self):
        doc = PAGE / "docs" / "ux-assessment.md"
        self.assertTrue(doc.is_file())
        text = read(doc)
        for heading in ("## P0", "## P1", "## P2", "375", "axe"):
            self.assertIn(heading, text)


class PagesCiTest(unittest.TestCase):
    def test_package_scripts_cover_typecheck_lint_tests_links(self):
        scripts = json.loads(read(PAGE / "package.json"))["scripts"]
        for name in ("typecheck", "lint", "test", "test:smoke", "check:links", "build"):
            self.assertIn(name, scripts)

    def test_pull_requests_touching_pages_run_the_checks(self):
        self.assertTrue(WORKFLOW.is_file(), "missing .github/workflows/pages-ci.yml")
        text = read(WORKFLOW)
        self.assertIn("pull_request", text)
        self.assertIn("github-page/**", text)
        for step in ("npm run typecheck", "npm run lint", "npm test", "npm run build", "npm run check:links", "npm run test:smoke"):
            self.assertIn(step, text)
        self.assertNotIn("pages: write", text, "the PR check never deploys")

    def test_preflight_mirrors_the_pages_checks(self):
        self.assertIn("github-page", read(REPO / "workspace" / "sdlc" / "preflight.sh"))


if __name__ == "__main__":
    unittest.main()
