# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1044 | open → implement in progress |
| Use Case | CU78 – Security, Privacy and Compliance; CU75 – Database Management and Migrations | exists (updated) |
| Related | audit-2026-09; #254 TLS; #256 backups; #901 production target; after #1047 on main | referenced |
| Specification | `openspec/changes/feat-1044-prod-compose/` | Gate 1 validated |
| Branch | `cursor/feat-1044-prod-compose-69d3` | created from `origin/main` @ `27aaf730` |
| Tasks | `tasks.md` | implement in progress |
| Commits | `50164b4f`, `307b4178`, `19bc3f28`, `947afe02` | pushed |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1155 | draft |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending (coordinator Gate 5 after heavy CI) |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Prod compose exists; no pgAdmin service | `scripts/test_prod_compose.py` | covered |
| Postgres/backend/frontend publish no host ports | same — YAML ports assert | covered |
| Only reverse-proxy publishes host ports | same | covered |
| `ENVIRONMENT=production` on backend | same | covered |
| Secrets use `${VAR:?}` with no insecure defaults | same | covered |
| Least-privilege env (no Grafana/pgAdmin/exporter on backend) | same | covered |
| Flyway baseline-on-migrate disabled in prod | same | covered |
| ProductionCredentialsGuard does not require unused-service defaults | `ProductionCredentialsGuardTest` | covered |
| Deployment guide documents prod compose | `test_deployment_guide_documents_prod_compose` + docs | covered |
| Static validator suite green | `python3 scripts/test_prod_compose.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/209-deployment/README.md` | yes | (this change) |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | yes | (this change) |
| `docs/100-business/102-use-cases/CU75 – Database Management and Migrations.md` | yes | (this change) |
| `.env.example` (prod pointers) | yes | (this change) |
| `CHANGELOG.md` | yes | (this change) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-1044-prod-compose` |
| 2 | Failing tests written, test cases designed | yes | compose tests failed before file existed; guard unused-cred policy tests added |
| 3 | Suite green, coverage held, docs updated | yes (local) | `python3 scripts/test_prod_compose.py`; `ProductionCredentialsGuardTest` |
| 4 | CI green, review approved, no conflicts | pending | heavy CI via `scripts/check-heavy-ci.sh` (coordinator) |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. #1047 squash-merged to main (`27aaf730`) before this implement branch.
