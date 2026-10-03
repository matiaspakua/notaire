#!/usr/bin/env python3
"""
Guards issue #1050 (CU76): repository hygiene for ignore rules, CODEOWNERS,
large user-manual PDF, and ADR-022 history-rewrite decision.

Asserts:
- .gitignore has no unbounded global *.txt rule blocking needed text assets
- testing/e2e-swing/requirements.txt is not ignored by git
- .serena/ is ignored and not tracked
- CODEOWNERS has no frontend-swing path and still covers live modules
- Manual de Usuario PDF is not an ordinary ~13 MB git blob
- ADR-022 exists and records the filter-repo decision

Plain stdlib unittest, consistent with scripts/test_dependabot_hygiene.py.
Run with: python3 scripts/test_repo_hygiene.py

Also discoverable via: python3 -m unittest discover -s scripts/tests
(see scripts/tests/test_repo_hygiene.py).
"""

from __future__ import annotations

import re
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
GITIGNORE = REPO_ROOT / ".gitignore"
CODEOWNERS = REPO_ROOT / ".github" / "CODEOWNERS"
ADR_DIR = REPO_ROOT / "docs" / "200-architecture" / "202-ADR"
ADR_INDEX = ADR_DIR / "README.md"
REQUIREMENTS_TXT = REPO_ROOT / "testing" / "e2e-swing" / "requirements.txt"
USER_MANUAL_DIR = (
    REPO_ROOT
    / "docs"
    / "100-business"
    / "105-manuals"
    / "C_Manual de Usuario"
)
USER_MANUAL_PDF = USER_MANUAL_DIR / "Manual de Usuario Notaire.doc.pdf"
USER_MANUAL_PDF_REL = (
    "docs/100-business/105-manuals/C_Manual de Usuario/"
    "Manual de Usuario Notaire.doc.pdf"
)

# Unbounded top-level rule like `*.txt` (optionally with trailing comment).
GLOBAL_TXT_RULE = re.compile(r"(?m)^\s*\*\.txt\s*(?:#.*)?$")
SERENA_IGNORE = re.compile(r"(?m)^\s*\.serena/?\s*(?:#.*)?$")
SWING_CODEOWNERS = re.compile(r"(?m)^/frontend-swing/")
LIVE_CODEOWNERS = (
    re.compile(r"(?m)^/frontend/"),
    re.compile(r"(?m)^/backend-api/"),
    re.compile(r"(?m)^/notaire-shared/"),
)

# Ordinary blob threshold: the tracked PDF was ~13 MB.
ORDINARY_PDF_BLOB_BYTES = 1_000_000

LFS_POINTER_PREFIX = "version https://git-lfs.github.com/spec/v1"

ADR022_NAME_PREFIX = "ADR-022-"
FILTER_REPO_DECISION = re.compile(
    r"filter-repo|history\s+rewrite|rewrite\s+history",
    re.IGNORECASE,
)
DEFER_OR_DECIDE = re.compile(
    r"\b(defer|deferred|later|follow-?up|never|proceed|rewrite)\b",
    re.IGNORECASE,
)


def _run_git(*args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", *args],
        cwd=REPO_ROOT,
        check=False,
        capture_output=True,
        text=True,
    )


def _gitignore_text() -> str:
    return GITIGNORE.read_text(encoding="utf-8")


def _codeowners_text() -> str:
    return CODEOWNERS.read_text(encoding="utf-8")


def _find_adr022() -> Path | None:
    matches = sorted(ADR_DIR.glob(f"{ADR022_NAME_PREFIX}*.md"))
    return matches[0] if matches else None


def _tracked_paths_under(prefix: str) -> list[str]:
    result = _run_git("ls-files", "--", prefix)
    if result.returncode != 0:
        return []
    return [line for line in result.stdout.splitlines() if line.strip()]


def _is_lfs_pointer(path: Path) -> bool:
    if not path.is_file():
        return False
    # LFS pointers are tiny text files.
    if path.stat().st_size > 1024:
        return False
    head = path.read_text(encoding="utf-8", errors="replace")[:200]
    return head.startswith(LFS_POINTER_PREFIX)


def _pdf_fetch_docs_present() -> bool:
    """True if permanent docs explain Release/LFS fetch for the user manual."""
    candidates = [
        USER_MANUAL_DIR / "README.md",
        USER_MANUAL_DIR / "OBTAINING.md",
        REPO_ROOT / "docs" / "100-business" / "105-manuals" / "README.md",
        REPO_ROOT / "docs" / "300-development" / "301-setup" / "README.md",
        REPO_ROOT / "scripts" / "fetch-user-manual.sh",
    ]
    keywords = re.compile(
        r"(gh\s+release|GitHub\s+Release|git\s+lfs|Manual de Usuario.*\.pdf|"
        r"fetch-user-manual|docs-manuals)",
        re.IGNORECASE | re.DOTALL,
    )
    for path in candidates:
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        if keywords.search(text):
            return True
    return False


