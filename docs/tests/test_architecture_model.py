#!/usr/bin/env python3
"""
Guards #2483 / CU76: the LikeC4 model is the single source of the architecture views (ADR-033).

The Mermaid diagrams in the SAD, the security, deployment and testing docs, and the GitHub Pages data are
generated from docs/200-architecture/210-architecture-model by `npm run generate`. This test needs no Node:
it checks that every generated copy still matches the committed `.mmd` file, so a hand edit of a diagram, or a
model change without regeneration, fails here. `npm run check` in the model folder checks the model side.

Run with: python3 docs/tests/test_architecture_model.py
"""
import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODEL = ROOT / "docs" / "200-architecture" / "210-architecture-model"
GENERATED = MODEL / "generated" / "mermaid"
PAGES_DATA = ROOT / "github-page" / "lib" / "architecture-model.json"
ADR_DIR = ROOT / "docs" / "200-architecture" / "202-ADR"

BLOCK = re.compile(r"<!-- likec4-view: (\S+) -->\n```mermaid\n(.*?)```\n<!-- /likec4-view -->", re.S)

REQUIRED_VIEWS = {
    "docs/200-architecture/201-SAD/sad.md": {
        "index", "containers", "backend", "frontend", "apiRequest", "loginMfa", "dev", "prod", "findings",
    },
    "docs/200-architecture/206-security/README.md": {"security", "securityPipeline", "loginMfa"},
    "docs/200-architecture/209-deployment/README.md": {"dev", "prod", "staging"},
    "docs/200-architecture/207-monitoring/README.md": {"dev"},
    "docs/300-development/303-testing/TEST-PLAN.md": {"testing", "delivery"},
}


def view_ids() -> set[str]:
    text = "\n".join(p.read_text(encoding="utf-8") for p in (MODEL / "src").glob("*.c4"))
    return set(re.findall(r"^\s*(?:dynamic |deployment )?view (\w+)", text, re.M))


class ArchitectureModelTest(unittest.TestCase):
    def test_likec4_is_pinned_to_an_exact_version(self):
        package = json.loads((MODEL / "package.json").read_text(encoding="utf-8"))
        version = package["devDependencies"]["likec4"]
        self.assertRegex(version, r"^\d+\.\d+\.\d+$", "pin likec4 exactly; no ranges")
        lock = json.loads((MODEL / "package-lock.json").read_text(encoding="utf-8"))
        self.assertEqual(lock["packages"]["node_modules/likec4"]["version"], version)

    def test_every_view_has_a_generated_mermaid_file(self):
        ids = view_ids()
        self.assertGreaterEqual(len(ids), 13, ids)
        generated = {p.stem for p in GENERATED.glob("*.mmd")}
        self.assertEqual(generated, ids, "run `npm run generate` in the model folder")

    def test_docs_embed_the_required_views_verbatim(self):
        for relative, required in REQUIRED_VIEWS.items():
            text = (ROOT / relative).read_text(encoding="utf-8")
            blocks = dict(BLOCK.findall(text))
            self.assertTrue(required <= set(blocks), f"{relative} misses {sorted(required - set(blocks))}")
            for view, body in blocks.items():
                expected = (GENERATED / f"{view}.mmd").read_text(encoding="utf-8")
                self.assertEqual(body, expected, f"{relative}: view {view} drifted; run `npm run generate`")

    def test_pages_data_matches_the_generated_views(self):
        data = json.loads(PAGES_DATA.read_text(encoding="utf-8"))
        views = {v["id"]: v for v in data["views"]}
        self.assertEqual(set(views), view_ids())
        for view_id, view in views.items():
            self.assertTrue(view["title"], view_id)
            self.assertEqual(view["mermaid"], (GENERATED / f"{view_id}.mmd").read_text(encoding="utf-8"))

    def test_every_finding_links_an_issue(self):
        data = json.loads(PAGES_DATA.read_text(encoding="utf-8"))
        self.assertGreaterEqual(len(data["findings"]), 5)
        for finding in data["findings"]:
            self.assertRegex(finding["issue"], r"^https://github\.com/matiaspakua/notaire/issues/\d+$", finding)

    def test_the_sad_has_no_plantuml_left(self):
        sad = (ROOT / "docs/200-architecture/201-SAD/sad.md").read_text(encoding="utf-8")
        self.assertNotIn("```plantuml", sad)

    def test_adr_033_records_the_decision_and_amends_adr_027(self):
        adr = next(ADR_DIR.glob("ADR-033-*.md"))
        self.assertIn("LikeC4", adr.read_text(encoding="utf-8"))
        self.assertIn("ADR-033", next(ADR_DIR.glob("ADR-027-*.md")).read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
