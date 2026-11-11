"""The kit builds into a wheel that installs and runs `aisdlc install` + `doctor` offline (#2256).

`python -m build` needs network for its isolated environment, so the wheel is built with
`pip wheel --no-deps --no-build-isolation` and the sdist with setuptools directly.
"""
from __future__ import annotations

import subprocess
import sys
import tarfile
import tempfile
import unittest
import zipfile
from pathlib import Path

from helpers import ENV, KIT, git, output

sys.path.insert(0, str(KIT))
from aisdlc import __version__  # noqa: E402


def run(*cmd: str, cwd: Path | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, cwd=cwd, env=ENV, capture_output=True, text=True)


def build_environment_problem() -> str | None:
    probe = run(sys.executable, "-c", "import setuptools, wheel")
    if probe.returncode != 0:
        return "setuptools/wheel not importable (offline build needs them preinstalled)"
    with tempfile.TemporaryDirectory(prefix="aisdlc-venvprobe-") as tmp:
        venv = run(sys.executable, "-m", "venv", str(Path(tmp) / "v"))
        if venv.returncode != 0:
            return f"venv unavailable: {venv.stderr.strip()[:120]}"
    return None


class PackagingTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        problem = build_environment_problem()
        if problem:
            raise unittest.SkipTest(problem)
        cls.work = Path(tempfile.mkdtemp(prefix="aisdlc-pkg-"))
        cls.addClassCleanup(lambda: __import__("shutil").rmtree(cls.work, ignore_errors=True))
        source = cls.work / "src"
        run("cp", "-r", str(KIT), str(source))
        for junk in ("build", "dist", "aisdlc.egg-info", ".venv-telemetry"):
            run("rm", "-rf", str(source / junk))
        cls.wheel_dir = cls.work / "wheels"
        built = run(sys.executable, "-m", "pip", "wheel", "--no-deps", "--no-build-isolation",
                    "--no-index", "-w", str(cls.wheel_dir), str(source))
        assert built.returncode == 0, output(built)
        cls.wheel = next(cls.wheel_dir.glob("*.whl"))
        sdist = run(sys.executable, "setup.py", "-q", "sdist", "-d", str(cls.work / "sdist"), cwd=source)
        assert sdist.returncode == 0, output(sdist)
        cls.sdist = next((cls.work / "sdist").glob("*.tar.gz"))
        cls.venv = cls.work / "venv"
        assert run(sys.executable, "-m", "venv", str(cls.venv)).returncode == 0
        installed = run(str(cls.venv / "bin" / "python"), "-m", "pip", "install", "--no-deps", "--no-index",
                        str(cls.wheel))
        assert installed.returncode == 0, output(installed)
        cls.aisdlc = str(cls.venv / "bin" / "aisdlc")

    def test_wheel_version_matches_the_package_version(self):
        self.assertTrue(self.wheel.name.startswith(f"aisdlc-{__version__}-"), self.wheel.name)

    def test_installed_console_script_reports_the_version(self):
        result = run(self.aisdlc, "--version")
        self.assertEqual(0, result.returncode, output(result))
        self.assertEqual(f"aisdlc {__version__}", result.stdout.strip())

    def test_install_from_the_wheel_then_doctor_passes(self):
        repo = Path(tempfile.mkdtemp(prefix="aisdlc-adopter-"))
        self.addCleanup(__import__("shutil").rmtree, repo, ignore_errors=True)
        git(repo, "init", "-q", "-b", "main")
        installed = run(self.aisdlc, "--root", str(repo), "install", "--path", "tools/ai-sdlc", "--name", "shop")
        self.assertEqual(0, installed.returncode, output(installed))
        self.assertTrue((repo / "tools/ai-sdlc/bin/aisdlc").is_file())
        self.assertTrue((repo / "tools/ai-sdlc/aisdlc/cli.py").is_file())
        self.assertTrue((repo / "tools/ai-sdlc/templates/project/aisdlc.toml").is_file())
        self.assertFalse((repo / "tools/ai-sdlc/aisdlc/_kit").exists())
        doctor = run(self.aisdlc, "--root", str(repo), "doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        copied = run("python3", str(repo / "tools/ai-sdlc/bin/aisdlc"), "--root", str(repo), "doctor")
        self.assertEqual(0, copied.returncode, output(copied))

    def test_wheel_carries_kit_data_and_no_tests_or_adopter_paths(self):
        names = zipfile.ZipFile(self.wheel).namelist()
        for expected in ("aisdlc/_kit/templates/project/aisdlc.toml", "aisdlc/_kit/profiles/java-maven.toml",
                         "aisdlc/_kit/openapi/factory-control.openapi.yaml", "aisdlc/_kit/bin/aisdlc",
                         "aisdlc/cli.py"):
            self.assertIn(expected, names)
        self.assertEqual([], [n for n in names if "/tests/" in n or n.startswith("tests/")])
        adopter = "notai" + "re"
        self.assertEqual([], [n for n in names if adopter in n.lower()])

    def test_sdist_carries_the_kit_data(self):
        names = tarfile.open(self.sdist).getnames()
        self.assertTrue(any(n.endswith("templates/project/aisdlc.toml") for n in names))
        self.assertTrue(any(n.endswith("pyproject.toml") for n in names))


if __name__ == "__main__":
    unittest.main()
