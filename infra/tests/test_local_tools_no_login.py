#!/usr/bin/env python3
"""
Guards local Grafana/SonarQube no-login + loopback bind (issue #1413, CU77).
Spec: docs/openspec/changes/chore-1413-local-tools-no-login/.

Run with: python3 infra/tests/test_local_tools_no_login.py
"""
import configparser
import os
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
OBS = REPO_ROOT / "infra" / "observability"
GRAFANA_INI = OBS / "grafana" / "grafana.ini"
OBS_COMPOSE = OBS / "docker-compose.yml"
PROD_COMPOSE = REPO_ROOT / "docker-compose.prod.yml"
KUSTOMIZE = REPO_ROOT / "infra" / "deploy" / "kustomize"


def load_compose():
    with OBS_COMPOSE.open(encoding="utf-8") as handle:
        return yaml.safe_load(handle)


def load_grafana_ini():
    parser = configparser.ConfigParser(strict=False)
    # Grafana ini uses section headers like [auth.anonymous]
    parser.read(GRAFANA_INI)
    return parser


class LocalToolsNoLoginTest(unittest.TestCase):
    def test_grafana_anonymous_admin_without_login_form(self):
        ini = load_grafana_ini()
        self.assertTrue(ini.getboolean("auth.anonymous", "enabled"))
        self.assertEqual("Admin", ini.get("auth.anonymous", "org_role", fallback=ini.get("users", "auto_assign_org_role")))
        # Prefer explicit anonymous org_role; fall back was for older layout.
        if ini.has_option("auth.anonymous", "org_role"):
            self.assertEqual("Admin", ini.get("auth.anonymous", "org_role"))
        self.assertTrue(ini.getboolean("auth", "disable_login_form"))

    def test_sonar_force_authentication_false(self):
        env = load_compose()["services"]["sonarqube"].get("environment") or []
        self.assertIn("SONAR_FORCEAUTHENTICATION=false", env)

    def test_grafana_and_sonar_ports_bind_localhost(self):
        services = load_compose()["services"]
        for name in ("grafana", "sonarqube"):
            ports = [str(p) for p in services[name].get("ports") or []]
            self.assertTrue(ports, f"{name} must publish a port")
            self.assertTrue(
                all(p.startswith("127.0.0.1:") for p in ports),
                f"{name} ports must bind localhost: {ports}",
            )

    def test_prod_and_kustomize_stay_authenticated(self):
        prod = PROD_COMPOSE.read_text(encoding="utf-8") if PROD_COMPOSE.exists() else ""
        self.assertNotIn("SONAR_FORCEAUTHENTICATION=false", prod)
        self.assertNotIn("auth.anonymous", prod)
        self.assertNotIn("127.0.0.1:3001:", prod)
        self.assertNotIn("127.0.0.1:9000:", prod)
        kustomize_text = ""
        if KUSTOMIZE.exists():
            for path in KUSTOMIZE.rglob("*"):
                if path.is_file() and path.suffix in {".yml", ".yaml", ".ini", ".properties"}:
                    kustomize_text += path.read_text(encoding="utf-8", errors="ignore")
        self.assertNotIn("SONAR_FORCEAUTHENTICATION=false", kustomize_text)
        self.assertNotIn("GF_AUTH_ANONYMOUS_ENABLED", kustomize_text)
        self.assertNotIn("[auth.anonymous]", kustomize_text)


if __name__ == "__main__":
    unittest.main()
