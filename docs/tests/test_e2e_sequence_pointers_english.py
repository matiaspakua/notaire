#!/usr/bin/env python3
"""Playwright headers cite the sequence file that exists."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SEQ = ROOT / "docs" / "200-architecture" / "204-diagrams" / "Secuencias"

# The header shorthand CUxx.puml is the sequence whose title starts with that
# number. Official titles on those diagrams stay.
CASES = (
    (
        "testing/e2e/tests/TS-0010-budget-workflow.spec.ts",
        "seq01-ingresar-gestión-de-escritura.puml",
        "CU01.puml",
        "CU01 — Ingresar gestión de escritura",
        "CU01 - Prepare a budget",
    ),
    (
        "testing/e2e/tests/TS-0011-managements-crud-workflow.spec.ts",
        "seq02-ingresar-gestión-de-trámite.puml",
        "CU02.puml",
        "CU02 — Ingresar gestión de trámite",
        "CU02 - Start a management",
    ),
    (
        "testing/e2e/tests/TS-0013-deeds-signing-workflow.spec.ts",
        "seq05-preparar-escritura.puml",
        "CU05.puml",
        "CU05 — Preparar escritura",
        "CU05 - Prepare a deed",
    ),
    (
        "testing/e2e/tests/TS-0012-documentation-testimony-workflow.spec.ts",
        "seq07-inscribir-escritura.puml",
        "CU07.puml",
        "CU07 — Inscribir escritura",
        "CU07 - Generate a testimony",
    ),
    (
        "testing/e2e/tests/TS-0015-people-clients-workflow.spec.ts",
        "seq17-buscar-gestiones-de-un-cliente.puml",
        "CU17.puml",
        "CU17 — Buscar gestiones de un cliente",
        "CU17 - Register a person",
    ),
)


class E2eSequencePointersEnglishTest(unittest.TestCase):
    def test_headers_cite_the_live_sequence_files(self):
        for spec, filename, retired, title, header in CASES:
            text = (ROOT / spec).read_text(encoding="utf-8")
            diagram = (SEQ / filename).read_text(encoding="utf-8")
            with self.subTest(spec=spec):
                self.assertIn(filename, text)
                self.assertNotIn(retired, text)
                self.assertIn(header, text)
                self.assertIn(title, diagram)
                self.assertIn('participant "Notaire system"', diagram)
                self.assertIn('actor "Escribano"', diagram)


if __name__ == "__main__":
    unittest.main()
