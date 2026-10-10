# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1401 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Create branch

- [x] 2.1 `cursor/cloud-install-jdk26-8fb3` from updated `main`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Observed tip Cloud RECURRING install fail: `release version 26 not supported` (`bld-20261010-71b55b1f…`)
- [x] 3.2 Observed local compile fail on JDK 21; pass after Temurin 26 install

## 4. Implementation

- [x] 4.1 Add Temurin 26 bootstrap to `.cursor/install.sh`

## 5. Update existing tests

- [ ] 5.1 n/a (no product tests)

## 6. Run regression

- [ ] 6.1 `bash workspace/sdlc/preflight.sh --full` (or `--fix`) on tip with JDK 26
- [ ] 6.2 Cloud AGENT MANUAL build install exit 0

## 7. Playwright

- [x] 7.1 Not applicable (no UI change)

## 8. Gate 3 — Update permanent documentation

- [ ] 8.1 `CHANGELOG.md` entry

## 9. Atomic commits

- [ ] 9.1 Conventional Commit with `Closes #1401`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open draft PR
- [ ] 10.2 Wait for CI (do not merge without coordinator heavy-CI gate)

## 11. Deploy

- [ ] 11.1 n/a (bootstrap script only)

## 12. Gate 5 — Smoke and close

- [ ] 12.1 Confirm next RECURRING or MANUAL Cloud install succeeds; close #1401 via PR

## Definition of Done

- [ ] Tip Cloud install exits 0 with JDK 26
- [ ] PR CI green; coordinator heavy-CI before merge
