## ADDED Requirements

### Requirement: Notaire Foreman plan remains honest after knobs expand
After Notaire fills additional `[[stages]]` knobs, `aisdlc foreman plan --json`
SHALL still list any remaining gate stages without knobs under
`unmapped_stages` (for example `merge`, `release`, `signal`). Expanding knobs
MUST NOT remove honesty fields (`executes=false`, `sandbox=false`, note denying
execution / Isolator). Doctor's Foreman INFO line SHALL continue to state that
unmapped stages are expected until knobs are filled.

#### Scenario: remaining Notaire stages stay unmapped honestly
- **WHEN** Notaire maps `plan`, `red`, `pre-push`, and `pre-pr` but not `merge`
  / `release` / `signal`, and `foreman plan --json` runs
- **THEN** `mapped_count` is ≥ 4, `unmapped_stages` includes the unmapped gate
  stages still without knobs, and `executes` / `sandbox` remain false
