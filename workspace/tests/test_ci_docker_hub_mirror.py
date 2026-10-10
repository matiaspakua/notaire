"""Issue #1380: PR workflows must not pull images from Docker Hub anonymously.

Shared GitHub runners hit Docker Hub's unauthenticated pull rate limit (429
"toomanyrequests"), which failed Bruno, Playwright, OpenAPI and Testcontainers
jobs before any test ran. Images come from Google's public Docker Hub cache
(mirror.gcr.io) instead, and transient browser downloads are retried.

Run with: python3 workspace/tests/test_ci_docker_hub_mirror.py
"""
import os
import unittest

import yaml

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
WORKFLOWS = os.path.join(REPO_ROOT, ".github", "workflows")
MIRROR = "mirror.gcr.io"


def load(name):
    with open(os.path.join(WORKFLOWS, name), encoding="utf-8") as f:
        return yaml.safe_load(f)


def steps(workflow, job):
    return workflow["jobs"][job].get("steps") or []


def service_images(workflow):
    for job in workflow.get("jobs", {}).values():
        for svc in (job.get("services") or {}).values():
            if isinstance(svc, dict) and "image" in svc:
                yield str(svc["image"])


class DockerHubMirrorTest(unittest.TestCase):
    def test_service_containers_use_the_mirror(self):
        found = []
        for name in sorted(os.listdir(WORKFLOWS)):
            if not name.endswith((".yml", ".yaml")):
                continue
            for image in service_images(load(name)):
                found.append(image)
                self.assertTrue(image.startswith(MIRROR + "/"),
                                f"{name}: service image {image} must come from {MIRROR} (#1380)")
        self.assertTrue(found, "expected service containers in the workflows")

    def test_testcontainers_pull_through_the_mirror(self):
        integration = [s for s in steps(load("ci.yml"), "integration-tests")
                       if "-Ppg-integration" in str(s.get("run", ""))]
        self.assertEqual(len(integration), 1)
        env = integration[0].get("env") or {}
        self.assertEqual(env.get("TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX"), MIRROR + "/")

    def test_buildx_uses_the_mirror_for_docker_io(self):
        workflow = load("ci.yml")
        buildx = [s for job in workflow["jobs"] for s in steps(workflow, job)
                  if "docker/setup-buildx-action" in str(s.get("uses", ""))]
        self.assertTrue(buildx)
        for step in buildx:
            config = str((step.get("with") or {}).get("buildkitd-config-inline", ""))
            self.assertIn('[registry."docker.io"]', config)
            self.assertIn(MIRROR, config)

    def test_openapi_gate_does_not_build_a_docker_image(self):
        workflow = load("openapi-contract.yml")
        uses = [str(s.get("uses", "")) for s in steps(workflow, "openapi-contract")]
        self.assertFalse([u for u in uses if "oasdiff-action" in u],
                         "oasdiff-action builds tufin/oasdiff from Docker Hub (#1380)")

    def test_playwright_browser_install_is_retried(self):
        workflow = load("playwright-e2e.yml")
        installs = [s for job in workflow["jobs"] for s in steps(workflow, job)
                    if "playwright install" in str(s.get("run", ""))]
        self.assertTrue(installs)
        for step in installs:
            self.assertIn("attempt", str(step["run"]), "browser download must be retried (#1380)")


if __name__ == "__main__":
    unittest.main()
