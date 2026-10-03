#!/usr/bin/env python3
"""
Guards issue #1043 (CU76): CD must publish the frontend image to GHCR with
SBOM + cosign sign + attest parity with the backend.

Asserts:
1. A publish job (or matrix entry) builds frontend/Dockerfile
2. Image name targets …/frontend (not only …/backend)
3. CycloneDX SBOM is generated and uploaded for the frontend image
4. Cosign keyless sign + CycloneDX attest run for the frontend digest
5. Tag wiring mirrors backend (publish SHA + latest-after-SHA when applicable)

Run with: python3 scripts/test_frontend_ghcr_publish.py
"""

from __future__ import annotations

import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CD_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "cd.yml")


def load_workflow(path: str) -> dict:
    with open(path, encoding="utf-8") as f:
        workflow = yaml.safe_load(f)
    if "on" not in workflow and True in workflow:
        workflow["on"] = workflow.pop(True)
    return workflow


def dump_job(job: dict) -> str:
    return yaml.dump(job, default_flow_style=False)


def publish_jobs(workflow: dict) -> dict:
    """Jobs that build/push Docker images (matrix or sibling)."""
    jobs = workflow.get("jobs", {}) or {}
    found = {}
    for job_id, job in jobs.items():
        if not isinstance(job, dict):
            continue
        blob = dump_job(job).lower()
        if "docker/build-push-action" in blob or "build-and-publish" in job_id:
            found[job_id] = job
    return found


def matrix_includes(job: dict) -> list:
    strategy = job.get("strategy") or {}
    matrix = strategy.get("matrix") or {}
    include = matrix.get("include") or []
    if isinstance(include, list):
        return [row for row in include if isinstance(row, dict)]
    return []


class FrontendGhcrPublishTest(unittest.TestCase):
    """cd.yml publishes frontend image with SBOM + cosign parity (#1043)."""

    def setUp(self):
        self.assertTrue(os.path.isfile(CD_WORKFLOW), CD_WORKFLOW)
        self.cd = load_workflow(CD_WORKFLOW)
        self.publish = publish_jobs(self.cd)
        self.assertTrue(self.publish, "expected at least one Docker publish job")

    def _frontend_surface(self) -> str:
        """Concatenated YAML for jobs that mention frontend publish paths."""
        parts = []
        for job_id, job in self.publish.items():
            blob = dump_job(job)
            if (
                "frontend" in blob.lower()
                or "frontend/dockerfile" in blob.lower()
                or any(
                    "frontend" in str(v).lower()
                    for row in matrix_includes(job)
                    for v in row.values()
                )
            ):
                parts.append(blob)
        return "\n".join(parts)

    def test_frontend_dockerfile_is_cd_build_context(self):
        surface = self._frontend_surface()
        self.assertTrue(
            surface,
            "cd.yml must have a frontend publish job or matrix entry",
        )
        self.assertRegex(
            surface,
            r"frontend/Dockerfile|file:\s*.*frontend/Dockerfile|\./frontend/Dockerfile",
            "frontend publish must use frontend/Dockerfile",
        )
        # Context under frontend/ (or file path that implies it)
        self.assertTrue(
            re.search(r"context:\s*\./frontend|context:\s*frontend\b", surface)
            or "frontend/Dockerfile" in surface,
            "frontend Docker build context/file must be under frontend/",
        )

    def test_frontend_image_name_not_backend_only(self):
        surface = self._frontend_surface()
        self.assertTrue(surface, "missing frontend publish surface")
        self.assertRegex(
            surface,
            r"/frontend|image:\s*frontend|component:\s*frontend",
            "IMAGE_NAME / matrix must target …/frontend",
        )
        # Backend-only global IMAGE_NAME alone is insufficient
        env = self.cd.get("env") or {}
        global_image = str(env.get("IMAGE_NAME", ""))
        if global_image.endswith("/backend") and "matrix" not in yaml.dump(self.publish):
            self.fail(
                "global IMAGE_NAME is backend-only and there is no matrix/sibling "
                "frontend image name override"
            )

    def test_frontend_cyclonedx_sbom_generated_and_uploaded(self):
        surface = self._frontend_surface()
        self.assertTrue(surface, "missing frontend publish surface")
        self.assertIn("cyclonedx", surface.lower())
        self.assertIn("trivy", surface.lower())
        self.assertRegex(
            surface,
            r"sbom.*frontend|frontend.*sbom|name:\s*sbom-frontend|sbom_artifact",
            "frontend SBOM artifact name must be distinct (e.g. sbom-frontend)",
        )
        self.assertIn("upload-artifact", surface.lower())

    def test_frontend_cosign_sign_keyless(self):
        surface = self._frontend_surface()
        self.assertTrue(surface, "missing frontend publish surface")
        self.assertIn("cosign sign", surface.lower())
        # Job-level or workflow permissions for OIDC
        perms_blob = yaml.dump(
            {jid: j.get("permissions") for jid, j in self.publish.items()}
        )
        self.assertIn("id-token", perms_blob)

    def test_frontend_cosign_attest_sbom(self):
        surface = self._frontend_surface()
        self.assertTrue(surface, "missing frontend publish surface")
        self.assertIn("cosign attest", surface.lower())
        self.assertIn("cyclonedx", surface.lower())
        self.assertIn("--predicate", surface)

    def test_frontend_tags_include_publish_sha_and_latest_after(self):
        """Parity with #1042 pin/latest ordering on the frontend path."""
        surface = self._frontend_surface()
        self.assertTrue(surface, "missing frontend publish surface")
        self.assertIn("publish_sha", surface)
        self.assertIn("latest", surface.lower())
        # latest must not appear only as first-push metadata value=latest in isolation;
        # require an imagetools or dedicated latest step after push (same as backend).
        self.assertTrue(
            "imagetools" in surface.lower() or "tag and push latest" in surface.lower(),
            "frontend path must move latest after SHA-tagged push (imagetools or equivalent)",
        )


if __name__ == "__main__":
    unittest.main()
