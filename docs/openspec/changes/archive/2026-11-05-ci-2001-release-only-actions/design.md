> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Heavy workflows already mirror `workspace/sdlc/preflight.sh`. `cd.yml` publishes after every successful CI run on `main` via `workflow_run`, and again on `v*` / `notaire-v*` tags. Release Please creates the tag with `GITHUB_TOKEN` and dispatches `cd.yml`, because that token does not start `on.push.tags` workflows (#1599). `deploy-github-page.yml` waits for the CI workflow on `main`. Path-filter jobs already force every output true when the event is not `pull_request`, so a tag run is a full suite.

## Goals / Non-Goals

**Goals:**

- One shared trigger for the heavy product workflows: `push` tags `v*` and `notaire-v*`, plus `workflow_dispatch`.
- Stop GHCR publishes on merge to `main` by removing `workflow_run` from `cd.yml`.
- Keep Release Please on push to `main`, and keep its tag dispatch so a `GITHUB_TOKEN` release still publishes and still starts the heavy suite.
- Keep Pages deploying on every push to `main`.
- Make `preflight.sh --full` the mandatory pre-push and pre-PR command, and stop describing a default run as enough to open a PR.

**Non-Goals:**

- Rewriting job bodies, shard counts, or aggregator check names.
- Applying the live ruleset in this change.
- Disabling Release Please, cleanup, ZAP, k6, or backup workflows.

## Decisions

1. **Shared tag trigger, not `workflow_run`.** A single `on.push.tags` plus `workflow_dispatch` is enough for a human-pushed tag. It is not enough for a Release Please tag, because `GITHUB_TOKEN` does not start `push.tags` workflows. The release-please job therefore dispatches the heavy workflows and `cd.yml` on the tag. Alternative rejected: leave `workflow_run` so every main merge still publishes images.
2. **`:latest` moves on a version tag.** Today `:latest` moves on `workflow_run` (every green main) or when the ref is the default branch. Release Please dispatches CD with `--ref` set to the tag, so that default-branch clause does not move `:latest` on a cut. After `workflow_run` is removed, `:latest` moves when `github.ref` is `refs/tags/v*` or `refs/tags/notaire-v*`, still only after the immutable SHA push. Manual dispatch on the default branch still moves it.
3. **Checkout is `github.sha`.** There is no `workflow_run.head_sha` once that trigger is gone. Tag and `workflow_dispatch` already use `github.sha` as the commit being published.
4. **PR Validation jobs stay, but only when a pull_request payload exists.** Title, body, and base SHA are not on a tag event. Those jobs gain `if: github.event_name == 'pull_request'` so a tag run skips them instead of failing on an empty title. The checks already run in preflight. Alternative rejected: delete the workflow; the Owner asked to retarget it, not remove the jobs.
5. **Database V&V drops workflow `paths`.** GitHub does not apply `paths` to tag pushes; leaving them would suppress the release run. The former path list stays in a comment. Local `--full` still runs the suite on every PR.
6. **Pages deploy does not wait for CI.** CI no longer runs on push to `main`. The portfolio site deploys on that push directly. Product packages do not.
7. **CodeQL keeps Monday 06:30 UTC.** Preflight cannot run CodeQL. The weekly schedule is the between-release scan. Tags and manual dispatch stay.
8. **Desired ruleset drops the five required checks.** Job names `CI`, `Frontend CI`, `Playwright E2E`, `Code Lint`, and `PR Validation` stay in the workflow files for a tag run. The desired JSON stops requiring them on pull requests. The Owner applies that file after merge (#1040). This change does not call the ruleset API.
9. **`check-heavy-ci.sh` treats a missing suite as "not run on pull requests".** If a heavy workflow was actually queued or failed on the head, the script still fails. An empty check list is no longer a merge block by itself; the local `--full` run is the gate. The live ruleset can still block until the Owner updates it.
10. **Pre-push hook passes `--full`.** `PREFLIGHT_FAST=1` remains an explicit narrow escape and is not sufficient to open a PR.

## Riesgos / Trade-offs

- [Live ruleset still requires checks that no longer run] → Owner must apply `security/rulesets/protect-main.desired.json` after merge (#1040). Until then GitHub refuses every merge. Called out in the PR body. This change does not apply the ruleset.
- [A release-please tag does not itself start `push.tags` workflows] → The dispatch step starts them with `workflow_dispatch`, which `GITHUB_TOKEN` is allowed to do.
- [CD no longer waits for a GitHub CI success on the tag] → The gate moved to local `--full` before the commits that Release Please will cut. The dispatched CI runs in parallel as a release record.
- [Skipped `--full` suites (no Docker)] → Those skips no longer say CI will cover them. Database V&V and the Docker smoke test fail `--full` when Docker is down.
- [`copilot-setup-steps.yml` still has `pull_request`] → Only when that file itself changes (`paths`). Left as-is. `opencode.yml` runs on comments, not on every PR.
- [Main health no longer runs daily] → Process checks run in preflight on every PR and on a version tag / manual dispatch. A drift between releases is not scanned every day. Accepted to save minutes.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Heavy workflows are tag and dispatch only | unit | `workspace/tests/test_ci_workflow_invariants.py` |
| Playwright has no weekday schedule | unit | `workspace/tests/test_ci_workflow_invariants.py` |
| Pages deploy on push to main, not workflow_run | unit | `workspace/tests/test_ci_workflow_invariants.py` |
| CD has no workflow_run and pins github.sha | unit | `workspace/tests/test_cd_pin_tested_sha.py` |
| CD keeps v* and notaire-v* | unit | `workspace/tests/test_release_asset_attachment.py` |
| Release Please still dispatches cd.yml | unit | `workspace/tests/test_release_asset_attachment.py` |
| OpenAPI contract is not on pull_request | unit | `workspace/tests/test_dast_contract_backup_assets.py` |
| Process checks are not on push-to-main or a daily cron | unit | `workspace/tests/test_sdlc_process_on_main.py` |
| Pages CI is not a pull_request trigger | unit | `workspace/tests/test_pages_site_shell.py` |
| Database workflow still names the suite paths | unit | `testing/tests/test_testing_standalone.py` |
| Desired ruleset drops the five required checks | unit | `security/tests/test_protect_main_ruleset.py` |

- New unit tests (`src/test/java/.../unit/`): none (no Java change)
- New integration tests (`src/test/java/.../integration/`): none
- Coverage impact (JaCoCo ratchet floor; 80% target): none

TDD: the workflow assertions above are updated and observed failing before the workflow files change.

## Regression Strategy

- Existing tests affected: the files in the table. Old expectations ("runs on every PR and every push to main", "CD pins workflow_run.head_sha") are wrong because the Owner approved the new trigger policy (#2001).
- Full suite command: `python3 workspace/tests/test_ci_workflow_invariants.py` and the other files in the table. `mvn verify -pl backend-api` is not required: no backend source changes.
- HTTP/Bruno API suite: n/a — no API change
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none

## Playwright Strategy

n/a - no UI surface. This change does not alter screens, routes, or client state. Playwright still runs locally via `preflight.sh --full` and on a version tag via `playwright-e2e.yml`.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not run for this change

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge does not publish images. The next version tag (Release Please) dispatches CD. Pages deploy on the merge to `main` by itself.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): after the Owner merges and applies the ruleset, open a docs-only PR and confirm GitHub does not require `CI` / `Frontend CI` / `Playwright E2E` / `Code Lint` / `PR Validation`. Confirm the next `v*` tag dispatches CD and the heavy workflows. Confirm Pages updates on the merge without a tag.

## Rollback Strategy

- Revert safe: yes. Reverting restores per-PR workflows and `workflow_run` publish. Images already pushed to GHCR stay.
- Database rollback: none needed
- Data written under the new behavior after revert: GHCR tags from any release cut in between stay; revert does not delete them.
- Blast radius if rollback is delayed: pull requests stay unmergeable until the ruleset is updated or this change is reverted (which brings the checks back).

## Migration Plan

1. Merge this PR (Owner only).
2. Owner applies `bash security/apply-protect-main-ruleset.sh --apply` and confirms with `bash security/assert-protect-main-ruleset.sh` (#1040).
3. Next Release Please cut dispatches CD and the heavy workflows on the tag.

## Open Questions

None. The Owner approved the trigger policy before this change. The `GITHUB_TOKEN` tag limitation is handled by extending the existing dispatch step, not by a new policy.
