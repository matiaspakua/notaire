#!/usr/bin/env python3
"""
Guards issue #1276 (CU76): build, CI and runtime use JDK 26, and the backend
Docker bases are pinned to a minor or digest (#1045).

Plain stdlib unittest, consistent with workspace/tests/test_notaire_shared_retired.py.
Run with: python3 workspace/tests/test_jdk26_toolchain.py

Also discoverable via: python3 -m unittest discover -s scripts/tests
(see scripts/tests/test_jdk26_toolchain.py).
"""

from __future__ import annotations

import re
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
JDK = "26"
POM_NS = {"m": "http://maven.apache.org/POM/4.0.0"}
WORKFLOWS = sorted((REPO_ROOT / ".github" / "workflows").glob("*.yml"))
DOCKERFILES = (
    REPO_ROOT / "backend-api" / "Dockerfile",
    REPO_ROOT / "backend-api" / "Dockerfile.slim",
)

JAVA_VERSION_LINE = re.compile(r"""^\s*(?:java-version|JAVA_VERSION):\s*['"]?([^'"\s$]+)""", re.MULTILINE)
JDK_BASE = re.compile(r"^FROM\s+((?:maven|eclipse-temurin):\S+)", re.MULTILINE)
MINOR_PINNED = re.compile(r"\d+\.\d+")


class Jdk26ToolchainTest(unittest.TestCase):
    def test_pom_targets_jdk_26(self) -> None:
        """Scenario: Build and CI use JDK 26 (POM)."""
        root = ET.parse(REPO_ROOT / "pom.xml").getroot()
        self.assertEqual(JDK, root.find("m:properties/m:java.version", POM_NS).text.strip())

    def test_every_workflow_java_version_is_jdk_26(self) -> None:
        """Scenario: Build and CI use JDK 26 (workflows)."""
        wrong = {
            f"{path.name}: {version}"
            for path in WORKFLOWS
            for version in JAVA_VERSION_LINE.findall(path.read_text(encoding="utf-8"))
            if version != JDK
        }
        self.assertEqual(set(), wrong)

    def test_workflows_set_a_java_version_somewhere(self) -> None:
        found = [p for p in WORKFLOWS if JAVA_VERSION_LINE.search(p.read_text(encoding="utf-8"))]
        self.assertTrue(found, "no workflow sets a Java version")

    def test_docker_bases_are_jdk_26_and_pinned(self) -> None:
        """Scenario: Docker bases are pinned JDK 26."""
        for dockerfile in DOCKERFILES:
            bases = JDK_BASE.findall(dockerfile.read_text(encoding="utf-8"))
            self.assertTrue(bases, f"{dockerfile.name} has no maven/temurin base")
            for base in bases:
                tag = base.split(":", 1)[1]
                self.assertIn(f"temurin-{JDK}" if base.startswith("maven") else JDK, tag, f"{dockerfile.name}: {base}")
                self.assertRegex(tag, MINOR_PINNED, f"{dockerfile.name}: {base} must be minor-pinned")


if __name__ == "__main__":
    unittest.main()
