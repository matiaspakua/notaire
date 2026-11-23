#!/usr/bin/env python3
"""Guards #2389: e2e rate-limit env wiring in docker-compose and preflight."""

from __future__ import annotations

import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DOCKER_COMPOSE = REPO_ROOT / "docker-compose.yml"
DOCKER_COMPOSE_PROD = REPO_ROOT / "docker-compose.prod.yml"
APPLICATION_PROPERTIES = REPO_ROOT / "backend-api" / "src" / "main" / "resources" / "application.properties"
PREFLIGHT_SH = REPO_ROOT / "workspace" / "sdlc" / "preflight.sh"


class E2ERateLimitEnvTest(unittest.TestCase):
    """Assert the e2e / preflight path raises the rate limit so Playwright doesn't hit 429s."""

    def setUp(self):
        self.compose = DOCKER_COMPOSE.read_text(encoding="utf-8")
        self.compose_prod = DOCKER_COMPOSE_PROD.read_text(encoding="utf-8")
        self.app_props = APPLICATION_PROPERTIES.read_text(encoding="utf-8")
        self.preflight = PREFLIGHT_SH.read_text(encoding="utf-8")

    # -- docker-compose.yml (local / e2e stack) --

    def test_docker_compose_backend_passes_rate_limit_max_with_default_2000(self):
        """docker-compose.yml backend service must carry NOTAIRE_API_RATE_LIMIT_MAX
        with a default of exactly 2000 (${NOTAIRE_API_RATE_LIMIT_MAX:-2000})."""
        # Find the backend service block and check for the env var
        match = re.search(
            r"NOTAIRE_API_RATE_LIMIT_MAX:\s*\$\{NOTAIRE_API_RATE_LIMIT_MAX:-2000\}",
            self.compose,
        )
        self.assertIsNotNone(
            match,
            "docker-compose.yml backend service should have "
            "'NOTAIRE_API_RATE_LIMIT_MAX: ${NOTAIRE_API_RATE_LIMIT_MAX:-2000}'",
        )

    # -- docker-compose.prod.yml must NOT raise the default --

    def test_docker_compose_prod_does_not_raise_rate_limit(self):
        """docker-compose.prod.yml must not set NOTAIRE_API_RATE_LIMIT_MAX above 2000."""
        match = re.search(
            r"NOTAIRE_API_RATE_LIMIT_MAX",
            self.compose_prod,
        )
        if match:
            # If it's set, the value must not exceed 2000
            block = self.compose_prod
            val_match = re.search(
                r"NOTAIRE_API_RATE_LIMIT_MAX:\s*\$\{NOTAIRE_API_RATE_LIMIT_MAX:-(\d+)\}",
                block,
            )
            if val_match:
                val = int(val_match.group(1))
                self.assertLessEqual(
                    val, 2000,
                    "docker-compose.prod.yml must not set rate limit above 2000",
                )

    def test_application_properties_default_is_2000(self):
        """application.properties default for rate limit must be 2000."""
        match = re.search(
            r"notaire\.api\.rate-limit\.max-requests=\$\{NOTAIRE_API_RATE_LIMIT_MAX:(\d+)\}",
            self.app_props,
        )
        self.assertIsNotNone(match, "application.properties should define rate-limit max-requests")
        val = int(match.group(1))
        self.assertEqual(
            val, 2000,
            "application.properties default rate limit must be 2000",
        )

    # -- preflight.sh must export a high value before starting the stack --

    def test_preflight_exports_high_rate_limit_before_stack_start(self):
        """preflight.sh must export NOTAIRE_API_RATE_LIMIT_MAX >= 20000
        before the workspace/stack/start.sh call in the server-backed suites section."""
        # Find the server-backed suites section and the start.sh call
        start_sh_match = re.search(
            r"workspace/stack/start\.sh",
            self.preflight,
        )
        self.assertIsNotNone(
            start_sh_match,
            "preflight.sh should reference workspace/stack/start.sh",
        )
        start_pos = start_sh_match.start()

        # Look for the export before the start.sh call
        before_start = self.preflight[:start_pos]
        export_match = re.search(
            r"export\s+NOTAIRE_API_RATE_LIMIT_MAX=(\d+)",
            before_start,
        )
        self.assertIsNotNone(
            export_match,
            "preflight.sh should export NOTAIRE_API_RATE_LIMIT_MAX before starting the stack",
        )
        val = int(export_match.group(1))
        self.assertGreaterEqual(
            val, 20000,
            f"preflight.sh NOTAIRE_API_RATE_LIMIT_MAX should be >= 20000 (got {val})",
        )


if __name__ == "__main__":
    unittest.main()
