#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_e2e_reliability.py (#1192 / CU76)."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_e2e_reliability.py"
_SPEC = importlib.util.spec_from_file_location("test_e2e_reliability", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(_MOD)

RetryAndArtifactsTest = _MOD.RetryAndArtifactsTest
SleepAndSkipRulesTest = _MOD.SleepAndSkipRulesTest
FeatureGapSkipInventoryTest = _MOD.FeatureGapSkipInventoryTest
InventorySanityTest = _MOD.InventorySanityTest

if __name__ == "__main__":
    unittest.main()
