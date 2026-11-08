> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1964 → CU88; labels and milestone listed in the issue body (token gets 403 on labels/milestone)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not — CU88 created in this PR
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec — `specs/assistant-chat/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — ADR-031 (Proposed)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — after Owner Gate 1 approval (owner step pending)
- [ ] 1.7 Owner approves this specification and decisions D1–D6 (design.md) — Gate 1, owner step pending

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>` — `feat/1964_ai_assistant`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [ ] 3.2 Write the unit tests for every scenario in the delta spec
- [ ] 3.3 Write the integration tests where applicable
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=<NewTestClass>`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

Phase 0 — spike (after Gate 1):

- [ ] 4.1 Import `spring-ai-bom:2.0.2`; verify dependency convergence on Boot 4.1.2 / Java 26 and the security scans
- [ ] 4.2 Spike `ChatClient` + `@Tool` against oMLX with Bonsai, Qwen3-Coder and gpt-oss; record tool-call reliability, TTFT and tokens/s; resolve D1 and the embedding model

Phase 1 — read-only assistant (v0.4):

- [ ] 4.3 Domain types and ports: `AskAssistantUseCase`, `ManageAssistantConversationsUseCase`, `ChatModelPort`, `KnowledgeRetrievalPort`
- [ ] 4.4 `AskAssistantService`: context assembly through inbound ports, PII masking, untrusted-data prompt template, rate limit, input cap
- [ ] 4.5 `adapter.out.ai.SpringAiChatAdapter`: OpenAI-compatible `ChatModel` configured from `NOTAIRE_ASSISTANT_*`, streaming, content observation flags off
- [ ] 4.6 Endpoint guard (local hosts only unless `NOTAIRE_ASSISTANT_ALLOW_REMOTE`) and `assistant` health indicator
- [ ] 4.7 Read-only tools over the case-summary, workflow-trace, required-documents, payments, upcoming-expirations ports and the glossary; `NotaireToolAdvisor` (role check, `SecurityContext` propagation, audit, limits)
- [ ] 4.8 JDBC chat memory (Flyway `V53`) scoped per user + conversation; retention job
- [ ] 4.9 `assistant_interaction` audit through `AuditRecordOpsPort`; `@AuditPersonalDataRead` on personal-data tools
- [ ] 4.10 `AssistantController` (REST + SSE), path rules in `SecurityAndCorsConfig`, regenerated `openapi.yaml`
- [ ] 4.11 BFF `app/api/v1/[...path]` streams `text/event-stream` unbuffered
- [ ] 4.12 Frontend `core/assistant`, `adapters/http/assistant`, `composition`; `AssistantPanel` in the dashboard layout with context chip, suggested prompts, Stop, citations, unavailable state, a11y and i18n
- [ ] 4.13 Grafana "Assistant" dashboard and availability alert in `infra/observability`
- [ ] 4.14 `testing/assistant-evals/` eval set v1 and `run-evals.sh`; record scores

Phase 2 (v0.5) and phase 3 (v0.6) are planned in the issue; their tasks are added here when they start.

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [ ] 7.2 `cd testing/e2e && npx playwright test` — all green
- [ ] 7.3 Verify the affected screens at 320px, 768px and 1024px
- [ ] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [ ] 8.4 Archive superseded documents into `docs/000-archive/`
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Refs #1964` (multi-PR change); only the last PR uses `Closes #1964`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
