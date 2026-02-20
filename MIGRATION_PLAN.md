# Plan de Migración: Monolito → Microservicios

## Estado Actual
- **Swing (evaluación técnica actual)**: 97 clases GUI totales, 44 clases aún con acoplamiento legacy (`ControllerNegocio`/JPA directo en GUI)
- **Backend/API**: Endpoints críticos agregados (`/api/v1/items/presupuesto/{idPresupuesto}`, `/api/v1/pagos/presupuesto/{idPresupuesto}`, `/api/v1/gestiones/*`, búsqueda extendida en `/api/v1/personas/buscar`)
- **Testing**: Suite `mvn test -pl backend-api` en verde (unit + integración H2), tests Testcontainers preparados (ejecución condicionada a compatibilidad Docker host)
- **E2E HTTP**: `bash scripts/test.sh` en verde con aserciones estrictas
- **Docker backend**: stack levanta en modo headless (`bash scripts/start.sh --no-frontend --no-admin --skip-build`)
- **Última actualización**: 20/02/2026

---

## Resumen de Progreso

### Componentes Críticos Completados

#### Módulo de Pagos (FASE 2.1) - COMPLETADO
**Fecha**: 19/02/2026

- [x] Endpoint REST `/api/v1/pagos` corregido y verificado
- [x] `RegistrarPago.java` - Migrado a REST API
  - Integración con `GenericRestClient` 
  - Validaciones de negocio (monto > 0, no exceder saldo)
  - Carga de presupuestos e items desde backend
  - Cálculo automático de saldos
  - Gestión de pagos parciales y totales
- [x] `ConsultarPagos.java` - Migrado a REST API
  - Consulta de pagos por presupuesto
  - Visualización en grilla
  - Cálculo de saldos pendientes

**Código de referencia**: Ver `RegistrarPago.java` líneas 58-200 para patrón de integración REST

---

## Fase 1: Infraestructura Crítica (Sprint 0) - EN PROGRESO

### 1.1 Backend API
- [x] Crear endpoints REST para pagos (`/api/v1/pagos`) - **COMPLETADO**
- [ ] Crear endpoints para reportes (`/api/v1/reportes`) - SIGUIENTE PRIORIDAD
- [ ] Implementar generación de PDFs con JasperReports en backend
- [ ] Agregar seguridad JWT a endpoints sensibles

### 1.2 Base de Datos
- [ ] Verificar integridad de datos PostgreSQL
- [ ] Crear índices optimizados para consultas frecuentes
- [ ] Configurar backups automáticos

**Criterio de aceptación**: Backend expone todos los endpoints necesarios para formularios pendientes.

---

## Fase 2: Formularios Críticos (Sprints 1-2)

### 2.1 Pagos (Alta prioridad) - ✅ COMPLETADO
**Archivos**: `RegistrarPago.java`, `ConsultarPagos.java`

**Estado**: ✅ Migración completada el 19/02/2026

**Implementación**:
```java
// Cliente REST inicializado en constructor
pagoClient = AdministradorJpa.getInstancia().getPagoJpa();
presupuestoClient = AdministradorJpa.getInstancia().getPresupuestoJpa();
itemClient = AdministradorJpa.getInstancia().getItemJpa();

// Crear pago
GenericDto pagoDto = new GenericDto();
pagoDto.put("monto", montoPago);
pagoDto.put("fecha", fechaPago);
pagoDto.put("observaciones", observaciones);
pagoDto.put("fkIdPresupuesto", presupuestoDto);
pagoClient.create(pagoDto);

// Cargar presupuesto
GenericDto presupuesto = presupuestoClient.find(idPresupuesto);
```

**Funcionalidades implementadas**:
- [x] Registro de pagos con validación de montos
- [x] Consulta de pagos por presupuesto
- [x] Cálculo automático de saldos
- [x] Carga dinámica de items del presupuesto
- [x] Manejo de errores con JOptionPane
- [x] Validaciones: monto > 0, no exceder saldo, formato de fecha

### 2.2 Reportes (Bloqueante) - SIGUIENTE PRIORIDAD
**Archivo**: `AdministradorReportes.java`

**Estado**: 🔄 Pendiente - Requiere migración de JasperReports al backend

**Impacto**: ALTO - Los recibos de pago y reportes legales dependen de esta funcionalidad

**Estrategia**:
1. Crear servicio de reportes en backend (`ReporteService.java`)
2. Mover templates .jrxml a `backend-api/src/main/resources/reports/`
3. Exponer endpoint REST `/api/v1/reportes/{tipo}`
4. Frontend solicita reporte vía REST (POST/GET)
5. Backend genera PDF y devuelve bytes o URL temporal
6. Frontend descarga o muestra en visor PDF

