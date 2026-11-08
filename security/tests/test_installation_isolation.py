#!/usr/bin/env python3
"""Tests for ADR-029 W3 / #1684 installation isolation checklist."""
import os
import subprocess
import sys
import tempfile
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CHECKER = os.path.join(REPO_ROOT, "security", "check_installation_isolation.py")


def run_checker(env: dict | None = None, env_file: str | None = None) -> subprocess.CompletedProcess:
    command = [sys.executable, CHECKER]
    if env_file:
        command.extend(["--env-file", env_file])
    merged = os.environ.copy()
    for key in (
        "JWT_SECRET",
        "APP_ADMIN_PASSWORD",
        "ACTUATOR_USER",
        "ACTUATOR_PASSWORD",
    ):
        merged.pop(key, None)
    if env is not None:
        merged.update(env)
    return subprocess.run(
        command,
        cwd=REPO_ROOT,
        env=merged,
        capture_output=True,
        text=True,
        check=False,
    )


class InstallationIsolationCheckerTest(unittest.TestCase):
    def test_default_jwt_secret_fails(self):
        result = run_checker(
            {
                "JWT_SECRET": "change-this-to-a-random-secret-of-at-least-32-bytes",
                "APP_ADMIN_PASSWORD": "strong-app-admin-password-32chars!!",
                "ACTUATOR_USER": "notaire-metrics",
                "ACTUATOR_PASSWORD": "strong-actuator-password-16",
            }
        )
        self.assertNotEqual(result.returncode, 0, msg=result.stdout + result.stderr)
        self.assertIn("JWT_SECRET", result.stdout + result.stderr)

    def test_missing_jwt_secret_fails(self):
        result = run_checker(
            {
                "JWT_SECRET": "",
                "APP_ADMIN_PASSWORD": "strong-app-admin-password-32chars!!",
                "ACTUATOR_USER": "notaire-metrics",
                "ACTUATOR_PASSWORD": "strong-actuator-password-16",
            }
        )
        self.assertNotEqual(result.returncode, 0)

    def test_default_admin_password_fails(self):
        result = run_checker(
            {
                "JWT_SECRET": "a" * 32,
                "APP_ADMIN_PASSWORD": "admin",
                "ACTUATOR_USER": "notaire-metrics",
                "ACTUATOR_PASSWORD": "strong-actuator-password-16",
            }
        )
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("APP_ADMIN_PASSWORD", result.stdout + result.stderr)

    def test_default_actuator_credentials_fail(self):
        result = run_checker(
            {
                "JWT_SECRET": "a" * 32,
                "APP_ADMIN_PASSWORD": "strong-app-admin-password-32chars!!",
                "ACTUATOR_USER": "admin",
                "ACTUATOR_PASSWORD": "change-me-actuator",
            }
        )
        self.assertNotEqual(result.returncode, 0)
        combined = result.stdout + result.stderr
        self.assertTrue(
            "ACTUATOR_USER" in combined or "ACTUATOR_PASSWORD" in combined,
            combined,
        )

    def test_strong_unique_credentials_pass(self):
        result = run_checker(
            {
                "JWT_SECRET": "installation-specific-jwt-secret-min-32b!!",
                "APP_ADMIN_PASSWORD": "strong-app-admin-password-32chars!!",
                "ACTUATOR_USER": "notaire-metrics",
                "ACTUATOR_PASSWORD": "strong-actuator-password-16",
            }
        )
        self.assertEqual(result.returncode, 0, msg=result.stdout + result.stderr)

    def test_env_file_is_honored(self):
        with tempfile.NamedTemporaryFile("w", delete=False, suffix=".env") as handle:
            handle.write(
                "JWT_SECRET=installation-specific-jwt-secret-min-32b!!\n"
                "APP_ADMIN_PASSWORD=strong-app-admin-password-32chars!!\n"
                "ACTUATOR_USER=notaire-metrics\n"
                "ACTUATOR_PASSWORD=strong-actuator-password-16\n"
            )
            path = handle.name
        try:
            result = run_checker(env_file=path)
            self.assertEqual(result.returncode, 0, msg=result.stdout + result.stderr)
        finally:
            os.unlink(path)


if __name__ == "__main__":
    unittest.main()
