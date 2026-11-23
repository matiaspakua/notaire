## MODIFIED Requirements

### Requirement: Snapshot preserves existing timestamp when data is unchanged

When the generator would produce a snapshot whose data (all fields except
`generated_at`) matches the currently tracked file, the script MUST reuse the
existing `generated_at` value so the written file is byte-identical to the
committed version.

#### Scenario: Stable timestamp when data unchanged

- **GIVEN** `github-page/lib/repolens-bench-snapshot.json` exists with a committed `generated_at` value
- **WHEN** `generate-repolens-bench.py` runs and the bench data has not changed
- **THEN** the written file retains the original `generated_at` and is byte-identical to the committed file

### Requirement: Snapshot writes new timestamp when data changes

When the generator produces a snapshot whose data differs from the currently
tracked file, the script MUST write a fresh `generated_at` timestamp reflecting
the current generation time.

#### Scenario: New timestamp when data changes

- **GIVEN** `github-page/lib/repolens-bench-snapshot.json` exists with a previous `generated_at` value
- **WHEN** `generate-repolens-bench.py` runs and the underlying bench data has changed
- **THEN** the written file contains a new `generated_at` value different from the previous one
