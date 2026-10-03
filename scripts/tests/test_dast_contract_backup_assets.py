#!/usr/bin/env python3
"""CI discover wrapper for scripts/test_dast_contract_backup_assets.py (#1067)."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_SRC = Path(__file__).resolve().parents[1] / "test_dast_contract_backup_assets.py"
_SPEC = importlib.util.spec_from_file_location("test_dast_contract_backup_assets", _SRC)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f"cannot load {_SRC}")
_MOD = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(_MOD)

ZapDastWorkflowTest = _MOD.ZapDastWorkflowTest
TrivyRetainedTest = _MOD.TrivyRetainedTest
OpenApiContractTest = _MOD.OpenApiContractTest
BackupRestoreSmokeTest = _MOD.BackupRestoreSmokeTest

if __name__ == "__main__":
    unittest.main()
