#!/usr/bin/env python3
"""
Tests for generate-coverage-snapshot.py (issue #758).

Prior behavior silently wrote fabricated 0%/hardcoded numbers when the
JaCoCo XML was missing, which deployed a misleading "0% coverage" to the
live GitHub Pages site while real coverage was 83%+. This script now fails
loudly (nonzero exit, no output file) instead -- these tests pin that
contract via subprocess, since the script is a CLI entry point rather than
an importable module.

Plain stdlib unittest, consistent with this project's other one-off CI/config
validation scripts (see scripts/test_performance_test_assets.py).
Run with: python3 scripts/test_generate_coverage_snapshot.py
"""
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest

SCRIPT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "generate-coverage-snapshot.py")

MINIMAL_JACOCO_XML = """<?xml version="1.0" encoding="UTF-8"?>
<report name="backend-api">
  <package name="com/example">
    <counter type="LINE" missed="1" covered="1"/>
  </package>
  <counter type="INSTRUCTION" missed="17" covered="83"/>
  <counter type="BRANCH" missed="26" covered="74"/>
  <counter type="LINE" missed="10" covered="90"/>
  <counter type="COMPLEXITY" missed="1" covered="9"/>
  <counter type="METHOD" missed="1" covered="9"/>
  <counter type="CLASS" missed="0" covered="1"/>
</report>
"""


def run_script(cwd):
    return subprocess.run([sys.executable, SCRIPT], cwd=cwd, capture_output=True, text=True)


class GenerateCoverageSnapshotTest(unittest.TestCase):

    def setUp(self):
        self.tmpdir = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, self.tmpdir, ignore_errors=True)

    def test_fails_loudly_and_writes_nothing_when_jacoco_xml_missing(self):
        result = run_script(self.tmpdir)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("not found", result.stderr)
        self.assertFalse(os.path.exists(os.path.join(self.tmpdir, "coverage-snapshot")))

    def test_writes_real_numbers_parsed_from_jacoco_xml(self):
        jacoco_dir = os.path.join(self.tmpdir, "backend-api", "target", "site", "jacoco")
        os.makedirs(jacoco_dir)
        with open(os.path.join(jacoco_dir, "jacoco.xml"), "w") as f:
            f.write(MINIMAL_JACOCO_XML)

        result = run_script(self.tmpdir)

        self.assertEqual(result.returncode, 0, result.stderr)
        snapshot_path = os.path.join(self.tmpdir, "coverage-snapshot", "coverage-snapshot.json")
        with open(snapshot_path) as f:
            snapshot = json.load(f)
        self.assertEqual(snapshot["backendInstruction"], 83)
        self.assertEqual(snapshot["backendBranch"], 74)
        self.assertIn("lastUpdated", snapshot)
        # Old hardcoded fields must be gone -- this script is backend-only now.
        self.assertNotIn("frontendComponent", snapshot)
        self.assertNotIn("e2eTests", snapshot)
        self.assertNotIn("apiEndpoints", snapshot)


if __name__ == "__main__":
    unittest.main()
