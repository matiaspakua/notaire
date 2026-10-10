# Repository Split Plan

Implementation plan for issue #1197, based on the evidence and critique in
[ADR-024](../200-architecture/202-ADR/ADR-024-repository-topology.md). Status: **proposed**; the Owner
decides between the staged plan below and the original eight-repository proposal (section 9).

## 1. Goals and measures

| Goal in #1197 | Measure | Baseline (2026-10-05) | Target |
|---------------|---------|-----------------------|--------|
| Faster feedback for docs, OpenSpec and agent-only PRs | wall-clock to last required check, no-code PR | about 12 min (full pipeline) | 3 min or less |
| Faster feedback for code PRs | wall-clock to last required check, code PR | 12 min (Playwright E2E 11 min 05 s) | 8 min or less |
| Smaller agent context | tokens always loaded (`CLAUDE.md` plus imported rules) | about 16,000 | 8,000 or less |
| Smaller clone | fresh `--depth 1` clone size and `size-pack` | `size-pack` 130 MiB; `deprecated/` 13.4 MB tracked | recorded in P0.1, then set by the Owner |
| Independent ownership | share of PRs that cross backend and frontend | 18% (since 2026-04-25) | tracked monthly; a split of those two needs below 5% |
| Typed backend-frontend contract | generated client, drift guard in CI | none (hand-written types) | generated, guarded |

P0.1 records these in [`REPO-METRICS-BASELINE.md`](REPO-METRICS-BASELINE.md) (generator:
`python3 workspace/ci/repo-metrics.py`) so each later decision cites numbers. Folder ownership
inside the monorepo is documented in [`MODULE-OWNERSHIP.md`](MODULE-OWNERSHIP.md) (ADR-026).

## 2. Phase 0: inside the monorepo (no split)

Each item is one issue, one OpenSpec change, one PR, independent of the Owner's topology decision.

| Id | Issue | Work | Acceptance |
|----|-------|------|------------|
| P0.1 | #1256 / #1417 | `workspace/ci/repo-metrics.py` prints offline git/tree metrics; baseline committed as `REPO-METRICS-BASELINE.md` | Script runs offline; unit test; doc updated (path under `workspace/` per ADR-026 — not `scripts/`) |
| P0.2 | #1257 | Path classifier job (`changes`) in `ci.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, `openapi-contract.yml`; docs, OpenSpec and agent-only PRs skip Java, Vitest and Playwright; required check names unchanged through aggregator jobs that succeed when their inputs are skipped | Path filter + aggregator `success\|skipped` + invariant guards on `cursor/ci-1257-path-scoped-cf98`; AC = docs-only PR green with no Java/E2E leaves; backend change still runs everything it affects |
| P0.3 | #1258 | Shard the Playwright suite across a matrix (3 shards) and merge reports | **Done** — squash-merged as #1425 (`d3eed416`); 3-shard matrix + fail-closed `e2e-merge-reports` keeps check name `UI E2E Tests (Playwright)` |
| P0.4 | #1259 | Move rules that are needed only for some areas out of the always-loaded imports (path-scoped guidance in `AGENTS.md`/skills); keep Constitution, workflow and general rules | **Done** — squash-merged as #1424 (`6e817569`); always-loaded context ~2k tokens via `agent-context-budget.py`; `check-agent-rules.sh` green |
| P0.5 | #1260 | Generate TypeScript API types from `backend-api/openapi/openapi.yaml` (openapi-typescript), replace hand-written DTO types incrementally, CI fails on drift | **Done** — squash-merged as #1423 (`5fc5d635`); `api.generated.ts` + Frontend CI drift check; gestiones/presupuestos/documentos/dashboard aliases |
| P0.6 | #1261 | Owner decision on `deprecated/` (764 files, 13.4 MB): archive behind tag `archive-monorepo-pre-split` and remove from the tree; decide whether to run the history purge deferred by ADR-022 | Decision recorded in ADR-022; if approved, tree and guards updated |
| P0.3 | #1258 | Shard the Playwright suite across a matrix (3 shards) and merge reports | E2E job wall-clock 6 min or less; report merged; flake rate unchanged over 10 runs |
| P0.4 | #1259 | Move rules that are needed only for some areas out of the always-loaded imports (path-scoped guidance in `AGENTS.md`/skills); keep Constitution, workflow and general rules | Always-loaded context 8,000 tokens or less, measured by P0.1; `check-agent-rules.sh` green |
| P0.5 | #1260 | Generate TypeScript API types from `backend-api/openapi/openapi.yaml` (openapi-typescript), replace hand-written DTO types incrementally, CI fails on drift | **In progress** on `cursor/feat-1260-openapi-ts-types-cf98`: `api.generated.ts` + drift check + gestiones/presupuestos/documentos/dashboard aliases |
| P0.6 | #1261 | Owner decision on `deprecated/` (~767 files, 13.4 MB): archive behind tag `archive-monorepo-pre-split` and remove it from the tree; decide whether to run the history purge deferred by ADR-022 | **Packaged** — ADR-022 §Pending Owner decision lists Option A/B/C; Pages Architecture links ADR-022; waiting Owner choice (no delete/rewrite until then) |

