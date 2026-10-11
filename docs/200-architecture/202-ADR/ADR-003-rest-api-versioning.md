# ADR-003: REST API versioning strategy

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez  
**Related:** ADR-001, ADR-002  

## Context

The project moved from a monolith to a REST API. That needs a versioning strategy so that:

- The API can evolve without breaking existing clients
- Changes can be rolled back
- More than one version can run in parallel when needed
- Breaking changes are communicated clearly

Constraints:

- Small team (no capacity to keep ten versions alive at once)
- Clients vary (the retired Swing GUI, and later web clients)
- Fast changes were needed during the refactor

## Decision

Version the API with **URL path versioning** and use **semantic versioning** for releases:

```text
/api/v1/budgets      # Version 1, stable
/api/v2/budgets      # Version 2, only if a breaking generation is needed
```

There is no `/api/v2` surface today. Current controllers use `/api/v1`.

### Version strategy

**Version 1 (v1):** current — main resources

- Budgets, people, deeds, managements
- Status: STABLE
- End of life: to be decided (at least 12 months after a v2 exists)

**Version 2 (v2):** future — a larger refactor

- Changes to response shape
- Status: not started
- Prerequisite: clients are on v1 first

### Endpoint shape

`BudgetController` is mapped at `/api/v1/budgets`. It returns the resource
directly (`BudgetResponse`, `Page<BudgetResponse>`, `DtoBudgetSummary`), not an
`ApiResponse` envelope. Errors use `ErrorResponse`.

```java
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {
    @GetMapping
    public ResponseEntity<Page<BudgetResponse>> getAll() { }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getById() { }

    @PostMapping
    public ResponseEntity<BudgetResponse> create() { }
}
```

### Semantic versioning

Semver applies to **release versions** (the code), not to the URL segment:

- `1.0.0` → first release with a stable API v1
- `1.1.0` → new endpoint on v1 (backward compatible)
- `1.1.1` → bug fix on v1
- `2.0.0` → breaking changes (needs API v2)

### Breaking-change policy

**Allowed without a version change:**

- Add an optional response field
- Add a new endpoint
- Deprecate an endpoint (`@Deprecated` plus header `Warning: 299`)
- Change a date format when parsing stays backward compatible

**Requires a version change:**

- Remove a response field
- Change the type of an existing field
- Change the meaning of a field
- Change the error structure
- Change functional behavior

## Options considered

### Option A: URL path versioning (selected)

```text
GET /api/v1/budgets
GET /api/v2/budgets
```

| Dimension | Assessment |
|-----------|------------|
| Clarity | Very high |
| SEO friendly | Yes |
| Caching | Easy |
| Documentation | Clear |
| Implementation complexity | Low |

**Pros:**

- Easy to read in logs and monitoring
- Easy to document
- Browser-friendly to explore
- Straightforward routing

**Cons:**

- Duplicated code between versions
- The version sits in the URL path
- Multiple controllers must be kept

### Option B: Accept-header versioning

```text
GET /api/budgets
Accept: application/vnd.notaire.v1+json
```

| Dimension | Assessment |
|-----------|------------|
| Clarity | Medium |
| Standards compliance | RESTful |
| Debugging | Hard |
| HTTP caching | Complex |

**Cons:**

- Less intuitive
- Hard to debug in a browser
- HTTP caching is awkward
- The client must know the header names

### Option C: Query-parameter versioning

```text
GET /api/budgets?api-version=1
```

| Dimension | Assessment |
|-----------|------------|
| Clarity | High |
| Caching | Problematic |
| REST compliance | Low |

**Cons:**

- Treats a routing concern as a query parameter
- Caching is problematic (query strings)
- Not a common REST versioning style

### Option D: No versioning (monolith)

**Cons:**

- Breaking changes force every client to update
- Rollback affects users
- Technology transitions are harder

## Trade-off analysis

**URL clarity versus code duplication**

URL path versioning is clear (better debugging and logging) at the cost of duplicated code. Parallel versions duplicate code anyway, so the duplication should be explicit.

**Standards compliance versus pragmatism**

Header versioning is closer to a strict REST style. URL versioning is more practical for a small team. Pragmatism wins here.

**Simplicity versus flexibility**

URL versioning is simple to understand and maintain. Extra endpoints for a version are a separate controller mapping.

## Consequences

### Positive

- **Clear URLs**: the path shows the version
- **Easy debugging**: logs and monitoring stay simple
- **HTTP caching**: works with ordinary caches
- **Browser exploration**: the URL is readable without special tools
- **Clear migration path**: clients can see which generation they call

### Challenges

- **Duplicated code**: v1 and v2 controllers would coexist
- **Maintenance**: bug fixes must be applied to each live version
- **Longer paths**: `/api/v1/` instead of `/api/`
- **Two doc sets**: OpenAPI for v1 and v2 separately, once v2 exists

## Implementation details

### Controller structure

```java
// v1 (stable) — this is the class that exists today
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {
    // v1 endpoints
}

// v2 (future, if needed)
@RestController
@RequestMapping("/api/v2/budgets")
public class BudgetControllerV2 {
    // v2 endpoints with breaking changes
}
```

### Error body

Successful responses return the resource. Errors return `ErrorResponse`:

```json
{
  "timestamp": "2024-03-20T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Budget not found",
  "path": "/api/v1/budgets/1",
  "traceId": "abc",
  "details": null
}
```

Stored budget status values such as `BORRADOR` and `PENDIENTE` stay.

### Deprecation headers

```java
@GetMapping
@Deprecated
public ResponseEntity<?> getBudgets() {
    return ResponseEntity.ok()
        .header("Deprecation", "true")
        .header("Sunset", "Sun, 31 Dec 2024 23:59:59 GMT")
        .body(response);
}
```

### OpenAPI

Live docs are served at `/v3/api-docs`. The UI is `/swagger-ui.html`.
A split of `/v3/api-docs/v1` and `/v3/api-docs/v2` is only needed once v2 exists.

## Migration plan

1. **Phase 1** (current): keep v1 stable
   - Endpoints under `/api/v1`
   - Swagger UI documents v1
   - Release 1.0.0

2. **Phase 2** (months 3–6): deprecate legacy operations
   - Mark legacy endpoints `@Deprecated`
   - Add deprecation headers
   - Tell clients six months before end of life

3. **Phase 3** (month 6 onward): create v2 only if needed
   - Keep v1 and v2 side by side
   - Keep v1 for at least 12 months
   - Define end of life for v1

4. **Phase 4** (month 18 onward): sunset v1
   - Remove v1 endpoints
   - Keep historical documentation

## Governance

### Change control

- Changes to v1 need approval (breaking changes are blocked)
- Changes to v2 and later need a feature review
- Bug fixes that apply to more than one version have a two-week SLA

### Client migration SLA

- Breaking changes: six months' notice
- Deprecation period: at least 12 months
- A critical client can extend the SLA by agreement

## Related ADRs

- ADR-001: Microservices architecture
- ADR-008: Security and authentication
- ADR-006: Testing strategy
- ADR-010: Error handling

## See also

- [Semantic Versioning](https://semver.org/)
- [API Versioning Best Practices](https://cloud.google.com/apis/design/versioning)
- [OpenAPI Specification](https://spec.openapis.org/)