class RepoHygieneTest(unittest.TestCase):
    """Static asserts for #1050 repo hygiene (CU76)."""

    def test_gitignore_has_no_global_txt_rule(self) -> None:
        """Scenario: Needed requirements.txt is not ignored (gitignore)."""
        text = _gitignore_text()
        self.assertIsNone(
            GLOBAL_TXT_RULE.search(text),
            ".gitignore must not contain an unbounded global *.txt rule",
        )

    def test_requirements_txt_is_not_ignored(self) -> None:
        """Scenario: Needed requirements.txt is not ignored (check-ignore)."""
        self.assertTrue(
            REQUIREMENTS_TXT.is_file(),
            f"expected {REQUIREMENTS_TXT.relative_to(REPO_ROOT)} to exist",
        )
        result = _run_git(
            "check-ignore",
            "-v",
            "--",
            str(REQUIREMENTS_TXT.relative_to(REPO_ROOT)),
        )
        # Exit 1 = not ignored; exit 0 = ignored (prints matching rule).
        self.assertNotEqual(
            result.returncode,
            0,
            "testing/e2e-swing/requirements.txt must not be gitignored; "
            f"check-ignore reported: {result.stdout.strip()!r}",
        )

    def test_serena_is_ignored(self) -> None:
        """Scenario: .serena is ignored and untracked (ignore rule)."""
        text = _gitignore_text()
        self.assertIsNotNone(
            SERENA_IGNORE.search(text),
            ".gitignore must ignore .serena/",
        )
        # A path under .serena should match the ignore rule.
        probe = ".serena/project.yml"
        result = _run_git("check-ignore", "-v", "--", probe)
        self.assertEqual(
            result.returncode,
            0,
            f"{probe} must be ignored by .gitignore",
        )

    def test_serena_is_untracked(self) -> None:
        """Scenario: .serena is ignored and untracked (index)."""
        tracked = _tracked_paths_under(".serena")
        self.assertEqual(
            tracked,
            [],
            f".serena/ must not be tracked; found: {tracked}",
        )

    def test_codeowners_has_no_frontend_swing(self) -> None:
        """Scenario: CODEOWNERS has no frontend-swing path."""
        text = _codeowners_text()
        self.assertIsNone(
            SWING_CODEOWNERS.search(text),
            "CODEOWNERS must not reference /frontend-swing/",
        )
        for pattern in LIVE_CODEOWNERS:
            self.assertIsNotNone(
                pattern.search(text),
                f"CODEOWNERS must still cover pattern {pattern.pattern}",
            )

    def test_user_manual_pdf_not_ordinary_blob(self) -> None:
        """Scenario: PDF relocated from ordinary blob storage."""
        tracked = _tracked_paths_under(USER_MANUAL_PDF_REL)
        if not tracked:
            self.assertTrue(
                _pdf_fetch_docs_present(),
                "PDF absent from tree but no Release/LFS fetch docs found",
            )
            return

        # Path still tracked: must be an LFS pointer, not a full ~13 MB blob.
        self.assertTrue(
            USER_MANUAL_PDF.is_file() or _is_lfs_pointer(USER_MANUAL_PDF),
            f"tracked PDF path missing on disk: {USER_MANUAL_PDF_REL}",
        )
        size = USER_MANUAL_PDF.stat().st_size if USER_MANUAL_PDF.is_file() else 0
        self.assertTrue(
            _is_lfs_pointer(USER_MANUAL_PDF) or size < ORDINARY_PDF_BLOB_BYTES,
            f"{USER_MANUAL_PDF_REL} is still an ordinary blob "
            f"({size} bytes); use LFS pointer or remove + document Release fetch",
        )
        if _is_lfs_pointer(USER_MANUAL_PDF):
            return
        # Absent-from-tree branch already returned; non-LFS small file is not OK
        # for the historic 13 MB manual — require LFS or removal.
        self.fail(
            f"{USER_MANUAL_PDF_REL} is tracked but is not an LFS pointer; "
            "relocate to Release asset or migrate to Git LFS"
        )

    def test_adr022_documents_filter_repo_decision(self) -> None:
        """Scenario: ADR documents filter-repo decision."""
        adr = _find_adr022()
        self.assertIsNotNone(
            adr,
            f"expected {ADR022_NAME_PREFIX}*.md under {ADR_DIR.relative_to(REPO_ROOT)}",
        )
        assert adr is not None
        text = adr.read_text(encoding="utf-8")
        self.assertRegex(
            text,
            FILTER_REPO_DECISION,
            f"{adr.name} must discuss filter-repo / history rewrite",
        )
        self.assertRegex(
            text,
            DEFER_OR_DECIDE,
            f"{adr.name} must state proceed/defer/never for the rewrite",
        )
        index = ADR_INDEX.read_text(encoding="utf-8")
        self.assertIn(
            "022",
            index,
            "ADR index README must list ADR-022",
        )


if __name__ == "__main__":
    unittest.main()
