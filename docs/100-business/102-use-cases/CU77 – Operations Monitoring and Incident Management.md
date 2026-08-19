# CU77 – Monitoreo de Operaciones y Gestión de Incidentes (Operations Monitoring and Incident Management)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU77 – Monitoreo de Operaciones y Gestión de Incidentes |
| **Actores** | Equipo de Operaciones (DevOps/SRE), Ingenieros de Guardia, Administrador |
| **Propósito** | Monitorear la infraestructura y servicios del sistema notarial en tiempo real, recolectar métricas operativas y logs, y proveer alertas tempranas para garantizar la alta disponibilidad y resolución rápida de incidentes. |
| **Descripción** | Establece los mecanismos de observabilidad basados en Prometheus, Grafana y Loki para asegurar la operatividad ininterrumpida de las estaciones de trabajo de escritorio y portátiles. |
| **Tipo** | Soporte / Operaciones |
| **Referencias Cruzadas** | RF #88 (PC de escritorio), RF #89 (Notebook) |
| **GitHub ID** | #253, #255, #270, #271, #273, #301, #304, #305, #306, #308 |

## Alcance Técnico

- Recolección de métricas de rendimiento y estado del sistema mediante Prometheus.
- Dashboards de monitoreo visual en Grafana para endpoints, base de datos y recursos.
- Agregación y consulta centralizada de logs mediante Grafana Loki.
- Reglas de alertas automáticas para anomalías de rendimiento, saturación de disco o caídas de servicio.
- Manuales operativos (runbooks) y procedimientos de rollback para incidentes de producción.

## Flujo Operativo de Monitoreo e Incidentes

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Los usuarios operan el sistema notarial desde PCs de escritorio y notebooks. | Expone métricas de salud (`/actuator/prometheus`) y logs estructurados. |
| 2 | Prometheus realiza scraping periódico de métricas. | Almacena series temporales de uso de memoria, hilos, latencia y conexiones a base de datos. |
| 3 | Se detecta un umbral de anomalía (ej. tiempo de respuesta > 10 s o tasa de error > 1%). | Grafana Alerting dispara una notificación automática al canal de guardia. |
| 4 | El ingeniero de guardia analiza el incidente mediante logs de Loki y dashboards. | Identifica la causa raíz y ejecuta el procedimiento de mitigación o rollback según el runbook. |

## Criterios de Aceptación

- [x] Recolección de métricas operativas con Prometheus en funcionamiento.
- [x] Tableros de control en Grafana accesibles para monitoreo de la escribanía.
- [x] Registro y búsqueda centralizada de logs con Loki implementados.
- [x] Procedimientos de respuesta a incidentes y runbooks operativos documentados.
