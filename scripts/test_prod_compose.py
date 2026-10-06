#!/usr/bin/env python3
"""
Guards issue #1044 (CU78 + CU75): production docker-compose must exist with
no pgAdmin, reverse-proxy-only host ports, ENVIRONMENT=production, required
secrets via ${VAR:?}, least-privilege backend env, and Flyway baseline off.

Plain stdlib unittest + PyYAML, consistent with infra/tests/test_infra_prometheus_hardening.py.
Run with: python3 scripts/test_prod_compose.py
"""
import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PROD_COMPOSE_FILE = os.path.join(REPO_ROOT, "docker-compose.prod.yml")
DEPLOYMENT_GUIDE = os.path.join(
    REPO_ROOT, "docs", "200-architecture", "209-deployment", "README.md"
)

FORBIDDEN_BACKEND_ENV = (
    "PGADMIN_DEFAULT_PASSWORD",
    "GRAFANA_ADMIN_USER",
    "GRAFANA_ADMIN_PASSWORD",
    "POSTGRES_EXPORTER_USER",
    "POSTGRES_EXPORTER_PASSWORD",
)

REQUIRED_SECRET_VARS = (
    "POSTGRES_PASSWORD",
    "JWT_SECRET",
    "ACTUATOR_PASSWORD",
    "APP_ADMIN_PASSWORD",
)

# Compose fail-if-unset: ${VAR:?message} or ${VAR?message}
REQUIRED_SECRET_PATTERN = re.compile(
    r"\$\{(?P<var>[A-Z0-9_]+):?\?(?P<rest>[^}]*)\}"
)
INSECURE_DEFAULT_PATTERN = re.compile(
    r"\$\{(?P<var>[A-Z0-9_]+):-admin\}"
)


def load_prod_compose():
    with open(PROD_COMPOSE_FILE, encoding="utf-8") as f:
        return yaml.safe_load(f)


def service_ports(service):
    return service.get("ports") or []


def env_map(service):
    env = service.get("environment") or {}
    if isinstance(env, list):
        result = {}
        for item in env:
            if isinstance(item, str) and "=" in item:
                key, value = item.split("=", 1)
                result[key] = value
            elif isinstance(item, str):
                result[item] = None
        return result
    return dict(env)


def collect_string_values(node, out=None):
    if out is None:
        out = []
    if isinstance(node, dict):
        for value in node.values():
            collect_string_values(value, out)
    elif isinstance(node, list):
        for value in node:
            collect_string_values(value, out)
    elif isinstance(node, str):
        out.append(node)
    return out


class ProductionComposeTest(unittest.TestCase):
    """Production compose invariants for issue #1044."""

    def test_prod_compose_file_exists(self):
        self.assertTrue(
            os.path.isfile(PROD_COMPOSE_FILE),
            f"missing production compose file: {PROD_COMPOSE_FILE} (issue #1044)",
        )

    def test_no_pgadmin_service(self):
        compose = load_prod_compose()
        services = compose.get("services") or {}
        self.assertNotIn(
            "pgadmin",
            services,
            "production compose must not define a pgadmin service (issue #1044)",
        )

    def test_postgres_backend_frontend_publish_no_host_ports(self):
        compose = load_prod_compose()
        services = compose["services"]
        for name in ("postgres", "backend", "frontend"):
            self.assertIn(name, services, f"production compose must define service {name}")
            ports = service_ports(services[name])
            self.assertEqual(
                ports,
                [],
                f"{name} must not publish host ports in production compose: {ports}",
            )

    def test_only_reverse_proxy_publishes_host_ports(self):
        compose = load_prod_compose()
        services = compose["services"]
        publishers = {
            name: service_ports(svc)
            for name, svc in services.items()
            if service_ports(svc)
        }
        self.assertTrue(
            publishers,
            "at least one reverse-proxy service must publish host ports",
        )
        for name, ports in publishers.items():
            self.assertTrue(
                "proxy" in name.lower() or "nginx" in name.lower() or "caddy" in name.lower(),
                f"non-proxy service {name} must not publish host ports: {ports}",
            )

    def test_backend_environment_is_production(self):
        compose = load_prod_compose()
        backend_env = env_map(compose["services"]["backend"])
        self.assertEqual(
            backend_env.get("ENVIRONMENT"),
            "production",
            "backend ENVIRONMENT must be production so ProductionCredentialsGuard activates",
        )

    def test_secrets_use_required_unset_syntax_without_admin_defaults(self):
        compose = load_prod_compose()
        raw_values = collect_string_values(compose)
        joined = "\n".join(raw_values)

        insecure = INSECURE_DEFAULT_PATTERN.findall(joined)
        self.assertEqual(
            insecure,
            [],
            f"production compose must not use ${{VAR:-admin}} defaults: {insecure}",
        )

        found_vars = {
            match.group("var")
            for value in raw_values
            for match in REQUIRED_SECRET_PATTERN.finditer(value)
        }
        missing = [var for var in REQUIRED_SECRET_VARS if var not in found_vars]
        self.assertEqual(
            missing,
            [],
            f"production compose must require secrets with ${{VAR:?...}}: missing {missing}",
        )

        backend_env = env_map(compose["services"]["backend"])
        for key in (
            "SPRING_DATASOURCE_USERNAME",
            "SPRING_DATASOURCE_PASSWORD",
            "ACTUATOR_USER",
            "ACTUATOR_PASSWORD",
            "APP_ADMIN_USER",
            "APP_ADMIN_PASSWORD",
            "JWT_SECRET",
        ):
            self.assertIn(key, backend_env, f"backend must wire {key}")
            value = str(backend_env[key])
            self.assertRegex(
                value,
                r"^\$\{[A-Z0-9_]+:?\?.*\}$",
                f"{key} must use fail-if-unset syntax, got: {value}",
            )

    def test_backend_env_is_least_privilege(self):
        compose = load_prod_compose()
        backend_env = env_map(compose["services"]["backend"])
        present = [key for key in FORBIDDEN_BACKEND_ENV if key in backend_env]
        self.assertEqual(
            present,
            [],
            f"backend must not receive unused-service credentials: {present}",
        )

    def test_flyway_baseline_on_migrate_disabled(self):
        compose = load_prod_compose()
        backend_env = env_map(compose["services"]["backend"])
        value = backend_env.get("SPRING_FLYWAY_BASELINE_ON_MIGRATE")
        self.assertTrue(
            value is None or str(value).lower() == "false",
            f"SPRING_FLYWAY_BASELINE_ON_MIGRATE must be absent or false, got: {value}",
        )
        self.assertNotEqual(
            str(value).lower() if value is not None else "",
            "true",
            "SPRING_FLYWAY_BASELINE_ON_MIGRATE must never be true in production",
        )

    def test_deployment_guide_documents_prod_compose(self):
        self.assertTrue(os.path.isfile(DEPLOYMENT_GUIDE), f"missing {DEPLOYMENT_GUIDE}")
        with open(DEPLOYMENT_GUIDE, encoding="utf-8") as f:
            text = f.read()
        required_snippets = (
            "docker-compose.prod.yml",
            "pgAdmin",
            "reverse proxy",
            "baseline",
            "production",
        )
        missing = [snippet for snippet in required_snippets if snippet not in text]
        self.assertEqual(
            missing,
            [],
            f"deployment guide must document production compose posture; missing: {missing}",
        )


if __name__ == "__main__":
    unittest.main()
