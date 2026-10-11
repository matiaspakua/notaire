#!/usr/bin/env python3
"""The living BFF spec names the current edge file."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVE = ROOT / "docs/openspec/specs/runtime-backend-url-proxy/spec.md"
ARCHIVED = (
    ROOT
    / "docs/openspec/changes/archive/2026-10-04-fix-1055-backend-url-runtime"
    / "specs/runtime-backend-url-proxy/spec.md"
)
RETIRED = "Whether the edge file is `middleware.ts` or `proxy.ts`"


class RuntimeProxyEdgeEnglishTest(unittest.TestCase):
    def test_living_spec_names_proxy_and_archive_keeps_the_old_alternative(self):
        live = LIVE.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn("`frontend/src/proxy.ts`", live)
        self.assertIn("#1056", live)
        self.assertIn("`/api/**` MUST bypass", live)
        self.assertIn("retired `middleware.ts`", live)
        self.assertNotIn(RETIRED, live)
        self.assertIn(RETIRED, archived)
        self.assertIn("#1056", archived)


if __name__ == "__main__":
    unittest.main()
