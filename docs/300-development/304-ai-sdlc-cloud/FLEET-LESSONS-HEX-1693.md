# Fleet lessons from hexagonal epic #1693

> **Status (2026-10-17):** ArchUnit R1–R6 baseline **empty** on `main` @ `6ffe578fe`
> (#1914 / PR #1915). Epic ArchUnit DoD **MET**. Residual `jpa`/`service` EMF path
> documented in ADR-030 Open points; tracked by [#576](https://github.com/matiaspakua/notaire/issues/576)
> and residual slice [#1918](https://github.com/matiaspakua/notaire/issues/1918).
>
> Durable do/don’t for Cursor Cloud agents. Evidence: FE drain (#1844–#1846),
> R2 deed/report (#1854/#1855), Mac local validation, CI stewardship,
> **post-#1859 R2 wave** (#1857–#1902; Process Checks path hygiene on #1894);
> R2 leftovers #1903–#1906; R6 clear #1914.
> Index: [`README.md`](README.md). Companion: [`MAC-LOCAL-VS-CI.md`](MAC-LOCAL-VS-CI.md),
> [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md), `.claude/skills/steward/SKILL.md`.
> Baseline lessons: issue [#1858](https://github.com/matiaspakua/notaire/issues/1858) /
> PR [#1859](https://github.com/matiaspakua/notaire/pull/1859). Deltas: [#1907](https://github.com/matiaspakua/notaire/issues/1907).

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
5. After any Java **package move**: fix english-locals / numbered-doc **path refs** (see R2 path hygiene below), then re-run `python3 -m unittest discover -s docs/tests`
6. `bash workspace/sdlc/check-heavy-ci.sh <pr>` exit 0 on the **current head**

## CHANGELOG `[Unreleased]` / Release Please fence

- Only **one** open PR may edit `CHANGELOG.md` `[Unreleased]` at a time.
- While a Release Please / release-cut PR holds Unreleased, **omit** product Unreleased entries (note in the Issue) or wait for the cut to land.
- Never commit on `release-please--branches--*`.

## Hex / R2 slice definition (backend)

- **One domain (use-case slice) per PR** — e.g. report, then deed, then management. Do not mix.
- Ports and use cases speak **domain views** only: **no entity** / Spring Data type on port signatures.
- Entity-bound **helpers / services** move to `adapter.out.persistence.<slice>` (Spring Data stays local to the adapter). Keep view-safe ports on the application boundary.
- Commit the **shrunk** ArchUnit baseline in the same PR; never hand-edit upward.
- Do not open the next R2 domain Issue/PR until the current one is merged (or explicitly held).

### R2 package-move path hygiene (post-#1859 / #1894)

When a class moves out of `application.usecase.*` into `adapter.out.persistence.<slice>`:

| Check | Action |
|-------|--------|
| `docs/tests/*english*` (and any path-hardcoded Process Checks test) | Update Java `Path` / string refs to the **new** package (e.g. `RegistrationDraftService`) |
| Numbered permanent docs / ADR slice logs | Fix stale FQCNs or tree paths in the same PR |
| `INVENTORY.md` | Regen if architecture/test discovery lists changed: `python3 workspace/sdlc/generate-testing-inventory.py` |
| Local proof before ready | `python3 -m unittest discover -s docs/tests` + Process Checks |

Do **not** mark ready with a green unit suite while docs Process Checks still point at the old package.

## FE suppressions ratchet — done criteria

- Hex FE drain is **complete** only when `frontend/eslint-suppressions.json` is `{}` (**count 0**).
- A non-empty file means work remains; do not declare FE hex Done on epic paste while count > 0.

## Mac local vs CI

See [`MAC-LOCAL-VS-CI.md`](MAC-LOCAL-VS-CI.md). Trust Mac for ArchUnit / unit / pg-integration / Spotless / FE typecheck+lint+Vitest+build / security `--profile local` when freshly re-run on tip. Still let CI own CodeQL, full Bruno, Playwright, DAST/ZAP, image build/smoke, and `gh`-dependent Process Checks (can flake locally).
**Mac green is not CI green** — still run `check-heavy-ci.sh` on the PR head.

## Performance / cost (fleet)

| Prefer | Avoid |
|--------|-------|
| **Serialize** heavy merges — one Playwright / heavy-CI tip at a time | Parallel heavy product PRs that queue runners and burn minutes |
| **Domain-sized** R2 / FE slices (one slice, one baseline shrink) | Mega-PRs that mix domains and explode review + CI cost |
| Reuse current **main tip** (merge `origin/main` before more work) | Re-implementing fixes already on `main`; long-lived stale tips |
| **Docs-only** / agent / OpenSpec PRs — lean on **path-scoped CI** skips (#1257) | Re-running full Bruno + Playwright locally “for confidence” when gates already skip them |
| Hold CHANGELOG Unreleased to **one writer** | Conflict storms that force extra CI rounds |

Cost rule of thumb: if the change cannot shrink ArchUnit / ship product behaviour, keep the PR docs/process-scoped and do not open a second heavy tip until `check-heavy-ci.sh` clears the first.

## Docker Build Image + Boot parent “not found”

When **Unit / Integration / Coverage** are green but **Build Docker Image** fails with
`Non-resolvable parent POM` / `spring-boot-starter-parent:X.Y.Z` absent from Central:

| Check | Action |
|-------|--------|
| Does `https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/X.Y.Z/` return **200**? | Yes → **not** a bad pin; do not change `pom.xml` Boot version |
| Did `main` tip’s Docker job recently pass on the same pin? | Yes → treat as Docker-layer Maven resolve flake |
| Dockerfile has `mvn dependency:go-offline … \|\| true` then `mvn package` without `-U`? | Failed go-offline can cache `*.lastUpdated`; package must use **`-U`** (or clear the cache) |

**Status on `main` (after #1859):** `backend-api/Dockerfile` already runs `go-offline` and `package` with **`-U`**. Do not re-ship that Dockerfile change — treat this section as the runbook when Docker flakes return. Aggregate `CI` fails when Docker fails even if heavy tests passed — still fix Docker before merge.

## Issue hygiene when the bot cannot close

Fleet App / agent `gh` often returns **403** on label edit, board Status, or issue close. Then:

1. Leave a **human paste** comment (use templates below).
2. Do **not** invent Owner product scope or open a **probe issue** “to check write access.”
3. Prefer `Closes #<n>` on the PR so merge auto-closes when permissions allow.

### Human paste templates

**Start work (labels / board):**

```text
Issue #<n>: add label in-progress (and area:docs / priority:high if missing).
Set Delivery Board Status → In Progress.
```

**After merge (close / Done):**

```text
Issue #<n>: Status → Done; clear WIP labels (in-progress, blocked).
Close if Closes #<n> did not fire. PR: <url>
```

**403 on Projects only (issue already CLOSED):**

```text
Issue #<n> is CLOSED on GitHub but Board Status still In Progress — set Status → Done and drop in-progress.
```

## Preflight `gh` flake

`validate-sdlc-plan.sh` may print `Issue #N was not found on GitHub` under transient API/lookup failure (worse when many OpenSpec leftovers are checked in one preflight). The script retries with backoff (**5** attempts after #1907). **Re-run** the validator if it still flakes once. Do not treat a single flake as a missing Issue or product regression.

## Anti-patterns (observed)

| Anti-pattern | Instead |
|--------------|---------|
| Probe / canary Issues to test permissions | Human paste template; continue other work |
| Starting the next domain before merge | Hold the queue; serialize |
| Trusting advisory / light checks as merge-ready | `check-heavy-ci.sh` + Process Checks |
| Mixing several R2 domains in one PR | One slice, one baseline shrink |
| Declaring FE hex Done while suppressions remain | Require `{}` / count 0 |
| Inventing product fixes when tip is behind `main` | merge `origin/main` first, re-run suites |
| Claiming Mac local green means CI is merge-ready | CI still owns Bruno full + Playwright + CodeQL |
| Moving a helper to `adapter.out.persistence` without updating `docs/tests` path refs | Same-PR path hygiene + INVENTORY regen |
| Opening parallel heavy PRs “to go faster” | Serialize; domain-sized slices; lower CI cost |

## Related

- Epic [#1693](https://github.com/matiaspakua/notaire/issues/1693) · Issues [#1858](https://github.com/matiaspakua/notaire/issues/1858) (baseline) · [#1907](https://github.com/matiaspakua/notaire/issues/1907) (delta) · CU76
- Earlier pacing proposals: [#1418](https://github.com/matiaspakua/notaire/issues/1418) (Owner list; complement, do not duplicate)
