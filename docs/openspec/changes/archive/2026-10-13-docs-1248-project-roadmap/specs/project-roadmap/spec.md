## Purpose

Classifies each Notaire issue onto one roadmap milestone and one parallel-agent
lane so managers can read progress and agents do not claim the same surface.

## ADDED Requirements

### Requirement: Closed issues keep an assigned milestone
The classifier SHALL leave a closed issue on the milestone it already has.

#### Scenario: Assigned history is preserved
- **WHEN** a closed issue already has milestone "Phase 0 - Foundations"
- **THEN** the classifier keeps that milestone and marks the issue not claimable

### Requirement: Unassigned closed issues join a historical cohort
The classifier SHALL assign a closed issue with no milestone from its phase label, or else from its close date.

#### Scenario: September close date
- **WHEN** a closed issue has no milestone and was closed in September 2026
- **THEN** the milestone is "Phase 5 — Observability & production audit"

#### Scenario: July or August close date
- **WHEN** a closed issue has no milestone and was closed in July or August 2026
- **THEN** the milestone is "Phase 4 — Frontend deliveries (history)" and not the open Phase 4 train

### Requirement: Catalog issues are scope, not delivery
The classifier SHALL place RF, CU, and RNF catalog issues on scope milestones and SHALL mark them not claimable. Each RNF SHALL record the delivery train it informs.

#### Scenario: Functional requirement
- **WHEN** an open issue is titled "RF-01: Gestionar Trámites"
- **THEN** the milestone is "Scope — Functional requirements" and the issue is not claimable

#### Scenario: Security non-functional requirement
- **WHEN** an open issue is titled "RNF-12: Seguridad y privacidad"
- **THEN** the milestone is "Scope — Non-functional requirements" and the informed train is "v1.0 — Production Ready"

### Requirement: Open product work has one delivery train and one lane
The classifier SHALL assign open non-catalog work to exactly one delivery milestone and exactly one lane label from the defined set.

#### Scenario: Frontend slice
- **WHEN** an open issue has the FRONTEND label, no BACKEND label, and is not a security finding
- **THEN** the milestone is "Phase 4 — Modern Frontend" and the lane is "lane:frontend"

#### Scenario: Audit finding
- **WHEN** an open issue has the audit-2026-09 label and priority high
- **THEN** the milestone is "v1.0 — Production Ready" and the lane is "lane:security"

#### Scenario: Critical jpa blocker stays on the production gate
- **WHEN** an open issue is priority critical and describes controllers instantiating JPA
- **THEN** the milestone is "v1.0 — Production Ready"
