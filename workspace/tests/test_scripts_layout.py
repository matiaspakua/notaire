#!/usr/bin/env python3
"""
Guards issue #1307 (CU76, ADR-026): scripts/ is organized by module. Every moved script exists at
its new path, is gone from the old one, and no tracked file still points at the old path.

Run with: python3 workspace/tests/test_scripts_layout.py
"""
import subprocess
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]

MOVED = {
    "scripts/start.sh": "workspace/stack/start.sh",
    "scripts/stop.sh": "workspace/stack/stop.sh",
    "scripts/logs.sh": "workspace/stack/logs.sh",
    "scripts/start-all.sh": "workspace/stack/start-all.sh",
    "scripts/setup-pgadmin.sh": "workspace/stack/setup-pgadmin.sh",
    "scripts/export-openapi.sh": "backend-api/tools/export-openapi.sh",
    "scripts/generate-coverage-snapshot.py": "backend-api/tools/generate-coverage-snapshot.py",
    "scripts/spotless-fallback.sh": "backend-api/tools/spotless-fallback.sh",
    "scripts/test_generate_coverage_snapshot.py": "backend-api/tools/tests/test_generate_coverage_snapshot.py",
    "scripts/fetch-user-manual.sh": "docs/tools/fetch-user-manual.sh",
    "scripts/generate_e2e_coverage_report.py": "testing/tools/generate_e2e_coverage_report.py",
    "scripts/test_generate_e2e_coverage_report.py": "testing/tests/test_generate_e2e_coverage_report.py",
    "scripts/enable-gh-secure.sh": "security/enable-gh-secure.sh",
    "scripts/test_dependabot_hygiene.py": "security/tests/test_dependabot_hygiene.py",
    "scripts/test_image_pins_and_dependabot.py": "security/tests/test_image_pins_and_dependabot.py",
    "scripts/test_prod_compose.py": "infra/tests/test_prod_compose.py",
    "scripts/test_dev_stack_isolation.py": "infra/tests/test_dev_stack_isolation.py",
    "scripts/preflight.sh": "workspace/sdlc/preflight.sh",
    "scripts/run_pipeline.sh": "workspace/sdlc/run_pipeline.sh",
    "scripts/validate-sdlc-plan.sh": "workspace/sdlc/validate-sdlc-plan.sh",
    "scripts/check-agent-rules.sh": "workspace/sdlc/check-agent-rules.sh",
    "scripts/check-commit-messages.sh": "workspace/sdlc/check-commit-messages.sh",
    "scripts/check-sdlc-exception.sh": "workspace/sdlc/check-sdlc-exception.sh",
    "scripts/check-tdd-evidence.sh": "workspace/sdlc/check-tdd-evidence.sh",
    "scripts/check-heavy-ci.sh": "workspace/sdlc/check-heavy-ci.sh",
    "scripts/seed-openspec-change.sh": "workspace/sdlc/seed-openspec-change.sh",
    "scripts/install-git-hooks.sh": "workspace/sdlc/install-git-hooks.sh",
    "scripts/validate-cu-api-matrix.py": "workspace/sdlc/validate-cu-api-matrix.py",
    "scripts/generate-cd-report.sh": "workspace/ci/generate-cd-report.sh",
    "scripts/generate-markdown-report.sh": "workspace/ci/generate-markdown-report.sh",
    "scripts/generate-pr-validation-report.sh": "workspace/ci/generate-pr-validation-report.sh",
    "scripts/generate-github-page-metrics.sh": "workspace/ci/generate-github-page-metrics.sh",
    "scripts/test_cd_pin_tested_sha.py": "workspace/tests/test_cd_pin_tested_sha.py",
    "scripts/test_ci_workflow_invariants.py": "workspace/tests/test_ci_workflow_invariants.py",
    "scripts/test_dast_contract_backup_assets.py": "workspace/tests/test_dast_contract_backup_assets.py",
    "scripts/test_frontend_ghcr_publish.py": "workspace/tests/test_frontend_ghcr_publish.py",
    "scripts/test_jdk26_toolchain.py": "workspace/tests/test_jdk26_toolchain.py",
    "scripts/test_no_bot_report_commits.py": "workspace/tests/test_no_bot_report_commits.py",
    "scripts/test_no_business_controller.py": "workspace/tests/test_no_business_controller.py",
    "scripts/test_notaire_shared_retired.py": "workspace/tests/test_notaire_shared_retired.py",
    "scripts/test_repo_hygiene.py": "workspace/tests/test_repo_hygiene.py",
    "scripts/test_report_job_needs_dependencies.py": "workspace/tests/test_report_job_needs_dependencies.py",
    "scripts/test_run_pipeline_invariants.py": "workspace/tests/test_run_pipeline_invariants.py",
    "scripts/test_semver_release_process.py": "workspace/tests/test_semver_release_process.py",
    "scripts/tests/test_frontend_eslint_blocking.py": "workspace/tests/test_frontend_eslint_blocking.py",
    "scripts/tests/test_pr_checks.py": "workspace/tests/test_pr_checks.py",
    "scripts/tests/test_validate_cu_api_matrix.py": "workspace/tests/test_validate_cu_api_matrix.py",
    "scripts/tests/test_validate_sdlc_plan.py": "workspace/tests/test_validate_sdlc_plan.py",
    "scripts/tests/test_workflow_concurrency.py": "workspace/tests/test_workflow_concurrency.py",
}
REMOVED = ("scripts/validate-cu-api-matrix.sh", "scripts/tests/test_guard_wrappers.py")
HISTORY = (
    "CHANGELOG.md", "deprecated/", "docs/000-archive/", "docs/openspec/changes/archive/",
    "workspace/tests/test_scripts_layout.py",
)


def tracked_text_files():
    out = subprocess.run(["git", "ls-files", "-z"], cwd=REPO_ROOT, capture_output=True, check=True).stdout
    for name in out.decode().split("\0"):
        path = REPO_ROOT / name
        if name and not name.startswith(HISTORY) and path.is_file():
            try:
                yield name, path.read_text(encoding="utf-8")
            except UnicodeDecodeError:
                continue


class ScriptsLayoutTest(unittest.TestCase):
    def test_the_scripts_folder_is_gone(self):
        self.assertFalse((REPO_ROOT / "scripts").exists(), "scripts/ was emptied into the modules (#1307)")

    def test_moved_scripts_exist_at_their_new_path(self):
        missing = [new for new in MOVED.values() if not (REPO_ROOT / new).is_file()]
        self.assertEqual([], missing)

    def test_moved_scripts_are_gone_from_the_old_path(self):
        left = [old for old in MOVED if (REPO_ROOT / old).exists()]
        self.assertEqual([], left)

    def test_removed_scripts_are_gone(self):
        self.assertEqual([], [p for p in REMOVED if (REPO_ROOT / p).exists()])

    def test_no_tracked_file_references_an_old_path(self):
        stale = [f"{name}: {old}" for name, text in tracked_text_files() for old in (*MOVED, *REMOVED) if old in text]
        self.assertEqual([], stale)


if __name__ == "__main__":
    unittest.main()
