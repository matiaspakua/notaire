#!/usr/bin/env python3
"""
Guards issue #1043 (CU76): automated semver release process.

Asserts:
1. A release-please (preferred) or tag-triggered release workflow exists
2. Release config/script wires Maven root pom.xml and frontend/package.json
3. CHANGELOG roll / Keep a Changelog is referenced in release config or docs
4. Permanent DevSecOps/development docs describe the operator release path

Run with: python3 scripts/test_semver_release_process.py
"""

from __future__ import annotations

import glob
import os
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WORKFLOWS_DIR = os.path.join(REPO_ROOT, ".github", "workflows")
DEVSECOPS_README = os.path.join(
    REPO_ROOT, "docs", "200-architecture", "208-devsecops", "README.md"
)
RELEASE_RUNBOOK_CANDIDATES = [
    os.path.join(REPO_ROOT, "docs", "300-development", "RELEASE.md"),
    os.path.join(
        REPO_ROOT, "docs", "200-architecture", "208-devsecops", "RELEASE.md"
    ),
    os.path.join(REPO_ROOT, "docs", "300-development", "SEMVER-RELEASE.md"),
]


def load_yaml(path: str) -> dict:
    with open(path, encoding="utf-8") as f:
        data = yaml.safe_load(f) or {}
    if isinstance(data, dict) and "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


def read_text(path: str) -> str:
    with open(path, encoding="utf-8") as f:
        return f.read()


def workflow_files() -> list[str]:
    return sorted(glob.glob(os.path.join(WORKFLOWS_DIR, "*.yml"))) + sorted(
        glob.glob(os.path.join(WORKFLOWS_DIR, "*.yaml"))
    )


def find_release_automation() -> tuple[str | None, str]:
    """Return (path, blob) for release-please or dedicated release automation."""
    # Config files
    for rel in (
        "release-please-config.json",
        ".release-please-config.json",
        "release-please-config.yml",
    ):
        path = os.path.join(REPO_ROOT, rel)
        if os.path.isfile(path):
            return path, read_text(path)

    for path in workflow_files():
        text = read_text(path)
        lower = text.lower()
        if "release-please" in lower:
            return path, text
        # Tag-triggered version bump / changelog roll (not mere softprops in cd.yml)
        if os.path.basename(path) in (
            "release.yml",
            "release-please.yml",
            "semver-release.yml",
        ):
            return path, text
        if "release-please-action" in lower or "googleapis/release-please" in lower:
            return path, text

    # Tag-triggered script referenced from workflows
    for path in workflow_files():
        text = read_text(path)
        if "scripts/release" in text or "bump-version" in text.lower():
            return path, text

    return None, ""


class SemverReleaseProcessTest(unittest.TestCase):
    """Automated semver + Maven/npm/CHANGELOG wiring (#1043)."""

    def test_automated_release_workflow_exists(self):
        path, blob = find_release_automation()
        self.assertIsNotNone(
            path,
            "expected release-please config/workflow or tag-triggered release automation",
        )
        lower = blob.lower()
        self.assertTrue(
            "release-please" in lower
            or "v*" in blob
            or "refs/tags/v" in blob
            or "semver" in lower,
            f"{path} does not look like automated v* release tooling",
        )

    def test_release_wires_maven_and_npm_versions(self):
        path, blob = find_release_automation()
        self.assertIsNotNone(path, "missing release automation")
        # Also search companion config / manifest / scripts
        extras = []
        for rel in (
            "release-please-config.json",
            ".release-please-manifest.json",
            "scripts/release_bump_versions.sh",
            "scripts/release-bump-versions.sh",
            "scripts/semver_release.sh",
        ):
            p = os.path.join(REPO_ROOT, rel)
            if os.path.isfile(p):
                extras.append(read_text(p))
        combined = blob + "\n" + "\n".join(extras)
        self.assertIn(
            "pom.xml",
            combined,
            "release automation must target root pom.xml for Maven version",
        )
        self.assertTrue(
            "frontend/package.json" in combined or "package.json" in combined,
            "release automation must target frontend/package.json version",
        )

    def test_changelog_roll_wired(self):
        path, blob = find_release_automation()
        self.assertIsNotNone(path, "missing release automation")
        extras = []
        for rel in (
            "release-please-config.json",
            "CHANGELOG.md",
        ):
            p = os.path.join(REPO_ROOT, rel)
            if os.path.isfile(p) and rel != "CHANGELOG.md":
                extras.append(read_text(p))
        docs = []
        for candidate in RELEASE_RUNBOOK_CANDIDATES + [DEVSECOPS_README]:
            if os.path.isfile(candidate):
                docs.append(read_text(candidate))
        combined = (blob + "\n" + "\n".join(extras + docs)).lower()
        self.assertTrue(
            "changelog" in combined and ("unreleased" in combined or "release-please" in combined),
            "release process must roll CHANGELOG [Unreleased] (config or docs)",
        )

    def test_release_process_documented(self):
        runbook = None
        for candidate in RELEASE_RUNBOOK_CANDIDATES:
            if os.path.isfile(candidate):
                runbook = candidate
                break
        self.assertIsNotNone(
            runbook,
            "expected a release runbook under docs/300-development/ or DevSecOps "
            "(RELEASE.md or SEMVER-RELEASE.md)",
        )
        text = read_text(runbook).lower()
        self.assertIn("semver", text)
        self.assertTrue(
            "release-please" in text or "tag" in text,
            "runbook must describe release-please or tag-triggered flow",
        )
        self.assertIn("changelog", text)
        self.assertTrue(
            "maven" in text or "pom.xml" in text,
            "runbook must mention Maven version derivation",
        )
        self.assertTrue(
            "npm" in text or "package.json" in text,
            "runbook must mention npm version derivation",
        )
        # DevSecOps README should point at frontend GHCR + release
        self.assertTrue(os.path.isfile(DEVSECOPS_README), DEVSECOPS_README)
        devsec = read_text(DEVSECOPS_README).lower()
        self.assertIn("frontend", devsec)
        self.assertTrue(
            "ghcr" in devsec or "container" in devsec,
            "DevSecOps README must document frontend image publish",
        )

class ReleasePleaseCanOpenPrTest(unittest.TestCase):
    """Guards issue #1264: the release PR must be opened by an identity that triggers checks."""

    def test_release_please_uses_dedicated_token(self):
        wf = load_yaml(os.path.join(WORKFLOWS_DIR, "release-please.yml"))
        steps = wf["jobs"]["release-please"]["steps"]
        action = next(s for s in steps if "release-please-action" in s.get("uses", ""))
        token = action.get("with", {}).get("token", "")
        self.assertIn("secrets.RELEASE_PLEASE_TOKEN", token)

    def test_release_please_has_bootstrap_sha(self):
        import json
        with open(os.path.join(REPO_ROOT, "release-please-config.json"), encoding="utf-8") as f:
            cfg = json.load(f)
        sha = cfg.get("bootstrap-sha", "")
        self.assertRegex(sha, r"^[0-9a-f]{40}$")

    def test_runbook_documents_release_token(self):
        text = read_text(os.path.join(REPO_ROOT, "docs", "300-development", "RELEASE.md"))
        self.assertIn("RELEASE_PLEASE_TOKEN", text)


if __name__ == "__main__":
    unittest.main()
