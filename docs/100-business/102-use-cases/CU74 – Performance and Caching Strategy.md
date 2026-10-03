# CU74 – Estrategia de Rendimiento y Caché (Performance and Caching Strategy)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU74 – Estrategia de Rendimiento y Caché (Performance and Caching Strategy) |
| **Actores** | Administrador del Sistema, Equipo de Desarrollo, Equipo de Operaciones |
| **Propósito** | Optimizar el rendimiento del sistema mediante estrategias de caché, optimización de consultas JPA/SQL, gestión de transacciones y pools de conexión para garantizar tiempos de respuesta menores a 10 segundos y uso eficiente de CPU y memoria RAM. |
| **Descripción** | Cubre los requerimientos no funcionales de rendimiento y sistema, optimizando la latencia de las transacciones notariales, consultas de carpetas, búsqueda de personas y generación de documentos en entornos de múltiples usuarios concurrentes. |
| **Tipo** | Soporte / Arquitectura |
| **Referencias Cruzadas** | RF #70 (Uso de memoria RAM), RF #71 (Uso de CPU), RF #72 (Tiempo de respuesta), RF #73 (Múltiples usuarios), RF #88 (PC de escritorio), RF #89 (Notebook); CU70, CU72 |
| **GitHub ID** | #298, #278, #277, #290, #303, #594, #1047 |

## Alcance Técnico

- Caché a nivel de aplicación (Spring Cache con Caffeine/Redis) para tablas maestras y catálogos (tipos de trámite, folios, conceptos).
- Optimización de consultas JPA con estrategias Lazy/Eager y prevención de problemas N+1.
- Configuración de pool de conexiones (HikariCP) y límites de aislamiento transaccional.
- Establecimiento de líneas base de rendimiento y pruebas de carga automatizadas
  (`infra/performance/k6/load-test.js`, workflow semanal `performance-test.yml`;
  umbrales CU74: p95 ≤ 2s, tasa de error &lt; 1%; issue #1047 / #594).

## Procedimiento de Ejecución y Monitoreo

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El equipo técnico configura las políticas de caché en memoria para catálogos y consultas frecuentes. | Aplica los interceptores de caché y almacena los resultados de lecturas repetitivas. |
| 2 | El usuario realiza consultas complejas (búsqueda de gestiones, historial o escrituras). | Recupera datos desde caché o ejecuta consultas indexadas optimizadas en PostgreSQL. |
| 3 | El sistema procesa la solicitud bajo carga concurrente. | Mantiene el consumo de memoria RAM por debajo de 300 MB por proceso y uso de CPU inferior al 50%. |
| 4 | El equipo de operaciones ejecuta pruebas de carga y monitoreo. | Valida que todos los endpoints respondan en menos de 10 segundos (objetivo p95 < 2 segundos). |

## Criterios de Aceptación

- [x] Estrategia de caché documentada e implementada.
- [x] Optimización de carga JPA (Lazy/Eager) validada sin consultas N+1.
- [x] Gestión de transacciones Spring configurada con aislamiento apropiado.
- [x] Pool de conexiones de base de datos ajustado y probado bajo concurrencia.
- [x] Pruebas de carga ejecutadas con cumplimiento de SLAs (< 10 s tiempo de respuesta).
- [x] Suite k6 restaurada en CI semanal / `workflow_dispatch` con login API en inglés
  (`name`/`password`), umbrales p95 ≤ 2000 ms y `http_req_failed` &lt; 1%, y artefacto
  `summary.json` (`k6-load-test-results`; issue #1047).
