# preflight-evidence Specification

## Purpose
TBD - created by archiving change feat-2159-preflight-evidence. Update Purpose after archive.

## Requirements

### Requirement: Local preflight records a signal stage
Notaire's local preflight SHALL feed the aisdlc evidence ledger by running the
cheap `signal` stage through `aisdlc gate` (which calls `record_stage`), not by
inventing a separate portal or writer.

#### Scenario: Profile declares signal stage
- **WHEN** the Notaire profile is loaded
- **THEN** it declares stage `signal` whose gates run ratchet (against the base)
  and skills lint

#### Scenario: Preflight invokes gate signal
- **WHEN** an agent inspects `workspace/sdlc/preflight.sh`
- **THEN** the script invokes `aisdlc gate signal` (with the Notaire profile) and
  does not call `aisdlc ratchet` / `aisdlc skills` as separate top-level `run`
  lines

#### Scenario: Signal run records ledger
- **WHEN** `aisdlc gate signal --base origin/main` completes successfully on Notaire
- **THEN** `.aisdlc/evidence/ledger.jsonl` gains a stage record with `"stage":"signal"`
  and `aisdlc report --json` shows at least one run
