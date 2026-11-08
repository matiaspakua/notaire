## ADDED Requirements

### Requirement: Audit R2 ports are entity-free
The audit application slice MUST NOT depend on `business.*` entities or Spring Data repositories via entity-speaking ports. Ops adapters own entity-bound helpers locally. Controllers call view-safe ops ports only.

#### Scenario: R2 baseline shrinks for audit
- **WHEN** HexagonArchitectureTest runs against the updated tree
- **THEN** the ~8 audit R2 baseline edges for AuditRecordRepositoryPort and AuditRecordService are gone

#### Scenario: Live OpsPort remains view-safe
- **WHEN** AuditRecordController lists or searches audit records
- **THEN** it uses AuditRecordOpsPort (DTO/PageResult only); AuditRecordService lives under adapter.out.persistence.audit
