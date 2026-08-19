# Deployment Plan — Notaire

Process-oriented companion to
[`209-deployment/README.md`](../200-architecture/209-deployment/README.md)
(topology diagrams, Docker Compose service reference, environment
variables). This document covers environments, the promotion/release
process, rollback, and the release checklist — it does not repeat the
architecture diagrams or service tables already in that README.

## 1. Environments

Notaire currently defines a **single deployment target: local/self-hosted
Docker Compose**. No staging or production environment is currently
provisioned — this is a known gap tracked in the SAD's Risks and Technical
Debt section (§11.1).

| Environment | Where it runs | Started by |
|-------------|---------------|------------|
| Local development | Developer machine, Docker | `bash scripts/start.sh` (app) + `bash infra/scripts/start-infra.sh` (observability/quality) |
| CI | GitHub Actions runners | `.github/workflows/ci.yml`, ephemeral per run |
| Published image | GHCR (`ghcr.io/<repo>/backend`) | CD pipeline, see §2 — consumed by whichever environment pulls it; none currently does automatically |

Until a real staging/production target exists, "deployment" in practice
means: CD publishes a signed, versioned image to GHCR, and a human pulls
and runs it (`docker-compose up -d` against that tag) wherever the system
is actually hosted.

## 2. Promotion / release process

Governed by `CONSTITUTION.md` §11 (Release Rules) and implemented in
[`.github/workflows/cd.yml`](../../.github/workflows/cd.yml). The pipeline
only runs after CI has already gone green — it never builds/publishes an
untested commit:

```
PR → CI green on merge to main (ci.yml)
        │  (workflow_run trigger, gated on conclusion == 'success')
        ▼
CD: build backend-api/Dockerfile → push to GHCR
        │
        ├─ Generate SBOM (CycloneDX via Trivy) — uploaded as artifact, report-only
        ├─ Scan published image (Trivy) — report-only, exit-code 0 (see #705 for the
        │   blocking-policy decision on fs/image scans generally)
        ├─ Sign image (cosign, keyless/OIDC)
        └─ Attest SBOM to the image (cosign attest)
        │
        ▼ (only on `v*` tag push)
Create GitHub Release (softprops/action-gh-release, includes SBOM)
        │
        ▼
Update GHCR/DockerHub description + publish CD report to docs/wiki/cicd-reports/
```

**Triggers**: `workflow_run` after CI succeeds on `main`, a `v*` tag push, or
manual `workflow_dispatch`. Tag pushes and dispatch build/publish
unconditionally; the `workflow_run` path only proceeds if CI's conclusion
was `success`.

**Versioning**: SemVer tags (`v<major>.<minor>.<patch>`) per `CONSTITUTION.md`
§11. Image tags follow `docker/metadata-action`: branch ref, semver (on tag),
short SHA, and `latest` (only on the default branch).

**CHANGELOG.md**: updated per release following Keep a Changelog format,
per `CONSTITUTION.md` §11 — do this before tagging, since the tag push is
what triggers the GitHub Release creation that references it.

## 3. Release checklist

Before pushing a `v*` tag:

- [ ] All CI gates green on `main` for the commit being released (`ci.yml`).
- [ ] `CHANGELOG.md` updated under "Unreleased" → moved to the new version
      heading, per Keep a Changelog.
- [ ] `CONSTITUTION.md` §3 Definition of Done satisfied for every issue
      included in the release.
- [ ] No open `priority:critical` issues targeting this release.

After the tag is pushed and CD completes:

- [ ] GitHub Release created with the correct SBOM attached (automatic —
      verify it happened).
- [ ] Image signature verifiable: `cosign verify` against the published
      digest.
- [ ] Smoke test the published image before closing any issues that
      shipped in it — per `CONSTITUTION.md` §11, closing an issue requires
      a smoke test, not just a green pipeline.
- [ ] CD report published to `docs/wiki/cicd-reports/` (automatic).

## 4. Rollback

Runbook for a bad deployment, expanding on
[`209-deployment/README.md`](../200-architecture/209-deployment/README.md)'s
basic procedure:

```bash
# 1. Stop the running stack
docker-compose down

# 2. Pull the previous known-good image tag from GHCR
docker pull ghcr.io/<repo>/backend:<previous-version>

# 3. Point docker-compose at that tag (edit docker-compose.yml or set an
#    image-tag environment variable, per the compose file's image reference)

# 4. If the bad release included a Flyway migration, do NOT roll the schema
#    back automatically — Flyway migrations are forward-only (see
#    .claude/rules/database-migrations.md). Restore from a pre-migration
#    database backup instead:
docker exec -i notary-postgres psql -U admin notaire < backup.sql

# 5. Restart on the previous version
docker-compose up -d

# 6. Verify health
curl http://localhost:8080/actuator/health
```

Because there is no automated staging environment yet, rollback today is a
manual operator action, not a pipeline capability — this is a gap to close
once a real production target is defined (see §1).

## 5. Monitoring the release

Post-deploy health is observed through the existing observability stack
(not duplicated here): Prometheus/Grafana dashboards
(`notaire-backend`, `notaire-postgres`) and Loki logs — see
[`207-monitoring/README.md`](../200-architecture/207-monitoring/README.md).
Watch these immediately after any rollback or tag-triggered release.

## Navigation

- [← Development](README.md)
- [Deployment architecture & Compose reference](../200-architecture/209-deployment/README.md)
- [CD pipeline](../../.github/workflows/cd.yml)
- [Development Plan](DEVELOPMENT-PLAN.md)
