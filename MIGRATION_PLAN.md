# Plan de Migración: Monolito → Microservicios + Kubernetes Local

## Estado de la Migración (27/02/2026)

### Resumen Ejecutivo

| Componente | Estado | Detalle |
|------------|--------|---------|
| **Backend API** | ~98% | APIs críticas listas; few endpoints específicos pendientes |
| **Swing Forms** | ~87% | 16 formularios aún usan ControllerNegocio (~105/120 migrados) |
| **Reportes PDF** | ✅ Listo | ReporteController con 10 endpoints JasperReports |
| **Docker Compose** | ✅ Listo | postgres + backend + pgadmin |
| **Kubernetes** | ❌ Pendiente | No existe configuración K8s |
| **Tests E2E** | Parcial | Shell tests; JUnit domain tests |

---

## Formularios Migrados (Completados)

### Batch A - Usuarios ✅ (21/02/2026)
- [x] `ActividadUsuario.java` - Migrado a REST
- [x] `DarAltaUsuario.java` - Migrado a REST
- [x] `ListarPersonasUsuario.java` - Migrado a REST
- [x] `ModificarUsuario.java` - Parcial (falta validación contraseña)
- [x] `VerRegistroActividadesUsuario.java` - Migrado a REST

### Batch B - Clientes ✅ (21/02/2026)
- [x] `Clientes.java` - Migrado a REST
- [x] `BuscarCliente.java` - Migrado a REST
- [x] `DarAltaPersona.java` - Migrado a REST
- [x] `AdministrarCliente.java` - Migrado a REST
- [x] `ListarPersonas.java` - Migrado a REST
- [x] `ModificarCliente.java` - Migrado a REST
- [x] `BuscarGestionesCliente.java` - Migrado a REST

### Administración - Catálogos ✅
- [x] `IngresarConcepto.java` - Migrado a REST
- [x] `ModificarConcepto.java` - Migrado a REST
- [x] `EliminarConcepto.java` - Migrado a REST
- [x] `IngresarDocumento.java` - Migrado a REST
- [x] `ModificarDocumento.java` - Migrado a REST
- [x] `EliminarDocumento.java` - Migrado a REST
- [x] `DarAltaEscribano.java` - Migrado a REST
- [x] `ConsultarSuplencias.java` - Migrado a REST
- [x] `RegistrarSuplencia.java` - Migrado a REST
- [x] `IngresarEstadoGestion.java` - Migrado a REST
- [x] `ModificarEstadoGestion.java` - Migrado a REST
- [x] `IngresarTipoDeFolio.java` - Migrado a REST
- [x] `ModificarEliminarFolio.java` - Migrado a REST
- [x] `IngresarTipoTramite.java` - Migrado a REST
- [x] `ModificarTipoTramite.java` - Migrado a REST
- [x] `EliminarTipoTramite.java` - Migrado a REST

### Pagos ✅
- [x] `RegistrarPago.java` - Migrado a REST
- [x] `ConsultarPagos.java` - Migrado a REST

### Presupuestos ✅
- [x] `BuscarPresupuesto.java` - Migrado a REST
- [x] `ModificarPresupuesto.java` - Migrado a REST
- [x] `ListaPresupuestosCliente.java` - Migrado a REST
- [x] `ListaPresupuestosClientesSinGestion.java` - Migrado a REST
- [x] `ListaPersonasPresupuesto.java` - Migrado a REST

### Gestiones ✅ (27/02/2026)
- [x] `ArchivarGestion.java` - Migrado a REST
- [x] `VerHistorialGestion.java` - Migrado a REST
- [x] `BuscarGestion.java` - Migrado a REST ✅ (27/02/2026)
- [x] `ListaGestionesCliente.java` - Migrado a REST ✅ (27/02/2026)
- [x] `ModificarGestion.java` - Migrado a REST ✅ (27/02/2026)
- [x] `IniciarGestion.java` - Migrado a REST ✅ (27/02/2026)
- [x] `DetalleGestion.java` - Migrado a REST ✅ (27/02/2026)

---

## Formularios Pendientes (por Migrar)

