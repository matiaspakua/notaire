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

- `preflight.sh` retries frontend Vitest once (`run_retry`) before failing the gate. Measured flakes that passed on the immediate rerun: `reports-page` and `dashboard-pages-render` (99 tests), `upcoming-expirations` (93 tests), and `budgets opens every dialog` (93 tests in the same file).

## 2026-11-18 locate routing

- Owner-delegated autonomous merge for the backlog loop, 2026-11-18, revocable by the owner. Merge only after `preflight.sh --full` exit 0 on the final head, required GitHub checks green, and a clean merge with main. No ruleset bypass, no force-push.
- Do not file a new GitHub issue unless it is necessary. Work open issues first. Other findings stay in this log.
- RepoLens is not the first call for every lookup. Exact symbol or path: `rg` (0.002s and 0.008s on two questions, versus 1.924s and 1.878s for search, hit rate not better). Unknown symbol: `repolens search`. Callers: `repolens impact`, then `rg` plus `repolens miss` if impact names no production file. Offline bench (`summary.md`): Haiku holdout tool calls 5.89 to 4.56 and input tokens 48,468 to 36,165; Sonnet holdout tool calls 4.00 to 2.00 but input tokens 21,914 to 24,419 and cost 0.03230 to 0.03312. Sonnet scale did not reduce tool calls (2.00 both) and raised input tokens.
- Paired grep-vs-repolens locate is sampled: every third implemented issue, and every miss. KPI file: `factory-kpis.json`.


