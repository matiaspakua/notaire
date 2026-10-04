#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_dev_stack_isolation.py (#1209 / CU76)."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_dev_stack_isolation.py"
_SPEC = importlib.util.spec_from_file_location("test_dev_stack_isolation", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(_MOD)

for _name in dir(_MOD):
    if isinstance(getattr(_MOD, _name), type) and issubclass(getattr(_MOD, _name), unittest.TestCase):
        globals()[_name] = getattr(_MOD, _name)

if __name__ == "__main__":
    unittest.main()
