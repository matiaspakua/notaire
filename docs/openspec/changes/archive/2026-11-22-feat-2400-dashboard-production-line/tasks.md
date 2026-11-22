> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case — #2400 → CU76; labels `area:devops`, `priority:high`, `type:feature`
- [x] 1.2 Use Case documentation exists and is accurate — CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec — `specs/factory-dashboard-production-line/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md` — factory-dashboard only
- [x] 1.5 ADR not required — no architectural change; dashboard remains a client of the kit contract (ADR-006)
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 2400` (on 403 print SKIPPED and continue)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/2400_dashboard_production_line`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from delta spec scenarios (server: DELETE proxy, SSE proxy, SSE cleanup; UI: timeline, intent bar, live sync, config viewer, delete, telemetry, a11y)
- [x] 3.2 Write server unit tests: `tests/server.test.ts` — DELETE proxy with CSRF/If-Match, SSE proxy forwards Last-Event-ID, SSE cleanup on disconnect
- [x] 3.3 Write UI hooks tests: `tests/hooks.test.ts` — polling fallback when SSE unavailable (covered in e2e via live indicator test)
- [x] 3.4 Run them and **observe them fail** — `cd framework/ai-sdlc/factory-dashboard && pnpm test` (commit 77231cca)
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

> **Mechanical deviation:** The kit's `GET /factory/line` has no agents/skills/mcp/model/effort per stage; they come from `GET /factory/config -> stages[]`. The dashboard merges `/factory/config` stages[] into the line view (does not change scope/contract). The old runs-by-status stations were replaced by the timeline while the Runs view keeps status filtering.

Group A — Server proxy extensions:

- [x] 4.1 Add `DELETE` to the proxy route's method list in `src/server/app.ts`; forward `If-Match` header
- [x] 4.2 Add `callKit` support for `DELETE` method in `src/server/upstream.ts`
- [x] 4.3 Implement SSE streaming proxy for `/factory/events` in `src/server/app.ts`: open `EventSource` to kit with token, forward `Last-Event-ID` from browser, relay frames, clean up on client disconnect

Group B — UI model types and API helpers:

- [x] 4.4 Add TypeScript interfaces to `src/ui/model.ts`: `ProductionLine`, `LineStage`, `LineGate`, `LineGuardrails`, `FactoryStatus`, `FactoryStatusStep`, `FactoryStatusNext`, `TelemetryReport`, `TelemetryRow`, `TelemetryRepolens`, `Intent`, `IntentSource`, `IntentReference`, `IntentSubmit`, `IntentList`, `ConfigFile`, `ConfigFileContent`
- [x] 4.5 Add API helper functions to `src/ui/api.ts` for factory endpoints (line, status, events, intents, telemetry, config files)

Group C — Production line timeline view:

- [x] 4.6 Create `src/ui/views/ProductionLine.tsx`: horizontal timeline with stations from `/factory/line`, gates between stations as required/advisory pills
- [x] 4.7 Station expand panel: agents, skills, tools, inputs, outputs, guardrails
- [x] 4.8 Current-run position overlay from `/factory/status`: highlight current station, show previous/next steps

Group D — Intent bar and drawer:

- [x] 4.9 Create `src/ui/components/IntentBar.tsx`: always-visible bottom bar with current intent title and status
- [x] 4.10 Create `src/ui/components/IntentDrawer.tsx`: expandable drawer with full intent text, acceptance criteria, references as links, source
- [x] 4.11 Intake form in drawer: prompt/reference/spec fields posting to `POST /factory/intents`

Group E — Live sync:

- [x] 4.12 Create `src/ui/hooks.ts` hook `useLiveSync`: `EventSource` on proxy URL, refresh status/line/intent on events, live indicator state
- [x] 4.13 Polling fallback (5s interval) when SSE unavailable
- [x] 4.14 Live indicator dot in sidebar footer

Group F — Config-as-code viewer:

- [x] 4.15 Update `src/ui/views/Projects.tsx`: file tree from `GET /factory/config/files` with kind badges
- [x] 4.16 Create `src/ui/components/ConfigViewer.tsx`: read-only viewer with line numbers from `GET /factory/config/files/{path}`

Group G — Catalog delete:

- [x] 4.17 Add delete button to `src/ui/views/Catalogs.tsx` with inline confirmation dialog (no `window.confirm`)
- [x] 4.18 Send `DELETE` with `If-Match` header; handle 409 version conflict

Group H — Telemetry panel:

- [x] 4.19 Update `src/ui/views/Metrics.tsx`: add telemetry panel with time/cost per intent and stage, failures by agent/skill/tool, RepoLens calls

Group I — i18n and accessibility:

- [x] 4.20 Add en/es copy to `src/ui/i18n.ts` for all new UI strings
- [x] 4.21 Ensure WCAG AA: text labels on all status indicators, contrast ratios, keyboard navigation

## 5. Actualizar tests existentes

- [x] 5.1 Update `tests/server.test.ts` — add DELETE proxy tests, SSE proxy tests
- [x] 5.2 Update existing E2E tests for overview → production-line timeline rename
- [x] 5.3 No tests made obsolete; overview tests are updated, not removed

## 6. Ejecutar regresión

- [x] 6.1 `cd framework/ai-sdlc/factory-dashboard && pnpm typecheck`
- [x] 6.2 `cd framework/ai-sdlc/factory-dashboard && pnpm test` — vitest server tests
- [x] 6.3 `cd framework/ai-sdlc/factory-dashboard && pnpm build` — production build
- [x] 6.4 `cd framework/ai-sdlc/factory-dashboard && pnpm e2e` — Playwright + axe
- [x] 6.5 No skipped tests without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 All E2E specs from design.md — Playwright Strategy
- [x] 7.2 `cd framework/ai-sdlc/factory-dashboard && pnpm e2e` — all green
- [x] 7.3 Verify at 320px, 768px, 1024px
- [x] 7.4 Axe core passes on all new views

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `framework/ai-sdlc/factory-dashboard/README.md` — views table
- [x] 8.2 Update `framework/ai-sdlc/factory-dashboard/DESIGN.md` — components section
- [x] 8.3 Update `docs/openspec/specs/factory-dashboard/spec.md` — add new requirements
- [x] 8.4 Update `CHANGELOG.md` — Features entry under `[Unreleased]`
- [x] 8.5 Confirm no information duplicated
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix`

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Refs #2400`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change: `cd docs && openspec archive feat-2400-dashboard-production-line --yes` — doing now in this step
- [ ] 10.2 `git push -u origin feat/2400_dashboard_production_line` — not done, push not allowed per task constraints
- [ ] 10.3 Open the PR titled `[#2400] feat(dashboard): production-line timeline, intent bar, live sync, config as code, telemetry` — not done, requires push first
- [ ] 10.4 Wait for CI workflows to pass — not done, requires PR
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete — not done, requires PR
- [ ] 10.6 Record the PR number in the archived `traceability.md` — not done, requires PR

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` — not done, requires PR
- [ ] 11.2 Confirm the CD pipeline published the image to GHCR — not done, requires merge
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md` — not done, requires merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Open dashboard, sign in, verify production line renders, intent bar visible, SSE connects — not done, requires deploy
- [ ] 12.2 Verify rollback path (git revert safe) — not done, requires merge
- [ ] 12.3 Close the GitHub Issue, referencing the PR — not done, requires PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md` — not done, requires deploy

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the vitest floor
- [x] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4) — pending push and PR creation
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5) — pending PR merge
- [ ] `traceability.md` complete from Issue through Release — partial; PR, merge, release, smoke pending