### Batch C - Gestiones ✅ COMPLETADO (27/02/2026)
- [x] `BuscarGestion.java` - Migrado a REST
- [x] `ListaGestionesCliente.java` - Migrado a REST
- [x] `ModificarGestion.java` - Migrado a REST
- [x] `IniciarGestion.java` -
- [x] `DetalleGestion.java` - Migrado a REST Migrado a REST

### Batch D - Escrituras
- [ ] `BuscarEscritura.java` - ControllerNegocio
- [ ] `ListaEscrituras.java` - ControllerNegocio
- [ ] `DetalleEscritura.java` - ControllerNegocio
- [ ] `PrepararEscritura.java` - Por verificar

### Batch E - Testimonios
- [ ] `GenerarTestimonio.java` - ControllerNegocio
- [ ] `VerificarTestimonio.java` - ControllerNegocio
- [ ] `RetirarTestimonio.java` - ControllerNegocio

### Batch F - Inscripciones
- [ ] `IngresarParaInscripcion.java` - ControllerNegocio
- [ ] `RegistrarInscripcion.java` - ControllerNegocio
- [ ] `RegistrarReingreso.java` - ControllerNegocio

### Batch G - Presupuestos
- [ ] `CrearPresupuesto.java` - ControllerNegocio
- [ ] `DetalleValoresTramites.java` - ControllerNegocio
- [ ] `BuscarInmueble.java` - ControllerNegocio

### Batch H - Protocolo
- [ ] `ModificarFolio.java` - ControllerNegocio
- [ ] `IngresarFolios.java` - ControllerNegocio
- [ ] `GenerarIndices.java` - Por verificar
- [ ] `GenerarDDJJ.java` - Por verificar
- [ ] `DeclaracionJurada.java` - Por verificar
- [ ] `DeclaracionJuradaRentas.java` - Por verificar

### Batch I - Plantillas Presupuesto
- [ ] `CrearPlantillaPresupuesto.java` - ControllerNegocio
- [ ] `ModificarPlantillaPresupuesto.java` - ControllerNegocio

### Batch J - Servicios Auxiliares
- [ ] `AdministradorValidaciones.java` - isPasswordCorrect → API

---

## APIs faltantes o por extender

| API | Uso | Estado |
|-----|-----|--------|
| `GET /personas/buscar?esCliente=true` | Clientes | ✅ Implementado |
| `GET /usuarios?nombre=X` | Buscar usuario por nombre | ❌ Pendiente |
| `GET /gestiones/{id}/estado-actual` | ListaGestionesCliente | ❌ Pendiente |
| `POST /usuarios/validate-password` | AdministradorValidaciones | ❌ Pendiente |

---

## Recomendaciones de implementación

### Migración Swing → REST (por formulario)

1. Identificar todas las llamadas a `ControllerNegocio.*` en el formulario
2. Mapear cada llamada a un endpoint REST existente o crearlo
3. Usar `AdministradorJpa.getInstancia().getXxxJpa()` y `GenericRestClient`
4. Usar `RestMapper.asociarFkTipoIdentificacion(nombre)` para tipos de identificación
5. Para búsquedas: usar `personaClient` con `findAll()` y filtrar
6. Reemplazar `ControllerNegocio` por código REST
7. Probar con `bash scripts/start.sh --no-frontend --no-admin`

### Próximo Batch a Migrar: Gestiones (Batch C)

**Formularios objetivo:**
1. `BuscarGestion.java` - Usado frecuentemente
2. `ListaGestionesCliente.java` - Usado frecuentemente
3. `ModificarGestion.java`
4. `DetalleGestion.java`
5. `IniciarGestion.java` - Parcialmente migrado

**APIs necesarias:**
- Endpoint para buscar gestión por cliente
- Endpoint para obtener estado actual de gestión

---

## Despliegue en Kubernetes (Pendiente)

### Checklist pre-Kubernetes

- [ ] Dockerfile.backend funcional y optimizado
- [ ] Variables de entorno externalizadas
- [ ] Health endpoint `/actuator/health` respondiendo
- [ ] PostgreSQL con datos iniciales
- [ ] Tests E2E pasando contra backend en Docker

