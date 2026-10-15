#!/usr/bin/env python3
"""Guards the legacy Sonar vulnerability inventory exporter (#1535 slice 1 / CU78)."""

from __future__ import annotations

import importlib.util
import os
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "infra" / "scripts" / "sonar-legacy-inventory.py"
INVENTORY_DOC = REPO_ROOT / "docs" / "300-development" / "SONAR-LEGACY-VULN-INVENTORY.md"

FIXTURE_ISSUES = {
    "issues": [
        {
            "key": "A1",
            "rule": "java:S2068",
            "severity": "CRITICAL",
            "type": "VULNERABILITY",
            "component": "notaire-backend:src/main/java/Example.java",
            "message": "Hard-coded credentials are security-sensitive.",
            "project": "notaire-backend",
        },
        {
            "key": "A2",
            "rule": "java:S4973",
            "severity": "MAJOR",
            "type": "BUG",
            "component": "notaire-backend:src/main/java/com/licensis/notaire/jpa/FooJpaController.java",
            "message": "Strings and Boxed types should be compared using equals().",
            "project": "notaire-backend",
        },
        {
            "key": "A3",
            "rule": "typescript:S4325",
            "severity": "MINOR",
            "type": "CODE_SMELL",
            "component": "notaire-frontend:src/lib/x.ts",
            "message": "This assertion is unnecessary.",
            "project": "notaire-frontend",
        },
        {
            "key": "A4",
            "rule": "java:S5131",
            "severity": "BLOCKER",
            "type": "VULNERABILITY",
            "component": "notaire-backend:src/main/java/Other.java",
            "message": "Endpoints should not be vulnerable to reflected XSS attacks.",
            "project": "notaire-backend",
        },
    ]
}


def load_module():
    spec = importlib.util.spec_from_file_location("sonar_legacy_inventory", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class SonarLegacyInventoryTest(unittest.TestCase):
    def test_script_exists(self):
        self.assertTrue(SCRIPT.is_file(), "infra/scripts/sonar-legacy-inventory.py")

    def test_inventory_doc_exists(self):
        self.assertTrue(INVENTORY_DOC.is_file(), "SONAR-LEGACY-VULN-INVENTORY.md")

    def test_selects_vulnerability_and_high_severity_rows(self):
        mod = load_module()
        rows = mod.select_inventory_rows(FIXTURE_ISSUES["issues"])
        keys = {row["key"] for row in rows}
        self.assertIn("A1", keys)
        self.assertIn("A4", keys)
        # High-severity BUG included; MINOR smell excluded.
        self.assertIn("A2", keys)
        self.assertNotIn("A3", keys)

    def test_render_markdown_requires_classification_column(self):
        mod = load_module()
        rows = mod.select_inventory_rows(FIXTURE_ISSUES["issues"])
        classified = mod.apply_classifications(
            rows,
            {
                mod.row_id(rows[0]): ("fix", "example"),
            },
        )
        # Fill remaining with defer_hexagon for jpa / empty otherwise for test of blanks.
        for row in classified:
            if not row.get("classification"):
                if "jpa" in row["component"]:
                    row["classification"] = "defer_hexagon"
                    row["rationale"] = "hexagon epic #1693"
                else:
                    row["classification"] = "accepted_risk"
                    row["rationale"] = "placeholder"
        md = mod.render_markdown(classified)
        self.assertIn("| classification |", md)
        self.assertIn("fix", md)
        self.assertIn("defer_hexagon", md)
        self.assertTrue(mod.all_vulnerabilities_classified(classified))

    def test_merge_preserves_existing_classifications(self):
        mod = load_module()
        existing = {
            "notaire-backend|java:S2068|notaire-backend:src/main/java/Example.java": (
                "false_positive",
                "test fixture only",
            )
        }
        rows = mod.select_inventory_rows(FIXTURE_ISSUES["issues"])
        merged = mod.apply_classifications(rows, existing)
        match = next(r for r in merged if r["rule"] == "java:S2068")
        self.assertEqual(match["classification"], "false_positive")
        self.assertEqual(match["rationale"], "test fixture only")

    def test_write_and_parse_roundtrip(self):
        mod = load_module()
        rows = mod.select_inventory_rows(FIXTURE_ISSUES["issues"])
        for row in rows:
            row["classification"] = "accepted_risk"
            row["rationale"] = "roundtrip"
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "inv.md"
            path.write_text(mod.render_markdown(rows), encoding="utf-8")
            parsed = mod.parse_classifications(path.read_text(encoding="utf-8"))
            self.assertGreaterEqual(len(parsed), 1)


if __name__ == "__main__":
    unittest.main()
