# Factory results, 2026-11-23

What the AI SDLC factory delivered from epic #2393 through #2439, measured from the evidence ledger,
the pre-push logs and GitHub. No figure here is estimated. Where a number could not be measured,
the note says so.

## What shipped

| Area | Issues | Result |
|---|---|---|
| Observable factory | #2394–#2399 (epic #2393) | SQLite store and telemetry, one intent entry point (CLI, API, MCP), production-line model, live status and events, configuration as code, catalog-owned AI config |
| Production-line diagram | #2400, #2404, #2407, #2410, #2411 | Stations with kind, gates with evidence state, correction loop, throughput and cost, intent progress; workflow steps carry their line stage |
| Inspection | #2434, #2435 | Collapsible sidebar; each station lists what the run's steps received (request, prompt, files, links) and produced (summary, commits, files) |
| Runs carry intent | #2426 | `run start --intent` attaches the run to its intent |
| Gate speed | #2413, #2414 | Branch-scoped `preflight --full` plus a pass cache per commit; one stack-backed run at a time |
| Fixes found by the factory | #2385, #2389, #2405, #2429 | Unregistered adapters refused; silent 429s in e2e; SSE headers; Pages build no longer dirties the tree |

## Factory runs

Both runs went through `factory-feature`, with Junie doing one step per invocation and the Foreman
(Claude) reviewing each diff. The times are from the ledger's `step.started` and `step.finished`
events.

| Step | #760 (run `issue-760`) | #2429 (run `issue-2429`) |
|---|---|---|
| spec | 157 s | 167 s |
| red | 3,239 s | 66 s |
| green | 342 s | 426 s, **failed**; Foreman correction, 472 s from step start |
| docs | 49 s | 28 s |
| archive | 122 s | 131 s |
| merge | 29 s | Foreman |
| Run wall time | 78.8 min | 22.6 min |
| Outcome | succeeded | succeeded |

Both runs needed one Foreman correction batch, the bounded-correction rule's limit:

- **#760:** the script only computed fresh numbers when two variables were set, and nothing set
  them. The Foreman wired the Pages workflow and added a test.
- **#2429:** the red tests wrote the file themselves, so the changed-data case could never pass,
  and the green made `build()` read the tracked output. The Foreman rewrote the tests to call
  `write()` and put the rule there.

The #2429 run is the first to record its inputs and outputs (#2435). The pre-push station shows
the green step's prompt, the correction summary, both commits and the two changed files.

Across the ledger: 20 runs, of which 16 succeeded, 3 failed and 1 was cancelled.

## Gate times

Before #2413 every push ran every suite: about 35 minutes. Afterwards, each push runs only the
groups its branch touches. Times are from the pre-push logs, creation to last write.

| Push | Groups | Time |
|---|---|---|
| #2409 (`.claude/` only) | none | 53 s |
| #760 | pages | about 3 min |
| #2429 | pages | 182 s, then 193 s |
| #2434 | boundaries, kit, pages | 627 s |
| #2435 | boundaries, kit, pages | 664 s, then 656 s after merging `main` |

## Worker behaviour (Junie)

Junie was the only worker with quota; kiro-cli, agy and Codex were exhausted. It needed
explicit guardrails:

- In earlier issues it ran `git stash`.
- It ran markdownlint over the whole repository twice.
- It committed code during a spec-only step.
- It left the #2429 green uncommitted.
- `timeout` does not stop it, because `junie` is a wrapper script.

The step driver therefore checks for a commit after every step, and the Foreman reviews every
diff.

## RepoLens

See `framework/repolens/bench/results/factory-learning-log.md`, 2026-11-23:

- The usage report shows 79 server calls with p50 at 40 ms.
- The paired locate sample for #2435 found `rg` faster on known strings. RepoLens ranked the exact
  merge function first on the behavioural question and missed the component on the second, and
  that miss is recorded.
