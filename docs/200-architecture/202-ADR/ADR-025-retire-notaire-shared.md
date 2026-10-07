# ADR-025: Retire the notaire-shared Module

**Status:** Accepted  
**Date:** 2026-10-05  
**Deciders:** Engineering (issue #1255 / CU76)  
**Related:** ADR-002 (superseded in part), ADR-005, ADR-017, ADR-020  
**Supersedes:** the three-module structure of ADR-002  

## Context

ADR-002 split the system into `backend-api`, `frontend-swing` and `notaire-shared`
so the Swing client and the backend could share DTO classes. The Swing client was
removed (#1046) and `frontend/` (Next.js) consumes the REST API as JSON, so
`backend-api` became the only Java consumer of `notaire-shared`.

The module then carried no second consumer but still cost a Maven module, a Docker
`COPY` layer, a `-am` build flag, a CODEOWNERS and release-please entry, and a
stale-jar risk in `~/.m2` (a `-pl backend-api` build without `-am` compiled against
an old jar). It also held a JPA-layer type (`jpa.exceptions`, #581) and metrics that
observed the module itself and were never recorded.

## Decision

1. **`backend-api` owns its DTOs.** All 56 classes moved with `git mv`, keeping their
   packages (`com.licensis.notaire.dto`, `dto.exceptions`, `dto.interfaces`,
   `jpa.exceptions`), so no import changed and the JSON contract is identical
   (verified: the exported OpenAPI document is unchanged).
2. **The reactor has one module.** The root `pom.xml` stays the parent and lists only
   `backend-api`; the Docker build, CODEOWNERS, release-please and the tooling stop
   naming the retired module.
3. **The folder is removed from the tree.** It was first archived in `deprecated/notaire-shared/`;
   #1261 then deleted `deprecated/` (Owner decision, 2026-10-07). History keeps it behind the git
   tag `archive-monorepo-pre-split`. A guard (`workspace/tests/test_notaire_shared_retired.py`)
   fails if it or `deprecated/` reappears.
4. **External services consume the REST API.** Any non-Java or separate service uses
   `/api/v1` through the OpenAPI contract (`backend-api/openapi/openapi.yaml`,
   Swagger UI), never a shared Java DTO library.
5. `SharedModuleMetrics` and the `notaire_shared_version` gauge are removed; nothing
   recorded into them and no dashboard read them.

## Consequences

- Positive: one module, simpler builds, no stale-jar ambiguity, no JPA-layer leak.
- Negative: a future separate Java service cannot reuse the DTOs as a library. It
  should generate a client from OpenAPI; revisit if the multi-repo split (#1197)
  needs a published contracts artifact.
- Neutral: `-pl backend-api -am` keeps working because the root POM is still the parent.
- Follow-up: CONSTITUTION §5 step 4 still lists `notaire-shared` as an example module;
  changing it needs its own PR (§12).

## Alternatives considered

- **Keep the module.** Rejected: no consumer needs it.
- **Flatten `backend-api` into the root project.** Rejected: larger, unrelated move.
- **Publish a contracts artifact now.** Rejected: speculative (YAGNI) until #1197 decides.
