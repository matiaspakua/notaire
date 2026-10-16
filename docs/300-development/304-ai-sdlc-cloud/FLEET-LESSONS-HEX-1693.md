# Fleet lessons from hexagonal epic #1693

> Durable do/don’t for Cursor Cloud agents. Evidence: FE drain (#1844–#1846),
> R2 deed/report (#1854/#1855), Mac local validation, CI stewardship.
> Index: [`README.md`](README.md). Companion: [`MAC-LOCAL-VS-CI.md`](MAC-LOCAL-VS-CI.md),
> [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md), `.claude/skills/steward/SKILL.md`.

## Steward merge cadence

| Do | Don't |
|----|-------|
| Prefer **one heavy-CI PR at a time**; wait for `check-heavy-ci.sh` exit 0 | Open the next product tip while another heavy suite is queued |
| **merge `origin/main`** into a pushed branch; confirm with `git ls-remote` | **never rebase** / force-push a pushed branch |
| After each land, **sync open tips** with new `main` before more work | Start the next domain slice before the current PR merges |
| Mark ready only when **Process Checks** + OpenSpec archive hygiene are green | Treat light-only green or advisory checks as merge authority |

### Process Checks before `gh pr ready`

1. `python3 workspace/sdlc/check-openspec-archive-hygiene.py origin/main`
2. Tick OpenSpec task groups **3–8**; archive in the PR (`openspec archive … --yes`) unless `multi_pr: true`
3. Spotless / format: `mvn spotless:apply -pl backend-api` if Java touched
4. If architecture or test inventory drifted: `python3 workspace/sdlc/generate-testing-inventory.py` and commit `INVENTORY.md`
5. `bash workspace/sdlc/check-heavy-ci.sh <pr>` exit 0 on the **current head**

## CHANGELOG `[Unreleased]` / Release Please fence

- Only **one** open PR may edit `CHANGELOG.md` `[Unreleased]` at a time.
- While a Release Please / release-cut PR holds Unreleased, **omit** product Unreleased entries (note in the Issue) or wait for the cut to land.
- Never commit on `release-please--branches--*`.

## Hex / R2 slice definition (backend)

- **One domain (use-case slice) per PR** — e.g. report, then deed, then management. Do not mix.
- Ports and use cases speak **domain views** only: **no entity** / Spring Data type on port signatures.
- Commit the **shrunk** ArchUnit baseline in the same PR; never hand-edit upward.
- Do not open the next R2 domain Issue/PR until the current one is merged (or explicitly held).

## FE suppressions ratchet — done criteria

- Hex FE drain is **complete** only when `frontend/eslint-suppressions.json` is `{}` (**count 0**).
- A non-empty file means work remains; do not declare FE hex Done on epic paste while count > 0.

## Mac local vs CI

See [`MAC-LOCAL-VS-CI.md`](MAC-LOCAL-VS-CI.md). Trust Mac for ArchUnit / unit / pg-integration / Spotless / FE typecheck+lint+Vitest+build / security `--profile local` when freshly re-run on tip. Still let CI own CodeQL, full Bruno, Playwright, DAST/ZAP, image build/smoke, and `gh`-dependent Process Checks (can flake locally).

## Issue hygiene when the bot cannot close

Fleet App / agent `gh` often returns **403** on label edit, board Status, or issue close. Then:

1. Leave a **human paste** comment (issue number, desired Status/labels, close reason, PR link).
2. Do **not** invent Owner product scope or open a **probe issue** “to check write access.”
3. Prefer `Closes #<n>` on the PR so merge auto-closes when permissions allow.

## Preflight `gh` flake

`validate-sdlc-plan.sh` may once print `Issue #N was not found on GitHub` under transient API/lookup failure. **Re-run** the validator (it retries). Do not treat a single flake as a missing Issue or product regression.

## Anti-patterns (observed)

| Anti-pattern | Instead |
|--------------|---------|
| Probe / canary Issues to test permissions | Human paste; continue other work |
| Starting the next domain before merge | Hold the queue; serialize |
| Trusting advisory / light checks as merge-ready | `check-heavy-ci.sh` + Process Checks |
| Mixing several R2 domains in one PR | One slice, one baseline shrink |
| Declaring FE hex Done while suppressions remain | Require `{}` / count 0 |
| Inventing product fixes when tip is behind `main` | merge `origin/main` first, re-run suites |

## Related

- Epic [#1693](https://github.com/matiaspakua/notaire/issues/1693) · Issue [#1858](https://github.com/matiaspakua/notaire/issues/1858) · CU76
- Earlier pacing proposals: [#1418](https://github.com/matiaspakua/notaire/issues/1418) (Owner list; complement, do not duplicate)
