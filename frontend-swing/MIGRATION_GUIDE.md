# Guía de Migración de GUI a REST API

## Patrón de Migración

### 1. Reemplazar imports

**Antes:**
```java
import com.licensis.notaire.negocio.ControllerNegocio;
import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.jpa.exceptions.*;
```

**Después:**
```java
import com.licensis.notaire.servicios.AdministradorJpa;
import com.licensis.notaire.servicios.GenericRestClient;
import com.licensis.notaire.dto.GenericDto;
import java.io.IOException;
```

### 2. Reemplazar instancias

**Antes:**
```java
private ControllerNegocio miControllerNegocio = null;
miControllerNegocio = ControllerNegocio.getInstancia();
```

**Después:**
```java
private GenericRestClient conceptoClient = null;
conceptoClient = AdministradorJpa.getInstancia().getConceptoJpa();
```

### 3. Reemplazar operaciones CRUD

#### Crear (Create)
**Antes:**
```java
DtoConcepto dto = new DtoConcepto();
dto.setNombre("IVA");
dto.setValor(1000.0f);
Boolean resultado = miControllerNegocio.darAltaConcepto(dto);
```

**Después:**
```java
GenericDto dto = new GenericDto();
dto.put("nombre", "IVA");
dto.put("valor", 1000.0f);
try {
    conceptoClient.create(dto);
} catch (IOException e) {
    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
}
```

#### Leer (Read/FindAll)
**Antes:**
```java
List<DtoConcepto> lista = miControllerNegocio.obtenerListaConceptosDisponibles();
for (DtoConcepto dto : lista) {
    String nombre = dto.getNombre();
}
```

**Después:**
```java
try {
    List<GenericDto> lista = conceptoClient.findAll();
    for (GenericDto dto : lista) {
        String nombre = dto.getString("nombre");
    }
} catch (IOException e) {
    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
}
```

#### Actualizar (Update)
**Antes:**
```java
dtoConcepto.setNombre("Nuevo nombre");
Boolean resultado = miControllerNegocio.modificarConcepto(dtoConcepto);
```

**Después:**
```java
dto.put("nombre", "Nuevo nombre");
try {
    conceptoClient.edit(dto);
} catch (IOException e) {
    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
}
```

#### Eliminar (Delete)
**Antes:**
```java
Boolean resultado = miControllerNegocio.eliminarConcepto(dtoConcepto);
```

**Después:**
```java
try {
    Integer id = dto.getInt("idConcepto");
    conceptoClient.remove(id);
} catch (IOException e) {
    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
}
```

### 4. Mapeo de entidades a endpoints REST

| Entidad | Cliente REST | Endpoint |
|---------|--------------|----------|
| Concepto | `getConceptoJpa()` | `/conceptos` |
| Copia | `getCopiaJpa()` | `/copias` |
| DocumentoPresentado | `getDocumentoPresentadoJpa()` | `/documentos-presentados` |
| Escritura | `getEscrituraJpa()` | `/escrituras` |
| Folio | `getFolioJpa()` | `/folios` |
| Historial | `getHistorialJpa()` | `/historiales` |
| Inmueble | `getInmuebleJpa()` | `/inmuebles` |
| Item | `getItemJpa()` | `/items` |
| MovimientoTestimonio | `getMovimientoTestimonioJpa()` | `/movimientos-testimonios` |
| Pago | `getPagoJpa()` | `/pagos` |
| Presupuesto | `getPresupuestoJpa()` | `/presupuestos` |
| Suplencia | `getSuplenciaJpa()` | `/suplencias` |
| Testimonio | `getTestimonioJpa()` | `/testimonios` |
| TipoDeDocumento | `getTipoDeDocumentoJpa()` | `/tipos-documentos` |
| TipoIdentificacion | `getTipoIdentificacionJpa()` | `/tipos-identificacion` |
| Tramite | `getTramiteJpa()` | `/tramites` |

### 5. Conversión de DTOs específicos a GenericDto

**Campos comunes:**
- `idConcepto` → `dto.getInt("idConcepto")` / `dto.put("idConcepto", id)`
- `nombre` → `dto.getString("nombre")` / `dto.put("nombre", nombre)`
- `valor` → `dto.getFloat("valor")` / `dto.put("valor", valor)`
- `habilitado` → `dto.getBoolean("habilitado")` / `dto.put("habilitado", true)`

### 6. Manejo de excepciones

**Antes:**
```java
catch (PreexistingEntityException ex) { ... }
catch (ClassEliminatedException ex) { ... }
catch (ClassModifiedException ex) { ... }
catch (NonexistentEntityException ex) { ... }
```

**Después:**
```java
catch (IOException ex) {
    JOptionPane.showMessageDialog(this, 
        "Error al comunicarse con el servidor: " + ex.getMessage(), 
        "Error", 
        JOptionPane.ERROR_MESSAGE);
}
```

### 7. Ejemplo completo: IngresarConcepto

Ver `IngresarConcepto.java` como referencia completa.

## Estado de Migración

- ✅ IngresarConcepto - Completado
- ⏳ ModificarConcepto - Pendiente
- ⏳ EliminarConcepto - Pendiente
- ⏳ Resto de clases GUI - Pendiente

## Notas

1. Todos los DTOs específicos deben convertirse a GenericDto
2. Todas las excepciones JPA deben reemplazarse por IOException
3. Verificar que los nombres de campos coincidan con los del backend
4. Probar cada clase migrada con el backend en ejecución
