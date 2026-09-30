# Design

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md).

## Context

The repository previously contained an unused `Jenkinsfile` legacy pipeline. The current CI strategy uses GitHub Actions exclusively. Removing this file eliminates a maintenance burden and clarifies where to look for build configuration.

## Goals / Non-Goals

### Goals

- Delete the obsolete `Jenkinsfile` from the repository.
- Update documentation to reflect that CI now relies solely on GitHub Actions.

### Non‑Goals

- No change to runtime code or application behavior.
- No addition of new CI steps or integration tests.

## Decisions

- **Delete file**: The Jenkinsfile is not referenced in any workflow and is considered dead code. Removing it is safe and reduces confusion.
<!-- No documentation record needed for artifact removal -->

## Riesgos / Trade-offs

- *Risk*: inadvertent reference in some hidden scripts could break a workflow. *Mitigation*: Verify that no CI job pulls a Jenkinsfile and run local preflight checks.
- *Trade‑off*: Historically, Jenkins allowed certain build features (e.g. matrix build). Since the repository is now fully GitHub‑Actions‑driven, that capability is intentionally dropped.

## Testing Strategy

No new unit or integration tests are required, as the change is purely artifact removal.

## Regression Strategy

- Run `scripts/preflight.sh --fix` locally.
- Execute `mvn test -pl backend-api` and `npx vitest run` to confirm build.

## Playwright Strategy

n/a – no UI impact.

## Deployment Strategy

Deploying this change simply amounts to merging the PR into the main branch. GitHub Actions will read the updated workflow configuration automatically.

## Rollback Strategy

If something unexpected occurs, the change can be safely undone by reverting the commit that deleted the stale `Jenkinsfile`. Since the file was unused and never referenced, no downstream build or runtime state is affected by its removal.

## Summary of Deliverables

- Removed `Jenkinsfile`.
- Updated docs with removal notice.
- Confirmed all CI and test suites remain green.
