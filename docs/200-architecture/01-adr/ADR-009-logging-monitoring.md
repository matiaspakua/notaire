# ADR-009: Logging & Monitoring

## Status
Accepted

## Context
As the system moves towards a containerized deployment (Docker/Kubernetes), we need centralized visibility into application health, performance, and errors.

## Decision
We will implement a structured logging and monitoring stack based on the **Loki-Prometheus-Grafana (LPG)** pattern.

### Key implementation details:
1.  **Logging**: Use **Logback** with a JSON appender for structured logs. Logs will be collected by **Grafana Loki**.
2.  **Metrics**: Export application metrics using **Spring Boot Actuator** and **Micrometer**.
3.  **Collection**: **Prometheus** will scrape metrics from the `/actuator/prometheus` endpoint.
4.  **Visualization**: **Grafana** dashboards for real-time monitoring.
5.  **Alerting**: Configure Alertmanager for critical errors (e.g., Database down, High 5xx error rate).

## Options Considered
-   **ELK Stack (Elasticsearch, Logstash, Kibana)**: Very powerful but resource-heavy for our current scale.
-   **Cloud-specific (AWS CloudWatch, etc.)**: Rejected to maintain provider-agnostic infrastructure.

## Consequences
-   **Pros**: High observability, low overhead (Loki is more efficient than ELK), easy integration with Spring Boot.
-   **Cons**: Requires additional infrastructure setup (k3d/Kubernetes).
