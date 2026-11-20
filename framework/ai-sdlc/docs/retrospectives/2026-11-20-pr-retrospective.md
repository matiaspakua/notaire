# PR retrospective — 2026-11-20

Sources are `gh pr view` timestamps and review counts, `factory-loop-comparison.md`, `factory-learning-log.md`, `factory-kpis.json`, and this agent's preflight log for #2374. A cell that was not in those sources is **unknown**. Open-to-merge is `createdAt` to `mergedAt` in minutes, rounded. It includes waiting for a merge. It is not agent work time.

The set is every merged pull request from #2292 through #2374. `gh pr view` could not resolve #2297, #2298, #2307, #2315, #2318, #2325, #2326, #2329, #2331, #2336, #2338, and #2348 through #2353. Those numbers are not pull requests. 66 pull requests were resolved.

## Findings

- **66 merged pull requests, 0 GitHub reviews.** Conversation comments: 6. #2301 has the only human one: the owner asked to resolve conflicts, and the branch was already mergeable. The other four are release-please notes on #2312, #2317, #2340 and #2362. Review rework beyond that comment is **unknown** (no review body was submitted).
- **Open to merge.** #2292–#2305 (n = 12, before this agent merged its own pull requests): median 103 minutes. #2301, #2304 and #2305 sat overnight (575, 408 and 339 minutes). #2355–#2374 (n = 20): median 1.4 minutes, and 18 of 20 are under 15 minutes. #2362 is the 0.6.0 release (460 minutes). #2358 is 39 minutes; the cause was not recorded.
- **Preflight restarts that were written down.** #2296: 2. #2299: 4. #2300: 1. #2301: 2 plus one later merge-of-main pass. #2302: 1. #2304: 2. #2305: 3. `factory-kpis.json` also names `registration-draft: 3`, `folio: 3`, `cli-usage: 2` without a pull-request number. #2374: 2 full runs. The first failed only on `server-backed suites: stack not reachable` after the earlier checks had passed. The second, after `workspace/stack/start.sh --no-admin`, exited 0 with 41 passed. The same stack-down failure is the 2026-11-18 registration-draft note. Other pull requests: **unknown**.
- **CI.** #2374 `submit-maven` on `2d56cec5` was SUCCESS, and the pull request merged as `9ff9f289`. The learning log records one GitHub Actions failure: `submit-maven` on #2337 (run 44496389413) got HTTP 429 from Maven Central while resolving the Spring Boot parent. The pull request still merged. Whether a later run of that job passed was not re-checked. No other check conclusion was queried. Playwright and Vitest flakes in the log are local preflight, not GitHub CI: Vitest files that failed once and passed on the immediate rerun (`reports-page`, `dashboard-pages-render`, `upcoming-expirations`, budgets dialogs), and one Playwright drawer timeout on the native-helper preflight that passed when that spec was run again. Those are in the learning log, not re-counted here.
- **RepoLens, only where a command was written down.** Before the index, search exited 1 with `no results` (the comparison doc). After the index: #2306 search named the entity and the adapter; `impact Property.getDto` returned 0 files. #2308 search ranked the controller first. Deed `getDto` (#2310): grep 3/4 diff files in 0.186s, search plus impact 3/4 in 4.552s, and impact also named `History.getDto`, which does not call `Deed.getDto`. Folio `getDto`: grep 5/6, search alone 1/6, impact 5/6 plus that false caller. `Testimony.getDto`: `rg` 0.004s, search 0.680s with `no results`, impact 0.205s with `node not found`, because the index was one merge behind. After #2333, search of `User.setAttributes` refreshed in 5.52s and ranked `User.java:210` first; `git grep` of the declaration was 0.0061s. The blocking refresh on the next range was 5.79s / 7.718s. With background refresh, the same range returned in 1.31s and the index matched HEAD 2.112s later. For every other pull request, whether RepoLens was used, and whether it hit, is **unknown**.
- **Tokens.** Haiku holdout (36 runs each arm, `summary.md` via `factory-kpis.json`): tool calls 5.89 to 4.56, input tokens 48,468 to 36,165, cost 0.00334 to 0.00228 USD. Sonnet holdout (6 runs): tool calls 4 to 2, input tokens 21,914 to 24,419, cost 0.03230 to 0.03312 USD. Sonnet scale (8 runs): tool calls 2 and 2, input tokens 18,727 to 22,506. The 0.6.1 answer-text stand-in on 12 holdout prompts, not a model session: 35,053 bytes down to 13,682 (ratio 0.390). Agent benches were not re-run on 2026-11-19: no provider credentials (`provider-probe-2026-11-19.json`). They were not re-run for this change either.
- **Process gaps that already had a rule, and still cost a stop or a wasted run.** GitHub issue comment and close return `Resource not accessible by integration`. The lessons page already says to leave a paste-ready note for the owner. Agents still treated the 403 as a stop until the instruction was repeated. A closing keyword in a pull-request body closed #580 and #2326 while the work was unfinished. `cursor/<desc>-47dc` warned on every full preflight of this agent's branches. The warning does not fail the run.

## What changed in this pull request

