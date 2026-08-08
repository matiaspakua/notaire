# Specification Template

> **Mandatory artifact of the SDLC workflow** (see `CONSTITUTION.md`, step 3 —
> *Specification*). A Specification describes **only the change** in scope. It
> is **not** permanent documentation: it lives with the Issue (small changes)
> or as a file under `docs/03-development/specifications/` (complex or
> architecture-affecting changes) and does not replace README/ADR/API docs.
>
> **Gate 1** — no implementation starts without this document (or the
> Specification section of the Issue) plus Acceptance Criteria.

---

## Specification: `<issue-number> - <title>`

| Field | Value |
|-------|-------|
| **Issue** | #`<issue-number>` |
| **Use Case (Caso de Uso)** | `CU-XX` / `RF-XX` / `RNF-XX` |
| **Type** | `feat` / `fix` / `refactor` / `test` / `docs` / `chore` / `ci` |
| **Status** | Draft / Refined / Approved |
| **Author** | `<name or agent>` |
| **Date** | `YYYY-MM-DD` |

---

## 1. Problem / Motivation

<!-- Why is this change needed? What requirement or defect does it address?
Reference the Use Case / Requirement ID. -->

## 2. Scope

### In scope
<!-- The exact behavior this change delivers. -->

### Out of scope
<!-- Explicitly what this change does NOT do (avoid scope creep). -->

## 3. Proposed Behavior

<!-- Describe only this change:
- Functional behavior / rules
- Inputs, outputs, error cases
- Affected endpoints / entities / UI screens -->

## 4. Constraints & Compatibility

<!-- Non-negotiable constraints: database (Flyway migration required?),
backwards compatibility, security, performance, design-system rules. -->

## 5. Impact Analysis

<!-- Affected modules, files, tests, and documentation. Dependencies on other
issues. Risks. -->

| Area | Impact |
|------|--------|
| Backend (`backend-api`) | ... |
| Frontend (`frontend`) | ... |
| Swing client (`frontend-swing`) | ... |
| Shared (`notaire-shared`) | ... |
| Database (Flyway) | ... |
| API contract / OpenAPI | ... |
| Tests (unit/integration/E2E) | ... |
| Documentation | ... |

## 6. Architecture Review

<!-- Confirm the design follows existing architecture. If architectural,
reference the new or updated ADR. -->

- [ ] Follows existing package/module architecture
- [ ] Follows project conventions (DTOs, REST, design system, Flyway)
- [ ] ADR required? Yes / No → (ADR number if yes)

## 7. Acceptance Criteria

<!-- Testable, ideally Given-When-Then. These MUST match the GitHub Issue. -->

- [ ] AC1: ...
- [ ] AC2: ...
- [ ] AC3: ...

## 8. Test Cases

<!-- Cases to be implemented as Unit / Integration / E2E tests (Gate 2). -->

| # | Level | Case | Expected result |
|---|-------|------|-----------------|
| 1 | Unit | ... | ... |
| 2 | Integration | ... | ... |
| 3 | E2E | ... | ... |

## 9. Documentation Changes

<!-- Which permanent documentation changes with this change (README, docs/,
ADR, API, runbooks, diagrams, business rules, CHANGELOG)? -->

- [ ] `CHANGELOG.md`
- [ ] `docs/...`
- [ ] OpenAPI / Swagger
- [ ] ADR
- [ ] None (documented why)

---

**Approvals**

| Role | Name | Date |
|------|------|------|
| Product / Business | | |
| Architecture | | |
| Engineering | | |
