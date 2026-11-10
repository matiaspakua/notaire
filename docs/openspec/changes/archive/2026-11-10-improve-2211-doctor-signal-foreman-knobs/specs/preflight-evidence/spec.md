## ADDED Requirements

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
