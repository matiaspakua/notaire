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

### Requirement: Doctor accepts signal as a documented local alias
The kit SHALL treat stage id `signal` as a documented adopter-local alias used for
cheap local ratchet/skills ledger recording (#2159). `aisdlc doctor` MUST NOT emit
a "not canonical" WARN for `signal`. Other non-canonical kebab-case stage ids
SHALL continue to warn. The kit MUST NOT rename Notaire's `signal` gates to a
canonical id in this change.

#### Scenario: doctor accepts signal local alias
- **WHEN** a profile declares gates with `stage = "signal"` and `aisdlc doctor` runs
- **THEN** the command exits 0 (when otherwise healthy) and the output does not
  contain a WARN that stage `'signal'` is not canonical

#### Scenario: unknown custom stage still warns
- **WHEN** a profile declares a gate with `stage = "custom-ops"` (not `signal`, not
  canonical) and `aisdlc doctor` runs
- **THEN** the output contains a WARN that stage `'custom-ops'` is not canonical
