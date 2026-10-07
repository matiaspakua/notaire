<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Track intentional Playwright feature-gap skips in TS-0014, TS-0016, TS-0017, and
TS-0020 so CU76 QA infrastructure keeps an honest inventory (#1146): every static
`test.skip` cites an issue, the permanent mapping matches live suite source, and
product ownership stays on domain Use Cases.

## ADDED Requirements

### Requirement: Feature-gap skips cite a tracking issue

Every static `test.skip(` declaration in TS-0014, TS-0016, TS-0017, and TS-0020
MUST include a `#\d+` GitHub issue reference in the skip title (tracker #1146
unless a more specific open issue replaces it).

#### Scenario: Static skips cite an issue number

- **WHEN** a Vitest hygiene check reads those four Playwright suite sources
- **THEN** every `test.skip("…")` title matches `/#\d+/`

#### Scenario: Inventory count is stable and non-zero

- **WHEN** the hygiene check inventories static skips in those four suites
- **THEN** it finds exactly fourteen skips (2 + 3 + 2 + 7) and fails if a skip
  loses its issue citation or the suite silently drops all intentional skips
  without updating the mapping

### Requirement: Mapping inventory matches live skips

`docs/300-development/303-testing/E2E-TEST-MAPPING.md` MUST document the live
static feature-gap skip inventory for TS-0014/16/17/20, including count, skip
id, owning CU, and that product work is tracked on the owning CU (not as UI
work inside #1146).

#### Scenario: Mapping lists fourteen feature-gap skips

- **WHEN** a reader opens the Skipped Tests Justification section
- **THEN** it states that fourteen intentional feature-gap skips remain in
  TS-0014/16/17/20 and lists each skip with owning CU and #1146 as the
  citation tracker

### Requirement: No silent unskip without product UI

This change MUST NOT re-enable a skipped scenario unless the suite gains a real
UI assertion against shipped controls. CU21 edit remains out of this inventory
(already unskipped in #1057).

#### Scenario: CU21 is not counted as a feature-gap skip

- **WHEN** the inventory for #1146 is built from TS-0016
- **THEN** CU21 edit is absent from the skipped list (covered by #1057)