| Change | Why it is first |
|---|---|
| `preflight.sh --full` runs `workspace/stack/start.sh --no-admin` once when :8080 or :3000 is down, then checks again | Two recorded full runs died at the end for this reason |
| `cursor/` is an accepted branch prefix in preflight and in the warn-only naming job | The warning fired on every cloud branch and never failed CI |
| RepoLens 0.6.2 records `REPOLENS_SESSION` or `CURSOR_CONVERSATION_ID` on CLI search and miss, and `report` counts that session | CLI usage was stuck at 0 sessions, so adoption stayed `n/a` even when search ran |
| Foreman template: a 403 forge write is an Orchestrator action, and the loop continues | The 403 path was documented as a lesson and was still a stop |

Kit version stays 1.0.0. The kit changelog notes the template sentence. RepoLens moves from 0.6.1 to 0.6.2.

## Measured for 0.6.2

Retrieval, caller, orphan, and agent benches were **not** re-run. The patch does not change the index or the answer text, so those published numbers are unchanged and are not a before/after for this release.

The usage report was measured by `tests/test_factory.py`:

| Log line | Report |
|---|---|
| CLI search, no `session` | `1 calls`, `0 sessions`, `adoption n/a` |
| CLI search, `session` `conv-20` | `1 calls`, `1 sessions`, `adoption 100%` |

`cli_session()` returns an empty string when both environment variables are unset, `CURSOR_CONVERSATION_ID` when only that is set, and `REPOLENS_SESSION` when both are set.

## Backlog

- Per-intent metrics (`aisdlc intent` / `aisdlc metrics`) are another agent's work. This change does not add them.
- Sonnet and Haiku benches need a provider login. They stay at the numbers above.
- Whether RepoLens was opened on each product pull request after #2308 is unknown. The usage log on the cloud VM was not exported with the pull requests.
- CI conclusions other than #2374 and the #2337 429 were not queried.
- Preflight restart counts exist only for the runs named above.
- Report builders and the admin seed still import `business.*` (#2351). That product work was stopped on the owner's order and is not in this pull request.
- `submit-maven` 429 has no local retry. An empty commit to retry it would change a head that preflight already passed.

## Pull requests

Minutes are rounded. #2292–#2296 open and merge times are the ones already printed in `factory-loop-comparison.md` from `gh pr view`. Review and comment counts for all 66, including those five, were read again with `gh pr view --json reviews,comments`.

| PR | Open to merge (min) | GitHub reviews | Conversation comments |
|----|---------------------|----------------|------------------------|
| #2292 | 174 | 0 | 0 |
| #2293 | 103 | 0 | 0 |
| #2294 | 27 | 0 | 0 |
| #2295 | 22 | 0 | 0 |
| #2296 | 23 | 0 | 0 |
| #2299 | 31 | 0 | 0 |
| #2300 | 129 | 0 | 0 |
| #2301 | 575 | 0 | 2 |
| #2302 | 8 | 0 | 0 |
| #2303 | 19 | 0 | 0 |
| #2304 | 408 | 0 | 0 |
| #2305 | 339 | 0 | 0 |
| #2306 | 51 | 0 | 0 |
| #2308 | 7 | 0 | 0 |
| #2309 | 150 | 0 | 0 |
| #2310 | 101 | 0 | 0 |
| #2311 | 12 | 0 | 0 |
| #2312 | 293 | 0 | 1 |
| #2313 | 8 | 0 | 0 |
| #2314 | 60 | 0 | 0 |
| #2316 | 0 | 0 | 0 |
| #2317 | 29 | 0 | 1 |
| #2319 | 15 | 0 | 0 |
| #2320 | 69 | 0 | 0 |
| #2321 | 196 | 0 | 0 |
| #2322 | 1 | 0 | 0 |
| #2323 | 7 | 0 | 0 |
| #2324 | 1 | 0 | 0 |
| #2327 | 12 | 0 | 0 |
| #2328 | 3 | 0 | 0 |
| #2330 | 1 | 0 | 0 |
| #2332 | 3 | 0 | 0 |
| #2333 | 1 | 0 | 0 |
| #2334 | 4 | 0 | 0 |
| #2335 | 2 | 0 | 0 |
| #2337 | 3 | 0 | 0 |
| #2339 | 3 | 0 | 0 |
| #2340 | 35 | 0 | 1 |
| #2341 | 3 | 0 | 0 |
| #2342 | 1 | 0 | 0 |
| #2343 | 1 | 0 | 0 |
| #2344 | 1 | 0 | 0 |
| #2345 | 1 | 0 | 0 |
| #2346 | 4 | 0 | 0 |
| #2347 | 71 | 0 | 0 |
| #2354 | 3 | 0 | 0 |
| #2355 | 4 | 0 | 0 |
| #2356 | 1 | 0 | 0 |
| #2357 | 2 | 0 | 0 |
| #2358 | 39 | 0 | 0 |
| #2359 | 2 | 0 | 0 |
| #2360 | 1 | 0 | 0 |
| #2361 | 4 | 0 | 0 |
| #2362 | 460 | 0 | 1 |
| #2363 | 1 | 0 | 0 |
| #2364 | 1 | 0 | 0 |
| #2365 | 2 | 0 | 0 |
| #2366 | 1 | 0 | 0 |
| #2367 | 1 | 0 | 0 |
| #2368 | 1 | 0 | 0 |
| #2369 | 1 | 0 | 0 |
| #2370 | 2 | 0 | 0 |
| #2371 | 1 | 0 | 0 |
| #2372 | 1 | 0 | 0 |
| #2373 | 1 | 0 | 0 |
| #2374 | 1 | 0 | 0 |
