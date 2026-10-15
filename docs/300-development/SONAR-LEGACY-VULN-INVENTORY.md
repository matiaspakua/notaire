# Sonar legacy vulnerability / high-severity inventory

Overall (not new-code) unresolved findings for `notaire-backend` and
`notaire-frontend`. Slice 1 of [#1535](https://github.com/matiaspakua/notaire/issues/1535)
(CU78). New-code severity ratchet remains [#1737](https://github.com/matiaspakua/notaire/issues/1737).

Refresh (Sonar up):

```bash
python3 infra/scripts/sonar-legacy-inventory.py --write
```

Classification values (English): `fix` | `false_positive` | `accepted_risk` | `defer_hexagon`.

| project | rule | severity | type | component | message | classification | rationale |
|---------|------|----------|------|-----------|---------|----------------|-----------|
| notaire-backend | java:S4973 | MAJOR | BUG | `notaire-backend:src/main/java/com/licensis/notaire/jpa/DocumentTypeJpaController.java` | Strings and Boxed types should be compared using equals(). | defer_hexagon | legacy JpaController equality — hexagonal epic #1693 / #1714+ |
| notaire-backend | java:S6863 | MAJOR | SECURITY_HOTSPOT | `notaire-backend:src/main/java/com/licensis/notaire/adapter/in/web/auth/AuthController.java` | Login response status intentionally 200 on bad credentials (anti-enumeration). | accepted_risk | anti-enumeration contract: bad credentials return HTTP 200 |
