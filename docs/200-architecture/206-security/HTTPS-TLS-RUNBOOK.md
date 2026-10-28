# HTTPS / TLS runbook (#254)

**Use case:** CU-78 · **Issue:** [#254](https://github.com/matiaspakua/notaire/issues/254)

## Spring Boot `https` profile

Profile file: `backend-api/src/main/resources/application-https.properties`

| Property | Purpose |
|----------|---------|
| `server.port=8443` | TLS listener |
| `server.ssl.*` | PKCS#12 keystore settings |
| `auth.cookie.secure=true` | HttpOnly JWT cookie requires HTTPS (#1051) |

Activate locally:

```bash
bash backend-api/scripts/generate-local-tls-keystore.sh
export SPRING_PROFILES_ACTIVE=https
export SSL_KEY_STORE=file:./backend-api/target/local-dev-keystore.p12
export SSL_KEY_STORE_PASSWORD=changeit
mvn spring-boot:run -pl backend-api
curl -k https://localhost:8443/actuator/health
```

Self-signed material is for **local prove-out only**. Browsers will warn until you trust the cert.

## Docker Compose

- **Development (`docker-compose.yml`)**: HTTP on published ports; optional `SPRING_PROFILES_ACTIVE=https` on the backend service if you mount a keystore volume and map `8443:8443`.
- **Production (`docker-compose.prod.yml`)**: terminate TLS at the **reverse proxy** (port 443) in front of the internal HTTP services—the usual deployment path. Set `COOKIE_SECURE=true` in `.env` when users reach the UI over HTTPS.

Example production env additions:

```bash
COOKIE_SECURE=true
# Optional direct backend TLS (uncommon when nginx terminates TLS):
# SPRING_PROFILES_ACTIVE=https
# SSL_KEY_STORE=file:/run/secrets/backend-keystore.p12
```

Mount real certificates or keystores via Docker secrets / volume; never commit private keys.

## Production certificate runbook

1. **Obtain certificates** from your CA (Let’s Encrypt, corporate PKI, or commercial CA) for the public hostname(s) serving the notary UI and API.
2. **Terminate TLS at the edge** (recommended): configure `infra/deploy/kustomize/base/nginx.conf` or your ingress with:
   - Full chain + private key
   - TLS 1.2+ only, modern cipher suites
   - HSTS (`Strict-Transport-Security`) once HTTPS is stable
3. **Set application flags**: `COOKIE_SECURE=true`, `CORS_ALLOWED_ORIGINS` must list `https://` origins only.
4. **Renewal**: automate ACME (certbot / ingress controller) or follow your PKI rotation calendar; reload the proxy after renewal.
5. **Verify**:
   - `curl -fsS https://<host>/actuator/health`
   - Login flow succeeds; cookie is `Secure` and `HttpOnly`
   - Prometheus scrapes still use actuator basic auth on the internal network

## Tests

- `mvn test -pl backend-api -Dtest=HttpsProfileSslPropertiesTest` — profile exposes `server.ssl` properties.
