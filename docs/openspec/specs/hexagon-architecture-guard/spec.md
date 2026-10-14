# hexagon-architecture-guard Specification

## Purpose

TBD - created by archiving change test-1695-archunit-hexagon-rules. Update Purpose after archive.

## Requirements

### Requirement: Hexagon dependency rules are enforced with a shrink-only baseline

The build MUST fail when production code adds a dependency forbidden by the hexagon rules, and MUST fail when the baseline lists a dependency that no longer exists.

#### Scenario: new violation fails

- **WHEN** a class in adapter.in depends on a repository class that is not in the baseline
- **THEN** the guard fails and names the dependency

#### Scenario: stale baseline entry fails

- **WHEN** the baseline lists a dependency that production code no longer has
- **THEN** the guard fails and asks for the entry to be removed

#### Scenario: current state passes

- **WHEN** the guard runs on the code at the time the baseline was written
- **THEN** it passes

### Requirement: Application framework use is an explicit allow-list

Classes in the application layer MUST NOT depend on Spring except Service and Transactional, nor on JPA, Hibernate or Jackson.

#### Scenario: allow-list

- **WHEN** an application class uses a Spring type other than Service or Transactional
- **THEN** it is reported as a violation
