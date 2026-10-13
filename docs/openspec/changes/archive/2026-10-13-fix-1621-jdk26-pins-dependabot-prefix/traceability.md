# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1621 | closed |
| Use Case | CU78 — Security, Privacy and Compliance | exists |
| Specification | `docs/openspec/changes/fix-1621-jdk26-pins-dependabot-prefix/` | drafted |
| Branch | `fix/1621_jdk26-pins-dependabot-prefix` | created |
| Tasks | `tasks.md` | complete |
| Commits | `5ce9ffca` `9d1a9665` `c3668c64` `52f1ca54` `1c8617c3` `19c1ed21` | done |
| Pull Request | #1625 | merged by Owner |
| CI run | 38429467270 (main) | success |
| Merge commit | `537f1d46` | done |
| Release / tag | next release-please cut | pending |
| Smoke test | CD 38429992159 success; process self-tests green on `main` | done |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Every Dependabot prefix is an allowed type | `ImagePinsAndDependabotTest.test_dependabot_commit_prefixes_are_allowed_conventional_types` | passing |
| Backend docker entry ignores JDK majors | `ImagePinsAndDependabotTest.test_backend_docker_ignores_jdk_major_bumps` | passing |
| Docker bases are pinned JDK 26 (`jdk-toolchain`) | `Jdk26ToolchainTest.test_docker_bases_are_jdk_26_and_pinned` | passing |
| Dockerfile bases minor-or-digest pinned (#1045) | `ImagePinsAndDependabotTest.test_backend_dockerfile_bases_are_pinned` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-017-container-base-images.md` | yes | this PR |
| `docs/200-architecture/208-devsecops/README.md` | yes | this PR |
| `.claude/rules/hooks.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1621; `validate-sdlc-plan.sh` on this change |
| 2 | Failing tests written, test cases designed | yes | `security/tests` 6 FAIL + `workspace/tests` 1 FAIL before; all OK after |
| 3 | Suite green, coverage held, docs updated | yes | 4 self-test suites OK; no Java delta |
| 4 | CI green, review approved, no conflicts | yes | #1625 merged by code owner |
| 5 | Deployed, smoke test passed, Issue closed | yes | CD 38429992159; #1621 closed |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
