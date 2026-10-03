> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1040 (audit-2026-09, CU76+CU78, priority:critical, security): protect
`main` with a ruleset — required checks, PR-only, no direct pushes.

Verified on `origin/main` (2026-10-03, tip `ce97e114` after fetch):

| Claim in issue body | Live finding |
|---------------------|--------------|
| `/rulesets` → `[]` | **Stale.** Active ruleset `protect-main` id `24128115` exists (`enforcement: active`, target default branch) |
| Classic protection → 404 | Integration token gets **403** on `/branches/main/protection`; branch JSON shows `"protected": true` (ruleset-driven). Do **not** add classic BP |
| Ruleset completeness | `protect-main` rules today: **only** `deletion` + `non_fast_forward`. Missing: `pull_request`, `required_status_checks` |
| `.claude/rules/hooks.md` | Still says protection “not currently configured” and cites 404 verified 2026-09-22 — **gap text to rewrite** |
| Bot coupling | #1041 still OPEN; CI/CD/E2E still commit reports to `main` with `contents: write` — a PR-only ruleset without bypass would break those pushes until #1041 lands |

### Exact GitHub check-run names (verified PR #1155)

Issue AC lists suite labels: **CI**, **Frontend CI**, **Playwright E2E**,
**Code Lint**, **PR Validation**. Live `gh pr checks` shows **job** names,
not workflow titles. Mapping today:

| AC suite label | Workflow `name:` | Existing check-run names (examples) | Exact name exists today? |
|----------------|------------------|--------------------------------------|--------------------------|
| CI | `CI - Build, Test & Security` | `Build & Compile`, `Unit Tests`, `Integration Tests`, `Coverage Gate (mvn verify)`, `Code Quality`, `Security Scan`, … | **No** check named `CI` |
| Frontend CI | `Frontend CI — Build, Typecheck & Test` | `TypeScript Check`, `Unit Tests (Vitest)`, `Build (Next.js)` | **No** check named `Frontend CI` |
| Playwright E2E | `Playwright E2E — Full Suite` | `UI E2E Tests (Playwright)`, `API Tests (Bruno)`, … | **No** check named `Playwright E2E` |
| Code Lint | `PR Validation` | `Code Lint` | **Yes** |
| PR Validation | `PR Validation` | `Validate PR`, `Code Lint`, `SDLC Plan Validation`, … | **No** check named `PR Validation` |

Fleet merge gate (`scripts/check-heavy-ci.sh`) already requires job names
`Integration Tests`, `Coverage Gate (mvn verify)`, `API Tests (Bruno)`,
`UI E2E Tests (Playwright)` — complementary, not a substitute for the ruleset.

## Goals / Non-Goals

**Goals:**

- Extend `protect-main` so `main` is PR-only, requires the five AC check names,
  and keeps force-push/deletion blocked.
- Make those five strings exist as check-run contexts via thin aggregator jobs.
- Document temporary Actions bypass until #1041; remove bypass when report
  commits stop.
- Update hooks.md / DevSecOps / CHANGELOG.
- Prove desired state with a failing-then-green static test + post-apply assert.

**Non-Goals:**

- Classic branch protection.
- Required approving reviews (would block unattended fleet merges).
- Implementing #1041 / #1042 / #1046 in this change.
- History rewrite.
- Enabling ruleset apply from a non-admin integration token (document admin step).

## Decisions

1. **Extend existing `protect-main` (id 24128115), do not create a second ruleset**
   - Keep `deletion` + `non_fast_forward`.
   - Add `pull_request` (require PR; do **not** require approving review count > 0).
   - Add `required_status_checks` with `strict` branch up-to-date as appropriate
     for this repo’s merge queue practice (default: require checks on the PR
     head; prefer `strict: true` so outdated heads re-run — document if fleet
     friction forces `strict: false`).
   - Target remains default branch (`~DEFAULT_BRANCH` / `main`).
   - Reject: layering classic BP via `enable-gh-secure.sh --with-branch-protection`.

