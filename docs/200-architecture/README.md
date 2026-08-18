# Documentación de Arquitectura

Arquitectura del sistema Notaire: visión arc42, decisiones de diseño, diagramas, modelo de datos,
seguridad, observabilidad, DevSecOps y deployment.

## Estructura

| Carpeta | Contenido |
|---------|-----------|
| [`201-SAD/`](201-SAD/) | Software Architecture Document, estructurado según arc42 |
| [`202-ADR/`](202-ADR/) | Architecture Decision Records — decisiones arquitectónicas relevantes |
| [`203-design/`](203-design/) | Diseño de API y sistema de diseño (design system) del frontend |
| [`204-diagrams/`](204-diagrams/) | Diagramas PlantUML: casos de uso, clases, estados, secuencia |
| [`205-data-model/`](205-data-model/) | ERD, diccionario de datos y diagrama de compensación (integridad referencial) |
| [`206-security/`](206-security/) | Autenticación, validación de entrada y prevención de inyección SQL |
| [`207-monitoring/`](207-monitoring/) | Observabilidad: Prometheus, Grafana, Loki |
| [`208-devsecops/`](208-devsecops/) | Infraestructura Docker y prácticas DevSecOps |
| [`209-deployment/`](209-deployment/) | Estrategia y guía de despliegue |

## Punto de entrada

El [SAD](201-SAD/sad.md) es el documento raíz: da contexto, restricciones, vista de bloques y
runtime, y enlaza a los ADRs y diagramas específicos según corresponda.

## Fuente de verdad del esquema de base de datos

Las migraciones Flyway (`backend-api/src/main/resources/db/migration/V*.sql`) son la única fuente
de verdad del esquema (ver [ADR-007](202-ADR/ADR-007-database-schema-versioning-flyway.md) y
[`.claude/rules/database-migrations.md`](../../.claude/rules/database-migrations.md)). Toda
discrepancia entre `205-data-model/` y Flyway se resuelve a favor de Flyway.

## Navigation

- [← Documentación](../)
- [Negocio](../100-business/)
- [Desarrollo](../300-development/)
