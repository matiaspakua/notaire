"""PRESERVE goldens for `workflow` and `catalog` CLI output (#2252, A.1 / #2254).

v1 command surfaces stay stable; envelope fields from A.3 (`schema_version`,
`created_at`, `created_by`) are pinned via env when recording.
Re-record only on purpose: AISDLC_RECORD_GOLDENS=1 python3 -m unittest test_cli_goldens
"""
from __future__ import annotations

import json
import os
import unittest

from helpers import ENV, KIT, Repo

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[skills]
dir = "skills"

[catalogs]
dir = ".aisdlc/catalogs"
"""

GOLDENS = KIT / "tests" / "fixtures" / "cli_goldens.json"
OWNER = ("--owner-override", "--reason", "owner decision")

CATALOG_CASES = [
    ("catalog-list-empty", ("catalog", "list")),
    ("catalog-list-kind-json", ("catalog", "list", "--kind", "skills", "--json")),
    ("catalog-add-skill", ("catalog", "add", "--kind", "skills", "--id", "s1", "--ref", "skills/s1", "--summary", "S one")),
    ("catalog-add-agent", ("catalog", "add", "--kind", "agents", "--id", "a1")),
    ("catalog-add-duplicate", ("catalog", "add", "--kind", "skills", "--id", "s1")),
    ("catalog-add-invalid-id", ("catalog", "add", "--kind", "skills", "--id", ".hidden")),
    ("catalog-list-text", ("catalog", "list")),
    ("catalog-list-kind-text", ("catalog", "list", "--kind", "agents")),
    ("catalog-list-json", ("catalog", "list", "--json")),
    ("catalog-show-text", ("catalog", "show", "--kind", "skills", "--id", "s1")),
    ("catalog-show-json", ("catalog", "show", "--kind", "skills", "--id", "s1", "--json")),
    ("catalog-show-missing", ("catalog", "show", "--kind", "skills", "--id", "nope")),
    ("catalog-show-invalid-id", ("catalog", "show", "--kind", "skills", "--id", "a/b")),
    ("catalog-history-text", ("catalog", "history", "--kind", "skills", "--id", "s1")),
    ("catalog-history-json", ("catalog", "history", "--kind", "skills", "--id", "s1", "--json")),
    ("catalog-history-missing", ("catalog", "history", "--kind", "skills", "--id", "nope")),
    ("catalog-update", ("catalog", "update", "--kind", "skills", "--id", "s1", "--summary", "S uno")),
    ("catalog-update-missing", ("catalog", "update", "--kind", "skills", "--id", "nope", "--summary", "x")),
    ("catalog-history-after-update", ("catalog", "history", "--kind", "skills", "--id", "s1")),
    ("catalog-show-legacy", ("catalog", "show", "--kind", "rules", "--id", "legacy", "--json")),
    ("catalog-history-legacy", ("catalog", "history", "--kind", "rules", "--id", "legacy", "--json")),
    ("catalog-list-unreadable", ("catalog", "list", "--kind", "rules", "--json")),
]

WORKFLOW_CASES = [
    ("wf-list-empty", ("workflow", "list")),
    ("wf-list-empty-json", ("workflow", "list", "--json")),
    ("wf-show-missing", ("workflow", "show", "--id", "w1")),
    ("wf-create", ("workflow", "create", "--id", "w1", "--summary", "First", "--agent", "a1", "--skill", "s1")),
    ("wf-create-json", ("workflow", "create", "--id", "w2", "--json", "--agent", "ghost")),
    ("wf-create-duplicate", ("workflow", "create", "--id", "w1")),
    ("wf-create-invalid-id", ("workflow", "create", "--id", "a/b")),
    ("wf-list", ("workflow", "list")),
    ("wf-list-json", ("workflow", "list", "--json")),
    ("wf-list-approved-none", ("workflow", "list", "--approved")),
    ("wf-show-text", ("workflow", "show", "--id", "w1")),
    ("wf-show-json", ("workflow", "show", "--id", "w1", "--json")),
    ("wf-transition-missing", ("workflow", "transition", "--id", "zz", "--action", "define")),
    ("wf-transition-illegal", ("workflow", "transition", "--id", "w1", "--action", "approve")),
    ("wf-define-unknown-ref", ("workflow", "transition", "--id", "w2", "--action", "define")),
    ("wf-define", ("workflow", "transition", "--id", "w1", "--action", "define")),
    ("wf-define-json", ("workflow", "show", "--id", "w1", "--json")),
    ("wf-validate-no-waive", ("workflow", "transition", "--id", "w1", "--action", "request_validation")),
    ("wf-validate-blank-waive", ("workflow", "transition", "--id", "w1", "--action", "request_validation", "--waive-reason", " ")),
    ("wf-validate", ("workflow", "transition", "--id", "w1", "--action", "request_validation", "--waive-reason", "dry run", "--json")),
    ("wf-approve-fail-closed", ("workflow", "transition", "--id", "w1", "--action", "approve")),
    ("wf-approve-override-no-reason", ("workflow", "transition", "--id", "w1", "--action", "approve", "--owner-override")),
    ("wf-approve-bad-signature", ("workflow", "transition", "--id", "w1", "--action", "approve", "--signature", "missing.sig")),
    ("wf-approve-override", ("workflow", "transition", "--id", "w1", "--action", "approve", *OWNER)),
    ("wf-list-approved", ("workflow", "list", "--approved")),
    ("wf-list-approved-json", ("workflow", "list", "--approved", "--json")),
    ("wf-demote-approved", ("workflow", "transition", "--id", "w1", "--action", "demote_approved")),
    ("wf-demote-validated", ("workflow", "transition", "--id", "w1", "--action", "demote_validated")),
    ("wf-reopen", ("workflow", "transition", "--id", "w1", "--action", "reopen", "--json")),
    ("wf-reopen-illegal", ("workflow", "transition", "--id", "w1", "--action", "reopen")),
]


def no_catalogs_cases():
    return [
        ("nocat-catalog-show", ("catalog", "show", "--kind", "skills", "--id", "x")),
        ("nocat-catalog-add", ("catalog", "add", "--kind", "skills", "--id", "x")),
        ("nocat-catalog-list", ("catalog", "list", "--json")),
        ("nocat-wf-create", ("workflow", "create", "--id", "x")),
        ("nocat-wf-list", ("workflow", "list")),
    ]


class CliGoldenTest(unittest.TestCase):
    def setUp(self) -> None:
        self.recorded: dict[str, dict] = {}
        # Pin actor/time so A.3 envelope fields stay byte-stable across runs.
        self._env_prev = {
            "AISDLC_ACTOR": os.environ.get("AISDLC_ACTOR"),
            "AISDLC_CREATED_AT": os.environ.get("AISDLC_CREATED_AT"),
        }
        os.environ["AISDLC_ACTOR"] = "golden"
        os.environ["AISDLC_CREATED_AT"] = "2026-01-01T00:00:00+00:00"

    def tearDown(self) -> None:
        for key, value in self._env_prev.items():
            if value is None:
                os.environ.pop(key, None)
            else:
                os.environ[key] = value

    def _replay(self, repo: Repo, cases) -> None:
        stored = json.loads(GOLDENS.read_text(encoding="utf-8")) if GOLDENS.is_file() else {}
        record = bool(os.environ.get("AISDLC_RECORD_GOLDENS"))
        # Ensure Repo.run inherits pinned actor/time.
        for key in ("AISDLC_ACTOR", "AISDLC_CREATED_AT"):
            ENV[key] = os.environ[key]
        for name, args in cases:
            with self.subTest(name):
                result = repo.run(*args)
                got = {
                    "rc": result.returncode,
                    "stdout": result.stdout.replace(str(repo.root), "<ROOT>"),
                    "stderr": result.stderr.replace(str(repo.root), "<ROOT>"),
                }
                if record:
                    self.recorded[name] = got
                else:
                    self.assertIn(name, stored)
                    self.assertEqual(stored[name], got)
        if record:
            stored.update(self.recorded)
            GOLDENS.parent.mkdir(parents=True, exist_ok=True)
            GOLDENS.write_text(json.dumps(stored, indent=1, sort_keys=True) + "\n", encoding="utf-8")

    def test_catalog_cli_is_byte_stable(self) -> None:
        repo = Repo(self, PROFILE)
        (repo.root / "skills" / "s1").mkdir(parents=True)
        repo.write("skills/s1/SKILL.md", "---\nname: s1\ndescription: Demo\n---\n")
        legacy = repo.root / ".aisdlc" / "catalogs" / "rules"
        legacy.mkdir(parents=True)
        (legacy / "legacy.json").write_text('{"id": "legacy", "summary": "old"}\n', encoding="utf-8")
        (legacy / "broken.json").write_text("{not json", encoding="utf-8")
        self._replay(repo, CATALOG_CASES)

    def test_workflow_cli_is_byte_stable(self) -> None:
        repo = Repo(self, PROFILE)
        repo.run("catalog", "add", "--kind", "agents", "--id", "a1")
        repo.run("catalog", "add", "--kind", "skills", "--id", "s1")
        self._replay(repo, WORKFLOW_CASES)

    def test_missing_catalogs_config_is_byte_stable(self) -> None:
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        self._replay(repo, no_catalogs_cases())


if __name__ == "__main__":
    unittest.main()
