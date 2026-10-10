# Tasks — chore-1261-deprecated-owner-decision

## 1. Gate 1

- [x] 1.1 proposal / traceability / specs / design / tasks

## 2. TDD

- [x] 2.1 Add failing `workspace/tests/test_adr022_owner_decision_pack.py`
- [x] 2.2 Observe failure on current ADR-022 / Pages content

## 3. Implement

- [x] 3.1 Amend ADR-022 with Pending Owner decision (#1261)
- [x] 3.2 Update REPO-SPLIT-PLAN P0.6 row
- [x] 3.3 Link ADR-022 (+ #1261) on Pages Architecture
- [x] 3.4 CHANGELOG Unreleased
- [x] 3.5 Make unit test green

## 4. Validate

- [x] 4.1 `python3 -m unittest workspace.tests.test_adr022_owner_decision_pack`
- [x] 4.2 `openspec validate chore-1261-deprecated-owner-decision --strict`
- [ ] 4.3 PR + heavy CI (docs path may skip Java/E2E leaves)
