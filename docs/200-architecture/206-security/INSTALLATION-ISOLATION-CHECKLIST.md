# Per-installation isolation checklist (ADR-029 W3)

**Use case:** CU-78 · **Issue:** [#1684](https://github.com/matiaspakua/notaire/issues/1684)

Each Notaire installation serves one notary ([ADR-029](../202-ADR/ADR-029-object-level-authorization.md)).
Cross-notary isolation is enforced by **separate deployments** with distinct secrets and data
stores—not by row-level tenancy in a shared database.

## Automated verifier

Run before production cutover or in CI/CD preflight:

```bash
# Uses the current shell environment (export vars or source .env first)
python3 security/check_installation_isolation.py

# Or point at the deployment .env file
python3 security/check_installation_isolation.py --env-file .env
```

The script **exits non-zero** when any of these are missing or equal to documented defaults:

| Variable | Documented insecure values |
|----------|----------------------------|
| `JWT_SECRET` | blank; `change-this-to-a-random-secret-of-at-least-32-bytes` (`.env.example`); `notaire-default-secret-key-change-in-production-!!` (legacy); shorter than 32 bytes |
| `APP_ADMIN_PASSWORD` | blank; `admin` |
| `ACTUATOR_USER` | blank; `admin`; `root` |
| `ACTUATOR_PASSWORD` | blank; `change-me-actuator` and other placeholders; common weak words; shorter than 16 characters |

Runtime guards complement the checklist:

- `JwtTokenService` rejects blank, short, or known-default JWT secrets at startup.
- `ProductionCredentialsGuard` rejects insecure credentials when `ENVIRONMENT=production`.

## Manual checklist (ADR-029)

- [ ] Dedicated PostgreSQL instance or database and credentials (no shared schema).
- [ ] Unique `JWT_SECRET`, `ACTUATOR_*`, and application admin password (verifier above).
- [ ] Dedicated object storage / uploads path and backups.
- [ ] Dedicated observability stack (no cross-tenant Prometheus/Grafana datasources).

## Tests

- `python3 security/tests/test_installation_isolation.py`
- Included in `bash security/verify.sh`
