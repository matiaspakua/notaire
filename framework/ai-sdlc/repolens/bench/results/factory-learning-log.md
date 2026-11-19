# Factory learning log

Short notes from the cloud factory loop. Product behavior is not documented here. A number is included only when a command in that run produced it.

## 2026-11-18

- Full preflight attempt on the registration-draft branch failed once with `stack not reachable` because an earlier smoke had run `docker compose down`. The next attempt, after `workspace/stack/start.sh`, passed the server-backed suites that had been reached when this note was written.
- The same branch's second full preflight failed only on frontend Vitest: 3 tests in `reports-page.test.tsx` and `dashboard-pages-render.test.tsx` (2 files, 1,580 tests). Re-running those two files alone: 99 passed. The third full preflight passed the Vitest step. The failures are treated as suite flakes, not as a product defect. No flake fix is in this change; it is recorded so a later gate can retry Vitest once before failing the run.
- `pip install pytest` into the RepoLens venv upgraded `mcp` to 2.x. `verify.sh` then failed 4 tests because `mcp.server.fastmcp` is gone. The extra is now pinned to `mcp>=1.2,<2`. After the pin, the local package version was 1.30.0.
- GitHub label writes from this agent still return 403. Issue creation for #2307 succeeded. Status labels were not applied.
- Cursor branch names `cursor/<desc>-47dc` warn in preflight (`does not match a documented pattern`). The warning does not fail the run.
- Release-please opened #2312 (`chore(main): release 0.4.2`) as soon as #2311 merged. That holds one of the three open-PR slots, so this round opens two agent pull requests, not three.
- Procedure-folder list and the users list were challenged and not implemented. The folder endpoint is a management search. The users endpoint also fills the audit filter.
- Repolens 0.4.2 stops linking an ambiguous same-directory call to the first three methods of that name. The fixture test is the before/after: `Reader.show` linked both `getDto` methods, and after the change it links neither, while `Reader.once` still links `Deed.onlyOnce`.

## 2026-11-18 vitest retry

- `preflight.sh` retries frontend Vitest once (`run_retry`) before failing the gate. Measured flakes that passed on the immediate rerun: `reports-page` and `dashboard-pages-render` (99 tests), `upcoming-expirations` (93 tests), and `budgets opens every dialog` (93 tests in the same file). The first full preflight of this branch logged `frontend vitest failed once; retrying` and then `frontend vitest (passed on retry)`, so that run did not restart.

## 2026-11-18 locate routing

- Owner-delegated autonomous merge for the backlog loop, 2026-11-18, revocable by the owner. Merge only after `preflight.sh --full` exit 0 on the final head, required GitHub checks green, and a clean merge with main. No ruleset bypass, no force-push.
- Do not file a new GitHub issue unless it is necessary. Work open issues first. Other findings stay in this log.
- RepoLens is not the first call for every lookup. Exact symbol or path: `rg` (0.002s and 0.008s on two questions, versus 1.924s and 1.878s for search, hit rate not better). Unknown symbol: `repolens search`. Callers: `repolens impact`, then `rg` plus `repolens miss` if impact names no production file. Offline bench (`summary.md`): Haiku holdout tool calls 5.89 to 4.56 and input tokens 48,468 to 36,165; Sonnet holdout tool calls 4.00 to 2.00 but input tokens 21,914 to 24,419 and cost 0.03230 to 0.03312. Sonnet scale did not reduce tool calls (2.00 both) and raised input tokens.
- Paired grep-vs-repolens locate is sampled: every third implemented issue, and every miss. KPI file: `factory-kpis.json`.

## 2026-11-19 exact-symbol search

