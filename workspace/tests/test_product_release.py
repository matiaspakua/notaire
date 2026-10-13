#!/usr/bin/env python3
"""Product release contract: backend, frontend, database, train, SBOM (CU76, #1571).

Run: python3 -m unittest workspace.tests.test_product_release
"""

from __future__ import annotations

import importlib.util
import json
import os
import sys
import tempfile
import unittest
from pathlib import Path

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MODULE = os.path.join(REPO_ROOT, "workspace", "release", "product_release.py")


def load_module():
    spec = importlib.util.spec_from_file_location("product_release", MODULE)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def load_cd() -> dict:
    with open(os.path.join(REPO_ROOT, ".github", "workflows", "cd.yml"), encoding="utf-8") as handle:
        data = yaml.safe_load(handle) or {}
    if "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


class TrainAndTagTest(unittest.TestCase):
    def setUp(self):
        self.mod = load_module()

    def test_pre_one_and_one_zero_advance_the_release_blockers_train(self):
        self.assertEqual(self.mod.TRAIN_V10, "2026-11 · Release blockers")
        self.assertEqual(self.mod.train_for("0.1.2"), self.mod.TRAIN_V10)
        self.assertEqual(self.mod.train_for("0.2.0"), self.mod.TRAIN_V10)
        self.assertEqual(self.mod.train_for("1.0.0"), self.mod.TRAIN_V10)
        self.assertEqual(self.mod.train_for("1.0.4"), self.mod.TRAIN_V10)

    def test_one_one_advances_the_quality_train(self):
        self.assertEqual(self.mod.TRAIN_V11, "2027-01 · Quality & traceability")
        self.assertEqual(self.mod.train_for("1.1.0"), self.mod.TRAIN_V11)

    def test_unmapped_minor_fails_closed(self):
        with self.assertRaises(ValueError):
            self.mod.train_for("1.2.0")

    def test_legacy_component_tags_stay_addressable(self):
        self.assertEqual(self.mod.tag_for("0.1.2"), "notaire-v0.1.2")
        self.assertEqual(self.mod.tag_for("0.2.0"), "v0.2.0")


