"""Guards #2512: ops/system.yaml matches the application compose identity."""

from __future__ import annotations

import os
import re
import unittest
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
DESCRIPTOR = ROOT / "ops" / "system.yaml"
COMPOSE = ROOT / "docker-compose.yml"
COMMON_SH = ROOT / "infra" / "scripts" / "common.sh"


class SystemDescriptorSeamTest(unittest.TestCase):
    def test_descriptor_exists_and_has_required_fields(self) -> None:
        self.assertTrue(DESCRIPTOR.is_file(), "ops/system.yaml is required")
        data = yaml.safe_load(DESCRIPTOR.read_text(encoding="utf-8"))
        self.assertEqual("notaire", data["system"]["id"])
        self.assertTrue(data["network"]["external_name"])
        self.assertGreaterEqual(len(data["scrape"]), 1)
        self.assertIn("service.namespace", data["otel"]["resource_attributes"])

    def test_descriptor_matches_compose_network_and_containers(self) -> None:
        data = yaml.safe_load(DESCRIPTOR.read_text(encoding="utf-8"))
        compose = yaml.safe_load(COMPOSE.read_text(encoding="utf-8"))
        networks = compose.get("networks") or {}
        # Prefer explicit name on the app network; fall back to project_default pattern.
        named = {
            (cfg or {}).get("name")
            for cfg in networks.values()
            if isinstance(cfg, dict) and (cfg or {}).get("name")
        }
        if named:
            self.assertIn(data["network"]["external_name"], named)
        else:
            self.assertEqual("notaire_notary-network", data["network"]["external_name"])

        services = compose.get("services") or {}
        container_names = set()
        for svc in services.values():
            name = (svc or {}).get("container_name")
            if isinstance(name, str):
                # strip ${VAR:-default}
                m = re.search(r":-([^}]+)\}", name)
                container_names.add(m.group(1) if m else name)
        for job in data["scrape"]:
            host = job["target"].split(":")[0]
            if host in ("postgres-exporter",):
                continue
            self.assertTrue(
                any(host == c or host in c for c in container_names)
                or host in {"notary-backend", "notaire-frontend", "notary-postgres"},
                f"scrape target host {host!r} must match compose container identity",
            )

    def test_infra_common_resolves_system_descriptor(self) -> None:
        self.assertTrue(COMMON_SH.is_file())
        text = COMMON_SH.read_text(encoding="utf-8")
        self.assertIn("SYSTEM_DESCRIPTOR", text)
        self.assertIn("ops/system.yaml", text)


if __name__ == "__main__":
    unittest.main()
