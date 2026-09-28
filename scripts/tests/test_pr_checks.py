"""Tests for the PR-range process checks run by pr-validation.yml and preflight.sh."""
import os
import shutil
import subprocess
import tempfile
import unittest

SCRIPTS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


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
        commit(repo, "docs(openspec): plan", "openspec/changes/x/proposal.md")
        self.assertEqual(run("check-sdlc-exception.sh", base, cwd=repo).returncode, 0)

    def test_accepts_dependency_bot(self):
        repo, base = new_repo()
        commit(repo, "chore(deps): bump", "frontend/package.json")
        self.assertEqual(run("check-sdlc-exception.sh", base, cwd=repo, PR_AUTHOR="dependabot[bot]").returncode, 0)


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
        self.write(".claude/rules/a.md", "Use `docs/real/` and `openspec/changes/<name>/` and `docs/*.md`.\n")
        self.assertEqual(run("check-agent-rules.sh", self.root, cwd=self.root).returncode, 0)


class SchemaLineTest(unittest.TestCase):
    def test_validator_fails_change_without_schema_line(self):
        root = tempfile.mkdtemp(prefix="schema-")
        os.makedirs(os.path.join(root, "scripts"))
        shutil.copy(os.path.join(SCRIPTS, "validate-sdlc-plan.sh"), os.path.join(root, "scripts"))
        change = os.path.join(root, "openspec/changes/no-schema")
        os.makedirs(change)
        with open(os.path.join(change, ".openspec.yaml"), "w") as f:
            f.write('change_id: "no-schema"\n')
        result = subprocess.run(["bash", "scripts/validate-sdlc-plan.sh"], cwd=root, capture_output=True, text=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("schema", result.stdout)


if __name__ == "__main__":
    unittest.main()