2. **Required checks = exact AC strings via aggregator jobs**
   - Add concluding jobs that succeed only when their suite’s blocking jobs
     succeed, with **exact** `name:` values:
     - `CI` in `ci.yml` — `needs:` at least `unit-tests`, `integration-tests`,
       `coverage`, `quality`, `security`, `build` (and docker-build if it is
       already a PR-blocking job on this workflow).
     - `Frontend CI` in `frontend-ci.yml` — `needs:` `typecheck`, `unit-tests`,
       `build`.
     - `Playwright E2E` in `playwright-e2e.yml` — `needs:` Playwright UI job +
       Bruno API job (`UI E2E Tests (Playwright)`, `API Tests (Bruno)`).
     - `PR Validation` in `pr-validation.yml` — `needs:` `validate-pr`, `lint`
       (`Code Lint`), `sdlc-plan`, `quick-build`, `branch-naming`,
       `dependency-analysis` (exclude comment-only / report-only jobs that may
       be informational).
     - `Code Lint` — **already** the lint job name; do not rename; list it in
       the ruleset as required.
   - Aggregator job body: no-op success step (`run: echo ok`) with
     `if: ${{ !cancelled() && needs.*.result == 'success' }}` pattern (or
     equivalent that fails if any needed job failed/skipped incorrectly).
   - Ruleset `required_status_checks.required_checks` contexts (exact strings):
     1. `CI`
     2. `Frontend CI`
     3. `Playwright E2E`
     4. `Code Lint`
     5. `PR Validation`

3. **Bot bypass: temporary, tied to #1041**
   - **While #1041 is not merged:** allow bypass for the GitHub Actions app
     (and only that app / the dedicated report token if distinct) so existing
     report pushes to `main` do not hard-fail mid-transition.
   - **When #1041 is merged (precondition for implement):** remove the bypass
     entirely in the #1040 apply step — document in tasks that implement MUST
     re-check `gh api …/rulesets/24128115` and leave `bypass_actors` empty
     (or org policy minimum) once bots no longer push.
   - Reject: permanent admin/user bypass for convenience.
   - Reject: delaying the ruleset forever “because bots need push”.

4. **Desired state in git + admin apply**
   - Check in `scripts/rulesets/protect-main.desired.json` (or under
     `docs/200-architecture/208-devsecops/`) describing the target ruleset
     payload.
   - Add `scripts/apply-protect-main-ruleset.sh` (dry-run default; `--apply`
     requires admin) and `scripts/assert-protect-main-ruleset.sh` (read-only
     verification for Gate 5 / CI optional).
   - Integration / fleet tokens without `admin:false` cannot apply — implement
     PR lands workflows + docs + scripts; a human admin (or admin PAT in a
     controlled run) executes `--apply`. Tasks must record that evidence.

5. **hooks.md rewrite (mandatory AC)**
   - Remove the 2026-09-22 “404 Branch not protected / not currently
     configured” claim.
   - State that GitHub ruleset `protect-main` enforces PR-only + required
     checks + no force-push/delete on `main`.
   - Keep the Claude `block-push-to-main` hook as session-level
     defense-in-depth; it is not a substitute for the ruleset.
   - Point to #1040 / DevSecOps for the GitHub-side control.

6. **Serialize after #1041**
   - Implement order: `#1046 → #1042 → #1041 → #1040`.
   - Do not start #1040 implement until #1041 is on `main` (bypass then
     removable). Prefer landing aggregators + ruleset apply with bypass
     already unnecessary.

## Exact protect-main fix (implement checklist)

1. Confirm #1041 merged; tip of `main` has no CI Bot report commits in new runs.
2. Add aggregator jobs (`CI`, `Frontend CI`, `Playwright E2E`, `PR Validation`).
3. Land desired-state JSON + apply/assert scripts + static unittest (TDD).
4. Open PR; wait for the five named checks to appear and pass.
5. Admin: `bash scripts/apply-protect-main-ruleset.sh --apply` (or UI equivalent
   updating ruleset `24128115`) with **no** bot bypass.
6. `bash scripts/assert-protect-main-ruleset.sh` → pass.
7. Update hooks.md + DevSecOps + CHANGELOG.
8. Smoke: direct push to `main` rejected; PR without green required checks
   cannot merge; force-push/delete still blocked.

## Riesgos / Trade-offs

