#!/usr/bin/env python3
"""GESTION.md prose is English. Filename, official CU titles, and stored labels stay."""

import unittest
from pathlib import Path

GESTION = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "203-design"
    / "GESTION.md"
)


class GestionDesignEnglishTest(unittest.TestCase):
    def test_filename_and_official_titles_stay(self):
        self.assertTrue(GESTION.is_file(), GESTION)
        text = GESTION.read_text(encoding="utf-8")
        for title in (
            "CU01 Preparar presupuesto",
            "CU02 Iniciar gestión",
            "CU04 Recibir documentación",
            "CU10 Movimiento de entidad externa",
            "CU05 Preparar escritura",
            "CU06 Firmar escritura",
            "CU07 Generar testimonio",
            "CU11 Ingresar a inscripción",
            "CU12 Retirar testimonio",
            "CU82 Minuta de inscripción",
            "CU15 Procesar pago",
            "CU16 Archivar",
            "CU83 Definir workflow",
            "escribano[Escribano]",
        ):
            self.assertIn(title, text, title)

    def test_prose_is_english_and_names_the_live_contract(self):
        text = GESTION.read_text(encoding="utf-8")
        for live in (
            "# Case process — analysis and design",
            "gestor[Clerk / Receptionist]",
            "participant API as Managements",
            "participant Esc as Deeds",
            "participant Tes as Testimony",
            "participant Mov as Movement",
            "participant API as Registration drafts",
            "POST /api/v1/managements/{id}/archive?confirmado=",
            "`Sucesion`",
            "`Poder`",
            "Se asignan trámites y comienza la gestión",
            "Toda la documentación requerida fue presentada",
            "Se redacta la escritura",
            "Las partes firman la escritura",
            "La gestión se archiva sin escritura",
            "Testimony generated after deed signing",
            "Testimony submitted for inscription",
            "Testimony withdrawn after inscription cycle",
            "Iniciada",
            "En Tramite",
            "Documentacion Completa",
            "Gestion con Escritura Sin Firmar",
            "Gestion con Escritura Firmada",
            "Testimonio Generado",
            "Testimonio Ingresado a Inscripcion",
            "Testimonio Retirado",
            "Archivada",
            "Espera",
            "Anulada",
            "No Paso",
            "Inscripta",
            "API->>DB: Generada",
            "API->>DB: Presentado para inscripción",
            "API->>DB: Observado",
            "API->>DB: Inscripto",
            "deeds.status",
        ):
            self.assertIn(live, text, live)
        for retired in (
            "El trámite notarial",
            "análisis y diseño del proceso central",
            "Casos de uso del proceso",
            "Gestor / Recepcionista",
            "as Gestiones",
            "as Escrituras",
            "as Minutas",
            "confirmed=",
            "Sucesión",
            "API->>DB: Presentada",
            "API->>DB: Observada",
            "API->>DB: Inscripta",
            "Dónde está el resto",
            "Saldo pendiente",
        ):
            self.assertNotIn(retired, text, retired)


if __name__ == "__main__":
    unittest.main()
