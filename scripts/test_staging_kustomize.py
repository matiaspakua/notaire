#!/usr/bin/env python3
"""
Guards issue #901 (CU77): staging Kustomize manifests mirroring #1044's
four-service topology (postgres, backend, frontend, reverse-proxy).

Asserts:
- base + staging overlay exist and `kustomize build` succeeds
- four Deployments + Services rendered
- postgres/backend/frontend are ClusterIP (internal-only)
- reverse-proxy is the sole external Service (NodePort/LoadBalancer)
- secrets are placeholders / secretKeyRef only (no real credentials)
- backend ENVIRONMENT=production; Flyway baseline-on-migrate off
- deployment docs mention the Kustomize path

Requires `kustomize` on PATH (or KUSTOMIZE=/path/to/kustomize).
Run with: python3 scripts/test_staging_kustomize.py
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KUSTOMIZE_ROOT = os.path.join(REPO_ROOT, "infra", "deploy", "kustomize")
BASE_DIR = os.path.join(KUSTOMIZE_ROOT, "base")
STAGING_DIR = os.path.join(KUSTOMIZE_ROOT, "overlays", "staging")
DEPLOYMENT_GUIDE = os.path.join(
    REPO_ROOT, "docs", "200-architecture", "209-deployment", "README.md"
)
DEPLOYMENT_PLAN = os.path.join(
    REPO_ROOT, "docs", "300-development", "DEPLOYMENT-PLAN.md"
)

REQUIRED_APPS = ("postgres", "backend", "frontend", "reverse-proxy")
INTERNAL_APPS = ("postgres", "backend", "frontend")

# Values that must never appear as committed secret material.
FORBIDDEN_SECRET_VALUES = (
    "admin",
    "password",
    "changeme",
    "secret123",
    "notaire",
)

PLACEHOLDER_HINTS = (
    "PLACEHOLDER",
    "REPLACE",
    "SET_AT_APPLY",
    "CHANGE_ME",
    "<",
)


def find_kustomize() -> str:
    env = os.environ.get("KUSTOMIZE")
    if env and os.path.isfile(env) and os.access(env, os.X_OK):
        return env
    which = shutil.which("kustomize")
    if which:
        return which
    for candidate in ("/tmp/kustomize", os.path.expanduser("~/bin/kustomize")):
        if os.path.isfile(candidate) and os.access(candidate, os.X_OK):
            return candidate
    raise FileNotFoundError(
        "kustomize binary not found on PATH (install kustomize v5+ or set KUSTOMIZE=)"
    )


def kustomize_build(path: str) -> list:
    try:
        binary = find_kustomize()
    except FileNotFoundError as error:
        raise unittest.SkipTest(str(error)) from error
    result = subprocess.run(
        [binary, "build", path],
        check=False,
        capture_output=True,
        text=True,
        cwd=REPO_ROOT,
    )
    if result.returncode != 0:
        raise AssertionError(
            f"kustomize build {path} failed ({result.returncode}):\n"
            f"{result.stderr or result.stdout}"
        )
    docs = list(yaml.safe_load_all(result.stdout))
    return [doc for doc in docs if doc]


def app_label(resource: dict) -> str | None:
    meta = resource.get("metadata") or {}
    labels = meta.get("labels") or {}
    return labels.get("app") or labels.get("app.kubernetes.io/name")


def resources_by_kind(docs: list, kind: str) -> list:
    return [doc for doc in docs if doc.get("kind") == kind]


def container_env_map(deployment: dict) -> dict:
    specs = ((deployment.get("spec") or {}).get("template") or {}).get("spec") or {}
    containers = specs.get("containers") or []
    if not containers:
        return {}
    env_list = containers[0].get("env") or []
    result = {}
    for item in env_list:
        name = item.get("name")
        if not name:
            continue
        if "value" in item:
            result[name] = item.get("value")
        elif "valueFrom" in item:
            result[name] = item.get("valueFrom")
    return result


def collect_strings(node, out=None):
    if out is None:
        out = []
    if isinstance(node, dict):
        for value in node.values():
            collect_strings(value, out)
    elif isinstance(node, list):
        for value in node:
            collect_strings(value, out)
    elif isinstance(node, str):
        out.append(node)
    return out


class StagingKustomizeTest(unittest.TestCase):
    """Staging Kustomize invariants for issue #901."""

    def test_base_and_staging_overlay_dirs_exist(self):
        self.assertTrue(
            os.path.isdir(BASE_DIR),
            f"missing Kustomize base directory: {BASE_DIR} (issue #901)",
        )
        self.assertTrue(
            os.path.isfile(os.path.join(BASE_DIR, "kustomization.yaml")),
            f"missing {BASE_DIR}/kustomization.yaml",
        )
        self.assertTrue(
            os.path.isdir(STAGING_DIR),
            f"missing staging overlay directory: {STAGING_DIR} (issue #901)",
        )
        self.assertTrue(
            os.path.isfile(os.path.join(STAGING_DIR, "kustomization.yaml")),
            f"missing {STAGING_DIR}/kustomization.yaml",
        )

    def test_base_renders_four_app_services(self):
        docs = kustomize_build(BASE_DIR)
        deployments = {app_label(d): d for d in resources_by_kind(docs, "Deployment")}
        services = {app_label(s): s for s in resources_by_kind(docs, "Service")}
        for name in REQUIRED_APPS:
            self.assertIn(
                name,
                deployments,
                f"base must render Deployment for {name}; got {sorted(deployments)}",
            )
            self.assertIn(
                name,
                services,
                f"base must render Service for {name}; got {sorted(services)}",
            )

    def test_staging_overlay_same_service_set(self):
        docs = kustomize_build(STAGING_DIR)
        deployments = {app_label(d): d for d in resources_by_kind(docs, "Deployment")}
        services = {app_label(s): s for s in resources_by_kind(docs, "Service")}
        for name in REQUIRED_APPS:
            self.assertIn(name, deployments)
            self.assertIn(name, services)
        # No extra application Deployments beyond the #1044 four-service set.
        extra = sorted(set(deployments) - set(REQUIRED_APPS) - {None})
        self.assertEqual(
            extra,
            [],
            f"staging overlay must not add app Deployments beyond {REQUIRED_APPS}: {extra}",
        )

    def test_postgres_backend_frontend_are_clusterip(self):
        docs = kustomize_build(STAGING_DIR)
        for name in INTERNAL_APPS:
            matches = [
                s
                for s in resources_by_kind(docs, "Service")
                if app_label(s) == name
            ]
            self.assertTrue(matches, f"missing Service for {name}")
            service = matches[0]
            service_type = (service.get("spec") or {}).get("type", "ClusterIP")
            self.assertEqual(
                service_type,
                "ClusterIP",
                f"{name} Service must be ClusterIP (internal-only), got {service_type}",
            )

    def test_only_reverse_proxy_is_external(self):
        docs = kustomize_build(STAGING_DIR)
        external = []
        for service in resources_by_kind(docs, "Service"):
            service_type = (service.get("spec") or {}).get("type", "ClusterIP")
            if service_type in ("NodePort", "LoadBalancer"):
                external.append((app_label(service), service_type))
        self.assertTrue(
            external,
            "reverse-proxy must expose a NodePort or LoadBalancer Service",
        )
        for name, service_type in external:
            self.assertEqual(
                name,
                "reverse-proxy",
                f"only reverse-proxy may be external; got {name} as {service_type}",
            )

    def test_no_committed_real_credentials(self):
        docs = kustomize_build(STAGING_DIR)
        secrets = resources_by_kind(docs, "Secret")
        self.assertTrue(secrets, "manifests must include a Secret placeholder resource")

        for secret in secrets:
            data = secret.get("stringData") or {}
            # binaryData / data should not hold opaque real secrets either
            encoded = secret.get("data") or {}
            self.assertFalse(
                encoded,
                "Secret must use stringData placeholders, not opaque data blobs",
            )
            for key, value in data.items():
                text = str(value)
                upper = text.upper()
                self.assertTrue(
                    any(hint in upper for hint in PLACEHOLDER_HINTS),
                    f"Secret.{key} must be an explicit placeholder, got: {text!r}",
                )
                lowered = text.lower()
                for forbidden in FORBIDDEN_SECRET_VALUES:
                    self.assertNotEqual(
                        lowered,
                        forbidden,
                        f"Secret.{key} must not use insecure value {forbidden!r}",
                    )

        # Deployments must wire credentials via secretKeyRef, not literal env values
        # for known secret keys.
        secret_env_keys = {
            "POSTGRES_PASSWORD",
            "JWT_SECRET",
            "ACTUATOR_PASSWORD",
            "APP_ADMIN_PASSWORD",
            "SPRING_DATASOURCE_PASSWORD",
        }
        for deployment in resources_by_kind(docs, "Deployment"):
            env = container_env_map(deployment)
            for key in secret_env_keys:
                if key not in env:
                    continue
                value = env[key]
                self.assertIsInstance(
                    value,
                    dict,
                    f"{app_label(deployment)}.{key} must use valueFrom/secretKeyRef, "
                    f"not a literal: {value!r}",
                )
                self.assertIn(
                    "secretKeyRef",
                    value,
                    f"{app_label(deployment)}.{key} must reference a Secret",
                )

    def test_backend_environment_is_production(self):
        docs = kustomize_build(STAGING_DIR)
        backends = [
            d for d in resources_by_kind(docs, "Deployment") if app_label(d) == "backend"
        ]
        self.assertTrue(backends, "missing backend Deployment")
        env = container_env_map(backends[0])
        self.assertEqual(
            env.get("ENVIRONMENT"),
            "production",
            "backend ENVIRONMENT must be production (or staging-equivalent activating "
            "ProductionCredentialsGuard)",
        )

    def test_flyway_baseline_on_migrate_disabled(self):
        docs = kustomize_build(STAGING_DIR)
        backends = [
            d for d in resources_by_kind(docs, "Deployment") if app_label(d) == "backend"
        ]
        env = container_env_map(backends[0])
        value = env.get("SPRING_FLYWAY_BASELINE_ON_MIGRATE")
        self.assertTrue(
            value is None or str(value).lower() == "false",
            f"SPRING_FLYWAY_BASELINE_ON_MIGRATE must be absent or false, got: {value}",
        )

    def test_staging_uses_ghcr_image_tags(self):
        docs = kustomize_build(STAGING_DIR)
        images = {}
        for deployment in resources_by_kind(docs, "Deployment"):
            name = app_label(deployment)
            specs = ((deployment.get("spec") or {}).get("template") or {}).get("spec") or {}
            containers = specs.get("containers") or []
            if containers:
                images[name] = containers[0].get("image", "")
        self.assertIn("ghcr.io/", images.get("backend", ""), images)
        self.assertIn("ghcr.io/", images.get("frontend", ""), images)
        # SHA-style tag (full sha, short sha, or sha-<hex>) — not a mutable floating tag alone
        for component in ("backend", "frontend"):
            image = images[component]
            tag = image.rsplit(":", 1)[-1] if ":" in image else ""
            self.assertTrue(
                re.fullmatch(r"(sha-[0-9a-f]{7,40}|[0-9a-f]{7,40})", tag)
                or "PLACEHOLDER" in tag.upper(),
                f"{component} image must use a GHCR SHA tag or explicit PLACEHOLDER, got: {image}",
            )

    def test_no_pgadmin_resources(self):
        docs = kustomize_build(STAGING_DIR)
        labels = [app_label(doc) for doc in docs]
        self.assertNotIn("pgadmin", labels)
        joined = "\n".join(collect_strings(docs)).lower()
        self.assertNotIn("pgadmin", joined)

    def test_deployment_docs_mention_kustomize(self):
        self.assertTrue(os.path.isfile(DEPLOYMENT_GUIDE), f"missing {DEPLOYMENT_GUIDE}")
        with open(DEPLOYMENT_GUIDE, encoding="utf-8") as f:
            guide = f.read()
        for snippet in (
            "infra/deploy/kustomize",
            "staging",
            "docker-compose.prod.yml",
            "Secret",
            "publish-only",
        ):
            self.assertIn(
                snippet,
                guide,
                f"209-deployment README must document Kustomize apply path; missing {snippet!r}",
            )

        self.assertTrue(os.path.isfile(DEPLOYMENT_PLAN), f"missing {DEPLOYMENT_PLAN}")
        with open(DEPLOYMENT_PLAN, encoding="utf-8") as f:
            plan = f.read()
        self.assertIn(
            "kustomize",
            plan.lower(),
            "DEPLOYMENT-PLAN.md must acknowledge the Kustomize staging target",
        )
        self.assertNotIn(
            "No staging or production environment is currently\nprovisioned",
            plan,
            "DEPLOYMENT-PLAN.md §1 must not still claim no staging/prod target",
        )


if __name__ == "__main__":
    unittest.main()
