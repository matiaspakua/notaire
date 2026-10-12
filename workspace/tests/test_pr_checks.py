"""Tests for the PR-range process checks run by sdlc-process.yml and preflight.sh."""
import os
import shutil
import subprocess
import tempfile
import unittest

SCRIPTS = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "workspace", "sdlc")

# A git hook (pre-push runs these suites) exports GIT_DIR and friends; they would
# point the temp repos, and the scripts under test, at the outer repository.
for _var in [v for v in os.environ if v.startswith("GIT_")]:
    del os.environ[_var]


def git(repo, *args):
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True, text=True).stdout.strip()


def new_repo():
    repo = tempfile.mkdtemp(prefix="pr-checks-")
    git(repo, "init", "-q", "-b", "main")
    git(repo, "config", "user.email", "t@example.com")
    git(repo, "config", "user.name", "t")
    git(repo, "commit", "-q", "--allow-empty", "-m", "chore: root")
    return repo, git(repo, "rev-parse", "HEAD")


def commit(repo, message, *paths):
    for path in paths:
        full = os.path.join(repo, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "a") as f:
            f.write("x\n")
    if paths:
        git(repo, "add", *paths)
    git(repo, "commit", "-q", "--allow-empty", "-m", message)
    return git(repo, "rev-parse", "--short", "HEAD")


def run(script, *args, cwd, **env):
    return subprocess.run(["bash", os.path.join(SCRIPTS, script), *args], cwd=cwd, capture_output=True,
                          text=True, env={**os.environ, "PR_LABELS": "", "PR_AUTHOR": "someone", **env})


class CommitMessagesTest(unittest.TestCase):
    def test_rejects_non_conventional_subject(self):
        repo, base = new_repo()
        commit(repo, "feat(api): add thing")
        commit(repo, "update stuff")
        result = run("check-commit-messages.sh", base, cwd=repo)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("update stuff", result.stdout)

    def test_accepts_conventional_subjects(self):
        repo, base = new_repo()
        commit(repo, "feat(api): add thing")
        commit(repo, "docs: add CI report for 2026-09-28 [skip ci]")
        commit(repo, "fix!: breaking fix")
        self.assertEqual(run("check-commit-messages.sh", base, cwd=repo).returncode, 0)


    def test_accepts_squash_merge_subjects_with_issue_prefix_and_pr_suffix(self):
        repo, base = new_repo()
        commit(repo, "[#1210] docs(constitution): point the Playwright suite at testing/e2e (#1214)")
        self.assertEqual(run("check-commit-messages.sh", base, cwd=repo).returncode, 0)

    def test_rejects_issue_prefix_without_a_conventional_subject(self):
        repo, base = new_repo()
        commit(repo, "[#1210] update stuff (#1214)")
        self.assertNotEqual(run("check-commit-messages.sh", base, cwd=repo).returncode, 0)


class TddEvidenceTest(unittest.TestCase):
    MAIN = "backend-api/src/main/java/a/Foo.java"
    TEST = "backend-api/src/test/java/a/FooTest.java"

    def test_rejects_production_code_without_tests(self):
        repo, base = new_repo()
        commit(repo, "feat: foo", self.MAIN)
        self.assertNotEqual(run("check-tdd-evidence.sh", base, cwd=repo).returncode, 0)

    def test_accepts_tests_first(self):
        repo, base = new_repo()
        commit(repo, "test: foo red", self.TEST)
        commit(repo, "feat: foo", self.MAIN)
        self.assertEqual(run("check-tdd-evidence.sh", base, cwd=repo).returncode, 0)

    def test_rejects_code_before_tests_and_names_commit(self):
        repo, base = new_repo()
        sha = commit(repo, "feat: foo", self.MAIN)
        commit(repo, "test: foo", self.TEST)
        result = run("check-tdd-evidence.sh", base, cwd=repo)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn(sha, result.stdout)

    def test_ignores_docs_only_ranges(self):
        repo, base = new_repo()
        commit(repo, "docs: readme", "docs/README.md")
        self.assertEqual(run("check-tdd-evidence.sh", base, cwd=repo).returncode, 0)

    def test_exception_label_passes(self):
        repo, base = new_repo()
        commit(repo, "feat: foo", self.MAIN)
        self.assertEqual(run("check-tdd-evidence.sh", base, cwd=repo, PR_LABELS="chore,sdlc-exception").returncode, 0)


