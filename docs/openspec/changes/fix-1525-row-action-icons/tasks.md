# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1525 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Create branch

- [x] 2.1 `fix/1525_notaire_icon_shrink` from updated `main`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 1 Vitest failure and 3 Playwright failures on the pre-change build

## 4. Implementation

- [x] 4.1 Minimal implementation

## 5. Update existing tests

- [x] 5.1 Existing tests adapted without weakening assertions

## 6. Run regression

- [x] 6.1 `bash frontend/verify.sh` (see PR)

## 7. Run Playwright

- [x] 7.1 TS-0140 6/6 (see PR)

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 Documentation updated (see traceability); CHANGELOG held (serialize)

## 9. Atomic commits

- [x] 9.1 Conventional Commits; the PR body ends with `Closes #1525`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test and close

- [ ] 12.1 Smoke test and close issue

## Definition of Done

- [ ] All gates passed
