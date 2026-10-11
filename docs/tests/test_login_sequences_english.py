#!/usr/bin/env python3
"""Login sequence diagrams follow the live users login contract."""

import unittest
from pathlib import Path

SEQUENCES = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "204-diagrams"
    / "Secuencias"
)


class LoginSequencesEnglishTest(unittest.TestCase):
    def test_login_sequences_use_the_live_contract(self):
        files = sorted(SEQUENCES.glob("seq-00*.puml"))
        self.assertEqual(7, len(files))
        combined = "\n".join(path.read_text(encoding="utf-8") for path in files)
        for retired in (
            "/api/v1/usuarios",
            "UsuarioController",
            "UsuarioRepository",
            "valido",
            "contrasenia",
            "Cuenta bloqueada",
        ):
            self.assertNotIn(retired, combined, retired)
        happy = (SEQUENCES / "seq-005-login-happy-path.puml").read_text(encoding="utf-8")
        self.assertIn("UserController", happy)
        self.assertIn("POST /api/v1/users/login", happy)
        self.assertIn("{valid: true, token:", happy)
        locked = (SEQUENCES / "seq-001-login-account-lockout.puml").read_text(encoding="utf-8")
        self.assertIn("Account temporarily locked after too many failed attempts.", locked)
        token = (SEQUENCES / "seq-002-login-token-validation.puml").read_text(encoding="utf-8")
        self.assertIn("GET /api/v1/users", token)


if __name__ == "__main__":
    unittest.main()
