# module-structure Delta — Constitution must not list retired modules as live

## ADDED Requirements

### Requirement: Constitution Impact Analysis lists only live modules

The Engineering Constitution (`CONSTITUTION.md`) MUST name only live product
modules in its §5 Impact Analysis example list. It MUST NOT present
`notaire-shared` as a live module beside `backend-api` and `frontend`. Mentions
that mark the module retired, deprecated, formerly, or that cite ADR-025 remain
allowed elsewhere in permanent docs covered by the retirement guard.

#### Scenario: Constitution does not list notaire-shared as a live module

- **WHEN** `CONSTITUTION.md` is scanned for the string `notaire-shared`
- **THEN** every matching line also carries a non-live marker (`retired`,
  `deprecated`, `formerly`, or `ADR-025`), so Impact Analysis cannot list it as
  a current module

#### Scenario: Retirement docs guard covers the Constitution

- **WHEN** `NotaireSharedRetiredTest.test_docs_do_not_present_the_module_as_live` runs
- **THEN** `CONSTITUTION.md` is included in `DOCS_AS_NON_LIVE` and a bare live
  mention fails the assertion
