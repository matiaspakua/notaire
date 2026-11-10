## Purpose
Approval-gated ForgePort write / `factory/gate` attest. Dry-run by default;
scoped live forge mutation only after ApprovalPort verify.

## ADDED Requirements

### Requirement: Factory gate attest verifies via ApprovalPort
The kit SHALL provide `aisdlc forge attest` that builds a `factory/gate` attest
subject (schema 1 JSON with gate name and outcome) and verifies it via
ApprovalPort before any forge mutation. Without a successful Approval verify
(or when Approval is `no-evaluable` for that subject), the command SHALL refuse
write honestly: `attested=false`, `write_enabled=false`, and SHALL NOT claim a
live attestation. The subject bytes that Approval signs/verifies SHALL be the
exact attest payload.

#### Scenario: Without Approval verify attest refuses and stays unattested
- **WHEN** `forge attest` runs without a verifiable Approval signature for the subject
- **THEN** the result has `attested=false`, `write_enabled=false`, and no mutating `gh` invocation occurs

#### Scenario: Verified Approval dry-run attests without writing
- **WHEN** Approval verify succeeds and `--write` is not set
- **THEN** the result has `attested=true`, `write_enabled=false`, `approvals_live` reflecting ApprovalPort, and no mutating `gh` invocation occurs

### Requirement: Scoped live write only behind verified Approval and explicit flag
Live forge mutation SHALL require both a successful Approval verify and an
explicit write enablement (`--write` or equivalent). Allowed write kinds SHALL
be limited to scoped surfaces: `comment`, `label`, and `check-run`. Unscoped
mutations (merge, close, edit body, approve review, etc.) SHALL remain refused.
Default SHALL be dry-run (`write_enabled=false`). Claiming `write_enabled=true`
when only dry-run executed is forbidden.

#### Scenario: Write without verify is refused
- **WHEN** `--write` is set but Approval verify fails or is unavailable
- **THEN** no mutating `gh` invocation occurs and `write_enabled` stays false

#### Scenario: Write with verify performs only scoped mutation
- **WHEN** Approval verify succeeds, `--write` is set, kind is `comment` (or `label` / `check-run`), and a PR or issue target is provided
- **THEN** the injectable runner is invoked with a scoped write argv for that kind only and the result reports `write_enabled=true` and `attested=true`

#### Scenario: Unscoped mutation verbs remain refused
- **WHEN** a write path is asked to merge, close, or approve
- **THEN** ForgePort refuses those verbs and does not invoke them

### Requirement: JSON honesty and doctor for ForgePort write
`forge attest --json` SHALL emit honesty fields including `write_enabled`,
`attested`, `approvals_live`, and `detail`. `aisdlc doctor` SHALL surface a
ForgePort write INFO line (dry-run vs live capability) and SHALL NOT fail the
profile solely because live write is unavailable.

#### Scenario: JSON honesty fields are present
- **WHEN** `forge attest --json` runs
- **THEN** the payload includes `write_enabled`, `attested`, `approvals_live`, and `detail`

#### Scenario: Doctor reports forge write honesty without failing alone
- **WHEN** `aisdlc doctor` runs on a valid profile
- **THEN** output mentions ForgePort write/attest honesty (dry-run or gated write) and exit code is not 1 solely because live write is unavailable
