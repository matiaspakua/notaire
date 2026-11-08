# testimony-r2-ports Specification

## Purpose
TBD - created by archiving change refactor-1879-testimony-r2-ports. Update Purpose after archive.

## Requirements

### Requirement: Testimony R2 ports are entity-free
The testimony application slice MUST NOT depend on `business.*` entities or Spring Data repositories via entity-speaking ports. Ops adapters own entity-bound helpers locally. Controllers call inbound/ops ports only.

#### Scenario: R2 baseline shrinks for testimony
- **WHEN** HexagonArchitectureTest runs against the updated tree
- **THEN** the ~12 testimony R2 baseline edges for Testimony*UseCase, Testimony*RepositoryPort, TestimonyGenerationVerificationService, and TestimonyMovementService are gone

#### Scenario: Live OpsPorts remain DTO-safe
- **WHEN** TestimonyController / TestimonyMovementController call generate/verify/register/withdraw/reenter
- **THEN** they use TestimonyOpsPort / TestimonyMovementOpsPort (DTO only); helpers live under adapter.out.persistence.testimony
