<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion and MUST map to a test in traceability.md. -->

## Purpose

Keep `docs/300-development/303-testing/CU-API-MATRIX.csv` synchronized with the
English REST controllers under `adapter.in.web`, required resource base paths,
and Bruno_Test column conventions, enforced by a CI/preflight validator.

## ADDED Requirements

### Requirement: Matrix Controller names MUST match live English REST controllers

The `Controller` column of `CU-API-MATRIX.csv` MUST use the current Java class
names of `@RestController` types under
`backend-api/src/main/java/com/licensis/notaire/adapter/in/web/`, or the sentinel
`N/A` for non-API rows. Stale Spanish controller names MUST fail validation.

#### Scenario: Stale Spanish controller name is rejected

- **WHEN** the matrix lists a `Controller` value that is not an existing
  `adapter.in.web` REST controller class and is not `N/A`
- **THEN** `scripts/validate-cu-api-matrix.py` exits non-zero and reports the
  stale name

#### Scenario: Current English controller names are accepted

- **WHEN** every non-`N/A` `Controller` value matches a live
  `adapter.in.web` REST controller class name
- **THEN** the controller-name check passes

### Requirement: Required resource base paths MUST appear in the matrix

The matrix Endpoint column MUST reference each of these live resource bases at
least once: `/carpetas`, `/cuadernos`, `/minutas-inscripcion`,
`/plantilla-costos-documento`, `/protocolo-auxiliar`, `/roles`,
`/tipo-identificacion`, `/tramites`.

#### Scenario: Missing required resource base fails validation

- **WHEN** one of the required resource base paths is absent from every
  `Endpoint` value
- **THEN** the validator exits non-zero naming the missing base

#### Scenario: All required resource bases present passes that check

- **WHEN** each required resource base appears in at least one `Endpoint`
- **THEN** the required-resource check passes

### Requirement: Bruno_Test column MUST use paths or allowed sentinels only

`Bruno_Test` MUST be exactly `N/A`, exactly `MISSING`, a relative Bruno folder
root ending in `/`, or a relative `.yml` path under `backend-api/api-test/`.
Status words such as `DONE` or `OK` MUST NOT appear in `Bruno_Test`.

#### Scenario: Status word in Bruno_Test is rejected

- **WHEN** a row has `Bruno_Test` equal to `DONE` or `OK`
- **THEN** the validator exits non-zero

#### Scenario: Path and sentinel values are accepted

- **WHEN** `Bruno_Test` is `N/A`, `MISSING`, a folder root, or an existing
  relative `.yml` / folder path convention used by the suite
- **THEN** the Bruno_Test format check passes for that row

### Requirement: MISSING Bruno rows MUST link issue #953

Every row with `Bruno_Test=MISSING` MUST mention `#953` in `Notas` and/or
`GitHub_Issue` so Bruno gap work stays tracked under #953.

#### Scenario: MISSING without #953 is rejected

- **WHEN** `Bruno_Test` is `MISSING` and neither `Notas` nor `GitHub_Issue`
  contains `#953`
- **THEN** the validator exits non-zero

### Requirement: Validator MUST run in preflight and CI process checks

`scripts/preflight.sh` MUST invoke the matrix validator as a blocking check, and
`scripts/tests/` MUST cover red/green behavior so `sdlc-process` process-script
self-tests exercise it.

#### Scenario: Preflight list includes the matrix validator

- **WHEN** `bash scripts/preflight.sh --list` is inspected
- **THEN** it maps a CU-API matrix validation check to a CI process job

#### Scenario: Unit tests prove stale matrix fails and refreshed matrix passes

- **WHEN** `python3 -m unittest scripts.tests.test_validate_cu_api_matrix` (or
  discover under `scripts/tests`) runs
- **THEN** fixtures prove a stale Spanish matrix fails and a refreshed matrix
  matching live controllers passes
