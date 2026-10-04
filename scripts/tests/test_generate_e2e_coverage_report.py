#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_generate_e2e_coverage_report.py (#1209 / CU76)."""

from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_generate_e2e_coverage_report.py"
_SPEC = importlib.util.spec_from_file_location("test_generate_e2e_coverage_report", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
sys.path.insert(0, str(_SRC.parent))
try:
    _SPEC.loader.exec_module(_MOD)
finally:
    sys.path.remove(str(_SRC.parent))

for _name in dir(_MOD):
    if isinstance(getattr(_MOD, _name), type) and issubclass(getattr(_MOD, _name), unittest.TestCase):
        globals()[_name] = getattr(_MOD, _name)

if __name__ == "__main__":
    unittest.main()
