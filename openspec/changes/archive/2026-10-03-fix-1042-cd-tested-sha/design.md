> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1042 (audit-2026-09, CU76): CD builds the tip of `main` instead of the
SHA CI tested.

Verified on `origin/main` (2026-10-03, tip after fetch):

| Location | Finding |
|----------|---------|
| Trigger | `workflow_run` of `"CI - Build, Test & Security"` on `main` (`types: [completed]`) + `v*` tags + `workflow_dispatch` |
| Job `build-and-publish` `if` | `github.event_name != 'workflow_run' \|\| github.event.workflow_run.conclusion == 'success'` — **already satisfies AC3 skip** |
| Checkout (build job) | `actions/checkout@v7` with **no `with.ref`** — defaults to tip of default branch for `workflow_run` |
| Metadata tags | `type=ref,event=branch` + `type=semver` + `type=sha,prefix=` + `type=raw,value=latest,enable={{is_default_branch}}` |
| Push | Single `docker/build-push-action` with `tags: ${{ steps.meta.outputs.tags }}` — SHA + `latest` pushed together |
| `github.sha` under `workflow_run` | Last commit on default branch at CD schedule time — **not** `workflow_run.head_sha` |
| Wiki publish job | Checks out `ref: main` and may push docs commits (related tip drift; out of image-build scope) |
| Related queue | `#1044 → #1051 → #1046 → #1042` — this draft is prep-only until #1046 merges |

## Goals / Non-Goals

**Goals:**

- Pin `build-and-publish` checkout to the CI-tested SHA on `workflow_run`.
- Tag the published image with that SHA; move `latest` only after SHA push OK.
- Keep skipping publish when CI conclusion ≠ `success`.
- Prove the three ACs with a failing-then-green static YAML unittest.
- Document the contract in DevSecOps README + CHANGELOG.

**Non-Goals:**

- Changing Trivy/SBOM/cosign/signing steps or registry auth.
- Redesigning wiki/report jobs (except documenting they must not redefine the
  image build SHA).