class RepositoryContractTest(unittest.TestCase):
    def setUp(self):
        self.mod = load_module()
        self.root = Path(REPO_ROOT)
        self.snapshot = self.mod.collect(self.root)

    def test_manifest_matches_maven_npm_and_latest_changelog(self):
        version = self.snapshot["version"]
        manifest = json.loads((self.root / ".release-please-manifest.json").read_text(encoding="utf-8"))
        self.assertEqual(version, manifest["."])
        self.assertEqual(self.snapshot["components"]["backend"]["version"], version)
        self.assertEqual(self.snapshot["components"]["backend"]["parentVersion"], version)
        self.assertEqual(self.snapshot["components"]["frontend"]["version"], version)
        self.assertNotEqual(self.snapshot["components"]["backend"]["version"], "1.0-SNAPSHOT")
        self.assertEqual(self.snapshot["releases"][0]["version"], version)

    def test_database_schema_is_recorded_and_not_the_semver(self):
        database = self.snapshot["components"]["database"]
        self.assertGreaterEqual(int(database["schemaVersion"]), 46)
        self.assertEqual(database["migrationCount"], len(self.snapshot["migrations"]))
        self.assertNotEqual(database["schemaVersion"], self.snapshot["version"])
        self.assertEqual(database["engine"], "flyway")

    def test_committed_record_matches_the_tree(self):
        errors = self.mod.check(self.root)
        self.assertEqual(errors, [])

    def test_snapshot_heading_is_not_a_product_release(self):
        versions = [item["version"] for item in self.snapshot["releases"]]
        self.assertIn("0.2.0", versions)
        self.assertIn("0.1.2", versions)
        self.assertIn("0.1.1", versions)
        self.assertNotIn("1.0.0-SNAPSHOT", versions)

    def test_product_sbom_lists_backend_frontend_and_database(self):
        sbom = self.mod.product_sbom(self.snapshot)
        self.assertEqual(sbom["bomFormat"], "CycloneDX")
        self.assertEqual(sbom["specVersion"], "1.6")
        self.assertEqual(sbom["metadata"]["component"]["name"], "notaire")
        self.assertEqual(sbom["metadata"]["component"]["version"], self.snapshot["version"])
        names = [component["name"] for component in sbom["components"]]
        self.assertEqual(names, ["notaire-backend", "notaire-frontend", "notaire-database"])
        trains = [
            prop["value"]
            for prop in sbom["metadata"]["properties"]
            if prop["name"] == "notaire:release-train"
        ]
        self.assertEqual(trains, [self.mod.TRAIN_V10])
        database = sbom["components"][2]
        self.assertEqual(database["version"], self.snapshot["components"]["database"]["schemaVersion"])
        self.assertNotEqual(database["version"], self.snapshot["version"])

    def test_database_sbom_lists_every_migration(self):
        sbom = self.mod.database_sbom(self.snapshot)
        self.assertEqual(sbom["metadata"]["component"]["version"], self.snapshot["components"]["database"]["schemaVersion"])
        self.assertEqual(len(sbom["components"]), self.snapshot["components"]["database"]["migrationCount"])
        self.assertTrue(all(component["type"] == "file" for component in sbom["components"]))
        self.assertEqual(sbom["components"][-1]["version"], self.snapshot["components"]["database"]["schemaVersion"])

    def test_pages_history_is_append_only_semver(self):
        history = self.mod.pages_history(self.snapshot)
        self.assertEqual(history["productVersion"], self.snapshot["version"])
        self.assertEqual(history["train"], self.mod.TRAIN_V10)
        self.assertEqual(
            history["components"]["databaseSchema"],
            self.snapshot["components"]["database"]["schemaVersion"],
        )
        self.assertEqual(
            [item["version"] for item in history["releases"]],
            [item["version"] for item in self.snapshot["releases"]],
        )
        self.assertTrue(history["releases"][0]["url"].endswith(f"/v{self.snapshot['version']}"))

    def test_sbom_writer_is_deterministic(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.mod.write_json(root / "a.json", self.mod.product_sbom(self.snapshot))
            self.mod.write_json(root / "b.json", self.mod.product_sbom(self.snapshot))
            self.assertEqual((root / "a.json").read_text(encoding="utf-8"), (root / "b.json").read_text(encoding="utf-8"))


class ReleaseAutomationTest(unittest.TestCase):
    def test_future_tags_are_v_and_legacy_tags_still_publish(self):
        with open(os.path.join(REPO_ROOT, "release-please-config.json"), encoding="utf-8") as handle:
            config = json.load(handle)
        self.assertIs(config["packages"]["."]["include-component-in-tag"], False)
        workflow = load_cd()
        tags = workflow["on"]["push"]["tags"]
        self.assertIn("v*", tags)
        self.assertIn("notaire-v*", tags)
        release_job = workflow["jobs"]["release"]
        condition = str(release_job.get("if", ""))
        self.assertIn("refs/tags/v", condition)
        self.assertIn("refs/tags/notaire-v", condition)
        blob = yaml.dump(release_job)
        self.assertIn("sbom-database.cdx.json", blob)
        self.assertIn("sbom-notaire.cdx.json", blob)
        self.assertIn("product_release.py", blob)

    def test_version_surfaces_and_docs_name_the_three_components(self):
        properties = Path(
            REPO_ROOT, "backend-api", "src", "main", "resources", "application.properties"
        ).read_text(encoding="utf-8")
        self.assertIn("app.version=@project.version@", properties)
        pom = Path(REPO_ROOT, "backend-api", "pom.xml").read_text(encoding="utf-8")
        self.assertIn("<useDefaultDelimiters>false</useDefaultDelimiters>", pom)
        for relative in (
            "docs/300-development/305-project-management/RELEASES.md",
            "docs/300-development/SBOM.md",
            "docs/300-development/RELEASE.md",
            "CONSTITUTION.md",
        ):
            text = Path(REPO_ROOT, relative).read_text(encoding="utf-8")
            self.assertIn("backend", text.lower())
            self.assertIn("frontend", text.lower())
            self.assertIn("database", text.lower())


if __name__ == "__main__":
    unittest.main()
