#!/usr/bin/env python3
"""
Guards issue #1255 (CU76): notaire-shared is retired. backend-api owns its DTOs,
no live manifest or tooling references the module, and the former folder lives
only under deprecated/.

Plain stdlib unittest, consistent with workspace/tests/test_repo_hygiene.py.
Run with: python3 workspace/tests/test_notaire_shared_retired.py

Also discoverable via: python3 -m unittest discover -s scripts/tests
(see scripts/tests/test_notaire_shared_retired.py).
"""

from __future__ import annotations

import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
MODULE = "notaire-shared"
POM_NS = {"m": "http://maven.apache.org/POM/4.0.0"}

BACKEND_JAVA = REPO_ROOT / "backend-api" / "src" / "main" / "java" / "com" / "licensis" / "notaire"
BACKEND_SOURCES = REPO_ROOT / "backend-api" / "src"

MOVED_SOURCES = (
    BACKEND_JAVA / "dto" / "DtoPerson.java",
    BACKEND_JAVA / "dto" / "GenericDto.java",
    BACKEND_JAVA / "dto" / "interfaces" / "DtoValido.java",
    BACKEND_JAVA / "dto" / "TypeItem.java",
    BACKEND_JAVA / "dto" / "exceptions" / "DtoInvalidoException.java",
    BACKEND_JAVA / "jpa" / "exceptions" / "PreexistingEntityException.java",
)
DEAD_SOURCE = BACKEND_JAVA / "observability" / "SharedModuleMetrics.java"

LIVE_TOOLING = (
    REPO_ROOT / ".github" / "CODEOWNERS",
    REPO_ROOT / ".github" / "workflows",
    REPO_ROOT / "release-please-config.json",
    REPO_ROOT / "testing" / "scripts",
    REPO_ROOT / ".cursor",
    REPO_ROOT / ".aisdlc",
    REPO_ROOT / ".dockerignore",
    REPO_ROOT / "backend-api" / "Dockerfile",
)
TOOLING_SUFFIXES = {".sh", ".yml", ".yaml", ".json", ""}

DOCS_AS_NON_LIVE = (
    REPO_ROOT / "README.md",
    REPO_ROOT / "AGENTS.md",
    REPO_ROOT / "CLAUDE.md",
    REPO_ROOT / "backend-api" / "README.md",
    REPO_ROOT / "docs" / "300-development" / "301-setup" / "README.md",
    REPO_ROOT / "docs" / "300-development" / "302-code-standards" / "DTO-MAPPING-GUIDE.md",
    REPO_ROOT / "docs" / "200-architecture" / "202-ADR" / "ADR-024-repository-topology.md",
)
NON_LIVE_MARKERS = ("deprecated", "retired", "formerly", "ADR-025")
ADR = REPO_ROOT / "docs" / "200-architecture" / "202-ADR" / "ADR-025-retire-notaire-shared.md"


def _modules(pom: Path) -> list[str]:
    root = ET.parse(pom).getroot()
    return [m.text.strip() for m in root.findall("m:modules/m:module", POM_NS)]


def _artifact_ids(pom: Path) -> list[str]:
    root = ET.parse(pom).getroot()
    return [a.text.strip() for a in root.iterfind(".//m:dependency/m:artifactId", POM_NS)]


def _tooling_files():
    for entry in LIVE_TOOLING:
        if entry.is_file():
            yield entry
            continue
        for path in sorted(entry.rglob("*")):
            if path.is_file() and path.suffix in TOOLING_SUFFIXES and "node_modules" not in path.parts:
                yield path


class NotaireSharedRetiredTest(unittest.TestCase):
    def test_root_reactor_has_one_module(self) -> None:
        """Scenario: Root reactor has one module."""
        self.assertEqual(["backend-api"], _modules(REPO_ROOT / "pom.xml"))

    def test_backend_does_not_depend_on_the_module(self) -> None:
        """Scenario: Backend does not depend on the module."""
        self.assertNotIn(MODULE, _artifact_ids(REPO_ROOT / "backend-api" / "pom.xml"))

    def test_no_live_manifest_or_tooling_references_the_module(self) -> None:
        """Scenario: No live manifest or tooling references the module (also covers Docker)."""
        offenders = [
            str(path.relative_to(REPO_ROOT))
            for path in _tooling_files()
            if MODULE in path.read_text(encoding="utf-8", errors="replace")
        ]
        self.assertEqual([], offenders, f"{MODULE} must not appear in live tooling")

    def test_module_folder_is_gone(self) -> None:
        """Scenario: Module folder is removed; history keeps it behind tag archive-monorepo-pre-split."""
        self.assertFalse((REPO_ROOT / MODULE).exists(), f"{MODULE}/ must not exist at the repo root")
        self.assertFalse((REPO_ROOT / "deprecated").exists(), "deprecated/ was removed (#1261)")

    def test_backend_owns_the_moved_sources(self) -> None:
        """Scenario: DTOs compile from backend-api."""
        missing = [str(p.relative_to(REPO_ROOT)) for p in MOVED_SOURCES if not p.is_file()]
        self.assertEqual([], missing)

    def test_module_observers_are_gone(self) -> None:
        """Scenario: Dead module observers are gone."""
        self.assertFalse(DEAD_SOURCE.exists(), "SharedModuleMetrics must be removed")
        leftovers = [
            str(p.relative_to(REPO_ROOT))
            for p in BACKEND_SOURCES.rglob("*.java")
            if "notaire_shared" in p.read_text(encoding="utf-8", errors="replace")
        ]
        self.assertEqual([], leftovers)

    def test_docs_do_not_present_the_module_as_live(self) -> None:
        """Scenario: External services use the API (module not listed as live)."""
        offenders = []
        for doc in DOCS_AS_NON_LIVE:
            for number, line in enumerate(doc.read_text(encoding="utf-8").splitlines(), start=1):
                if MODULE in line and not any(marker in line for marker in NON_LIVE_MARKERS):
                    offenders.append(f"{doc.relative_to(REPO_ROOT)}:{number}")
        self.assertEqual([], offenders)

    def test_adr_points_external_services_to_the_api(self) -> None:
        """Scenario: External services use the API."""
        self.assertTrue(ADR.is_file(), "ADR-025 must record the decision")
        text = ADR.read_text(encoding="utf-8")
        self.assertIn("OpenAPI", text)
        self.assertIn("/api/v1", text)


if __name__ == "__main__":
    unittest.main()
