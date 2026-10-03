> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

After #953 / OpenCollection work, Cloud Agents need Bruno CLI ≥4.x on PATH.
`.cursor/install.sh` already pins OpenSpec but not Bruno; coordinator VMs showed
`bru` missing (`bruno-cli-gap-evidence`). CI often uses unpinned
`npx @usebruno/cli`; older 2.x fails OpenCollection roots. Pin 4.2.0 in install
and document it under the same fleet process RNF as #1121.

## Goals / Non-Goals

**Goals:**

- Pin `@usebruno/cli@4.2.0` in `.cursor/install.sh` after the OpenSpec block.
- Ensure `bru` is on PATH via `/usr/local/bin/bru`.
- Document Bruno ≥4.2.0 in ENVIRONMENT-CHECKLIST toolchain / PATH sections.
- Satisfy Process Checks with this OpenSpec folder (no human `sdlc-exception`).

**Non-Goals:**

- Changing CI Bruno workflow pinning (stays unpinned `npx` unless a later issue).
- Closing #1121 (Closes-keyword docs; use `Related: #1121` only).
- Product API / UI changes.
- Triggering Environment Save / rebuild (coordinator after merge).

## Decisions

1. **Pin exact 4.2.0** — matches OpenCollection ≥4.x need and the ready patch;
   avoids floating majors on cloud boots.
2. **Symlink with fallback** — prefer `$HOME/.local/bin/bru`, else
   `$(npm root -g)/@usebruno/cli/bin/bru.js` under `/usr/local/bin/bru`.
3. **Related #1121, not Closes** — Gate 1 needs an OPEN Issue; #1121 is the open
   cloud-fleet process issue covering install.sh PATH docs. Do not close it.
4. **OpenSpec over sdlc-exception** — agents cannot set the human-only label.

## Riesgos / Trade-offs

- [npm install -g slows install.sh] → Acceptable; install runs once per snapshot/boot.
- [Symlink target differs by npm prefix] → Fallback path covers both layouts.
- [Attaching to #1121 without closing] → Traceability uses Related; dedicated chore
  Issue preferred later if Owner wants a closer-scoped ticket.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| install.sh pins Bruno 4.2.0 | static / review | grep `@usebruno/cli@4.2.0` in `.cursor/install.sh` |
| checklist documents Bruno ≥4.2.0 | static / review | ENVIRONMENT-CHECKLIST toolchain table |
| OpenSpec Gate 1 structure | script | `bash scripts/validate-sdlc-plan.sh chore-bruno-cli-install-pin` |

- New unit tests (`scripts/tests/`): n/a — install script change verified by
  presence checks + CI Process Checks / SDLC plan validation
- New integration tests: n/a — no Java surface
- Coverage impact (JaCoCo): n/a

## Regression Strategy

- Existing tests affected: none (no production `backend-api` / `frontend` code).
- Full suite command: `bash scripts/validate-sdlc-plan.sh chore-bruno-cli-install-pin`
- HTTP/Bruno API suite: n/a for this PR (pin enables workers; CI Bruno unchanged)
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. Install script + Markdown docs only.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge via PR; coordinator triggers environment
  build + `propose-environment-json` after merge; user Save still required
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): on a VM that ran install,
  `bru --version` reports 4.2.x

## Rollback Strategy

- Revert safe: yes — revert the PR; cloud boots lose pinned Bruno again
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: agents keep Bruno on PATH (benign)
