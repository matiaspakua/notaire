## ADDED Requirements

### Requirement: CD builds the CI-tested SHA on workflow_run

When CD is triggered by a successful CI `workflow_run` on `main`, the
`build-and-publish` job MUST check out and build
`github.event.workflow_run.head_sha` — not the moving tip of `main` at CD
start time — so the signed GHCR image matches the commit CI tested.

#### Scenario: Checkout pins workflow_run head SHA

- **WHEN** `.github/workflows/cd.yml` job `build-and-publish` runs the
  `actions/checkout` step on a `workflow_run` trigger
- **THEN** that step sets `with.ref` to an expression that resolves to
  `${{ github.event.workflow_run.head_sha }}` for `workflow_run` (tag /
  `workflow_dispatch` MAY fall back to `${{ github.sha }}`)

#### Scenario: Image is tagged with the publish SHA

- **WHEN** CD publishes the backend image after a `workflow_run` CI success
- **THEN** the published tags include the same publish SHA used for checkout
  (explicit raw/SHA tag derived from `workflow_run.head_sha`, not metadata
  defaults that read tip-of-default-branch `github.sha` under `workflow_run`)

### Requirement: latest moves only after the SHA tag succeeds

Mutable `latest` MUST NOT be the sole or first tag push for a CD publish of a
CI-tested commit. The SHA-tagged image push MUST succeed before `latest` is
moved to that digest.

#### Scenario: latest is gated after SHA-tagged push

- **WHEN** CD publishes from a successful CI `workflow_run` on the default branch
- **THEN** the workflow pushes (or builds+pushes) the SHA-tagged image first,
  and only a later step tags/pushes `latest` to the same digest (or equivalently
  depends on the SHA push succeeding before `latest` is applied)

### Requirement: CD skips publish when CI did not succeed

CD MUST NOT publish when the triggering CI `workflow_run` concluded with any
result other than `success`.

#### Scenario: Non-success CI skips build-and-publish

- **WHEN** CD is triggered by `workflow_run` and
  `github.event.workflow_run.conclusion` is not `success`
- **THEN** job `build-and-publish` is skipped via its `if` condition
  (`github.event_name != 'workflow_run' || github.event.workflow_run.conclusion == 'success'`)
