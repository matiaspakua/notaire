> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. This is a documentation-only change
> (`skip_specs: true`); code-only groups are marked n/a with the reason.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1028 exists, labeled (`requerimiento-no-funcional`, `DOC`, `DEVOPS`, `priority:high`), linked to CU84/CU73
- [x] 1.2 Use Case documentation exists (CU84 – Login, CU73 – Registro de Auditoría); accurate, no update needed
- [x] 1.3 n/a — no delta spec / Acceptance Criteria (`skip_specs: true`); Gate 1 satisfied via proposal.md review
- [x] 1.4 Impact Analysis confirmed in `proposal.md` (no modules touched beyond `docs/`)
- [x] 1.5 No ADR required — documentation only, not an architecture change
- [x] 1.6 Issue moved to IN PROGRESS (`gh issue edit 1028 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b docs/1028_threat_model_auth_audit`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Tests (n/a for this change)

- [x] 3.1–3.5 n/a — documentation artifact, no executable behavior to test.
      Validation is `bash scripts/validate-sdlc-plan.sh` for plan
      completeness and human technical review of threat-model content.

## 4. Implementación

- [x] 4.1 Write `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md`:
      trust-boundary diagram, STRIDE table for login/auth flow, STRIDE table
      for audit-trail flow, `SR-*` requirement IDs with status
- [x] 4.2 Update `docs/200-architecture/206-security/README.md` to link the
      new document and remove the stale "not a formal STRIDE exercise" caveat

## 5. Actualizar tests existentes

- [x] 5.1–5.3 n/a — no code or tests touched by this change

## 6. Ejecutar regresión

- [ ] 6.1 n/a — no backend code touched; skip `mvn test`
- [ ] 6.2 n/a — no coverage impact
- [ ] 6.3 n/a — no code to verify with `mvn verify`
- [ ] 6.4 n/a — no API surface touched
- [x] 6.5 No `@Disabled`/skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1–7.4 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update the two documents listed in `proposal.md` — Documentation Impact
- [x] 8.2 n/a — no endpoints changed, no OpenAPI/Swagger impact
- [x] 8.3 Add `CHANGELOG.md` `[Unreleased]` entry
- [x] 8.4 n/a — nothing superseded/archived
- [x] 8.5 Confirm no duplication between README.md and the new threat-model doc
- [ ] 8.6 `bash scripts/preflight.sh --fix`

## 9. Commits atómicos

- [ ] 9.1 One commit: `docs(security): add STRIDE threat model for auth & audit trail`
- [ ] 9.2 Commit message ends with `Closes #1028`
- [ ] 9.3 No secrets, no unrelated changes
- [ ] 9.4 Record commit SHA in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin docs/1028_threat_model_auth_audit`
- [ ] 10.2 Open PR titled `[#1028] docs: STRIDE threat model for auth & audit trail`
- [ ] 10.3 Wait for required workflows to pass
- [ ] 10.4 Gate 4 — CI green, no merge conflicts, docs complete
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via Pull Request only (left for human review per task instructions)
- [ ] 11.2 n/a — documentation, no image to publish
- [ ] 11.3 Record merge commit in `traceability.md` once merged

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 n/a — no runtime behavior to smoke test
- [ ] 12.2 n/a — no rollback beyond `git revert` (documented in design.md)
- [ ] 12.3 Close Issue #1028 once PR merges (referencing PR)
- [ ] 12.4 `openspec archive threat-model-auth-audit-trail` once merged

## Definition of Done

- [x] Issue linked to Use Case, findings documented
- [x] Plan (proposal/design/tasks/traceability) written — Gate 1
- [x] n/a — no tests to write first (documentation change)
- [ ] Documentation reviewed for accuracy (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green (Gate 4) — left for human review, not merged by this agent
- [ ] `traceability.md` complete from Issue through PR
