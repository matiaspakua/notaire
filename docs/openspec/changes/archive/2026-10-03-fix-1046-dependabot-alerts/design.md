> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1046 (audit-2026-09, CU78): Dependabot stays red because of dead Swing
`log4j` and a frontend `smol-toml` finding.

Verified on `origin/main` tip (2026-10-03, after `git fetch origin main`):

| Location | Finding |
|----------|---------|
| `deprecated-frontend-swing/` | **Still present** on `origin/main` (hundreds of files); README marks DEPRECATED; excluded from root reactor |
| `frontend-swing/` | **Does not exist** as a top-level path (renamed to `deprecated-frontend-swing`) |
| Root `pom.xml` | Modules are only `notaire-shared` + `backend-api` (no Swing) |
| `deprecated-frontend-swing/pom.xml` | Direct dep `log4j:log4j:1.2.17` (EOL; multiple CRITICAL/HIGH GHSA, no 1.x patch) |
| `.github/dependabot.yml` | Maven ecosystem at `/` — nested Swing POM is still discoverable for alerts |
| `frontend/package-lock.json` | `smol-toml@1.8.0` via `markdownlint-cli2@0.23.3` (devDependency) |
| GHSA-7w5x-hrqm-74c2 | Vulnerable `smol-toml` `<=1.7.0`; first patched `1.7.1` (so 1.8.0 is already ≥ patch; AC still asks upgrade/override — pin latest `1.9.0`) |
| Issue #585 | **Still OPEN**; body targets `src.old` cleanup; audit comment links Swing/log4j/#1046 for priority — not a substitute for this PR’s Swing delete |
| `deprecated-src.old/` | Still on `main` — out of scope (belongs to #585 track) |

Serialize: implement only after **#1044** and **#1051** merge. If any E2E
touches are needed (unexpected), serialize Playwright with other heavy PRs.

## Goals / Non-Goals

**Goals:**

- Remove `deprecated-frontend-swing/` so Maven Dependabot log4j alerts from that
  tree close.
- Force frontend lockfile to a safe `smol-toml` (≥1.7.1; prefer 1.9.0) via
  minimal `overrides` (or parent bump if a non-breaking bump appears).
- Align CODEOWNERS/docs with “Swing gone from the tree.”
- Leave Security tab without these critical/high noise alerts.

**Non-Goals:**

- Deleting `deprecated-src.old/` / closing #585.
- Fixing unrelated npm audit findings (`braces`, etc.).
- Reintroducing or “fixing” Swing to compile against current DTOs.
- Backend Log4j2 upgrades (active stack uses modern logging — out of AC).

## Decisions

1. **Delete `deprecated-frontend-swing/` rather than bump/replace log4j**
   - Why: Module is dead, out of reactor/CI, CLAUDE.md forbids recreating it.
     Log4j 1.x has no safe 1.2.x patch for the critical advisories; upgrading
     a dead client wastes effort and keeps Dependabot scanning a fake surface.
   - Alternative rejected: replace with `reload4j` / Log4j2 inside Swing —
     implies maintaining dead code.
   - Alternative rejected: Dependabot `ignore` rules — hides rather than fixes;
     AC requires alerts resolved.

2. **#1046 owns Swing deletion; #585 stays separate for `src.old`**
   - Why: #585 body/AC are about the legacy `src.old` tree (now
     `deprecated-src.old/`). Audit cross-links confused ownership; design makes
     ownership explicit so implement does not wait on #585 closure.
   - If coordinator later wants one mega-cleanup PR, widen via an explicit
     follow-up — do not silently expand this Gate 1 pack.

3. **Minimal smol-toml fix: npm `overrides` → `1.9.0` (or `^1.9.0`)**
   - Path today: `frontend` → `markdownlint-cli2@0.23.3` → `smol-toml@1.8.0`.
   - `markdownlint-cli2` latest is still `0.23.3` (no upstream bump available).
   - Why override: smallest diff; does not force `--force` downgrades of
     markdownlint; satisfies “upgraded/overridden” AC even if Dependabot
     already considers 1.8.0 patched for GHSA-7w5x-hrqm-74c2.
   - Alternative rejected: remove `markdownlint-cli2` — out of scope / breaks
     lint tooling.
   - Implement must re-run `npm install` under `frontend/` and commit the
     lockfile so `node_modules/smol-toml` resolves to the overridden version.

