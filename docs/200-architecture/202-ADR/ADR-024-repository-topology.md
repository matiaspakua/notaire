# ADR-024: Repository Topology — Evidence-Gated Decomposition

**Status:** Proposed — awaiting Owner decision
**Date:** 2026-10-05
**Deciders:** Owner (issue #1197 / CU76)
**Related:** ADR-021, ADR-022, #1179 (infra standalone), #1190 (testing standalone), #1242 (assessment)

## Context

Issue #1197 proposes splitting the monorepo into eight repositories
(`notaire-workspace`, `-backend`, `-frontend`, `-infra`, `-testing`, `-security`, `-ai`, `-docs`)
and moving every high-level test out of the application repositories. Its goals are valid: faster
feedback, smaller agent context, independent ownership, a smaller clone. This ADR tests the
proposal against `main` @ `610fa7b0` and records a topology that reaches those goals at lower risk.

### Measured evidence

| Question | Measurement on `main` |
|----------|-----------------------|
| Who maintains it? | One human maintainer plus AI agents and bots (all-time commits: Owner 886, CI Bot 472, dependabot 77) |
| History size | 1,723 commits; `.git` 167 MB, `size-pack` 130 MiB; tracked `deprecated/` is 13.4 MB in 764 files; `docs/` is 15.6 MB |
| Where does CI time go? (PR #1254, 12 min to last required check) | Playwright E2E 11 min 05 s (starts PostgreSQL, the backend jar and the frontend, then runs the suite); Integration 3 min 14 s; Coverage gate 3 min 10 s; Docker build 3 min 17 s; Bruno 1 min 49 s; Unit tests **56 s** |
| Is Testcontainers the backend bottleneck? | No. Testcontainers appears in 2 test files; 66 integration test classes run on H2; 8 use PostgreSQL |
| Do workflows skip irrelevant work? | No. 2 of 17 workflows have `paths:` filters. Of 125 PRs merged since 2026-09-01, **51 (41%) touch no backend, frontend or testing code** yet run the full Java and Playwright pipeline |
| How often does one change cross the proposed boundaries? (370 PR merges since 2026-04-25) | backend and frontend in the same PR: 18%; code and docs: 32% (40% since September); code and an OpenSpec change: 17% (44% since September) |
| What does the agent always load? | `CLAUDE.md` plus eight rule files: about 16,000 tokens. The "150k tokens" figure is not supported; agents read files on demand |
| How coupled are the guards? | 11 scripts under `scripts/` read three or more top-level areas (docs, backend, frontend, testing, openspec, infra, local-ai); the ERD and data dictionary generators read Flyway migrations, the CU-API matrix validator reads the matrix in `docs/` and the backend controllers |
| Contract between backend and frontend | `backend-api/openapi/openapi.yaml` is committed and diffed in CI; the frontend uses hand-written types (`frontend/src/types/index.ts`), no generated client |
| Prior Owner decisions | #1190 (2026-10-03): Bruno API tests and `pg-integration` stay with the backend module; k6 stays in `infra/`; the E2E suite moves to `testing/` and the real repository split is a later, separate phase |
| Repository facts | Public repository owned by a user account (no organisation `notaire-org`), no `LICENSE` file (#1226) |

## Options

| Option | Description | Verdict |
|--------|-------------|---------|
| A | Stay a monorepo; fix CI scoping, agent context and size inside it | Necessary first step in every option |
| B | Core monorepo plus **satellite** repositories extracted only when gates are met | **Proposed** |
| C | Eight repositories as in #1197 | Rejected as specified (see critique) |
| D | Backend and frontend split, rest stays | Rejected for now: shares the 18% atomic-change cost without the satellites' benefits |

## Decision (proposed)

1. **Do phase 0 inside the monorepo**: baseline metrics, path-scoped CI, trimmed agent context,
   generated frontend types from the committed OpenAPI contract, size reduction. These deliver most of
   the stated benefits at no coordination cost.
2. **Keep the core together** in `notaire`: `backend-api`, `frontend` (`notaire-shared` was retired, ADR-025), business and
   architecture `docs/`, `docs/openspec/`, `CONSTITUTION.md`, the SDLC guards in `scripts/`, Bruno API
   tests, Flyway migrations and the schema guard test. They change together and are released together.
3. **Extract satellites in order of independence, each behind explicit gates**:
   `notaire-infra` (guard from #1179 exists), then the local AI engine (`local-ai/`), then
   `notaire-testing` (E2E, stack smoke, database V&V) once cross-repository PR gating exists.
4. **Do not create** `notaire-security`, `notaire-docs` or `notaire-workspace` now. Security scanning
   must run where the source is; business documentation is part of the atomic change under
   Constitution Gate 3; a workspace control plane is justified only with three or more repositories.
5. Record every extraction in this ADR's status table and in the plan
   ([REPO-SPLIT-PLAN.md](../../300-development/REPO-SPLIT-PLAN.md)).

## Critique of the original proposal

Severity: **C** critical (changes the decision), **M** major (must be fixed in any version).

| # | Sev | Finding | Evidence |
|---|-----|---------|----------|
| 1 | C | Reverses recorded Owner decisions made one hour earlier | #1190 keeps Bruno and `pg-integration` in the backend module; #1197 moves both |
| 2 | C | Removes the PR-level gate for backend and frontend | Backend and frontend CI become fast only because verification is deferred. Branch protection cannot require a check from another repository's workflow unless that result is reported on the PR. The proposed "Mode B" pulls images from GHCR, which `cd.yml` publishes only after CI succeeds on `main`, so a PR image does not exist. Regressions reach `main` first |
| 3 | C | The speed premise is wrong | Backend unit tests already run in 56 s; the critical path is the Playwright job (11 min), which moves to another repository but still must run before release. No Testcontainers bottleneck exists (2 files) |
| 4 | C | Atomic change is lost | 18% of PRs span backend and frontend, 32% to 40% span code and docs, 17% to 44% span code and OpenSpec. The Constitution requires tests, docs and the regenerated OpenAPI artifact in the same PR. One change becomes up to five correlated PRs with merge-order rules; `Closes #n` and issue numbers become ambiguous across repositories |
| 5 | C | `notaire-security` cannot do its job | CodeQL (java-kotlin, javascript-typescript, actions), Trivy filesystem scan and Sonar analyse source and must run in the repository that contains it. Rulesets are per-repository settings; storing JSON elsewhere does not apply it |
| 6 | C | The migration playbook is broken | It copies `e2e-playwright/` and `api-bruno/` into the clone and then runs `git filter-repo --path ...`, which deletes every path not listed, including the copies; it uses the pre-#1190 Playwright locations under `frontend/`, which now live in `testing/e2e`; `deprecated-frontend-swing/` and `deprecated-src.old/` do not exist (`deprecated/` does); `gh repo create --confirm` is obsolete and the organisation does not exist; moved directories lose history without `--path-rename`; deleting `*FlywaySchemaValidationIntegrationTest` removes the guard required by `.claude/rules/database-migrations.md` |
| 7 | C | The coverage gate would fail | JaCoCo enforces 80% line / 65% branch over unit and H2 integration tests; "unit and MockMvc only" does not meet it |
| 8 | M | Rewriting history breaks references | Commit SHAs cited in 288 open issues, `CHANGELOG.md` and OpenSpec traceability tables change; PRs, issues, Actions history, releases and tags do not move; the plan does not say where the 288 open issues go |
| 9 | M | Contract-first is not in place | No generated client, no mocks, no published versioned OpenAPI artifact, no compatibility matrix. The repository split cannot precede them |
| 10 | M | Guards cross areas | 11 scripts read three or more areas; ERD and data dictionary generators need Flyway migrations, the matrix validator needs `docs/` and the backend controllers. Splitting `docs` or `testing` breaks them unless each is parameterised first |
| 11 | M | Team assumptions have no staffing basis | The "eight teams" simulation maps to one maintainer and agents; eight rulesets, eight sets of secrets, Dependabot and CodeQL configurations and release trains are overhead for one person |
| 12 | M | Facts are stale or unmeasured | "CU01-CU80+" (87 use cases), "TS-0001 to TS-0096" (99), "ADR-001-022" (23), "Flyway V1..V12" (V42), `api/negocio/service` packages (now hexagonal), "150k tokens" (16k always loaded), "12-25 min" CI (12 min measured) |
| 13 | M | Wrong Use Case | CU-77 is operations monitoring and CU-75 database management; neither covers repository topology. CU76 (QA and testing infrastructure) is the closest fit |
| 14 | M | Acceptance criteria are not verifiable | "approved by stakeholders", "validated" without a metric, no baseline, no go/no-go gate, no rollback |
| 15 | M | Cheaper alternatives were not weighed | Path-scoped CI, test sharding, agent-context trimming, removing 13 MB of `deprecated/`, sparse checkout and worktrees give most of the benefit without a split |
| 16 | M | Missing prerequisites | `LICENSE` (#1226), `SECURITY.md`, `CODEOWNERS`, rulesets, secrets and image-tag policy per repository; release-please is configured for one package and one `CHANGELOG.md` |
| 17 | M | `notaire-ai` extraction ignores runtime use | Claude Code and other agents read `CLAUDE.md` and `.claude/` from the repository they run in; CI scripts (`check-agent-rules.sh`, `sdlc-process.yml`) enforce them. A source-of-truth repository needs vendoring plus a drift guard |

## Consequences

- The Owner decides between A+B (proposed) and the original C. The plan holds the gates that make
  the decision measurable: each extraction proceeds only when its gates pass and is reversible for one
  release through a read-only stub.
- Phase 0 benefits land immediately and independently of the decision.
- The repository stays a single atomic-change unit for the product core; satellites change on their
  own cadence.

## Extraction status

| Satellite | Gate status | Repository |
|-----------|-------------|------------|
| infra | not started (standalone guard exists) | — |
| local AI engine | not started | — |
| testing | not started | — |

## Navigation

- [Plan](../../300-development/REPO-SPLIT-PLAN.md)
- [ADR index](README.md)
