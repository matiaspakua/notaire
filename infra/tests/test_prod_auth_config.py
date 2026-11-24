#!/usr/bin/env python3
"""
Guards #2488 / CU78: every production deployment passes the MFA key, the MFA policy and COOKIE_SECURE.

MFA shipped under #1640 (ADR-032) but only docker-compose.yml wired it. Without the key TOTP enrolment fails, and
without COOKIE_SECURE the httpOnly JWT cookie is sent without Secure.

Run with: python3 infra/tests/test_prod_auth_config.py
"""
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
PROD = ROOT / "docker-compose.prod.yml"
BASE = ROOT / "infra" / "deploy" / "kustomize" / "base"


def prod_backend_env() -> dict:
    env = yaml.safe_load(PROD.read_text(encoding="utf-8"))["services"]["backend"]["environment"]
    return env if isinstance(env, dict) else dict(item.split("=", 1) for item in env)


def kustomize_backend_env() -> dict:
    deployment = yaml.safe_load((BASE / "backend-deployment.yaml").read_text(encoding="utf-8"))
    container = deployment["spec"]["template"]["spec"]["containers"][0]
    return {item["name"]: item for item in container["env"]}


class ProductionComposeAuthTest(unittest.TestCase):
    def test_mfa_key_is_required(self):
        value = str(prod_backend_env().get("NOTAIRE_MFA_ENCRYPTION_KEY", ""))
        self.assertRegex(value, r"^\$\{NOTAIRE_MFA_ENCRYPTION_KEY:\?.+\}$")

    def test_mfa_policy_defaults_to_optional(self):
        self.assertEqual(prod_backend_env().get("NOTAIRE_MFA_POLICY"), "${NOTAIRE_MFA_POLICY:-OPTIONAL}")

    def test_session_cookie_is_secure_by_default(self):
        self.assertEqual(prod_backend_env().get("COOKIE_SECURE"), "${COOKIE_SECURE:-true}")


class KustomizeAuthTest(unittest.TestCase):
    def test_mfa_key_comes_from_the_secret(self):
        ref = kustomize_backend_env()["NOTAIRE_MFA_ENCRYPTION_KEY"]["valueFrom"]["secretKeyRef"]
        self.assertEqual(ref, {"name": "notaire-secrets", "key": "NOTAIRE_MFA_ENCRYPTION_KEY"})
        secret = yaml.safe_load((BASE / "secrets.yaml").read_text(encoding="utf-8"))
        self.assertIn("PLACEHOLDER", secret["stringData"]["NOTAIRE_MFA_ENCRYPTION_KEY"])

    def test_policy_and_secure_cookie_are_set(self):
        env = kustomize_backend_env()
        self.assertEqual(env["NOTAIRE_MFA_POLICY"]["value"], "OPTIONAL")
        self.assertEqual(env["COOKIE_SECURE"]["value"], "true")


if __name__ == "__main__":
    unittest.main()
