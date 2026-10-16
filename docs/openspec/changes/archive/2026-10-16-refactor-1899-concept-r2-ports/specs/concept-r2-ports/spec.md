## ADDED Requirements

### Requirement: Concept application ports stay entity-free

The concept application slice MUST NOT depend on JPA entities or Spring Data repositories via entity-speaking ports. Entity orchestration lives in outbound persistence adapters behind view-safe ops ports.

#### Scenario: Entity helper lives in outbound adapter
- **GIVEN** ConceptService needs Concept entities
- **WHEN** the R2 shrink lands
- **THEN** the helper is under adapter.out.persistence.concept and injects Spring Data locally

#### Scenario: Entity RepositoryPort removed
- **GIVEN** ConceptRepositoryPort exposed Concept entities
- **WHEN** the R2 shrink lands
- **THEN** that port and its unused PersistenceAdapter are deleted

#### Scenario: OpsPort remains view-safe
- **WHEN** ConceptController queries or mutates concepts
- **THEN** it uses inbound ports backed by ConceptOpsPort (DTO views only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** two R2 baseline edges for concept
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear