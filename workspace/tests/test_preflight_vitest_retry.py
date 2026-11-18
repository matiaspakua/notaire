#!/usr/bin/env python3
"""preflight retries frontend Vitest once (CU76).

Full-suite runs have failed a single test on a timeout and passed when that
file was run again. The gate should spend one more attempt before failing.
"""

from __future__ import annotations

import unittest
from pathlib import Path

PREFLIGHT = Path(__file__).resolve().parents[2] / "workspace" / "sdlc" / "preflight.sh"


class PreflightVitestRetryTest(unittest.TestCase):
    def test_frontend_vitest_is_retried_once(self) -> None:
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("run_retry()", text)
        self.assertIn(
            'run_retry "frontend vitest" bash -c "cd frontend && ./node_modules/.bin/vitest run"',
            text,
        )
        self.assertIn("failed once; retrying", text)
        self.assertNotIn(
            'run "frontend vitest" bash -c "cd frontend && ./node_modules/.bin/vitest run"',
            text,
        )


if __name__ == "__main__":
    unittest.main()
