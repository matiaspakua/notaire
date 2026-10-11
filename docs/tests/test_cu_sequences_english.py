#!/usr/bin/env python3
"""CU sequence step text is English. Titles, filenames, and stored labels stay."""

import unittest
from pathlib import Path

SEQUENCES = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "204-diagrams"
    / "Secuencias"
)


def cu_files():
    return sorted(
        path for path in SEQUENCES.glob("seq*.puml") if not path.name.startswith("seq-")
    )


class CuSequencesEnglishTest(unittest.TestCase):
    def test_titles_and_filenames_stay(self):
        files = cu_files()
        self.assertEqual(83, len(files))
        names = {path.name for path in files}
        self.assertIn("seq01-ingresar-gestión-de-escritura.puml", names)
        seq01 = (SEQUENCES / "seq01-ingresar-gestión-de-escritura.puml").read_text(
            encoding="utf-8"
        )
        self.assertIn("title CU01 — Ingresar gestión de escritura", seq01)
        self.assertIn('actor "Escribano" as Actor', seq01)
        seq83 = (
            SEQUENCES / "seq83-definir-workflow-de-estados-y-transiciones.puml"
        ).read_text(encoding="utf-8")
        self.assertIn(
            "title CU83 — Definir Workflow de Estados y Transiciones", seq83
        )

    def test_steps_are_english_and_keep_stored_labels(self):
        bodies = []
        for path in cu_files():
            kept = [
                line
                for line in path.read_text(encoding="utf-8").splitlines()
                if not line.startswith("title ")
            ]
            bodies.append("\n".join(kept))
        combined = "\n".join(bodies)
        for live in (
            'participant "Notaire system" as System',
            "The notary (Escribano)",
            "Protocolo Auxiliar",
            '"Asignados a cuaderno"',
            '"Presentado"',
            '"Finalizado / Entregado"',
            "ProcedureType",
            "ManagementStatus",
            'actor "Security officer (Oficial de Seguridad)" as Actor',
        ):
            self.assertIn(live, combined, live)
        for retired in (
            "Sistema Notaire",
            "El Escribano",
            "El Administrador",
            "TipoDeTramite",
            "EstadoDeGestion",
            "gestión de",
            "tipo de trámite",
            "Solicita:",
        ):
            self.assertNotIn(retired, combined, retired)


if __name__ == "__main__":
    unittest.main()
