# Estado de Migración Frontend-Swing

## Resumen General

- **Total archivos GUI**: 97
- **Archivos migrados**: 1 (IngresarConcepto)
- **Archivos pendientes**: 62 (que usan ControllerNegocio)
- **Formularios (.form)**: 95 copiados ✅
- **Recursos (iconos)**: Copiados ✅

## Archivos Migrados

### ✅ Completado
1. `IngresarConcepto.java` - Migrado a REST API usando GenericDto

## Archivos Pendientes de Migración

### Módulo Administración - Conceptos
- [ ] `ModificarConcepto.java`
- [ ] `EliminarConcepto.java`
- [ ] `Conceptos.java` (menú principal)

### Módulo Administración - Documentos
- [ ] `Documentos.java`
- [ ] `IngresarDocumento.java`
- [ ] `ModificarDocumento.java`
- [ ] `EliminarDocumento.java`

### Módulo Administración - Escribanos
- [ ] `Escribanos.java`
- [ ] `DarAltaEscribano.java`
- [ ] `RegistrarSuplencia.java`
- [ ] `ConsultarSuplencias.java`

### Módulo Administración - Estados Gestión
- [ ] `EstadosDeGestion.java`
- [ ] `IngresarEstadoGestion.java`
- [ ] `ModificarEstadoGestion.java`

### Módulo Administración - Folios
- [ ] `Folios.java`
- [ ] `IngresarTipoDeFolio.java`
- [ ] `ModificarEliminarFolio.java`

### Módulo Administración - Plantillas Presupuesto
- [ ] `PlantillasPresupuesto.java`
- [ ] `CrearPlantillaPresupuesto.java`
- [ ] `ModificarPlantillaPresupuesto.java`
- [ ] `EliminarPlantillaPresupuesto.java`

### Módulo Administración - Trámites
- [ ] `Tramites.java`
- [ ] `IngresarTipoTramite.java`
- [ ] `ModificarTipoTramite.java`
- [ ] `EliminarTipoTramite.java`

### Módulo Administración - Usuarios
- [ ] `Usuarios.java`
- [ ] `DarAltaUsuario.java`
- [ ] `ModificarUsuario.java`
- [ ] `ActividadUsuario.java`
- [ ] `ListarPersonasUsuario.java`
- [ ] `VerRegistroActividadesUsuario.java`

### Módulo Clientes
- [ ] `Clientes.java`
- [ ] `BuscarCliente.java`
- [ ] `DarAltaPersona.java`
- [ ] `ModificarCliente.java`
- [ ] `AdministrarCliente.java`
- [ ] `BuscarGestionesCliente.java`
- [ ] `ListarPersonas.java`

### Módulo Gestiones - Documentación
- [ ] `Documentacion.java`
- [ ] `IngresarDocumento.java`
- [ ] `ListarDocumentos.java`
- [ ] `RegistrarEntregaDocumentos.java`
- [ ] `ReingresarDocumentos.java`
- [ ] `ConsultarVencimientosDocumentos.java`
- [ ] `ConsultarDeudasDocumentos.java`
- [ ] `NomenclaturaCatastral.java`

### Módulo Gestiones - Escrituras
- [ ] `Escrituras.java`
- [ ] `PrepararEscritura.java`
- [ ] `BuscarEscritura.java`
- [ ] `DetalleEscritura.java`
- [ ] `ListaEscrituras.java`

### Módulo Gestiones - Gestión
- [ ] `Gestion.java`
- [ ] `IniciarGestion.java`
- [ ] `ModificarGestion.java`
- [ ] `DetalleGestion.java`
- [ ] `BuscarGestion.java`
- [ ] `ArchivarGestion.java`
- [ ] `VerHistorialGestion.java`
- [ ] `ListaGestionesCliente.java`

### Módulo Gestiones - Inscripciones
- [ ] `Inscripciones.java`
- [ ] `IngresarParaInscripcion.java`
- [ ] `RegistrarInscripcion.java`
- [ ] `RegistrarReingreso.java`

### Módulo Gestiones - Testimonios
- [ ] `Testimonios.java`
- [ ] `GenerarTestimonio.java`
- [ ] `VerificarTestimonio.java`
- [ ] `RetirarTestimonio.java`

### Módulo Presupuestos
- [ ] `Presupuestos.java`
- [ ] `CrearPresupuesto.java`
- [ ] `ModificarPresupuesto.java`
- [ ] `BuscarPresupuesto.java`
- [ ] `BuscarInmueble.java`
- [ ] `DetalleInmueble.java`
- [ ] `DetalleValoresTramites.java`
- [ ] `ListaPersonasPresupuesto.java`
- [ ] `ListaPresupuestosCliente.java`
- [ ] `ListaPresupuestosClientesSinGestion.java`

### Módulo Pagos
- [ ] `Pagos.java`
- [ ] `RegistrarPago.java`
- [ ] `ConsultarPagos.java`

### Módulo Protocolo
- [ ] `Protocolo.java`
- [ ] `Folios.java`
- [ ] `IngresarFolios.java`
- [ ] `ModificarFolio.java`
- [ ] `GenerarIndices.java`
- [ ] `GenerarDDJJ.java`
- [ ] `DeclaracionJurada.java`
- [ ] `DeclaracionJuradaRentas.java`

## Endpoints REST Disponibles en Backend

✅ ConceptoController - `/api/v1/conceptos`
✅ CopiaController - `/api/v1/copias`
✅ DocumentoPresentadoController - `/api/v1/documentos-presentados`
✅ EscrituraController - `/api/v1/escrituras`
✅ FolioController - `/api/v1/folio`
✅ HistorialController - `/api/v1/historiales`
✅ InmuebleController - `/api/v1/inmuebles`
✅ ItemController - `/api/v1/items`
✅ MovimientoTestimonioController - `/api/v1/movimientos-testimonios`
✅ PagoController - `/api/v1/pagos`
✅ PresupuestoController - `/api/v1/presupuestos`
✅ SuplenciaController - `/api/v1/suplencias`
✅ TestimonioController - `/api/v1/testimonios`
✅ TipoDeDocumentoController - `/api/v1/tipos-documentos`
✅ TipoIdentificacionController - `/api/v1/tipos-identificacion`
✅ TramiteController - `/api/v1/tramites`
✅ PersonaController - `/api/v1/personas`
✅ UsuarioController - `/api/v1/usuarios`

## Próximos Pasos

1. Continuar migrando clases módulo por módulo
2. Verificar coherencia de campos entre DTOs y GenericDto
3. Probar cada módulo migrado con el backend
4. Actualizar documentación

## Notas

- Todas las clases deben usar `GenericDto` en lugar de DTOs específicos
- Todas las excepciones JPA deben reemplazarse por `IOException`
- Ver `MIGRATION_GUIDE.md` para el patrón de migración detallado
