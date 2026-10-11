# ADR-002: Maven module structure and package organization

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez  
**Related:** ADR-001, ADR-005  
**Supersedes:** Legacy monolithic structure  

> **Update (see ADR-005):** the `frontend-swing` module referenced below was later
> renamed to `deprecated-frontend-swing` and excluded from the root Maven reactor.
> `mvn clean install -pl frontend-swing -am` no longer works — see the corrected
> command in [Build Commands](#build-commands).
>
> **Update (see ADR-025):** the `notaire-shared` module below was retired under #1255.
> Its classes live in `backend-api` and the folder is archived in
> `deprecated/notaire-shared/`; the reactor now has the single module `backend-api`.
>
> **Update (see ADR-021):** REST controllers live under
> `com.licensis.notaire.adapter.in.web`. Use-case services live under
> `com.licensis.notaire.application.usecase`. The package samples below name
> types that exist today.

## Context

Notaire had three distinct responsibilities that needed a clear layout:

1. **REST API backend**: business logic and persistence
2. **Swing frontend**: user interface
3. **Shared code**: DTOs and common utilities

The previous monolith mixed those responsibilities. A Maven module layout was needed to:

- Enforce separation of responsibilities
- Make independent testing possible
- Allow selective builds
- Make component versioning and release easier

## Decision

Use a multi-module Maven structure with three main modules. Two of those modules
are retired (see the updates above). The reactor module that remains is
`backend-api`.

```text
notaire/ (aggregator pom.xml)
├── backend-api/
│   ├── src/main/java/com/licensis/notaire/
│   │   ├── adapter/in/web/   # REST controllers
│   │   ├── application/      # use-case services
│   │   ├── repository/       # Spring Data JPA
│   │   ├── business/         # @Entity domain classes
│   │   ├── dto/              # response DTOs
│   │   ├── config/           # Spring configuration
│   │   ├── exception/        # custom exceptions
│   │   ├── jpa/              # legacy JPA controllers
│   │   ├── service/          # JpaAdministrator, SessionAdministrator, ValidationAdministrator
│   │   └── util/             # utilities
│   └── src/test/java/com/licensis/notaire/
│       ├── unit/             # unit tests
│       └── integration/      # integration tests
│
├── frontend-swing/           # removed from the tree (#1046); history stays in git
│
├── notaire-shared/           # retired (#1255); archived at deprecated/notaire-shared/
│
└── pom.xml (aggregator)
```

The coverage exclusion `service.Administrador*` stays as the JaCoCo rule name.
The classes in that package are `JpaAdministrator`, `SessionAdministrator`, and
`ValidationAdministrator`.

### Backend-API: internal layers

**adapter/in/web/** (REST controllers)

```text
adapter/in/web/
├── budget/BudgetController.java
├── person/PersonController.java
├── deed/DeedController.java
├── management/ManagementController.java
├── audit/AuditRecordController.java
└── report/ReportController.java
```

**application/usecase/** (business orchestration)

```text
application/usecase/
├── budget/BudgetService.java
├── person/PersonService.java
├── deed/DeedService.java
└── audit/AuditRecordService.java
```

**repository/** (Spring Data JPA)

```text
repository/
├── BudgetRepository.java
├── PersonRepository.java
├── DeedRepository.java
├── DeedManagementRepository.java
└── UserRepository.java
```

**business/** (domain entities)

```text
business/
├── Budget.java
├── Person.java
├── Deed.java
├── DeedManagement.java
├── User.java
└── Folio.java
```

**config/** (Spring configuration)

```text
config/
├── JpaConfig.java
├── SecurityAndCorsConfig.java
├── OpenApiConfig.java
└── ObservabilityConfig.java
```

**exception/** (custom exceptions)

```text
exception/
├── NotaireException.java
├── ResourceNotFoundException.java
├── BusinessValidationException.java
└── ErrorResponse.java
```

**dto/** (response DTOs)

```text
dto/
├── DtoBudget.java
├── DtoPerson.java
├── DtoDeed.java
├── DtoDeedManagement.java
└── DtoHistorySummary.java
```

## Options considered

### Option A: Single monolithic JAR

| Dimension | Assessment |
|-----------|------------|
| Complexity | Low |
| Independent deploy | No |
| Testability | Medium |

**Cons:**

- Frontend and backend builds cannot be separated
- Independent testing is harder
- This is the legacy structure

### Option B: Multi-module Maven (selected)

| Dimension | Assessment |
|-----------|------------|
| Complexity | Medium |
| Independent deploy | Yes |
| Testability | High |

**Pros:**

- Clear separation of responsibilities
- Selective builds (`mvn -pl backend-api -am`)
- Centralized dependency management
- Granular CI/CD
- Independent module versioning

### Option C: Gradle monorepo

| Dimension | Assessment |
|-----------|------------|
| Complexity | High |
| Performance | Better for large monorepos |

**Cons:**

- Learning curve for a Maven team
- Costly migration away from Maven

## Trade-off analysis

### Complexity versus separation

A multi-module layout adds some upfront complexity (dependencies between modules),
but the separation is worth it. Teams can work independently with fewer merge conflicts.

### Build time versus change safety

Selective builds (`mvn -pl backend-api`) are faster because the backend does not
wait for the GUI. Each module can be tested on its own.

## Consequences

### Positive

- **Clear separation**: no cross-imports from backend to GUI
- **Independent builds**: the backend does not wait for the frontend
- **Granular testing**: unit tests per module
- **Independent deployment**: the backend can ship without the GUI

### Challenges

- **Dependency versions**: keep modules compatible with each other
- **Shared-module overhead**: a small coordination cost
- **Slower full build**: a multi-module reactor builds modules in order

## Implementation rules

### Backend-API

1. **No Swing imports**

   ```java
   // Forbidden
   import javax.swing.*;
   ```

2. **No direct database access from controllers**

   ```java
   // Incorrect — direct JDBC
   Statement stmt = connection.createStatement();

   // Correct — repository injection
   @Autowired private BudgetRepository repo;
   ```

3. **No swallowed exceptions**

   ```java
   // Incorrect
   try { ... } catch (Exception e) { }

   // Correct
   catch (Exception e) {
       LOGGER.error("Database error", e);
       throw new ResourceNotFoundException("Failed to fetch", e);
   }
   ```

### Frontend-Swing

These rules applied to the retired Swing client. The active UI is `frontend/`.

1. **No business logic in the GUI**

   ```java
   // Incorrect
   if (budget.getAmount() > 1000) {
       // tax calculation
   }

   // Correct
   BigDecimal taxes = apiClient.calculateTaxes(budget);
   ```

2. **No direct database access**

   ```java
   // Incorrect
   Connection conn = DriverManager.getConnection(...);

   // Correct
   DtoBudget dto = apiClient.getBudget(id);
   ```

3. **Use ApiClient for every operation**

   ```java
   @Autowired private ApiClient apiClient;

   void loadBudgets() {
       List<DtoBudget> items = apiClient.listBudgets();
   }
   ```

### Notaire-Shared

Retired under ADR-025. While it existed:

- Only DTOs, constants, and shared utilities
- Versioned on its own
- No imports from `backend-api` or `frontend-swing`

## Build commands

```bash
# Full build
mvn clean install

# Backend only
mvn clean install -pl backend-api -am

# Swing only (module removed from the root reactor; history stays in git)
# Do not rebuild frontend-swing. See ADR-005.

# Backend tests
mvn test -pl backend-api

# Quality check
mvn verify -pl backend-api
```

## Related ADRs

- ADR-001: Microservices architecture
- ADR-003: REST API versioning
- ADR-004: MySQL to PostgreSQL migration
- ADR-005: Frontend migration from Java Swing to Next.js
- ADR-006: Testing strategy
- ADR-021: Hexagonal architecture pilot
- ADR-025: Retire `notaire-shared`

## See also

- `CLAUDE.md`: code conventions
- [Architecture index](../README.md)