- Sampled locate for `Testimony.getDto` (third implemented issue since the routing rule): `rg` 0.004s, `repolens search` 0.680s with `no results`, `repolens impact` 0.205s with `node not found`. The live index has no node of that name. `git grep origin/main` (0.015s) was the caller list that was implemented.
- #2325 was opened because Gate 1 requires an open issue and no open RepoLens issue remained (#2307 is closed). It tracks the three measured gaps: exact-symbol embed latency, search-alone caller recall, and `adoption 0%` with zero harness sessions.
- 0.4.4 skips `embed_query` when the query is one identifier and the name index hits. It also adds `calls` predecessors of that hit when full-text search is empty. `report --gate` prints `adoption n/a` when `sessions` is 0. The fixture tests failed first (`embed_query` count was 1; the gate line contained `adoption 0%`).
- Merging #2324 closed #580 (`cursor[bot]`, no commit id on the close event) even though the pull request said to keep it open. `gh issue reopen 580` returned `Could not reopen the issue`. The remainder is #2326. Do not file a pull request body that puts a closing keyword next to an issue that must stay open. #2326 later closed the same way because the body contained `close #2326`. #2329 is the current remainder and its pull request avoided that phrase; it stayed open after #2330 merged.

## 2026-11-19 index freshness

- #2331 was opened because no open issue covered a stale index. #2307 is closed.
- Fixture before: searching the graph that was indexed at the previous commit for `brand_new_symbol` returned 0 hits in 0.0019s.
- Fixture after: the CLI query printed `index is behind HEAD` and `index refreshed in 0.01s`, then listed `src/new.py`. The query took 0.0164s. Embedder was `none`. This does not measure an incremental refresh of the Notaire index.
- The Testimony.getDto miss (0.680s, no results) was this failure mode: the live index was one merge behind HEAD.

## 2026-11-19 Notaire refresh after a merge

- Indexed at `9c267f89` (cold index 1265.78s, embed 1251.85s, 84636 vectors). Fast-forwarded to `b0065a20`, the #2333 merge.
- First `repolens search User.setAttributes` printed `index refreshed in 5.52s`. The query took 7.104s and ranked `User.java:210` first.
- The next search took 1.508s and did not refresh.
- `git grep` for the declaration took 0.0061s and named only that line.
- 5.52s is not a re-embed. It is one incremental pass.
- After #2334 merged, the next search (`Procedure.getDto`, `b0065a20` to `623b2d56`) printed `index refreshed in 5.79s`. The query took 7.718s and ranked `Procedure.getDto`, then `DeedLegacyMapper.toDto` and `TestimonyLegacyMapper.copyDeed`. Same conclusion: about 6s, not the next RepoLens item.

## 2026-11-19 Maven Central 429 on dependency submission

- `submit-maven` on #2337 (run 44496389413) failed while resolving `org.springframework.boot:spring-boot-starter-parent:pom:4.1.2` from Maven Central: HTTP 429 Too Many Requests. The job is GitHub Automatic Dependency Submission. It does not call the Notaire API.
- This agent cannot re-run that workflow (`Resource not accessible by integration`).
- Fallback: the owner re-runs the failed job with an owner token, or a later push retries it. Do not open an issue unless the 429 happens again on a later pull request. Do not push an empty commit just to retry, because that changes the head that `preflight.sh --full` already passed.

## 2026-11-19 Sonnet tokens and the post-merge refresh

- Sonnet input tokens rose with RepoLens (holdout 21914 to 24419, scale 18727 to 22506). The default search was 10 fully detailed hits (signature up to 160 characters, every symbol, the `via` line) and the MCP server added 10 more one-line hits. Snippet defaulted to 80 lines. Impact defaulted to depth 3. Those payloads are what the model reads back as input on the next turn.
- On the session fixture, query `code verification`: 10 detailed hits were 1697 bytes; the new default (4 detailed plus 4 one-line) was 940 bytes, 0.554 of that text. Signatures are now capped at 80 characters and symbol lists at 6. This is not a new Sonnet bench run.
- The 5.52s and 5.79s waits were the query itself running the incremental index (`wait=True`) because the cloud VM had not run the git hook. `post-merge` already starts `refresh --background`. A query that is still stale now starts that same detached index and returns. The fixture test no longer prints `refreshed in` on the first search, and the following search finds `brand_new_symbol` after the background run. The Notaire 5.5s figure is the old blocking path, not a new cold index.


