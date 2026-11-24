## ADDED Requirements

### Requirement: The public ingress exposes only the actuator health endpoints

The reverse proxy (`infra/deploy/kustomize/base/nginx.conf`, used by `docker-compose.prod.yml` and the Kustomize
base) SHALL forward `/actuator/health` and its sub-paths to the backend and SHALL answer 404 to every other
`/actuator/` path. Prometheus and the container probes keep reaching the backend directly on the private network.

#### Scenario: Health stays reachable through the ingress
- **WHEN** a client requests `/actuator/health/liveness` through the reverse proxy
- **THEN** the proxy forwards it to the backend

#### Scenario: Other actuator endpoints are not published
- **WHEN** a client requests `/actuator/threaddump`, `/actuator/loggers` or `/actuator/prometheus` through the reverse proxy
- **THEN** the proxy answers 404 without contacting the backend
