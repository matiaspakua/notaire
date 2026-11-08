<!-- Governed by CONSTITUTION.md. CU78, issue #1637. -->

## ADDED Requirements

### Requirement: The tenancy model is documented consistently

The ADR, the SAD and the security register MUST describe one installation per notary and MUST NOT describe an assigned-notary ownership check as planned.

#### Scenario: ADR records the decision

- **WHEN** ADR-029 is read
- **THEN** its status is Accepted and it states single tenant per notary with insert-if-absent role seeding

#### Scenario: register no longer lists SR-11 as an open object-level gap

- **WHEN** the SR-11 row of THREAT-MODEL-SYSTEM.md is read
- **THEN** it records cross-tenant as mitigated by architecture and intra-tenant as accepted

#### Scenario: SAD has a tenancy section

- **WHEN** the SAD deployment view is read
- **THEN** section 7.5 describes per-installation data and the standard role template
