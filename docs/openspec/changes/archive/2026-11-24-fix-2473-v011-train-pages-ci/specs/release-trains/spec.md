## ADDED Requirements

### Requirement: The next minor's train exists before a release is cut

The release trains SHALL always include the train for the minor after the current release
(`.release-please-manifest.json`), so a release-please PR can never cut a version without its train.

#### Scenario: the next train is missing

- **GIVEN** the manifest at `0.11.0` and no `v0.12 · …` train
- **WHEN** the release tests run
- **THEN** `test_the_next_minor_already_has_a_train` fails

#### Scenario: v0.11 resolves its train

- **WHEN** `train_for("0.11.0")` runs
- **THEN** it returns `v0.11 · Factory evidence & kit-only SDLC (2027-03)`

### Requirement: Pages CI regenerates its data before the tests

Pages CI SHALL run the prebuild generators (release history, testing inventory, RepoLens bench snapshot) before
Vitest, and the RepoLens Pages test SHALL derive its headline labels from the committed bench JSON.

#### Scenario: a release lands before the snapshot is regenerated

- **GIVEN** a manifest one release ahead of the committed `releases.json`
- **WHEN** Pages CI runs
- **THEN** the data is regenerated first and `pages-status.test.ts` passes

#### Scenario: a re-measured latency

- **GIVEN** a refreshed `retrieval.json` with a new latency ratio
- **WHEN** `repolens-pages.test.ts` runs
- **THEN** the label it expects is computed from that JSON