```java
// Nuevo endpoint backend
@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {
    
    @GetMapping("/{tipo}")
    public ResponseEntity<byte[]> generarReporte(
            @PathVariable String tipo,
            @RequestParam Map<String, Object> parametros) {
        // JasperReports en servidor
        byte[] pdfBytes = reporteService.generarPdf(tipo, parametros);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte.pdf\"")
            .body(pdfBytes);
    }
}

// Frontend - Descargar reporte
GenericRestClient reporteClient = new GenericRestClient("/reportes");
byte[] pdfData = reporteClient.getPdf("recibo-pago", parametros);
// Guardar archivo o mostrar
```

**Tareas detalladas**:
- [ ] Identificar todos los templates .jrxml usados
  - Recibo de pago
  - Reporte de gestiones
  - Reporte de clientes
  - Declaraciones juradas
- [ ] Crear `ReporteService` en backend
- [ ] Migrar templates .jrxml a `backend-api/src/main/resources/reports/`
- [ ] Configurar JasperReports con datasource PostgreSQL
- [ ] Crear `ReporteController` con endpoints REST
- [ ] Crear `ReporteRestClient` en frontend
- [ ] Actualizar `AdministradorReportes.java` para usar API
- [ ] Eliminar dependencia JDBC del frontend
- [ ] Implementar cache de reportes (opcional - fase 2)
- [ ] Tests de generación de PDFs

**Dependencias**: 
- Requiere Fase 2.1 completada (Pagos) ✅
- Bloquea: Impresión de recibos en RegistrarPago

---

## Fase 3: Gestión de Datos (Sprints 3-4)

### 3.1 Clientes
**Archivo**: `ModificarCliente.java`

**Pasos**:
1. Agregar endpoint PUT `/api/v1/personas/{id}`
2. Reemplazar `ControllerNegocio.modificarPersona()`
3. Usar `GenericRestClient` o `PersonaRestClient`

### 3.2 Protocolo
**Archivos**: `DeclaracionJurada.java`, `GenerarDDJJ.java`, etc.

**Enfoque**:
- Crear módulo `protocolo` en backend
- Migrar lógica de folios y declaraciones
- Integrar con módulo existente de testimonios

---

## Fase 4: Refactorización de Menús (Sprint 5)

### 4.1 Menús Contenedores
**Archivos**: Todos los `*.java` que son solo menús

**Estrategia**: No migrar funcionalidad, solo estructura
- Mantener como contenedores de navegación
- Asegurar que carguen formularios migrados
- Eliminar referencias a clases eliminadas

```java
// Ejemplo: Administracion.java
// No necesita cambios mayores, solo verificar imports
```

---

## Fase 5: Componentes UI (Sprint 6)

### 5.1 Componentes Auxiliares
**Archivos**: `BarraProgreso.java`, `CartelConstruccion.java`

**Acción**: Mantener sin cambios (no tienen lógica de negocio)

---

## Checklist de Migración por Formulario

### Template para cada formulario:
```markdown
### Formulario: [Nombre]
- [ ] 1. Identificar dependencias (JPA, ControllerNegocio, etc.)
- [ ] 2. Crear/verificar endpoints REST en backend
- [ ] 3. Reemplazar imports de jpa.* por servicios.*
- [ ] 4. Migrar lógica de eventos (actionPerformed)
- [ ] 5. Usar GenericRestClient para operaciones CRUD
- [ ] 6. Manejar errores con try-catch + JOptionPane
- [ ] 7. Tests manuales
- [ ] 8. Eliminar código comentado y TODOs
```

---

## Convenciones de Código

### Patrón RestClient
```java
// Correcto
private GenericRestClient cliente = AdministradorJpa.getInstancia().getXxxJpa();

// Evento
try {
    GenericDto dto = new GenericDto();
    dto.put("campo", valor);
    cliente.create(dto);
} catch (IOException ex) {
    logger.log(Level.SEVERE, "Error", ex);
    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
}
```

### Nomenclatura Endpoints
- GET `/api/v1/{recurso}` - Listar
- GET `/api/v1/{recurso}/{id}` - Obtener
- POST `/api/v1/{recurso}` - Crear
- PUT `/api/v1/{recurso}/{id}` - Actualizar
- DELETE `/api/v1/{recurso}/{id}` - Eliminar

---

## Comandos Útiles

```bash
# Verificar compilación
mvn clean install -DskipTests

# Ejecutar tests
bash test.sh

# Iniciar sistema completo
bash start.sh

# Ver logs
./logs.sh backend
```

---

## Métricas de Éxito

### Objetivos Generales
1. **0 errores de compilación** en frontend-swing
2. **0 dependencias** de `com.licensis.notaire.jpa` en frontend
3. **100% formularios** usando DTOs + RestClient
4. **Tests HTTP** pasando para todos los endpoints
5. **Sin conexiones JDBC** directas desde frontend

### Métricas Actuales (19/02/2026)
- ✅ Backend: 21/21 endpoints REST funcionando
- ✅ Frontend: 69/97 formularios migrados (71%)
- ✅ Módulo Pagos: 2/2 formularios migrados (100%)
- ⏳ Reportes: 0/1 completados (pendiente - bloqueante)
- ⏳ Pendientes: 28 formularios

---

## Timeline Actualizado

