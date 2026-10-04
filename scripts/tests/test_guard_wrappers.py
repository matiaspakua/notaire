#!/usr/bin/env python3
"""
Meta-guard (#1209 / CU76): every top-level scripts/test_*.py guard has a wrapper in
scripts/tests/, because CI and preflight only run `unittest discover -s scripts/tests`.
A guard without a wrapper passes by hand and enforces nothing.
"""

from __future__ import annotations

import unittest
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parents[1]
WRAPPERS = Path(__file__).resolve().parent

# Guard -> reason it is deliberately not wired. Add an entry only with a reason.
EXEMPT: dict[str, str] = {}


class GuardWrapperTest(unittest.TestCase):
    def test_every_top_level_guard_has_a_wrapper(self):
        guards = sorted(p.name for p in SCRIPTS.glob("test_*.py"))
        unwrapped = [g for g in guards if g not in EXEMPT and not (WRAPPERS / g).is_file()]
        self.assertEqual([], unwrapped, f"guards CI never runs (add a wrapper in scripts/tests/): {unwrapped}")

    def test_exemptions_name_existing_guards_and_give_a_reason(self):
        for guard, reason in EXEMPT.items():
            with self.subTest(guard=guard):
                self.assertTrue((SCRIPTS / guard).is_file(), "stale exemption")
                self.assertTrue(reason.strip(), "exemption needs a reason")

    def test_every_wrapper_loads_its_guard(self):
        for wrapper in sorted(WRAPPERS.glob("test_*.py")):
            guard = SCRIPTS / wrapper.name
            if wrapper.name == Path(__file__).name or not guard.is_file():
                continue
            with self.subTest(wrapper=wrapper.name):
                self.assertIn(wrapper.name, wrapper.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
