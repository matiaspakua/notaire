# Raise JaCoCo Coverage Floor Thresholds

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This design documents
> the testing and deployment strategy for raising JaCoCo coverage floor thresholds.

## Context

Current JaCoCo configuration in `backend-api/pom.xml`, `.claude/rules/code-quality.md` and `CONSTITUTION.md`
~74% branch. The branch floor (25%) is unrealistically low and doesn't drive meaningful
quality improvements.

Evidence:
- `backend-api/pom.xml:329,334`: floor 70% line / 25% branch, actual 84.9% / 74.0%
- JaCoCo reports: line 74%, branch 25%
- `JacocoCoverageConfigConsistencyTest#shouldHaveConsistentCoverageFloorAcrossDocsAndPom`:
  validates pom.xml and code-quality.md have matching thresholds

## Goals / Non-Goals

**Goals:**
- Raise JaCoCo line coverage floor from 70% to 80%
- Raise JaCoCo branch coverage floor from 25% to 65%
- Ensure both pom.xml and code-quality.md have aligned threshold values
**Non-Goals:**
- Raising line floor above 80% (intermediate target; business package at ~75% is fragile)
- DeedManagement class test coverage (~41% gap, requires dedicated service tests)
- Business package branch coverage optimization (already at ~67%, close to 65% floor)

## Decisions

- **Raise thresholds incrementally:** 80% line / 65% branch provides ~15% headroom vs 85%/70%.
- **Update both configuration files simultaneously:** pom.xml and code-quality.md must
  have aligned values; `JacocoCoverageConfigConsistencyTest` enforces this.
- **Align CONSTITUTION.md with pom.xml:** The CONSTITUTION.md floor must match the
  pom.xml JaCoCo thresholds for consistency.
- **Enable branch floor ratcheting:** The values are "ratchet floor" thresholds that
  increase automatically on subsequent PRs when coverage improves.

## Riesgos / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| Business package branch coverage at ~67% is close to 65% floor | CI will fail if threshold pushed too high | Set 65% floor; monitor business layer tests after merge |
| DeedManagement class at ~41% will fail branch check | Service layer test gap | File follow-up issue for dedicated DeedManagement service tests |
| Line floor 80% may be too aggressive for some packages | Some packages may fail | 80% is intermediate target; acceptable for most packages |

## Testing Strategy

### Unit Tests
- `JacocoCoverageConfigConsistencyTest#shouldHaveConsistentCoverageFloorAcrossDocsAndPom`
  - Arrange: Load current pom.xml and code-quality.md
  - Act: Call coverage floor accessor
  - Assert: Both have line coverage floor/line coverage floor = 80, branch coverage floor/branch coverage floor = 65

### Integration Tests
- `mvn jacoco:check -pl backend-api`
  - Arrange: Run JaCoCo analysis on backend-api module
  - Act: Quality gate validates thresholds
  - Assert: No threshold violation exceptions thrown

### Frontend E2E
- N/A — no UI or API changes in this change

## Regression Strategy

- **Functional regression:** None — this is a configuration-only change
- **CI regression:** Only `mvn jacoco:check` affected; full backend suite unaffected
- **Risk:** If 65% branch floor is too high, CI will start failing
  - **Mitigation:** Review branch coverage report after merge; if business layer < 65%,
    have team prepare additional integration tests

## Playwright Strategy

n/a — no UI surface changes.

## Deployment Strategy

### Pre-deployment
- No database migration needed (Flyway schema unchanged)
- No configuration in `.env` required

### Deployment
- No app redeploy needed — CI changes only
- Changes affect:
  - JaCoCo analysis during builds
  - Quality gate validation in CI

### Post-deployment verification
- Run `mvn jacoco:check -pl backend-api` locally
- Verify CI "Quality Gate" job passes (Gate 4)

## Rollback Strategy

If new thresholds cause excessive CI failures:
1. Revert pom.xml and code-quality.md to 70%/25%
2. Restore test expectations in `JacocoCoverageConfigConsistencyTest`
3. File new issue to address coverage gaps (DeedManagement, business package) incrementally

## Migration Plan

Not applicable — single-change atomic update of configuration thresholds.
