# Factory learning log

Short notes from the cloud factory loop. Product behavior is not documented here. A number is included only when a command in that run produced it.

## 2026-11-18

- Full preflight attempt on the registration-draft branch failed once with `stack not reachable` because an earlier smoke had run `docker compose down`. The next attempt, after `workspace/stack/start.sh`, passed the server-backed suites that had been reached when this note was written.
- The same branch's second full preflight failed only on frontend Vitest: 3 tests in `reports-page.test.tsx` and `dashboard-pages-render.test.tsx` (2 files, 1,580 tests). Re-running those two files alone: 99 passed. The third full preflight passed the Vitest step. The failures are treated as suite flakes, not as a product defect. No flake fix is in this change; it is recorded so a later gate can retry Vitest once before failing the run.
- `pip install pytest` into the RepoLens venv upgraded `mcp` to 2.x. `verify.sh` then failed 4 tests because `mcp.server.fastmcp` is gone. The extra is now pinned to `mcp>=1.2,<2`. After the pin, the local package version was 1.30.0.
- GitHub label writes from this agent still return 403. Issue creation for #2307 succeeded. Status labels were not applied.
- Cursor branch names `cursor/<desc>-47dc` warn in preflight (`does not match a documented pattern`). The warning does not fail the run.
