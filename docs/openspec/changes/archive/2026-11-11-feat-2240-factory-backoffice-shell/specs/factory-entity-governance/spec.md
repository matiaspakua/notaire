## MODIFIED Requirements

### Requirement: Workflow governance state machine
A workflow SHALL carry governance state in
`draft | defined | validated | approved`. Legal transitions are `define`
(draft→defined), `request_validation` (defined→validated), `approve`
(validated→approved), and documented demote/reopen pairs. Illegal transitions
SHALL fail closed. List/show/history MUST NOT execute workflow steps.
**Phase B:** any HTML/serve adapter SHALL expose only legal transitions for the
current state and MUST fail closed on illegal approve attempts (same guards as CLI).

#### Scenario: happy path to approved
- **WHEN** a workflow is created and transitions define → request_validation → approve with honest approval
- **THEN** its governance state is `approved`

#### Scenario: illegal transition
- **WHEN** a draft workflow attempts `approve`
- **THEN** the command fails and state remains `draft`

#### Scenario: UI respects illegal transition
- **WHEN** backoffice attempts `approve` on a draft workflow
- **THEN** the adapter fails and governance remains `draft`