class SdlcExceptionTest(unittest.TestCase):
    def test_rejects_pr_without_change_folder_or_label(self):
        repo, base = new_repo()
        commit(repo, "docs: readme", "docs/README.md")
        result = run("check-sdlc-exception.sh", base, cwd=repo)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("§12", result.stdout)

    def test_accepts_labelled_exception(self):
        repo, base = new_repo()
        commit(repo, "docs: readme", "docs/README.md")
        self.assertEqual(run("check-sdlc-exception.sh", base, cwd=repo, PR_LABELS="sdlc-exception").returncode, 0)

    def test_accepts_pr_with_change_folder(self):
        repo, base = new_repo()
        commit(repo, "docs(openspec): plan", "docs/openspec/changes/x/proposal.md")
        self.assertEqual(run("check-sdlc-exception.sh", base, cwd=repo).returncode, 0)

    def test_accepts_a_large_diff_whose_first_path_is_an_openspec_change(self):
        repo, base = new_repo()
        long_name = "x" * 180
        paths = ["docs/openspec/changes/archive/2026-10-04-big/" + f"{index:04d}-{long_name}.md" for index in range(700)]
        commit(repo, "chore(openspec): archive a large change", *paths)
        self.assertEqual(run("check-sdlc-exception.sh", base, cwd=repo).returncode, 0)

    def test_accepts_dependency_bot(self):
        repo, base = new_repo()
        commit(repo, "chore(deps): bump", "frontend/package.json")
        self.assertEqual(run("check-sdlc-exception.sh", base, cwd=repo, PR_AUTHOR="dependabot[bot]").returncode, 0)

    def test_merge_checkout_ignores_openspec_files_that_landed_on_main(self):
        """Actions checks out refs/pull/N/merge. A stale event base SHA must not
        count an OpenSpec path that arrived on main after the PR opened."""
        repo, stale = new_repo()
        commit(repo, "docs(openspec): someone else's plan", "docs/openspec/changes/other/proposal.md")
        git(repo, "checkout", "-q", "-b", "pr", stale)
        commit(repo, "docs: readme", "docs/README.md")
        git(repo, "checkout", "-q", "main")
        git(repo, "merge", "-q", "--no-ff", "pr", "-m", "Merge pr into main")
        result = run("check-sdlc-exception.sh", stale, cwd=repo)
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("sdlc-exception", result.stdout)

    def test_merge_checkout_accepts_an_openspec_change_made_by_the_pr(self):
        repo, stale = new_repo()
        commit(repo, "docs: unrelated main note", "docs/OTHER.md")
        git(repo, "checkout", "-q", "-b", "pr", stale)
        commit(repo, "docs(openspec): plan", "docs/openspec/changes/x/proposal.md")
        git(repo, "checkout", "-q", "main")
        git(repo, "merge", "-q", "--no-ff", "pr", "-m", "Merge pr into main")
        self.assertEqual(run("check-sdlc-exception.sh", stale, cwd=repo).returncode, 0)


class AgentRulesTest(unittest.TestCase):
    def setUp(self):
        self.root = tempfile.mkdtemp(prefix="rules-")
        os.makedirs(os.path.join(self.root, ".claude/rules"))
        os.makedirs(os.path.join(self.root, "docs/real"))

    def write(self, path, text):
        with open(os.path.join(self.root, path), "w") as f:
            f.write(text)

    def test_rejects_empty_rule_file(self):
        self.write(".claude/rules/empty.md", "")
        result = run("check-agent-rules.sh", self.root, cwd=self.root)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("empty.md", result.stdout)

    def test_rejects_dead_path(self):
        self.write(".claude/rules/a.md", "See `docs/real/` and `docs/does-not-exist/x.md`.\n")
        result = run("check-agent-rules.sh", self.root, cwd=self.root)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("docs/does-not-exist/x.md", result.stdout)
        self.assertNotIn("docs/real/", result.stdout)

    def test_ignores_placeholders(self):
        self.write(".claude/rules/a.md", "Use `docs/real/`, `docs/openspec/changes/<name>/`, `docs/*.md`, branch `docs/257_readme`.\n")
        self.assertEqual(run("check-agent-rules.sh", self.root, cwd=self.root).returncode, 0)

    def test_rejects_root_without_rule_files(self):
        result = run("check-agent-rules.sh", self.root, cwd=self.root)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("no agent rule files", result.stdout)

    def _current_stack_refactoring(self):
        return (
            "# Refactoring\n"
            "Package root `com.licensis.notaire`. "
            "Spring Boot 4.1, Java 26, PostgreSQL 16, Next.js frontend. "
            "DTOs named `DtoLoginUser`.\n"
        )

    def test_rejects_obsolete_package_in_refactoring_md(self):
        self.write(".claude/rules/refactoring.md", "Package: com.notaria.backend\n")
        result = run("check-agent-rules.sh", self.root, cwd=self.root)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("com\\.notaria", result.stdout)

    def test_rejects_swing_target_markers_in_refactoring_md(self):
        self.write(
            ".claude/rules/refactoring.md",
            self._current_stack_refactoring() + "Use SwingWorker and JOptionPane.\n",
        )
        result = run("check-agent-rules.sh", self.root, cwd=self.root)
        self.assertNotEqual(result.returncode, 0)
        self.assertTrue(
            "SwingWorker" in result.stdout or "JOptionPane" in result.stdout,
            result.stdout,
        )

    def test_rejects_obsolete_boot_markers_in_refactoring_md(self):
        self.write(
            ".claude/rules/refactoring.md",
            "Spring Boot 3.x with Java 17 and PostgreSQL 15. EntityRequestDTO.\n",
        )
        result = run("check-agent-rules.sh", self.root, cwd=self.root)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("#1070", result.stdout)

    def test_accepts_current_stack_refactoring_md(self):
        self.write(".claude/rules/refactoring.md", self._current_stack_refactoring())
        self.assertEqual(run("check-agent-rules.sh", self.root, cwd=self.root).returncode, 0)


