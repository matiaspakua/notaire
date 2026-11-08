## ADDED Requirements

### Requirement: Copy application ports stay entity-free

The copy application slice MUST NOT depend on JPA entities or Spring Data repositories via entity-speaking ports. Entity orchestration lives in outbound persistence adapters behind view-safe ops ports.

#### Scenario: Entity helper lives in outbound adapter
- **GIVEN** CopyService needs Copy entities and TestimonyMovementRepository
- **WHEN** the R2 shrink lands
- **THEN** the helper is under adapter.out.persistence.copy and injects Spring Data locally

#### Scenario: Entity RepositoryPort removed
- **GIVEN** CopyRepositoryPort exposed Copy and Testimony entities
- **WHEN** the R2 shrink lands
- **THEN** that port and its unused PersistenceAdapter are deleted

#### Scenario: OpsPort remains view-safe
- **WHEN** CopyController queries or mutates copies
- **THEN** it uses inbound ports backed by CopyOpsPort (CopyView only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** four R2 baseline edges for copy
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear