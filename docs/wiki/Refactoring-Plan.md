# Plan de Refactoring

## Objetivo

Refactorizar el sistema desde un monolito Java Swing hacia una arquitectura de microservicios con REST API.

## Arquitectura Target

```
┌─────────────────┐     ┌─────────────────┐
│  Frontend Swing  │────▶│   REST API      │
│  (Cliente)      │     │  (Spring Boot)  │
└─────────────────┘     └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │   PostgreSQL     │
                        │   (Docker)       │
                        └─────────────────┘
```

## Módulos del Proyecto

| Módulo | Descripción | Paquete |
|--------|-------------|---------|
| `backend-api` | REST API | `com.licensis.notaire.api` |
| `frontend-swing` | GUI Client | `com.licensis.notaire.gui` |
| `notaire-shared` | DTOs comunes | `com.licensis.notaire.dto` |

## Proceso de Refactoring

### Fase 1: Migración de Base de Datos
- [x] Schema PostgreSQL
- [ ] Migración de datos MySQL
- [ ] Validación de integridad

### Fase 2: Backend API
- [x] Crear repositorios Spring Data JPA
- [x] Crear servicios (`PersonaService`, `EscrituraService`)
- [x] Refactorizar controllers (`PersonaController`, `EscrituraController`)
- [ ] Refactorizar restantes controllers
- [ ] Eliminar JpaControllers legacy

### Fase 3: Frontend
- [ ] Implementar cliente REST
- [ ] Reemplazar acceso directo a BD por llamadas API
- [ ] Remover lógica de negocio del frontend

### Fase 4: Testing
- [ ] Tests unitarios para servicios
- [ ] Tests de integración con H2
- [ ] Cobertura mínima 80%

## Reglas de Arquitectura

### Backend (Spring Boot)
```
Package: com.licensis.notaire.{api,service,jpa,negocio,dto}
├── api/       → @RestController, @RequestMapping("/api/v1/...")
├── service/   → @Service, @Transactional
├── jpa/       → JpaController.getInstancia() (legacy, a eliminar)
├── negocio/   → Entidades (Usuario, Persona, Presupuesto)
└── dto/       → DTOs para transferencia
```

### Frontend (Swing)
```
Package: com.licensis.notaire.gui
├── No acceso directo a BD
├── No lógica de negocio
├── Solo presentación
└── REST client para API
```

## Convenciones de Código

- **Java**: 21
- **Indentación**: 4 espacios
- **Línea máximo**: 120 caracteres
- **Imports**: Sin wildcards, ordenados por paquete
- **Naming**: PascalCase clases, camelCase métodos

## Estado Actual

### Issues Creadas
- 95 Requerimientos Funcionales (GitHub Issues #3-#95)
- 25 Requerimientos No Funcionales (Issues #70-#93, #121)

### Servicios Implementados
- ✅ `PersonaService`
- ✅ `EscrituraService`

### Controllers Refactorizados
- ✅ `PersonaController`
- ✅ `EscrituraController`

## Recursos

- [Requerimientos](https://github.com/matiaspakua/notaire/labels/requerimiento-funcional)
- [Backend API](https://github.com/matiaspakua/notaire/labels/BACKEND)
- [Frontend](https://github.com/matiaspakua/notaire/labels/FRONTEND)
