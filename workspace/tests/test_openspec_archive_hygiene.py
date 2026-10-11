#!/usr/bin/env python3
"""Gate 2 tests for OpenSpec archive hygiene (#1252, CU76).

Covers every `#### Scenario:` in
`docs/openspec/changes/chore-1252-openspec-archive-hygiene/specs/ai-sdlc-enforcement/spec.md`.

Run: python3 -m unittest workspace.tests.test_openspec_archive_hygiene
"""

from __future__ import annotations

from pathlib import Path

import importlib.util
import os
import shutil
import subprocess
import tempfile
import textwrap
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
CHECKER = os.path.join(REPO_ROOT, "workspace", "sdlc", "check-openspec-archive-hygiene.py")
CHECKER_REL = "workspace/sdlc/check-openspec-archive-hygiene.py"
PREFLIGHT = os.path.join(REPO_ROOT, "workspace", "sdlc", "preflight.sh")
SDLC_PROCESS = os.path.join(REPO_ROOT, ".github", "workflows", "sdlc-process.yml")
MERGE_GATE = os.path.join(
    REPO_ROOT, "docs", "300-development", "304-ai-sdlc-cloud", "CI-MERGE-GATE.md"
)

# Clear outer-repo git env so temp repos are isolated (same as test_pr_checks).
for _var in [v for v in os.environ if v.startswith("GIT_")]:
    del os.environ[_var]


def load_checker():
    spec = importlib.util.spec_from_file_location("check_openspec_archive_hygiene", CHECKER)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod


TASKS_TEMPLATE = textwrap.dedent(
    """\
    ## 1. Gate 1 — Prerequisites

    - [x] 1.1 Issue exists

    ## 2. Create branch

    - [x] 2.1 Branch created

    ## 3. Gate 2 — Write tests

    - [{g3}] 3.1 Test case

    ## 4. Implementation

    - [{g4}] 4.1 Implement checker

    ## 5. Update existing tests

    - [{g5}] 5.1 Regression

    ## 6. Run regression

    - [{g6}] 6.1 Suite green

    ## 7. Run Playwright

    - [{g7}] 7.1 n/a

    ## 8. Gate 3 — Update permanent documentation

    - [{g8}] 8.1 Docs updated

    ## 9. Atomic commits

    - [ ] 9.1 Conventional commits

    ## 10. Pull Request and CI validation

    - [ ] 10.1 Open PR

    ## 11. Deploy

    - [ ] 11.1 Merge via PR

    ## 12. Gate 5 — Smoke and close

    - [ ] 12.1 Close issue
    """
)


def write_change(repo: str, name: str, issue: int, *, impl_ticked: bool) -> None:
    root = os.path.join(repo, "docs", "openspec", "changes", name)
    os.makedirs(root, exist_ok=True)
    mark = "x" if impl_ticked else " "
    with open(os.path.join(root, "proposal.md"), "w", encoding="utf-8") as fh:
        fh.write(
            textwrap.dedent(
                f"""\
                | Field | Value |
                |-------|-------|
                | GitHub Issue | #{issue} |
                | Use Case | CU76 |
                """
            )
        )
    with open(os.path.join(root, "tasks.md"), "w", encoding="utf-8") as fh:
        fh.write(
            TASKS_TEMPLATE.format(
                g3=mark, g4=mark, g5=mark, g6=mark, g7=mark, g8=mark
            )
        )


def git(repo: str, *args: str) -> str:
    return subprocess.run(
        ["git", "-C", repo, *args],
        check=True,
        capture_output=True,
        text=True,
    ).stdout.strip()


def new_repo() -> tuple[str, str]:
    repo = tempfile.mkdtemp(prefix="openspec-hygiene-")
    git(repo, "init", "-q", "-b", "main")
    git(repo, "config", "user.email", "t@example.com")
    git(repo, "config", "user.name", "t")
    git(repo, "commit", "-q", "--allow-empty", "-m", "chore: root")
    return repo, git(repo, "rev-parse", "HEAD")


def commit_all(repo: str, message: str) -> str:
    git(repo, "add", "-A")
    git(repo, "commit", "-q", "-m", message)
    return git(repo, "rev-parse", "HEAD")


def run_checker(repo: str, base: str, *, labels: str = "", extra: list[str] | None = None):
    env = {**os.environ, "PR_LABELS": labels, "PR_AUTHOR": "someone"}
    cmd = ["python3", CHECKER, base]
    if extra:
        cmd.extend(extra)
    return subprocess.run(cmd, cwd=repo, capture_output=True, text=True, env=env)


