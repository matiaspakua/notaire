#!/usr/bin/env python3
"""
Guards issue #1186 (CU76): the dev compose stack takes container names and host
ports from overridable variables whose defaults equal the historical values.

Plain stdlib unittest + PyYAML, consistent with scripts/test_prod_compose.py.
Run with: python3 scripts/test_dev_stack_isolation.py
"""
import json
import os
import re
import shutil
import subprocess
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[1]
COMPOSE_FILE = REPO_ROOT / "docker-compose.yml"
START_SCRIPT = REPO_ROOT / "workspace" / "stack" / "start.sh"
ENV_EXAMPLE = REPO_ROOT / ".env.example"
DEPLOYMENT_GUIDE = REPO_ROOT / "docs" / "200-architecture" / "209-deployment" / "README.md"

# service -> (name variable, default name, port variable, default host port, container port)
SERVICES = {
    "postgres": ("NOTAIRE_POSTGRES_CONTAINER_NAME", "notary-postgres", "POSTGRES_PORT", 5432, 5432),
    "backend": ("NOTAIRE_BACKEND_CONTAINER_NAME", "notary-backend", "BACKEND_PORT", 8080, 8080),
    "pgadmin": ("NOTAIRE_PGADMIN_CONTAINER_NAME", "notary-pgadmin", "PGADMIN_PORT", 5050, 80),
    "frontend": ("NOTAIRE_FRONTEND_CONTAINER_NAME", "notaire-frontend", "FRONTEND_PORT", 3000, 3000),
}
OVERRIDE_KEYS = [key for entry in SERVICES.values() for key in (entry[0], entry[2])]
OVERRIDES = {
    "NOTAIRE_POSTGRES_CONTAINER_NAME": "alt-postgres",
    "NOTAIRE_BACKEND_CONTAINER_NAME": "alt-backend",
    "NOTAIRE_PGADMIN_CONTAINER_NAME": "alt-pgadmin",
    "NOTAIRE_FRONTEND_CONTAINER_NAME": "alt-frontend",
    "POSTGRES_PORT": "15432",
    "BACKEND_PORT": "18080",
    "PGADMIN_PORT": "15050",
    "FRONTEND_PORT": "13000",
}
LITERAL_LOCAL_PORT = re.compile(r"localhost:(?:8080|5050|3000|5432)\b")


def raw_services():
    with open(COMPOSE_FILE, encoding="utf-8") as f:
        return yaml.safe_load(f)["services"]


def rendered_config(overrides):
    env = {k: v for k, v in os.environ.items() if k not in OVERRIDE_KEYS}
    env.update(overrides)
    result = subprocess.run(
        ["docker", "compose", "-f", str(COMPOSE_FILE), "--env-file", str(ENV_EXAMPLE),
         "config", "--format", "json"],
        capture_output=True, text=True, env=env, cwd=REPO_ROOT, check=True,
    )
    return json.loads(result.stdout)["services"]


def published_to_target(service):
    return {(int(p["published"]), int(p["target"])) for p in service.get("ports", [])}


class ComposeParametrisationTest(unittest.TestCase):
    def test_container_names_are_overridable_with_current_default(self):
        services = raw_services()
        for name, (variable, default, *_rest) in SERVICES.items():
            self.assertEqual(
                f"${{{variable}:-{default}}}", services[name]["container_name"], name
            )

    def test_host_ports_are_overridable_with_current_default(self):
        services = raw_services()
        for name, (_n, _d, variable, default, container) in SERVICES.items():
            self.assertEqual(
                [f"${{{variable}:-{default}}}:{container}"], services[name]["ports"], name
            )


@unittest.skipUnless(shutil.which("docker"), "docker not installed")
class RenderedConfigTest(unittest.TestCase):
    def test_defaults_render_the_historical_names_and_ports(self):
        services = rendered_config({})
        for name, (_v, default, _pv, host, container) in SERVICES.items():
            self.assertEqual(default, services[name]["container_name"], name)
            self.assertEqual({(host, container)}, published_to_target(services[name]), name)

    def test_overrides_change_only_host_ports_and_names(self):
        services = rendered_config(OVERRIDES)
        for name, (name_var, _d, port_var, _h, container) in SERVICES.items():
            self.assertEqual(OVERRIDES[name_var], services[name]["container_name"], name)
            self.assertEqual(
                {(int(OVERRIDES[port_var]), container)}, published_to_target(services[name]), name
            )


class StartScriptTest(unittest.TestCase):
    def test_start_script_follows_the_configured_ports(self):
        text = START_SCRIPT.read_text(encoding="utf-8")
        offenders = [
            f"line {n}: {line.strip()}"
            for n, line in enumerate(text.splitlines(), 1)
            if not line.lstrip().startswith("#") and LITERAL_LOCAL_PORT.search(line)
        ]
        self.assertEqual([], offenders, "start.sh still uses literal ports")

    def test_start_script_reads_every_port_variable(self):
        text = START_SCRIPT.read_text(encoding="utf-8")
        for key in ("POSTGRES_PORT", "BACKEND_PORT", "PGADMIN_PORT", "FRONTEND_PORT"):
            self.assertIn(key, text)


class DocumentationTest(unittest.TestCase):
    def test_env_example_lists_every_override_key(self):
        declared = ENV_EXAMPLE.read_text(encoding="utf-8")
        missing = [k for k in OVERRIDE_KEYS if not re.search(rf"^#?\s*{k}=", declared, re.MULTILINE)]
        self.assertEqual([], missing)

    def test_deployment_guide_documents_keys_and_observability_limit(self):
        guide = DEPLOYMENT_GUIDE.read_text(encoding="utf-8")
        missing = [k for k in OVERRIDE_KEYS if k not in guide]
        self.assertEqual([], missing, "deployment guide must list every override key")
        self.assertIn("COMPOSE_PROJECT_NAME", guide)
        self.assertRegex(guide, r"(?is)observability.{0,300}default container names")


if __name__ == "__main__":
    unittest.main()
