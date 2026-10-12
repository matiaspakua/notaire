#!/usr/bin/env python3
"""The data dictionary names the live stored statuses."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)


class DataDictionaryStatusLabelsEnglishTest(unittest.TestCase):
    def test_status_rows_name_the_live_labels(self):
        text = DICTIONARY.read_text(encoding="utf-8")
        self.assertIn(
            "Stored values `Sin Firmar`, `Firmada`, `Anulada`, `No Paso`, and `Inscripta` stay.",
            text,
        )
        self.assertIn(
            "Stored examples such as `Iniciada`, `En Tramite`, and `Testimonio Generado` stay.",
            text,
        )
        self.assertIn(
            "Stored values `Nuevo`, `Utilizado`, `Errose`, `no pasó`, and `Asignado a cuaderno` stay.",
            text,
        )
        self.assertIn("`Honorarios`", text)
        self.assertIn("`Compraventa`", text)
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in (
            "Stored values `Sin Firmar`, `Firmada`, `Anulada`, `No Paso`, and `Inscripta` stay.",
            "Stored examples such as `Iniciada`, `En Tramite`, and `Testimonio Generado` stay.",
            "Stored values `Nuevo`, `Utilizado`, `Errose`, `no pasó`, and `Asignado a cuaderno` stay.",
        ):
            self.assertIn(live, entities, live)
        for retired in (
            "Preparada",
            "Disponible",
            "`Usado`",
            "Listo para firmar",
            "`En proceso`",
            "`Generado`",
        ):
            self.assertNotIn(retired, text, retired)
            self.assertNotIn(retired, entities, retired)


if __name__ == "__main__":
    unittest.main()