4. **Stale path hygiene in the same PR (KIS, only live pointers)**
   - Update `.github/CODEOWNERS` (`/frontend-swing/`), root README tree line,
     and any **non-archive** doc that still says the deprecated directory is
     present. Do not rewrite all of `docs/000-archive/**`.

5. **TDD for dependency hygiene via failing guards, not product JUnit**
   - Prefer a small script/unit check that fails while Swing POM / vulnerable
     lockfile entry exist, then pass after delete/override (same pattern as
     prod-compose static tests). Optionally assert with `npm ls smol-toml` /
     lockfile JSON parse in CI-friendly script under `scripts/` or
     `frontend/` test.

## Riesgos / Trade-offs

- **[Risk] Someone still clones Swing for archaeology** → Git history retains
  the tree; README/CHANGELOG point to the commit; do not recreate on `main`.
- **[Risk] Dependabot alerts linger after delete until rescan** → Smoke:
  confirm Security tab / `gh` alerts after merge; re-run Dependabot if needed.
- **[Risk] Override fights a future markdownlint peer range** → Pin only
  `smol-toml`; re-check `npm ls` / install on implement; prefer caret `^1.9.0`.
- **[Risk] #585 stakeholders expect Swing delete under that issue** → Cross-link
  in PR body: Swing delete closes #1046; #585 remains for `deprecated-src.old`.
- **[Risk] Accidental product Playwright churn** → Plan is deps/docs only; keep
  E2E n/a. Still serialize behind #1051 because heavy CI shares runners.
- **[Trade-off] Leaving `deprecated-src.old/`** → Acceptable; out of AC; keeps
  PR reviewable and avoids blocking on #585 scope debates.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| `deprecated-frontend-swing/` absent | unit/script | `scripts/test_dependabot_hygiene.py` (or equiv.) |
| No `log4j:log4j` in tracked Maven POMs | unit/script | same |
| Lockfile `smol-toml` ≥ 1.7.1 (expect 1.9.x) | unit/script | same / `npm ls smol-toml` |
| `package.json` declares override | unit/script | same |
| CODEOWNERS has no `/frontend-swing/` | unit/script or review | same / PR checklist |
| Frontend install still works | command | `cd frontend && npm ci` |
| Markdown lint still runnable | command | existing frontend lint script if applicable |

- New unit tests: stdlib script asserting tree absence + lockfile version.
- New integration tests: none required for backend.
- Coverage impact (JaCoCo): none expected (no Java product code).

## Regression Strategy

- Existing tests affected: none expected in `backend-api`.
- Full suite command: `mvn test -pl backend-api` (sanity); frontend
  `npm ci` + `npm test` / lint as preflight requires.
- HTTP/Bruno API suite: n/a for product API delta (`bash testing/scripts/test.sh`
  only if preflight `--full` is run).
- Legacy paths at risk: intentional removal of `deprecated-frontend-swing`;
  ensure root `mvn clean install -pl backend-api -am` still works.

## Playwright Strategy

- n/a — no UI product surface (dependency + dead-tree deletion + docs).
- Repository heavy CI still runs Playwright on the PR; do **not** skip that job.
- Serialize implement after #1044 and #1051; if unexpected E2E edits appear,
  serialize with other Playwright-heavy PRs per fleet playbook.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge to `main` → Dependabot rescan → verify
  Security tab
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): confirm `deprecated-frontend-swing` absent
  on `main`; `frontend/package-lock.json` shows `smol-toml` ≥ 1.7.1; Security /
  Dependabot shows no open critical/high for these findings

## Rollback Strategy

- Revert safe: yes — `git revert` restores the deleted tree and prior lockfile;
  no schema/data impact
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: low (dev/prod runtimes do not deploy Swing)

## Migration Plan

1. Wait for **#1044** and **#1051** to merge to `main`.
2. Copy this draft into `openspec/changes/fix-1046-dependabot-alerts/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh fix-1046-dependabot-alerts`.
4. Branch `cursor/fix-1046-dependabot-alerts-69d3` → failing hygiene tests →
   delete Swing tree + npm override + docs → green → PR `Closes #1046`.
5. After merge: confirm Dependabot/Security tab cleared for these alerts.

## Open Questions

- Whether to add a Dependabot `ignore` for historical noise — **no** unless
  delete+override somehow leave a false positive; prefer fix over ignore.
- Whether CODEOWNERS cleanup alone is enough vs touching archived docs —
  prefer live paths only (Decision 4).
