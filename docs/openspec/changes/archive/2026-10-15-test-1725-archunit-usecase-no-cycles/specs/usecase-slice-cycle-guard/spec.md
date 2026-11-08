## ADDED Requirements

### Requirement: Use-case slices are free of dependency cycles except a shrink-only baseline

The build MUST fail when production code introduces a new dependency cycle between `application.usecase.<slice>` packages, and MUST fail when the cycle baseline lists a cycle that no longer exists.

#### Scenario: new cycle fails

- **WHEN** a use-case slice gains a dependency path that closes a cycle with another slice and that cycle is not in the baseline
- **THEN** the guard fails and names the cycle in canonical form

#### Scenario: stale baseline cycle fails

- **WHEN** the baseline lists a cycle that production code no longer has
- **THEN** the guard fails and asks for the entry to be removed via the shrink workflow

#### Scenario: current state passes

- **WHEN** the guard runs on the code at the time the baseline was written
- **THEN** it passes

### Requirement: Cycle baseline uses slice rings, not class pairs

The cycle baseline MUST identify cycles by ordered use-case slice names (canonical rings), not by class-to-class pairs, because ArchUnit cycle members are not a stable class-pair list.

#### Scenario: baseline line is a canonical slice ring

- **WHEN** a cycle is recorded in the baseline
- **THEN** the line is a closed ring of slice package names in a deterministic order (lexicographically smallest start, then the ring direction)