---

## Criterios de finalización

1. **0 usos** de `ControllerNegocio` en formularios Swing
2. **100%** formularios usando REST vía `AdministradorJpa` / `GenericRestClient`
3. **Todas** las APIs necesarias implementadas y documentadas en Swagger
4. **Stack** desplegable con `kubectl apply -f k8s/` en entorno local
5. **Tests** `bash scripts/test.sh` en verde

---

## Próximos pasos inmediatos

1. ~~Implementar APIs faltantes~~ (parcialmente completado)
2. Migrar Batch C - Gestiones
3. Crear estructura base `k8s/`
4. Validar despliegue local

---

## APIs añadidas para Gestiones (Batch C)

### APIs existentes utilizadas
- `GET /api/v1/personas/buscar?nombre=X&apellido=Y&esCliente=true` - Buscar personas con gestión
- `GET /api/v1/tipo-identificacion` - Listar tipos de identificación

### Nuevas APIs creadas
- `GET /api/v1/gestiones/{id}/estado-actual` - Obtener estado actual de una gestión (para ListaGestionesCliente)
- `GET /api/v1/gestiones/{id}/cliente-referencia` - Obtener cliente referencia de una gestión (para DetalleGestion)

### Estado actual
- APIs de Gestiones expandidas ✅
- Formularios en proceso de migración 🔄

---

## Próximo paso: Migrar formulario BuscarGestion.java

El formulario BuscarGestion usa:
1. `ControllerNegocio.listarTiposIdentificacion()` → `tipoIdentificacionClient.findAll()`
2. `buscarPersonaNombreApellidoConGestion()` → `personaClient.findAllByPath("buscar?nombre=X&apellido=Y&esCliente=true")`
3. `asociarFkTipoIdentificacion()` → `RestMapper.asociarFkTipoIdentificacion()`
4. `buscarPersonaTipoNumeroIdentificacionConGestion()` → `personaClient.findAllByPath("buscar?numeroIdentificacion=X&idTipoIdentificacion=Y")`

---

## Estado Actual: Batch C en Progreso

### APIs añadidas para Gestiones
- `GET /api/v1/gestiones/{id}/estado-actual` - Obtener estado actual de una gestión ✅
- `GET /api/v1/gestiones/{id}/cliente-referencia` - Obtener cliente referencia ✅

### Formularios Gestiones restantes (4):
1. **ListaGestionesCliente.java** - Usa `ControllerNegocio.obtenerEstadoActualDeGestion()` → API `/gestiones/{id}/estado-actual` ya existe
2. **ModificarGestion.java** - Necesita analizar métodos usados
3. **IniciarGestion.java** - Necesita analizar métodos usados
4. **DetalleGestion.java** - Necesita analizar métodos usados

---

## Próximo Batch Propuesto: Batch D - Escrituras

### Formularios objetivo:
- `BuscarEscritura.java`
- `ListaEscrituras.java`
- `DetalleEscritura.java`
- `PrepararEscritura.java` (por verificar)

### APIs necesarias:
- Verificar que existen endpoints para escrituras
- Posiblemente endpoint para buscar escritura por número

---

## Archivos Pendientes Totales (20 archivos)

| Módulo | Cantidad | Archivos |
|--------|----------|----------|
| Gestiones | 4 | ListaGestionesCliente, ModificarGestion, IniciarGestion, DetalleGestion |
| Escrituras | 4 | BuscarEscritura, ListaEscrituras, DetalleEscritura, PrepararEscritura |
| Testimonios | 3 | GenerarTestimonio, VerificarTestimonio, RetirarTestimonio |
| Inscripciones | 3 | IngresarParaInscripcion, RegistrarInscripcion, RegistrarReingreso |
| Presupuestos | 3 | CrearPresupuesto, DetalleValoresTramites, BuscarInmueble |
| Protocolo | 2 | ModificarFolio, IngresarFolios |
| Plantillas | 2 | CrearPlantillaPresupuesto, ModificarPlantillaPresupuesto |

---

*Última actualización: 27 de Febrero de 2026*
