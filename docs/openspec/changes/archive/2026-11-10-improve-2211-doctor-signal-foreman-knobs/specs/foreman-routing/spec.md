## ADDED Requirements

### Requirement: Unmapped Foreman stages are stated honestly
`aisdlc foreman plan|route [--json]` SHALL report gate stages that have no
`[[stages]]` knobs as unmapped (for example an `unmapped_stages` list and/or an
explicit note). Unmapped stages are **expected** until knobs are filled. The kit
MUST NOT invent fake stage knobs for red/green/pre-pr/merge/release/`signal` to
inflate `mapped_count`. Doctor's Foreman INFO line SHALL point operators at this
honesty (mapped vs unmapped / knobs optional).

#### Scenario: foreman plan lists unmapped stages
- **WHEN** a profile has gates for `plan` and `pre-push` but `[[stages]]` knobs only
  for `plan`, and `foreman plan --json` runs
- **THEN** `mapped_count` is 1, the JSON includes `pre-push` under unmapped stages
  (or an equivalent honesty field), and the note states that unmapped stages are
  expected until knobs are filled

#### Scenario: foreman doctor note mentions unmapped
- **WHEN** `aisdlc doctor` runs on a valid profile
- **THEN** the Foreman INFO line mentions that unmapped stages are expected until
  `[[stages]]` knobs are filled (and still denies execute / Isolator / sandbox)
