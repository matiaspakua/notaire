# Design — CU-API-MATRIX refresh + CI validator

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules.

## Context

The English rename moved REST controllers to
`com.licensis.notaire.adapter.in.web.*` with English class names, but
`CU-API-MATRIX.csv` still lists 22 Spanish `*Controller` names. Eight resource
bases exist in code (and have Bruno folders from #953) yet are absent from the
matrix; CU80–CU82 still say `NOT-IMPLEMENTED`. There is no script rejecting
that drift in CI.

## Goals / Non-Goals

**Goals:**

- Refresh Controller names and add the eight missing resources (CU80–CU82,
  CU85, plus inventory rows for roles / identification types / document cost
  templates / procedures).
- Normalize `Bruno_Test`; cite `#953` on every `MISSING` row without inventing
  new Bruno requests.
- Ship `scripts/validate-cu-api-matrix.py` with unittest red→green proof;
  wire into preflight and process self-tests.

**Non-Goals:**

- Bruno request authoring or suite expansion (#953).
- Changing Spanish URL paths still returned by the API.
- Editing product Java/TS runtime behavior.
- Using or depending on `local-ai/`.

## Decisions

- **Python validator** (not bash-only): matches `scripts/tests` unittest style
  and makes CSV/`Path` parsing straightforward; entrypoint
  `scripts/validate-cu-api-matrix.py` executable via `python3`.
- **Discover controllers from source**: scan `adapter/in/web/**/*Controller.java`
  for `@RestController` + class name + class-level `@RequestMapping`; do not
  hard-code the English rename map in the validator (the map is applied once
  when refreshing the CSV).
- **Required bases allowlist**: the eight paths from #1064 are asserted
  explicitly so a future delete of a row fails CI even if another controller
  remains listed.
- **Bruno_Test rules**: allow `N/A`, `MISSING`, `folder/`, and `folder/file.yml`;
  reject status tokens; require `#953` on `MISSING` (gap ownership), not on
  rows that already point at Bruno folders from #953.
- **TDD**: commit/tests first against a stale fixture copied from today's CSV
  shape; then refresh the real CSV until the validator is green on the repo
  file.

## Riesgos / Trade-offs

- [False fail if a controller is intentionally omitted] → Only `adapter.in.web`
  REST controllers are required; `N/A` rows remain for transversal CUs;
  `BusinessController` outside that package is ignored.
- [Bruno folders exist but matrix still says MISSING for some deed ops] → Keep
  those as `MISSING` + `#953` rather than inventing request files here.
- [CSV quoting / commas in Notas] → Use Python `csv` module, not naive splits.

## Testing Strategy

| Scenario | Level | Proof |
|----------|-------|-------|
| Stale Spanish controller rejected | unit | `test_validate_cu_api_matrix.py` fixture |
| English controllers accepted | unit | refreshed fixture / repo CSV |
| Missing required resource fails | unit | fixture omitting one base |
| Required resources present | unit + repo | validator on real CSV |
| Bruno_Test status word rejected | unit | fixture with `DONE` |
| Path/sentinel accepted | unit | fixture |
| MISSING without #953 rejected | unit | fixture |
| Preflight list includes check | unit/grep | assert `--list` text |
| Repo matrix green after refresh | unit + script | run validator at repo root |

## Regression Strategy

- Process-script unittest discover remains green.
- No Java/TS product surface; no JaCoCo impact.
- Confirm `validate-sdlc-plan.sh docs-1064-cu-api-matrix-refresh` passes.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Nothing deployed. Merge updates docs/scripts; next CI/preflight run enforces the
gate. Feature flag: no.

## Rollback Strategy

`git revert` of the merge commit. Revert safe: yes (docs + optional CI check).

## Migration Plan

1. Add failing unittest + validator skeleton.
2. Refresh CSV (rename map, eight resources, Bruno_Test/#953).
3. Make validator pass on repo CSV.
4. Wire preflight + docs/CHANGELOG.
5. OpenSpec validate + validate-sdlc-plan.

## Open Questions

None — decisions supplied by #1064 acceptance criteria and dispatch brief.
