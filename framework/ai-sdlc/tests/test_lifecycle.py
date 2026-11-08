"""aisdlc install / update / uninstall lifecycle (#2135)."""
from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from helpers import BIN, ENV, KIT, git, output


def _empty_repo(test: unittest.TestCase) -> Path:
    root = Path(tempfile.mkdtemp(prefix="aisdlc-life-"))
    test.addCleanup(lambda: __import__("shutil").rmtree(root, ignore_errors=True))
    git(root, "init", "-q")
    git(root, "config", "user.email", "dev@example.com")
    git(root, "config", "user.name", "Dev")
    (root / "README.md").write_text("demo\n", encoding="utf-8")
    git(root, "add", "README.md")
    git(root, "commit", "-q", "-m", "init")
    return root


def _run(root: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, str(BIN), "--root", str(root), *args],
        cwd=root,
        env=ENV,
        capture_output=True,
        text=True,
    )


class InstallLifecycleTest(unittest.TestCase):
    def test_install_places_kit_and_scaffold_outside_product_trees(self):
        root = _empty_repo(self)
        # Product trees that must stay untouched even if present empty.
        (root / "frontend").mkdir()
        (root / "backend-api").mkdir()
        (root / "backend-api" / "openapi.yaml").write_text("openapi: 3.0.0\n", encoding="utf-8")
        before_fe = list((root / "frontend").rglob("*"))
        before_api = (root / "backend-api" / "openapi.yaml").read_text(encoding="utf-8")

        result = _run(root, "install", "--path", "tools/ai-sdlc", "--name", "demo", "--level", "1")
        self.assertEqual(0, result.returncode, output(result))
        self.assertTrue((root / "tools" / "ai-sdlc" / "bin" / "aisdlc").is_file())
        self.assertTrue((root / "aisdlc.toml").is_file())
        self.assertTrue((root / "CONSTITUTION.md").is_file())
        self.assertTrue((root / ".aisdlc" / "kit-install.toml").is_file())
        self.assertEqual(before_fe, list((root / "frontend").rglob("*")))
        self.assertEqual(before_api, (root / "backend-api" / "openapi.yaml").read_text(encoding="utf-8"))

        doctor = subprocess.run(
            [sys.executable, str(root / "tools" / "ai-sdlc" / "bin" / "aisdlc"), "--root", str(root), "doctor"],
            cwd=root,
            env=ENV,
            capture_output=True,
            text=True,
        )
        # Fresh scaffold has placeholder gates; doctor may warn but must not crash.
        self.assertIn(doctor.returncode, (0, 1), output(doctor))

    def test_install_refuses_forbidden_destination(self):
        root = _empty_repo(self)
        (root / "frontend").mkdir()
        result = _run(root, "install", "--path", "frontend/ai-sdlc", "--name", "demo")
        self.assertEqual(2, result.returncode, output(result))
        self.assertIn("forbidden", output(result).lower())
        self.assertFalse(any((root / "frontend").rglob("aisdlc")))

    def test_update_is_idempotent(self):
        root = _empty_repo(self)
        self.assertEqual(0, _run(root, "install", "--path", "tools/ai-sdlc", "--name", "demo").returncode)
        marker = root / "tools" / "ai-sdlc" / "aisdlc" / "__init__.py"
        original = marker.read_text(encoding="utf-8")
        # Corrupt a kit file, then update restores it.
        marker.write_text("# corrupted\n", encoding="utf-8")
        first = _run(root, "update", "--path", "tools/ai-sdlc")
        second = _run(root, "update", "--path", "tools/ai-sdlc")
        self.assertEqual(0, first.returncode, output(first))
        self.assertEqual(0, second.returncode, output(second))
        self.assertEqual(original, marker.read_text(encoding="utf-8"))
        # Adopter profile is not rewritten by update.
        profile = (root / "aisdlc.toml").read_text(encoding="utf-8")
        (root / "aisdlc.toml").write_text(profile + "\n# adopter note\n", encoding="utf-8")
        self.assertEqual(0, _run(root, "update", "--path", "tools/ai-sdlc").returncode)
        self.assertIn("# adopter note", (root / "aisdlc.toml").read_text(encoding="utf-8"))

    def test_uninstall_removes_kit_only_unless_purge(self):
        root = _empty_repo(self)
        self.assertEqual(0, _run(root, "install", "--path", "tools/ai-sdlc", "--name", "demo").returncode)
        evidence = root / ".aisdlc" / "evidence"
        evidence.mkdir(parents=True, exist_ok=True)
        (evidence / "ledger.jsonl").write_text("{}\n", encoding="utf-8")

        result = _run(root, "uninstall", "--path", "tools/ai-sdlc")
        self.assertEqual(0, result.returncode, output(result))
        self.assertFalse((root / "tools" / "ai-sdlc").exists())
        self.assertTrue((root / "aisdlc.toml").is_file())
        self.assertTrue((root / "CONSTITUTION.md").is_file())
        self.assertTrue((evidence / "ledger.jsonl").is_file())
        self.assertFalse((root / ".aisdlc" / "kit-install.toml").exists())

        # Reinstall then purge.
        self.assertEqual(0, _run(root, "install", "--path", "tools/ai-sdlc", "--name", "demo", "--force").returncode)
        evidence.mkdir(parents=True, exist_ok=True)
        (evidence / "ledger.jsonl").write_text("{}\n", encoding="utf-8")
        purged = _run(root, "uninstall", "--path", "tools/ai-sdlc", "--purge")
        self.assertEqual(0, purged.returncode, output(purged))
        self.assertFalse((root / "tools" / "ai-sdlc").exists())
        self.assertFalse((root / "aisdlc.toml").exists())
        self.assertFalse((root / "CONSTITUTION.md").exists())
        self.assertFalse((evidence / "ledger.jsonl").exists())


class NotaireLifecycleSmokeTest(unittest.TestCase):
    """Notaire already vendors the kit; update --check / doctor must stay green."""

    def test_notaire_profile_validates_with_lifecycle_module_loaded(self):
        # Source kit is this repository's framework/ai-sdlc; doctor on Notaire profile.
        repo = KIT.parents[1]  # framework/ai-sdlc -> framework -> notaire root when vendored as such
        # When tests run from Notaire checkout, KIT.parents[1] is repo root.
        profile = repo / ".aisdlc" / "aisdlc.toml"
        if not profile.is_file():
            self.skipTest("not running inside Notaire adopter checkout")
        result = subprocess.run(
            [sys.executable, str(BIN), "--profile", str(profile), "doctor"],
            cwd=repo,
            env=ENV,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, output(result))


if __name__ == "__main__":
    unittest.main()
