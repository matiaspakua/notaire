#!/usr/bin/env python3
"""
Guards issue #1046 (CU78): Dependabot hygiene for dead Swing log4j and smol-toml.

Asserts:
- deprecated-frontend-swing/ and frontend-swing/ are absent
- no log4j:log4j (Log4j 1.x) in tracked Maven POMs
- frontend/package.json overrides smol-toml to a patched range
- frontend/package-lock.json resolves smol-toml >= 1.7.1
- CODEOWNERS / root README do not present Swing paths as live modules

Plain stdlib unittest (JSON), consistent with scripts/test_prod_compose.py.
Run with: python3 scripts/test_dependabot_hygiene.py

Also discoverable via: python3 -m unittest discover -s scripts/tests
(see scripts/tests/test_dependabot_hygiene.py).
"""

from __future__ import annotations

import json
import os
import re
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
FRONTEND_PACKAGE_JSON = REPO_ROOT / "frontend" / "package.json"
FRONTEND_LOCKFILE = REPO_ROOT / "frontend" / "package-lock.json"
CODEOWNERS = REPO_ROOT / ".github" / "CODEOWNERS"
README = REPO_ROOT / "README.md"
ROOT_POM = REPO_ROOT / "pom.xml"

DEPRECATED_SWING = REPO_ROOT / "deprecated-frontend-swing"
ACTIVE_SWING = REPO_ROOT / "frontend-swing"

# GHSA-7w5x-hrqm-74c2: vulnerable <=1.7.0; first patched 1.7.1.
# #1046 AC still requires override to latest (prefer 1.9.x) even though 1.8.0
# already clears the advisory floor.
MIN_SMOL_TOML = (1, 7, 1)
PREFERRED_SMOL_TOML = (1, 9, 0)

LOG4J_1X_PATTERNS = (
    re.compile(r"log4j\s*:\s*log4j\b"),
    re.compile(
        r"<groupId>\s*log4j\s*</groupId>\s*<artifactId>\s*log4j\s*</artifactId>",
        re.IGNORECASE | re.DOTALL,
    ),
)

SWING_CODEOWNERS_PATHS = (
    re.compile(r"(?m)^/frontend-swing/"),
    re.compile(r"(?m)^/deprecated-frontend-swing/"),
)

# Tree listing lines that present the deprecated dir as an on-disk module
README_PRESENT_TREE = re.compile(
    r"(?m)^\s*├──\s+deprecated-frontend-swing/"
)


def parse_semver(version: str) -> tuple[int, int, int]:
    """Parse leading major.minor.patch from a version string."""
    match = re.match(r"^v?(\d+)\.(\d+)\.(\d+)", version.strip())
    if not match:
        raise ValueError(f"unparseable semver: {version!r}")
    return int(match.group(1)), int(match.group(2)), int(match.group(3))


def override_forces_patched_smol_toml(overrides: dict) -> bool:
    """True if overrides pin smol-toml to >=1.7.1 (exact, caret, or >=)."""
    if not isinstance(overrides, dict):
        return False
    value = overrides.get("smol-toml")
    if value is None:
        return False
    if not isinstance(value, str):
        return False
    text = value.strip()
    if text.startswith("^"):
        return parse_semver(text[1:]) >= MIN_SMOL_TOML
    if text.startswith(">="):
        return parse_semver(text[2:].lstrip()) >= MIN_SMOL_TOML
    if text.startswith("~"):
        return parse_semver(text[1:]) >= MIN_SMOL_TOML
    return parse_semver(text) >= MIN_SMOL_TOML


def lockfile_smol_toml_versions(lock: dict) -> list[str]:
    """Collect resolved smol-toml versions from package-lock.json (v2/v3)."""
    versions: list[str] = []
    packages = lock.get("packages") or {}
    for key, meta in packages.items():
        if not isinstance(meta, dict):
            continue
        # Keys look like "node_modules/smol-toml"
        if key == "node_modules/smol-toml" or key.endswith("/node_modules/smol-toml"):
            ver = meta.get("version")
            if isinstance(ver, str):
                versions.append(ver)
    # Legacy "dependencies" tree (lockfileVersion 1 / nested)
    deps = lock.get("dependencies") or {}
    if isinstance(deps, dict) and "smol-toml" in deps:
        ver = deps["smol-toml"].get("version") if isinstance(deps["smol-toml"], dict) else None
        if isinstance(ver, str):
            versions.append(ver)
    return versions


def tracked_pom_files() -> list[Path]:
    """Return pom.xml files under the repo, excluding .git and node_modules."""
    poms: list[Path] = []
    for root, dirs, files in os.walk(REPO_ROOT):
        dirs[:] = [
            d
            for d in dirs
            if d not in {".git", "node_modules", "target", ".next", "dist"}
        ]
        if "pom.xml" in files:
            poms.append(Path(root) / "pom.xml")
    return poms


