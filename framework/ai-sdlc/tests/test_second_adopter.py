"""Second-adopter proof: non-Notaire sample adopts via the wheel within 15 minutes (#2256)."""
from __future__ import annotations

import time
import unittest

from conformance import WheelEnv, fill_change, new_repo
from helpers import KIT, output

SAMPLE = KIT / "samples" / "second-adopter"
MAX_SECONDS = 900


class SecondAdopterTest(unittest.TestCase):
    def test_sample_tree_is_non_notaire(self):
        self.assertTrue(SAMPLE.is_dir(), "samples/second-adopter missing")
        readme = (SAMPLE / "README.md").read_text(encoding="utf-8").lower()
        self.assertIn("second", readme)
        blob = "\n".join(
            p.read_text(encoding="utf-8")
            for p in SAMPLE.rglob("*")
            if p.is_file() and p.suffix in {".md", ".toml", ".py", ".txt"}
        ).lower()
        self.assertNotIn("backend-api", blob)
        self.assertNotIn("workspace/sdlc", blob)

    def test_sample_records_quickstart_budget(self):
        evidence = SAMPLE / "QUICKSTART-TIME.txt"
        self.assertTrue(evidence.is_file(), "QUICKSTART-TIME.txt missing")
        text = evidence.read_text(encoding="utf-8").lower()
        self.assertIn("max_seconds=900", text.replace(" ", ""))
        self.assertIn("pypi", (SAMPLE / "README.md").read_text(encoding="utf-8").lower())

    def test_wheel_adoption_reaches_gate_plan_within_fifteen_minutes(self):
        wheel = WheelEnv(self)
        started = time.monotonic()
        root = new_repo(self, "shop")
        installed = wheel.run(root, "install", "--path", "tools/ai-sdlc", "--name", "shop")
        self.assertEqual(0, installed.returncode, output(installed))
        profile = (root / "aisdlc.toml").read_text(encoding="utf-8")
        if "verify =" not in profile:
            profile += '\n[tracker]\nverify = "true"\n'
        (root / "aisdlc.toml").write_text(profile, encoding="utf-8")

        doctor = wheel.run(root, "doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))

        created = wheel.run(root, "change", "new", "quickstart", "--issue", "1", "--title", "Quickstart")
        self.assertEqual(0, created.returncode, output(created))
        change = next(root.rglob("quickstart.md"))
        change.write_text(fill_change(change.read_text(encoding="utf-8")), encoding="utf-8")

        plan = wheel.run(root, "gate", "plan")
        elapsed = time.monotonic() - started
        self.assertEqual(0, plan.returncode, output(plan))
        self.assertLessEqual(elapsed, MAX_SECONDS, f"quickstart took {elapsed:.1f}s (> {MAX_SECONDS})")


if __name__ == "__main__":
    unittest.main()
