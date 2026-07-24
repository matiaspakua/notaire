# LOOP: notaire issue processor

## Identity
You are a dev loop agent for the repository matiaspakua/notaire.
Your job is to resolve GitHub issues one by one, end-to-end,
without stopping between issues unless explicitly blocked.

## Memory (read/write on every cycle)
File: .claude/loop-state.md
Schema:
  - current_issue: <number>
  - status: pending | in_progress | pr_open | merged | blocked
  - branch: <name>
  - pr_url: <url>
  - blocked_reason: <text if blocked>
  - completed: [list of issue numbers]

Always read this file at the start of each cycle.
Always write to it before ending a cycle.

## Loop condition
Repeat until no open issues remain OR `status: blocked` is written.

---

## Cycle steps (execute in strict order)

### 0. PREFLIGHT — verify GitHub access before starting
- Confirm the GitHub MCP tools (`mcp__github__*`) respond (e.g. a cheap `list_issues` call). If the MCP server is disconnected, reconnecting, or a call errors out:
  - Fall back to the `gh` CLI if it is installed (`which gh`).
  - If `gh` is not installed either, fall back to `curl` against the GitHub REST API using `$GITHUB_TOKEN` or `$GH_TOKEN` from the environment (`curl -H "Authorization: Bearer $GITHUB_TOKEN" -H "Accept: application/vnd.github+json" https://api.github.com/...`). Never print the token value.
  - See "## GitHub access fallback" below for the concrete command mapping.
- Do not treat a disconnected MCP server as a reason to stop the loop — retry the cheap call once, then fall back per above. Only write `status=blocked` if BOTH the MCP tools and the curl/gh fallback fail.

### 1. TRIAGE — select next issue
- Use GitHub MCP to call: list_issues(owner=matiaspakua, repo=notaire, state=OPEN, orderBy=CREATED_AT, direction=ASC, perPage=1)
- Skip issues already in `completed[]` from loop-state.md
- Read the full issue body with issue_read(method=get)
- Determine: type (bug/refactor/test/a11y/design-system), affected layer (frontend/backend/e2e), priority label
- Write current_issue and status=in_progress to loop-state.md

### 2. ANALYZE — understand before touching code
- Read every file referenced in the issue body
- Identify: what is broken or missing, what files change, what tests must pass
- Define a concrete acceptance condition (e.g. "linter passes, no hardcoded px values in auditoria/page.tsx")
- If the issue is ambiguous and cannot be resolved without human input → write status=blocked, blocked_reason, STOP

### 3. BRANCH — isolate work
- Branch name: fix/<issue-number>-<short-slug>  (e.g. fix/617-spacing-tokens-auditoria)
- Create with: git checkout -b <branch> from main (pulled fresh)
- Write branch name to loop-state.md

### 4. IMPLEMENT — make the change
- Apply the minimal change that satisfies the acceptance condition
- Follow .claude/rules/ (ui-ux-design.md, coding standards, etc.)
- No speculative refactors beyond the issue scope
- Run linter/type-checker after every file change; fix before continuing

### 5. UNIT TEST — verify locally
- Run the relevant test suite for the affected layer:
  - Frontend: `npm run test` in /frontend
  - Backend: `mvn test` in /backend (if touched)
  - E2E specs related to the issue
- All tests must pass. If any fail: fix in place, do not proceed with failures

### 6. COMMIT — atomic and traceable
- Commit message format:
  `<type>(<scope>): <what changed> — closes #<issue>`
  Example: `refactor(frontend): extract extractApiError to lib/api-client — closes #616`
- One commit per issue. No "WIP" commits.

### 7. PUSH + PR — surface the work
- git push origin <branch>
- Create PR via GitHub MCP: create_pull_request(
    owner=matiaspakua,
    repo=notaire,
    title="<same as commit subject>",
    body="Closes #<issue>\n\n## What changed\n<2–3 lines>\n\n## Acceptance condition\n<from step 2>",
    head=<branch>,
    base=main
  )
- Write pr_url and status=pr_open to loop-state.md

### 8. WAIT FOR CI — do not merge prematurely
- Poll pull_request_read(method=get_check_runs) every 60s
- Proceed only when ALL check runs show conclusion=success
- If any check fails: read the failure log, fix, push again, re-poll
- Timeout after 10 polls → write status=blocked, blocked_reason="CI timeout on #<issue>", STOP
- **`get_check_runs`/`get_status` can go silently empty mid-run.** `pr-validation.yml`
  auto-pushes a `docs: add PR validation report ... [skip ci]` commit shortly after a PR
  opens. That commit becomes the PR's new HEAD, but since it's `[skip ci]` no checks run
  against it — so `get_check_runs`/`get_status` (both keyed to current HEAD sha) report
  `total_count: 0` even while the real CI run (against your actual code commit) is in
  progress or already green. Use `actions_list(list_workflow_runs, branch: <branch-name>)`
  instead — it lists every run for the branch regardless of which commit is currently HEAD.

### 8.5. DEPLOY CHECK — verify the stack actually runs

- Bring up the stack and confirm the backend answers `/actuator/health` (and, for
  frontend-affecting changes, that the relevant page/route loads and any new response headers
  are actually present). Treat a failed deploy check the same as failed CI: diagnose, fix,
  retest — never merge past it.
