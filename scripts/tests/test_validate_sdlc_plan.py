"""Self-tests for OpenSpec plan seeding and leftover-template rejection (#1108)."""

from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
import textwrap
import unittest

REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SCRIPTS = os.path.join(REPO, "workspace", "sdlc")
TEMPLATES = os.path.join(REPO, "docs", "openspec", "schemas", "notaire-sdlc", "templates")


def run_validate(change_dir: str) -> subprocess.CompletedProcess[str]:
    """Run validate-sdlc-plan.sh against a single change directory by name.

    The script expects changes under <repo>/docs/openspec/changes/<name>. We build a
    temporary repo tree that mirrors that layout and copies the script + templates.
    """
    return _run_in_temp_repo("validate", change_dir)


def _run_in_temp_repo(mode: str, change_dir: str | None = None, seed_args: list[str] | None = None):
    root = tempfile.mkdtemp(prefix="sdlc-plan-")
    try:
        changes = os.path.join(root, "docs", "openspec", "changes")
        os.makedirs(changes)
        # Validator and seeder read templates from the repo layout.
        tmpl_dst = os.path.join(root, "docs", "openspec", "schemas", "notaire-sdlc", "templates")
        os.makedirs(os.path.dirname(tmpl_dst), exist_ok=True)
        shutil.copytree(TEMPLATES, tmpl_dst)

        scripts_dst = os.path.join(root, "workspace", "sdlc")
        os.makedirs(scripts_dst)
        for name in ("validate-sdlc-plan.sh", "seed-openspec-change.sh"):
            src = os.path.join(SCRIPTS, name)
            if os.path.isfile(src):
                shutil.copy(src, os.path.join(scripts_dst, name))

        if change_dir:
            name = os.path.basename(change_dir.rstrip("/"))
            dest = os.path.join(changes, name)
            shutil.copytree(change_dir, dest)

        env = {**os.environ, "GH_TOKEN": "", "PATH": os.environ.get("PATH", "")}
        # Force gh unavailable so live Issue checks do not run against GitHub.
        env["PATH"] = "/usr/bin:/bin"

        if mode == "validate":
            name = os.path.basename(change_dir.rstrip("/"))
            return subprocess.run(
                ["bash", os.path.join(scripts_dst, "validate-sdlc-plan.sh"), name],
                cwd=root,
                capture_output=True,
                text=True,
                env=env,
            )
        if mode == "seed":
            return subprocess.run(
                ["bash", os.path.join(scripts_dst, "seed-openspec-change.sh"), *(seed_args or [])],
                cwd=root,
                capture_output=True,
                text=True,
                env=env,
            )
        raise ValueError(mode)
    finally:
        # Caller may still need files; tests that need the tree keep their own root.
        pass


