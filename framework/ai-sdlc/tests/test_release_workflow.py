"""Kit tag release workflow, CHANGELOG and upgrade policy (#2256)."""
from __future__ import annotations

import unittest
from pathlib import Path

from helpers import KIT

REPO = KIT.parents[1]
WORKFLOW = REPO / ".github" / "workflows" / "aisdlc-kit.yml"


class ReleaseWorkflowTest(unittest.TestCase):
    def test_workflow_triggers_on_aisdlc_tags(self):
        self.assertTrue(WORKFLOW.is_file(), f"missing {WORKFLOW}")
        text = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("aisdlc-v", text)
        self.assertRegex(text, r"tags:\s*\n\s*-\s*['\"]?aisdlc-v")

    def test_workflow_builds_wheel_sdist_sbom_and_attests(self):
        text = WORKFLOW.read_text(encoding="utf-8")
        lowered = text.lower()
        self.assertIn("wheel", lowered)
        self.assertIn("sdist", lowered)
        self.assertTrue("sbom" in lowered or "cyclonedx" in lowered, "SBOM step missing")
        self.assertTrue(
            "attest-build-provenance" in lowered or "actions/attest@" in lowered,
            "provenance attestation step missing",
        )
        self.assertIn("attestations: write", text)
        self.assertIn("id-token: write", text)

    def test_workflow_does_not_publish_to_pypi(self):
        text = WORKFLOW.read_text(encoding="utf-8").lower()
        self.assertNotIn("pypa/gh-action-pypi-publish", text)
        self.assertNotIn("twine upload", text)

    def test_kit_changelog_and_upgrade_notes_exist(self):
        changelog = KIT / "CHANGELOG.md"
        upgrade = KIT / "docs" / "UPGRADE.md"
        self.assertTrue(changelog.is_file(), "framework/ai-sdlc/CHANGELOG.md missing")
        self.assertTrue(upgrade.is_file(), "framework/ai-sdlc/docs/UPGRADE.md missing")
        upgrade_text = upgrade.read_text(encoding="utf-8").lower()
        self.assertIn("minor", upgrade_text)
        self.assertTrue("deprecat" in upgrade_text, "deprecation window missing")
        self.assertTrue("pypi" in upgrade_text, "PyPI non-goal missing")


if __name__ == "__main__":
    unittest.main()
