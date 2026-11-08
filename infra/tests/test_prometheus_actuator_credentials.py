#!/usr/bin/env python3
"""Guards #1657 (CU78): Prometheus carries no hardcoded actuator credential."""

import os
import re
import subprocess
import tempfile
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
OBSERVABILITY = REPO_ROOT / "infra" / "observability"
HELPER = REPO_ROOT / "infra" / "scripts" / "ensure-actuator-credentials.sh"
SECRET_DIR = "/run/secrets"


def backend_job():
    config = yaml.safe_load((OBSERVABILITY / "prometheus" / "prometheus.yml").read_text(encoding="utf-8"))
    return next(job for job in config["scrape_configs"] if job["job_name"] == "notaire-backend")


def env_value(path, key):
    match = re.search(rf"^{key}=(.*)$", Path(path).read_text(encoding="utf-8"), re.MULTILINE)
    return match.group(1) if match else None


class PrometheusScrapeConfigTest(unittest.TestCase):
    def test_backend_job_reads_credentials_from_files(self):
        auth = backend_job()["basic_auth"]
        self.assertTrue(auth["username_file"].startswith(SECRET_DIR))
        self.assertTrue(auth["password_file"].startswith(SECRET_DIR))

    def test_backend_job_has_no_literal_credential(self):
        auth = backend_job()["basic_auth"]
        self.assertNotIn("password", auth)
        self.assertNotIn("username", auth)


class ComposeSecretsTest(unittest.TestCase):
    def setUp(self):
        self.compose = yaml.safe_load((OBSERVABILITY / "docker-compose.yml").read_text(encoding="utf-8"))

    def test_secrets_come_from_the_environment(self):
        secrets = self.compose["secrets"]
        self.assertEqual(secrets["actuator_user"], {"environment": "ACTUATOR_USER"})
        self.assertEqual(secrets["actuator_password"], {"environment": "ACTUATOR_PASSWORD"})

    def test_prometheus_mounts_both_secrets(self):
        mounted = self.compose["services"]["prometheus"]["secrets"]
        self.assertCountEqual(mounted, ["actuator_user", "actuator_password"])


class ExampleEnvTest(unittest.TestCase):
    def test_templates_ship_a_placeholder(self):
        for template in (REPO_ROOT / ".env.example", REPO_ROOT / "infra" / ".env.example"):
            with self.subTest(template=template.name):
                self.assertTrue(env_value(template, "ACTUATOR_PASSWORD").startswith("change-me"))


class EnsureActuatorCredentialsTest(unittest.TestCase):
    def run_helper(self, content):
        handle, name = tempfile.mkstemp(prefix="env-")
        os.close(handle)
        self.addCleanup(os.remove, name)
        Path(name).write_text(content, encoding="utf-8")
        result = subprocess.run(["bash", str(HELPER), name], capture_output=True, text=True, timeout=30)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        return name

    def test_placeholder_is_replaced_with_a_random_value(self):
        name = self.run_helper("ACTUATOR_USER=prometheus\nACTUATOR_PASSWORD=change-me-actuator\nOTHER=1\n")
        password = env_value(name, "ACTUATOR_PASSWORD")
        self.assertFalse(password.startswith("change-me"))
        self.assertGreaterEqual(len(password), 24)
        self.assertEqual(env_value(name, "OTHER"), "1")

    def test_missing_password_is_added(self):
        name = self.run_helper("OTHER=1\n")
        self.assertGreaterEqual(len(env_value(name, "ACTUATOR_PASSWORD")), 24)

    def test_real_value_is_kept(self):
        content = "ACTUATOR_PASSWORD=already-chosen-by-someone\n"
        name = self.run_helper(content)
        self.assertEqual(Path(name).read_text(encoding="utf-8"), content)

    def test_second_run_keeps_the_generated_value(self):
        name = self.run_helper("ACTUATOR_PASSWORD=change-me-actuator\n")
        first = env_value(name, "ACTUATOR_PASSWORD")
        subprocess.run(["bash", str(HELPER), name], check=True, timeout=30)
        self.assertEqual(env_value(name, "ACTUATOR_PASSWORD"), first)


if __name__ == "__main__":
    unittest.main()