TRACEABILITY = """# Traceability

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/203-design/GESTION.md` | yes | this PR |
| `docs/README.md` | no | — |

## Gate log
"""


def run_doc(base, cwd):
    return subprocess.run(
        ["python3", os.path.join(SCRIPTS, "check-doc-traceability.py"), base],
        cwd=cwd, capture_output=True, text=True,
    )


class DocTraceabilityTest(unittest.TestCase):
    def test_rejects_a_claimed_document_missing_from_the_diff(self):
        repo, base = new_repo()
        path = "docs/openspec/changes/docs-case/traceability.md"
        full = os.path.join(repo, path)
        os.makedirs(os.path.dirname(full))
        with open(full, "w") as handle:
            handle.write(TRACEABILITY)
        commit(repo, "docs(openspec): plan", path)
        result = run_doc(base, repo)
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("GESTION.md", result.stdout)

    def test_accepts_when_every_claimed_document_is_in_the_diff(self):
        repo, base = new_repo()
        trace = "docs/openspec/changes/docs-case/traceability.md"
        doc = "docs/200-architecture/203-design/GESTION.md"
        for path, body in ((trace, TRACEABILITY), (doc, "# Management\n")):
            full = os.path.join(repo, path)
            os.makedirs(os.path.dirname(full), exist_ok=True)
            with open(full, "w") as handle:
                handle.write(body)
        commit(repo, "docs: case analysis", trace, doc)
        self.assertEqual(run_doc(base, repo).returncode, 0)

    def test_ignores_a_diff_with_no_traceability_file(self):
        repo, base = new_repo()
        commit(repo, "docs: readme", "docs/README.md")
        self.assertEqual(run_doc(base, repo).returncode, 0)

    def test_accepts_claimed_path_with_en_dash(self):
        """git quotepath C-quotes en-dashes; claims must still match (CU76)."""
        repo, base = new_repo()
        doc = "docs/100-business/102-use-cases/CU76 – Quality Assurance.md"
        trace = "docs/openspec/changes/docs-case/traceability.md"
        body = (
            "# Traceability\n\n## Permanent documentation updated\n\n"
            "| Document | Updated | Commit |\n"
            "|----------|---------|--------|\n"
            f"| `{doc}` | yes | this PR |\n\n"
            "## Gate log\n"
        )
        for path, content in ((trace, body), (doc, "# CU76\n")):
            full = os.path.join(repo, path)
            os.makedirs(os.path.dirname(full), exist_ok=True)
            with open(full, "w", encoding="utf-8") as handle:
                handle.write(content)
        git(repo, "add", "-A")
        git(repo, "commit", "-q", "-m", "docs: cu76 en-dash")
        self.assertEqual(run_doc(base, repo).returncode, 0, run_doc(base, repo).stdout)


class SchemaLineTest(unittest.TestCase):
    def test_validator_fails_change_without_schema_line(self):
        root = tempfile.mkdtemp(prefix="schema-")
        os.makedirs(os.path.join(root, "workspace", "sdlc"))
        shutil.copy(os.path.join(SCRIPTS, "validate-sdlc-plan.sh"), os.path.join(root, "workspace", "sdlc"))
        change = os.path.join(root, "docs/openspec/changes/no-schema")
        os.makedirs(change)
        with open(os.path.join(change, ".openspec.yaml"), "w") as f:
            f.write('change_id: "no-schema"\n')
        result = subprocess.run(["bash", "workspace/sdlc/validate-sdlc-plan.sh"], cwd=root, capture_output=True, text=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("schema", result.stdout)


if __name__ == "__main__":
    unittest.main()
