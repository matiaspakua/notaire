#!/usr/bin/env python3
"""The data dictionary names the full seeded management-status catalog."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Status name. Stored examples such as `Iniciada`, `En Tramite`, and "
    "`Testimonio Generado` stay. The seeded catalog also keeps "
    "`Documentacion Completa`, `Archivada`, `Gestion Modificada`, "
    "`Gestion con Escritura Firmada`, `Gestion con Escritura Sin Firmar`, "
    "`Gestion con Escritura Anulada`, `Gestion con Escritura No Paso`, "
    "`Gestion con Escritura Inscripta`, `Testimonio Ingresado a Inscripcion`, "
    "and `Testimonio Retirado`."
)


class ManagementStatusCatalogEnglishTest(unittest.TestCase):
    def test_status_name_lists_the_seeded_catalog(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Status name. Stored examples such as `Iniciada`, `En Tramite`, "
            "and `Testimonio Generado` stay. |",
            dictionary,
        )
        self.assertNotIn(
            '"Status name. Stored examples such as `Iniciada`, `En Tramite`, '
            'and `Testimonio Generado` stay."\n',
            entities,
        )


if __name__ == "__main__":
    unittest.main()
