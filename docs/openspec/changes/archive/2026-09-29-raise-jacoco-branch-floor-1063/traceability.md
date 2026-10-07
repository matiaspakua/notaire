# Traceability

## Issue

- **Issue #1063:** Improve JaCoCo coverage floor configuration
  - "branch coverage floor 70%/25%, actual is 74%, but 74% < 70%"
  - Test coverage reports: DeedManagement at ~41% (major gap), business package ~67%

## Use Case

CU76 — Quality Assurance and Testing Infrastructure

## Chain

```text
#1063 → CU76 → [requirements] → [tests] → [files]
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1063 | closed |
| Use Case | CU76 | exists |
| Specification | `openspec/changes/raise-jacoco-branch-floor-1063/` | valid |
| Branch | `test/1063_raise_jacoco_branch_floor` | open |
| Tasks | `tasks.md` | in progress |
| Commits | 322f9ee, ba46127, 63f4581, ae4300f, d819b54, eff58fe | done |
| Pull Request | #1090 | merged |
| CI run | ci.yml 36547641597 on `e52307f` | green |
| Merge commit | `e52307f` | merged |
| Release / tag | cd.yml 36548557870 (GHCR image) | published |
| Smoke test | `/actuator/health` UP on `e52307f` | passed |

## Requirements and Scenarios

| Requirement | Scenario | Test | File |
|-------------|---------|------|------|
| JaCoCo SHALL enforce minimum coverage thresholds via pom.xml | Floor is at least 80% line / 65% branch | `JacocoCoverageConfigConsistencyTest#shouldEnforceRaisedCoverageFloor` (new) | `backend-api/pom.xml` |
| JaCoCo SHALL enforce minimum coverage thresholds via pom.xml | Threshold values are proven by successful build | `mvn jacoco:check -pl backend-api` | `backend-api/pom.xml` |
| Documented floor SHALL match pom.xml | Code-quality.md matches pom.xml | `JacocoCoverageConfigConsistencyTest#shouldHaveConsistentCoverageFloorAcrossDocsAndPom` | `.claude/rules/code-quality.md` |
| Documented floor SHALL match pom.xml | CONSTITUTION.md matches pom.xml | `JacocoCoverageConfigConsistencyTest#shouldMatchConstitutionFloorToPom` (new) | `CONSTITUTION.md` |

## Requirement coverage

- [ ] Requirement: JaCoCo SHALL enforce minimum coverage thresholds via pom.xml (4 scenarios)
- [ ] Requirement: Documented floor SHALL match pom.xml (2 scenarios)

## Planned Tests

| Test | File | Type | Status |
|------|------|------|--------|
| `JacocoCoverageConfigConsistencyTest#shouldEnforceRaisedCoverageFloor` | `backend-api/src/test/java/com/licensis/notaire/unit/JacocoCoverageConfigConsistencyTest.java` | unit (new, red) | pending |
| `JacocoCoverageConfigConsistencyTest#shouldMatchConstitutionFloorToPom` | `backend-api/src/test/java/com/licensis/notaire/unit/JacocoCoverageConfigConsistencyTest.java` | unit (new, guard) | pending |

## Planned Files

| File | Purpose | Action |
|------|---------|--------|
| `backend-api/pom.xml` | JaCoCo ratchet floor | LINE 0.70→0.80, BRANCH 0.25→0.65 |
| `.claude/rules/code-quality.md` | Documented floor | 70% line / 25% branch → 80% line / 65% branch |
| `CONSTITUTION.md` | Documented floor (line 496) | 70% line / 25% branch → 80% line / 65% branch |
| `JacocoCoverageConfigConsistencyTest.java` | Consistency test | add shouldEnforceRaisedCoverageFloor, shouldMatchConstitutionFloorToPom |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `.claude/rules/code-quality.md` | yes | ae4300f |
| `CONSTITUTION.md` | yes | ae4300f |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 4 | CI green, review approved, no conflicts | yes | PR #1090 reviewed by the foreman, merged by the owner |
| 5 | Deployed, smoke test passed, Issue closed | yes | ci.yml 36547641597, cd.yml 36548557870, health UP, #1063 closed |

## Exceptions

None at this time.
