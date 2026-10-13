# OpenSpec in Notaire — installation, adaptations, and quality gates

> **Authority**: `CONSTITUTION.md` at the repository root is the highest
> authority. This file is the canonical record of how **OpenSpec** was
> installed, adapted, and connected to that Constitution in the Notaire
> project. It consolidates and replaces
> `docs/300-development/OPENSPEC-CONSTITUTION-BRIDGE.md` (archived under
> `docs/000-archive/`), per Constitution P7/§8 (never duplicate permanent
> documentation). Tracked under #783 (original adoption) and #870
> (consolidation).
>
> This document covers **only OpenSpec**, the project's only spec-driven
> framework. SpecKit was evaluated in parallel and retired in #1083; its
> history stays in `docs/000-archive/speckit/`.

---

## Table of contents

1. [What is OpenSpec?](#1-what-is-openspec)
2. [Installation](#2-installation)
3. [Initial project configuration](#3-initial-project-configuration)
4. [Notaire adaptations — step by step](#4-notaire-adaptations--step-by-step)
   - 4.1 [Schema fork: `notaire-sdlc`](#41-schema-fork-notaire-sdlc)
   - 4.2 [Constitution context in `config.yaml`](#42-constitution-context-in-configyaml)
   - 4.3 [The Explore → Issue → Propose gap](#43-the-explore--issue--propose-gap)
   - 4.4 [Project-owned skill: `openspec-triage`](#44-project-owned-skill-openspec-triage)
   - 4.5 [Mechanical gate: `validate-sdlc-plan.sh`](#45-mechanical-gate-validate-sdlc-plansh)
   - 4.6 [Archive: `openspec-archive-change`](#46-archive-openspec-archive-change)
5. [Full flow — canonical sequence](#5-full-flow--canonical-sequence)
6. [Quality gates by SDLC stage](#6-quality-gates-by-sdlc-stage)
   - 6.1 [Gate 1 — Specification (do not start without this)](#61-gate-1--specification-do-not-start-without-this)
   - 6.2 [Gate 2 — TDD (tests first, failing)](#62-gate-2--tdd-tests-first-failing)
   - 6.3 [Gate 3 — PR-ready (green suite + docs)](#63-gate-3--pr-ready-green-suite--docs)
   - 6.4 [Gate 4 — Merge (green CI + review)](#64-gate-4--merge-green-ci--review)
   - 6.5 [Gate 5 — Done (deploy + smoke test + close)](#65-gate-5--done-deploy--smoke-test--close)
7. [Tool map by stage](#7-tool-map-by-stage)
8. [Available skills and commands](#8-available-skills-and-commands)
   - 8.1 [Skill composition for the SDLC](#81-skill-composition-for-the-sdlc)
   - 8.2 [Artifact and evidence contracts](#82-artifact-and-evidence-contracts)
9. [Evidence: changes that completed the flow](#9-evidence-changes-that-completed-the-flow)
10. [Mechanical gate verification](#10-mechanical-gate-verification)
11. [How to adapt OpenSpec in a new project](#11-how-to-adapt-openspec-in-a-new-project)
12. [References](#12-references)
13. [SDLC orchestration with OpenSpec — parallelization and cost](#13-sdlc-orchestration-with-openspec--parallelization-and-cost)

---

## 1. What is OpenSpec?

OpenSpec is a schema-driven specification CLI. Its core: a **schema**
(`docs/openspec/schemas/<name>/schema.yaml`) declares a set of **artifacts**
(proposal, spec deltas, design, tasks, traceability). Each artifact has a
Markdown template and an `instruction:` block that the CLI injects into the
agent context through `openspec instructions`. The command
`openspec new change <name>` creates the directory
`docs/openspec/changes/<name>/` with `.openspec.yaml`. The four Markdown
templates (`proposal`, `design`, `tasks`, `traceability`) are copied with
`bash workspace/sdlc/seed-openspec-change.sh <name> …` when they do not exist
yet. `openspec archive <name>` moves a completed change to
`docs/openspec/changes/archive/` and syncs its accepted deltas into
`docs/openspec/specs/<capability>/spec.md`.

What OpenSpec does not do by default: verify that the Issue exists on GitHub,
require a technical design on every change, or connect to a project's CI
gates. Notaire adds those layers on top of OpenSpec.

### Quick CLI reference

```bash
# Install / update
npm install -g openspec

# Change lifecycle
openspec new change "<kebab-case-name>"           # creates the change .openspec.yaml
bash workspace/sdlc/seed-openspec-change.sh "<name>" \ # copies templates + known values
  --issue N --use-case "CU…" --branch "type/N_desc"
openspec status --change "<name>"                 # artifact build order
openspec instructions <artifact> --change "<name>"  # instructions for the agent
openspec validate "<name>" --strict               # structural checks
bash workspace/sdlc/validate-sdlc-plan.sh "<name>"     # Gate 1 (includes leftover <!-- -->)
openspec list                                     # active changes
openspec archive "<name>"                         # archive a completed change

# Integrity checks
openspec doctor                                   # installation health
openspec context --json                           # root resolved by the CLI
openspec schemas --json                           # available schemas
```

---

## 2. Installation

### Prerequisites

| Tool | Minimum version | Use |
|------|-----------------|-----|
| Node.js | 18+ | CLI runtime |
| npm | 9+ | CLI installation |
| `gh` CLI | 2.x | Live Issue verification (Gate 1) |
| Git | any | Branches, commits, pre-push hook |
| Bash | 3.2+ | Validation scripts |
| Java 26 + Maven 3.9 | (project) | Backend quality gates |
| Node.js 22 + npm 10 | (project) | Frontend quality gates |

### Installation steps

```bash
# 1. Install the CLI globally
npm install -g openspec

# 2. Verify the installation
openspec --version
openspec doctor

# 3. Authenticate the gh CLI (required for the live Issue gate)
gh auth login

# 4. Install the project git hooks (pre-push runs preflight.sh)
bash workspace/sdlc/install-git-hooks.sh

# 5. Verify that the project schema is recognized
openspec schemas --json
# must include "notaire-sdlc"
```

### Verify that everything is connected

```bash
# Must list the active changes under docs/openspec/changes/
openspec list

# Must validate every active change
bash workspace/sdlc/validate-sdlc-plan.sh

# Must list every local gate
bash workspace/sdlc/preflight.sh --list
```

---

## 3. Initial project configuration

OpenSpec configuration in Notaire lives in **two project-owned files**:

```text
docs/openspec/
├── config.yaml                        # Constitution context + rules + operations
└── schemas/
    └── notaire-sdlc/
        ├── schema.yaml                # spec-driven schema fork with adaptations
        └── templates/
            ├── proposal.md            # template with every mandatory section
            ├── traceability.md        # ledger Issue → ... → Release
            ├── spec.md                # delta spec with Scenarios as criteria
            ├── design.md              # technical design + testing/deploy strategies
            └── tasks.md               # 12 SDLC groups + Definition of Done
```

None of these files is generated by `openspec update`. They are project-owned
and survive CLI upgrades. The vendor skills (`openspec-propose`,
`openspec-apply-change`, `openspec-archive-change`, `openspec-explore`,
`openspec-sync-specs`, `openspec-update-change`) in `.claude/skills/` are
replaced by `openspec update`. That is why no project adaptation lives inside
them.

---

## 4. Notaire adaptations — step by step

### 4.1 Schema fork: `notaire-sdlc`

**File**: `docs/openspec/schemas/notaire-sdlc/schema.yaml`

The upstream schema is `spec-driven` (packaged with openspec 1.8.0). Notaire
forks it into its own schema so the Constitution can be added without touching
vendor code. The fork keeps every OpenSpec parsing primitive intact:

- `## ADDED/MODIFIED/REMOVED/RENAMED Requirements`
- `### Requirement:` (three hashes)
- `#### Scenario:` with exactly four hashes
- `- [ ]` task checkboxes

What the fork **adds** on top of upstream:

| Addition | Why |
|----------|-----|
| `CONSTITUTION.md` as mandatory context on every artifact | Every agent receives the Constitution through `openspec instructions`, regardless of the tool |
| Mandatory sections in `proposal.md` | Impact Analysis, Documentation Impact, `Reglas de negocio` — required by §5 steps 3-4 |
| `traceability` artifact | Implements P4 (Issue→Release traceability). It does not exist in the upstream schema |
| `design.md` mandatory (not conditional) | Constitution §6/§7 requires testing, regression, Playwright, deploy, and rollback strategies on every change |
| Schema `notaire-sdlc` in `config.yaml` | Makes `openspec new change` use this schema by default |

To upgrade openspec without losing the fork:

```bash
openspec update          # updates vendor skills
# Then review manually:
diff <(openspec schema which spec-driven --json) docs/openspec/schemas/notaire-sdlc/schema.yaml
```

### 4.2 Constitution context in `config.yaml`

**File**: `docs/openspec/config.yaml`

This is the mechanism by which **any agent** — Claude Code, OpenCode, GitHub
Copilot, Codex, Cursor — receives the Constitution and the project rules. The
CLI injects `context:` into every call to `openspec instructions <artifact>`.

```yaml
schema: notaire-sdlc
githubCopilot:
  cloudAgent: true

context: |
  This repository is governed by CONSTITUTION.md at its root...
  [pointer to the Constitution + non-negotiables, never a copy]

rules:
  proposal:    [...]   # mandatory sections, a real Issue, never fabricated
  traceability:[...]   # do not pre-fill, only real evidence
  specs:       [...]   # Scenarios with 4 hashes, happy+edge+error paths
  design:      [...]   # every section, complete Testing Strategy
  tasks:       [...]   # 12 mandatory groups + DoD

operations:
  apply:
    guidance:  [...]   # verify Gate 1 first, TDD, preflight.sh before push
  archive:
    guidance:  [...]   # only after Gate 5: deploy + smoke + Issue closed
```

`context:` is a **pointer** to `CONSTITUTION.md` plus its non-negotiables, not
a copy. Permanent documentation has a single owner (P7/§8). The per-artifact
`rules:` and the per-command `operations:` explicitly require a real Issue,
the 12 task groups, and that `traceability.md` is never pre-filled.

### 4.3 The Explore → Issue → Propose gap

**The problem that existed**: `opsx:explore` produces a findings/gaps report
but never scaffolds a change (it is a thinking posture). `opsx:propose`
scaffolds a change and expects an Issue number in the `proposal.md` header.
The only check that existed validated the **format** of the Issue reference
(`#[0-9]+`), not that the Issue was **real**. Nothing stopped an agent from
writing `#999999` in a proposal and passing Gate 1.

**The solution**: three pieces that work together:

```text
opsx:explore          openspec-triage          opsx:propose
(thinking, produces ──▶  (report → real      ──▶  (scaffolds the change,
 a report; never          Issues, estimated,        proposal.md cites a
 scaffolds a change)      prioritized, with CU)     REAL Issue)
                                   │
                                   ▼
                     workspace/sdlc/validate-sdlc-plan.sh
                     resolves the Issue live via
                     `gh issue view` — a fabricated
                     or closed Issue fails Gate 1
```

Every finding in `docs/openspec/explore*.md` must have a real, open GitHub
Issue before `/opsx:propose` turns it into a change. This rule is in
`docs/openspec/config.yaml`'s `context:` and is enforced mechanically by the
script (see §4.5).

### 4.4 Project-owned skill: `openspec-triage`

**File**: `.claude/skills/openspec-triage/SKILL.md`

- `metadata.author: notaire-project` — `openspec update` does not touch it
- The missing link between `opsx:explore` and `opsx:propose`
- Takes an exploration report and produces a list of estimated, prioritized
  candidates with an associated CU-XX
- Creates GitHub Issues only with explicit user confirmation (`gh issue create`)
- Its only output is a real Issue number — the only thing that may be passed
  to `opsx:propose`

**Internal skill flow**:

1. Read the full source report
2. Extract one candidate per feature/gap: title, CU-XX, description,
   acceptance criteria, technical notes
3. Estimate size (S/M/L) and priority (`priority:high/medium/low`)
4. Write the candidate list to `docs/openspec/<source>_issues.md`
5. Show it to the user and **stop** — never create Issues without confirmation
6. With confirmation, `gh issue create` one by one and record the resulting number
7. Report which candidates have a real Issue and are ready for `opsx:propose`

**Guardrails**: never invent an Issue number; never scaffold a change
(`openspec new change`) from this skill; never skip the Use Case (Caso de Uso)
check.

### 4.5 Mechanical gate: `validate-sdlc-plan.sh`

**File**: `workspace/sdlc/validate-sdlc-plan.sh`

This is the mechanical gate that turns the schema's advisory rules into a
check that fails with an exit code other than 0. It is plain bash. It works
for any agent, CI, or human, without tool-specific hooks.

```bash
# Usage
bash workspace/sdlc/validate-sdlc-plan.sh                 # every active change
bash workspace/sdlc/validate-sdlc-plan.sh <change-name>   # one change
bash workspace/sdlc/validate-sdlc-plan.sh --list          # checks ↔ Constitution §
```

**What it verifies**:

| Check | File | What fails |
|-------|------|------------|
| Issue + Use Case in the header | `proposal.md` | §4/Gate 1 — missing number or placeholder |
| Issue **exists and is OPEN** on GitHub | `proposal.md` | Explore→Issue→Propose — resolves live via `gh issue view` |
| Mandatory sections | `proposal.md` | `Objetivo`, What Changes, `Reglas de negocio`, Impact Analysis, Documentation Impact |
| Complete chain | `traceability.md` | Issue→Spec→Tasks→Commits→PR→Merge→Release |
| Not pre-filled | `traceability.md` | Detects fictional SHAs/PRs in the chain |
| At least one delta spec | `specs/**/*.md` | Unless `skip_specs: true` in `.openspec.yaml` |
| Well-formed Scenarios | `specs/**/*.md` | `#### Scenario:` with 4 hashes |
| Design sections | `design.md` | Testing/Regression/Playwright/Deployment/Rollback |
| 12 SDLC groups | `tasks.md` | Gates 1-5, groups 1-12 + Definition of Done |
| Bodies that are only `<!-- … -->` | `proposal`/`design`/`tasks`/`traceability` | Gate 1 — unfilled template section (names the file + heading) |

**Template seed (cloud/generic equivalent of `phase_setup`)**:

`openspec new change` writes only `.openspec.yaml`. Before the agent fills
the specification, run:

```bash
bash workspace/sdlc/seed-openspec-change.sh "<change>" \
  --issue <n> --use-case "CU76 — …" --branch "<type>/<n>_desc"
```

That copies `proposal.md`, `design.md`, `tasks.md`, and `traceability.md`
from `docs/openspec/schemas/notaire-sdlc/templates/` **only when they are
missing**, fills Issue / Use Case / Branch / change name, and does **not**
overwrite a file that is already present. The agent must **fill** the seeded
sections (keep every `##`). `validate-sdlc-plan.sh` rejects a body that is
still only the template HTML comment. Self-tests:
`workspace/tests/test_validate_sdlc_plan.py`.

**The live Issue check** (the most important one):

```bash
# When gh is available and authenticated:
# ✗ - Issue #999999 was not found on GitHub (Gate 1 fails)
# ✓ - Issue #780 verified live on GitHub (OPEN)

# Without gh:
# - (advisory) gh CLI not available/authenticated — Issue not live-verified
#   (visible note, never a silent pass)
```

**Integration**: run by `workspace/sdlc/preflight.sh` (local, pre-push) and by
`.github/workflows/pr-validation.yml` (CI, blocking).

### 4.6 Archive: `openspec-archive-change`

**Skill**: `.claude/skills/openspec-archive-change/SKILL.md` (vendor, `metadata.author: openspec`)

The archive mechanism needed no adaptation. It is used as shipped.
`docs/openspec/config.yaml`'s `operations.archive.guidance` pins the sequence
so an agent cannot archive early:

```yaml
operations:
  archive:
    guidance:
      - "Do not archive before Gate 5 - deployed, smoke test passed, Issue closed."
      - "Confirm traceability.md is complete from Issue through Release..."
      - "Confirm the permanent documentation named in Documentation Impact was updated..."
      - "Summarize what moved into docs/openspec/specs/ and what remains outstanding."
```

**Archive result**:

- Change moved to `docs/openspec/changes/archive/YYYY-MM-DD-<name>/`
- Accepted deltas synced into `docs/openspec/specs/<capability>/spec.md`
- `traceability.md` complete with real evidence (SHAs, PR, CI run, smoke test)

---

## 5. Full flow — canonical sequence

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                     NOTAIRE SDLC — FULL FLOW                                │
└─────────────────────────────────────────────────────────────────────────────┘

  opsx:explore                                           GATE 1
  ─────────────                                          ──────
  Produce findings/gaps                       ◄── openspec-triage → real Issue
  in docs/openspec/explore*.md                         (never opsx:propose without an Issue)
          │
          ▼
  openspec-triage (SKILL, project-owned)
  Report → estimated/prioritized candidates
  gh issue create (with confirmation)
  Real Issue # recorded in the report
          │
          ▼
  opsx:propose (SKILL, vendor)
  openspec new change "<name>"
  Scaffolds: proposal.md, traceability.md,
  specs/, design.md, tasks.md
          │
          ▼
  bash workspace/sdlc/validate-sdlc-plan.sh ◄── Mechanical gate:
  • Issue exists and is OPEN                Fabricated Issue → FAIL
  • Mandatory sections                      Closed Issue     → FAIL
  • Traceability not pre-filled             No design        → FAIL
  • 12 SDLC groups present                  No specs         → FAIL
  • Well-formed Scenarios
          │
          ▼ Gate 1 passes
  ─────────────────────────────────────────────────────
  git checkout main && git pull
  git checkout -b <type>/<issue-number>_<description>  ◄── branch from main
  gh issue edit <n> --add-label "in-progress"
  ─────────────────────────────────────────────────────
          │
          ▼
  GATE 2 — TDD
  Write tests (unit + integration) → watch them FAIL
  mvn test -pl backend-api -Dtest=<NewTestClass>  # must FAIL here
          │
          ▼
  opsx:apply (SKILL, vendor)
  Implement (make tests pass)
  Refactor (KISS, SRP, remove dead/duplicated code)
          │
          ▼
  GATE 3 — PR-ready
  mvn test -pl backend-api                  # unit + integration
  mvn jacoco:check -pl backend-api          # coverage ratchet ≥ 70% line / 25% branch
  mvn verify -pl backend-api                # Checkstyle + SpotBugs
  cd testing/e2e && npx playwright test        # E2E (when the UI changes)
  Actualizar documentación permanente       # docs/, README.md, CHANGELOG.md
  bash workspace/sdlc/preflight.sh --fix           # mirrors every CI gate
  bash workspace/sdlc/run_pipeline.sh              # final pre-PR gate (HTML dashboard)
          │
          ▼
  git push -u origin <branch>
  gh pr create --title "[#<issue>] type(scope): description"
          │
          ▼
  GATE 4 — green CI + review
  ci.yml + pr-validation.yml + frontend-ci.yml + playwright-e2e.yml
  Code review (code owner merging = approval in a maintainer-only repo)
  Merge via PR (never a direct push to main)
          │
          ▼
  GATE 5 — Done
  cd.yml → Docker image → GHCR
  Smoke test in the target environment
  gh issue close <n> --comment "Closes #<n> via PR #<pr>"
  openspec archive <change-name>            # sync specs, complete traceability
```

---

## 6. Quality gates by SDLC stage

### 6.1 Gate 1 — Specification (do not start without this)

**When**: Before creating the branch or writing any code.

**Conditions** (all of them):

- [ ] A GitHub Issue exists, has a label, and is linked to a Use Case (Caso de Uso) (`CU-XX` / `RF-XX` / `RNF-XX`)
- [ ] Specification written (proposal, specs, design, tasks with `openspec new change`)
- [ ] Acceptance Criteria defined as `#### Scenario:` in the delta spec
- [ ] Impact Analysis and `Módulos afectados` confirmed in `proposal.md`
- [ ] An ADR when the change is architectural (`docs/200-architecture/202-ADR/`)

**Scripts and tools that enforce it**:

```bash
# Local mechanical validation (and in CI)
bash workspace/sdlc/validate-sdlc-plan.sh <change-name>

# Live Issue verification
gh issue view <number>   # used internally by validate-sdlc-plan.sh

# Structural schema validation
openspec validate <change-name> --strict
```

**CI jobs that block on it**:

- `pr-validation.yml` → job `sdlc-plan-validation` → `bash workspace/sdlc/validate-sdlc-plan.sh` (BLOCKING)

**Traceability rule** (P4, §4): every finding in `docs/openspec/explore*.md`
must have a real Issue before `opsx:propose` turns it into a change. See
`.claude/skills/openspec-triage/SKILL.md` for the process.

---

### 6.2 Gate 2 — TDD (tests first, failing)

**When**: After creating the branch, before writing implementation code.

**Conditions**:

- [ ] Unit tests written (and **observed failing**)
- [ ] Integration tests written where they apply
- [ ] Each `#### Scenario:` in the delta spec has at least one test that covers it

**Mandatory commands**:

```bash
# Backend — must FAIL at this point
mvn test -pl backend-api -Dtest=<NewTestClass>

# Frontend — must FAIL at this point
cd frontend && npx vitest run <new-test>
```

**Tools**:

| Level | Framework | Location | Coverage target |
|-------|-----------|----------|-----------------|
| Unit (backend) | JUnit 5 + Mockito | `src/test/java/.../unit/` | 70-85% line |
| Integration (backend) | Testcontainers / H2 | `src/test/java/.../integration/` | 60-75% |
| Component (frontend) | Vitest + React Testing Library | `frontend/src/**/*.test.tsx` | 60-75% line |
| API Contract | Bruno | `backend-api/api-test/` | 80-100% of endpoints |
| E2E | Playwright | `testing/e2e/tests/TS-nnnn-*.spec.ts` | 1+ per Use Case (Caso de Uso) |

**Conventions**:

- Names: `shouldXxxYyy` with `@DisplayName` (backend); `describe/it` (frontend)
- E2E naming: `TS-nnnn-<workflow-name>.spec.ts` with CU traceability
- DTOs: `DtoEntityName` (for example `DtoLoginUser`)
- Assertions: AssertJ (backend); React Testing Library (frontend)

---

### 6.3 Gate 3 — PR-ready (green suite + docs)

**When**: Before opening the pull request. The full gate runs with
`bash workspace/sdlc/run_pipeline.sh`, which starts the Docker stack itself.

**Conditions** (none of them may fail):

- [ ] Full suite green: unit + integration + regression + E2E
- [ ] Coverage at or above the ratchet floor (JaCoCo: 70% line / 25% branch; Vitest: 75% line)
- [ ] Playwright green for UI changes
- [ ] Checkstyle with no violations
- [ ] Spotless formatted
- [ ] ESLint with no errors
- [ ] Permanent documentation updated and consistent
- [ ] `bash workspace/sdlc/run_pipeline.sh` passed (writes an HTML dashboard under `reports/pipeline/<timestamp>/`)

**Full commands**:

```bash
# Backend: full quality
mvn test -pl backend-api                          # unit + integration
mvn jacoco:check -pl backend-api                  # coverage gate (ratchet floor)
mvn verify -pl backend-api                        # Checkstyle + SpotBugs + coverage
mvn checkstyle:check -pl backend-api              # style only
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false  # static analysis

# Frontend: full quality
cd frontend && npm run build                      # TypeScript compile + Next.js build
cd frontend && npx eslint src                     # ESLint
cd frontend && npx vitest run --coverage          # Vitest + coverage

# E2E (requires a running stack)
cd testing/e2e && npx playwright test                # every test
cd testing/e2e && npx playwright test --reporter=html  # with an HTML report

# API tests
bash testing/scripts/test.sh                      # Bruno / HTTP suite

# Java format (Spotless — NOT in mvn verify, requires an explicit call)
mvn spotless:check -pl backend-api                # verify
mvn spotless:apply -pl backend-api                # fix

# Full local gate (mirrors CI)
bash workspace/sdlc/preflight.sh                         # everything except server-backed suites
bash workspace/sdlc/preflight.sh --fix                   # auto-fix what is fixable
bash workspace/sdlc/preflight.sh --fast                  # format/lint/compile/typecheck only
bash workspace/sdlc/preflight.sh --full                  # + Playwright E2E + Bruno + Docker

# Pre-PR gate with an HTML dashboard
bash workspace/sdlc/run_pipeline.sh                      # REQUIRED before opening a PR
```

**CI jobs that block on it** (all of them must be green):

| Workflow | Key jobs | What it verifies |
|----------|----------|------------------|
| `ci.yml` | build, unit-tests, integration-tests, coverage, security | Compile, JUnit, Testcontainers, JaCoCo, Trivy |
| `pr-validation.yml` | branch-naming, sdlc-plan, spotless, checkstyle, dep-analysis | Gate 1, format, Spotless (BLOCKING) |
| `frontend-ci.yml` | typecheck, eslint, vitest, build | TypeScript, ESLint, Vitest ≥75%, Next.js build |
| `playwright-e2e.yml` | playwright, bruno-api | Playwright E2E, Bruno API suite |

**Quality tools in detail**:

| Tool | Config | Ratchet/Target | Where it is enforced |
|------|--------|----------------|----------------------|
| JaCoCo | `pom.xml` | 70% line / 25% branch (floor); 80%/80% (target) | `mvn verify`, `ci.yml` job `coverage` |
| Checkstyle | `backend-api/checkstyle.xml` | 0 violations (warn in CI) | `mvn checkstyle:check`, `pr-validation.yml` |
| SpotBugs | `backend-api/spotbugs-exclude.xml` | report-only | `ci.yml` job `code-quality` |
| Spotless | `pom.xml` (unbound from the lifecycle) | 0 errors (BLOCKING) | `mvn spotless:check`, `pr-validation.yml` Code Lint |
| ESLint | `frontend/eslint.config.mjs` | 0 errors | `frontend-ci.yml` |
| Vitest | `frontend/vitest.config.ts` | 75% line | `frontend-ci.yml` |
| Trivy | built-in | HIGH+CRITICAL reported | `ci.yml` job `security`, `cd.yml` |
| markdownlint | ratchet vs `origin/main` | only `.md` files on the branch | `run_pipeline.sh` (local) |

> **Critical note**: Spotless is **deliberately unbound** from `mvn verify`
> (`backend-api/pom.xml`, see issue #705). A branch can pass local
> `mvn verify` and fail "Code Lint" in CI. That is why
> `workspace/sdlc/preflight.sh` invokes Spotless explicitly. `mvn verify`
> alone does not predict CI.

---

### 6.4 Gate 4 — Merge (green CI + review)

**When**: Before merging the pull request.

**Conditions**:

- [ ] CI/CD fully green (every required workflow)
- [ ] Code review approved (in this repo: the code owner merging the PR is the approval)
- [ ] No merge conflicts
- [ ] Documentation updates complete
- [ ] `traceability.md` updated with the PR number and the CI run URL

**PR conventions**:

```text
Title: [#<issue>] type(scope): description (≤70 chars)
Body: .github/PULL_REQUEST_TEMPLATE.md (Issue + Use Case reference)
```

**Commit conventions** (Conventional Commits):

```text
feat(scope): description
fix(scope): description
...
Closes #<issue-number>
```

---

### 6.5 Gate 5 — Done (deploy + smoke test + close)

**When**: After the merge, before closing the Issue.

**Conditions**:

- [ ] Deploy executed (CD pipeline `cd.yml` published the image to GHCR)
- [ ] Smoke test passed in the target environment (health endpoint + the change's key flow)
- [ ] Issue closed with a reference to the PR
- [ ] `openspec archive <change-name>` executed with a complete `traceability.md`

**Commands**:

```bash
# Verify the deploy
gh run list --workflow=cd.yml --limit=1

# Manual smoke test
curl http://localhost:8080/actuator/health
# + verify the change's specific flow

# Close the Issue
gh issue close <number> --comment "Implemented in PR #<pr>. Closes #<number>."

# Archive the change (sync specs + complete traceability)
openspec archive <change-name>
```

---

## 7. Tool map by stage

```text
STAGE                 TOOL / FILE                                    GATE
─────────────────────────────────────────────────────────────────────────
Issue + Use Case      GitHub Issues; gh CLI; .github/ISSUE_TEMPLATE/  Gate 1
                      issue.md

Exploration           opsx:explore                                    (pre-Gate 1)
                      .claude/skills/openspec-explore/SKILL.md
                      .claude/commands/opsx/explore.md
                      → produces docs/openspec/explore*.md

Triage                openspec-triage (project-owned)                 (pre-Gate 1)
                      .claude/skills/openspec-triage/SKILL.md
                      → produces docs/openspec/*_issues.md + real Issues

Specification         openspec new change                             Gate 1
                      docs/openspec/schemas/notaire-sdlc/schema.yaml
                      docs/openspec/config.yaml (context + rules)
                      docs/openspec/changes/<name>/ (5 artifacts)

Plan validation       bash workspace/sdlc/validate-sdlc-plan.sh              Gate 1
                      .github/workflows/pr-validation.yml
                      (BLOCKING: live Issue, sections, 12 groups)

Traceability          traceability.md per change                      P4
                      openspec archive → docs/openspec/specs/

Branch + commits      Git; branch <type>/<issue-number>_<desc>        §9

TDD                   JUnit 5 + Mockito (unit)                        Gate 2
                      Testcontainers (integration)
                      Vitest (frontend components)
                      mvn test -pl backend-api -Dtest=<Class>

Implementation        opsx:apply                                      Gate 2→3
                      .claude/skills/openspec-apply-change/SKILL.md
                      .claude/commands/opsx/apply.md

Coverage              JaCoCo (mvn jacoco:check)                       Gate 3
                      70% line / 25% branch (floor); 80% (target)
                      Vitest --coverage (75% line, frontend)

Code style            Checkstyle (checkstyle.xml)                     Gate 3
                      Spotless (mvn spotless:check — unbound)
                      ESLint (eslint.config.mjs)

Static analysis       SpotBugs (spotbugs-exclude.xml)                 Gate 3
                      Trivy (filesystem + Docker image)

E2E                   Playwright (testing/e2e/tests/)                 Gate 3
                      Bruno (backend-api/api-test/)
                      npx playwright test

Local preflight       bash workspace/sdlc/preflight.sh [--fix/--fast/--full] Gate 3
                      (pre-push hook installed by install-git-hooks.sh)

Pre-PR gate           bash workspace/sdlc/run_pipeline.sh                    Gate 3
                      → reports/pipeline/<timestamp>/index.html

CI/CD                 ci.yml (build, test, coverage, security)        Gate 4
                      pr-validation.yml (lint, sdlc, branch naming)
                      frontend-ci.yml (ts, eslint, vitest, build)
                      playwright-e2e.yml (E2E + Bruno)
                      cd.yml (Docker → GHCR)

Security              Trivy (ci.yml security job)                     Gate 4
                      SARIF → GitHub Security tab

Agent rules           AGENTS.md, CLAUDE.md                            §10
                      .claude/rules/ai-agent-workflow.md
                      .claude/rules/code-quality.md
                      .claude/rules/general.md
                      .claude/rules/programming.md
                      .claude/rules/ui-ux-design.md
                      .claude/rules/database-migrations.md
                      .claude/rules/refactoring.md

Agent context (all)   docs/openspec/config.yaml (context + rules)     §10
                      → injected by the CLI into openspec instructions
                      → reaches Claude Code, OpenCode, Copilot,
                        Codex, and Cursor through the same channel

Deploy                cd.yml → GHCR                                   Gate 5
                      (trigger: push to main, tag v*, or manual dispatch)

Archive               openspec archive <name>                         Gate 5
                      .claude/skills/openspec-archive-change/SKILL.md
                      .claude/commands/opsx/archive.md
```

---

## 8. Available skills and commands

### Vendor skills (generated by `openspec update`, do not edit)

| Skill | File | When to use it |
|-------|------|----------------|
| `openspec-explore` | `.claude/skills/openspec-explore/SKILL.md` | Explore the codebase or the business without scaffolding anything |
| `openspec-propose` | `.claude/skills/openspec-propose/SKILL.md` | Scaffold a change from a real Issue |
| `openspec-apply-change` | `.claude/skills/openspec-apply-change/SKILL.md` | Implement the tasks of a change |
| `openspec-archive-change` | `.claude/skills/openspec-archive-change/SKILL.md` | Archive after Gate 5 |
| `openspec-sync-specs` | `.claude/skills/openspec-sync-specs/SKILL.md` | Sync deltas into the main specs |
| `openspec-update-change` | `.claude/skills/openspec-update-change/SKILL.md` | Update a change's artifacts |

### Project-owned skill (`openspec update` does not touch it)

| Skill | File | When to use it |
|-------|------|----------------|
| `openspec-triage` | `.claude/skills/openspec-triage/SKILL.md` | After `opsx:explore`, before `opsx:propose` |

### Slash commands (Claude Code)

```text
.claude/commands/opsx/
├── explore.md     # /opsx:explore
├── propose.md     # /opsx:propose
├── apply.md       # /opsx:apply
├── update.md      # /opsx:update
├── sync.md        # /opsx:sync
└── archive.md     # /opsx:archive
```

### Entry points by agent (Constitution §10)

| Agent | Entry point |
|-------|-------------|
| **Claude Code** | `AGENTS.md` (imported by `CLAUDE.md`) + `.claude/rules/ai-agent-workflow.md` |
| **OpenCode** | `opencode.json` (loads `AGENTS.md`, `.claude/rules/*`, and `.claude/skills/*`) |
| **GitHub Copilot** | `AGENTS.md`; `.claude/agents/` |
| **Any agent** | `AGENTS.md` at the root; `.claude/skills/openspec-*` |
| **Any agent through the CLI** | `openspec instructions <artifact> --change <name>` |

### 8.1 Skill composition for the SDLC

The generic skills derived from the specialization guide are installed under
`.claude/skills/`. They are provider-neutral and describe intent, inputs,
outputs, validation, and risks. Notaire-specific skills keep authority over
commands, packages, conventions, and local tools. They do not replace each
other. They compose by responsibility.

| Moment | Cross-cutting skill | Complementary Notaire skill | Expected output |
|---|---|---|---|
| Strategy | `delivery-maturity-roadmap` | `product-owner`, `analyst` | baseline, outcome, roadmap, and prioritized backlog |
| Discovery | `devsecops-traceability` | `analyst`, `openspec-explore` | Issue, CU/RF/RNF, impact, and source evidence |
| Triage | `devsecops-traceability` | `openspec-triage` | estimated candidates and real Issues |
| Proposal | `devsecops-traceability` | `openspec-propose` | proposal and initial ledger with no invented evidence |
| Architecture | `architecture-decision-design` | `java-architect`, `plantuml`, `design-doc-mermaid` | design, ADR, C4/UML, and reviewable decisions |
| Security | `secure-threat-modeling` | `backend`, `devops`, `programming` | threat model, risk, privacy, `SR-*`, and verifiers |
| QA | `qa-automation-strategy` | `testing`, `maven-build`, `api-rest` | MTP, scenario matrix, fixtures, vectors, and evidence |
| API | `api-contract-testing` | `api-rest`, `backend` | OpenAPI/Swagger, contract, compatibility, and Bruno tests |
| Implementation | `devsecops-traceability` | `openspec-apply-change`, `java`, `frontend-design` | observed TDD, code, tests, and an updated ledger |
| Integration | `ci-cd-quality-gates` | `devops`, `maven-build` | gates, reports, artifact digest/provenance, and promotion |
| Operations | `operations-observability-readiness` | `devops` | SLO, telemetry, dashboards, alerts, runbook, and rollback |
| Close | `devsecops-traceability` | `openspec-archive-change` | complete traceability, permanent docs, and an archived change |

#### Recommended operating sequence

```text
1. delivery-maturity-roadmap
  -> define outcome, constraints, metrics, and priority
2. analyst + product-owner + openspec-explore
  -> refine the need, the CU, the requirements, and the scenarios
3. devsecops-traceability + openspec-triage
  -> turn findings into real Issues linked to a CU
4. openspec-propose + validate-sdlc-plan.sh
  -> create and validate proposal, traceability, specs, design, and tasks
5. architecture-decision-design + secure-threat-modeling
  -> decide the architecture, model threats, and define SR/controls
6. qa-automation-strategy + api-contract-testing
  -> design tests, the contract, fixtures, and evidence before code
7. openspec-apply-change + local implementation skills
  -> red TDD, minimal implementation, refactor, and an incremental ledger
8. ci-cd-quality-gates
  -> preflight, full suite, security, artifact, promotion, and rollback
9. operations-observability-readiness
  -> readiness, deploy verification, smoke test, SLO, and support
10. devsecops-traceability + openspec-archive-change
  -> close the evidence, update docs, sync specs, and archive
```

A skill is activated by intent, not by tool name. For example, an endpoint
change activates `api-contract-testing` whether the implementation uses REST,
gRPC, or AsyncAPI. The local skill `api-rest` then supplies the Bruno and
repository details.

### 8.2 Artifact and evidence contracts

Every change must keep a single verifiable chain. OpenSpec records the
temporary change. `docs/` records permanent knowledge. GitHub/CI records
delivery status. No artifact may copy the Constitution or repeat permanent
documentation.

| Artifact | Primary owner | Must contain | Validated at |
|---|---|---|---|
| Issue | Product/FA | CU/RF/RNF, scope, AC, priority, owner | Gate 1 |
| `proposal.md` | Analyst/agent | objective, changes, rules, impact, docs, out-of-scope | Gate 1 |
| `specs/**/spec.md` | Product/FA/QA | normative behavior and happy/edge/error scenarios | Gate 1 |
| `design.md` | Architecture/Dev/Sec/QA/Ops | decisions, risks, testing, deploy, rollback | Gate 1 |
| `traceability.md` | Change owner | Issue→CU→spec→test→code→docs→PR→deploy | Gates 1-5 |
| Test plan/cases | QA + Dev | `TC/TS-*`, fixtures, vectors, expected/evidence | Gate 2 |
| Threat model | Security + Architecture | assets, boundaries, STRIDE/LINDDUN, risk, `SR-*` | Gates 1-3 |
| API contract | API owner | schemas, auth/authz, errors, compatibility, examples | Gates 1-3 |
| Pipeline evidence | DevOps/CI | source, tests, scans, artifact digest, approvals | Gates 3-4 |
| Runbook/SLO | Operations | signal, threshold, action, owner, escalation, recovery | Gates 3-5 |
| Release/smoke evidence | Release owner | version, target, health, key flow, timestamp, result | Gate 5 |

#### Evidence quality rules

1. Use stable IDs and keep existing IDs. Never use line numbers as
   traceability identifiers.
2. Distinguish `planned`, `verified`, `missing`, and `conflicting`. A plan is
   not evidence of execution.
3. Record owner, date, environment, tool/version, and evidence location when
   the datum can change or is not reproducible.
4. Redact secrets, tokens, personal data, financial data, biometric data, and
   production records. Define retention and access control.
5. A green test proves the scenario that ran. It does not prove the absence of
   defects. A scan with no findings does not prove total security.
6. An exception must name the approver, the scope, the reason, the expiry
   date, and the compensating action. Never turn a temporary exception into a
   rule.

#### How to choose the level of rigor

Apply the full flow to business, API, data, security, architecture,
infrastructure, or production changes. For purely editorial changes the
functional specification may be omitted, but the reason must be recorded and
the documentation review must remain. Never omit impact analysis,
traceability, or the validation that matches the risk.

#### Maintenance and evolution

- Generic skills are updated at their origin and synced as a unit. Notaire
  adaptations live in this guide, the Constitution, and the catalog
  `.claude/skills/README.md`.
- Before updating a skill, run its `evals/evals.json` scenarios and compare
  behavior. Keep the test set when the scope changes.
- Before updating OpenSpec, preserve the `notaire-sdlc` fork and run
  `openspec validate --strict` and `bash workspace/sdlc/validate-sdlc-plan.sh`
  against a representative fixture.
- When a tool changes, update the tooling map and the matching local skill.
  Practices and contracts must not stay tied to a single tool.

---

## 9. Evidence: changes that completed the flow

At the time of this review, `docs/openspec/changes/archive/` contains the
following completed changes with the flow Explore → Issue → Propose → Apply →
Archive. Each one has a `traceability.md` with real SHAs, a merged PR, and
passing CI:

| Change | Issue | PR | Date |
|--------|-------|----|------|
| `persist-metodo-pago-on-pago` | #792 | #828 | 2026-08-20 |
| `resolve-presupuesto-tramite-cardinality` | — | — | 2026-08-20 |
| `verify-debt-on-gestion-archive` | #819 | #826 | 2026-08-20 |
| `payment-financial-tracking` | #820 | #845 | 2026-08-26 |
| `escritura-post-firma-legal-cycle` | #832 | #852 | 2026-08-27 |
| `gestion-workflow-y-bitacora` | #833 | #855 | 2026-08-28 |
| `fix-inmueble-valuacion-fiscal-type` | — | — | 2026-08-31 |
| `persona-validacion-duplicados` | #835 | — | 2026-09-02 |
| `protocolo-auxiliar-tramites` | #839 | #907 | 2026-09-02 |
| `escritura-folio-picker-form` | #892 | — | 2026-09-02 |

More changes are active under `docs/openspec/changes/` at any moment.
`docs/openspec/explore.md` is the permanent finding↔Issue traceability record
for the business domain. Every exploration must resolve to a real Issue, with
no exceptions.

---

## 10. Mechanical gate verification

Tested directly against `workspace/sdlc/validate-sdlc-plan.sh` using a
temporary fixture under `docs/openspec/changes/` (never committed):

| Case | Issue reference | Result |
|------|-----------------|--------|
| Negative | `#999999` (does not exist) | ✗ `proposal: Issue #999999 was not found on GitHub` — Gate 1 fails |
| Positive | `#780` (real, open) | ✓ `proposal: Issue #780 verified live on GitHub (OPEN)` |
| Degraded | `gh` unavailable | `- proposal: gh CLI not available/authenticated — Issue not live-verified` (advisory, does not fail) |

---

## 11. How to adapt OpenSpec in a new project

This section summarizes, in generic form, the steps Notaire followed. It is a
guide for applying the same pattern in another project.

### Step 1: Install and verify

```bash
npm install -g openspec
openspec doctor
gh auth login    # for the live Issue gate
```

### Step 2: Initialize the docs/openspec/ directory

```bash
openspec init    # creates docs/openspec/ with config.yaml and the default schema
```

### Step 3: Create the project's own schema

```bash
cp -r "$(openspec schema which spec-driven --path)" docs/openspec/schemas/<my-project>/
# Edit docs/openspec/schemas/<my-project>/schema.yaml:
#   name: <my-project>
#   Add required artifacts (for example traceability)
#   Make design.md mandatory
#   Adjust the templates
```

Point `docs/openspec/config.yaml` at the new schema:

```yaml
schema: <my-project>
```

### Step 4: Inject the governance process into config.yaml

```yaml
context: |
  [Pointer to your Constitution/process + non-negotiables]

rules:
  proposal:    [...]   # mandatory sections, a real Issue
  traceability:[...]   # do not pre-fill
  specs:       [...]   # complete Scenarios
  design:      [...]   # Testing/Deployment/Rollback
  tasks:       [...]   # SDLC groups + DoD

operations:
  apply:    { guidance: [...] }
  archive:  { guidance: [...] }
```

### Step 5: Create the mechanical gate (bash)

Adapt `workspace/sdlc/validate-sdlc-plan.sh` to verify:

- An Issue reference is present in `proposal.md`
- The Issue exists and is open (`gh issue view <number>`)
- The process's mandatory sections
- The mandatory task groups

Integrate it into:

- The git pre-push hook (`workspace/sdlc/install-git-hooks.sh`)
- CI (PR validation workflow, BLOCKING)

### Step 6: Create the triage skill (project-owned)

Create `.claude/skills/<my-triage>/SKILL.md` with
`metadata.author: <my-project>`. The skill takes an exploration report and
produces real Issues before any change is scaffolded.

### Step 7: Connect it to the existing CI/CD quality gates

Add this to `workspace/sdlc/preflight.sh` (or the equivalent):

```bash
# SDLC plan validation
bash workspace/sdlc/validate-sdlc-plan.sh

# ... the rest of the project gates (tests, lint, coverage, and so on)
```

### Guiding principles (from Constitution P10)

- **Adapt, do not replace**: vendor skills and the OpenSpec engine are
  installed and updated untouched. Adaptations live in project-owned files.
- **A single source of truth**: the Constitution or governance process lives
  in one file. `config.yaml` points at it and never copies it.
- **Agent-agnostic**: `config.yaml` uses OpenSpec's native extension point.
  It works for Claude Code, Copilot, Codex, Cursor, and any future tool that
  reads `openspec instructions`.
- **Mechanical gates, not advisory text**: schema rules are advice. The bash
  script is the check that actually fails CI.

---

## 12. References

| Document | What it covers |
|----------|----------------|
| `CONSTITUTION.md` | §3 Definition of Done; §4 Conventions; §5 SDLC Workflow; §6 Quality Gates; §7 Testing; §8 Documentation; §9 Git; §10 AI Agents; §11 Release; §13 Tooling Map |
| `AGENTS.md` | Available agents, mandatory workflow, quick reference |
| `CLAUDE.md` | Entry point for Claude Code |
| `.claude/rules/ai-agent-workflow.md` | Step-by-step workflow for agents |
| `.claude/rules/code-quality.md` | JaCoCo, Checkstyle, SpotBugs, Trivy — configuration and thresholds |
| `.claude/rules/ui-ux-design.md` | Design system, tokens, FormContainer |
| `.claude/rules/database-migrations.md` | Flyway — conventions, V{n}__description.sql |
| `docs/300-development/303-testing/TEST-PLAN.md` | Coverage targets, fixture patterns by level |
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | Concrete code: AAA, Test Data Builders, RTL |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | TS-nnnn naming, CU traceability |
| `docs/300-development/CI-PREFLIGHT.md` | Local ↔ CI mapping of each gate |
| `docs/openspec/explore.md` | Permanent finding↔Issue record for the business domain |
| `docs/openspec/schemas/notaire-sdlc/schema.yaml` | Full schema with instructions per artifact |
| `docs/openspec/config.yaml` | Context, per-artifact rules, per-operation guidance |
| `workspace/sdlc/validate-sdlc-plan.sh` | Mechanical gate — `--list` maps each check to a Constitution § |
| `workspace/sdlc/preflight.sh` | Full local gate — `--list` maps local ↔ CI job |
| `workspace/sdlc/run_pipeline.sh` | Pre-PR gate with an HTML dashboard |
| `.github/workflows/pr-validation.yml` | CI — SDLC validation, Spotless, branch naming |
| `.github/workflows/ci.yml` | CI — build, tests, coverage, security |
| `.github/workflows/frontend-ci.yml` | CI — TypeScript, ESLint, Vitest, Next.js |
| `.github/workflows/playwright-e2e.yml` | CI — Playwright E2E + Bruno API |
| `.github/workflows/cd.yml` | CD — Docker image → GHCR |

---

## 13. SDLC orchestration with OpenSpec — parallelization and cost

> This block documents how **OpenSpec** acts as the spec-driven backbone of
> the §5 flow in `CONSTITUTION.md` when work is split across several agents
> or sub-agents in parallel. Each `openspec change` is the atomic unit that a
> lane takes, runs end to end, and closes, so the backlog state is always
> reconstructable by reading `docs/openspec/changes/` plus GitHub Issues,
> without depending on conversation memory. The assistant of the moment
> (Claude Code, Copilot, or another agent that conforms to `CONSTITUTION.md`
> §10) does the execution. OpenSpec does not execute code. It is the contract
> and the record that everyone shares.

### 13.1 Why OpenSpec is the orchestration backbone

`openspec new change "<slug>"` scaffolds the mandatory artifacts
(`proposal.md`, `traceability.md`, `specs/<capability>/spec.md`, `design.md`,
`tasks.md`) according to the `notaire-sdlc` schema. This matters for
parallelization because:

- **Each change is a self-contained, verifiable unit of work**:
  `bash workspace/sdlc/validate-sdlc-plan.sh <slug>` confirms mechanically
  (without AI) that the Issue, the specification, and the tasks are complete
  before any agent starts writing code. Gate 1 does not depend on the agent
  remembering to check it.
- **`tasks.md` is the source of truth for progress**, not the chat history.
  An agent that resumes an interrupted change (rate limit, session restart)
  reads `tasks.md` to see what remains, instead of reconstructing state from
  the conversation.
- **The same contract serves any agent** (Claude Code, Copilot, Codex, or
  another that conforms to Constitution §10). There is no lock-in to a
  concrete execution tool. The fixed parts are the OpenSpec schema and the
  mechanical gates.

### 13.2 Parallelization model — independent lanes

Each Issue is assigned to a **lane** according to the module it touches.
Lanes run in parallel while they do not share files:

| Lane | Modules | Typical executor |
|---|---|---|
| `backend` | `backend-api/src/main` | `mvn`, Checkstyle, Spotless, JaCoCo |
| `backend-test` | `backend-api/src/test` (unit/integration) | `mvn test`, `mvn verify -Ppg-integration` |
| `frontend` | `frontend/src` | `npx tsc`, ESLint, Vitest |
| `api-contract` | `backend-api/api-test/` (Bruno) | `npx @usebruno/cli run` |
| `e2e` | `testing/e2e/tests` | Playwright |
| `docs` | `docs/`, `docs/openspec/` | markdown-lint, `validate-sdlc-plan.sh` |

Parallelization rules (derived from Constitution §5 "Sub-agent delegation is
the default for multi-part work"):

1. **One Issue, one worktree.** Each Issue processed in parallel gets its own
   `git worktree` (`git worktree add ../notaire-issue-<n> -b
   <type>/<n>_<slug> main`), never the shared checkout. That stops two
   concurrent runs from overwriting each other's uncommitted changes (lesson
   from session #984-#995: two agents writing in the same working tree
   without a worktree produced a manual merge of about 135 files).
2. **Independent Issues run in parallel. Issues with overlapping files run
   sequentially.** Before launching the batch, do a quick impact analysis
   (grep the files and packages each Issue is expected to touch) and
   parallelize only the subset with no overlap.
3. **Tests run in parallel with the implementation of *other* Issues**, never
   with the implementation of their own Issue. TDD stays sequential inside
   one Issue: failing test → implement → passing test.
4. **Merge sequentially, one at a time**, with `mvn verify` / `npm run build`
   and the gates re-run against updated `main` before each merge. Never
   assume that two PRs that were green in parallel stay compatible without
   re-checking after the first merge (Constitution Gate 4).

### 13.3 Fewer AI calls — prefer the deterministic toolchain

Principle: **if a deterministic script can answer the question, do not spend
a model call on it.** Applied in this flow:

| Before (depended on AI) | Now (toolchain, 0 AI tokens) |
|---|---|
| "Does the backend compile?" | `mvn -q compile -pl backend-api -am` (exit code) |
| "Do the tests pass?" | `mvn test -pl backend-api` / `npx vitest run` — parse `Tests run:` / the summary JSON |
| "Does it meet the style rules?" | `mvn checkstyle:check` + `mvn spotless:check` — do not ask a model to "review the format" |
| "Is the SDLC plan complete?" | `bash workspace/sdlc/validate-sdlc-plan.sh` (mechanical gate, resolves the Issue live through `gh`) |
| "Is everything ready for the PR?" | `bash workspace/sdlc/run_pipeline.sh` (HTML dashboard, a single exit code) |
| "Which Bruno endpoints fail?" | `npx @usebruno/cli run` + grep of the summary — AI reads only the failure *diff*, not the full log |
| "Is there a merge conflict?" | `gh pr view <n> --json mergeable,mergeStateStatus` |

AI is reserved for what the toolchain cannot decide: designing the test case,
interpreting *why* a test failed, choosing the implementation strategy,
writing the ADR, and reviewing whether the fix is correct.

**RTK (Rust Token Killer)** — already configured in the environment (see
`~/.claude/RTK.md`) — is used transparently for every shell command an agent
runs (`git status`, `mvn test`, large greps). The hook rewrites the command
to cut up to about 90% of the output before it reaches the model context. It
requires no prompt changes. It is an infrastructure layer, not a per-task
decision.

### 13.4 Model routing by task type

This applies the Constitution §5 table "Sub-agent delegation... cost-matched
to task complexity" regardless of which concrete assistant executes each
`openspec change`. The criterion is the same for every provider:

| Complexity | Task examples in this flow | Model level |
|---|---|---|
| Mechanical / find-replace | Mass rename, apply a proven pattern in a new module, run and summarize a fixed script | the cheapest available (for example Haiku) |
| Test and validation | Write or fix unit tests, interpret a suite, triage failures, consistency checks | mid level (for example Sonnet) |
| Standard implementation | A CRUD endpoint that follows an existing convention, a bounded bugfix with a clear root cause | mid level (for example Sonnet) |
| Complex / critical analysis | Architecture decision, ADR, cross-cutting design, anything with a real business or security consequence if it goes wrong | the most capable available (for example Opus) |

Operating rule: **always start at the cheapest level that can resolve the
task** and escalate only if the result does not pass the mechanical gates
(the test still fails, checkstyle is still red). Do not pick the most
expensive model "just in case".

### 13.5 Operating loop — Issue → PR guided by OpenSpec

For each backlog Issue, in the lane that matches it:

```bash
# 1. Dedicated branch in its own worktree (never the shared checkout)
git worktree add ../notaire-issue-<n> -b <type>/<n>_<slug> main
cd ../notaire-issue-<n>

# 2. Spec (Gate 1) — the OpenSpec artifact is the contract the
#    executor (agent or human) follows end to end
openspec new change "<slug>"
bash workspace/sdlc/validate-sdlc-plan.sh   # mechanical gate, 0 AI tokens

# 3. TDD + implementation — the agent of the moment (Claude Code or
#    another that conforms to Constitution §10), cost-matched model (13.4),
#    uses tasks.md as the progress list: write the tests first
#    (they must fail), then the minimum code to pass them, marking each
#    tasks.md item as it is completed — so an agent that resumes the change
#    after an interruption does not need to reconstruct state from chat.

# 4. Gates — all mechanical, 0 tokens except to interpret a real failure
mvn test -pl backend-api && mvn verify -pl backend-api
cd frontend && npx vitest run && npx tsc --noEmit
bash workspace/sdlc/run_pipeline.sh   # Gate 3 required before the PR

# 5. PR + mergeability check (Gate 4) — `gh`, no AI
gh pr create --title "[#<n>] ..." --body "Closes #<n>"
gh pr view <pr> --json mergeable,mergeStateStatus

# 6. Sequential merge + re-check against updated main
gh pr merge <pr> --merge --delete-branch

# 7. Archive the change — fold the delta spec into docs/openspec/specs/
openspec archive <slug>
```

An orchestrator (a human or a coordinating agent) launches step 3 of several
Issues in parallel (independent worktrees plus the sub-agent tool of the
assistant that is coordinating — for example `Agent`/`Task` in Claude Code).
Steps 5–7 are **always sequential**: one PR at a time, re-checking against
`main` after each merge.

### 13.6 Lessons from the reference session

- **Never trust a sub-agent summary without re-running `mvn test` / `mvn
  verify` independently.** In the hexagonal migration (#984-#995) several
  sub-agents reported "tests passing" after running only a subset. The full
  verification found real regressions (`NullPointerException` in stale mocks,
  `LazyInitializationException` on a list endpoint). The orchestrator always
  re-verifies before opening the PR and never trusts the executor's report
  blindly.
- **`git worktree`, not the shared checkout**, for any parallel execution.
  That avoids manual reconciliation of hundreds of files.
- **A test against a real database (H2 is not enough) for anything that
  depends on JPA lazy/eager fetch.** Several serialization bugs appeared only
  when Bruno ran against Postgres with real data, never in the unit tests
  with mocks.
- **Docker with a clean volume (`docker compose down -v`) between Bruno
  runs.** Otherwise DNI/ID collisions from previous runs produce false
  negatives (409) that cannot be distinguished from a real bug.

---

*Last review: 2026-09-18. This document is the single canonical source for
how OpenSpec was installed and adapted in Notaire, including how its
artifacts (`proposal.md` / `traceability.md` / `tasks.md`) are used as the
backbone of a parallel, cost-optimized execution across several agents and
sub-agents. Changes to this document require a PR, like any other process
change (Constitution §12).*
