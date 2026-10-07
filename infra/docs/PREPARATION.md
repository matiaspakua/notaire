# Preparing the infrastructure

What you need before running anything in `infra/`. Next: [CONFIGURATION](CONFIGURATION.md)
→ [DEFINITION](DEFINITION.md) → [OPERATION](OPERATION.md).

## Prerequisites

| Need | Why | Required for |
|------|-----|--------------|
| Docker + Docker Compose v2 | runs every service | observability stack |
| ~6 GB free RAM | SonarQube alone needs ~2 GB | observability stack |
| Linux hosts: `sysctl vm.max_map_count=262144` | SonarQube's embedded search | SonarQube |
| The Notaire application running | the stack joins its Docker network and scrapes it | observability stack |
| `kustomize` v5+ and `kubectl` | render and apply the manifests | staging deploy |
| Python 3 + PyYAML | static guard tests in `scripts/` | CI / local checks |
| `k6` (or the CI action) | load test | performance |

## 1. Application first

The observability stack attaches to the application's Docker network
(`notaire_notary-network`, declared `external`) and scrapes `notary-backend` and
`notary-postgres` by container name. Start the application before the infra:

```bash
bash workspace/stack/start.sh            # from the application repository root
```

`start-infra.sh` checks the network and stops with guidance if it is missing.
Override the names with `NOTAIRE_APP_NETWORK` if your application uses another network.

## 2. Environment file

```bash
cp infra/.env.example infra/.env
```

Lookup order (`scripts/common.sh`): `$INFRA_ENV_FILE` → `infra/.env` → the
application's root `.env`. If none exists, `start-infra.sh` creates `infra/.env`
from the template. Variables are documented in [CONFIGURATION](CONFIGURATION.md).

## 3. Free ports

| Port | Service |
|------|---------|
| 8888 | Homer dashboard |
| 9090 | Prometheus |
| 3001 | Grafana |
| 3100 | Loki |
| 9187 | PostgreSQL exporter |
| 9000 | SonarQube |

## 4. Where the application checkout lives

`run-sonar.sh` analyses the application code. While `infra/` sits inside the
application repository it finds it automatically (`..`); once split, point
`NOTAIRE_APP_DIR` at the application checkout.
