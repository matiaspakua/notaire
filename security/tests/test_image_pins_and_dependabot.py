#!/usr/bin/env python3
"""
Guards issue #1045 (CU78): container images pinned + Dependabot npm/docker.

Asserts:
- Scoped compose files have no `:latest` / bare `sonarqube:community` /
  major-only postgres floats
- Application Dockerfile FROM lines are minor-or-digest pinned
- CI postgres service images follow the same pin rule
- .github/dependabot.yml keeps npm /frontend, maven, github-actions and adds
  docker for /backend-api and /frontend without duplicates

Plain stdlib unittest + PyYAML, consistent with infra/tests/test_prod_compose.py.
Run with: python3 security/tests/test_image_pins_and_dependabot.py
"""

from __future__ import annotations

import os
import re
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]

INFRA_COMPOSE = REPO_ROOT / "infra" / "observability" / "docker-compose.yml"
TESTING_DB_COMPOSE = REPO_ROOT / "testing" / "database" / "docker-compose.yml"
ROOT_COMPOSE = REPO_ROOT / "docker-compose.yml"
PROD_COMPOSE = REPO_ROOT / "docker-compose.prod.yml"

BACKEND_DOCKERFILE = REPO_ROOT / "backend-api" / "Dockerfile"
BACKEND_DOCKERFILE_SLIM = REPO_ROOT / "backend-api" / "Dockerfile.slim"
FRONTEND_DOCKERFILE = REPO_ROOT / "frontend" / "Dockerfile"

CI_WORKFLOWS = (
    REPO_ROOT / ".github" / "workflows" / "playwright-e2e.yml",
    REPO_ROOT / ".github" / "workflows" / "performance-test.yml",
)
DEPENDABOT = REPO_ROOT / ".github" / "dependabot.yml"

# Image refs that look pinned: digest, or tag with at least major.minor
DIGEST_REF = re.compile(r".+@sha256:[0-9a-f]{64}$", re.IGNORECASE)
# major.minor somewhere in the tag (allows patch / suffixes / alpine)
MINOR_IN_TAG = re.compile(r":[^\s\"]*\d+\.\d+")

# Floating major-only postgres selectors forbidden by AC
MAJOR_ONLY_POSTGRES = re.compile(
    r"^postgres:(?:1[56]|1[56]-alpine)$"
)

FROM_LINE = re.compile(r"^\s*FROM\s+(\S+)", re.MULTILINE)
IMAGE_LINE = re.compile(r"^\s*image:\s*[\"']?([^\s\"'#]+)", re.MULTILINE)


def load_yaml(path: Path) -> dict:
    with path.open(encoding="utf-8") as handle:
        data = yaml.safe_load(handle) or {}
    # PyYAML (YAML 1.1) may parse top-level `on:` as boolean True in workflows.
    if "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


def compose_images(path: Path) -> list[str]:
    text = path.read_text(encoding="utf-8")
    return IMAGE_LINE.findall(text)


def dockerfile_from_images(path: Path) -> list[str]:
    text = path.read_text(encoding="utf-8")
    # Strip stage alias noise already handled by capturing first token
    return [ref.split()[0] for ref in FROM_LINE.findall(text)]


def is_pinned(image_ref: str) -> bool:
    """True if ref uses a digest or a tag containing major.minor."""
    if DIGEST_REF.fullmatch(image_ref):
        return True
    if image_ref.endswith(":latest") or ":latest@" in image_ref:
        return False
    if image_ref == "sonarqube:community":
        return False
    if MAJOR_ONLY_POSTGRES.fullmatch(image_ref):
        return False
    return bool(MINOR_IN_TAG.search(image_ref))


def workflow_postgres_images(path: Path) -> list[str]:
    data = load_yaml(path)
    found: list[str] = []

    def walk(node):
        if isinstance(node, dict):
            services = node.get("services")
            if isinstance(services, dict):
                for svc in services.values():
                    if isinstance(svc, dict) and "image" in svc:
                        image = str(svc["image"])
                        # Pulled through Google's Docker Hub cache (#1380); the pin
                        # rule applies to the Docker Hub reference behind it.
                        image = image.removeprefix("mirror.gcr.io/library/")
                        if image.startswith("postgres:"):
                            found.append(image)
            for value in node.values():
                walk(value)
        elif isinstance(node, list):
            for item in node:
                walk(item)

    walk(data)
    return found