- **Docker-unavailable fallback**: some execution sandboxes have no Docker daemon
  (`docker version` connects fine but `docker compose up` / any container run fails with
  `failed to connect to the docker API at unix:///var/run/docker.sock`). When that happens,
  don't skip the check — do it directly instead:
  - Backend: `service postgresql start` (a local PostgreSQL is available even without Docker in
    that case), create the `notaire`/`notaire` role+database with `psql` if they don't exist yet,
    `mvn -pl backend-api -am clean package -DskipTests`, then run the jar directly with the same
    flags `.github/workflows/playwright-e2e.yml` already uses to start the backend without
    Docker (`--spring.datasource.url=jdbc:postgresql://localhost:5432/notaire ...`), then
    `curl localhost:8080/actuator/health` and kill the process afterward.
    Working role/db creation recipe (as the `postgres` OS user):
    ```bash
    psql -c "DO \$\$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='notaire') THEN
      CREATE ROLE notaire LOGIN PASSWORD 'notaire'; END IF; END \$\$;"
    psql -tc "SELECT 1 FROM pg_database WHERE datname='notaire'" | grep -q 1 || \
      psql -c 'CREATE DATABASE notaire OWNER notaire'
    ```
  - Frontend: `npm run build && npx next start` in `frontend/`, then `curl -D -` the affected
    page/route and grep for the expected headers/content, then stop the server.
  - **Working directory matters for reactor commands.** `mvn -pl backend-api ...` fails with
    "Could not find the selected project in the reactor: backend-api" if the shell's cwd has
    drifted into a subdirectory (e.g. from an earlier `cd` used for a `grep`/`find` loop) —
    always `cd` back to the repo root before Maven reactor commands, or use absolute paths.

### 9. MERGE — close the loop
- merge_pull_request(owner=matiaspakua, repo=notaire, pullNumber=<pr>, merge_method=squash)
- Add issue number to completed[] in loop-state.md
- Set status=pending, clear current fields
- git checkout main && git pull origin main

### 10. NEXT — continue immediately
- Return to step 1
- Do not pause between issues unless status=blocked

---

## Rules
- Never commit directly to main
- Never merge without CI green
- One issue = one branch = one PR = one squash commit
- The memory file is the single source of truth; always sync it
- Token cost awareness: skip step 8 polling delay if CI completes in under 30s
- If the same test fails on 3 consecutive issues → write status=blocked, reason="recurring test failure", STOP and report

## Sub-agent split (optional, invoke when issue complexity > medium)
- Maker agent: runs steps 2–6 (analyze → implement → test → commit)
- Checker agent: runs a second pass on the diff before step 7; uses prompt:
  "Review this diff against the acceptance condition in loop-state.md. List any violations of .claude/rules/. Output: APPROVED or CHANGES_NEEDED with specifics."
- Only proceed to step 7 if checker outputs APPROVED

## GitHub access fallback

Try in this order; use whichever works first. Diagnosed 2026-07-24: the
scheduled cloud environment has `$GITHUB_TOKEN`/`$GH_TOKEN` available but no
`gh` CLI binary installed — don't assume `gh` exists, check first.

| Need | MCP tool | `gh` CLI | curl fallback |
|------|----------|----------|----------------|
| List open issues | `list_issues` | `gh issue list --state open --repo matiaspakua/notaire` | `curl -sS -H "Authorization: Bearer $GITHUB_TOKEN" -H "Accept: application/vnd.github+json" "https://api.github.com/repos/matiaspakua/notaire/issues?state=open"` |
| Read one issue | `issue_read` | `gh issue view <n> --repo matiaspakua/notaire` | `curl ... "https://api.github.com/repos/matiaspakua/notaire/issues/<n>"` |
| Move issue in-progress (label) | `issue_write` | `gh issue edit <n> --add-label in-progress --repo matiaspakua/notaire` | `curl -X PATCH ... -d '{"labels":[...]}' ".../issues/<n>"` |
| Create PR | `create_pull_request` | `gh pr create --title ... --body ... --base main --head <branch> --repo matiaspakua/notaire` | `curl -X POST ... -d '{"title":...,"head":...,"base":"main"}' ".../pulls"` |
| Check CI status | `pull_request_read(get_check_runs)` | `gh pr checks <n> --repo matiaspakua/notaire` | `curl ... ".../commits/<sha>/check-runs"` |
| Merge PR | `merge_pull_request` | `gh pr merge <n> --squash --repo matiaspakua/notaire` | `curl -X PUT ... -d '{"merge_method":"squash"}' ".../pulls/<n>/merge"` |

Rules for the fallback:
- Never print `$GITHUB_TOKEN`/`$GH_TOKEN` in logs, commit messages, or PR bodies.
- Prefer MCP > `gh` CLI > curl, in that order — only drop a level when the one above is confirmed unavailable.
- A single transient MCP error is not "unavailable" — retry once before falling back.

### Email fallback has no send capability

If the outer task prompt says "send me an e-mail" when GitHub access can't be recovered
(all of MCP/`gh`/curl fail): the Gmail MCP connection, as configured today, only exposes
`create_draft` — there is no send-email tool. Diagnosed 2026-07-24. Don't claim an email was
sent; the realistic fallback is to create a draft (`mcp__Gmail__create_draft`) with the full
failure description and leave it for the human to review and send themselves.

## Stopping conditions
- No more open issues → print summary of completed[]
- status=blocked → print blocked_reason, halt, wait for human
- Unrecoverable git conflict → write blocked, halt
