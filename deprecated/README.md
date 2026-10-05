# Deprecated

Historical reference only. Nothing here is built, tested, scanned, or deployed.

| Folder | What it was | Removed in |
|--------|-------------|------------|
| `frontend-swing/` | Legacy Java Swing GUI client (was `deprecated-frontend-swing/`) | `dbc15d30` (#1046) |
| `src.old/` | Pre-migration monolith sources (was `deprecated-src.old/`) | moved here from the repo root |
| `notaire-shared/` | Shared DTO Maven module; its classes now live in `backend-api` (ADR-024) | #1255 |

- `frontend-swing/pom.xml` and `notaire-shared/pom.xml` are stored as `pom.xml.archived` so it is not a live
  manifest (it declares vulnerable `log4j`; see `scripts/test_dependabot_hygiene.py`).
- Do not add this folder to the root `pom.xml` modules or to Dependabot.
- New client work belongs in `frontend/` (Next.js).