### Completado ✅
- **Fase 2.1 (Pagos)**: ~3 días - **COMPLETADO** (19/02/2026)

### En Progreso 🔄
- **Fase 2.2 (Reportes)**: ~1 semana - **INICIANDO**
  - Día 1-2: Migrar templates JasperReports
  - Día 3-4: Implementar ReporteService y Controller
  - Día 5: Integrar con frontend y tests

### Pendiente ⏳
- **Fase 1 (Infraestructura restante)**: 3 días
  - JWT Security
  - Optimización de base de datos
- **Fase 3 (Gestión de Datos)**: 2 semanas
  - Clientes: 6 formularios
  - Protocolo: 7 formularios
- **Fase 4 (Menús)**: 1 semana
- **Fase 5 (UI Components)**: 3 días

**Timeline revisado**: 
- **Trabajo completado**: ~30%
- **Tiempo restante estimado**: ~5 semanas (1 desarrollador full-time)
- **Fecha estimada de finalización**: 26/03/2026

---

## Lecciones Aprendidas - Fase 2.1 (Pagos)

### Patrones Exitosos

#### 1. Inicialización de Clientes REST
```java
// En constructor del formulario
pagoClient = AdministradorJpa.getInstancia().getPagoJpa();
presupuestoClient = AdministradorJpa.getInstancia().getPresupuestoJpa();
itemClient = AdministradorJpa.getInstancia().getItemJpa();
```

#### 2. Manejo de Relaciones entre Entidades
```java
// Crear DTO relacionado (Presupuesto dentro de Pago)
GenericDto presupuestoDto = new GenericDto();
presupuestoDto.put("idPresupuesto", idPresupuestoActual);
pagoDto.put("fkIdPresupuesto", presupuestoDto);
```

#### 3. Carga de Datos Relacionados
```java
// Iterar resultados y filtrar por relación
List<GenericDto> items = itemClient.findAll();
for (GenericDto item : items) {
    Object presupuesto = item.get("fkIdPresupuesto");
    if (presupuesto instanceof GenericDto) {
        Object idPres = ((GenericDto) presupuesto).get("idPresupuesto");
        if (idPres != null && idPres.equals(idPresupuesto)) {
            // Procesar item
        }
    }
}
```

#### 4. Validaciones de Negocio en Frontend
```java
// Validar antes de enviar al backend
float montoPago = Float.parseFloat(importeTexto);
if (montoPago <= 0) {
    JOptionPane.showMessageDialog(this, "El importe debe ser mayor a cero");
    return;
}
float saldoActual = Float.parseFloat(campoSaldo.getText());
if (montoPago > saldoActual) {
    JOptionPane.showMessageDialog(this, "El importe no puede superar el saldo");
    return;
}
```

### Desafíos Encontrados

1. **Inconsistencia de nombres de endpoints**: Algunos controllers usaban singular (`/pago`) en lugar de plural (`/pagos`)
   - **Solución**: Estandarizar a plural siguiendo convenciones REST
   
2. **Método find() vs findById()**: El BaseRestClient usa `find(Object id)` no `findById()`
   - **Solución**: Usar siempre `client.find(id)`

3. **Conversión de fechas**: El backend espera objetos Date, frontend trabaja con String
   - **Solución**: Usar SimpleDateFormat para parseo consistente

### Mejoras Identificadas

1. **Endpoint específico para consultas**: En lugar de cargar todos los items y filtrar en frontend, debería existir:
   ```
   GET /api/v1/items/presupuesto/{idPresupuesto}
   GET /api/v1/pagos/presupuesto/{idPresupuesto}
   ```
   
2. **Validaciones duplicadas**: Validaciones de negocio (monto <= saldo) deberían estar en backend también

3. **Manejo de transacciones**: Múltiples operaciones deberían ser atómicas (ej: registrar pago + actualizar saldo)

---

## Próximos Pasos Inmediatos

### Prioridad 1: Reportes (Esta semana)
- [ ] Crear `ReporteService.java` en backend
- [ ] Migrar templates .jrxml
- [ ] Implementar endpoint `/api/v1/reportes/recibo-pago`
- [ ] Integrar con `RegistrarPago.java` para impresión de recibos

### Prioridad 2: Optimización de Consultas (Próxima semana)
- [ ] Agregar endpoints específicos para consultas frecuentes
- [ ] Implementar paginación en listados grandes
- [ ] Agregar caché en backend para datos de catálogo

### Prioridad 3: Seguridad
- [ ] Implementar JWT en endpoints sensibles (pagos, reportes)
- [ ] Agregar autorización por rol de usuario
- [ ] Auditar operaciones críticas

---

## Notas para Agentes AI

1. **Siempre verificar** que el backend expone el endpoint antes de migrar el frontend
2. **No modificar** lógica de negocio, solo moverla
3. **Mantener compatibilidad** con UI existente
4. **Documentar** cambios en AGENTS.md
5. **Probar** con `bash start.sh` después de cada migración
6. **Seguir patrones** documentados en "Lecciones Aprendidas"
7. **Actualizar este plan** después de completar cada fase
