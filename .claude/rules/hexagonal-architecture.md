# Hexagonal Architecture (Ports & Adapters) — mandatory

Notaire is a full Ports & Adapters system in backend **and** frontend (ADR-030, epic #1693, top priority).
Load this before touching any code under `backend-api/src/main` or `frontend/src`. Details: ADR-030, SAD §5.2 and
§5.3, and the worked examples in `.claude/skills/hexagonal-arch/references/notaire.md`.

## Backend dependency rules

```text
adapter.in.*  ->  application (port.in)  ->  domain
adapter.out.* ->  application (port.out)  ->  domain
```

- Controllers call **inbound ports only**: no repository, `jpa`, entity, `EntityManager` or `@Transactional`.
- Use cases (`application.usecase`) are `@Service` + `@Transactional` and nothing else from Spring; no JPA,
  Hibernate or Jackson; they reach other contexts only through inbound ports.
- Use-case packages under `application.usecase.<slice>` must not form dependency cycles; ArchUnit R7
  (`UseCaseSliceCycleArchitectureTest`, #1725) enforces this with a shrink-only ring baseline.
- Ports speak domain types: never an entity, a Spring Data type or a web DTO.
- The domain is plain Java. JPA entities (`FooJpaEntity`), Spring Data repositories and mappers live in
  `adapter.out.persistence.<context>`.
- Domain exceptions carry no HTTP status; the global handler maps them.
- `jpa`, `service`, `repository`, `business` and `dto` are legacy: no new dependents.

## Frontend dependency rules

- `app/`, `components/`, `hooks/` never import `lib/api-client` or call `fetch`; they call use cases.
- `core/**` never imports React, Next, `adapters/**` or `lib/**`.
- `adapters/http/<context>` implements the ports with `lib/api-client`; `composition/` wires them.

## Doing a slice (one bounded context per PR, REST contract unchanged)

1. Issue + OpenSpec change (CONSTITUTION §5); claim the lane; push the branch at creation.
2. **Characterization tests first** of the current REST behaviour (status codes, success bodies); they must pass
   on the old code and stay green. Include delete and update paths: cascades hide bugs (found in #574).
3. Inbound ports and command/result records; outbound ports in domain terms; use cases with unit tests that need
   no Spring or database.
4. Domain model in `domain.<context>`; JPA entity, Spring Data repository, mapper and adapter in
   `adapter.out.persistence.<context>`; mapper unit tests.
5. Controller reduced to validation, port call and web DTO mapping.
6. Delete the legacy classes of the context. Run
   `mvn test -pl backend-api -Dtest=HexagonArchitectureTest,UseCaseSliceCycleArchitectureTest -Darchitecture.baseline=shrink` and commit the
   smaller baselines. Frontend: the same with the lint baseline (#1696).
7. `mvn verify -pl backend-api` (JaCoCo gate), OpenAPI export unchanged, Playwright for any UI change.

## R2 shrink slices (ArchUnit application-layer ratchet, epic #1693)

- **One domain per PR** (one `application.usecase.<slice>` / port family). Hold the next domain until merge.
- Ports and use cases: **domain views only** — no JPA entity, no Spring Data `Page`/`Pageable`, no `business.*` on
  port signatures; map in `adapter.out.persistence.<slice>`.
- Entity-bound **helpers / services** move under `adapter.out.persistence.<slice>` (not the application layer).
- After a package move: update english-locals / `docs/tests` path refs, numbered docs, and regen `INVENTORY.md`
  before Process Checks (see `FLEET-LESSONS-HEX-1693.md` — R2 path hygiene / #1894).
- Commit the **shrunk** R2 baseline in the same PR; never raise counts by hand.
- FE hex suppressions ratchet is **Done** only when `frontend/eslint-suppressions.json` is `{}` (**count 0**).
- Fleet cadence: `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md`.

## Never

- Add a line to a baseline by hand, or `@Suppress` a rule: fix the dependency instead.
- Keep a legacy class "for now" after its callers are gone.
- Put a business rule in an adapter, a mapper or a hook.
- Return a JPA entity, or accept one, in a controller or a port.
- Mix several R2 domains in one PR, or start the next domain before the current PR merges.
