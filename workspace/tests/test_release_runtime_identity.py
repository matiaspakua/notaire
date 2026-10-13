#!/usr/bin/env python3
"""Release runtime identity wiring (#1591)."""

from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]


class RuntimeIdentityWiringTest(unittest.TestCase):
    def test_backend_dockerfile_does_not_copy_release_record(self):
        dockerfile = (REPO_ROOT / "backend-api" / "Dockerfile").read_text(encoding="utf-8")
        self.assertNotIn("release/", dockerfile)
        self.assertNotIn("product-release.json", dockerfile)

    def test_application_properties_filters_app_version(self):
        properties = (
            REPO_ROOT / "backend-api" / "src" / "main" / "resources" / "application.properties"
        ).read_text(encoding="utf-8")
        self.assertIn("app.version=@project.version@", properties)
        self.assertIn(
            "management.opentelemetry.resource-attributes.service.version=${app.version}",
            properties,
        )

    def test_logback_declares_service_version(self):
        logback = (REPO_ROOT / "backend-api" / "src" / "main" / "resources" / "logback-spring.xml").read_text(
            encoding="utf-8"
        )
        self.assertIn('source="app.version"', logback)
        self.assertIn("serviceVersion", logback)


class ImageIdentityTest(unittest.TestCase):
    def setUp(self):
        with open(REPO_ROOT / ".release-please-manifest.json", encoding="utf-8") as handle:
            self.version = json.load(handle)["."]

    def test_compose_images_use_product_version(self):
        semver = re.compile(r"^\d+\.\d+\.\d+$")
        for relative in ("docker-compose.yml", "docker-compose.prod.yml"):
            text = (REPO_ROOT / relative).read_text(encoding="utf-8")
            for component in ("backend", "frontend"):
                match = re.search(
                    rf"ghcr\.io/matiaspakua/notaire/{component}:([^\s#]+)",
                    text,
                )
                self.assertIsNotNone(match, relative)
                tag = match.group(1)
                self.assertRegex(tag, semver)
                self.assertEqual(tag, self.version)

    def test_staging_overlay_and_release_please(self):
        overlay = (
            REPO_ROOT / "infra" / "deploy" / "kustomize" / "overlays" / "staging" / "kustomization.yaml"
        ).read_text(encoding="utf-8")
        self.assertIn(f"newTag: {self.version}", overlay)
        self.assertIn("app.kubernetes.io/version", overlay)
        config = json.loads((REPO_ROOT / "release-please-config.json").read_text(encoding="utf-8"))
        paths = {entry["path"] for entry in config["packages"]["."]["extra-files"]}
        for path in (
            "docker-compose.yml",
            "docker-compose.prod.yml",
            "infra/deploy/kustomize/overlays/staging/kustomization.yaml",
        ):
            self.assertIn(path, paths)


if __name__ == "__main__":
    unittest.main()
