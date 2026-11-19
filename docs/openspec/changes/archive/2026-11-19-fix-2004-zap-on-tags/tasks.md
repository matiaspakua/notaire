> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2004 and CU78
- [x] 1.2 Acceptance is the tag trigger, not a new scan
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/2004-zap-on-tags-47dc` from `origin/main` at `7f1feeb0`

## 3. Gate 2 — Escribir tests

- [x] 3.1 `test_dast_zap_runs_on_version_tags_and_keeps_monday` written first
- [x] 3.2 Observed `AssertionError: 'v*' not found in []`

## 4. Implementación

- [x] 4.1 Add `push.tags` `v*` and `notaire-v*` to `dast-zap.yml`
- [x] 4.2 Leave Monday, manual dispatch, `fail_action`, and `.zap/rules.tsv` as they are

## 5. Actualizar tests existentes

- [x] 5.1 ZAP ratchet and gate-surface tests still assert the blocking baseline

## 6. Ejecutar regresión

- [x] 6.1 Workflow unit tests
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface. The change is a GitHub Actions trigger.

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Security program, assessment, tools, factory gates, DevSecOps row, and the cybersec skill

## 9. Commits atómicos

- [x] 9.1 Conventional commit referencing #2004

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, then open the PR. The issue closes when this trigger is on `main`.

## 11. Deploy

- [ ] 11.1 Merge when preflight exit 0, required checks are green, and the branch is clean

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The next version tag is the live proof. This issue is finished when the trigger is on `main`.

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open and the tag trigger merged
