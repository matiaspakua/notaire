"""Notaire Foreman stage knobs beyond plan (#2215 / CU76).

Adopter assertions against the live Notaire profile. Kit schema unchanged;
these fail closed until `.aisdlc` declares real knobs for ≥4 gate stages.
"""
from __future__ import annotations

import json
import subprocess
import sys
import unittest
from pathlib import Path

from helpers import BIN, ENV, KIT, output


def _notaire_root() -> Path | None:
    repo = KIT.parents[1]
    if (repo / ".aisdlc" / "aisdlc.toml").is_file():
        return repo
    return None


def _run(repo: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, str(BIN), "--profile", str(repo / ".aisdlc" / "aisdlc.toml"), *args],
        cwd=repo,
        env=ENV,
        capture_output=True,
        text=True,
    )


class NotaireForemanStagesTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = _notaire_root()
        if self.repo is None:
            self.skipTest("not running inside Notaire adopter checkout")

    def test_foreman_plan_maps_at_least_four_real_stages(self):
        result = _run(self.repo, "foreman", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertGreaterEqual(data.get("mapped_count", 0), 4, result.stdout)
        ids = {stage["id"] for stage in data.get("stages", [])}
        for required in ("plan", "red", "pre-pr"):
            self.assertIn(required, ids, result.stdout)
        self.assertNotIn("green", ids, "no green knob without a green gate")
        for stage in data["stages"]:
            self.assertTrue(
                stage.get("agents") or stage.get("skills"),
                f"stage {stage.get('id')} needs real agents/skills: {stage}",
            )
            self.assertEqual([], stage.get("mcp") or [], f"no vanity MCP on {stage.get('id')}")
        self.assertFalse(data.get("executes", True))
        self.assertFalse(data.get("sandbox", True))

    def test_remaining_stages_stay_unmapped_honestly(self):
        result = _run(self.repo, "foreman", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        unmapped = set(data.get("unmapped_stages") or [])
        # merge/release/signal remain without knobs in this slice
        for stage_id in ("merge", "release", "signal"):
            self.assertIn(stage_id, unmapped, result.stdout)
        note = (data.get("note") or "").lower()
        self.assertIn("unmapped", note)

    def test_doctor_stays_clean_with_signal_alias(self):
        result = _run(self.repo, "doctor")
        self.assertEqual(0, result.returncode, output(result))
        text = output(result)
        self.assertNotRegex(text, r"WARN:.*stage ['\"]signal['\"].*not canonical")


if __name__ == "__main__":
    unittest.main()
