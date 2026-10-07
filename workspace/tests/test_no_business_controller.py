#!/usr/bin/env python3
"""
Guards issue #900 / CU76: the BusinessController god class stays removed.

Run with: python3 workspace/tests/test_no_business_controller.py
"""
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
BACKEND = REPO_ROOT / "backend-api"
FORBIDDEN = "BusinessController"
ALLOWED_PATHS = (
    "workspace/tests/test_no_business_controller.py",
    "CHANGELOG.md",
    "docs/openspec/",
    "docs/000-archive/",
    "docs/200-architecture/201-SAD/sad.md",
    "backend-api/src/test/java/com/licensis/notaire/integration/CoreBusinessControllersIntegrationTest.java",
    "backend-api/src/test/java/com/licensis/notaire/unit/TraceabilityMatrixTest.java",
)


def tracked_files():
    out = subprocess.run(["git", "ls-files", "-z"], cwd=REPO_ROOT, capture_output=True, check=True).stdout
    return [p for p in out.decode("utf-8").split("\0") if p and (REPO_ROOT / p).is_file()]


class BusinessControllerRemovedTest(unittest.TestCase):
    def test_the_class_file_is_gone(self):
        self.assertFalse((BACKEND / "src/main/java/com/licensis/notaire/business/BusinessController.java").exists())

    def test_no_production_source_or_build_file_references_it(self):
        offenders = []
        for rel in tracked_files():
            if rel.startswith(ALLOWED_PATHS) or not rel.startswith("backend-api/"):
                continue
            if rel.endswith((".java", ".xml", ".properties", ".yml", ".yaml")):
                if FORBIDDEN in (REPO_ROOT / rel).read_text(encoding="utf-8", errors="ignore"):
                    offenders.append(rel)
        self.assertEqual([], offenders)


if __name__ == "__main__":
    unittest.main()
