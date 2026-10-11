#!/usr/bin/env python3
"""ADR-023 cites the retired Spanish paths the naming decision compares."""

import unittest
from pathlib import Path

DOCS = Path(__file__).resolve().parents[1]
ADR = DOCS / "200-architecture" / "202-ADR" / "ADR-023-rest-resource-naming.md"
REGISTRY = DOCS / "200-architecture" / "203-design" / "REST-API-ENDPOINT_REGISTRY.md"


class AdrRetiredPathsEnglishTest(unittest.TestCase):
    def test_adr_cites_the_retired_paths(self):
        text = ADR.read_text(encoding="utf-8")
        for retired in (
            "`/folio`",
            "`/inmueble`",
            "`/copia`",
            "`/buscar`",
            "`/firmar`",
            "`/archivar`",
            "`POST /pagos/params`",
            "Prefer `/search` over `/buscar`",
            "`/buscar`→",
            "`PUT /registration-drafts/{id}/submit`",
            "registration-draft create",
        ):
            self.assertIn(retired, text, retired)
        self.assertNotIn("Prefer `/search` over `/search`", text)
        self.assertNotIn("/payments/params", text)

    def test_registry_names_the_removed_params_route(self):
        text = REGISTRY.read_text(encoding="utf-8")
        self.assertIn("POST /api/v1/payments", text)
        self.assertIn("~~`POST /api/v1/pagos/params`~~", text)
        self.assertNotIn("/payments/params", text)


if __name__ == "__main__":
    unittest.main()
