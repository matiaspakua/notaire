> Governed by CONSTITUTION.md — groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1259 + CU76
- [x] 1.2 Copy offline OpenSpec tree after #1422/#1419 settle as needed
- [x] 1.3 `openspec validate --strict` + `validate-sdlc-plan.sh`

## 2. Crear branch

- [x] 2.1 Branch `cursor/chore-1259-agent-context-cf98` from updated main

## 3. Gate 2 — TDD

- [x] 3.1 Add failing `test_agent_context_budget.py` / budget script max-tokens
- [x] 3.2 Observe fail on current ~30k load

## 4. Implementación

- [x] 4.1 Add CONSTITUTION-AGENT-CARD.md from stockpile
- [x] 4.2 Replace AGENTS.md with slim draft
- [x] 4.3 alwaysApply false on large skills/rules
- [x] 4.4 agent-context-budget.py + wire to metrics/docs

## 5. Actualizar tests existentes

- [x] 5.1 Budget ≤8k green; check-agent-rules green

## 6. Ejecutar regresión

- [x] 6.1 workspace/docs verify scripts as applicable

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI

## 8. Gate 3 — Documentación

- [x] 8.1 Cloud packing note; baseline refresh; CHANGELOG

## 9. Commits atómicos

- [x] 9.1 Conventional; `Closes #1259` `Refs #1197`

## 10. Pull Request y validación CI

- [ ] 10.1 Push; PR; heavy CI / process checks

## 11. Deploy

- [ ] 11.1 Merge via PR

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Meter command documented; issue closed

## Definition of Done

- [x] Always-loaded ≤8000 tokens by meter
- [x] Gates still enforceable
- [ ] All gates passed
