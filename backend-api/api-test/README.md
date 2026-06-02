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
bru run . -r --env Developmen

# one resource (a full CRUD lifecycle)
bru run conceptos -r --env Developmen

# write reports
bru run . -r --env Developmen --reporter-html results.html --reporter-junit results.xml
```

The `Developmen` environment (`environments/Developmen.yml`) sets
`base_url: http://localhost:8080`.

## Request file conventions

Each resource folder is a **self-contained lifecycle** ordered by `info.seq`:

```
01-create        POST   → captures the new id with bru.setVar(...)
02-list          GET    → asserts the created row is present
03-get-by-id     GET    → asserts the row by id
04..-filters     GET    → search / by-parent filters where the API exposes them
05-update        PUT    → asserts the change persisted
06-delete        DELETE → 200/204
07-verify-delete GET    → 404
```

Key rules learned the hard way:

- **JSON bodies must be real JSON.** Use `body.type: json` with a JSON object in
  `data` (the previous files used YAML key/value under `type: json`, which the
  server rejected with `400`).
- **Chain with variables.** `bru.setVar('x_id', body.id)` in create, then
  `{{x_id}}` in later requests — otherwise GET/PUT/DELETE hit an empty id and
  pass trivially.
- **Order with `info.seq`.** Folder execution is otherwise alphabetical, which
  would run `delete` before `create`.

## Coverage status

Fully covered, green CRUD lifecycles (create → list → get → [filters] → update →
delete → verify):

- `conceptos`, `tipo-tramite`, `estado-gestion`, `tipo-folio`,
  `tipo-documento`, `tipo-identificacion`
- `personas` (+ search), `usuarios` (+ login, +/- credentials, by-persona),
  `presupuestos` (+ by-persona, + search), `folios`

See `COVERAGE.md` for the full endpoint matrix and the backend defects this
suite uncovered and fixed.