def dependabot_updates() -> list[dict]:
    data = load_yaml(DEPENDABOT)
    updates = data.get("updates") or []
    assert isinstance(updates, list)
    return [u for u in updates if isinstance(u, dict)]


class ImagePinsAndDependabotTest(unittest.TestCase):
    """Image pin + Dependabot ecosystem hygiene for issue #1045 / CU78."""

    def test_pgadmin_is_not_latest(self):
        self.assertTrue(ROOT_COMPOSE.is_file(), ROOT_COMPOSE)
        images = compose_images(ROOT_COMPOSE)
        pgadmin = [img for img in images if img.startswith("dpage/pgadmin4")]
        self.assertTrue(pgadmin, "docker-compose.yml must define a pgadmin image")
        for img in pgadmin:
            self.assertNotEqual(
                img,
                "dpage/pgadmin4:latest",
                "pgadmin must not use :latest (#1045)",
            )
            self.assertTrue(
                is_pinned(img),
                f"pgadmin must be minor-or-digest pinned (#1045); got {img}",
            )

    def test_infra_stack_has_no_latest_tags(self):
        self.assertTrue(INFRA_COMPOSE.is_file(), INFRA_COMPOSE)
        images = compose_images(INFRA_COMPOSE)
        self.assertTrue(images, "infra/observability/docker-compose.yml must declare images")
        latest = [img for img in images if img.endswith(":latest")]
        self.assertEqual(
            latest,
            [],
            f"infra images must not use :latest (#1045); found {latest}",
        )
        unpinned = [img for img in images if not is_pinned(img)]
        self.assertEqual(
            unpinned,
            [],
            f"infra images must be minor-or-digest pinned (#1045); unpinned={unpinned}",
        )

    def test_testing_database_stack_is_pinned(self):
        self.assertTrue(TESTING_DB_COMPOSE.is_file(), TESTING_DB_COMPOSE)
        images = compose_images(TESTING_DB_COMPOSE)
        self.assertTrue(images, "testing/database/docker-compose.yml must declare images")
        unpinned = [img for img in images if not is_pinned(img)]
        self.assertEqual(
            unpinned,
            [],
            f"testing database images must be minor-or-digest pinned (#1045, #1191); unpinned={unpinned}",
        )

    def test_sonarqube_channel_tag_is_versioned(self):
        images = compose_images(INFRA_COMPOSE)
        sonar = [img for img in images if img.startswith("sonarqube:")]
        self.assertTrue(sonar, "infra compose must define a sonarqube image")
        for img in sonar:
            self.assertNotEqual(
                img,
                "sonarqube:community",
                "sonarqube must not use bare community channel (#1045)",
            )
            self.assertTrue(
                is_pinned(img) and "community" in img,
                f"sonarqube must be versioned within community family (#1045); got {img}",
            )

    def test_postgres_images_are_minor_or_digest_pinned(self):
        for path in (ROOT_COMPOSE, PROD_COMPOSE, INFRA_COMPOSE):
            images = [img for img in compose_images(path) if img.startswith("postgres:")]
            self.assertTrue(images, f"{path.name} must declare a postgres image")
            for img in images:
                self.assertFalse(
                    MAJOR_ONLY_POSTGRES.fullmatch(img),
                    f"{path.name}: major-only postgres float forbidden (#1045): {img}",
                )
                self.assertTrue(
                    is_pinned(img),
                    f"{path.name}: postgres must be minor-or-digest pinned (#1045); got {img}",
                )

    def test_backend_dockerfile_bases_are_pinned(self):
        for path in (BACKEND_DOCKERFILE, BACKEND_DOCKERFILE_SLIM):
            self.assertTrue(path.is_file(), path)
            refs = dockerfile_from_images(path)
            self.assertTrue(refs, f"{path} must have FROM lines")
            for ref in refs:
                # ignore scratch / local stage names without registry tag
                if "/" not in ref and ":" not in ref:
                    continue
                if ref.startswith("maven:") or ref.startswith("eclipse-temurin:"):
                    self.assertTrue(
                        is_pinned(ref),
                        f"{path.name}: base must be minor-or-digest pinned (#1045); got {ref}",
                    )
                    # Explicitly reject known floating channel tags (major-only JDK /
                    # Maven selectors without a minor.pin in the tag).
                    self.assertNotEqual(ref, "maven:3.9-eclipse-temurin-21-alpine")
                    self.assertNotEqual(ref, "maven:3-eclipse-temurin-24-alpine")
                    self.assertNotEqual(ref, "maven:3-eclipse-temurin-26-alpine")
                    self.assertNotEqual(ref, "eclipse-temurin:21-jre-alpine")
                    self.assertNotEqual(ref, "eclipse-temurin:24-jre-alpine")
                    self.assertNotEqual(ref, "eclipse-temurin:26-jre-alpine")

    def test_frontend_dockerfile_bases_are_pinned(self):
        self.assertTrue(FRONTEND_DOCKERFILE.is_file(), FRONTEND_DOCKERFILE)
        refs = dockerfile_from_images(FRONTEND_DOCKERFILE)
        node_refs = [ref for ref in refs if ref.startswith("node:")]
        self.assertTrue(node_refs, "frontend Dockerfile must use node base images")
        for ref in node_refs:
            self.assertNotEqual(ref, "node:22-alpine")
            self.assertTrue(
                is_pinned(ref),
                f"frontend node base must be minor-or-digest pinned (#1045); got {ref}",
            )

    def test_ci_postgres_service_images_are_pinned(self):
        found_any = False
        for path in CI_WORKFLOWS:
            self.assertTrue(path.is_file(), path)
            images = workflow_postgres_images(path)
            for img in images:
                found_any = True
                self.assertFalse(
                    MAJOR_ONLY_POSTGRES.fullmatch(img),
                    f"{path.name}: major-only CI postgres forbidden (#1045): {img}",
                )
                self.assertTrue(
                    is_pinned(img),
                    f"{path.name}: CI postgres must be minor-or-digest pinned (#1045); got {img}",
                )
        self.assertTrue(found_any, "expected CI workflows to declare postgres service images")

    def test_npm_ecosystem_targets_frontend_exactly_once(self):
        updates = dependabot_updates()
        npm = [
            u
            for u in updates
            if u.get("package-ecosystem") == "npm"
            and str(u.get("directory", "")).rstrip("/") == "/frontend"
        ]
        self.assertEqual(
            len(npm),
            1,
            "dependabot.yml must contain exactly one npm entry for /frontend (#1045)",
        )

    def test_maven_and_github_actions_ecosystems_remain(self):
        updates = dependabot_updates()
        ecosystems = {(u.get("package-ecosystem"), u.get("directory")) for u in updates}
        self.assertIn(
            ("maven", "/"),
            ecosystems,
            "maven ecosystem at / must remain (#1045)",
        )
        self.assertIn(
            ("github-actions", "/"),
            ecosystems,
            "github-actions ecosystem at / must remain (#1045)",
        )

    def test_docker_ecosystem_for_backend_api(self):
        updates = dependabot_updates()
        docker_backend = [
            u
            for u in updates
            if u.get("package-ecosystem") == "docker"
            and str(u.get("directory", "")).rstrip("/") == "/backend-api"
        ]
        self.assertEqual(
            len(docker_backend),
            1,
            "dependabot.yml must include docker for /backend-api (#1045)",
        )

    def test_docker_ecosystem_for_frontend(self):
        updates = dependabot_updates()
        docker_frontend = [
            u
            for u in updates
            if u.get("package-ecosystem") == "docker"
            and str(u.get("directory", "")).rstrip("/") == "/frontend"
        ]
        self.assertEqual(
            len(docker_frontend),
            1,
            "dependabot.yml must include docker for /frontend (#1045)",
        )

    def test_no_duplicate_docker_directory_entries(self):
        updates = dependabot_updates()
        dirs = [
            str(u.get("directory", "")).rstrip("/")
            for u in updates
            if u.get("package-ecosystem") == "docker"
        ]
        self.assertEqual(
            len(dirs),
            len(set(dirs)),
            f"docker ecosystem directories must be unique (#1045); got {dirs}",
        )


if __name__ == "__main__":
    unittest.main()
