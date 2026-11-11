"""Build hook: ships the kit data (templates, profiles, openapi, plugins, optional, bin) as `aisdlc/_kit`.

Everything else is declared in pyproject.toml. Setuptools only, so the build needs no network."""
import shutil
from pathlib import Path

from setuptools import setup
from setuptools.command.build_py import build_py

KIT_DATA = ("templates", "profiles", "openapi", "plugins", "optional", "bin")
HERE = Path(__file__).resolve().parent


class BuildPyWithKitData(build_py):
    def run(self):
        super().run()
        target = Path(self.build_lib) / "aisdlc" / "_kit"
        for name in KIT_DATA:
            shutil.copytree(HERE / name, target / name, dirs_exist_ok=True,
                            ignore=shutil.ignore_patterns("__pycache__", ".venv-telemetry"))


setup(cmdclass={"build_py": BuildPyWithKitData})
