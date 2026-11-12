"""Profile [project] schema_version is recorded by init and rejected when invalid (#2256)."""
from __future__ import annotations

import sys
import tomllib
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc.profile import PROFILE_SCHEMA_VERSION  # noqa: E402


class ProfileSchemaVersionTest(unittest.TestCase):
    def test_init_records_schema_version(self):
        repo = Repo(self)
        self.assertEqual(0, repo.run("init", "--name", "shop").returncode)
        profile = tomllib.loads((repo.root / "aisdlc.toml").read_text(encoding="utf-8"))
        self.assertEqual(PROFILE_SCHEMA_VERSION, profile["project"]["schema_version"])

    def test_matching_schema_version_loads(self):
        body = (
            f'[project]\nname = "demo"\npolicy = "POLICY.md"\n'
            f"schema_version = {PROFILE_SCHEMA_VERSION}\n"
        )
        result = Repo(self, body).run("doctor")
        self.assertEqual(0, result.returncode, output(result))

    def test_absent_schema_version_defaults_compatibly(self):
        result = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n').run("doctor")
        self.assertEqual(0, result.returncode, output(result))

    def test_invalid_schema_version_is_rejected(self):
        result = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\nschema_version = 0\n').run(
            "doctor"
        )
        self.assertNotEqual(0, result.returncode)
        self.assertIn("schema_version", output(result))

    def test_non_integer_schema_version_is_rejected(self):
        result = Repo(
            self, '[project]\nname = "demo"\npolicy = "POLICY.md"\nschema_version = "latest"\n'
        ).run("doctor")
        self.assertNotEqual(0, result.returncode)
        self.assertIn("schema_version", output(result))


if __name__ == "__main__":
    unittest.main()
