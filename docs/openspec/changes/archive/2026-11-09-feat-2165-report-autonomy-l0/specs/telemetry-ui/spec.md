## MODIFIED Requirements

### Requirement: Static HTML monitor from evidence
The system SHALL support `aisdlc report --html <path>` that writes a self-contained HTML page summarizing stage/gate pass rates, stage durations, cost totals, optional lead-time from a fixture, and the Wilson L0 `autonomy` object (status, level, wilson_lower, trials) from required `signal` gates. The page SHALL be read-only presentation of evidence JSON; it SHALL NOT provide controls that mutate policy, catalogs, or gates, SHALL NOT claim level 3, and SHALL NOT render prompts, diffs, or secrets. `aisdlc report --json` SHALL include the same `autonomy` object.

#### Scenario: HTML contains stage and cost summaries
- **WHEN** an evidence ledger has stage runs and cost entries and the operator runs `aisdlc report --html out.html`
- **THEN** the file exists and includes pass-rate and cost summary text without prompts, diffs, or secret values

#### Scenario: Lead-time fixture embeds without secrets
- **WHEN** `--lead-time-fixture` points at a forge sample JSON
- **THEN** the HTML includes lead-time seconds (or no-evaluable) and never includes a `prompt` field

#### Scenario: JSON and HTML surface autonomy L0
- **WHEN** the operator runs `aisdlc report --json` or `aisdlc report --html PATH`
- **THEN** the payload / page includes autonomy with level 0 by default (or 1 if demoted)
  and status one of `no-evaluable`, `ok`, `held`, or `demoted`
