# cd-pin-tested-sha Specification

## Purpose

TBD - created by archiving change fix-1042-cd-tested-sha. Update Purpose after archive.

## Requirements

### Requirement: latest moves only after the SHA tag succeeds

Mutable `latest` MUST NOT be the sole or first tag push for a CD publish. The SHA-tagged image push MUST succeed before `latest` is moved to that digest. `latest` SHALL move when the ref is a `v*` or `notaire-v*` tag (including a `workflow_dispatch` whose ref is that tag) or when the ref is the default branch. It SHALL NOT move merely because CI finished on `main`.

#### Scenario: latest is gated after SHA-tagged push

- **WHEN** CD publishes from a version tag
- **THEN** the workflow pushes the SHA-tagged image first, and only a later step tags `latest` to the same digest
- **AND** that later step's condition includes `refs/tags/v` and does not depend on `workflow_run`

### Requirement: CD publishes on version tags and manual dispatch only

`cd.yml` SHALL trigger on `push` tags `v*` and `notaire-v*` and on `workflow_dispatch`. It SHALL NOT trigger on `workflow_run` or on push to a branch. Checkout and the publish SHA SHALL use `github.sha` and SHALL NOT read `github.event.workflow_run.head_sha`.

#### Scenario: No workflow_run trigger

- **WHEN** `.github/workflows/cd.yml` triggers are read
- **THEN** `workflow_run` is absent, `push.tags` includes `v*` and `notaire-v*`, and `workflow_dispatch` is present

#### Scenario: Publish SHA is the tag commit

- **WHEN** the `build-and-publish` checkout and publish-SHA step are read
- **THEN** they do not reference `workflow_run`, and the publish SHA is `github.sha`