def pom_declares_log4j_1x(text: str) -> bool:
    return any(pattern.search(text) for pattern in LOG4J_1X_PATTERNS)


class DependabotHygieneTest(unittest.TestCase):
    """Dependabot alert hygiene for issue #1046 / CU78."""

    def test_deprecated_frontend_swing_directory_absent(self):
        self.assertFalse(
            DEPRECATED_SWING.exists(),
            "deprecated-frontend-swing/ must be deleted (#1046); "
            f"still present at {DEPRECATED_SWING}",
        )

    def test_active_frontend_swing_directory_absent(self):
        self.assertFalse(
            ACTIVE_SWING.exists(),
            "frontend-swing/ must not be reintroduced (#1046); "
            f"found at {ACTIVE_SWING}",
        )

    def test_root_pom_has_no_frontend_swing_module(self):
        self.assertTrue(ROOT_POM.is_file(), ROOT_POM)
        text = ROOT_POM.read_text(encoding="utf-8")
        self.assertNotRegex(
            text,
            r"<module>\s*frontend-swing\s*</module>",
            "root pom.xml must not declare frontend-swing module (#1046)",
        )
        self.assertNotRegex(
            text,
            r"<module>\s*deprecated-frontend-swing\s*</module>",
            "root pom.xml must not declare deprecated-frontend-swing module (#1046)",
        )

    def test_no_log4j_log4j_in_tracked_poms(self):
        offenders: list[str] = []
        for pom in tracked_pom_files():
            text = pom.read_text(encoding="utf-8")
            if pom_declares_log4j_1x(text):
                offenders.append(str(pom.relative_to(REPO_ROOT)))
        self.assertEqual(
            offenders,
            [],
            "tracked pom.xml must not declare log4j:log4j (Log4j 1.x) (#1046); "
            f"found in: {offenders}",
        )

    def test_package_json_declares_smol_toml_override(self):
        self.assertTrue(FRONTEND_PACKAGE_JSON.is_file(), FRONTEND_PACKAGE_JSON)
        data = json.loads(FRONTEND_PACKAGE_JSON.read_text(encoding="utf-8"))
        overrides = data.get("overrides")
        self.assertIsInstance(
            overrides,
            dict,
            "frontend/package.json must declare npm overrides for smol-toml (#1046)",
        )
        self.assertTrue(
            override_forces_patched_smol_toml(overrides),
            "overrides.smol-toml must force >=1.7.1 (prefer ^1.9.0) (#1046); "
            f"got {overrides.get('smol-toml')!r}",
        )

    def test_lockfile_resolves_patched_smol_toml(self):
        self.assertTrue(FRONTEND_LOCKFILE.is_file(), FRONTEND_LOCKFILE)
        lock = json.loads(FRONTEND_LOCKFILE.read_text(encoding="utf-8"))
        versions = lockfile_smol_toml_versions(lock)
        self.assertTrue(
            versions,
            "frontend/package-lock.json must contain a smol-toml package entry (#1046)",
        )
        for version in versions:
            parsed = parse_semver(version)
            self.assertGreaterEqual(
                parsed,
                MIN_SMOL_TOML,
                f"smol-toml@{version} is below patched floor 1.7.1 "
                f"(GHSA-7w5x-hrqm-74c2) (#1046)",
            )
            # Prefer overridden 1.9.x per #1046 design (markdownlint still pins 1.8.0)
            self.assertGreaterEqual(
                parsed,
                PREFERRED_SMOL_TOML,
                f"smol-toml@{version} must resolve to >=1.9.0 via npm overrides (#1046)",
            )

    def test_codeowners_has_no_live_swing_path(self):
        self.assertTrue(CODEOWNERS.is_file(), CODEOWNERS)
        text = CODEOWNERS.read_text(encoding="utf-8")
        for pattern in SWING_CODEOWNERS_PATHS:
            self.assertIsNone(
                pattern.search(text),
                "CODEOWNERS must not own removed Swing paths "
                f"({pattern.pattern}) (#1046)",
            )

    def test_readme_does_not_list_deprecated_swing_as_present(self):
        self.assertTrue(README.is_file(), README)
        text = README.read_text(encoding="utf-8")
        self.assertIsNone(
            README_PRESENT_TREE.search(text),
            "README.md tree must not list deprecated-frontend-swing/ as present (#1046)",
        )
        # Live markdown link to the on-disk path also implies presence
        self.assertNotRegex(
            text,
            r"\]\(deprecated-frontend-swing/",
            "README.md must not link to deprecated-frontend-swing/ as a live path (#1046)",
        )


if __name__ == "__main__":
    unittest.main()
