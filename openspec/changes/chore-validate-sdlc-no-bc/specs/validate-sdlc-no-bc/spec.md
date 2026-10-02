## MODIFIED Requirements

### Requirement: Gate 1 scenario counting does not require bc

`scripts/validate-sdlc-plan.sh` SHALL sum per-file counts of lines matching
`^#### Scenario:` under a change's `specs/` directory using arithmetic that does
not require the `bc` utility (for example `awk`). The summed count SHALL be
identical to the historical `paste -sd+ | bc` result when `bc` is present. When
the sum is zero, Gate 1 SHALL still fail with the existing "no Scenario" message.

#### Scenario: Scenario count works without bc

- **WHEN** a filled notaire-sdlc change has at least one `#### Scenario:` and
  `bc` is missing or exits non-zero on PATH
- **THEN** `validate-sdlc-plan.sh` reports a non-zero scenario count and does not
  fail Gate 1 solely because of scenario counting

#### Scenario: Scenario count still works when bc exists

- **WHEN** the same filled change is validated with a working `bc` on PATH
- **THEN** the validator still accepts the change and reports the same scenario
  count

#### Scenario: Zero scenarios still fails Gate 1

- **WHEN** a notaire-sdlc change has spec files but no `#### Scenario:` headings
- **THEN** the validator exits non-zero and reports that no scenarios were found
