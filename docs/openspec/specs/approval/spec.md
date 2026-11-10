## Purpose
Design-only ApprovalPort custody boundary (ADR-005). Not a live approval runtime.

## Requirements

### Requirement: ApprovalPort is design-only until evaluable
The system SHALL document an **ApprovalPort** custody boundary (token + channel authority;
SSH/DSSE or equivalent mint/verify; agent never self-approves) that is distinct from forge UI
clicks, request-body identity fields, dual-LLM advisory review, and Isolator OS runtime
(#2173 / ADR-004). Until a real approval runtime is configured, ApprovalPort status SHALL be
`no-evaluable`, SHALL report `approvals_live=false` and `agent_can_approve=false`, and SHALL NOT
claim that approvals are enforced. `aisdlc doctor` SHALL surface that `no-evaluable` status
without failing the profile solely for ApprovalPort being unevaluable.

#### Scenario: ApprovalPort stub is no-evaluable and never live
- **WHEN** no approval runtime is configured
- **THEN** ApprovalPort status is `no-evaluable`, `approvals_live` is false, and `agent_can_approve` is false

#### Scenario: Doctor reports ApprovalPort no-evaluable without failing for it
- **WHEN** `aisdlc doctor` runs on a valid profile without an ApprovalPort runtime
- **THEN** output mentions ApprovalPort `no-evaluable` and exit code is not 1 solely because ApprovalPort is unevaluable

#### Scenario: Agent self-approval remains forbidden in design
- **WHEN** documenting ApprovalPort
- **THEN** the ADR states agents never self-approve and cites improve-validate / axiom refusal as the policy surface ApprovalPort must eventually enforce
