# Spring Transaction Management Guide

## Context

The Notaire backend sets `spring.jpa.open-in-view=false` (see `application.properties`). This is the correct production setting — it means the Hibernate session is opened and closed within the service/repository call boundary, not the full HTTP request. Without this understanding, accessing lazy-loaded collections outside a transaction boundary throws `LazyInitializationException` → HTTP 500.

## The Root Problem

```java
// BROKEN — session closes after findAll() returns, before .map() runs
@GetMapping
public ResponseEntity<List<DtoFoo>> getAll() {
    return ResponseEntity.ok(
        repository.findAll().stream()   // ← session opens and closes HERE
            .map(Foo::getDto)           // ← LazyInitializationException if getDto() touches LAZY fields
            .toList()
    );
}
```

## The Fix

Add `@Transactional(readOnly = true)` to any controller or service method that maps lazy-loaded entities to DTOs:

```java
import org.springframework.transaction.annotation.Transactional;

@GetMapping
@Transactional(readOnly = true)         // ← session stays open through the whole method
public ResponseEntity<List<DtoFoo>> getAll() {
    return ResponseEntity.ok(
        repository.findAll().stream()
            .map(Foo::getDto)           // ← safe, session still open
            .toList()
    );
}
```

## Rules

### 1. Use `readOnly = true` for GET methods

```java
@GetMapping
@Transactional(readOnly = true)
public ResponseEntity<List<DtoX>> getAll() { ... }

@GetMapping("/{id}")
@Transactional(readOnly = true)
public ResponseEntity<DtoX> getById(@PathVariable Integer id) { ... }
```

`readOnly = true` enables Hibernate optimizations (no dirty checking, smaller memory footprint) and signals intent.

### 2. Mutation methods need `@Transactional` (without readOnly)

```java
@PostMapping
@Transactional
public ResponseEntity<Object> create(@RequestBody DtoX dto) { ... }

@PutMapping("/{id}")
@Transactional
public ResponseEntity<Object> update(...) { ... }

@DeleteMapping("/{id}")
@Transactional
public ResponseEntity<Object> delete(...) { ... }
```

### 3. Service layer is the preferred boundary

When business logic is in a `@Service` class, the transaction should be on the service, not the controller. Controllers that call services inherit the service transaction. However, controllers that call `repository` methods directly (the pattern in this codebase) must manage their own transaction.

```java
// Service with its own transaction — controller needs none
@Service
public class EscrituraService {
    @Transactional(readOnly = true)
    public Page<Escritura> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
```

### 4. Identify LAZY fields that need covering

When a new `@OneToMany(fetch = FetchType.LAZY)` is added to an entity whose `getDto()` iterates that collection, the controller or service calling `.getDto()` must be transactional. Check with:

```bash
grep -rn "FetchType.LAZY" backend-api/src/main/java/com/licensis/notaire/negocio/
```

## Verification

If a GET endpoint returns HTTP 500 with `LazyInitializationException` in the logs:

1. Find the entity's `getDto()` method — which lazy collections does it access?
2. Find the controller method calling `.getDto()`.
3. Add `@Transactional(readOnly = true)` to that controller method.
4. Write an integration test that exercises the full GET → DTO mapping:

```java
@Test
@DisplayName("GET /api/v1/foo should return 200 and list")
void shouldReturnAll() throws Exception {
    mockMvc.perform(get("/api/v1/foo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
}
```

## History

- **2026-06-16**: Fixed `LazyInitializationException` in 25 controllers (PR #504, PR #506). All `@GetMapping` methods in the `api` package now have `@Transactional(readOnly = true)`. Root cause: `spring.jpa.open-in-view=false` + `FetchType.LAZY` on entity associations + controller calling `.stream().map(Entity::getDto)` outside any transaction.

## Related

- `@.claude/rules/database-migrations.md` — schema management
- `JPA-LAZY-LOADING-GUIDE.md` — which entities have lazy associations and why
- Issues: #277 (this guide), #278 (lazy loading guide), #503 (Testimonio fix), #505 (systemic fix)
