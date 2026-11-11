> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Phase A (#2239/#2244) shipped immutable catalog revisions, workflow governance
FSM, OpenAPI 3.1, and CLI JSON. `#2142 report --html` already proves a kit-local
static HTML adapter pattern. Phase B must add operator chrome for workflows,
agents, and skills without inventing a Notaire SPA or a second config store.
Product Playwright (#2088) is clear on main; this tip still stays kit-only to
avoid product suite contention and kit path pollution.

## Goals / Non-Goals

**Goals:**
- Right-nav backoffice shell branded **AI SDLC factory** (kit-neutral)
- List / detail / create / edit for workflows, agents, skills over Phase A ports
- Governance UI: current state + allowed transitions only; approve fails closed
- Version history visible; edits → `write_new_revision` / catalog update
- Redacted credentials only
- Kit HTML + contract tests; `aisdlc contract check` remains green

**Non-Goals:**
- Phase C/D modules and runtime project UI
- Context Lake / Port.io / Wilson vanity / fake Isolator
- Notaire product frontend routes or Playwright specs
- Mandatory always-on HTTP server (serve is optional loopback adapter)
- Changing Phase A FSM semantics

## Decisions

1. **Host** — sibling of `report_html.py`: `backoffice_html.py` renders self-contained
   HTML; `backoffice.py` assembles redacted snapshots and mutation adapters that
   call `catalogs` / `workflows` only.
2. **CLI** — `aisdlc backoffice --html PATH` writes a static snapshot; `aisdlc
   backoffice serve [--host 127.0.0.1] [--port N]` starts a thin stdlib HTTP
   adapter for interactive create/edit/transition (POST → Phase A → redirect).
3. **Nav** — CSS right-side module nav (`Workflows`, `Agents`, `Skills`); one
   primary job per module page.
4. **Governance** — HTML offers only actions legal for current state (from
   `workflows.TRANSITIONS`); server-side still enforces Phase A guards (never
   trust the form alone).
5. **Secrets** — always `catalogs.show(..., redacted=True)` / `factory.redact`
   before render; banned plaintext credential fields.
6. **OpenAPI** — no new SoR schemas; info/description notes that the backoffice
   is an HTML adapter over existing catalog/workflow paths.
7. **Tests** — unittest HTML/contract + short-lived serve round-trip; no product
   Playwright.

## Riesgos / Trade-offs

- [Static HTML alone cannot POST] → Optional `serve` for mutations; `--html`
  remains read-only snapshot for offline/evidence.
- [UI could soft-offer illegal approve] → Client lists allowed actions only;
  Phase A still fail-closed if bypassed.
- [Loopback server abuse] → Bind default `127.0.0.1`; no auth beyond local
  machine trust (documented); never expose secrets.
- [Scope creep into Phase C] → Only three modules in nav; others omitted or
  marked disabled/future.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| right-nav shell + kit brand | unit | `tests/test_backoffice_html.py` |
| list/detail agents and skills | unit | `tests/test_backoffice_html.py` |
| workflow governance + allowed transitions | unit | `tests/test_backoffice_html.py` |
| illegal approve not offered / server rejects | unit | `tests/test_backoffice_html.py` |
| version history visible | unit | `tests/test_backoffice_html.py` |
| credentials redacted | unit | `tests/test_backoffice_html.py` |
| create/edit via serve uses Phase A versions | unit | `tests/test_backoffice_serve.py` |
| contract still green / no Notaire brand in kit HTML | unit | `tests/test_contract.py`, `tests/test_backoffice_html.py` |

- New unit tests: `framework/ai-sdlc/tests/test_backoffice_html.py`,
  `framework/ai-sdlc/tests/test_backoffice_serve.py`
- New integration tests: n/a (kit CLI via Repo helper)
- Coverage impact: kit unittest suite; JaCoCo n/a (no backend-api)

## Regression Strategy

- Existing tests affected: `test_contract.py` (optional description assertion);
  `test_report_html.py` untouched
- Full suite command: `bash framework/ai-sdlc/verify.sh` then
  `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: n/a — no product API change
- Legacy paths at risk: none

## Playwright Strategy

- n/a — kit HTML/contract tests only; no Notaire product UI surface in this tip
  (avoids product Playwright contention; Phase B host is `framework/ai-sdlc`)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: kit-only; no image change required for HTML export
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc backoffice --html /tmp/bo.html` on a
  profile with catalogs; optional brief `serve` create→history round-trip

## Rollback Strategy

- Revert safe: yes — additive kit modules/CLI; no schema migration
- Database rollback: none needed
- Data written under the new behavior after revert: catalog revisions remain
  valid Phase A artifacts
- Blast radius if rollback is delayed: local HTML/serve only

## Migration Plan

1. Gate 1 OpenSpec + branch (done in planning).
2. Red tests for HTML shell, governance, redaction, serve mutations.
3. Implement render + adapters + CLI.
4. Green kit verify + preflight --full.
5. PR `Closes #2240` only.