- Frontend image publishing (#1043).
- Implementing before #1046 merges.

## Decisions

1. **Checkout `ref` for build-and-publish**
   ```yaml
   - name: Checkout code
     uses: actions/checkout@v7
     with:
       ref: ${{ github.event.workflow_run.head_sha || github.sha }}
   ```
   - Why: AC1 requires `workflow_run.head_sha`. Empty `head_sha` on tag /
     `workflow_dispatch` falls back to `github.sha`.
   - Alternative rejected: bare `${{ github.event.workflow_run.head_sha }}`
     with no fallback — breaks tag and manual publish checkouts.
   - Equivalent accepted: explicit ternary on `github.event_name == 'workflow_run'`.

2. **Explicit publish SHA step (do not trust metadata `type=sha` alone)**
   ```yaml
   - name: Resolve publish SHA
     id: publish_sha
     run: |
       if [ "${{ github.event_name }}" = "workflow_run" ]; then
         echo "sha=${{ github.event.workflow_run.head_sha }}" >> "$GITHUB_OUTPUT"
       else
         echo "sha=${{ github.sha }}" >> "$GITHUB_OUTPUT"
       fi
   ```
   - Why: under `workflow_run`, metadata-action’s `type=sha` / `github.sha`
     tracks tip-of-default-branch, which is the bug class #1042 describes.
   - Tags for the immutable push MUST include
     `type=raw,value=${{ steps.publish_sha.outputs.sha }}` (short SHA optional
     only if also keeping full SHA; prefer full 40-char for exact match).

3. **Two-phase tag push: SHA first, then `latest`**
   - Phase A — build once, push immutable tags (raw publish SHA; keep
     branch/semver as today if still meaningful for tag/dispatch; **omit
     `latest`** from this push).
   - Phase B — after Phase A succeeds, retag the same digest as `latest` and
     push (e.g. second metadata + `docker/build-push-action` with
     `tags: …:latest` and `push: true`, or `docker buildx imagetools create`
     from `steps.build.outputs.digest`).
   - Why: AC2 — `latest` only moves after SHA-tagged push succeeds.
   - Alternative rejected: keep single multi-tag push — atomic failure modes
     do not satisfy the ordered “SHA then latest” acceptance wording.

4. **Keep existing success gate; assert it in tests**
   - Retain
     `if: github.event_name != 'workflow_run' || github.event.workflow_run.conclusion == 'success'`
     on `build-and-publish`.
   - Do not weaken publish-report / wiki guards that also check conclusion.

5. **TDD via static YAML unittest**
   - Add `scripts/test_cd_pin_tested_sha.py` (pattern:
     `scripts/test_ci_workflow_invariants.py` / `scripts/tests/test_workflow_concurrency.py`)
     asserting: checkout `ref` expression contains `workflow_run.head_sha`;
     publish SHA / raw tag wiring; `latest` not in the first push tags and a
     later step applies `latest` (or step `needs`/order proves gating); job
     `if` retains success conclusion.
   - Prove red on current `origin/main` before editing `cd.yml`.

6. **Serialize after #1046**
   - Coordinator queue `#1044 → #1051 → #1046 → #1042`. Avoid concurrent
     heavy-CI PRs racing runners.

## Exact pin-to-SHA fix (implement checklist)

Edit **only** `.github/workflows/cd.yml` (plus tests/docs):

1. `build-and-publish` → Checkout: add `with.ref` as in Decision 1.
2. Add **Resolve publish SHA** step (Decision 2) before metadata.
3. Split metadata/build-push:
   - **meta-immutable** (or rename `meta`): tags include raw publish SHA;
     **remove** `type=raw,value=latest,…` from this set.
   - **build** push uses immutable tags only; capture `digest`.
   - **meta-latest** / **Push latest tag**: only when default-branch /
     `workflow_run` path should update `latest`; tag digest as `latest` after
     build step success.
4. Leave job-level `if` success gate unchanged.
5. Downstream SBOM/scan/cosign continue to use `steps.build.outputs.digest`
   (digest from the SHA push).
6. Tag/dispatch paths: checkout fallback `github.sha`; still produce a SHA tag
   from `github.sha`; `latest` rules follow existing `is_default_branch` /
   tag semantics without publishing an untested tip under `workflow_run`.

## Riesgos / Trade-offs

- **[Risk] metadata-action still emits tip SHA via `type=sha`** → Decision 2:
  prefer explicit raw publish SHA; drop or override `type=sha` if it would
  label the wrong commit.
- **[Risk] Two pushes race with another CD run** → Acceptable; each run pins
  its own `head_sha`; last successful SHA push that also completes latest-move
  wins `latest` (correct for sequential merges).
- **[Risk] Wiki bot moves `main` after image publish** → Documented; image
  remains pinned to tested SHA; do not re-checkout tip for build.
- **[Risk] Short vs full SHA tags break existing consumers** → Prefer full SHA
  raw tag; if short SHA was previously produced by `type=sha`, keep a short
  alias only if tests prove it matches `head_sha` prefix.
- **[Trade-off] Expression `A \|\| B` vs event_name ternary** → Either OK if
  tests assert `head_sha` appears in checkout `ref` for the `workflow_run` path.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Checkout pins `workflow_run.head_sha` | unit | `scripts/test_cd_pin_tested_sha.py` |
| Image tagged with publish SHA | unit | same |
| `latest` after SHA push | unit | same (ordered steps / separate latest) |
| Skip on non-success CI | unit | same (job `if`) |
| DevSecOps docs updated | review | PR checklist |

- New unit tests: stdlib unittest + PyYAML (same as existing CI invariant scripts).
- New integration tests: none required (workflow config).
- Coverage impact (JaCoCo): none (no Java).

## Regression Strategy

- Existing tests affected: none expected; if
  `test_ci_workflow_invariants.py` or report-job dependency tests parse
  `cd.yml`, update without weakening asserts.
- Full suite: `python3 scripts/test_cd_pin_tested_sha.py`;
  `bash scripts/preflight.sh` as applicable; heavy CI
  `bash scripts/check-heavy-ci.sh <pr>`.
- Confirm tag + `workflow_dispatch` paths still checkout a concrete SHA.
- HTTP/Bruno: n/a for product API delta.

## Playwright Strategy

- No UI product change. No new Playwright scenarios.
- PR still must pass repository heavy CI (includes Playwright) before merge.
- Mark product E2E n/a for this capability; do not skip the PR Playwright job.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge workflow change → next successful CI on
  `main` triggers CD → image for that `head_sha` published → `latest` moved
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): inspect the next CD run on `main` —
  checkout ref / logs show `workflow_run.head_sha`; GHCR has that SHA tag;
  `latest` digest equals that image; a simulated/observed non-success CI does
  not publish

## Rollback Strategy

- Revert safe: yes — revert `cd.yml` (+ test/docs) to prior tip-checkout
  behavior; no schema/data change
- Database rollback: none needed
- Data written under the new behavior after revert: GHCR tags already pushed
  remain; operators may retag `latest` manually if needed
- Blast radius if rollback delayed: low — pin-to-SHA is strictly safer than tip
  checkout; worst case is delayed `latest` if phase B fails after phase A
  (SHA tag still correct)

## Migration Plan

1. Wait for **#1046** merge to `main` (after #1044 and #1051).
2. Copy this draft into `openspec/changes/fix-1042-cd-tested-sha/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh fix-1042-cd-tested-sha`.
4. Branch `cursor/fix-1042-cd-tested-sha-69d3` → failing CD invariant tests →
   implement `cd.yml` pin + split latest → green tests → docs → PR
   `Closes #1042`.
5. After merge: Gate 5 observe next CD run SHA vs CI `head_sha`.

## Open Questions

- Prefer `docker buildx imagetools create` vs second `build-push-action` for
  `latest` — choose the smaller/clearer approach at implement time; AC only
  requires ordering.
- Whether short SHA tags must remain for existing scripts — keep if cheap and
  derived from the same publish SHA; otherwise full SHA only is acceptable.