class InventoryHygieneTest(unittest.TestCase):
    """Scenarios: closed leftover / archived / open WIP."""

    def test_closed_issue_still_active_fails(self):
        mod = load_checker()
        errors = mod.check_inventory(
            active={"feat-shipped": 99},
            issue_states={99: "CLOSED"},
            archived_names=set(),
        )
        self.assertTrue(errors)
        self.assertTrue(any("feat-shipped" in e and "#99" in e for e in errors))

    def test_archived_after_merge_passes(self):
        mod = load_checker()
        errors = mod.check_inventory(
            active={},
            issue_states={99: "CLOSED"},
            archived_names={"2026-10-11-feat-shipped"},
        )
        self.assertEqual(errors, [])

    def test_open_issue_wip_stays_active(self):
        mod = load_checker()
        errors = mod.check_inventory(
            active={"feat-wip": 42},
            issue_states={42: "OPEN"},
            archived_names=set(),
        )
        self.assertEqual(errors, [])


class TaskCompletionGateTest(unittest.TestCase):
    """Scenarios: unticked fail / ticked pass / sdlc-exception bypass."""

    def test_unticked_implementation_task_fails_the_gate(self):
        repo, base = new_repo()
        try:
            write_change(repo, "foo", 1252, impl_ticked=False)
            commit_all(repo, "docs(openspec): plan foo")
            result = run_checker(repo, base)
            self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
            combined = result.stdout + result.stderr
            self.assertIn("foo", combined)
            self.assertRegex(combined, r"3\.\d|4\.\d|group")
        finally:
            shutil.rmtree(repo, ignore_errors=True)

    def test_implementation_groups_complete(self):
        repo, base = new_repo()
        try:
            write_change(repo, "foo", 1252, impl_ticked=True)
            commit_all(repo, "docs(openspec): plan foo")
            result = run_checker(repo, base)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        finally:
            shutil.rmtree(repo, ignore_errors=True)

    def test_exception_label_bypass(self):
        repo, base = new_repo()
        try:
            write_change(repo, "foo", 1252, impl_ticked=False)
            commit_all(repo, "docs(openspec): plan foo")
            result = run_checker(repo, base, labels="sdlc-exception")
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        finally:
            shutil.rmtree(repo, ignore_errors=True)


class MergeGateDocGuardTest(unittest.TestCase):
    """Scenarios: merge-gate names archive verification / missing fails guard."""

    def test_merge_gate_document_names_archive_verification(self):
        mod = load_checker()
        self.assertTrue(os.path.isfile(MERGE_GATE), MERGE_GATE)
        text = Path(MERGE_GATE).read_text(encoding="utf-8")
        errors = mod.check_merge_gate_doc(text)
        self.assertEqual(errors, [], errors)

    def test_missing_instruction_fails_the_doc_guard(self):
        mod = load_checker()
        errors = mod.check_merge_gate_doc("# CI merge gate\n\nHeavy CI only.\n")
        self.assertTrue(errors)


class WiringTest(unittest.TestCase):
    def test_checker_file_exists(self):
        self.assertTrue(os.path.isfile(CHECKER), CHECKER)

    def test_sdlc_process_invokes_checker(self):
        text = Path(SDLC_PROCESS).read_text(encoding="utf-8")
        self.assertIn(CHECKER_REL, text)

    def test_preflight_lists_and_runs_checker(self):
        text = Path(PREFLIGHT).read_text(encoding="utf-8")
        self.assertIn(CHECKER_REL, text)
        self.assertIn("openspec archive hygiene", text.lower())


class SpecScenarioCoverageTest(unittest.TestCase):
    """Every delta-spec Scenario maps to at least one test method name."""

    EXPECTED = {
        "Closed issue still has an active change": "test_closed_issue_still_active_fails",
        "Archived after merge": "test_archived_after_merge_passes",
        "Open issue WIP stays active": "test_open_issue_wip_stays_active",
        "Unticked implementation task fails the gate": "test_unticked_implementation_task_fails_the_gate",
        "Implementation groups complete": "test_implementation_groups_complete",
        "Exception label bypass": "test_exception_label_bypass",
        "Merge-gate document names archive verification": "test_merge_gate_document_names_archive_verification",
        "Missing instruction fails the doc guard": "test_missing_instruction_fails_the_doc_guard",
    }

    def test_every_scenario_has_a_named_test(self):
        methods = {name for name in dir(self) if name.startswith("test_")}
        # Collect from sibling classes
        for cls in (
            InventoryHygieneTest,
            TaskCompletionGateTest,
            MergeGateDocGuardTest,
        ):
            methods |= {n for n in dir(cls) if n.startswith("test_")}
        for scenario, method in self.EXPECTED.items():
            self.assertIn(method, methods, f"missing test for scenario: {scenario}")


if __name__ == "__main__":
    unittest.main()
