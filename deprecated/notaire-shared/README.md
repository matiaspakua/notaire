# notaire-shared (retired)

> ⚠️ **DEPRECATED**: retired under #1255 (ADR-025). Nothing here is built, tested,
> scanned or deployed, and it is not a module of the root `pom.xml`.

`notaire-shared` was a Maven module of shared DTO classes used by the Swing client
and the backend. Swing was removed (#1046), leaving `backend-api` as the only Java
consumer, so the classes moved into `backend-api` (same packages) and the module was
retired.

- External services and clients consume the REST API (`/api/v1`) through the OpenAPI
  contract (`backend-api/openapi/openapi.yaml`, Swagger UI), never Java DTO classes.
- `pom.xml.archived` keeps the former manifest for reference; do not rename it back.
- Do not add this folder to the root `pom.xml` modules or to Dependabot.
