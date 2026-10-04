#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_testing_standalone.py (#1192 / CU76)."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_testing_standalone.py"
_SPEC = importlib.util.spec_from_file_location("test_testing_standalone", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(_MOD)

for _name in dir(_MOD):
    if _name.endswith("Test") and isinstance(getattr(_MOD, _name), type):
        globals()[_name] = getattr(_MOD, _name)

if __name__ == "__main__":
    unittest.main()
