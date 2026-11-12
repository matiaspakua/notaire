"""Conformance matrix: each stock profile against an installed kit wheel (#2256)."""
from __future__ import annotations

import unittest
from pathlib import Path

from conformance import PROFILES, WheelEnv, adapt_profile, new_repo, seed_change
from helpers import KIT, output


class ConformanceMatrixTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.wheel = WheelEnv(cls)

    def test_stock_profile_files_exist(self):
        for name in PROFILES:
            self.assertTrue((KIT / "profiles" / f"{name}.toml").is_file(), name)

    def test_matrix_rows_exit_zero_through_report(self):
        for name in PROFILES:
            with self.subTest(profile=name):
                self._row(name)

    def _row(self, name: str) -> None:
        root = new_repo(self, name)
        installed = self.wheel.run(root, "install", "--path", "tools/ai-sdlc", "--name", name, "--force")
        self.assertEqual(0, installed.returncode, output(installed))
        stock = (KIT / "profiles" / f"{name}.toml").read_text(encoding="utf-8")
        (root / "aisdlc.toml").write_text(adapt_profile(stock, name), encoding="utf-8")
        (root / "POLICY.md").write_text("# Policy\nGates are absolute.\n", encoding="utf-8")

        doctor = self.wheel.run(root, "doctor")
        self.assertEqual(0, doctor.returncode, f"{name} doctor: {output(doctor)}")

        seed_change(self.wheel, root)

        plan = self.wheel.run(root, "gate", "plan")
        self.assertEqual(0, plan.returncode, f"{name} plan: {output(plan)}")

        red = self.wheel.run(root, "gate", "red", "--var", "test=tests/test_x.py")
        self.assertEqual(0, red.returncode, f"{name} red: {output(red)}")

        report = self.wheel.run(root, "report")
        self.assertEqual(0, report.returncode, f"{name} report: {output(report)}")

        # Fixture identity: the adapted profile still names the stock profile.
        text = (root / "aisdlc.toml").read_text(encoding="utf-8")
        self.assertIn(f'name = "{name}"', text)


if __name__ == "__main__":
    unittest.main()
