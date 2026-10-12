#!/usr/bin/env python3
"""The data dictionary names the seeded person identity samples."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LAST = "Last name or legal name. The seeded sample `Garcia` stays."
FIRST = "First name or trade name. The seeded sample `Juan Carlos` stays."
IDENTIFICATION = "Primary identification number. The seeded sample `20123456` stays."
TAX = (
    "Tax id (`CUIT` / `CUIL`). Those stored names stay. "
    "The seeded sample `20-20123456-3` stays."
)
PHONE = "Landline. The seeded sample `011-4555-1234` stays."
MOBILE = "Mobile phone. The seeded sample `011-15-5555-1234` stays."


class PersonIdentityLabelsEnglishTest(unittest.TestCase):
    def test_identity_columns_name_the_seeded_samples(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in (LAST, FIRST, IDENTIFICATION, TAX, PHONE, MOBILE):
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        for retired in (
            "| Last name or legal name |",
            "| First name or trade name |",
            "| Primary identification number |",
            "| Tax id (`CUIT` / `CUIL`). Those stored names stay. |",
            "| Landline |",
            "| Mobile phone |",
        ):
            self.assertNotIn(retired, dictionary, retired)
        for retired in (
            "Text,,,,Last name or legal name\n",
            "Text,,,,First name or trade name\n",
            "Text,,,,Primary identification number\n",
            "Text,,,x,Tax id (`CUIT` / `CUIL`). Those stored names stay.\n",
            "Text,,,x,Landline\n",
            "Text,,,x,Mobile phone\n",
        ):
            self.assertNotIn(retired, entities, retired)


if __name__ == "__main__":
    unittest.main()
