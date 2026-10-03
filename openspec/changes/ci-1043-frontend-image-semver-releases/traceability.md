# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1043 | open (implement in progress) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #681 closed (backend only); supersedes frontend part; queue #1046 → #1042 → #1041 → #1040 → #1043 | referenced |
| Specification | `openspec/changes/ci-1043-frontend-image-semver-releases/` | Gate 1 validated on branch |
| Branch | `cursor/ci-1043-frontend-image-semver-69d3` | created |
| Tasks | `tasks.md` | Gate 1 planning complete; implement pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Frontend Dockerfile is the CD build context | `scripts/test_frontend_ghcr_publish.py` | covered |
| Frontend image tags mirror backend conventions | same | covered |
| CycloneDX SBOM generated for frontend image | same | covered |
| Frontend image cosign-signed keyless | same | covered |
| Frontend SBOM attested with cosign | same | covered |
| Automated release workflow exists | `scripts/test_semver_release_process.py` | covered |
| Release process is documented | docs checklist / doc assert in same or PR | covered |
| Unreleased section rolled on release | same (config/script wiring) | covered |
| Maven version matches tag without v prefix | same | covered |
| npm version matches tag without v prefix | same | covered |
| Static validator suite green | both scripts above | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/208-devsecops/README.md` | yes | — |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | — |
| `docs/300-development/RELEASE.md` | yes | — |
| `CHANGELOG.md` | yes | — |
| `README.md` (optional badges) | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder; validate via temp copy into `openspec/changes/` |
| 2 | Failing tests written, test cases designed | yes | red observed on pre-change tree; then green |
| 3 | Suite green, coverage held, docs updated | yes (local) | unittest discover + related CD guards |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. #1040 merged via #1160 (`6e4b86aa`); implement authorized.
Label `in-progress` ACL 403 for integration token.
