## Purpose
ApprovalPort cryptographic custody runtime (ADR-005): mint and verify authority
signatures. Honest `approvals_live` only after a proven round-trip.

## Modified Requirements

### Requirement: ApprovalPort mint/verify custody
The system SHALL provide an ApprovalPort that mints and verifies cryptographic
approvals bound to exact subject bytes via OpenSSH `ssh-keygen -Y` (SSH sign /
verify). Authority SHALL come from token + channel (configured identity + private
key), never from request-body role fields alone. Agents SHALL NEVER self-approve:
`agent_can_approve` SHALL always be false, and mint SHALL refuse an agent role.
`approvals_live` SHALL be true **only** after a proven mint→verify round-trip with
resolved keys; when keys or signer tooling are missing (or Approval is forced off),
status SHALL be `no-evaluable` with `approvals_live=false`. `aisdlc doctor` SHALL
surface that status as informational and SHALL NOT fail the profile solely because
ApprovalPort is unevaluable. Local/dev keys MAY be used in kit tests and documented
opt-in; the system SHALL NOT claim production HSM custody from this slice alone.

#### Scenario: Without keys ApprovalPort stays no-evaluable and not live
- **WHEN** no approval signing key / allowed_signers / identity is configured
- **THEN** ApprovalPort status is `no-evaluable`, `approvals_live` is false, and `agent_can_approve` is false

#### Scenario: Doctor reports ApprovalPort without failing for unevaluable alone
- **WHEN** `aisdlc doctor` runs on a valid profile without ApprovalPort keys
- **THEN** output mentions ApprovalPort `no-evaluable` (or approvals not live) and exit code is not 1 solely because ApprovalPort is unevaluable

#### Scenario: Agent self-approval remains forbidden
- **WHEN** ApprovalPort status or mint is consulted
- **THEN** `agent_can_approve` is false and mint refuses an agent role

#### Scenario: Mint then verify round-trip sets approvals live
- **WHEN** local/dev SSH keys and identity are configured and mint then verify succeed on a subject
- **THEN** ApprovalPort status reports `approvals_live=true`, an algorithm, and the subject identity/principal used

#### Scenario: Tampered subject fails verify
- **WHEN** a valid signature is verified against altered subject bytes
- **THEN** verify fails closed and does not claim a live approval for that subject

#### Scenario: Missing signer tooling degrades honestly
- **WHEN** `ssh-keygen` with `-Y` support is unavailable or Approval is forced off
- **THEN** status stays `no-evaluable` / `approvals_live=false` (never fakes live)
