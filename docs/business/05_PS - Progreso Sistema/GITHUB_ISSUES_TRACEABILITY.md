# GitHub Issues Traceability

This document maintains the single source of truth for GitHub issue tracking across the Notaire project.

## Migration Tasks (Tareas de Migración)

All migration tasks are tracked in GitHub Issues with IDs #240-261. See `TAREAS_MIGRACION.csv` for details.

| GitHub ID | Task | Priority | Status |
|-----------|------|----------|--------|
| #240 | Completar migración de formularios restantes | Alta | Pendiente |
| #241 | Validar formularios con API REST | Alta | Pendiente |
| #242 | Probar flujos completos de negocio | Alta | Pendiente |
| #243 | Verificar generación de reportes PDF | Alta | Pendiente |
| #244 | Testing manual de casos edge | Alta | Pendiente |
| #245 | Implementar autenticación JWT/OAuth2 | Alta | Pendiente |
| #246 | Completar el 5% de APIs restantes | Alta | Pendiente |
| #247 | Agregar paginación a endpoints | Alta | Pendiente |
| #248 | Tests E2E automatizados | Media | Pendiente |
| #249 | Logs en formato Prometheus | Media | Pendiente |
| #250 | Visualización con Grafana | Media | Pendiente |
| #251 | Tests unitarios con tags a requerimientos | Media | Pendiente |
| #252 | Flyway para base de datos | Media | Pendiente |
| #253 | Configurar Kubernetes | Baja | Pendiente |
| #254 | Configurar HTTPS/TLS | Baja | Pendiente |
| #255 | Setup monitoreo Prometheus/Grafana | Baja | Pendiente |
| #256 | Backup automatizado PostgreSQL | Baja | Pendiente |
| #257 | Documentar endpoints en Swagger | Baja | Pendiente |
| #258 | Migrar documentación a Markdown | Baja | Pendiente |
| #259 | Crear guía de instalación producción | Baja | Pendiente |
| #260 | Documentar arquitectura del sistema | Baja | Pendiente |
| #261 | Crear manual de usuario | Baja | Pendiente |

## Use Cases (Casos de Uso)

All 68 use cases are tracked in GitHub Issues with IDs #154-221. See `cu_issues_mapping.json` for complete mapping.

### Use Case Issues by Status

**Completed (Terminado)** - 65 use cases
- CU01-CU14, CU16-CU23, CU26-CU45, CU46, CU48-CU68
- Issues #154-221 (excluding #168, #177, #178, #200, #203)

**In Progress or Not Started** - 3 use cases
- CU15 – Procesar pago (#168) - Estaria Bueno, 0%
- CU24 – Generar libro de índices (#177) - Estaria Bueno, 0%
- CU25 – Generar Declaración Jurada del mes (#178) - Estaria Bueno, 0%
- CU47 – Consultar Pago (#200) - Estaria Bueno, 0%
- CU50 – Generar Declaración Jurada de Rentas (#203) - Estaria Bueno, 0%

## Functional Requirements (Requerimientos Funcionales)

All functional requirements are tracked with IDs #3-121 in `requerimientos.csv`.

See `cu_rf_traceability.json` for the mapping between use cases and requirements.

## Improvements and Enhancements (Mejoras y Ampliaciones)

All improvements, enhancements, and missing features are tracked in GitHub Issues with IDs #262-#309. These 48 issues were extracted from:
- Legacy improvements list (`Progreso Sistema - MEJORAS.csv`)
- Documentation needs (`Progreso Sistema - DOCUMENTACION.csv`)
- Security, backend, DevOps, and operational improvements

### Issues by Priority and Category

**Critical Priority (10 issues - #262-#271)**
- API authentication & authorization (JWT, OAuth2, RBAC)
- Error handling strategy and standards
- Database migration guide
- Environment configuration management
- Incident response and rollback procedures
- OWASP Top 10 compliance
- Input validation and XSS/CSRF prevention
- SQL injection prevention
- Disaster recovery plan
- Database maintenance procedures

**High Priority (18 issues - #272-#289)**
- REST API versioning strategy
- Logging and monitoring configuration
- Frontend REST client implementation
- Flyway/Liquibase database migration
- Spring Boot testing best practices
- Code review checklist
- Rate limiting and security testing
- Dependency vulnerability management
- Configuration management and parity
- Complete Swagger/OpenAPI documentation

**Medium Priority (20 issues - #290-#309)**
- Performance tuning guide
- Architecture Decision Records (ADRs)
- Database schema documentation
- REST client error handling
- Swing component patterns
- Test data management
- Integration test isolation
- Javadoc and documentation standards

## Legacy Issues (Historical)

Legacy bugs and issues from the monolithic application are documented in `Progreso Sistema - ISSUES.csv`. These are mostly fixed issues from the original Java Swing application and are kept for reference only.

## Cleanup and Consolidation (2026-04-07)

The following files have been removed as their content is now tracked in GitHub issues:
- ~~`Progreso Sistema - MEJORAS.csv`~~ → GitHub issues #262-#309
- ~~`Progreso Sistema - DOCUMENTACION.csv`~~ → GitHub issues in improvements
- ~~`Progreso Sistema - PANTALLAS A MODIFICAR.csv`~~ → GitHub issues in improvements
- ~~`Progreso Sistema.xls`~~ → GitHub issue tracking system
- ~~`PLAN.md`~~ → GitHub issues #240-#309
- ~~`PLANO_REFACTORING.md`~~ → GitHub issues and refactoring documentation

**Single source of truth**: All issues are now tracked exclusively in GitHub for better visibility, filtering, and project management.

## How to Update Traceability

1. **For new migration tasks**: Create GitHub issue and reference in this file
2. **For new use cases**: Create GitHub issue and update `cu_issues_mapping.json` with the issue ID
3. **For new requirements**: Create GitHub issue and update `requerimientos.csv` with the issue ID
4. **For improvements/enhancements**: Create GitHub issue and add to the appropriate priority section above
5. **Always update this file** when adding new tracked items

## Files to Maintain

| File | Purpose | Updated |
|------|---------|---------|
| `TAREAS_MIGRACION.csv` | Migration tasks tracking | 2026-04-07 |
| `cu_issues_mapping.json` | Use case to GitHub issue mapping | 2026-04-07 |
| `cu_rf_traceability.json` | Use case to requirements mapping | 2026-04-07 |
| `requerimientos.csv` | Functional requirements | 2026-04-07 |
| `Progreso Sistema - ISSUES.csv` | Legacy historical issues (reference only) | 2026-04-07 |
| `GITHUB_ISSUES_TRACEABILITY.md` | This file - overall traceability index | 2026-04-07 |

---

**Last Updated**: 2026-04-07
**Updated By**: Claude Code
**Total Issues Tracked**: 138 (68 CUs + 22 Migration Tasks + 48 Improvements/Enhancements)
