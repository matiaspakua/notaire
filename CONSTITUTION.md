# Notaire Engineering Constitution

> **The official, mandatory development process for this repository.**
>
> This document is the single source of truth for how any change is made in
> Notaire — by a human or by an AI coding agent. It is **agent-agnostic**: it
> applies equally to Claude Code, OpenCode, GitHub Copilot, Cursor, Codex, and
> any other tool or person touching this codebase.
>
> Existing project documents (`.claude/rules/*`, `AGENTS.md`, `CLAUDE.md`,
> `.claude/rules/`, `docs/`) implement this Constitution. Where a lower-level
> document contradicts this Constitution, **this document prevails**.

---

## Table of Contents

1. [Purpose and Scope](#1-purpose-and-scope)
2. [Engineering Principles](#2-engineering-principles)
3. [Definition of Done](#3-definition-of-done)
4. [Mandatory Conventions](#4-mandatory-conventions)
5. [Official SDLC Workflow](#5-official-sdlc-workflow)
6. [Quality Gates](#6-quality-gates)
7. [Testing Rules](#7-testing-rules)
8. [Documentation Rules](#8-documentation-rules)
9. [Git Rules](#9-git-rules)
10. [Rules for AI Agents](#10-rules-for-ai-agents)
11. [Release Rules](#11-release-rules)
12. [Governance, Exceptions and Enforcement](#12-governance-exceptions-and-enforcement)
13. [Tooling Map](#13-tooling-map)

---

## 1. Purpose and Scope

This Constitution defines the mandatory engineering process for the Notaire
repository: a multi-module modernization of a Java Swing monolith into a
three-tier system (PostgreSQL 16 + Spring Boot 4.1 REST API + Next.js 16
frontend), with a transitional Swing REST client.

It applies to **every change** in the repository:

- New features and bug fixes
- Refactors, tests, and documentation changes
- CI/CD, infrastructure, and configuration changes
- Database schema changes (Flyway migrations)

No change is "done" unless it has passed the full process defined here.

---

## 2. Engineering Principles

| # | Principle | Meaning |
|---|-----------|---------|
| P1 | **TDD first** | Write failing tests before implementation. A test you never saw fail proves nothing. |
| P2 | **KIS — Keep It Simple** | The simplest solution that satisfies the Specification and passes all tests. Reject unnecessary complexity. |
| P3 | **SRP — Single Responsibility** | Each class, method, test, and module does exactly one thing. |
| P4 | **Traceability** | Every change traces from Issue → Use Case (Caso de Uso) → Specification → tests → code → docs → PR → deploy. Every finding raised by exploration (`openspec/explore*.md`) must resolve to a real GitHub Issue before it can become a change — a finding with no Issue is unfinished triage, not a rejected one (see §4, §13). |
| P5 | **Clean code** | Self-explanatory code; comments only explain *why*, never *what*. Remove dead and duplicate code. |
| P6 | **Flyway is the single source of truth** for the database schema; never alter old migrations — add a new `V{n}` migration. |
| P7 | **Documentation is part of the change** — permanent docs are updated before merge, never duplicated. |
| P8 | **Quality gates are absolute** — no skipping, no "it works on my machine". |
| P9 | **Least privilege & no secrets** — credentials only in the git-ignored `.env`; never hardcode secrets. |
| P10 | **Adapt, don't replace** — follow existing architecture (`repository` over legacy `jpa`, design system tokens, etc.). |

---

## 3. Definition of Done

A change is **Done** only when **all** of the following are true:

- [ ] Associated GitHub Issue exists, linked to a Use Case (`CU-XX`), with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; Unit + Integration tests written first (Gate 2)
- [ ] Implementation passes the full test suite: unit, integration, regression, E2E (Gate 3)
- [ ] Coverage gate satisfied (JaCoCo ratchet floor; 80% target)
- [ ] Playwright E2E passed for UI changes
- [ ] Permanent documentation updated and consistent (no duplication)
- [ ] Commits atomic, Conventional Commits format, `Closes #<issue>`
- [ ] Pull Request created, CI/CD green, code review approved (Gate 4)
- [ ] Merged to `main` via PR, deployed, smoke test passed, Issue closed (Gate 5)

---

## 4. Mandatory Conventions

| Concern | Convention |
|---------|------------|
| Issue | One GitHub Issue per change; labeled; linked to a Use Case (`CU-XX`), Functional Requirement (`RF-XX`) or Non-Functional Requirement (`RNF-XX`) |
| Branch | `<type>/<issue-number>_<description>` — e.g. `feat/253_user_auth`, `fix/254_login_timeout` |
| Commit | Conventional Commits: `<type>(<scope>): <description>`, ending with `Closes #<issue-number>` |
| PR title | `[#<issue-number>] type(scope): description` |
| Code style | Checkstyle: 120-char lines, 4-space indent, no wildcard imports, ordered imports (java → javax → third-party → own) |
| DTOs | `DtoEntityName` (e.g. `DtoUsuario`) |
| REST URLs | `/api/v1/resource` (plural nouns) |
| Test names | `shouldXxxYyy` with `@DisplayName`; AssertJ assertions |
| Test folders | `src/test/java/.../unit/` and `integration/`; Playwright under `frontend/tests/e2e/` |
| Database | New Flyway migration `V{n}__description.sql`; never edit applied migrations |
| Frontend | Centralized design system: `src/theme/tokens.ts` + `FormContainer → FormSection → FormField → FormActions` |
| Endpoints | Every REST endpoint must be invoked from the UI at least once (UI traceability) and documented in OpenAPI/Swagger |
| Exploration finding | Every finding ("hallazgo") in an exploration report (`openspec/explore*.md`) must carry a real, verifiable GitHub Issue reference — via the `openspec-triage` skill — before `/opsx:propose` may scaffold a change from it; see §13 and `docs/300-development/OPENSPEC-CONSTITUTION-BRIDGE.md` |

---

## 5. Official SDLC Workflow

The workflow below is **mandatory and sequential**. No step may be skipped
(see [Exceptions](#12-governance-exceptions-and-enforcement)). The Quality
Gates in [section 6](#6-quality-gates) are checked at the marked points.

```
Issue
  │
  ▼
Refinar requerimiento (Refine requirement)
  │
  ▼
Specification                              ◄── GATE 1
  │
  ▼
Impact Analysis
  │
  ▼
Architecture Review (ADR if architectural change)
  │
  ▼
Crear Branch (<type>/<issue-number>_<description>)
  │
  ▼
Definir Acceptance Criteria
  │
  ▼
Diseñar casos de prueba                     ◄── GATE 2
  │
  ▼
Escribir Unit Tests (failing)
  │
  ▼
Escribir Integration Tests (failing)
  │
  ▼
Implementar (make tests pass)
  │
  ▼
Actualizar Tests existentes
  │
  ▼
Ejecutar Tests (unit + integration)
  │
  ▼
Regression completa
  │
  ▼
Playwright E2E
  │
  ▼
Actualizar documentación permanente         ◄── GATE 3
  │
  ▼
Atomic Commits
  │
  ▼
Crear Pull Request
  │
  ▼
Esperar CI/CD verde
  │
  ▼
Code Review                                ◄── GATE 4
  │
  ▼
Merge
  │
  ▼
Deploy
  │
  ▼
Smoke Test                                 ◄── GATE 5
  │
  ▼
Cerrar Issue
```

### Step-by-step detail

**1. Issue.** Create/verify a GitHub Issue (`.github/ISSUE_TEMPLATE/issue.md`)
with: Description, mandatory Use Case reference, Context, Acceptance Criteria,
Technical Notes, Definition of Done, and appropriate labels.

**2. Refinar requerimiento.** Clarify the requirement with the requester and
affected areas. Confirm the Use Case is accurate or must be created/updated.

**3. Specification.** Produce a written Specification describing **only this
change**: behavior, boundary, inputs/outputs, and constraints. Specifications
are produced with **OpenSpec** using the project schema `notaire-sdlc`, which
encodes this Constitution:

```bash
openspec new change "<kebab-case-name>"      # scaffolds the mandatory artifacts
bash scripts/validate-sdlc-plan.sh "<name>"  # rejects an incomplete plan
```

The schema produces `proposal.md`, `traceability.md`, `specs/<capability>/spec.md`,
`design.md` and `tasks.md`. `docs/300-development/templates/specification-template.md`
maps every requirement of this Constitution to the artifact that carries it.
Acceptance Criteria are the delta spec's `#### Scenario:` blocks. → **Gate 1.**

**4. Impact Analysis.** Identify affected modules (backend-api, frontend,
frontend-swing, notaire-shared), entities, endpoints, database schema, tests,
and documentation. List risks and dependencies.

**5. Architecture Review.** Verify the design follows the existing
architecture and conventions. If the change is architectural, record it in an
ADR (`docs/200-architecture/202-ADR/`).

**6. Crear Branch.** From an updated `main`:

```bash
git checkout main && git pull origin main
git checkout -b <type>/<issue-number>_<short-description>
```

**7. Definir Acceptance Criteria.** Formalize testable criteria (Given-When-Then
where useful) in the Issue and the Specification.

**8. Diseñar casos de prueba.** Enumerate the test cases: happy path, edge
cases, error paths — at the unit, integration, and E2E level. → **Gate 2.**

**9–10. Escribir Unit e Integration Tests (TDD).** Write the tests first and
**watch them fail** before implementing:

```bash
mvn test -pl backend-api -Dtest=YourNewTestClass   # must FAIL at this point
```

**11. Implementar.** Write the minimum code to make the tests pass, following
project conventions and the Specification.

**12. Actualizar Tests existentes.** Update any existing tests affected by the
change. Never weaken assertions to force green.

**13. Ejecutar Tests.**

```bash
mvn test -pl backend-api                 # unit + integration
mvn jacoco:check -pl backend-api         # coverage gate
mvn verify -pl backend-api               # all quality checks
```

**14. Regression completa.** Run the full suite, including HTTP/Bruno API tests
(`bash testing/scripts/test.sh`) and any affected legacy paths. No
`@Disabled` or skipped tests without documented, approved justification.

**15. Playwright E2E.** For any UI change, run `cd frontend && npx playwright test`
— golden path and edge cases, on mobile/tablet/desktop widths.

**16. Actualizar documentación permanente.** Update the permanent documentation
affected by the change (see [section 8](#8-documentation-rules)). → **Gate 3.**

**17. Atomic Commits.** Commit in small, focused, self-contained units using
Conventional Commits; each commit references the issue (`Closes #<issue-number>`).

**17.5 Ejecutar el pipeline completo.** Before opening the PR, run
`bash scripts/run_pipeline.sh` — the single, dashboarded pre-PR gate. It brings
the Docker stack up itself and composes `validate-sdlc-plan.sh` +
`preflight.sh --full` + a markdown-lint pass, writing one HTML dashboard under
`reports/pipeline/<timestamp>/index.html`. Do not open the PR until it passes.

**18. Crear Pull Request.** Open a PR from the branch to `main` using
`.github/PULL_REQUEST_TEMPLATE.md`, referencing the Issue and Use Case.

**19. Esperar CI/CD verde.** Wait for all required GitHub Actions workflows to
pass (see [Tooling Map](#13-tooling-map)). Do not request review or merge while
CI is red. Locally, run `bash scripts/preflight.sh` before pushing (installed
pre-push hook enforces it).

**20. Code Review.** Address review feedback in new commits. The PR is ready to
merge only when approved by the code owner. In this repository the code owner
(`CODEOWNERS`) is also the person who merges; a separate formal GitHub
"Approve" review is not required as long as the code owner is the one who
merges the PR — that act **is** the approval. Record this plainly in
`traceability.md` (e.g. "merged by code owner directly; no formal GitHub
review recorded") rather than treating it as a gap to explain away — it is
the expected shape of review in a solo-maintainer repo, not an exception.
→ **Gate 4.**

**21. Merge.** Merge via the PR only (squash or merge commit consistent with
repo history). Never push directly to `main`.

**22. Deploy.** The release pipeline deploys the merged change (CI → CD →
Docker image; see [Release Rules](#11-release-rules)).

**23. Smoke Test.** Verify the deployed change on the target environment
(endpoint health, key flow of the change). → **Gate 5.**

**24. Cerrar Issue.** Close the GitHub Issue only after deploy + smoke test
succeeded, referencing the PR.

---

## 6. Quality Gates

The five gates are **hard stops**. Work does not progress past a gate until
every condition is satisfied.

### Gate 1 — Do not start implementation without:

- [ ] GitHub Issue exists (linked to a Use Case, with Acceptance Criteria)
- [ ] Specification written (approved for complex changes)
- [ ] Acceptance Criteria defined

### Gate 2 — Do not implement without:

- [ ] Unit Tests written (and observed failing)
- [ ] Test cases designed (happy path + edge + error paths)
- [ ] Integration Tests written where applicable

### Gate 3 — Do not create a PR if:

- [ ] Permanent documentation is not updated and consistent
- [ ] Any test is failing (unit, integration, regression)
- [ ] Coverage is reduced below the JaCoCo ratchet floor (80% target)
- [ ] Playwright E2E is failing for UI changes
- [ ] Checkstyle / Spotless / lint gates fail
- [ ] `bash scripts/run_pipeline.sh` has not been run, or last failed

### Gate 4 — Do not merge if:

- [ ] CI/CD is failing or not yet green
- [ ] Code review is pending or unresolved (in this repo, the code owner
      merging the PR themselves — see step 20 — counts as review approval;
      it is not "pending")
- [ ] Merge conflicts exist
- [ ] Documentation updates are pending

### Gate 5 — Do not consider the change finished until:

- [ ] Deploy succeeded
- [ ] Smoke test passed on the target environment
- [ ] Issue closed

---

## 7. Testing Rules

Every modification must produce or update:

| Test level | Required for | Location |
|------------|--------------|----------|
| **Unit Tests** | All changes | `backend-api/src/test/java/.../unit/` |
| **Integration Tests** | All changes with data/API impact | `backend-api/src/test/java/.../integration/` (H2 + Testcontainers/PostgreSQL) |
| **Contract Tests** | When a contract (API DTO/schema) changes and a contract suite exists | `testing/http/`, `backend-api/api-test/` (Bruno) |
| **Playwright E2E** | Any UI change | `frontend/tests/e2e/` |
| **Regression Tests** | All changes — full suite must stay green | entire suite |

Rules:

- TDD is mandatory: failing test first, then implementation, then refactor.
- No change is accepted without appropriate coverage. Coverage must never
  decrease; the JaCoCo ratchet floor is enforced by `mvn verify`.
- Never skip or `@Disabled` tests without documented justification.
- Test the happy path, the edge cases, and the error paths.
- For a bug fix, first write a test that reproduces the bug, then fix.
- For a new endpoint: controller + integration tests, OpenAPI documentation,
  and UI traceability (the endpoint is called from the UI at least once).

---

## 8. Documentation Rules

Every change must declare which permanent documentation it affects. Update the
permanent documentation **before merge**. Never duplicate documentation —
centralize information in the most coherent place; move outdated documents to
`docs/000-archive/`.

| Type | Where it lives | When it changes |
|------|----------------|-----------------|
| README | `README.md` | Project-level overview, quick start, badges |
| Architecture | `docs/200-architecture/` (SAD, ADRs, design, diagrams) | Architectural or structural changes |
| ADRs | `docs/200-architecture/202-ADR/` | Architectural decisions |
| Business rules & Use Cases | `docs/100-business/` (`CU-XX`, `RF-XX`, `RNF-XX`) | Business behavior changes; new Use Cases |
| API | `docs/200-architecture/203-design/` + OpenAPI/Swagger | Endpoint changes |
| Development | `docs/300-development/` | Build, test, contribution process |
| Operations / Runbooks | `docs/200-architecture/` (`206-security`, `207-monitoring`, `208-devsecops`, `209-deployment`) | Deploy, monitoring, security |
| Diagrams | `docs/200-architecture/204-diagrams/` | Architecture or data-model changes |
| Changelog | `CHANGELOG.md` (Keep a Changelog) | Every user-visible change |

Specifications describe **only the change** (they are not permanent
documentation) and are stored with the Issue or under
`docs/300-development/specifications/`.

---

## 9. Git Rules

Mandatory:

- One Issue per change; one branch per Issue.
- Never work on `main`. `main` is always stable.
- Branches: `<type>/<issue-number>_<description>`, created from an updated `main`.
- Commits: atomic, descriptive, Conventional Commits, referencing the issue.
- PRs: mandatory for every change; merge only via Pull Request.
- Merge requires: green CI, code review approval, no conflicts (Gate 4).
- No secrets in commits; `.env` is git-ignored.
- Before pushing, run the local CI gates: `bash scripts/preflight.sh`
  (enforced by the `pre-push` hook after `bash scripts/install-git-hooks.sh`).

---

## 10. Rules for AI Agents

This Constitution applies to any AI coding agent. An AI agent **must** follow
the full workflow in [section 5](#5-official-sdlc-workflow) and may **never**:

- ❌ Omit or skip steps of the workflow
- ❌ Implement directly from a prompt without an Issue + Specification
- ❌ Generate code without a Specification (Gate 1)
- ❌ Write code before its failing tests (Gate 2)
- ❌ Ignore any Quality Gate
- ❌ Create a PR with failing CI, missing/outdated docs, or reduced coverage
- ❌ Skip the documentation update step
- ❌ Commit to `main`, skip tests, or leave failing tests
- ❌ Mark an Issue closed before deploy + smoke test (Gate 5)

An AI agent **must**:

- ✅ Read and follow this Constitution at the start of any task
- ✅ Adapt to the existing project architecture and conventions (do not replace them)
- ✅ Reference the existing rule files (`AGENTS.md`, `.claude/rules/*`) for operational detail
- ✅ Treat human review as authoritative when conflicts arise

**Spec-Driven Development is the mechanism.** This Constitution is wired into
OpenSpec through the project schema `openspec/schemas/notaire-sdlc` and
`openspec/config.yaml`. Because both are read by the `openspec` CLI rather than
by any one assistant, every agent receives the same context, the same mandatory
sections and the same task groups — no agent-proprietary feature is involved.
An agent that never reads `CLAUDE.md` still gets this Constitution.

Agent-specific entry points:
- **Claude Code** → `CLAUDE.md` + `.claude/rules/ai-agent-workflow.md`
- **OpenCode** → `opencode.json` (loads `CLAUDE.md` and `.claude/rules/*`)
- **GitHub Copilot** → `.github/agents/openspec.agent.md`, `.github/prompts/opsx-*`
- **Any agent** → `AGENTS.md` at repo root; `.agents/skills/openspec-*`
- **Any agent, via the CLI** → `openspec instructions <artifact> --change <name>`

---

## 11. Release Rules

- Versioning follows **Semantic Versioning** (SemVer): `MAJOR.MINOR.PATCH`.
- `CHANGELOG.md` follows **Keep a Changelog** and is updated with every
  user-visible change; the `[Unreleased]` section is curated per release.
- Releases are tagged `v*` (e.g. `v1.2.0`).
- The CD pipeline (`cd.yml`) publishes the Docker image to GHCR **only after**
  CI on `main` succeeds (or on version tags / manual dispatch).
- GitHub Pages documentation deployment is handled by `deploy-github-page.yml`.
- Every release/deploy must pass a **smoke test** before the associated Issues
  are closed (Gate 5).
- Emergency hotfixes still follow this workflow, with the minimum viable
  Specification and the same gates; document the exception in the commit.

---

## 12. Governance, Exceptions and Enforcement

- **Authority:** This Constitution is the highest authority for the
  development process. Conflicts with lower-level documents are resolved in
  favor of this document.
- **Amending the Constitution:** Changes to this document require a dedicated
  PR reviewed by the code owner; they are themselves subject to this workflow.
- **Exceptions:** Only in extreme circumstances and with explicit human
  approval: emergency security hotfixes, one-time migration scripts, and
  trivial documentation-only typo fixes. The exception must be documented in
  the commit and the PR.
- **Enforcement:**
  - Local: `scripts/preflight.sh` + pre-push git hook (mirrors CI gates);
    `scripts/run_pipeline.sh` is the mandatory, dashboarded final check
    before opening a PR (→ Gate 3).
  - CI: GitHub Actions workflows block PRs that violate quality gates.
  - Human: code review by code owner (`CODEOWNERS`) before merge.

---

## 13. Tooling Map

Operational tools implement the Constitution. When adding or changing a CI
gate, update `scripts/preflight.sh` in the same PR so local and CI never
drift.

| Process step / gate | Tooling |
|---------------------|---------|
| Issue + Use Case | GitHub Issues; `.github/ISSUE_TEMPLATE/issue.md`; `gh` CLI |
| Specification | OpenSpec, schema `openspec/schemas/notaire-sdlc`; `openspec new change`; section map in `docs/300-development/templates/specification-template.md` |
| Constitution as agent context | `openspec/config.yaml` (`context`, `rules`, `operations`) — injected by the CLI for every agent |
| Plan completeness (Gates 1–5) | `bash scripts/validate-sdlc-plan.sh` (`--list` maps each check to its Constitution section) |
| Spec structure | `openspec validate <change> --strict` |
| Traceability (P4) | `traceability.md` per change; `openspec archive` folds deltas into `openspec/specs/` |
| Exploration → Issue traceability (P4, §4) | Explore → Issue → Propose sequence: `.claude/skills/openspec-triage/SKILL.md` turns an exploration report into real, estimated, Use-Case-linked Issues; `scripts/validate-sdlc-plan.sh` resolves the Issue live via `gh` so an invented number cannot pass Gate 1; see `docs/300-development/OPENSPEC-CONSTITUTION-BRIDGE.md` |
| Branch + commits | Git; Conventional Commits; branch `<type>/<issue-number>_<description>` |
| Unit + Integration tests | `mvn test -pl backend-api`; `mvn verify -pl backend-api` |
| Coverage | JaCoCo ratchet floor (`mvn jacoco:check`); CI job `coverage` |
| Lint / format | Spotless (CI "Code Lint"), Checkstyle, ESLint |
| Frontend | `frontend-ci.yml` (TypeScript, ESLint, Vitest, Next.js build) |
| E2E | `playwright-e2e.yml` (Playwright + Bruno API suite) |
| Local preflight | `bash scripts/preflight.sh [--fix|--fast|--full]`; pre-push hook |
| Pre-PR pipeline gate (Gate 3) | `bash scripts/run_pipeline.sh` — composes `validate-sdlc-plan.sh` + `preflight.sh --full` + markdown-lint (ratchet vs `origin/main`); writes `reports/pipeline/<timestamp>/index.html` dashboard |
| CI/CD | `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, `cd.yml` |
| Security | Trivy (`ci.yml` security job) |
| Deploy | `cd.yml` → build, scan, sign (cosign) and publish backend image to GHCR; no automated smoke test |
| Agent rules | `AGENTS.md`, `CLAUDE.md`, `.claude/rules/*`, `.claude/skills/*` |

---

*Last reviewed: 2026-08-08. This Constitution supersedes the process
summary in `.claude/rules/ai-agent-workflow.md` where they conflict; that
document remains the operational implementation.*
