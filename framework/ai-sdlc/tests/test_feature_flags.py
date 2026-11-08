"""FeatureFlagPort stub: OpenFeature-shaped hooks; policy keys cannot be relaxed (#2125)."""
from __future__ import annotations

import sys
import unittest

from helpers import KIT


class FeatureFlagPortTest(unittest.TestCase):
    def setUp(self) -> None:
        sys.path.insert(0, str(KIT))
        from aisdlc.flags import EvaluationContext, FlagHooks, InMemoryProvider, evaluate  # noqa: E402

        self.EvaluationContext = EvaluationContext
        self.FlagHooks = FlagHooks
        self.InMemoryProvider = InMemoryProvider
        self.evaluate = evaluate

    def test_evaluation_returns_default_and_reason_without_throwing(self):
        provider = self.InMemoryProvider({"demo.flag": True})
        result = self.evaluate(provider, "demo.flag", default=False, context=self.EvaluationContext(targeting_key="a"))
        self.assertTrue(result.value)
        self.assertEqual("STATIC", result.reason)
        missing = self.evaluate(provider, "missing", default=False, context=self.EvaluationContext())
        self.assertFalse(missing.value)
        self.assertEqual("DEFAULT", missing.reason)

    def test_hooks_run_around_evaluation(self):
        seen: list[str] = []

        class Capture(self.FlagHooks):
            def before(self, flag_key, context):
                seen.append(f"before:{flag_key}")

            def after(self, flag_key, result, context):
                seen.append(f"after:{flag_key}:{result.value}")

            def error(self, flag_key, error, context):
                seen.append(f"error:{flag_key}")

        provider = self.InMemoryProvider({"x": "on"}, hooks=Capture())
        self.evaluate(provider, "x", default="off", context=self.EvaluationContext())
        self.assertEqual(["before:x", "after:x:on"], seen)

    def test_core_and_security_flags_cannot_override_invariants(self):
        provider = self.InMemoryProvider({"core.skip_gate": True, "security.allow_all": True, "ui.theme": "dark"})
        core = self.evaluate(provider, "core.skip_gate", default=False, context=self.EvaluationContext())
        security = self.evaluate(provider, "security.allow_all", default=False, context=self.EvaluationContext())
        ui = self.evaluate(provider, "ui.theme", default="light", context=self.EvaluationContext())
        self.assertFalse(core.value)
        self.assertEqual("ERROR", core.reason)
        self.assertFalse(security.value)
        self.assertEqual("ERROR", security.reason)
        self.assertEqual("dark", ui.value)

    def test_evaluation_never_throws_on_provider_fault(self):
        class Boom(self.InMemoryProvider):
            def resolve(self, flag_key, default, context):
                raise RuntimeError("provider down")

        result = self.evaluate(Boom({}), "any", default=42, context=self.EvaluationContext())
        self.assertEqual(42, result.value)
        self.assertEqual("ERROR", result.reason)


if __name__ == "__main__":
    unittest.main()
