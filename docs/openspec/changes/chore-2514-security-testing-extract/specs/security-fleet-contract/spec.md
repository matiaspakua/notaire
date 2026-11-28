<!-- Governed by CONSTITUTION.md. CU76, issue #2514. -->

## Purpose

Notaire obtains security gates, policy and cybersecurity program documentation from
an independent fleet repository and keeps code-tied threat models in the product.

## ADDED Requirements

### Requirement: Security path is a git submodule of the fleet repository

Notaire MUST record `security/` as a git submodule whose URL is
`https://github.com/matiaspakua/notaire-security.git`, and MUST NOT vendor a
second copy of that tree in the monorepo.

#### Scenario: gitmodules points at notaire-security

- **WHEN** a developer inspects `.gitmodules`
- **THEN** an entry with `path = security` names the `matiaspakua/notaire-security` repository

#### Scenario: module verify still runs from the security path

- **WHEN** the submodule is initialized and `bash security/verify.sh` runs
- **THEN** the security module gate executes from that path

### Requirement: Program docs live in the security fleet; threat models stay in product

Notaire MUST keep STRIDE/threat-model documents and diagrams under
`docs/200-architecture/206-security/`, and MUST NOT keep the cybersecurity program
prose (`CYBERSECURITY-PROGRAM.md`, assessment, tools inventory, monitoring,
HTTPS-TLS runbook, installation-isolation checklist) as the normative copy in
the product — those MUST live in `matiaspakua/notaire-security` (product may keep
stubs that link to the fleet).

#### Scenario: threat-model docs remain in product

- **WHEN** a developer lists `docs/200-architecture/206-security/`
- **THEN** `THREAT-MODEL-SYSTEM.md`, `THREAT-MODEL-AUTH-AUDIT.md`, and the
  `threat-model/` / `diagrams/` trees are present

#### Scenario: program docs moved or stubbed to security repo

- **WHEN** a developer looks for `CYBERSECURITY-PROGRAM.md` under product `206-security/`
- **THEN** either the file is absent or it is a stub that points to
  `matiaspakua/notaire-security`, and the normative program copy is not claimed
  as product-owned
