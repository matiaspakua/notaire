## ADDED Requirements

### Requirement: A run started from the CLI carries its intent

`aisdlc run start --intent <id>` SHALL validate the intent, record it on the run and attach the run
to the intent, the same as `POST /factory/runs`.

#### Scenario: The run records its intent

- **WHEN** `aisdlc run start --project web --workflow feature --intent <id>` runs for a submitted intent
- **THEN** the run shows that intent, the intent lists the run, and `aisdlc status` reports the intent

#### Scenario: An unknown intent is refused

- **WHEN** `aisdlc run start` is given an intent id that does not exist
- **THEN** it exits 2 saying the intent was not found, and no run is recorded
