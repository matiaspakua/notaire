> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1135 exists and names CU78
- [x] 1.2 Use Case CU78 exists (`docs/100-business/102-use-cases/CU78 – Security and Compliance.md`)
- [x] 1.3 Acceptance Criteria are on Issue #1135 (`skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 n/a — no ADR (CI and repository settings only)
- [ ] 1.6 Issue #1135 labeled in-progress — `gh issue edit` returned HTTP 403 for labels

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/gh-secure-codeql-pipeline-2d5b` created from `origin/main`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 n/a — no delta spec (`skip_specs: true`); checks are listed in design.md
- [x] 3.2 n/a — no unit-testable application code
- [x] 3.3 n/a — no integration-testable application code
- [x] 3.4 Shell syntax, YAML parse, and SDLC plan validation after implementation
- [x] 3.5 n/a — no spec scenarios to map

## 4. Implementación

- [x] 4.1 Add `.github/workflows/codeql.yml` (java-kotlin manual, javascript-typescript, actions)
- [x] 4.2 Add `scripts/enable-gh-secure.sh` (status by default; `--apply` omits code-scanning and branch protection)
- [x] 4.3 Add npm ecosystem to `.github/dependabot.yml` for `/frontend`
- [x] 4.4 Add `SECURITY.md`
- [x] 4.5 Record the GitHub-only CodeQL gate in `scripts/preflight.sh` and the DevSecOps / preflight docs
- [x] 4.6 Add this OpenSpec change folder (`skip_specs: true`)

## 5. Actualizar tests existentes

- [x] 5.1 n/a — no existing test asserts on these workflow files
- [x] 5.2 n/a — no assertions to update
- [x] 5.3 n/a — no obsolete tests

## 6. Ejecutar regresión

- [x] 6.1 n/a — no Java or TypeScript production change; Maven suite not required to prove the workflow file
- [x] 6.2 n/a — coverage floor unchanged
- [x] 6.3 n/a — Checkstyle/SpotBugs inputs unchanged
- [x] 6.4 n/a — no API change
- [x] 6.5 No tests skipped

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface
- [x] 7.4 n/a — no UI surface; the change is CI YAML, a shell script, and docs

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update DevSecOps README, CI-PREFLIGHT, CHANGELOG, SECURITY.md
- [x] 8.2 n/a — no endpoints
- [x] 8.3 CHANGELOG `[Unreleased]`
- [x] 8.4 n/a — nothing archived
- [x] 8.5 DevSecOps README is the pipeline description; the workflow header points at it
- [x] 8.6 `bash scripts/preflight.sh --list` confirms the CodeQL mapping (full `--fix` is a Maven/frontend run and does not execute CodeQL)

## 9. Commits atómicos

- [x] 9.1 One Conventional Commit for the baseline
- [x] 9.2 Commit message ends with `Closes #1135`
- [x] 9.3 No secrets and no unrelated product edits
- [ ] 9.4 Record the commit SHA in `traceability.md` after the commit

## 10. Pull Request y validación CI

- [ ] 10.1 Push `cursor/gh-secure-codeql-pipeline-2d5b`
- [ ] 10.2 Open the PR referencing Issue #1135 and CU78
- [ ] 10.3 Wait for CI, including the new CodeQL workflow
- [ ] 10.4 Gate 4 — CI green and review
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [ ] 11.2 n/a — no image change; CD is unchanged
- [ ] 11.3 Record the merge commit when it exists

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke is the first green CodeQL run on the pull request (SARIF upload)
- [ ] 12.2 Rollback is `git revert` of the merge, as in design.md
- [ ] 12.3 Close Issue #1135 when the pull request merges (`Closes #1135`)
- [ ] 12.4 Archive the change after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1, `skip_specs: true`)
- [x] n/a Gate 2 product tests — no application code; script and YAML checks replace them
- [x] n/a full suite — no production code change
- [x] n/a coverage — ratchet inputs unchanged
- [x] n/a Playwright — no UI surface
- [x] Permanent documentation updated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, CodeQL run green, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
