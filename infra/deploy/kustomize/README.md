# Notaire Kustomize manifests (issue #901)

KIS staging/production Kubernetes target mirroring
[`docker-compose.prod.yml`](../../docker-compose.prod.yml) (#1044):

| Service | Role | Service type |
|---------|------|--------------|
| `postgres` | Database | ClusterIP |
| `backend` | Spring Boot API | ClusterIP |
| `frontend` | Next.js UI | ClusterIP |
| `reverse-proxy` | Sole ingress (nginx) | LoadBalancer |

No pgAdmin. Secrets are placeholders only. CD (`.github/workflows/cd.yml`)
remains **publish-only** — apply these manifests manually to a real cluster.

## Layout

```
infra/deploy/kustomize/
  base/                 # shared resources
  overlays/staging/     # GHCR SHA image tags + staging labels
```

## Quick start

```bash
# Requires kustomize v5+
kustomize build infra/deploy/kustomize/overlays/staging

# Static AC guard (issue #901)
python3 scripts/test_staging_kustomize.py

# Apply (after replacing Secret placeholders + image SHAs)
kubectl apply -k infra/deploy/kustomize/overlays/staging
```

See `docs/200-architecture/209-deployment/README.md` for Secret keys, image
pinning, and the relationship to production compose.
