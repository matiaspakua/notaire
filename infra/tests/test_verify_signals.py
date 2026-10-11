#!/usr/bin/env python3
"""
Unit tests for infra/scripts/verify_signals.py (issue #1469, CU77).
Spec: docs/openspec/changes/feat-1469-infra-e2e-validation/.

The HTTP layer is injected, so no stack is needed.
Run with: python3 infra/tests/test_verify_signals.py
"""
import importlib.util
import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SCRIPT = os.path.join(REPO_ROOT, "infra", "scripts", "verify_signals.py")


def load_module():
    spec = importlib.util.spec_from_file_location("verify_signals", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class StubStack:
    """Answers each endpoint family with healthy data unless told otherwise."""

    def __init__(self, empty=(), unauthorized=()):
        self.empty = set(empty)
        self.unauthorized = set(unauthorized)

    def __call__(self, kind, query):
        if kind in self.unauthorized:
            return 401, None
        if kind in self.empty or query in self.empty:
            return 200, {"result": []}
        if kind == "prom_targets":
            return 200, {"targets": [{"job": "a", "health": "up"}, {"job": "b", "health": "up"}]}
        if kind == "alertmanagers":
            return 200, {"count": 1}
        if kind == "tempo_search":
            return 200, {"traces": [{"traceID": "abc"}]}
        if kind == "sonar_project":
            return 200, {"component": {"key": query}}
        return 200, {"result": [{"value": "1"}]}


class VerifySignalsTest(unittest.TestCase):
    def setUp(self):
        self.module = load_module()

    def test_all_signals_present_exits_zero(self):
        results = self.module.run_checks(StubStack())
        self.assertTrue(results)
        self.assertEqual({"PASS"}, {r.status for r in results}, [(r.name, r.detail) for r in results])
        self.assertEqual(0, self.module.exit_code(results))

    def test_missing_signal_fails_with_reason(self):
        results = self.module.run_checks(StubStack(empty={"tempo_search"}))
        failed = [r for r in results if r.status == "FAIL"]
        self.assertEqual(["tempo traces"], [r.name for r in failed])
        self.assertIn("no", failed[0].detail.lower())
        self.assertEqual(1, self.module.exit_code(results))

    def test_a_down_target_fails(self):
        def stub(kind, query):
            if kind == "prom_targets":
                return 200, {"targets": [{"job": "backend", "health": "down"}]}
            return StubStack()(kind, query)

        results = self.module.run_checks(stub)
        down = [r for r in results if r.name == "prometheus targets up"][0]
        self.assertEqual("FAIL", down.status)
        self.assertIn("backend", down.detail)

    def test_unauthenticated_sonar_is_skipped(self):
        results = self.module.run_checks(StubStack(unauthorized={"sonar_project"}))
        sonar = [r for r in results if r.name.startswith("sonarqube")]
        self.assertTrue(sonar)
        self.assertEqual({"SKIP"}, {r.status for r in sonar})
        self.assertEqual(0, self.module.exit_code(results))

    def test_unreachable_service_fails_instead_of_crashing(self):
        def stub(kind, query):
            if kind == "loki_query":
                raise OSError("connection refused")
            return StubStack()(kind, query)

        results = self.module.run_checks(stub)
        loki = [r for r in results if r.name.startswith("loki")]
        self.assertEqual({"FAIL"}, {r.status for r in loki})
        self.assertIn("connection refused", loki[0].detail)


if __name__ == "__main__":
    unittest.main()