def write_change(root_change: str, files: dict[str, str]) -> str:
    os.makedirs(root_change, exist_ok=True)
    with open(os.path.join(root_change, ".openspec.yaml"), "w") as f:
        f.write("schema: notaire-sdlc\n")
    for rel, body in files.items():
        path = os.path.join(root_change, rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as f:
            f.write(body)
    return root_change


MINIMAL_FILLED = {
    "proposal.md": textwrap.dedent(
        """\
        | Field | Value |
        |-------|-------|
        | GitHub Issue | #1108 |
        | Use Case | CU76 — Quality Assurance |

        ## Objetivo

        Seed templates so agents fill instead of inventing structure.

        ## What Changes

        - Seed script and leftover-comment rejection.

        ## Reglas de negocio

        | Rule | Source | New / Changed / Made explicit |
        |------|--------|-------------------------------|
        | Plans must not keep template HTML comments | CU76 | New |

        ## Capabilities

        ### New Capabilities

        None.

        ### Modified Capabilities

        - `ai-sdlc-enforcement`: seeding and leftover rejection.

        ## Impact Analysis

        ### Módulos afectados

        | Module | Touched | What changes |
        |--------|---------|--------------|
        | `scripts/` | yes | validator and seeder |

        ### Surface area

        - Entities: none

        ### Architecture review

        Tooling only.

        ## Documentation Impact

        | Permanent document | What must change |
        |--------------------|------------------|
        | `docs/openspec/NOTAIRE-ADAPTATIONS.md` | document seed + check |

        ## Out of Scope

        local-ai harness changes.
        """
    ),
    "traceability.md": textwrap.dedent(
        """\
        ## Chain

        Issue → Specification → Tasks → Commits → PR → Merge → Release

        | Link | Reference | Status |
        |------|-----------|--------|
        | Issue | #1108 | open |
        | Use Case | CU76 | exists |
        | Specification | docs/openspec/changes/x/ | writing |
        | Branch | cursor/chore-1108-openspec-template-seed-30a2 | created |
        | Tasks | tasks.md | pending |
        | Commits | pending | pending |
        | Pull Request | pending | pending |
        | CI run | pending | pending |
        | Merge commit | pending | pending |
        | Release / tag | pending | pending |
        | Smoke test | pending | pending |

        ## Requirement coverage

        | Scenario (Acceptance Criterion) | Test | Status |
        |---------------------------------|------|--------|
        | Leftover HTML-comment body rejected | test_validate_sdlc_plan.py | pending |

        ## Permanent documentation updated

        | Document | Updated | Commit |
        |----------|---------|--------|
        | docs/openspec/NOTAIRE-ADAPTATIONS.md | pending | pending |

        ## Gate log

        | Gate | Condition | Passed | Evidence |
        |------|-----------|--------|----------|
        | 1 | Issue + Specification + Acceptance Criteria | yes | this folder |
        | 2 | Failing tests written, test cases designed | pending | pending |
        | 3 | Suite green, coverage held, docs updated | pending | pending |
        | 4 | CI green, review approved, no conflicts | pending | pending |
        | 5 | Deployed, smoke test passed, Issue closed | pending | pending |

        ## Exceptions

        None.
        """
    ),
    "design.md": textwrap.dedent(
        """\
        ## Context

        Agents omit sections when writing from scratch.

        ## Goals / Non-Goals

        **Goals:**
        Seed templates and reject leftovers.

        **Non-Goals:**
        Forking the OpenSpec CLI.

        ## Decisions

        Put the check in validate-sdlc-plan.sh.

        ## Riesgos / Trade-offs

        [False positives on optional empty sections] → Omit optional sections instead.

        ## Testing Strategy

        | Scenario (spec) | Test level | Test class / file |
        |-----------------|------------|-------------------|
        | Leftover HTML-comment body rejected | unit | test_validate_sdlc_plan.py |

        ## Regression Strategy

        - Existing tests affected: none beyond scripts/tests

        ## Playwright Strategy

        n/a - no UI surface

        ## Deployment Strategy

        - Flyway migration required: no

        ## Rollback Strategy

        - Revert safe: yes
        """
    ),
    "tasks.md": textwrap.dedent(
        """\
        ## 1. Gate 1 — Prerequisites

        - [x] 1.1 Issue exists

        ## 2. Crear branch

        - [x] 2.1 Create branch

        ## 3. Gate 2 — Escribir tests

        - [ ] 3.1 Write tests

        ## 4. Implementación

        - [ ] 4.1 Implement leftover rejection
        - [ ] 4.2 Implement seed script

        ## 5. Actualizar tests existentes

        - [ ] 5.1 Review existing tests

        ## 6. Ejecutar regresión

        - [ ] 6.1 Run script tests

        ## 7. Ejecutar Playwright

        - [ ] 7.4 n/a — no UI surface

        ## 8. Gate 3 — Actualizar documentación permanente

        - [ ] 8.1 Update docs

        ## 9. Commits atómicos

        - [ ] 9.1 Commit

        ## 10. Pull Request y validación CI

        - [ ] 10.1 Push and open PR

        ## 11. Deploy

        - [ ] 11.1 Merge via PR

        ## 12. Gate 5 — Smoke test y cierre

        - [ ] 12.1 Smoke test

        ## Definition of Done

        - [ ] Issue linked to a Use Case, with Acceptance Criteria
        - [ ] Specification written and reviewed (Gate 1)
        - [ ] Tests designed and written first, observed failing (Gate 2)
        - [ ] Full suite green: unit, integration, regression, E2E
        - [ ] Coverage at or above the JaCoCo ratchet floor
        - [ ] Playwright E2E green for UI changes
        - [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
        - [ ] Commits atomic and conventional, referencing the Issue
        - [ ] PR created, CI green, review approved (Gate 4)
        - [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
        - [ ] `traceability.md` complete from Issue through Release
        """
    ),
    "specs/ai-sdlc-enforcement/spec.md": textwrap.dedent(
        """\
        ## ADDED Requirements

        ### Requirement: Example

        Text.

        #### Scenario: Example scenario

        - **WHEN** x
        - **THEN** y
        """
    ),
}


class LeftoverCommentRejectionTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="change-")
        self.addCleanup(shutil.rmtree, self.tmp, ignore_errors=True)

    def _validate_files(self, files: dict[str, str]) -> subprocess.CompletedProcess[str]:
        change = write_change(os.path.join(self.tmp, "demo-change"), files)
        # Build a mini-repo for the script
        root = tempfile.mkdtemp(prefix="repo-")
        self.addCleanup(shutil.rmtree, root, ignore_errors=True)
        changes = os.path.join(root, "docs", "openspec", "changes")
        os.makedirs(changes)
        shutil.copytree(change, os.path.join(changes, "demo-change"))
        scripts = os.path.join(root, "workspace", "sdlc")
        os.makedirs(scripts)
        shutil.copy(os.path.join(SCRIPTS, "validate-sdlc-plan.sh"), scripts)
        # Stub gh so live Issue checks are skipped (auth status fails).
        bin_dir = os.path.join(root, "bin")
        os.makedirs(bin_dir)
        gh = os.path.join(bin_dir, "gh")
        with open(gh, "w") as f:
            f.write("#!/bin/sh\nexit 1\n")
        os.chmod(gh, 0o755)
        env = {**os.environ, "PATH": f"{bin_dir}:/usr/bin:/bin"}
        return subprocess.run(
            ["bash", os.path.join(scripts, "validate-sdlc-plan.sh"), "demo-change"],
            cwd=root,
            capture_output=True,
            text=True,
            env=env,
        )

    def test_rejects_leftover_html_comment_body(self):
        files = dict(MINIMAL_FILLED)
        files["proposal.md"] = files["proposal.md"].replace(
            "Seed templates so agents fill instead of inventing structure.",
            "<!-- Why this change is needed -->",
        )
        result = self._validate_files(files)
        self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
        combined = result.stdout + result.stderr
        self.assertIn("proposal.md", combined)
        self.assertIn("Objetivo", combined)

    def test_accepts_filled_section_body(self):
        result = self._validate_files(dict(MINIMAL_FILLED))
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_accepts_scenarios_when_bc_unavailable(self):
        """Gate 1 must not false-fail if bc is missing or broken (Cloud Agents)."""
        files = dict(MINIMAL_FILLED)
        change = write_change(os.path.join(self.tmp, "no-bc-change"), files)
        root = tempfile.mkdtemp(prefix="repo-")
        self.addCleanup(shutil.rmtree, root, ignore_errors=True)
        changes = os.path.join(root, "docs", "openspec", "changes")
        os.makedirs(changes)
        shutil.copytree(change, os.path.join(changes, "no-bc-change"))
        scripts = os.path.join(root, "workspace", "sdlc")
        os.makedirs(scripts)
        shutil.copy(os.path.join(SCRIPTS, "validate-sdlc-plan.sh"), scripts)
        bin_dir = os.path.join(root, "bin")
        os.makedirs(bin_dir)
        # Broken bc first on PATH — old paste|bc path would count 0 and fail Gate 1.
        bc = os.path.join(bin_dir, "bc")
        with open(bc, "w") as f:
            f.write("#!/bin/sh\nexit 1\n")
        os.chmod(bc, 0o755)
        gh = os.path.join(bin_dir, "gh")
        with open(gh, "w") as f:
            f.write("#!/bin/sh\nexit 1\n")
        os.chmod(gh, 0o755)
        env = {**os.environ, "PATH": f"{bin_dir}:/usr/bin:/bin"}
        result = subprocess.run(
            ["bash", os.path.join(scripts, "validate-sdlc-plan.sh"), "no-bc-change"],
            cwd=root,
            capture_output=True,
            text=True,
            env=env,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("scenario", result.stdout.lower())

    def test_rejection_names_file_and_heading_for_design(self):
        files = dict(MINIMAL_FILLED)
        files["design.md"] = files["design.md"].replace(
            "Put the check in validate-sdlc-plan.sh.",
            "<!-- Key design decisions with rationale -->",
        )
        result = self._validate_files(files)
        self.assertNotEqual(result.returncode, 0, result.stdout + result.stderr)
        combined = result.stdout + result.stderr
        self.assertIn("design.md", combined)
        self.assertIn("Decisions", combined)


class SeedOpenspecChangeTest(unittest.TestCase):
    def setUp(self):
        self.root = tempfile.mkdtemp(prefix="seed-repo-")
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        changes = os.path.join(self.root, "docs", "openspec", "changes", "seeded-change")
        os.makedirs(changes)
        with open(os.path.join(changes, ".openspec.yaml"), "w") as f:
            f.write("schema: notaire-sdlc\n")
        tmpl_dst = os.path.join(self.root, "docs", "openspec", "schemas", "notaire-sdlc", "templates")
        shutil.copytree(TEMPLATES, tmpl_dst)
        scripts = os.path.join(self.root, "workspace", "sdlc")
        os.makedirs(scripts)
        seed_src = os.path.join(SCRIPTS, "seed-openspec-change.sh")
        if not os.path.isfile(seed_src):
            self.fail("workspace/sdlc/seed-openspec-change.sh is missing — write it after observing RED")
        shutil.copy(seed_src, scripts)
        self.scripts = scripts
        self.change = changes

    def _seed(self, *args: str) -> subprocess.CompletedProcess[str]:
        return subprocess.run(
            ["bash", os.path.join(self.scripts, "seed-openspec-change.sh"), "seeded-change", *args],
            cwd=self.root,
            capture_output=True,
            text=True,
        )

    def test_seed_copies_four_templates_when_absent(self):
        result = self._seed()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        for name in ("proposal.md", "design.md", "tasks.md", "traceability.md"):
            path = os.path.join(self.change, name)
            self.assertTrue(os.path.isfile(path), f"missing {name}")
            with open(path) as f:
                text = f.read()
            self.assertRegex(text, r"(?m)^## ", msg=f"{name} should keep ## headings")

    def test_seed_leaves_existing_file_alone(self):
        proposal = os.path.join(self.change, "proposal.md")
        with open(proposal, "w") as f:
            f.write("# Filled already\n\n## Objetivo\n\nReal prose.\n")
        with open(proposal) as f:
            before = f.read()
        result = self._seed()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        with open(proposal) as f:
            self.assertEqual(f.read(), before)
        # Other missing files still seeded
        self.assertTrue(os.path.isfile(os.path.join(self.change, "design.md")))

    def test_seed_fills_known_header_values(self):
        result = self._seed(
            "--issue",
            "1108",
            "--use-case",
            "CU76 — Quality Assurance and Testing Infrastructure",
            "--branch",
            "cursor/chore-1108-openspec-template-seed-30a2",
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        with open(os.path.join(self.change, "proposal.md")) as f:
            proposal = f.read()
        with open(os.path.join(self.change, "traceability.md")) as f:
            trace = f.read()
        self.assertIn("#1108", proposal)
        self.assertIn("CU76 — Quality Assurance and Testing Infrastructure", proposal)
        self.assertIn("cursor/chore-1108-openspec-template-seed-30a2", proposal)
        self.assertIn("#1108", trace)
        self.assertIn("CU76 — Quality Assurance and Testing Infrastructure", trace)
        self.assertIn("cursor/chore-1108-openspec-template-seed-30a2", trace)
        self.assertIn("seeded-change", trace)


if __name__ == "__main__":
    unittest.main()
