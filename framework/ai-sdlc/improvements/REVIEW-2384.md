# Review of the factory dashboard session (#2384)

This reviews [SESSION-FEATURES.md](SESSION-FEATURES.md) (the dashboard built under #2381) and
[ai_engineering_coach_full_audit.md](ai_engineering_coach_full_audit.md) against the factory UI
spec (`docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md`). It records what was kept,
what was rebuilt and why, and what to do next. The decision record is
[ADR-006](../docs/adr/ADR-006-dashboard-contract-client.md).

## What the review found

| # | Finding | Evidence | Effect |
|---|---------|----------|--------|
| 1 | The dashboard read and wrote the kit's files directly | `src/server/storage.ts` read `.aisdlc/evidence`, `.aisdlc/runs`; `config-store.ts` wrote `.claude/` | No token, scope, CSRF or actor; bound to one disk and one machine |
| 2 | Runs the factory never ran were on screen | demo events in `App.tsx` | Breaks the run honesty rule: only adapters move a run past queued |
| 3 | Controls the contract does not have | play, pause, resume, stop, skip | Buttons that could not work through `aisdlc serve` |
| 4 | One project, with its paths hard-coded | links and paths in `App.tsx`, `FileViewerView.tsx` | Not usable in another repository |
| 5 | No tests, no type check | `tsc` failed on the `@/types` alias and implicit `any` | Nothing proved behavior |
| 6 | A 5,950-line stylesheet | `styles/dashboard.css` | Inconsistent components, no token system |
| 7 | `POST /factory/catalogs/{kind}` was in the contract but not served | `serve.py` dispatch | Contract drift |
| 8 | `.aisdlc/serve.token` was not git-ignored | `git check-ignore` | `git add -A` could commit a live token |
| 9 | The coach audit was a one-off report from an outside tool | `ai_engineering_coach_full_audit.md` | No repeatable signal for the factory |

## Verdict per feature

| SESSION-FEATURES item | Verdict | Now |
|-----------------------|---------|-----|
| 1 CLI benchmark integration | kept, generalized | The experiment takes `--tasks-file` and `--corpus`; provider settings are built as JSON |
| 2 Standalone dashboard server | rebuilt | Fastify server that speaks only the serve contract and RepoLens; many workspaces |
| 2.2 Light/dark theme | rebuilt | One token set, system default, manual toggle, WCAG AA checked by axe in both themes |
| 2.3 Sidebar navigation | kept | Rail on desktop, scrolling strip on phones |
| 3 Production line | rebuilt | Run counts by status from evidence; the running station is the only animated element |
| 3.3 Sequential stage execution | dropped | The kit has no runner; the screen says requested runs stay queued |
| 4 Intent view | deferred | The kit's `intent` data is not in the contract yet (proposal 3) |
| 5 Bidirectional control | dropped | Not in the contract; cancel answers 501 while `runner=false` |
| 6 Configuration store and UI | rebuilt | Catalog views over `/factory/catalogs/*` with versions, `If-Match` and `env:NAME` credentials |
| 6.3 AI providers | rebuilt | Providers are catalog entries with model, endpoint and an `env:` credential |
| 7 Metrics | rebuilt | Gates, stages, autonomy and cost from `/factory/report` |
| 7.3 Foreman monitor | deferred | Needs a contract path for the Foreman queue (proposal 4) |
| 8 Persistent storage layer | dropped | The kit is the store; the dashboard keeps only sessions in memory |
| 9 RepoLens integration | rebuilt | Search with repository labels, orphans and usage through the RepoLens API |
| 10 File viewer | dropped | A dashboard that reads files bypasses the contract; links to the forge are enough |
| 11 Event stream | rebuilt | Run evidence per run, polled every 5 s; no example events presented as live |
| 12 Error boundary | rebuilt | Designed states per resource: loading, empty, error, stale, forbidden, read-only |
| 13 Pages showcase | corrected | The factory-line page replays example events and now says so |
| 14 RepoLens light theme | kept | Unchanged |
| 15 Types and endpoints | rebuilt | `src/ui/model.ts` mirrors the OpenAPI shapes |

## The audit, turned into a living coach

The audit counted anti-patterns once, with an outside extension. `aisdlc coach` measures the same
habits from local session stores whenever it runs, and `GET /factory/coach` serves them to the
owner. It never emits prompt text.

| Audit finding | Coach rule | Threshold |
|---------------|-----------|-----------|
| Repeated prompts / retries | `repeated_prompt` | the same normalized prompt (12+ characters) again in one session |
| Runaway agent loops | `runaway_loop` | 40 tool calls without a human prompt |
| Unsandboxed host execution | `unguarded_autonomy` | Claude Code `bypassPermissions`, Codex `danger-full-access` |
| Mega-sessions | `mega_session` | more than 30 prompts |
| Prompt bloat | `prompt_bloat` | a prompt over 8,000 characters |
| Never uses plan mode | `no_plan` | 10+ prompts or 40+ tool calls with no plan signal |
| Low context provision | not measured | needs a reliable signal; a heuristic would mostly be noise |

On this repository's real stores, the first version counted task notifications and compaction
summaries as prompts, and most "repeated prompts" were those. The coach now skips messages the
harness writes into the user role.

## Proposals, ranked

1. **Push channel on `aisdlc serve`.** An event stream (`GET /factory/events`, server-sent events)
   would replace the dashboard's polling and give every client live runs. Contract change.
2. **One UI.** The kit has three HTML surfaces (`backoffice --serve`, `runtime --serve`, the static
   `/ui/` mount) and now the dashboard. Retire `backoffice` and `runtime` in favor of the dashboard,
   or ship the dashboard's built UI as the static `/ui/` of `aisdlc serve`, after the Owner decides
   the UI host (ADR-006 is Proposed).
3. **Intents in the contract.** `aisdlc intent` already groups usage by intent; `/factory/intents`
   would let the dashboard show a work order with its runs, cost and knowledge use.
4. **Foreman queue in the contract.** A read path for the claim queue and lanes would bring the
   Foreman monitor back without file reads.
5. **Preflight stack identity.** Preflight checks that something answers on port 3000, not that it
   is the product frontend. A Pages dev server on 3000 made it skip the stack start and run product
   e2e against the wrong app. Check `/login` for the product's sign-in field and name the process
   holding the port.
6. **Coach in the improvement loop.** Feed `coach` findings to `aisdlc improve propose`, so a
   recurring anti-pattern becomes a proposal with evidence instead of a chat remark.
7. **Session store for the dashboard.** Sessions live in memory; several dashboard instances behind
   one address would need a shared store. Not needed for a single host.
8. **Coach thresholds in the profile.** Teams differ: a `[coach]` table could tune thresholds; the
   report already prints the ones it used.