`LICENSE` (#1226) is a prerequisite for every extraction and tracked there.

## 3. Gates for every extraction

An extraction starts only when all gates pass; each is verified by a command or a file, not by opinion.

1. **Standalone guard green** in the monorepo (`scripts/test_<area>_standalone.py`).
2. **Seams documented**: every file, variable, image name or endpoint the satellite shares with the
   core is listed in the satellite's `docs/` and has a guard.
3. **PR-level verification preserved**: the core PR still reports a required check that exercises the
   satellite's behaviour at a pinned ref (section 6). No deferred verification.
4. **Repository prerequisites ready**: `LICENSE`, `SECURITY.md`, `CODEOWNERS`, ruleset, CI, secrets,
   Dependabot, release configuration.
5. **References preserved**: history filtered with commit messages rewritten to
   `matiaspakua/notaire#N` (section 7); open issues for the area transferred or relinked.
6. **Rollback ready**: tag `pre-extract-<area>` on the core and a read-only stub left in place for one release.
7. **Owner approval** recorded in the issue.

## 4. Phase 1: `notaire-infra`

Why first: the guard (#1179) exists, the area is 40 files, the seam is narrow (image names,
environment variable names and the actuator and Prometheus endpoints), and nothing in the product
build imports from it.

| Step | Detail |
|------|--------|
| 1 | Pass gates 1 to 4; decide the owner namespace (user account or a new organisation) |
| 2 | Extract with the recipe in section 7 using `--path infra/ --path-rename infra/:` |
| 3 | In the new repository add CI (`kustomize build`, `docker compose config`, k6 smoke), ruleset, `LICENSE`, `SECURITY.md`, `CODEOWNERS` |
| 4 | In the core, replace `infra/` with a stub `README.md`, point `workspace/stack/start-all.sh` and `docker-compose.prod.yml` consumers to `NOTAIRE_INFRA_DIR`, keep `docker-compose*.yml` for the dev stack |
| 5 | Move `infra/tests/test_infra_standalone.py` and the infra part of `workspace/sdlc/preflight.sh` to the new repository; the core keeps a seam guard that checks image names and variable names |
| 6 | Update `CLAUDE.md`, `docs/200-architecture/208-devsecops`, ADR-024 status table |
| Exit | Core CI green without `infra/`; `bash workspace/stack/start-all.sh` works with `NOTAIRE_INFRA_DIR`; infra CI green; rollback tag exists |

## 5. Phase 2: local AI engine

`local-ai/` (63 files, the autonomous engine) has no product consumer, so it can become
`notaire-local-ai` after gates 1, 2, 4, 5 and 6. Agent definitions, skills and rules stay in the core
because agents read `CLAUDE.md` and `.claude/` from the repository they run in and CI enforces them
(`check-agent-rules.sh`, `sdlc-process.yml`). Extracting those requires a source-of-truth repository
with a sync workflow and a drift guard; do it only when a second project consumes them.

## 6. Phase 3: `notaire-testing`

Benefit is ownership and release independence of the QA suites, not speed: the Playwright job stays
on the PR critical path. Start only if the Owner wants a separate QA lifecycle.

Required design (gate 3): the core PR calls a reusable workflow from the testing repository.

```yaml
# core: .github/workflows/e2e.yml (sketch)
jobs:
  e2e:
    uses: matiaspakua/notaire-testing/.github/workflows/e2e.yml@<pinned-sha>
    with:
      app-ref: ${{ github.sha }}
```

The reusable workflow runs in the caller's context, checks out the core at `app-ref`, builds and
starts the stack exactly as `playwright-e2e.yml` does today, and checks out the testing repository
at the pinned SHA for the specs. The result is a required check on the core PR, so nothing is
deferred, and a spec change is a PR in the testing repository followed by a pin bump in the core.

Further conditions:

- Constitution sections 4 and 7 amended (Owner-reviewed PR, section 12).
- Bruno API tests and `pg-integration` stay with the backend module (#1190); a reversal is a
  separate Owner decision recorded in #1190, not a side effect of this plan.
- `testing/tools/generate_e2e_coverage_report.py` and the CU-API matrix validator parameterised with
  `NOTAIRE_APP_ROOT` and `NOTAIRE_TESTING_ROOT`.
- A compatibility table in the testing repository maps spec tags to core releases.

## 7. Extraction recipe

Run on a fresh clone, never on the working repository.

```bash
git clone --no-local https://github.com/matiaspakua/notaire.git notaire-<area>-split
cd notaire-<area>-split
git filter-repo --analyze                      # review the largest blobs first
git filter-repo \
  --path <area>/ --path-rename <area>/: \
  --prune-empty always \
  --message-callback 'import re
return re.sub(rb"(?<![\w/])#(\d+)", rb"matiaspakua/notaire#\1", message)'
git remote add origin git@github.com:<owner>/notaire-<area>.git
git push -u origin main                        # new repository only; no --force on existing ones
```

Differences from the playbook in #1197: no copying into the clone before filtering (filter-repo
deletes unlisted paths), `--path-rename` so history is kept under the new root, issue references
stay unambiguous, no force push to an existing repository, no `deprecated-*` paths, current
directory names (the Playwright suite is `testing/e2e`).

Open issues for the area move with `gh issue transfer <n> <owner>/notaire-<area>` (same owner
only); pull requests are closed or re-opened. Commit SHAs cited in `CHANGELOG.md` and OpenSpec
traceability tables are not rewritten: they keep pointing at the archived tag.

## 8. Rollback

- Before each extraction tag the core `pre-extract-<area>`.
- Keep the stub in the core for one release; reverting is restoring the directory from the tag and
  archiving the new repository.
- If any metric in section 1 regresses after an extraction (PR wall-clock, cross-repository PR
  count, red `main` days), stop further extractions and review ADR-024.

## 9. If the Owner chooses the eight repositories

All of these must exist first; each is a Phase 0 or gate item above:

1. P0.2, P0.3 and P0.5 done (scoped CI and a typed contract).
2. Cross-repository PR gates for backend, frontend and testing (section 6 pattern for each pair).
3. A versioned OpenAPI artifact published per backend release and a compatibility matrix.
4. `LICENSE`, `SECURITY.md`, `CODEOWNERS`, rulesets, secrets, Dependabot, release-please per repository.
5. SAST, SCA and Sonar run in the repository that holds the source; `notaire-security` limited to
   policies, DAST against staging and ruleset definitions.
6. A rule for atomic cross-cutting changes (correlated PRs, merge order, `Closes owner/repo#n`).
7. A decision on where the 288 open issues live and a transfer plan.
8. A second human team per repository; otherwise the cost has no owner.

Backend and frontend should be split only if the cross-area PR ratio stays below 5% for three
months, the contract is generated and gated, and two teams release on different cadences.
`notaire-docs` and `notaire-workspace` are not recommended before the guards are parameterised
and three or more repositories exist.

## 10. Risk register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| Path-scoped CI lets a breaking change through | medium | high | aggregator jobs; classifier tested in `test_ci_workflow_invariants.py`; full run on `main` |
| Required-check names change and block merges | medium | high | keep names; roll out on one workflow first |
| Extraction breaks a guard that reads across areas | high | medium | gate 2 and the seam guard; run `workspace/sdlc/preflight.sh` on both sides |
| Referenced commit SHAs and issue links go stale | certain | low | archive tag; message rewrite; transfer issues |
| Spec and app versions drift (testing repository) | medium | high | pinned SHA, compatibility table, bump PRs |
| Owner overhead from several repositories | high | medium | extract only gated areas; metrics decide |
| Public repository exposes extracted history | low | high | `git filter-repo --analyze` and secret scan before pushing |

## 11. Issue map

| Item | Issue |
|------|-------|
| Umbrella, decision and criteria | #1197 |
| P0.1 metrics | #1256 |
| P0.2 path-scoped CI | #1257 |
| P0.3 E2E sharding | #1258 |
| P0.4 agent context | #1259 |
| P0.5 generated API types | #1260 |
| P0.6 `deprecated/` and history purge decision | #1261 |
| `LICENSE` prerequisite | #1226 |
| Testing standalone (phases 1 to 3) | #1190 |
| Infra standalone | #1179 |

Phase 1 to 3 issues are created when the Owner approves the staged plan.

## Navigation

- [ADR-024](../200-architecture/202-ADR/ADR-024-repository-topology.md)
- [Development docs](README.md)
