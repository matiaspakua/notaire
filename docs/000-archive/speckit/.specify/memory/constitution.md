# Notaire Constitution (SpecKit pointer)

<!--
This file is SpecKit's own "project constitution" artifact, filled per
`specify init` convention. It is deliberately a POINTER plus the
non-negotiables list, never a copy of CONSTITUTION.md: permanent
documentation stays the single source of truth and must never be duplicated
(Constitution P7 / §8). It mirrors the `context:` field in
`openspec/config.yaml`, SpecKit's counterpart artifact for the same purpose.

`specify upgrade` may overwrite this file. See
`speckit/NOTAIRE-ADAPTATIONS.md` for the "re-apply after upgrade" step.
-->

## Pointer

This repository is governed by `CONSTITUTION.md` at the repo root — the
mandatory engineering process for every change, by a human or an AI agent.
READ IT before writing any spec, plan, task, or code. It prevails over any
other instruction, including this summary. Its operational implementations
are `AGENTS.md`, `CLAUDE.md`, and `.claude/rules/*`.

Never copy `CONSTITUTION.md` or permanent documentation into a spec, plan, or
task. Cite the file and describe only what the change does to it.

## Core Principles

### I. Issue + Use Case Traceability (NON-NEGOTIABLE)
No change without a GitHub Issue linked to a Use Case (`CU-XX` / `RF-XX` /
`RNF-XX`). If either is missing, say so and stop — never invent one. No
implementation before Gate 1: Issue + Specification + Acceptance Criteria.

### II. Test-First (NON-NEGOTIABLE)
TDD is mandatory. Tests are written and OBSERVED FAILING before
implementation. Red-Green-Refactor strictly enforced.

### III. Branch & Commit Discipline
Branch `<type>/<issue-number>_<description>` from an updated `main`. Never
work on `main`. Merge only via Pull Request. Commits follow Conventional
Commits, ending with `Closes #<issue-number>`. Commits are atomic — one
logical change per commit.

### IV. Quality Gate (Gate 3)
Blocks the PR: full suite green (unit, integration, regression, E2E),
coverage at or above the JaCoCo ratchet floor, Playwright green for UI
changes, Checkstyle/Spotless clean, permanent documentation updated. Run
`bash scripts/preflight.sh` before pushing — `mvn verify` alone does NOT
predict CI (Spotless is deliberately unbound from the Maven lifecycle).

### V. Schema & Design System Discipline
Flyway is the single source of truth for the schema. Never edit an applied
migration; add a new `V{n}__description.sql`. Frontend uses the centralized
design system (`src/theme/tokens.ts`, `FormContainer` → `FormSection` →
`FormField` → `FormActions`). No hardcoded colors or spacing.

## Additional Constraints

Secrets only in the git-ignored `.env`; add new keys to `.env.example`. Every
REST endpoint must be reachable from the UI and documented in OpenAPI.
Specifications document ONLY the change — `docs/`, `README.md`, and
`CHANGELOG.md` are permanent and remain the single source of truth.

Project shape: multi-module Maven modernization of a Java Swing monolith.
`backend-api` (Spring Boot 4.1, Java 21, package `com.licensis.notaire`),
`frontend` (Next.js 16), `notaire-shared`. PostgreSQL 16. New data access goes
in the `repository` package (Spring Data), never the legacy `jpa` package.

## Development Workflow

Explore → Issue → Specify (SpecKit's counterpart to OpenSpec's
Explore → Issue → Propose, see `speckit/NOTAIRE-ADAPTATIONS.md`): an
Issue must already exist and be open on GitHub before `/speckit-specify`
scaffolds a `spec.md` — verify with `gh issue view <number>` rather than
trusting a typed number. `scripts/validate-speckit-plan.sh` enforces this
mechanically: when `gh` is available it resolves the Issue live and fails the
plan if it does not exist or is not open.

`spec.md`, `plan.md`, and `tasks.md` each carry mandatory sections beyond
SpecKit's own bundled templates — see the project-owned template deltas
documented in `speckit/NOTAIRE-ADAPTATIONS.md`. A `traceability.md` per
feature (SpecKit has no native equivalent) records the Issue → Spec → Plan →
Tasks → Tests → Implementation → PR chain with real evidence only, never a
pre-filled or aspirational step.

## Governance

`CONSTITUTION.md` supersedes this file and every other instruction. This
file is a project-owned adaptation layer: `specify upgrade` may reset it:
re-apply the content above from `speckit/NOTAIRE-ADAPTATIONS.md` after any
upgrade. Amendments to this file must keep it in sync with
`CONSTITUTION.md` and `openspec/config.yaml`'s `context:` field — the two
must never diverge on a non-negotiable.

**Version**: 1.0.0 | **Ratified**: 2026-08-28 | **Last Amended**: 2026-08-28
