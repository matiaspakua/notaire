<!-- Governed by CONSTITUTION.md §2 and §12 (a Constitution amendment is its own reviewed PR). CU76, issue #1591. -->

## Purpose

Engineering principles use the standard KISS spelling and a bounded Boy Scout
principle, stated identically in the Constitution and in every digest that
agents load, without growing the always-loaded context past its budget.

## ADDED Requirements

### Requirement: KISS is the only spelling of the simplicity principle

The Constitution principle P2 SHALL read "KISS — Keep It Simple". The
Constitution, the agent card, the agent entry point, the operational workflow
rule, the AI SDLC documents and the process documents SHALL NOT use the bare
abbreviation KIS for that principle. Archived material and historical change
records are exempt.

#### Scenario: Constitution P2 is KISS

- **WHEN** the principles table of the Constitution is read
- **THEN** P2 is named "KISS — Keep It Simple" and no principle is named with the bare abbreviation

#### Scenario: Governed files carry no old spelling

- **WHEN** the governed files are scanned for the word KIS
- **THEN** none is found outside the archive and change-record exemptions

#### Scenario: Exemptions are explicit

- **WHEN** the scan configuration is read
- **THEN** it exempts only archived documents, deprecated sources and OpenSpec change and capability records, and lists them

### Requirement: The Boy Scout principle is bounded

The Constitution SHALL add principle P11, "Boy Scout — leave it cleaner (bounded)":
clean up only the files and functions the change already modifies, within the
same pull request, with the existing tests green and no behavior change; any
larger cleanup, architectural move, or cross-module change SHALL get its own
Issue; P10 (adapt, do not replace) prevails.

#### Scenario: P11 states the bounds

- **WHEN** P11 is read
- **THEN** it names the touched-files limit, the same-PR limit, the no-behavior-change limit, the separate-Issue rule and the P10 precedence

#### Scenario: Principle numbers are stable

- **WHEN** the principles table is read
- **THEN** P1 to P10 keep their numbers and P11 is appended, so existing citations such as P4 and P7 stay valid

### Requirement: Digests carry the same principles under the context budget

`CONSTITUTION-AGENT-CARD.md`, `AGENTS.md` and `.claude/rules/ai-agent-workflow.md`
SHALL each state KISS and the bounded Boy Scout principle in one short line that
cites the Constitution. The always-loaded set SHALL remain at or below 8,000
estimated tokens, and the agent card SHALL remain at or below 3 KB.

#### Scenario: Digests mention both principles

- **WHEN** the three digests are read
- **THEN** each contains "KISS" and "Boy Scout" and points to the Constitution section

#### Scenario: Context budget holds

- **WHEN** the always-loaded context budget is measured
- **THEN** the estimate is at most 8,000 tokens and the agent card is at most 3,072 bytes

#### Scenario: Digest does not copy the Constitution

- **WHEN** a digest line for the principles is read
- **THEN** it is a single line and cites the Constitution instead of reproducing the P11 bounds in full
