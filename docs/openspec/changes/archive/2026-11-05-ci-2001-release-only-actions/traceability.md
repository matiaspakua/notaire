# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2001 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/ci-2001-release-only-actions/` | drafted |
| Branch | `cursor/ci-2001-release-only-d385` | pushed |
| Tasks | `tasks.md` | groups 1–8 complete |
| Commits | `fce889d7` tests; implementation commit follows | in progress |
| Pull Request | recorded after open | pending |
| CI run | local workflow unit tests; GitHub does not re-run the suite on the PR | passing |
| Merge commit | Owner only | pending |
| Release / tag | not this PR | pending |
| Smoke test | Owner applies ruleset | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Default preflight is not the PR gate | `workspace/sdlc/preflight.sh` help and summary (reviewed with the script) | passing |
| Skipped full suites do not promise a pull-request rerun | `workspace/sdlc/preflight.sh` skip reasons | passing |
| CI workflow trigger | `workspace/tests/test_ci_workflow_invariants.py` | passing |
| Playwright has no weekday cron | `workspace/tests/test_ci_workflow_invariants.py` | passing |
| Process checks have no daily cron | `workspace/tests/test_sdlc_process_on_main.py` | passing |
| CodeQL schedule remains | `workspace/tests/test_ci_workflow_invariants.py` | passing |
| Release Please still runs on main | `workspace/tests/test_release_asset_attachment.py` | passing |
| Pages deploy trigger | `workspace/tests/test_ci_workflow_invariants.py` | passing |
| latest is gated after SHA-tagged push | `workspace/tests/test_cd_pin_tested_sha.py` | passing |
| No workflow_run trigger | `workspace/tests/test_cd_pin_tested_sha.py` | passing |
| Publish SHA is the tag commit | `workspace/tests/test_cd_pin_tested_sha.py` | passing |
| A broken internal link | `workspace/tests/test_pages_site_shell.py` | passing |
| Pages CI is not a pull-request workflow | `workspace/tests/test_pages_site_shell.py` | passing |
| Ruleset lists the five exact check names | `security/tests/test_protect_main_ruleset.py` | passing |
| Aggregator jobs publish those check names | `security/tests/test_protect_main_ruleset.py` | passing |
| Dispatch names the repository without a git checkout | `workspace/tests/test_release_asset_attachment.py` | passing |
| Release creation dispatches delivery on the tag | `workspace/tests/test_release_asset_attachment.py` | passing |
| No release created, no dispatch | `workspace/tests/test_release_asset_attachment.py` | passing |
| Manually pushed tags still publish | `workspace/tests/test_release_asset_attachment.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | yes | this PR |
| `AGENTS.md` | yes | this PR |
| `CHANGELOG.md` | yes | this PR |
| `docs/300-development/CI-PREFLIGHT.md` | yes | this PR |
| `docs/300-development/RELEASE.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2001, this change, `openspec validate` |
| 2 | Failing tests written, test cases designed | yes | workflow guards failed before the edit, then passed |
| 3 | Suite green, coverage held, docs updated | yes | python workflow tests; permanent docs in this PR |
| 4 | Owner review, no conflicts; do not merge | pending | PR open, not merged |
| 5 | Owner applies ruleset, smoke, Issue closed | pending | Owner action #1040 |

## Exceptions

Branch name is `cursor/ci-2001-release-only-d385` because this Cloud Agent run requires that prefix. The Constitution branch form is recorded in the proposal header as the work identity (`#2001`). No `sdlc-exception` label: the OpenSpec change is present.

Issue comments returned 403 for this token. Progress is on the branch and in the pull request body.
