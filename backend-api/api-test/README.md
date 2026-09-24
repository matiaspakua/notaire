# Notaire Backend — Bruno API Test Suite (YAML / OpenCollection)

End-to-end API tests for the Notaire backend, written in Bruno's **YAML
(OpenCollection)** format and run with the Bruno CLI.

## Why YAML / how the CLI selects the format

Bruno picks the collection format by file presence (`@usebruno/cli`):

| File at collection root | Format | Files discovered |
|-------------------------|--------|------------------|
| `opencollection.yml`    | **yml** | `*.yml` + `folder.yml` |
| `bruno.json`            | bru     | `*.bru` + `folder.bru` |

`opencollection.yml` is checked **first**, so this collection runs the `.yml`
requests. (Without it, the CLI silently runs `.bru` and ignores every `.yml` —
which is why a yml-only run reported `Requests: 0` before this file existed.)

## Running

Prerequisites: the backend must be up at `http://localhost:8080`
(`bash scripts/start.sh` from the repo root).

```bash
cd backend-api/api-test

# whole suite
bru run . -r --env Development

# one resource (a full CRUD lifecycle); 00-auth first to get a token
bru run 00-auth concepts --env Development

# write reports
bru run . -r --env Development --reporter-html results.html --reporter-junit results.xml
```

The `Development` environment (`environments/Development.yml`) sets
`base_url: http://localhost:8080`; `00-auth/01-login.yml` stores `token`.
Every other id is a runtime variable set by the suite itself.

## Request file conventions

Folder and file names are English and follow the backend's domain class names
(`ConceptController` → `concepts/`, `ManagementStatusController` →
`management-statuses/`). URL paths, JSON keys and domain codes (`RECARGO`,
`EMPLEADO`, `Pendiente`, ...) stay exactly as the API exposes them.

Each resource folder is a **self-contained lifecycle** ordered by `info.seq`:

```text
00-create-<fixture>  POST   → setup: parent rows the resource needs (optional)
01-create            POST   → captures the new id with bru.setVar(...)
02-list              GET    → asserts the created row is present
03-get-by-id         GET    → asserts the row by id
04..-filters         GET    → search / by-parent filters where the API exposes them
0x-update            PUT    → then 0x-verify-update asserts the change persisted
0x-delete            DELETE → then 0x-verify-delete expects 404
NN-teardown-delete-* DELETE → removes the setup fixtures, child → parent
```

Rules:

- **Idempotent.** The suite must pass when run any number of times against the
  same database. Unique values (document numbers, usernames, names, numbers)
  come from `Date.now()` in a `before-request` script of the first request, and
  every folder deletes everything it creates. Seeded rows (person 1, procedure
  type 1, folio type 1, identification type 1, management statuses 1/2) are
  referenced, never modified or deleted.
- **Headers on every request.** The CLI does not apply the collection-level
  headers from `opencollection.yml`, so each request declares `Content-Type`
  and `Authorization: Bearer {{token}}` itself.
- **JSON bodies must be real JSON** (`body.type: json` with a JSON object).
- **Chain with namespaced variables** (`budget_id`, `items_budget_id`,
  `payments_budget_id`, ...) so folders never read each other's ids.
- **Order with `info.seq`.** Folders run alphabetically (hence `00-auth`);
  requests inside a folder run by `seq`.
- **Traceability.** Every request description ends with
  `Traceability: CUxx (RF #n, ...)`, from
  `docs/100-business/104-traceability/`.

## Coverage status

21 folders, 164 requests, 291 tests — see `COVERAGE.md` for the resource
table and the backend defects this suite uncovered and fixed.