- **[Risk] Aggregator names collide with workflow titles / confuse UI** →
  Use exact AC strings; document in DevSecOps; keep underlying job names.
- **[Risk] Required checks never reported if aggregator `needs` a skipped job** →
  Aggregator `if:` must treat expected path skips carefully; only `needs`
  jobs that always run on `pull_request`.
- **[Risk] Applying PR-only before #1041 bricks bot report pushes** →
  **Mitigation:** implement only after #1041; if emergency earlier, temporary
  Actions bypass then remove immediately after #1041.
- **[Risk] Admin-only API — fleet agent cannot apply** → Scripts + clear
  admin step; Gate 5 blocked until assert passes live.
- **[Risk] `strict: true` causes perpetual re-runs under load** → Start with
  strict; if merge starvation, document flip to non-strict without dropping
  the five checks.
- **[Trade-off] No required reviews** → AC does not demand reviews; fleet
  needs unattended merge; human review remains process/CONSTITUTION, not
  GitHub hard-gate in this issue.
- **[Trade-off] Extend vs replace ruleset** → Extend id `24128115` to preserve
  audit trail and avoid a window with zero rules.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Ruleset requires PR | unit + live assert | `scripts/test_protect_main_ruleset.py` + `assert-protect-main-ruleset.sh` |
| Ruleset requires five exact checks | unit + live assert | same |
| Force-push/deletion blocked | unit (desired JSON) + live assert | same |
| Bypass absent after #1041 | unit + live assert | same |
| Aggregator jobs exist with exact names | unit (YAML parse) | same |
| hooks.md no longer claims unprotected gap | unit (string assert) | same |
| Direct push rejected (smoke) | manual / admin checklist | Gate 5 |

- New unit tests: stdlib unittest + PyYAML parsing workflows + JSON desired state.
- New integration tests: none in Maven; live `gh api` assert is Gate 5.
- Coverage impact (JaCoCo): none (no Java).

TDD: write tests that fail against current `protect-main` (missing
`pull_request` / `required_status_checks`) and against workflows missing
aggregator job names; then implement.

## Regression Strategy

- Existing tests affected: workflow invariant tests if they assume final job
  graphs — update `needs` expectations without weakening lint/Spotless coverage.
- Full suite: `python3 scripts/test_protect_main_ruleset.py`;
  `bash scripts/assert-protect-main-ruleset.sh` (after apply);
  heavy CI `bash scripts/check-heavy-ci.sh <pr>` still required for product PRs.
- HTTP/Bruno: n/a for product API delta (Bruno remains under Playwright suite
  aggregator).

## Playwright Strategy

- n/a — no UI product surface.
- Workflow `playwright-e2e.yml` gains an aggregator job only; product specs
  unchanged.
- PR must still pass repository Playwright / heavy CI before merge.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling:
  1. `#1046 → #1042 → #1041` merged
  2. #1040 PR: aggregators + scripts + docs
  3. Admin applies ruleset (no bypass)
  4. Assert + smoke
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5):
  - `gh api repos/matiaspakua/notaire/rulesets/24128115` shows
    `pull_request` + required checks for the five exact names + deletion +
    non_fast_forward; bypass empty
  - Attempted direct push to `main` fails
  - hooks.md updated

## Rollback Strategy

- Revert safe: **partially** — reverting the git PR removes aggregators/docs
  but does **not** automatically roll back the live ruleset.
- Ruleset rollback: admin re-applies previous ruleset JSON (keep a copy of
  pre-change ruleset in the PR description / apply script backup).
- Database rollback: none needed
- Blast radius if rollback delayed: PRs may require check names that no longer
  exist if aggregators were reverted while ruleset still requires them —
  always roll back ruleset and git together.

## Migration Plan

1. Merge #1041 (and predecessors #1046, #1042).
2. Land #1040 code/docs PR with aggregators + desired JSON.
3. Admin `--apply` ruleset with no bypass.
4. Assert + update issue AC checkboxes.
5. Archive OpenSpec change after Gate 5.

## Open Questions

None material. Admin identity for `--apply` is an operations detail, not a
spec fork: any repo admin with `administration` scope can run the script.
