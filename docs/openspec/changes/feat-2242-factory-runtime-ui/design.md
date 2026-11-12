# Phase D — factory runtime UI design

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

A.4 (#2255/#2274) shipped file-backed Project / Run / RunRequest with
evidence-derived status and `aisdlc serve` paths. B/C shipped backoffice HTML
over catalogs + evidence runs monitor. Operators still lack the **runtime**
surface (FACTORY-UI-SPEC §6 / #2242): Projects, Start run, Monitor.

## Goals / Non-Goals

**Goals:**
- Kit-neutral runtime HTML/serve: create project, bind repos (`dry_run` default)
- Approved-only workflow picker; start RunRequest; refuse non-approved
- Monitor Run status + events from evidence; never paint `running` from click
- Cancel control honestly disabled / unavailable (Runner port not proven)
- Adapter over `projects` / `runs` / `workflows` ports only
- Kit unittests; no product Playwright

**Non-Goals:**
- Phase E Notaire demo (#2243)
- Second OpenAPI tree or parallel project/run store
- Agent commit/approve from UI
- Context Lake / Wilson vanity / fake Isolator / `required=true`
- Contending with MFA docs #2271

## Decisions

1. **New `runtime.py` + `runtime_html.py`** — separate from backoffice MODULES
   (Spec §6). Modules: `projects`, `start`, `monitor`.
2. **Host** — `aisdlc runtime --html PATH` and `--serve` loopback (B/C pattern);
   mutations call `projects.create` / `projects.add_repo` / `runs.start` directly.
   Document mechanical Deviation vs mounting only under `aisdlc serve /ui/`:
   same ports, faster ship; OpenAPI remain SoR.
3. **Approved picker** — `workflows.list_workflows(approved_only=True)` only;
   draft/defined/validated never appear in Start form options.
4. **Honesty** — after start, UI shows derived status (`queued`/`requested`);
   `running` only after harness evidence; empty monitor copy explains no adapter
   reported; cancel `data-supported="false"`.
5. **No commit/approve controls** in runtime HTML.
6. **OpenAPI** — descriptive note only; no path/schema additions.
7. **Tokens** — optional `ui/tokens.css` extract if cheap; otherwise inline CSS
   matching backoffice kit-neutral teal/IBM Plex (no Notaire chrome).

## Riesgos / Trade-offs

- [Runtime serve lacks Bearer/CSRF of `aisdlc serve`] → Accept for B/C parity;
  loopback-only; document; OpenAPI path remains for tokenized clients.
- [Blurring C6 evidence Runs vs D3 Run monitor] → Distinct brand area + copy;
  C6 stays backoffice evidence summary; D3 uses Run entity + `list_events`.
- [PR line budget] → Keep HTML/adapters focused; tests assert data-* contracts.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| runtime shell: Projects / Start / Monitor + kit brand | unit | `tests/test_runtime_html.py` |
| create project + bind repo dry_run default | unit | `tests/test_runtime_html.py` + serve |
| approved-only picker excludes draft | unit | `tests/test_runtime_html.py` |
| start run shows queued not running | unit | `tests/test_runtime_serve.py` |
| unapproved start refused | unit | `tests/test_runtime_serve.py` |
| monitor events / empty no-adapter copy | unit | `tests/test_runtime_html.py` |
| cancel honestly disabled | unit | `tests/test_runtime_html.py` |
| no commit/approve affordances | unit | `tests/test_runtime_html.py` |
| no second OpenAPI tree | unit | existing `test_contract.py` |

## Regression Strategy

- Existing tests affected: none expected for backoffice MODULES
- Full suite: `bash framework/ai-sdlc/verify.sh` then
  `bash workspace/sdlc/preflight.sh --full`
- Bruno / product API: n/a

## Playwright Strategy

- n/a — kit HTML/contract tests only; no Notaire product UI in this tip
- Kit UI E2E (if any later) stays in dedicated kit job (`optional/ui-e2e.txt`),
  never `testing/e2e`

## Deployment Strategy

- Flyway: no
- Kit-only; smoke: `aisdlc runtime --html /tmp/rt.html --module projects`
  and create project / start approved run via serve

## Rollback Strategy

- Revert safe: yes — additive kit modules/CLI; no schema migration
- Database rollback: none needed
- Data written under the new behavior after revert: Project/Run files remain
  valid A.4 artifacts; evidence ledger unchanged by the HTML adapter
- Blast radius if rollback is delayed: local HTML/serve only

## Migration Plan

- None — adopters opt into `aisdlc runtime` when ready; no forced migrate
