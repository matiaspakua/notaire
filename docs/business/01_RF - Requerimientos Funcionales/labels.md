# Estrategia de Labels para Issues

## Labels Existentes (GitHub defaults)
- `bug` - Something isn't working
- `documentation` - Improvements or additions to documentation  
- `duplicate` - This issue or pull request already exists
- `enhancement` - New feature or request
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `invalid` - This doesn't seem right
- `question` - Further information is requested
- `wontfix` - This will not be worked on

## Labels de Tipo de Issue

### Tipo de Requerimiento
| Label | Color | Descripción |
|-------|-------|-------------|
| `requerimiento-funcional` | 36a64f | Requerimiento funcional del sistema |
| `requerimiento-no-funcional` | ff9800 | Requerimiento no funcional (rendimiento, seguridad, etc) |

### Tipo de Issue (no requerimientos)
| Label | Color | Descripción | Proximos Pasos |
|-------|-------|-------------|----------------|
| `BUG` | d73a4a | Bug o defecto en el sistema | ✅ Crear cuando se detecten bugs |
| `CASO-DE-USO` | 00b4d8 | Caso de uso del sistema | 🔜 Crear después de requerimientos |
| `TAREA` | cccccc | Tarea general de desarrollo | ✅ Usar para subtareas |
| `DOCUMENTACION` | 0075ca | Documentación técnica o de usuario | 🔜 Crear después de casos de uso |
| `MEJORAS` | a2eeef | Mejora o enhancement | ✅ Usar cuando aplique |

## Labels de Categoría (Capa/Componente)

| Label | Color | Descripción |
|-------|-------|-------------|
| `BACKEND` | 1d76db | API REST, servicios, persistencia |
| `FRONTEND` | 84b6eb | Swing forms, UI, cliente REST |
| `DB` | d4a5a5 | Schema, migración, optimización |
| `DEVOPS` | 7cd5b4 | Docker, CI/CD, deployment |
| `TEST` | 9b59b6 | Unit tests, integration tests, E2E |
| `DOC` | fad8c3 | Documentación técnica/usuario |
| `REFACTOR` | bfdadc | Mejora de código sin cambio funcional |

## Labels de Prioridad

| Label | Color | Descripción |
|-------|-------|-------------|
| `priority:critical` | b60205 | Bloqueante, crítico para el negocio |
| `priority:high` | d93f0b | Alta prioridad |
| `priority:medium` | fbc02d | Prioridad media |
| `priority:low` | 0e8a16 | Prioridad baja |

## Labels de Estado

| Label | Color | Descripción |
|-------|-------|-------------|
| `blocked` | ef9998 | Bloqueado por dependencia |
| `in-review` | 7057ff | En revisión |
| `ready-for-dev` | 00b4d8 | Listo para desarrollo |
| `needs-info` | d876e3 | Necesita más información |

## Convenciones para Uso

### Combinaciones Típicas

1. **Requerimiento Funcional - Backend**
   - `requerimiento-funcional` + `BACKEND`

2. **Requerimiento Funcional - Full Stack**
   - `requerimiento-funcional` + `BACKEND` + `FRONTEND`

3. **Bug Report**
   - `BUG` + `BACKEND` o `FRONTEND` + `priority:*`

4. **Caso de Uso**
   - `CASO-DE-USO` + `BACKEND` + `FRONTEND`

5. **Documentación**
   - `DOCUMENTACION` + `DOC`

### Ejemplo: RF-01 Gestionar Trámites
```
Labels: requerimiento-funcional, BACKEND, FRONTEND
```

### Ejemplo: RNF-03 Tiempo de respuesta
```
Labels: requerimiento-no-funcional, BACKEND, FRONTEND
```

### Ejemplo: Bug en API de Personas
```
Labels: BUG, BACKEND, priority:high
```

## Plan de Creación de Issues

### Fase 1: Requerimientos ✅ COMPLETADO
- [x] 67 Requerimientos Funcionales (RF-01 a RF-67)
- [x] 24 Requerimientos No Funcionales (RNF-01 a RNF-24)
- [x] 28 Requerimientos Adicionales del RS (RF-68 a RF-95)

### Fase 2: Casos de Uso (Próximo)
- [ ] Casos de uso del sistema
- [ ] Diagramas de casos de uso
- [ ] Especificaciones de casos de uso

### Fase 3: Tareas de Desarrollo
- [ ] Tareas técnicas
- [ ] Subtareas de cada requerimiento
- [ ] Tareas de arquitectura

### Fase 4: Documentación
- [ ] Documentación técnica
- [ ] Manuales de usuario
- [ ] Documentación de API

### Fase 5: Testing
- [ ] Casos de prueba
- [ ] Plan de testing
- [ ] Test cases
