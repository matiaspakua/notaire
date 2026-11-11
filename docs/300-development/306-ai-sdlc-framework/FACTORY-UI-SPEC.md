# Factory UI and host — architecture spec

Design record for [#2251](https://github.com/matiaspakua/notaire/issues/2251) ·
Use Case **CU76** · epic [#2238](https://github.com/matiaspakua/notaire/issues/2238) (phases
[#2239](https://github.com/matiaspakua/notaire/issues/2239) done,
[#2240](https://github.com/matiaspakua/notaire/issues/2240),
[#2241](https://github.com/matiaspakua/notaire/issues/2241),
[#2242](https://github.com/matiaspakua/notaire/issues/2242),
[#2243](https://github.com/matiaspakua/notaire/issues/2243)).

| Field | Value |
|-------|-------|
| Status | Shared contract for agents working the phases; Owner decisions open (end of this file) |
| Companion | [`FACTORY-PRODUCT-ANALYSIS.md`](FACTORY-PRODUCT-ANALYSIS.md) — verdict, findings F1-F12, reuse plan, SDLC improvements |
| Parent plan | [`FACTORY-ROADMAP.md`](FACTORY-ROADMAP.md) |

Treat this as the contract: deviate only by commenting on #2251 (section 11). Findings
(`F1`-`F12`) are defined in the companion document. Section numbers follow the issue; sections
1, 2, 9 and 10 live in the companion.

## 3. Target architecture

```text
adopter profile (.aisdlc/)  <-- single config truth (files)
        |
   kit core: entities, governance FSM, runs, evidence         (pure Python, stdlib only)
        |
   service layer: functions return data/dataclasses, raise KitError(code, detail)
        |                       |
   CLI adapter              HTTP adapter  `aisdlc serve`  --> static UI assets
   (argparse, text/JSON,    (stdlib http.server/wsgiref, implements the OpenAPI,
    exit codes)              loopback, token, problem+json)
```

Rules:

- The CLI stays the system of record; its JSON output is byte-identical after the refactor
  (golden tests are the PRESERVE tests).
- **Serve host rules.** `aisdlc serve` binds `127.0.0.1` by default and refuses a non-loopback
  bind unless `--allow-remote` is given and a token is configured. The token lives in
  `.aisdlc/serve.token` (mode 0600) with scopes `read` < `write` < `owner`. Mutating requests
  need a custom header (CSRF) and `--write`. No CORS. Errors are RFC 9457
  `application/problem+json` with a stable `code`.
- **Actor.** Token identity plus channel (`ui`, `cli`, `agent:<id>`), recorded server-side on
  every transition and run event. No request body field can set it (F6, axiom 4).
- **UI.** Static, no build step: ES modules and CSS custom properties served by the HTTP
  adapter. Reason: the kit invariant is stdlib-only with no Node, so adopters in any stack can
  run it; swapping the front end later does not change the contract. Strings come from one JSON
  catalog (`es`, `en`). Escape with `textContent` only; no `innerHTML` on entity fields.
- **Tokens.** Kit UI tokens live in `framework/ai-sdlc/ui/tokens.css`; do not import
  `frontend/src/theme` (F10).
- **Capabilities.** `GET /factory/capabilities` reports which ports are live (`approvals_live`,
  runner, forge write, isolator `sandbox`, `enforces_committer`) so the UI renders honest
  affordances: a disabled button with the reason, never a fake "secured" or "running" badge.

## 4. Contract additions (OpenAPI 3.1, one tree, `contract check` + oasdiff)

| Path | Purpose | Slice |
|------|---------|-------|
| `GET /factory/capabilities` | live ports and honest limits | A.2 |
| `PUT /factory/catalogs/{kind}/{id}` | new version; requires `If-Match: <version>`; 409 problem on mismatch | A.3 |
| `GET /factory/workflows/{id}/versions/{n}` and diff | version history and compare | B |
| `GET/POST /factory/projects`, `POST /factory/projects/{id}/repos` | file-backed project and repo binding (dry-run default) | D |
| `GET/POST /factory/runs`, `GET /factory/runs/{id}`, `GET /factory/runs/{id}/events` | run requests and evidence-derived progress | D |
| `POST /factory/runs/{id}/cancel` | only when the Runner port supports it; else `501` with the capability reason | D |

List endpoints accept `limit`, `cursor`, `q` and `governance` filters. Secrets never appear in
any response (redaction golden tests). Declared today and unchanged:
`/factory/config|plugins|harness|report`, `/factory/catalogs/{kind}`,
`/factory/catalogs/{kind}/{id}`, `/factory/catalogs/{kind}/{id}/versions`,
`/factory/workflows`, `/factory/workflows/{id}`, `/factory/workflows/{id}/transitions`.

## 5. Entity model v2

Common envelope: `id, kind, version, schema_version, digest, created_at, created_by`
(`created_by` = channel + identity).

**Workflow body v2** (migrate v1 with `aisdlc migrate`; v1 reads stay valid until migrated):

```text
steps[]: { id, stage, agent, skills[], mcp[], provider, gates[], budget{tokens,time_seconds,usd}, on_fail }
refs: "<kind>:<id>@<version>"  (pinned; digest recorded at validated and approved)
inputs[], outputs[]
```

Governance: `request_validation` resolves and pins every ref; `approve` is refused if any pinned
ref is no longer resolvable at its pinned digest. A new version of a workflow returns to `draft`.

**Project**: `{ id, profile_ref, repos[{ id, forge, url, default_branch, mode: dry_run|live }] }`
(file or profile based, not SaaS tenancy).

**Run**: `{ id, project, workflow{id, version, digest}, status, requested_by, requested_at,
evidence_head, steps[{id, status}] }`, with `status` in
`requested | queued | running | succeeded | failed | cancelled | blocked`.

**Run honesty rule.** The kit records a `RunRequest` and the evidence events. Only a harness
adapter (Cursor Cloud fleet, Claude Code, local worker) appending `run.started`, `step.*` and
`run.finished` events can move a run past `queued`. The UI never shows `running` from a click;
it shows `requested` until an adapter reports. Skip is never pass (axiom 3). The agent never
commits or approves from this surface.

## 6. UI specification (kit-neutral)

Information architecture: right-hand module navigation, one job per module, in this order:
Workflows, Agents, Skills, Tools/MCP, Providers, Knowledge, Rules/Gates, Integrations, Runs.
The runtime area is separate: Projects, Start run, Monitor.

Per-module screens: list (filter, sort, governance chip), detail (current version, metadata,
referenced entities with their pinned versions), version history with diff, edit form,
transition dialog.

States every screen must implement:

| State | Behaviour |
|-------|-----------|
| loading | visible, non-blocking indicator |
| empty | explains why and offers the next action |
| error | shows the problem `code` and `detail` |
| forbidden | token scope insufficient; names the required scope |
| stale | HTTP 409: "changed since you opened it"; offer reload and re-apply |
| read-only | capability missing; disabled control with the reason |

Specific rules:

- The transition dialog requires a reason string, shows the required capability
  (`approvals_live` or Owner override) and the CLI equivalent. A workflow cannot reach
  `approved` from the UI unless the same guard passes as in the CLI.
- Credentials use the reference form only (`env:NAME`); the UI shows redacted values and never
  fetches plaintext.
- The runs monitor has an `aria-live="polite"` region for status changes and an evidence event
  list with hash-chain verify status; the empty state explains that no adapter has reported.
- Accessibility: WCAG 2.2 AA, keyboard operable, visible focus, text contrast 4.5:1,
  `prefers-reduced-motion`, no colour-only state (governance chips carry text).
- Responsive down to 360 px; no horizontal page scroll.

## 7. Test strategy (kit-owned)

- **Service layer.** Unit tests in the existing style (temp git repos): one failing test per
  acceptance scenario and one PRESERVE test per behaviour a refactor touches (CLI JSON golden
  files).
- **HTTP.** Contract tests start `aisdlc serve` on an ephemeral port and check every response
  against the OpenAPI schemas; an `oasdiff` breaking-change gate with accept files.
- **Security.** Token required; loopback-only default; CSRF header; id regex blocks path
  traversal; XSS payloads in summaries render inert; secret redaction golden; 409 on stale
  write; no plaintext credential in any response or log.
- **UI.** Playwright plus axe in a dedicated kit CI job (`optional/ui-e2e.txt`, outside the
  stdlib-only core). Kept out of `testing/e2e` so kit UI tests never depend on, or contend
  with, a Notaire product suite (F11).
- The kit independence test is extended to the UI assets; the kit stays stdlib-only at runtime.

## 8. Re-sliced phases

Each PR ≤ 400 counted lines and one concern.

| Slice | Content | Depends on | Issue |
|-------|---------|------------|-------|
| A.1 | Service-layer extraction for workflows and catalogs; typed `KitError`; CLI byte-identical (goldens) | A (done) | [#2252](https://github.com/matiaspakua/notaire/issues/2252) |
| A.2 | `aisdlc serve` read-only: loopback, token, `GET` endpoints, `/factory/capabilities`, OpenAPI conformance test | A.1 | [#2253](https://github.com/matiaspakua/notaire/issues/2253) |
| A.3 | `If-Match` writes, `schema_version`, workflow body v2 with pinned refs, `aisdlc migrate`, actor audit trail | A.2 | [#2254](https://github.com/matiaspakua/notaire/issues/2254) |
| A.4 | Project, Run, RunRequest schemas; evidence event types; harness reporting contract; `GET/POST` runs and projects | A.3 | [#2255](https://github.com/matiaspakua/notaire/issues/2255) |
| B1 | Shell, nav, tokens, i18n, workflows list and detail (read-only) | A.2 | #2240 |
| B2 | Workflow edit, version diff, governance transitions | A.3, B1 | #2240 |
| B3, B4 | Agents; Skills | B1 | #2240 |
| C1..C5 | tools/MCP, providers, knowledge, rules/gates, integrations (shipped together via `aisdlc backoffice` HTML adapters over Phase A catalogs when A.2/A.3 tips are contested) | B1 | #2241 |
| C6 | Runs monitor (read-only; evidence/`aisdlc report` until A.4 Run entities; cancel/retry honest-empty) | A.4 (entities) / evidence interim | #2241 |
| D1 | Projects and repo binding | A.4, B1 | #2242 |
| D2 | Approved-workflow picker and start run (`RunRequest`) | D1, B2 | #2242 |
| D3 | Progress monitor from evidence | D2, C6 | #2242 |
| E | Notaire adopter demo path plus second adopter fixture | D3 | #2243 |

Packaging, conformance matrix and second adopter are independent of the A.x chain
([#2256](https://github.com/matiaspakua/notaire/issues/2256)); the intake lint is
[#2257](https://github.com/matiaspakua/notaire/issues/2257).

Claim protocol: one open PR per slice, `framework/ai-sdlc/**` only (plus docs and OpenSpec); a
Notaire product PR needs the coordinator.

## 11. Coordination between agents

- The design record is the contract. Phase issues link to it and add only slice-specific
  acceptance criteria.
- **Deviation:** comment on #2251 with `Deviation: <what>, <why>, <mechanical|decision>`. A
  decision deviation stops for the Owner (decision packet format in
  `.claude/rules/ai-agent-workflow.md`).
- **Slice claim:** comment `Claim: <slice> by <agent>` before branching; one open PR per slice;
  remove the claim on merge or abandon.
- **Docs:** permanent docs land via the PR that implements the issue; engineers update the
  sections they change.

## Owner decisions (open)

Decision needed: UI host for the factory control surface
Context: the kit hosts no server (`framework/ai-sdlc/aisdlc/contract.py`; no `serve` in
`cli.py`), and the kit invariant is stdlib-only with no Node (F1).
Options:
  A. `aisdlc serve` plus a static no-build UI — consequence: runs in any adopter stack, no new
     toolchain; the UI is hand-rolled (no React ecosystem) and slices A.2 and B1 own the host.
  B. A Next.js app in the repository — consequence: reuses Notaire front-end skills; couples the
     kit to Node and to this stack, breaking the stdlib-only invariant.
Recommendation (proposal): A, because it keeps the kit adoptable by any stack and the contract
unchanged if the front end is swapped later.

Decision needed: distribution channel for the kit
Context: no `pyproject.toml`, wheel or release asset exists (F7); P1 in the companion document
needs a channel before [#2256](https://github.com/matiaspakua/notaire/issues/2256) can publish.
Options:
  A. PyPI plus GitHub Release assets — consequence: `pipx install aisdlc==X` works; claims a
     public namespace and widens the supply-chain surface (token, trusted publishing).
  B. GitHub Release assets only — consequence: no public namespace and a smaller supply-chain
     surface; installs go through a release URL, less discoverable.
Recommendation (proposal): A, with trusted publishing and provenance attestation.

Decision needed: Owner approval of workflow transitions from the UI
Context: the CLI guards `approved` with the ApprovalPort or an Owner override
(`aisdlc/workflows.py` `_guard_approve`); the UI would expose the same transition (F6).
Options:
  A. Allowed with `owner` token scope, a reason, and the same ApprovalPort/override guard as the
     CLI — consequence: one governed path for both channels; the UI becomes an approval
     surface that must be hardened (token custody, CSRF).
  B. CLI only; the UI shows the command — consequence: smaller attack surface; operators leave
     the UI to approve.
Recommendation (proposal): A, because the guard is identical to the CLI and every transition is
recorded with token identity and channel.

Unanswered. When answered, record the outcome in an ADR under `framework/ai-sdlc/docs/adr/`
and link it from here (acceptance criterion 4 of #2251).
