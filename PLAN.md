# Notaire Project - Action Plan

**Last Updated:** March 2026  
**Status:** Migration Complete - Improvements Phase

---

## Completed ✅

- [x] Database schema migration (MySQL → PostgreSQL)
- [x] Backend REST API (26 controllers)
- [x] Frontend GUI refactoring (Swing with REST client)
- [x] All 68 use cases implemented

---

## Priority 1: Security Improvements

| Task | Description | Effort | Status |
|------|-------------|--------|--------|
| S1 | Replace MD5 password hashing with bcrypt | Medium | Pending |
| S2 | Implement JWT authentication | Medium | Pending |
| S3 | Add role-based access control (RBAC) | Medium | Pending |
| S4 | Add API rate limiting | Low | Pending |

**Files to modify:**
- `backend-api/src/main/java/com/licensis/notaire/api/UsuarioController.java`
- `backend-api/src/main/java/com/licensis/notaire/negocio/Usuario.java`
- Create `backend-api/src/main/java/com/licensis/notaire/security/` package

---

## Priority 2: Backend Enhancements

| Task | Description | Effort | Status |
|------|-------------|--------|--------|
| B1 | Add input validation annotations (@Valid) | Low | Pending |
| B2 | Create standardized error response DTO | Low | Pending |
| B3 | Add global exception handler (@ControllerAdvice) | Low | Pending |
| B4 | Implement pagination for list endpoints | Low | Pending |
| B5 | Add request/response logging interceptor | Low | Pending |

**Files to create:**
- `backend-api/src/main/java/com/licensis/notaire/dto/ErrorResponse.java`
- `backend-api/src/main/java/com/licensis/notaire/exception/GlobalExceptionHandler.java`

---

## Priority 3: Testing Coverage

| Task | Description | Effort | Status |
|------|-------------|--------|--------|
| T1 | Increase unit test coverage to 85% | High | Pending |
| T2 | Add integration tests for all controllers | High | Pending |
| T3 | Add API contract tests | Medium | Pending |
| T4 | Add performance tests for critical endpoints | Medium | Pending |

**Commands:**
```bash
mvn test -pl backend-api
mvn jacoco:check -pl backend-api
```

---

## Priority 4: Documentation

| Task | Description | Effort | Status |
|------|-------------|--------|--------|
| D1 | Complete OpenAPI annotations for all endpoints | Medium | Pending |
| D2 | Add API usage examples | Low | Pending |
| D3 | Update README with deployment instructions | Low | Pending |

---

## Quick Wins (This Sprint)

1. **Add @Valid annotations** to controller request bodies
2. **Create ErrorResponse DTO** with consistent error format
3. **Add global exception handler** for cleaner error responses
4. **Run JaCoCo coverage check** to identify gaps

---

## Build Commands

```bash
# Full build
mvn clean install

# Single test
mvn test -Dtest=PresupuestoEntityTest

# Coverage check
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api

# Start application
bash scripts/start.sh

# Run API tests
bash scripts/test.sh
```

---

## File Structure Reference

```
notaire/
├── AGENTS.md                    # Agent instructions
├── backend-api/                 # Spring Boot REST API
│   └── src/main/java/com/licensis/notaire/
│       ├── api/               # REST Controllers (26)
│       ├── negocio/            # Entity classes
│       ├── dto/                # Data Transfer Objects
│       ├── jpa/                # JPA Controllers
│       └── service/            # Business services
├── frontend-swing/             # Swing GUI Client
│   └── src/main/java/com/licensis/notaire/
│       ├── api/client/        # REST client
│       ├── gui/               # Swing forms
│       └── servicios/          # Services
├── init-db/                    # Database schema
│   ├── 01-schema.sql
│   └── 02-data.sql
└── notaire-shared/            # Shared DTOs
```
