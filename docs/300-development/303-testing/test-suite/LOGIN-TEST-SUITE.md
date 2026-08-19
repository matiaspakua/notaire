# Test Suite — Login Authentication (CU-78)

**Use Case:** CU-78 — Seguridad, Privacidad y Cumplimiento (Security and Compliance)
**Endpoint:** `POST /api/v1/usuarios/login`
**Issue:** [#824](https://github.com/matiaspakua/notaire/issues/824)
**Status:** Complete ✅

## 1. Scope

This test suite validates the login authentication flow for the Notaire system,
covering credential validation, JWT token generation, account lockout, and
security event logging.

## 2. Test Cases

| TC-ID | Scenario | Level | Priority | Status |
|-------|----------|-------|----------|--------|
| TC-LOGIN-01 | Valid credentials return JWT token | Integration | High | ✅ Pass |
| TC-LOGIN-02 | Wrong password returns valido=false | Integration | High | ✅ Pass |
| TC-LOGIN-03 | Unknown user returns valido=false | Integration | High | ✅ Pass |
| TC-LOGIN-04 | Locked account returns 429 | Integration | High | ✅ Pass |
| TC-LOGIN-05 | Expired JWT token rejected with 401 | Integration | High | ✅ Pass |
| TC-LOGIN-06 | Invalid JWT token rejected with 401 | Integration | High | ✅ Pass |
| TC-LOGIN-07 | Missing Bearer token rejected with 401 | Integration | High | ✅ Pass |
| TC-LOGIN-08 | Valid token accepted for API access | Integration | High | ✅ Pass |
| TC-LOGIN-09 | Password matching with BCrypt | Unit | High | ✅ Pass |
| TC-LOGIN-10 | LoginAttemptService lockout logic | Unit | High | ✅ Pass |
| TC-LOGIN-11 | LoginAttemptService reset on success | Unit | Medium | ✅ Pass |
| TC-LOGIN-12 | Empty username/password handled | Integration | Medium | ✅ Pass |
| TC-LOGIN-13 | Case-insensitive username matching | Integration | Low | ✅ Pass |
| TC-LOGIN-14 | Inactive user cannot login | Integration | High | ✅ Pass |

## 3. Traceability Matrix

| Requirement (CU-78) | Test Cases |
|---------------------|------------|
| Credentials validated against secure hash | TC-LOGIN-01, TC-LOGIN-02, TC-LOGIN-09 |
| JWT token generated on valid login | TC-LOGIN-01, TC-LOGIN-08 |
| Account lockout after failed attempts | TC-LOGIN-04, TC-LOGIN-10 |
| Token transmitted via HTTPS/TLS | TC-LOGIN-01, TC-LOGIN-05 |
| Role-based authorization | TC-LOGIN-08 |
| Audit logging of access attempts | TC-LOGIN-01, TC-LOGIN-02, TC-LOGIN-03 |
| No sensitive info in error responses | TC-LOGIN-02, TC-LOGIN-03, TC-LOGIN-12 |

## 4. Test Data

| User | Username | Password | Role | Status |
|------|----------|----------|------|--------|
| Admin | admin | admin | ADMIN | Active |
| Escribano | escribano | escribano | ESCRIBANO | Active |
| Inactive | inactive | inactive | CLIENTE | Inactive |

## 5. Running Tests

```bash
# Unit tests
mvn test -pl backend-api -Dtest=LoginAttemptServiceTest

# Integration tests
mvn test -pl backend-api -Dtest=JwtAuthIntegrationTest

# Full suite
mvn test -pl backend-api -Dtest="*Login*,*Auth*,*Usuario*"
```
