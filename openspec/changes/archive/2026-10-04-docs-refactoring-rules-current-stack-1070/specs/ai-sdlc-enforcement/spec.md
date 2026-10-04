<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## MODIFIED Requirements

### Requirement: Agent rule files stay valid

CI and preflight SHALL fail when an always-loaded agent rule file is empty or
references a repository path that does not exist. In addition, CI and preflight
SHALL fail when `.claude/rules/refactoring.md` describes an obsolete migration
target rather than the current stack (Spring Boot 4.1 / Java 21 / PostgreSQL 16,
package `com.licensis.notaire`, Next.js frontend, `Dto*` DTOs).

#### Scenario: Empty rule file

- **WHEN** a file under `.claude/rules/` is empty
- **THEN** the check fails and names it

#### Scenario: Dead path

- **WHEN** a rule file references `` `docs/does-not-exist/` ``
- **THEN** the check fails and names the file and the path

#### Scenario: No rule files found

- **WHEN** the check runs against a root that has no agent rule files
- **THEN** it fails instead of passing with nothing checked

#### Scenario: Obsolete package root rejected

- **WHEN** `.claude/rules/refactoring.md` contains the string `com.notaria`
- **THEN** `scripts/check-agent-rules.sh` fails and names the obsolete marker

#### Scenario: Swing-as-target markers rejected

- **WHEN** `.claude/rules/refactoring.md` contains `SwingWorker`, `JOptionPane`,
  or `standalone Swing GUI client` as current guidance
- **THEN** the check fails and names the obsolete marker

#### Scenario: Obsolete Boot Java Postgres markers rejected

- **WHEN** `.claude/rules/refactoring.md` contains `Spring Boot 3`, `Java 17`,
  `PostgreSQL 15`, or `EntityRequestDTO` as current target guidance
- **THEN** the check fails and names the obsolete marker

#### Scenario: Current stack markers accepted

- **WHEN** `.claude/rules/refactoring.md` describes Spring Boot 4.1, Java 21,
  PostgreSQL 16, `com.licensis.notaire`, Next.js, and `Dto*` naming, and
  contains none of the obsolete markers above
- **THEN** `scripts/check-agent-rules.sh` passes for that file
