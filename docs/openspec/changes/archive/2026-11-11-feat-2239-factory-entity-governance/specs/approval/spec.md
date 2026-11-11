## ADDED Requirements

### Requirement: Workflow approve consumes ApprovalPort honesty
Workflow governance `approve` SHALL call ApprovalPort verify (or an equivalent
mint→verify subject check) when approvals are live. When `approvals_live` is
false, approve SHALL fail closed unless an explicit documented Owner override is
supplied. Agents MUST NOT approve workflows.

#### Scenario: live verify required for approve
- **WHEN** approvals are live and a valid signature is provided for the workflow subject
- **THEN** approve transitions validated → approved

#### Scenario: no cosmetic approve
- **WHEN** approvals are not live and override is omitted
- **THEN** approve fails and governance remains validated
