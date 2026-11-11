"""The profile records the kit version it was written for; doctor reports skew (#2256)."""
from __future__ import annotations

import sys
import tomllib
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc import __version__  # noqa: E402


def profile_with(version: str) -> str:
    return f'[project]\nname = "demo"\npolicy = "POLICY.md"\nkit_version = "{version}"\n'


def other_major() -> str:
    return f"{int(__version__.split('.')[0]) + 1}.0.0"


def other_minor() -> str:
    major, minor, _ = __version__.split(".")
    return f"{major}.{int(minor) + 1}.0"


class KitVersionTest(unittest.TestCase):
    def test_matching_version_prints_no_skew(self):
        result = Repo(self, profile_with(__version__)).run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn("kit_version", output(result))

    def test_absent_key_prints_no_skew(self):
        result = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n').run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn("kit_version", output(result))

    def test_minor_difference_is_info(self):
        recorded = other_minor()
        result = Repo(self, profile_with(recorded)).run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        line = next(l for l in output(result).splitlines() if "kit_version" in l)
        self.assertIn("INFO", line)
        self.assertIn(recorded, line)
        self.assertIn(__version__, line)

    def test_major_difference_is_warn_and_not_a_failure(self):
        recorded = other_major()
        result = Repo(self, profile_with(recorded)).run("doctor")
        self.assertEqual(0, result.returncode, output(result))
        line = next(l for l in output(result).splitlines() if "kit_version" in l)
        self.assertIn("WARN", line)
        self.assertIn(recorded, line)

    def test_invalid_version_rejects_the_profile(self):
        result = Repo(self, profile_with("latest")).run("doctor")
        self.assertNotEqual(0, result.returncode)
        self.assertIn("kit_version", output(result))

    def test_init_records_the_running_version(self):
        repo = Repo(self)
        self.assertEqual(0, repo.run("init", "--name", "shop").returncode)
        profile = tomllib.loads((repo.root / "aisdlc.toml").read_text(encoding="utf-8"))
        self.assertEqual(__version__, profile["project"]["kit_version"])


if __name__ == "__main__":
    unittest.main()
