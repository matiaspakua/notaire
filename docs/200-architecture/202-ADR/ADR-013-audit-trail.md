# ADR-013: Auditoría Transversal con Spring AOP

**Status:** Accepted
**Date:** 2026-05-12
**Deciders:** Matías Miguez
**Related:** ADR-001 (Microservices Architecture), ADR-008 (Security & Authentication), ADR-010 (Error Handling)

## Context

Por requerimiento legal (ver `.claude/rules/general.md` y la sección "Legal Constraints" del [SAD](../201-SAD/sad.md#2-constraints)), toda operación de creación, modificación y eliminación sobre entidades del dominio notarial debe quedar registrada de forma inmutable, junto con el usuario autenticado que la ejecutó. Implementar el registro manualmente en cada controlador (31 `*Controller`) sería repetitivo, propenso a omisiones y difícil de mantener consistente.

Un incidente de diseño previo (issue #555) identificó que confiar en un header enviado por el cliente (`X-Notaire-User`) para atribuir la autoría permitía a un cliente malicioso falsificar el usuario auditado.

## Decision

Implementar la auditoría como una **preocupación transversal (cross-cutting concern)** usando **Spring AOP**, centralizada en `AuditoriaAspect` (`com.licensis.notaire.audit`):

- **Pointcut único**: intercepta todo método público de cualquier clase `*Controller` en el paquete `api`.
- **Advice `@AfterReturning`**: el registro solo se genera si la operación fue exitosa (no audita intentos fallidos de escritura, salvo login/logout).
- **Filtro de operaciones auditables** (`isAuditableOperation`): solo métodos anotados `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`, o cuyo nombre contenga `login`/`logout`. Las operaciones `GET` (lectura) **no se auditan**.
- **Atribución del usuario**: el actor se resuelve exclusivamente desde el `SecurityContextHolder`, poblado por `JwtAuthenticationFilter` a partir de un JWT válido — nunca desde un header enviado por el cliente. Como todo endpoint de escritura bajo `/api/**` requiere autenticación, en el momento en que el aspecto se ejecuta siempre existe un usuario autenticado.
- **Persistencia**: cada registro se guarda en la tabla `registro_auditoria` vía `RegistroAuditoriaService` → `RegistroAuditoriaRepository`.
- **Exposición**: consultable vía `RegistroAuditoriaController` (`GET /api/v1/auditoria`) y visible en el frontend en `/dashboard/auditoria`.

### Por qué AOP (y no auditoría manual por servicio, ni triggers de base de datos)

| Criterio | Spring AOP (elegido) | Auditoría manual en cada servicio | Triggers PostgreSQL |
|----------|----------------------|-------------------------------------|----------------------|
| Cobertura garantizada | Sí — un único pointcut cubre todos los controllers presentes y futuros | No — depende de que cada desarrollador recuerde añadirla | Sí, pero a nivel de fila, sin contexto HTTP/usuario JWT |
| Acceso al usuario autenticado | Directo vía `SecurityContextHolder` | Directo, pero duplicado en cada servicio | No tiene acceso al contexto de aplicación |
| Acoplamiento con lógica de negocio | Ninguno — el aspecto vive fuera de los servicios | Alto — mezcla auditoría con lógica de negocio | Ninguno, pero opaco para el equipo de aplicación |
| Mantenibilidad | Un solo punto de cambio | N cambios (uno por servicio) | Requiere migraciones SQL por tabla |

## Consequences

### Positivos

- Nuevos endpoints de escritura quedan auditados automáticamente sin código adicional, siempre que sigan la convención `*Controller` + verbo HTTP de escritura.
- La atribución del usuario no puede ser falsificada por el cliente (issue #555 resuelto).
- La lógica de auditoría está completamente desacoplada de los servicios de negocio (Single Responsibility).

### Negativos

- El pointcut basado en convención de nombres (`*Controller`) requiere disciplina: un controlador que no siga la convención de paquete `api` o de sufijo `Controller` no será auditado.
- No audita cambios realizados directamente en base de datos fuera de la aplicación (fuera de alcance: se asume que solo `backend-api` accede a PostgreSQL, ver ADR-001).

## Related ADRs

- ADR-001: Microservices Architecture (solo el backend accede a la base de datos)
- ADR-008: Security & Authentication (JWT como única fuente de identidad)
- ADR-010: Error Handling
