#!/usr/bin/env python3
"""Guards #1976: the Technical docs explain the architecture internals, grounded in the repository.

The stack, hexagon, database and API pages must exist and be in the site map; every path they
cite through ``src("…")`` must exist; their versions and counts come from
``github-page/lib/tech-facts.ts`` (read at build time), never typed into the page; and the
permanent technology-stack document exists and is linked from the architecture index.

Run with: python3 workspace/tests/test_pages_tech_docs.py
"""
from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
PAGE = REPO / "github-page"
DOCS_APP = PAGE / "app" / "docs"
PRODUCT_APP = PAGE / "app" / "product"
FACTS = PAGE / "lib" / "tech-facts.ts"
SITE = PAGE / "lib" / "site.ts"
TECH_PAGES = ("stack", "hexagonal", "database", "api")
PRODUCT_PAGES = ("", "story", "platform", "numbers")
STACK_DOC = REPO / "docs" / "200-architecture" / "TECHNOLOGY-STACK.md"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def cited_paths(text: str) -> list[str]:
    return re.findall(r'\bsrc\(\s*"([^"]+)"\s*\)', text)


class TechDocsTest(unittest.TestCase):
    def test_internals_pages_exist_and_are_in_the_site_map(self):
        site = read(SITE)
        for name in TECH_PAGES:
            self.assertTrue((DOCS_APP / name / "page.tsx").is_file(), f"app/docs/{name}/page.tsx")
            self.assertIn(f'href: "/docs/{name}/"', site)
        self.assertIn('label: "Architecture internals"', site)

    def test_every_cited_path_exists(self):
        total = 0
        for name in TECH_PAGES + ("architecture",):
            text = read(DOCS_APP / name / "page.tsx")
            for rel in cited_paths(text):
                total += 1
                self.assertTrue((REPO / rel.rstrip("/")).exists(), f"app/docs/{name} cites missing {rel}")
        self.assertGreaterEqual(total, 20, "the internals pages should cite their sources")

    def test_facts_are_read_from_the_repository(self):
        facts = read(FACTS)
        for needle in ("pom.xml", "package.json", "db/migration", "openapi.yaml", "api-test", "hexagon-baseline.txt", "202-ADR", ".github/workflows"):
            self.assertIn(needle, facts)
        for name in TECH_PAGES:
            text = read(DOCS_APP / name / "page.tsx")
            self.assertIn("@/lib/tech-facts", text, f"app/docs/{name} reads lib/tech-facts")
            for version in ("4.1.2", "16.4.2", "19.3.1", "16.15"):
                self.assertNotIn(version, text, f"app/docs/{name} hard-codes version {version}")
            self.assertIsNone(re.search(r">\s*\d{2,}\s*<", text), f"app/docs/{name} hard-codes a count")

    def test_architecture_page_lists_every_adr_from_the_folder(self):
        text = read(DOCS_APP / "architecture" / "page.tsx")
        self.assertIn("adrs()", text)
        self.assertNotIn("ADR-001 … ADR-027", text)

    def test_stack_document_is_permanent_and_linked(self):
        self.assertTrue(STACK_DOC.is_file())
        doc = read(STACK_DOC)
        for topic in ("Spring Boot", "Flyway", "OpenAPI", "Bruno", "Ports & Adapters", "ArchUnit", "Playwright", "OpenTelemetry"):
            self.assertIn(topic, doc)
        index = read(REPO / "docs" / "200-architecture" / "README.md")
        self.assertIn("TECHNOLOGY-STACK.md", index)


    def test_file_names_read_from_disk_are_encoded_in_links(self):
        # CodeQL js/stored-xss: names read from the repo must not reach an href raw (#1976).
        arch = read(PAGE / "app" / "docs" / "architecture" / "page.tsx")
        stack = read(PAGE / "app" / "docs" / "stack" / "page.tsx")
        self.assertNotIn("202-ADR/${a.file}", arch)
        self.assertIn("encodeURIComponent(a.file)", arch)
        self.assertNotIn("workflows/${w.file}", stack)
        self.assertIn("encodeURIComponent(w.file)", stack)

    def test_compose_image_lookup_escapes_every_regex_metacharacter(self):
        facts = read(PAGE / "lib" / "tech-facts.ts")
        self.assertNotIn('repo.replace(/[/.]/g', facts)
        self.assertIn("escapeRegExp(repo)", facts)


class ArtifactsPageTest(unittest.TestCase):
    """#2067: the Engineering artifacts page tells the artifact chain from repository facts."""

    PAGE_FILE = DOCS_APP / "artifacts" / "page.tsx"
    CONTENT = PAGE / "lib" / "artifacts-content.ts"
    FACTS = PAGE / "lib" / "artifact-facts.ts"

    def test_page_exists_and_is_in_the_site_map(self):
        self.assertTrue(self.PAGE_FILE.is_file())
        self.assertIn('href: "/docs/artifacts/"', read(SITE))

    def test_every_cited_path_exists(self):
        cited = cited_paths(read(self.CONTENT)) + cited_paths(read(self.PAGE_FILE))
        self.assertGreaterEqual(len(cited), 40, "every artifact should link its source")
        for rel in cited:
            self.assertTrue((REPO / rel.rstrip("/")).exists(), f"artifacts page cites missing {rel}")

    def test_counts_come_from_the_facts_reader(self):
        page = read(self.PAGE_FILE)
        self.assertIn("@/lib/artifact-facts", page)
        self.assertIsNone(re.search(r">\s*\d{2,}\s*<", page), "the artifacts page hard-codes a count")
        facts = read(self.FACTS)
        for needle in ("102-use-cases", "requerimientos.csv", "202-ADR", "openspec/specs", "system.stride.yml", "security/cases/catalog.yml"):
            self.assertIn(needle, facts)
        content = read(self.CONTENT)
        self.assertIsNone(re.search(r"\b\d{2,} (use cases|ADRs|requirements|scenarios|migrations)\b", content), "content types a count")


class ProductSectionTest(unittest.TestCase):
    def test_product_has_four_pages_in_the_document_shell(self):
        self.assertIn("<DocShell", read(PRODUCT_APP / "layout.tsx"))
        for name in PRODUCT_PAGES:
            self.assertTrue((PRODUCT_APP / name / "page.tsx").is_file(), f"app/product/{name}/page.tsx")

    def test_home_is_a_landing_that_opens_the_sections(self):
        home = read(PAGE / "app" / "page.tsx")
        for href in ("/product/", "/docs/", "/ai-sdlc/"):
            self.assertIn(f'"{href}"', home)


if __name__ == "__main__":
    unittest.main()
