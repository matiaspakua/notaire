"""Workflow body v2 pins, digest-guarded approve, new_version→draft (#2254)."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class WorkflowV2Test(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            result = self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
            self.assertEqual(0, result.returncode, output(result))
        created = self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "v1",
            "--agent", "implementer", "--skill", "java", "--mcp", "github",
            "--json",
        )
        self.assertEqual(0, created.returncode, output(created))
        migrated = self.repo.run("migrate", "--json")
        self.assertEqual(0, migrated.returncode, output(migrated))

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_workflow_body_v2_shape(self):
        shown = self.repo.run("workflow", "show", "--id", "feature-delivery", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        payload = json.loads(shown.stdout)
        self.assertEqual(2, payload["schema_version"])
        body = payload["body"]
        self.assertIsInstance(body["steps"], list)
        self.assertIsInstance(body["inputs"], list)
        self.assertIsInstance(body["outputs"], list)
        step = body["steps"][0]
        for key in ("id", "stage", "agent", "skills", "mcp", "provider", "gates", "budget", "on_fail"):
            self.assertIn(key, step)
        self.assertIn("tokens", step["budget"])
        self.assertIn("time_seconds", step["budget"])
        self.assertIn("usd", step["budget"])
        self.assertRegex(str(step["agent"]), r"^agents:[^@]+@\d+$")

    def test_request_validation_pins_digests(self):
        define = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery", "--action", "define", "--json",
        )
        self.assertEqual(0, define.returncode, output(define))
        validate = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery",
            "--action", "request_validation", "--waive-reason", "unit", "--json",
        )
        self.assertEqual(0, validate.returncode, output(validate))
        payload = json.loads(validate.stdout)
        pins = payload.get("validation", {}).get("pins")
        self.assertIsInstance(pins, list)
        self.assertGreaterEqual(len(pins), 1)
        for pin in pins:
            self.assertIn("ref", pin)
            self.assertIn("digest", pin)
            self.assertTrue(pin["digest"])

    def test_approve_fails_on_pin_digest_drift(self):
        from aisdlc import catalogs, workflows

        ctx = self._ctx()
        workflows.transition(ctx, "feature-delivery", "define")
        validated = workflows.transition(
            ctx, "feature-delivery", "request_validation", waive_reason="unit",
        )
        pins = validated["validation"]["pins"]
        self.assertTrue(pins)
        # Tamper the immutable pinned revision file so its digest no longer matches.
        kind, entry_id, version = workflows.parse_ref(pins[0]["ref"])
        path = (
            self.repo.root / ".aisdlc" / "catalogs" / kind / entry_id / "versions" / f"{version}.json"
        )
        rev = json.loads(path.read_text(encoding="utf-8"))
        rev["summary"] = "tampered-after-pin"
        path.write_text(json.dumps(rev, indent=2, sort_keys=True) + "\n", encoding="utf-8")

        with self.assertRaises(Exception) as raised:
            workflows.transition(
                ctx,
                "feature-delivery",
                "approve",
                owner_override=True,
                reason="should-fail",
            )
        code = getattr(raised.exception, "code", "")
        self.assertEqual("pin_digest_mismatch", code)
        shown = workflows.show(ctx, "feature-delivery")
        self.assertEqual("validated", shown["governance"])

    def test_new_version_returns_to_draft(self):
        from aisdlc import workflows

        ctx = self._ctx()
        workflows.transition(ctx, "feature-delivery", "define")
        updated = workflows.new_version(
            ctx, "feature-delivery", summary="edited body",
        )
        self.assertEqual("draft", updated["governance"])
        self.assertGreater(updated["version"], 1)


if __name__ == "__main__":
    unittest.main()
