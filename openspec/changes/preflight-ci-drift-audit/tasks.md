# Tasks — preflight-ci-drift-audit

## 1. Gate 1 — Prerequisites

- [x] GitHub Issue #1029 created with full drift-audit table in body
- [x] Documented Use Case exception (precedent #973) — no Use Case applies
- [x] OpenSpec change scaffolded (`openspec new change`), `skip_specs: true`
- [x] proposal.md, traceability.md, design.md, tasks.md complete

## 2. Crear branch

- [x] `git checkout -b chore/1029_preflight_ci_drift_audit` from updated
      `origin/main`

## 3. Gate 2 — Escribir tests

- [x] N/A — no executable behavior change; verification is manual diff of
      `scripts/preflight.sh --list` output (documented in design.md
      "Testing Strategy")

## 4. Implementación

- [x] Enumerate every job in `.github/workflows/ci.yml`, `pr-validation.yml`,
      `frontend-ci.yml`, `playwright-e2e.yml`
- [x] Diff against `scripts/preflight.sh --list`
- [x] Cross-check JaCoCo floor in `backend-api/pom.xml` vs CONSTITUTION.md
      (confirmed accurate — no change needed)
- [x] Fix `scripts/preflight.sh --list`'s `frontend eslint` row to annotate
      it as advisory-in-CI (`continue-on-error: true`, tracked #701)

## 5. Actualizar tests existentes

- [x] N/A — no existing test suite covers this script's printed text

## 6. Ejecutar regresión

- [x] `bash -n scripts/preflight.sh` (syntax check)
- [x] `bash scripts/preflight.sh --list` (manual output review)
- [ ] `bash scripts/preflight.sh` full run — **limitation**: this sandbox
      does not have a running Docker daemon / local Postgres / frontend dev
      server, so the server-backed suites cannot be exercised here; noted
      honestly in the PR rather than skipped silently, per instructions

## 7. Ejecutar Playwright

- [x] N/A — no UI change

## 8. Gate 3 — Documentación permanente

- [x] `scripts/preflight.sh` is the only permanent-doc-adjacent artifact
      touched; no `docs/` file required correction (JaCoCo floor docs
      already accurate)
- [x] `CHANGELOG.md` — n/a, not user-visible

## 9. Commits atómicos

- [x] Single atomic commit: `chore(scripts): annotate advisory ESLint gate
      in preflight --list`, `Closes #1029`

## 10. Pull Request y validación CI

- [ ] `gh pr create` referencing #1029
- [ ] `gh pr view <number> --json mergeable,mergeStateStatus` verified
      `MERGEABLE`

## 11. Deploy

- [x] N/A — no deployment; merges directly

## 12. Gate 5 — Smoke test y cierre

- [ ] PR merged, issue #1029 closed via `Closes #1029`

## Definition of Done

- [x] Drift audit complete across all four CI workflow files
- [x] JaCoCo floor verified accurate against CONSTITUTION.md — no drift
- [x] One concrete, safe, mechanical drift fixed
      (`scripts/preflight.sh --list` ESLint annotation)
- [x] Judgment call (not downgrading local ESLint to advisory) documented,
      not silently decided
- [ ] PR open, referencing #1029, mergeable
