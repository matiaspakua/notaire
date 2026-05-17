# ADR-001: Migración de Monolito Swing a Arquitectura de Microservicios

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez, Equipo de Arquitectura  
**Supersedes:** N/A  
**Superseded by:** N/A  

## Context

El sistema Notaire era originalmente una aplicación monolítica desarrollada en Java 1.6 con interfaz Swing, acceso directo a base de datos MySQL, y lógica de negocio acoplada a la capa de presentación. Esta arquitectura presentaba los siguientes problemas:

- **Acoplamiento fuerte**: La lógica de negocio estaba distribuida entre la GUI y la base de datos
- **Dificultad de mantenimiento**: Cambios en requisitos requerían modificaciones en múltiples capas
- **Escalabilidad limitada**: Imposible escalar componentes individuales
- **Testing difícil**: Acoplamiento a Swing y JDBC dificultaba pruebas automatizadas
- **Tecnología desactualizada**: Java 1.6, MySQL, sin estándares modernos

**Restricciones:**
- Mantener funcionalidad existente (54 casos de uso)
- Migración gradual, sin parada total del sistema
- Equipo con experiencia en Java pero nuevo en microservicios
- Presupuesto y tiempo limitados

## Decision

Migrar a una arquitectura de tres capas con separación clara:

1. **Backend REST API** (Spring Boot 4.0.4, Java 21, PostgreSQL 16)
   - Centraliza toda lógica de negocio
   - Proporciona API REST estándar
   - Gestiona persistencia de datos
   - Implementa auditoría y seguridad

2. **Frontend Web (Next.js)** (TECNOLOGÍA TARGET - ver ADR-005)
   - Interfaz de usuario moderna basada en web
   - Reemplaza gradualmente al cliente Swing
   - Utiliza sistema de diseño centralizado (ver ADR-011)

3. **Frontend Swing Cliente** (Mantenimiento / Transición)
   - Refactorizado para consumir API REST
   - Siendo deprecado en favor de la versión web
   - Sin lógica de negocio ni acceso directo a BD

4. **Módulo Compartido** (DTOs y código común)
   - Definiciones comunes entre backend y frontend (Swing/Java)
   - Facilita versionado de contratos

**Tecnologías elegidas:**
- **Backend**: Spring Boot 4.0.4, Java 21
- **Database**: PostgreSQL 16, Hibernate
- **Build**: Maven 3.x
- **Deploy**: Docker + Docker Compose
- **API**: REST con OpenAPI 3.0 (Swagger)

## Options Considered

### Option A: Mantener monolito Swing + mejorar
| Dimensión | Evaluación |
|-----------|-----------|
| Complejidad | Baja |
| Costo | Bajo |
| Escalabilidad | Muy baja |
| Mantenibilidad | Muy baja |
| Testabilidad | Muy baja |

**Pros:**
- Cambios mínimos en código existente
- No requiere reentrenamiento del equipo
- Costo inicial bajo

**Cons:**
- No resuelve problemas de acoplamiento
- Imposible escalar componentes
- Testing automatizado limitado
- Continúa dependencia de tecnología antigua
- Dificulta incorporación de nuevas características

### Option B: Rewrite completo (Big Bang)
| Dimensión | Evaluación |
|-----------|-----------|
| Complejidad | Alta |
| Costo | Muy alto |
| Escalabilidad | Alta |
| Mantenibilidad | Alta |
| Riesgo | Muy alto |

**Pros:**
- Código completamente nuevo y limpio
- Arquitectura moderna desde cero
- Mejor performance potencial
- Oportunidad de refactorizar todo

**Cons:**
- Alto riesgo de fallos durante transición
- Tiempo de desarrollo muy largo (6+ meses)
- Sistema antigua no disponible durante migración
- Pérdida de conocimiento acumulado
- Costo muy elevado

### Option C: Migración gradual a 3 capas (SELECCIONADO)
| Dimensión | Evaluación |
|-----------|-----------|
| Complejidad | Media |
| Costo | Medio |
| Escalabilidad | Alta |
| Mantenibilidad | Alta |
| Riesgo | Bajo |

**Pros:**
- Migración controlada y validable
- Sistema funcional en cada etapa
- Reduce riesgo de fallos críticos
- Permite mantener negocio operativo
- Testing incremental de cada módulo
- Aprendizaje gradual de nuevas tecnologías

**Cons:**
- Requiere más coordinación
- Coexistencia temporal de tecnologías
- Mantenimiento de múltiples versiones
- Trabajo inicial de setup duplicado

## Trade-off Analysis

**Escalabilidad vs. Complejidad**

La opción C requiere más coordinación inicial que mantener el monolito, pero permite escalar cada componente independientemente. El backend puede replicarse sin tocar la GUI, y la base de datos puede optimizarse sin afectar la lógica de aplicación.

**Riesgo vs. Velocidad**

El Big Bang (Option B) sería más rápido en teoría, pero el riesgo operacional es inaceptable. La migración gradual reduce riesgo significativamente: cada cambio puede probarse en prod con rollback.

**Costo de Transición vs. Costo de Mantenimiento**

El costo inicial de la migración es compensado ampliamente por:
- Reducción de bugs en producción
- Menor time-to-market para features nuevas
- Mejor performance
- Capacidad de escalar bajo carga

## Consequences

### Positivas
- **Separación de responsabilidades**: GUI y negocio desacoplados
- **Testabilidad**: El backend puede testearse independientemente sin GUI
- **Escalabilidad horizontal**: Backend puede escalarse según demanda
- **Modernización tecnológica**: Java 21, Spring Boot, PostgreSQL
- **API reutilizable**: Otros clientes (móvil, web) pueden consumir la API
- **Seguridad mejorada**: Control centralizado, auditoría en backend

### Desafíos
- **Mantenimiento de múltiples módulos**: Más packages que mantener
- **Versionado de API**: Necesario planificar evolucion de contratos REST
- **Network latency**: Llamadas HTTP tienen latencia vs. llamadas directas
- **Complejidad operacional**: Más servicios que deployar y monitorear
- **Duplicación temporal**: Código legacy y nuevo coexisten durante transición

### Acciones de seguimiento
- [ ] Establecer SLAs para API REST
- [ ] Implementar circuit breakers para resiliencia
- [ ] Plan de deprecación para código legacy
- [ ] Estrategia de versionado de API (semantic versioning)
- [ ] Documentación de contratos REST (OpenAPI)

## Implementation Plan

1. **Phase 1**: Setup Spring Boot backend con módulo compartido
2. **Phase 2**: Migrar entidades de negocio y repositorios
3. **Phase 3**: Implementar servicios de negocio
4. **Phase 4**: Crear endpoints REST
5. **Phase 5**: Refactorizar GUI para consumir API
6. **Phase 6**: Deprecar código legacy
7. **Phase 7**: Monitoreo y optimización

## Related Decisions

- ADR-002: Estructura de paquetes y módulos Maven
- ADR-003: Estrategia de versionado de API REST
- ADR-004: Seguridad y autenticación en REST API
- ADR-005: Estrategia de testing para arquitectura distribuida
