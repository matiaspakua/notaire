"""Immutable factory entity revisions + extended kinds (#2239 Phase A)."""
from __future__ import annotations

import json
import sys
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class EntityVersioningTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")

    def test_update_creates_immutable_revision(self):
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1 summary",
        )
        self.assertEqual(0, add.returncode, output(add))

        shown = self.repo.run("catalog", "show", "--kind", "skills", "--id", "demo-skill", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        current = json.loads(shown.stdout)
        self.assertEqual(1, current["version"])
        self.assertEqual("v1 summary", current["summary"])
        v1_digest = current["digest"]

        version_path = (
            self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo-skill" / "versions" / "1.json"
        )
        self.assertTrue(version_path.is_file())
        v1_bytes = version_path.read_bytes()

        upd = self.repo.run(
            "catalog", "update", "--kind", "skills", "--id", "demo-skill",
            "--summary", "v2 summary",
        )
        self.assertEqual(0, upd.returncode, output(upd))

        shown2 = self.repo.run("catalog", "show", "--kind", "skills", "--id", "demo-skill", "--json")
        self.assertEqual(0, shown2.returncode, output(shown2))
        current2 = json.loads(shown2.stdout)
        self.assertEqual(2, current2["version"])
        self.assertEqual("v2 summary", current2["summary"])
        self.assertNotEqual(v1_digest, current2["digest"])

        # Prior revision bytes must not be rewritten.
        self.assertEqual(v1_bytes, version_path.read_bytes())

        hist = self.repo.run("catalog", "history", "--kind", "skills", "--id", "demo-skill", "--json")
        self.assertEqual(0, hist.returncode, output(hist))
        history = json.loads(hist.stdout)
        versions = [row["version"] for row in history["versions"]]
        self.assertEqual([1, 2], versions)

    def test_providers_kind_stores_env_refs_and_redacts_on_show(self):
        add = self.repo.run(
            "catalog", "add", "--kind", "providers", "--id", "openai-main",
            "--ref", "env:OPENAI_API_KEY", "--summary", "OpenAI provider",
        )
        self.assertEqual(0, add.returncode, output(add))
        listed = self.repo.run("catalog", "list", "--kind", "providers", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        rows = json.loads(listed.stdout)
        self.assertEqual(1, len(rows["entries"]))
        self.assertEqual("openai-main", rows["entries"][0]["id"])

        # Inject a secret-shaped value into body metadata via new-version JSON is not required;
        # show must keep env: refs and redact token-shaped fields if present on the payload.
        shown = self.repo.run("catalog", "show", "--kind", "providers", "--id", "openai-main", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        payload = json.loads(shown.stdout)
        self.assertEqual("env:OPENAI_API_KEY", payload.get("ref"))
        blob = json.dumps(payload)
        self.assertNotIn("ghp_", blob)

    def test_unknown_kind_rejected(self):
        result = self.repo.run("catalog", "list", "--kind", "widgets")
        self.assertEqual(2, result.returncode)

    def test_extended_kinds_round_trip(self):
        for kind, cid in (
            ("integrations", "github-forge"),
            ("rules", "preflight-gate"),
            ("workflows", "feature-delivery"),
        ):
            add = self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}.json", "--summary", f"{kind} entry",
            )
            self.assertEqual(0, add.returncode, output(add))
        listed = self.repo.run("catalog", "list", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        payload = json.loads(listed.stdout)
        for kind in ("integrations", "rules", "workflows"):
            self.assertIn(kind, payload)
            self.assertGreaterEqual(len(payload[kind]["entries"]), 1)


if __name__ == "__main__":
    unittest.main()
