# JPA Lazy/Eager Loading Guide

## Summary

With `spring.jpa.open-in-view=false`, lazy loading only works inside an active Hibernate session (i.e., inside a `@Transactional` method). Any lazy collection accessed outside a transaction throws `LazyInitializationException`.

## Entities with Lazy Collections

The following entities have `FetchType.LAZY` associations that are accessed during DTO mapping:

| Entity | Lazy Field | Accessed in `getDto()`? | Controller fix needed |
|--------|-----------|-------------------------|-----------------------|
| `Testimony` | `testimonyMovementList` | Yes (iterates) | ✅ Fixed PR #504 |
| `Testimony` | `copyList` | No (not in `getDto()`) | N/A |
| `Deed` | `folioList`, `procedureList`, `testimonyList` | Yes | ✅ Fixed PR #506 |
| `DeedManagement` | `procedureList`, `historyList` | Yes | ✅ Fixed PR #506 |
| `Budget` | `itemList`, `procedureList` | Entity returned directly | ✅ Fixed PR #506 |
| `Procedure` | `personProcedureList`, `personList`, `submittedDocumentList` | Yes | ✅ Fixed PR #506 |
| `WorkflowDefinition` | `nodes`, `transitions` | Entity returned directly | ✅ Fixed PR #506 |
| `Person` | `procedureList`, `budgetList`, `DeedManagementList`, `folioList`, `userList`, `copyList` | Yes | ✅ Fixed PR #506 |
| `User` | `auditRecordList` | Entity returned directly | ✅ Fixed PR #506 |
| `ProcedureType` | `budgetTemplateList`, `procedureTemplateList`, `procedureList` | Yes | ✅ Fixed PR #506 |

GET handlers that map entities with lazy collections use `@Transactional(readOnly = true)`. `ReportController` returns PDFs and does not walk those collections. `WorkflowValidationController` exposes only `POST /{id}/validate`, so it has no GET mapping of a lazy graph.

## When to Use LAZY vs EAGER

### Use `FetchType.LAZY` (default for `@OneToMany`, `@ManyToMany`)

Collections that are large or rarely needed in every query:

```java
@OneToMany(cascade = CascadeType.ALL, mappedBy = "fkIdTestimony", fetch = FetchType.LAZY)
private List<TestimonyMovement> testimonyMovementList = new ArrayList<>();
```

**Requirement**: the code that accesses this list must run inside a transaction.

### Use `FetchType.EAGER` (default for `@ManyToOne`, `@OneToOne`)

Single associations that are almost always needed. The `@ManyToOne` default is `EAGER` for a reason — the parent record is almost always needed when loading the child:

```java
@ManyToOne(optional = false, fetch = FetchType.EAGER)
private Deed fkIdDeed;
```

**Caution**: never use `EAGER` on `@OneToMany` / `@ManyToMany` — it generates N+1 queries or Cartesian products.

## Patterns

### Pattern 1: Controller with direct repository call (our current pattern)

```java
@GetMapping
@Transactional(readOnly = true)          // required
public ResponseEntity<List<DtoFoo>> getAll() {
    return ResponseEntity.ok(
        repository.findAll().stream()
            .map(Foo::getDto)            // touches lazy fields safely
            .toList()
    );
}
```

### Pattern 2: Service-layer encapsulation (preferred for complex logic)

```java
@Service
public class FooService {
    @Transactional(readOnly = true)
    public List<DtoFoo> findAll() {
        return repository.findAll().stream()
            .map(Foo::getDto)
            .toList();
    }
}

@RestController
public class FooController {
    @GetMapping
    public ResponseEntity<List<DtoFoo>> getAll() {
        return ResponseEntity.ok(fooService.findAll()); // transaction in service
    }
}
```

### Pattern 3: Fetch join to avoid N+1 (for performance)

If the lazy collection is always needed, use a JPQL fetch join instead of changing to EAGER:

```java
@Query("SELECT t FROM Testimony t LEFT JOIN FETCH t.testimonyMovementList WHERE t.idTestimony = :id")
Optional<Testimony> findByIdWithMovements(@Param("id") Integer id);
```

This fetches the collection in a single SQL query without triggering N+1.

## Diagnosing LazyInitializationException

```
org.hibernate.LazyInitializationException: failed to lazily initialize a collection
    of role: com.licensis.notaire.business.Foo.barList,
    could not initialize proxy - no Session
```

**Checklist:**
1. Is `spring.jpa.open-in-view=false` set? (Yes — this is correct)
2. Which lazy collection triggered the error? (`barList` in the example)
3. Which method accesses it? Typically `getDto()` or a serialization step
4. Is that method called from a `@Transactional` context? If not, add it.

## N+1 Query Detection

The current architecture calls `.getDto()` inside a stream over `findAll()` results. Each `getDto()` that accesses a lazy collection triggers an additional SELECT. For large datasets, use fetch joins or `@EntityGraph`:

```java
// Option 1: EntityGraph on repository method
@EntityGraph(attributePaths = {"testimonyMovementList"})
List<Testimony> findAll();

// Option 2: Explicit JPQL fetch join
@Query("SELECT t FROM Testimony t LEFT JOIN FETCH t.testimonyMovementList")
List<Testimony> findAllWithMovements();
```

For the current scale of the Notaire system (notarial office, hundreds of records) the N+1 impact is acceptable. Monitor via the Grafana `notaire-backend` dashboard if response times degrade.

## Related

- `SPRING-TRANSACTION-GUIDE.md` — transaction boundary management
- `@.claude/rules/database-migrations.md` — schema management
- Issues: #277 (transaction guide), #278 (this guide)
