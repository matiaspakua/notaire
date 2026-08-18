# ADR-014: Motor de Workflow Configurable para Gestiones

**Status:** Accepted
**Date:** 2026-06-09
**Deciders:** Matías Miguez
**Related:** ADR-001 (Microservices Architecture), ADR-004 (Database Migration)

## Context

El monolito legacy modelaba el ciclo de vida de una `GestionDeEscritura` (y de un `Tramite`) como un conjunto fijo de estados codificados en Java (`EstadoDeGestion`), con las transiciones válidas dispersas en condicionales dentro de los controladores JPA legacy. Añadir o modificar un estado, o cambiar qué transiciones son válidas para un tipo de trámite, requería cambiar código y desplegar.

El sistema notarial tiene múltiples tipos de trámite (`TipoDeTramite`), cada uno con su propio flujo de estados válido — un modelo de máquina de estados fija en código no escala a esa variabilidad sin duplicar lógica por tipo.

## Decision

Modelar el flujo de estados como datos versionados en base de datos, mediante tres entidades JPA en `com.licensis.notaire.negocio`:

- **`WorkflowDefinition`** — una definición de flujo, asociable a uno o más `TipoDeTramite`.
- **`WorkflowNode`** — un nodo del flujo, vinculado a un `EstadoDeGestion` existente y tipado como `WorkflowNodeType`: `INITIAL`, `INTERMEDIATE` o `FINAL`. Incluye coordenadas (`posicionX`, `posicionY`) para renderizado visual del diagrama en el frontend.
- **`WorkflowTransition`** — una arista dirigida entre dos `WorkflowNode` (`nodoOrigen` → `nodoDestino`), con una `condicion` opcional (expresión evaluada para habilitar la transición) y `descripcion` legible.

La validación de transiciones se expone vía `WorkflowValidationController` / `WorkflowValidationService`, y la trazabilidad de instancias de flujo vía `WorkflowTraceService`. La definición y edición de flujos se expone vía `WorkflowDefinitionController`, `WorkflowNodeController` y `WorkflowTransitionController`.

Todas las entidades incluyen columna `version` para versionado optimista (`@Version` semántico gestionado a nivel de aplicación), consistente con el resto del modelo de dominio.

### Por qué un motor de workflow basado en datos (y no un enum de estados fijo, ni un motor BPMN externo)

| Criterio | Motor basado en datos (elegido) | Enum de estados fijo | Motor BPMN externo (Camunda/Flowable) |
|----------|----------------------------------|------------------------|------------------------------------------|
| Flexibilidad por tipo de trámite | Alta — cada `TipoDeTramite` referencia su propia `WorkflowDefinition` | Ninguna — un solo flujo para todos los tipos | Alta, pero con modelo de proceso ajeno al dominio |
| Complejidad añadida | Media — 3 entidades nuevas, sin motor externo | Mínima | Alta — nuevo runtime, nuevo lenguaje (BPMN XML), curva de aprendizaje |
| Cambios de flujo sin despliegue | Sí — se editan filas, no código | No | Sí |
| Encaje con Spring Data JPA existente | Directo — mismas convenciones que el resto del dominio | Directo | Requiere integración adicional |

**Decisión:** motor de workflow propio y minimalista basado en datos, evitando la sobre-ingeniería de introducir un motor BPMN completo para un dominio con un número acotado de tipos de trámite.

## Consequences

### Positivos

- Un administrador puede definir o ajustar el flujo de un tipo de trámite sin cambios de código, vía los endpoints `Workflow*Controller`.
- El modelo es directamente representable como diagrama de estados (ver `docs/200-architecture/204-diagrams/Diagrama de Estados/`), ya que cada `WorkflowNode` mapea 1:1 a un `EstadoDeGestion`.
- Reutiliza los patrones ya establecidos (JPA, DTOs, repositorios) — no introduce un paradigma nuevo en el backend.

### Negativos

- La validación de la `condicion` de una `WorkflowTransition` depende de convenciones de aplicación (no hay motor de expresión formal); su semántica debe documentarse por caso de uso.
- Un `WorkflowDefinition` mal configurado (por ejemplo, sin nodo `FINAL` alcanzable) no se detecta en tiempo de diseño; depende de `WorkflowValidationService` en tiempo de ejecución.

## Related ADRs

- ADR-001: Microservices Architecture
- ADR-004: Database Migration (MySQL → PostgreSQL)
