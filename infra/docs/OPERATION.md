# Operating the infrastructure

Commands are run from the repository root while `infra/` is co-located.
Prerequisites: [PREPARATION](PREPARATION.md); variables: [CONFIGURATION](CONFIGURATION.md).

## Start and stop

```bash
bash scripts/start-all.sh               # application, then infra
bash infra/scripts/start-infra.sh       # infra only (application must be up)
docker compose -f infra/observability/docker-compose.yml --env-file infra/.env down   # stop
```

Open Homer at http://localhost:8888 for links to every service.

## Health check

```bash
bash infra/scripts/check-infra.sh
```

Checks Homer, SonarQube, Prometheus, Grafana, Loki, Promtail, the exporter and
the application endpoints. Quick manual probes:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:9090/api/v1/targets        # all targets UP
curl http://localhost:3001/api/health
curl http://localhost:3100/ready
curl http://localhost:9187/metrics | head
```

## Dashboards and logs

- Backend: http://localhost:3001/d/notaire-backend
- PostgreSQL: http://localhost:3001/d/notaire-postgres
- Auth and security: http://localhost:3001/d/notaire-auth
- Logs: Grafana → Explore → Loki, for example
  `{container_name="notary-backend"} | json` or `{service="notaire"}`
  (labels: `container`, `container_name`, `service`, `app`, `level`).

## Code quality (SonarQube)

```bash
bash infra/scripts/run-sonar.sh
```

Waits for SonarQube, handles the first-login password change, generates an
analysis token (saved as `SONAR_TOKEN` in your env file), runs the backend tests
with JaCoCo and submits the analysis. Results:
http://localhost:9000/dashboard?id=notaire-backend.

## Reports

```bash
bash infra/scripts/generate-report.sh    # Markdown + HTML under infra/reports/ (git-ignored)
```

## Load test

```bash
k6 run -e BASE_URL=http://localhost:8080 -e ADMIN_USER=admin -e ADMIN_PASSWORD=admin \
  infra/performance/k6/load-test.js
```

## Staging deployment (Kustomize)

Needs `kustomize` v5+ and a Kubernetes cluster (not provided here).

1. Pin the GHCR SHA tags published by CD (replace `sha-PLACEHOLDER`):

   ```bash
   cd infra/deploy/kustomize/overlays/staging
   kustomize edit set image \
     notaire-backend=ghcr.io/<owner>/notaire/backend:<git-sha> \
     notaire-frontend=ghcr.io/<owner>/notaire/frontend:<git-sha>
   ```

2. Create the real Secret (never commit values):

   ```bash
   kubectl -n notaire create secret generic notaire-secrets \
     --from-literal=POSTGRES_DB=... --from-literal=POSTGRES_USER=... \
     --from-literal=POSTGRES_PASSWORD=... \
     --from-literal=SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/... \
     --from-literal=JWT_SECRET=... \
     --from-literal=ACTUATOR_USER=... --from-literal=ACTUATOR_PASSWORD=... \
     --from-literal=APP_ADMIN_USER=... --from-literal=APP_ADMIN_PASSWORD=... \
     --from-literal=POSTGRES_EXPORTER_USER=... --from-literal=POSTGRES_EXPORTER_PASSWORD=... \
     --dry-run=client -o yaml | kubectl apply -f -
   ```

3. Render, validate and apply:

   ```bash
   kustomize build infra/deploy/kustomize/overlays/staging | kubectl apply -f -
   python3 infra/tests/test_staging_kustomize.py
   ```

4. Reach the app only through the reverse-proxy LoadBalancer. A published image
   does **not** mean the cluster was updated: apply is a separate manual step.

## Troubleshooting

**Prometheus cannot scrape the backend** — check `curl http://localhost:8080/actuator/health`,
the targets page http://localhost:9090/targets, and
`curl -u $ACTUATOR_USER:$ACTUATOR_PASSWORD http://localhost:8080/actuator/prometheus`;
confirm the container shares the application network.

**Loki receives no logs** — `curl http://localhost:3100/ready`,
`docker logs devsecops-promtail`, and check the Promtail mounts.

**Grafana dashboards missing** — check http://localhost:3001/datasources,
`docker logs devsecops-grafana`, and that the JSON in
`observability/grafana/provisioning/dashboards/` is valid.

**Exporter has no metrics** — `curl http://localhost:9187/metrics`, verify the
exporter credentials against Flyway V12, and the queries in
`observability/prometheus/postgres_exporter.yml`.

**"Application network not found"** — start the application first
(`bash scripts/start.sh`).

## Rollback

Observability: `docker compose ... down`, redeploy the previous revision of `infra/`
and start again; volumes persist. Staging: re-apply the previous overlay revision
(`git checkout <rev> -- infra/deploy && kubectl apply -k ...`).
