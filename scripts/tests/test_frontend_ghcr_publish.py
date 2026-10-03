#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_frontend_ghcr_publish.py (#1043 / CU76)."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_frontend_ghcr_publish.py"
_SPEC = importlib.util.spec_from_file_location("test_frontend_ghcr_publish", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(_MOD)

FrontendGhcrPublishTest = _MOD.FrontendGhcrPublishTest

if __name__ == "__main__":
    unittest.main()
