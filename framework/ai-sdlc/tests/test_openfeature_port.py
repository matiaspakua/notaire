"""OpenFeature port: JSON/flagd file provider; flags cannot weaken invariants (#2137)."""
from __future__ import annotations

import json
import sys
import textwrap
import unittest
from pathlib import Path

from helpers import KIT, Repo, output


class JsonFileProviderTest(unittest.TestCase):
    def setUp(self) -> None:
        sys.path.insert(0, str(KIT))
        from aisdlc.flags import EvaluationContext, JsonFileProvider, evaluate  # noqa: E402

        self.EvaluationContext = EvaluationContext
        self.JsonFileProvider = JsonFileProvider
        self.evaluate = evaluate

    def test_loads_flat_and_flagd_shaped_json(self):
        flat = Path(self._tmp("flat.json"))
        flat.write_text(json.dumps({"experiment.banner": True, "optional.extra": False}), encoding="utf-8")
        provider = self.JsonFileProvider(flat)
        self.assertTrue(self.evaluate(provider, "experiment.banner", False, self.EvaluationContext()).value)

        flagd = Path(self._tmp("flagd.json"))
        flagd.write_text(
            json.dumps(
                {
                    "flags": {
                        "optional.stage": {
                            "state": "ENABLED",
                            "variants": {"on": True, "off": False},
                            "defaultVariant": "off",
                        }
                    }
                }
            ),
            encoding="utf-8",
        )
        provider = self.JsonFileProvider(flagd)
        result = self.evaluate(provider, "optional.stage", True, self.EvaluationContext())
        self.assertFalse(result.value)
        self.assertEqual("STATIC", result.reason)

    def test_missing_or_corrupt_file_returns_default_without_throwing(self):
        provider = self.JsonFileProvider(Path(self._tmp("missing.json")))
        result = self.evaluate(provider, "any", default=42, context=self.EvaluationContext())
        self.assertEqual(42, result.value)
        self.assertEqual("DEFAULT", result.reason)

        bad = Path(self._tmp("bad.json"))
        bad.write_text("{not-json", encoding="utf-8")
        provider = self.JsonFileProvider(bad)
        result = self.evaluate(provider, "any", default="safe", context=self.EvaluationContext())
        self.assertEqual("safe", result.value)
        self.assertEqual("ERROR", result.reason)

    def _tmp(self, name: str) -> str:
        root = Path(self.id().replace(".", "_"))
        # use Repo temp via unittest cleanup
        if not hasattr(self, "_dir"):
            import tempfile, shutil

            self._dir = Path(tempfile.mkdtemp(prefix="flags-"))
            self.addCleanup(shutil.rmtree, self._dir, ignore_errors=True)
        return str(self._dir / name)


class InvariantFlagTest(unittest.TestCase):
    def setUp(self) -> None:
        sys.path.insert(0, str(KIT))
        from aisdlc.flags import (  # noqa: E402
            EvaluationContext,
            InMemoryProvider,
            can_skip_gate,
            clamp_pr_ceiling,
            evaluate,
        )

        self.EvaluationContext = EvaluationContext
        self.InMemoryProvider = InMemoryProvider
        self.can_skip_gate = can_skip_gate
        self.clamp_pr_ceiling = clamp_pr_ceiling
        self.evaluate = evaluate

    def test_flag_cannot_disable_required_gate(self):
        provider = self.InMemoryProvider({"gate.plan.validate.skip": True})
        self.assertFalse(
            self.can_skip_gate(provider, gate_name="plan.validate", required=True, context=self.EvaluationContext())
        )
        # Optional gates may skip when flag is true.
        self.assertTrue(
            self.can_skip_gate(provider, gate_name="plan.validate", required=False, context=self.EvaluationContext())
        )

    def test_flag_cannot_raise_pr_ceiling(self):
        provider = self.InMemoryProvider({"factory.max_open_prs": 99})
        capped, reason = self.clamp_pr_ceiling(provider, ceiling=3, context=self.EvaluationContext())
        self.assertEqual(3, capped)
        self.assertEqual("ERROR", reason)
        # May lower.
        provider = self.InMemoryProvider({"factory.max_open_prs": 1})
        capped, reason = self.clamp_pr_ceiling(provider, ceiling=3, context=self.EvaluationContext())
        self.assertEqual(1, capped)
        self.assertEqual("STATIC", reason)


class FlagsProfileAdoptionTest(unittest.TestCase):
    def test_notaire_style_flags_off_by_default_and_doctor_ok(self):
        repo = Repo(
            self,
            textwrap.dedent(
                """
                [project]
                name = "demo"
                policy = "POLICY.md"

                [factory]
                max_open_prs = 3

                [flags]
                enabled = false
                file = ".aisdlc/flags.json"
                """
            ),
        )
        repo.write("POLICY.md", "# Policy\n")
        repo.write(".aisdlc/flags.json", json.dumps({"gate.plan.skip": True, "factory.max_open_prs": 99}))
        # config validate / doctor should succeed with flags disabled
        shown = repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        data = json.loads(shown.stdout)
        self.assertEqual(3, data["max_open_prs"])

    def test_enabled_flags_still_cannot_raise_ceiling_via_config_show(self):
        repo = Repo(
            self,
            textwrap.dedent(
                """
                [project]
                name = "demo"
                policy = "POLICY.md"

                [factory]
                max_open_prs = 3

                [flags]
                enabled = true
                file = ".aisdlc/flags.json"
                """
            ),
        )
        repo.write("POLICY.md", "# Policy\n")
        repo.write(".aisdlc/flags.json", json.dumps({"factory.max_open_prs": 99}))
        shown = repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        data = json.loads(shown.stdout)
        self.assertEqual(3, data["max_open_prs"])


class RequiredGateSkipCliTest(unittest.TestCase):
    def test_required_gate_still_runs_when_skip_flag_set(self):
        repo = Repo(
            self,
            textwrap.dedent(
                """
                [project]
                name = "demo"
                policy = "POLICY.md"

                [flags]
                enabled = true
                file = ".aisdlc/flags.json"

                [[gates]]
                stage = "plan"
                name = "must-run"
                run = "python3 -c \\"open('RAN','w').write('1')\\""
                required = true
                """
            ),
        )
        repo.write("POLICY.md", "# Policy\n")
        repo.write(".aisdlc/flags.json", json.dumps({"gate.must-run.skip": True}))
        result = repo.run("gate", "plan")
        self.assertEqual(0, result.returncode, output(result))
        self.assertTrue((repo.root / "RAN").is_file(), "required gate must execute despite skip flag")


if __name__ == "__main__":
    unittest.main()
