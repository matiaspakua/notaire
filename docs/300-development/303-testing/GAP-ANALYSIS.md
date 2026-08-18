# Gap Analysis: CU ↔ API ↔ Bruno Tests

**Fecha:** 15 de Abril de 2026  
**Branch:** `docs/335_cu-api-gap-analysis`  
**Issue:** [#335](https://github.com/matiaspakua/notaire/issues/335)

---

## Resumen Ejecutivo

| Dimensión | Total | OK | Problemas |
|-----------|-------|----|-----------|
| Casos de Uso (CU) | 68 | 58 | 10 |
| Endpoints REST | 155 | ~140 | ~15 |
| Controllers | 26 | 20 | 6 |
| Tests Bruno (.bru files) | 85 | ~50 | ~35 |
| Cobertura CU con test Bruno | 68 | 45 | 23 (sin test) |

---

## 1. Estado por Categoría

### 1.1 CU en 0% de Progreso (requieren trabajo)

| CU | Nombre | Endpoint existente | Estado real |
|----|--------|--------------------|-------------|
| CU15 | Procesar pago | POST /api/v1/pagos | Endpoint CRUD existe; lógica de negocio incompleta |
| CU24 | Generar libro de índices | GET /api/v1/reportes/libro-indice | Endpoint existe, sin test, sin validación |
| CU25 | Generar Declaración Jurada mes | GET /api/v1/reportes/declaracion-jurada-mensual | Endpoint existe, sin test, sin validación |
| CU47 | Consultar Pago | GET /api/v1/pagos/{id} | Endpoint existe; marcado 0% por clasificación |
| CU50 | Generar Declaración Jurada de Rentas | GET /api/v1/reportes/declaracion-jurada-rentas | Endpoint existe, sin test, sin validación |

**Nota:** Los CU24, CU25 y CU50 tienen endpoints en el backend. El 0% probablemente indica que el reporte JasperReports no fue validado end-to-end. Requieren test con datos reales.

### 1.2 Endpoints con Error 500 (bugs de backend)

Estos endpoints devuelven 500 cuando se testean, indicando errores en el código del backend:

| Controller | Endpoint | CU afectados | Prioridad |
|-----------|----------|--------------|-----------|
| TipoDeFolioController | GET /api/v1/tipo-folio | CU36, CU40, CU58, CU68 | 🔴 ALTA |
| TipoDeDocumentoController | GET /api/v1/tipo-de-documento | CU27, CU32, CU38, CU65 | 🔴 ALTA |
| EstadoDeGestionController | GET /api/v1/estado-gestion | CU30, CU35, CU67 | 🔴 ALTA |
| TestimonioController | GET /api/v1/testimonio | CU07, CU08, CU12, CU44 | 🔴 ALTA |
| MovimientoTestimonioController | GET /api/v1/movimiento-testimonio | CU10, CU12, CU44 | 🔴 ALTA |
| EscrituraController | GET /api/v1/escrituras/escribanos-disponibles | CU48, CU51 | 🟡 MEDIA |

**Impacto:** 16 CU son inutilizables desde el frontend si estos endpoints retornan 500.

### 1.3 CU con ERROR en Concurrencia (requieren revisión)

| CU | Nombre | Endpoint | Nota CSV |
|----|--------|----------|----------|
| CU39 | Crear Plantilla Presupuesto | POST /api/v1/plantilla-presupuestos | ERROR |
| CU49 | Eliminar Plantilla Presupuesto | DELETE /api/v1/plantilla-presupuestos/... | ERROR |
| CU52 | Modificar Escritura | PUT /api/v1/escrituras/{id} | ERROR |
| CU55 | Modificar Plantilla Presupuesto | PUT /api/v1/plantilla-presupuestos/... | ERROR |

### 1.4 Tests Bruno con Errores de Datos (404 esperado)

Estos fallan porque los tests usan IDs hardcodeados que no existen en la DB de test:

| Test | Endpoint | Solución |
|------|----------|---------|
| usuarios/put-usuario.bru | PUT /api/v1/usuarios/{id} | Encadenar POST→PUT→DELETE |
| personas/put-persona.bru | PUT /api/v1/personas/{id} | Encadenar POST→PUT→DELETE |
| personas/get-persona-by-id.bru | GET /api/v1/personas/{id} | Usar variable de ambiente dinámica |
| catalogos/delete-tipo-documento.bru | DELETE /api/v1/tipo-de-documento/{id} | FK constraint + datos seed |
| catalogos/delete-tipo-tramite.bru | DELETE /api/v1/tipo-tramite/{id} | FK constraint + datos seed |

---

## 2. Cobertura Bruno por Módulo

| Módulo | CU Total | Tests OK | Sin Test | 500/FAIL |
|--------|----------|----------|----------|----------|
| Gestiones | 16 | 10 | 3 | 3 |
| Administracion | 22 | 12 | 7 | 3 |
| Clientes | 10 | 7 | 0 | 3 |
| Presupuestos | 5 | 4 | 0 | 1 |
| Pagos | 2 | 2 | 0 | 0 |
| Protocolos | 7 | 2 | 5 | 0 |
| **TOTAL** | **68** | **45** | **15** | **8** |

### Tests Bruno faltantes (NO-TEST)

Los siguientes CU tienen endpoint funcional pero sin test Bruno:

```
CU03  – GET /api/v1/plantilla-tramite/tipo-tramite/{id}
CU22  – POST /api/v1/suplencia
CU24  – GET /api/v1/reportes/libro-indice
CU25  – GET /api/v1/reportes/declaracion-jurada-mensual
CU32  – PUT /api/v1/tipo-de-documento/{id}        (+ GET tiene 500)
CU33  – PUT /api/v1/folio/{id}
CU39  – POST /api/v1/plantilla-presupuestos
CU43  – PUT /api/v1/documento-presentado/{id}
CU49  – DELETE /api/v1/plantilla-presupuestos/...
CU50  – GET /api/v1/reportes/declaracion-jurada-rentas
CU55  – PUT /api/v1/plantilla-presupuestos/...
CU56  – PUT /api/v1/gestiones/{id} (estado=inscripto)
CU58  – DELETE /api/v1/tipo-folio/{id}             (+ GET tiene 500)
CU62  – GET /api/v1/escrituras/buscar
```

---

## 3. Cross-check Frontend ↔ Backend

### Estado de la migración Swing → REST

El frontend está **completamente migrado** a REST. No existe JDBC directo ni SQL en el GUI.

| Cliente REST (Frontend) | Endpoint Backend | Estado |
|------------------------|-----------------|--------|
| conceptoClient | /api/v1/conceptos | ✅ OK |
| copiaClient | /api/v1/copia | ✅ OK |
| documentoPresentadoClient | /api/v1/documento-presentado | ✅ OK |
| escrituraClient | /api/v1/escrituras | ⚠️ escribanos-disponibles 500 |
| estadoDeGestionClient | /api/v1/estado-gestion | 🔴 500 |
| folioClient | /api/v1/folio | ✅ OK |
| gestionClient | /api/v1/gestiones | ✅ OK |
| historialClient | /api/v1/historial | ✅ OK |
| inmuebleClient | /api/v1/inmueble | ✅ OK |
| itemClient | /api/v1/items | ✅ OK |
| movimientoTestimonioClient | /api/v1/movimiento-testimonio | 🔴 500 |
| pagoClient | /api/v1/pagos | ✅ OK |
| personaClient | /api/v1/personas | ✅ OK |
| plantillaPresupuestoClient | /api/v1/plantilla-presupuestos | ⚠️ ERROR concurrencia |
| plantillaTramiteClient | /api/v1/plantilla-tramite | ✅ OK |
| presupuestoClient | /api/v1/presupuestos | ✅ OK |
| registroAuditoriaClient | /api/v1/registro-auditoria | ✅ OK |
| suplenciaClient | /api/v1/suplencia | ✅ OK |
| testimonioClient | /api/v1/testimonio | 🔴 500 |
| tipoDeDocumentoClient | /api/v1/tipo-de-documento | 🔴 500 |
| tipoDeFolioClient | /api/v1/tipo-folio | 🔴 500 |
| tipoDeTramiteClient | /api/v1/tipo-tramite | ✅ OK |
| tipoIdentificacionClient | /api/v1/tipo-identificacion | ✅ OK |
| tramiteClient | /api/v1/tramites | ✅ OK |
| usuarioClient | /api/v1/usuarios | ✅ OK |

**Resultado:** 5 de 25 módulos del frontend están BLOQUEADOS por errores 500 del backend.

---

## 4. Inventario Completo API (155 endpoints / 26 controllers)

Ver `CU-API-MATRIX.csv` para el mapeo completo CU ↔ Endpoint ↔ Test.

### Controllers con mayor cobertura OK
- PresupuestoController, GestionController, PersonaController, ConceptoController, TramiteController, UsuarioController — CRUD completo funcionando

### Controllers con problemas
| Controller | Problema | Issues afectados |
|-----------|---------|-----------------|
| TipoDeFolioController | GET devuelve 500 | CU36,40,58,68 |
| TipoDeDocumentoController | GET devuelve 500 | CU27,32,38,65 |
| EstadoDeGestionController | GET devuelve 500 | CU30,35,67 |
| TestimonioController | GET devuelve 500 | CU07,08,12,44 |
| MovimientoTestimonioController | GET devuelve 500 | CU10,12,44 |
| PlantillaPresupuestoController | ERROR concurrencia | CU39,49,55 |

---

## 5. Plan de Fases

### Fase 1 — Fix errores 500 (ALTA prioridad)

Investigar y corregir los 6 controllers con 500. Crear 1 issue + 1 branch + 1 PR por controller.

**Issues a crear:**
- fix/336_testimonio-movimiento-controller-500
- fix/337_tipo-folio-tipo-documento-estado-controller-500
- fix/338_escribanos-disponibles-500

### Fase 2 — Tests Bruno completos con fixtures

Completar tests POST/PUT/DELETE usando secuencias (POST→captura ID→PUT→DELETE):
- Todos los módulos sin test Bruno
- Tests de reportes (CU24, CU25, CU50)
- Tests de búsqueda (CU62)

### Fase 3 — CU de negocio (0% progress)

- CU15 (Procesar pago): revisar si falta lógica de negocio
- CU47 (Consultar Pago): documentar como cubierto

### Fase 4 — Errores de concurrencia

- CU39, CU49, CU52, CU55: reproducir error, documentar, corregir

### Fase 5 — CI / Coverage / Reportes finales

---

## 6. Issues Hijos a Crear

```
#336 fix: TestimonioController y MovimientoTestimonioController retornan 500
#337 fix: TipoDeFolioController, TipoDeDocumentoController, EstadoDeGestionController retornan 500
#338 fix: EscrituraController.getEscribanosDisponibles() retorna 500
#339 test: completar suite Bruno con tests POST/PUT/DELETE y fixtures
#340 test: agregar tests Bruno para reportes (CU24, CU25, CU50)
#341 test: agregar tests Bruno para endpoints sin cobertura (CU03, CU22, CU33, CU39, CU43, CU49, CU55, CU56, CU62)
#342 fix: errores de concurrencia en PlantillaPresupuesto (CU39, CU49, CU55)
#343 fix: errores de concurrencia en EscrituraController (CU52)
```

---

## 7. Archivos de Referencia

| Archivo | Descripción |
|---------|-------------|
| `docs/testing/CU-API-MATRIX.csv` | Matriz completa CU↔Endpoint↔Test↔Estado |
| `docs/testing/GAP-ANALYSIS.md` | Este documento |
| `docs/01-business/03_CU - Casos de Uso/` | Documentación de casos de uso |
| `backend-api/src/main/java/.../api/` | Controllers REST |
| `backend-api/api-test/` | Tests Bruno |
| `frontend-swing/src/.../servicios/AdministradorJpa.java` | Clientes REST del frontend |

---

*Generado el 15 de Abril de 2026 — branch `docs/335_cu-api-gap-analysis`*
