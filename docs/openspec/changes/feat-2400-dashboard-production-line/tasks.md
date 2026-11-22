> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case — #2400 → CU76; labels `area:devops`, `priority:high`, `type:feature`
- [x] 1.2 Use Case documentation exists and is accurate — CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec — `specs/factory-dashboard-production-line/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md` — factory-dashboard only
- [ ] 1.5 ADR not required — no architectural change; dashboard remains a client of the kit contract (ADR-006)
- [ ] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 2400` (on 403 print SKIPPED and continue)

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/2400_dashboard_production_line`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from delta spec scenarios (server: DELETE proxy, SSE proxy, SSE cleanup; UI: timeline, intent bar, live sync, config viewer, delete, telemetry, a11y)
- [ ] 3.2 Write server unit tests: `tests/server.test.ts` — DELETE proxy with CSRF/If-Match, SSE proxy forwards Last-Event-ID, SSE cleanup on disconnect
- [ ] 3.3 Write UI hooks tests: `tests/hooks.test.ts` — polling fallback when SSE unavailable
- [ ] 3.4 Run them and **observe them fail** — `cd framework/ai-sdlc/factory-dashboard && pnpm test`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

Group A — Server proxy extensions:

- [ ] 4.1 Add `DELETE` to the proxy route's method list in `src/server/app.ts`; forward `If-Match` header
- [ ] 4.2 Add `callKit` support for `DELETE` method in `src/server/upstream.ts`
- [ ] 4.3 Implement SSE streaming proxy for `/factory/events` in `src/server/app.ts`: open `EventSource` to kit with token, forward `Last-Event-ID` from browser, relay frames, clean up on client disconnect

Group B — UI model types and API helpers:

- [ ] 4.4 Add TypeScript interfaces to `src/ui/model.ts`: `ProductionLine`, `LineStage`, `LineGate`, `LineGuardrails`, `FactoryStatus`, `FactoryStatusStep`, `FactoryStatusNext`, `TelemetryReport`, `TelemetryRow`, `TelemetryRepolens`, `Intent`, `IntentSource`, `IntentReference`, `IntentSubmit`, `IntentList`, `ConfigFile`, `ConfigFileContent`
- [ ] 4.5 Add API helper functions to `src/ui/api.ts` for factory endpoints (line, status, events, intents, telemetry, config files)

Group C — Production line timeline view:

- [ ] 4.6 Create `src/ui/views/ProductionLine.tsx`: horizontal timeline with stations from `/factory/line`, gates between stations as required/advisory pills
- [ ] 4.7 Station expand panel: agents, skills, tools, inputs, outputs, guardrails
- [ ] 4.8 Current-run position overlay from `/factory/status`: highlight current station, show previous/next steps

Group D — Intent bar and drawer:

- [ ] 4.9 Create `src/ui/components/IntentBar.tsx`: always-visible bottom bar with current intent title and status
- [ ] 4.10 Create `src/ui/components/IntentDrawer.tsx`: expandable drawer with full intent text, acceptance criteria, references as links, source
- [ ] 4.11 Intake form in drawer: prompt/reference/spec fields posting to `POST /factory/intents`

Group E — Live sync:

- [ ] 4.12 Create `src/ui/hooks.ts` hook `useLiveSync`: `EventSource` on proxy URL, refresh status/line/intent on events, live indicator state
- [ ] 4.13 Polling fallback (5s interval) when SSE unavailable
- [ ] 4.14 Live indicator dot in sidebar footer

Group F — Config-as-code viewer:

- [ ] 4.15 Update `src/ui/views/Projects.tsx`: file tree from `GET /factory/config/files` with kind badges
- [ ] 4.16 Create `src/ui/components/ConfigViewer.tsx`: read-only viewer with line numbers from `GET /factory/config/files/{path}`

Group G — Catalog delete:

- [ ] 4.17 Add delete button to `src/ui/views/Catalogs.tsx` with inline confirmation dialog (no `window.confirm`)
- [ ] 4.18 Send `DELETE` with `If-Match` header; handle 409 version conflict

Group H — Telemetry panel:

- [ ] 4.19 Update `src/ui/views/Metrics.tsx`: add telemetry panel with time/cost per intent and stage, failures by agent/skill/tool, RepoLens calls

Group I — i18n and accessibility:

- [ ] 4.20 Add en/es copy to `src/ui/i18n.ts` for all new UI strings
- [ ] 4.21 Ensure WCAG AA: text labels on all status indicators, contrast ratios, keyboard navigation

## 5. Actualizar tests existentes

- [ ] 5.1 Update `tests/server.test.ts` — add DELETE proxy tests, SSE proxy tests
- [ ] 5.2 Update existing E2E tests for overview → production-line timeline rename
- [ ] 5.3 No tests made obsolete; overview tests are updated, not removed

## 6. Ejecutar regresión

- [ ] 6.1 `cd framework/ai-sdlc/factory-dashboard && pnpm typecheck`
- [ ] 6.2 `cd framework/ai-sdlc/factory-dashboard && pnpm test` — vitest server tests
- [ ] 6.3 `cd framework/ai-sdlc/factory-dashboard && pnpm build` — production build
- [ ] 6.4 `cd framework/ai-sdlc/factory-dashboard && pnpm e2e` — Playwright + axe
- [ ] 6.5 No skipped tests without documented justification

## 7. Ejecutar Playwright

- [ ] 7.1 All E2E specs from design.md — Playwright Strategy
- [ ] 7.2 `cd framework/ai-sdlc/factory-dashboard && pnpm e2e` — all green
- [ ] 7.3 Verify at 320px, 768px, 1024px
- [ ] 7.4 Axe core passes on all new views

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `framework/ai-sdlc/factory-dashboard/README.md` — views table
- [ ] 8.2 Update `framework/ai-sdlc/factory-dashboard/DESIGN.md` — components section
- [ ] 8.3 Update `docs/openspec/specs/factory-dashboard/spec.md` — add new requirements
- [ ] 8.4 Update `CHANGELOG.md` — Features entry under `[Unreleased]`
- [ ] 8.5 Confirm no information duplicated
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --fix`

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Refs #2400`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change: `cd docs && openspec archive feat-2400-dashboard-production-line --yes`
- [ ] 10.2 `git push -u origin feat/2400_dashboard_production_line`
- [ ] 10.3 Open the PR titled `[#2400] feat(dashboard): production-line timeline, intent bar, live sync, config as code, telemetry`
- [ ] 10.4 Wait for CI workflows to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Open dashboard, sign in, verify production line renders, intent bar visible, SSE connects
- [ ] 12.2 Verify rollback path (git revert safe)
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the vitest floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
