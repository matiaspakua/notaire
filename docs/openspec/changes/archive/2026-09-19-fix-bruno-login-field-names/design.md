> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`playwright-e2e.yml`'s "API Tests (Bruno)" job authenticates once up front
(the Bruno collection sends `Authorization: Bearer {{token}}` on every
request, and folders run alphabetically, so the token can't be captured
mid-run) via a raw `curl` to `POST /api/v1/usuarios/login`. `UsuarioController
#login` has taken `{name, password}` since #977; this workflow step still
sends `{nombre, contrasenia}` and was never updated because #977's rename
swept `.java`/`.ts` sources, not CI YAML.

## Goals / Non-Goals

**Goals:**
- Make the Bruno login curl call use the real field names so the job can
  obtain a token and run the collection.

**Non-Goals:**
- Changing `UsuarioController#login`'s contract (unchanged, correct).
- Auditing or fixing the Bruno collection's own request bodies (already
  fixed under #1006/#1007 for the app-side field names it exercises).

## Decisions

- **Fix the workflow file directly, one string literal.** No abstraction,
  helper script, or shared step is justified for a single hardcoded curl
  call already isolated in its own step.
- **Do not add a CI-level regression test for this.** The failure mode is a
  string literal in a YAML `run:` block; the only meaningful regression test
  is running the workflow itself, which Gate 4 (CI green on the PR) already
  provides. Adding a shell script whose only job is to assert two strings are
  equal would be theater.

## Riesgos / Trade-offs

- [Another workflow step could regress the same way in the future] →
  Mitigated by the Acceptance Criterion grep (`contrasenia`/stale `nombre`
  login payload) run once now; not a standing CI gate, since a permanent
  grep-based gate for one already-fixed rename is disproportionate to the
  risk. Accepted as a one-time check, documented in `tasks.md`.

## Testing Strategy

`skip_specs: true` — no delta spec scenarios exist to map. Verification
substitutes a live reproduction for the missing unit/integration surface
(see traceability.md's Exceptions section):

| Verification | Level | Method |
|---------------|-------|--------|
| Login payload accepted, token returned | manual / CI | `curl` the corrected payload directly against a running backend before editing the workflow, to confirm the fix payload is correct in isolation |
| Bruno job passes end-to-end | CI (E2E) | GitHub Actions run on the PR — "API Tests (Bruno)" job must go green |
| No other stale occurrence | static check | `grep -rn "contrasenia" .github/workflows/` |

- New unit tests: none — no application code changed.
- New integration tests: none.
- Coverage impact (JaCoCo ratchet floor): none — `backend-api` is untouched.

## Regression Strategy

- Existing tests affected: none.
- Full suite command: not applicable to this change (no backend/frontend
  source changed); backend/frontend suites are unaffected and were already
  green on `main` per #1006/#1007.
- HTTP/Bruno API suite: this change's entire point is making
  `backend-api/api-test/` (Bruno) runnable again in CI; run locally via the
  same command the workflow uses (see `tasks.md`).
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. This change touches only `.github/workflows/playwright-e2e.yml`; the Playwright job in that same workflow already passes and is untouched.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — CI-only change, nothing to deploy to
  an application environment
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): the "smoke test" for this change is the
  next `main`-branch CI run showing the "API Tests (Bruno)" job green

## Rollback Strategy

- Revert safe: yes — a plain `git revert` restores the previous (broken)
  string; no other state depends on this line.
- Database rollback: none needed.
- Data written under the new behavior after revert: none — this only affects
  a CI job's ability to authenticate, not any persisted data.
- Blast radius if rollback is delayed: none beyond the status quo (the
  Bruno job stays red, exactly as it is today).

## Migration Plan

Not applicable — single-file, single-line change with no staged rollout.
