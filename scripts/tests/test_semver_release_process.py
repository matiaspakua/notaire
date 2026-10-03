#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_semver_release_process.py (#1043 / CU76)."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_semver_release_process.py"
_SPEC = importlib.util.spec_from_file_location("test_semver_release_process", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(_MOD)

SemverReleaseProcessTest = _MOD.SemverReleaseProcessTest

if __name__ == "__main__":
    unittest.main()
