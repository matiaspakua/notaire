#!/usr/bin/env python3
"""Use-case diagram structure is English. Official CU titles and filenames stay."""

import unittest
from pathlib import Path

FOLDER = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "204-diagrams"
    / "Casos de Uso"
)


def diagrams():
    return sorted(FOLDER.glob("*.puml"))


class UseCaseDiagramsEnglishTest(unittest.TestCase):
    def test_catalog_titles_and_filenames_stay(self):
        files = diagrams()
        self.assertEqual(18, len(files))
        names = {path.name for path in files}
        self.assertIn("clientes.puml", names)
        self.assertIn("gestiones-escrituras.puml", names)
        clients = (FOLDER / "clientes.puml").read_text(encoding="utf-8")
        self.assertIn('usecase "CU19 - Buscar gestiones de un Cliente"', clients)
        self.assertIn("actor Escribano", clients)
        deeds = (FOLDER / "gestiones-escrituras.puml").read_text(encoding="utf-8")
        self.assertIn('usecase "CU05 - Preparar escritura"', deeds)

    def test_structure_is_english_and_keeps_escribano(self):
        clients = (FOLDER / "clientes.puml").read_text(encoding="utf-8")
        self.assertIn("title Use cases — Clients module", clients)
        self.assertIn('rectangle "Clients module"', clients)
        self.assertIn('actor "Receptionist / Clerk (Gestor)" as ReceptionistClerk', clients)
        actors = (FOLDER / "actores.puml").read_text(encoding="utf-8")
        self.assertIn("title Actors of the Notaire system", actors)
        self.assertIn("actor Person", actors)
        self.assertIn("Client --|> Person", actors)
        self.assertNotIn("Persona", actors)
        self.assertIn("Escribano", actors)
        structural = []
        for path in diagrams():
            for line in path.read_text(encoding="utf-8").splitlines():
                if "usecase " in line:
                    continue
                structural.append(line)
        combined = "\n".join(structural)
        for retired in (
            "Casos de Uso",
            "Módulo",
            "Submódulo",
            " : requiere",
            " : solicita",
            "actor Persona",
            "actor Cliente",
            "actor Administrador",
            "Recepcionista / Gestor",
        ):
            self.assertNotIn(retired, combined, retired)


if __name__ == "__main__":
    unittest.main()
