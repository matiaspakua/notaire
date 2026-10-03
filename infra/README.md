# Notaire infrastructure

Everything that runs **around** the Notaire application: the observability and
code-quality stack, the Kubernetes staging manifests, the reverse-proxy config and
the load test. This folder is self-contained and prepared to live in its own
repository; for now it sits inside the application repository (#1179).

Project-level documentation (architecture, ADRs, business) stays in
[`docs/`](../docs/README.md); everything specific to configuring, preparing,
defining and running the infrastructure is here.

## Guides

| Guide | Answers |
|-------|---------|
| [PREPARATION](docs/PREPARATION.md) | What do I need installed and running first? |
| [CONFIGURATION](docs/CONFIGURATION.md) | Which variables and config files do I set? |
| [DEFINITION](docs/DEFINITION.md) | What is defined here, and how does it couple to the application? |
| [OPERATION](docs/OPERATION.md) | How do I start, check, deploy, troubleshoot and roll back? |

## Layout

```text
infra/
  observability/   Prometheus, Grafana, Loki/Promtail, SonarQube, Homer (compose)
  deploy/          Kubernetes (Kustomize) base + staging overlay, shared nginx.conf
  performance/k6   load test
  scripts/         start-infra, check-infra, run-sonar, generate-report
  docs/            the four guides above
  .env.example     variables this folder needs
```

## Quick start

```bash
bash scripts/start.sh                       # 1) the application (creates its network)
cp infra/.env.example infra/.env            # 2) once
bash infra/scripts/start-infra.sh           # 3) observability + SonarQube
bash infra/scripts/check-infra.sh           # 4) verify
```

Or both at once: `bash scripts/start-all.sh`. Then open http://localhost:8888.

## Guard tests

`python3 scripts/test_infra_standalone.py` enforces the layout, self-containment
and the single `nginx.conf`; the other `scripts/test_*.py` guards cover images,
Prometheus hardening, Kustomize and the load test.
