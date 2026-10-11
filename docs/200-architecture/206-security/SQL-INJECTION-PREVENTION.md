# SQL Injection Prevention Guide

This guide documents how Notaire prevents SQL injection and how to keep new code safe.

## Why JPA is safe by default

Notaire uses **Spring Data JPA with Hibernate**. The `JpaRepository` methods (`findById`, `findAll`, `save`, `deleteById`) use **prepared statements** under the hood — Hibernate binds parameters separately from the query string, making injection structurally impossible.

```java
// Safe: Spring Data generates a parameterized query
Optional<User> user = userRepository.findById(id);
// Hibernate executes: SELECT ... FROM users WHERE id = ?
// The value of `id` is bound as a parameter, never interpolated.
```

## Safe patterns

### Spring Data derived query methods

```java
// Safe — Hibernate generates: ... WHERE username = ?
Optional<User> findByName(String name);

// Safe — Hibernate generates: ... WHERE user_type = ? AND status = ?
List<User> findByTypeAndStatus(String type, boolean status);
```

### @Query with named parameters

```java
// Safe — :name is bound as a PreparedStatement parameter
@Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)")
Optional<User> findByNameIgnoreCase(@Param("name") String name);
```

### @Query with JPQL positional parameters

```java
// Safe — ?1 is a positional bind parameter
@Query("SELECT g FROM DeedManagement g WHERE g.number = ?1")
Optional<DeedManagement> findByNumber(Integer number);
```

## Dangerous patterns — never do this

### String concatenation in queries

```java
// DANGEROUS — DO NOT DO THIS
@Query("SELECT u FROM User u WHERE u.name = '" + name + "'")
// An attacker could pass: ' OR '1'='1 to return all users.
```

### Native queries with concatenation

```java
// DANGEROUS
entityManager.createNativeQuery(
    "SELECT * FROM users WHERE username = '" + name + "'"
);
```

## Native queries (when unavoidable)

Use named parameters with `@Query(nativeQuery = true)`:

```java
// Safe native query with parameter binding
@Query(value = "SELECT * FROM users WHERE username = :name", nativeQuery = true)
List<User> findNativeByName(@Param("name") String name);
```

If using `EntityManager` directly (legacy `jpa` package), always use `createNativeQuery` with parameter setting:

```java
// Safe
Query q = em.createNativeQuery("SELECT * FROM deed_managements WHERE number = :num");
q.setParameter("num", number);
List<?> results = q.getResultList();
```

## Dynamic queries (Criteria API)

When query conditions are built at runtime, use the JPA Criteria API:

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<DeedManagement> cq = cb.createQuery(DeedManagement.class);
Root<DeedManagement> root = cq.from(DeedManagement.class);

List<Predicate> predicates = new ArrayList<>();
if (heading != null) {
    predicates.add(cb.equal(root.get("heading"), heading));  // parameterized
}
cq.where(predicates.toArray(new Predicate[0]));
return em.createQuery(cq).getResultList();
```

Never build a JPQL/SQL string by concatenating user values into it.

## Code review checklist

When reviewing code that touches data access:

- [ ] No string concatenation inside `@Query` value strings
- [ ] All `@Query` parameters use `:name` or `?n` bindings with `@Param`
- [ ] All `createNativeQuery` calls use `setParameter()` for user-supplied values
- [ ] No `String.format()` or `+` in any SQL/JPQL string that includes a variable

## Testing for SQL injection

The `EdgeCaseBoundaryConditionsTest` includes an SQL injection attempt in the login request:

```java
// Confirms the login endpoint does not 500 on a SQL injection payload
mvc.perform(post("/api/v1/users/login")
    .contentType(APPLICATION_JSON)
    .content("{\"name\":\"' OR '1'='1\",\"password\":\"\"}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.valid").value(false));
```

The test must remain green. If it ever returns `valid: true` or a 5xx, a regression has been introduced.

## Tools

| Tool | Purpose | How to run |
|------|---------|------------|
| SpotBugs (`SQL_INJECTION` detector) | Static analysis for unsafe SQL | `mvn spotbugs:check -pl backend-api -DskipSpotBugs=false` |
| OWASP Dependency-Check | CVEs in JDBC/JPA libraries | `trivy fs .` |
| Manual review | `grep -r "createNativeQuery\|createQuery" --include="*.java"` followed by inspection | — |

## Related documentation

- [`INPUT-VALIDATION-STRATEGY.md`](INPUT-VALIDATION-STRATEGY.md)
- [`API-AUTHENTICATION-GUIDE.md`](API-AUTHENTICATION-GUIDE.md)
- `.claude/rules/programming.md` — Security section
