# Progreso de Migración del Módulo de Administración

## Estado Actual

**Total archivos en módulo administración**: 33
**Archivos migrados**: 23 (70%)
**Archivos pendientes**: 10 (30%)

## Archivos Migrados ✅

### Módulo Conceptos (3/3) - ✅ COMPLETO
- ✅ IngresarConcepto.java
- ✅ ModificarConcepto.java
- ✅ EliminarConcepto.java

### Módulo Documentos (3/3) - ✅ COMPLETO
- ✅ IngresarDocumento.java
- ✅ ModificarDocumento.java
- ✅ EliminarDocumento.java

### Módulo Escribanos (2/3) - ⏳ PARCIAL
- ✅ DarAltaEscribano.java
- ✅ RegistrarSuplencia.java
- ⏳ ConsultarSuplencias.java (migrado pero tiene referencia restante)

### Módulo Estados Gestión (2/2) - ✅ COMPLETO
- ✅ IngresarEstadoGestion.java
- ✅ ModificarEstadoGestion.java

### Módulo Folios (2/2) - ✅ COMPLETO
- ✅ IngresarTipoDeFolio.java
- ✅ ModificarEliminarFolio.java

## Archivos Pendientes (10)

### Módulo Escribanos
- ⏳ ConsultarSuplencias.java (necesita limpieza de referencias)

### Módulo Trámites (3)
- ⏳ IngresarTipoTramite.java (parcialmente migrado)
- ⏳ ModificarTipoTramite.java
- ⏳ EliminarTipoTramite.java

### Módulo Plantillas Presupuesto (3)
- ⏳ CrearPlantillaPresupuesto.java
- ⏳ ModificarPlantillaPresupuesto.java
- ⏳ EliminarPlantillaPresupuesto.java

### Módulo Usuarios (5)
- ⏳ DarAltaUsuario.java
- ⏳ ModificarUsuario.java
- ⏳ ActividadUsuario.java
- ⏳ ListarPersonasUsuario.java
- ⏳ VerRegistroActividadesUsuario.java

## Endpoints REST Necesarios en Backend

Los siguientes endpoints deben crearse en el backend (actualmente no existen):

1. **EstadoDeGestionController** - `/api/v1/estados-gestion`
2. **TipoDeFolioController** - `/api/v1/tipos-folio`
3. **TipoDeTramiteController** - `/api/v1/tipos-tramite`
4. **PlantillaPresupuestoController** - `/api/v1/plantillas-presupuesto`

## Notas

- Los clientes REST para Persona y Usuario ya están agregados en AdministradorJpa
- Algunas clases requieren manejo de relaciones complejas (PlantillasPresupuesto con Conceptos y Tramites)
- Las clases de Usuarios pueden requerir endpoints adicionales para búsquedas específicas
