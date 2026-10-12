#!/usr/bin/env python3
"""The data dictionary names the remaining seeded person and login values."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
BIRTH = "Birth date. The seeded sample `1980-01-15` stays."
MARRIAGES = "Marriage count. The seeded sample `1` stays."
REGISTRATION = "Notary registration number. The seeded sample `1001` stays."
CLIENT = (
    "`true` when the person is a client of the practice. The seeded sample stays `false`."
)
NOTARY = (
    "`true` when the person is a licensed notary. The seeded sample stays `true`."
)
USERNAME = "Username (unique login). The seeded sample `admin` stays."
PASSWORD = "Password hash. The seeded account stores a hash of `admin`."
ACTIVE = "`true` when the account is active. The seeded sample stays `true`."
CONCEPT = (
    "Whether the concept can be used on budgets. Seeded flags stay: `true` for "
    "`IVA`, `Honorarios`, `Documentacion`, and `Protocolo`."
)
DOCUMENT = (
    "`true` when it can be used on templates. Seeded flags stay: `true` for "
    "`Libre Deuda Municipal`, `Informe de Dominio`, `Certificado Catastral`, and "
    "`Fotocopia DNI`."
)
LIVE = (
    BIRTH,
    MARRIAGES,
    REGISTRATION,
    CLIENT,
    NOTARY,
    USERNAME,
    PASSWORD,
    ACTIVE,
    CONCEPT,
    DOCUMENT,
)


class PersonAccountSeedLabelsEnglishTest(unittest.TestCase):
    def test_person_and_login_seeds_are_named(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in LIVE:
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| Birth date |", dictionary)
        self.assertNotIn("| Marriage count |", dictionary)
        self.assertNotIn("| Notary registration number |", dictionary)
        self.assertNotIn(
            "| `true` when the person is a client of the practice |",
            dictionary,
        )
        self.assertNotIn(
            "| `true` when the person is a licensed notary |",
            dictionary,
        )
        self.assertNotIn("| Username (unique login) |", dictionary)
        self.assertNotIn("| Password hash |", dictionary)
        self.assertNotIn("| `true` when the account is active |", dictionary)
        self.assertNotIn("| Whether the concept can be used on budgets |", dictionary)
        self.assertNotIn("| `true` when it can be used on templates |", dictionary)
        self.assertNotIn("21232f297a57a5a743894a0e4a801fc3", dictionary)
        self.assertNotIn("21232f297a57a5a743894a0e4a801fc3", entities)
        self.assertNotIn("Date,,,x,Birth date\n", entities)
        self.assertNotIn("Integer,,,x,Marriage count\n", entities)
        self.assertNotIn("Integer,,,x,Notary registration number\n", entities)
        self.assertNotIn(
            "Boolean,,,,`true` when the person is a client of the practice\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,`true` when the person is a licensed notary\n",
            entities,
        )
        self.assertNotIn("Text,,,,Username (unique login)\n", entities)
        self.assertNotIn("Text,,,,Password hash\n", entities)
        self.assertNotIn(
            "Boolean,,,,`true` when the account is active\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,Whether the concept can be used on budgets\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,`true` when it can be used on templates\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
