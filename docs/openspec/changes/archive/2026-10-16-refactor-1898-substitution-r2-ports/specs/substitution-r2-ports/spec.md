## ADDED Requirements

### Requirement: Substitution application ports stay entity-free

The substitution application slice MUST NOT depend on JPA entities or Spring Data repositories via entity-speaking ports. Entity orchestration lives in outbound persistence adapters behind view-safe ops ports.

#### Scenario: Entity helper lives in outbound adapter
- **GIVEN** SubstitutionService needs Substitution and Person entities
- **WHEN** the R2 shrink lands
- **THEN** the helper is under adapter.out.persistence.substitution and injects Spring Data locally

#### Scenario: Entity RepositoryPort removed
- **GIVEN** SubstitutionRepositoryPort exposed Substitution and Person entities
- **WHEN** the R2 shrink lands
- **THEN** that port and its unused PersistenceAdapter are deleted

#### Scenario: OpsPort remains view-safe
- **WHEN** SubstitutionController queries or mutates substitutions
- **THEN** it uses inbound ports backed by SubstitutionOpsPort (SubstitutionView only)

#### Scenario: ArchUnit R2 baseline shrinks
- **GIVEN** four R2 baseline edges for substitution
- **WHEN** HexagonArchitectureTest runs after the change
- **THEN** those edges are gone and no new R2 violations appear