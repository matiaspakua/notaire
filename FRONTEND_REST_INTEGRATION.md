# Frontend Swing + API REST Integration

## Overview

El frontend Swing de Notaire ha sido modernizado para comunicarse con la API REST del backend en lugar de acceder directamente a la base de datos mediante JPA. Esta arquitectura desacopla la presentación de la persistencia, permitiendo:

1. **Separación de responsabilidades** - El frontend solo maneja la interfaz de usuario
2. **Escalabilidad** - Múltiples clientes pueden usar la misma API
3. **Mantenibilidad** - Cambios en la base de datos no afectan el frontend
4. **Flexibilidad futura** - Fácil crear nuevos clientes (web, móvil, etc.)

## Architecture

### Componentes principales:

```
┌─────────────────────────────────────────────────────────┐
│           Frontend Swing (GUI)                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │  GUIs (Login, Administración, Gestiones, etc)     │  │
│  └─────────────────┬─────────────────────────────────┘  │
│                    │                                      │
│  ┌─────────────────▼─────────────────────────────────┐  │
│  │  AdministradorJpa (REST Proxy)                    │  │
│  │  - Singleton que proporciona acceso a clientes    │  │
│  └─────────────────┬─────────────────────────────────┘  │
│                    │                                      │
│  ┌─────────────────▼─────────────────────────────────┐  │
│  │  GenericRestClient (16 instancias, una por        │  │
│  │  entidad: Concepto, Copia, Escritura, etc)       │  │
│  └─────────────────┬─────────────────────────────────┘  │
│                    │                                      │
│  ┌─────────────────▼─────────────────────────────────┐  │
│  │  BaseRestClient<GenericDto>                       │  │
│  │  - Implementa operaciones CRUD estándar           │  │
│  └─────────────────┬─────────────────────────────────┘  │
│                    │                                      │
│  ┌─────────────────▼─────────────────────────────────┐  │
│  │  RestClient (HTTP + Jackson JSON)                 │  │
│  │  - Comunicación HTTP con el backend               │  │
│  └─────────────────┬─────────────────────────────────┘  │
│                    │                                      │
└────────────────────┼──────────────────────────────────────┘
                     │ HTTP (JSON)
                     ▼
┌─────────────────────────────────────────────────────────┐
│           Backend API (Spring Boot 3.2.9)               │
│           http://localhost:8080/api/v1/                 │
└─────────────────────────────────────────────────────────┘
```

## Capa de abstracción REST

### 1. `GenericDto` - Objeto de transferencia de datos

Reemplaza los DTOs específicos complejos con un Map genérico:

```java
GenericDto dto = new GenericDto();
dto.put("idConcepto", 1);
dto.put("nombre", "IVA");
dto.put("valor", 1000.0f);

// Acceso type-safe
Integer id = dto.getInt("idConcepto");
String nombre = dto.getString("nombre");
```

**Ventajas:**
- No requiere recompilación para nuevos campos
- Flexible para cambios en la API
- Serialización/deserialización automática con Jackson

### 2. `BaseRestClient<T>` - Cliente REST genérico

Proporciona operaciones CRUD estándar:

```java
GenericRestClient conceptoClient = AdministradorJpa.getInstancia().getConceptoJpa();

// Listar todos
List<GenericDto> conceptos = conceptoClient.findAll();

// Obtener uno
GenericDto concepto = conceptoClient.find(1);

// Crear
GenericDto newConcepto = new GenericDto();
newConcepto.put("nombre", "ISR");
newConcepto.put("valor", 500.0f);
conceptoClient.create(newConcepto);

// Actualizar
concepto.put("valor", 1100.0f);
conceptoClient.edit(concepto);

// Eliminar
conceptoClient.remove(1);
```

### 3. `RestClient` - Implementación HTTP

Usa `HttpURLConnection` (incluido en Java) y Jackson para JSON:

```java
// GET /api/v1/conceptos
List<GenericDto> lista = RestClient.getList("/conceptos", GenericDto.class);

// POST /api/v1/conceptos
GenericDto created = RestClient.post("/conceptos", dto, GenericDto.class);

// PUT /api/v1/conceptos/1
GenericDto updated = RestClient.put("/conceptos/1", dto, GenericDto.class);

// DELETE /api/v1/conceptos/1
RestClient.delete("/conceptos/1");
```

### 4. `AdministradorJpa` - Acceso centralizado

Singleton que proporciona acceso a todos los clientes REST:

```java
AdministradorJpa admin = AdministradorJpa.getInstancia();

// Acceso a clientes específicos
GenericRestClient conceptoClient = admin.getConceptoJpa();
GenericRestClient tramiteClient = admin.getTramiteJpa();
GenericRestClient escrituraClient = admin.getEscrituraJpa();
// ... etc para los 16 clientes

// Probar conectividad
admin.testConexion(); // Throws RuntimeException si no hay conexión
```

## Endpoints disponibles

El frontend puede acceder a 16 endpoints RESTful:

| Entidad | Endpoint | Método |
|---------|----------|--------|
| Concepto | `/conceptos` | GET, POST, PUT, DELETE |
| Copia | `/copias` | GET, POST, PUT, DELETE |
| Documento Presentado | `/documentos-presentados` | GET, POST, PUT, DELETE |
| Escritura | `/escrituras` | GET, POST, PUT, DELETE |
| Folio | `/folios` | GET, POST, PUT, DELETE |
| Historial | `/historiales` | GET, POST, PUT, DELETE |
| Inmueble | `/inmuebles` | GET, POST, PUT, DELETE |
| Item | `/items` | GET, POST, PUT, DELETE |
| Movimiento Testimonio | `/movimientos-testimonios` | GET, POST, PUT, DELETE |
| Pago | `/pagos` | GET, POST, PUT, DELETE |
| Presupuesto | `/presupuestos` | GET, POST, PUT, DELETE |
| Suplencia | `/suplencias` | GET, POST, PUT, DELETE |
| Testimonio | `/testimonios` | GET, POST, PUT, DELETE |
| Tipo de Documento | `/tipos-documentos` | GET, POST, PUT, DELETE |
| Tipo Identificación | `/tipos-identificacion` | GET, POST, PUT, DELETE |
| Trámite | `/tramites` | GET, POST, PUT, DELETE |

## Configuración de la API

### URL base predeterminada

```java
// Predeterminado: http://localhost:8080/api/v1
ApiConfig.getApiBaseUrl(); // "http://localhost:8080/api/v1"
```

### Cambiar la URL en tiempo de ejecución

```java
// Opción 1: Directamente en código
ApiConfig.setApiBaseUrl("http://192.168.1.100:8080/api/v1");

// Opción 2: Mediante propiedades del sistema
System.setProperty("api.base.url", "http://192.168.1.100:8080/api/v1");
ApiConfig.loadFromProperties();

// Opción 3: Archivo de configuración
// Agregar a resource/api.properties o similar
api.base.url=http://192.168.1.100:8080/api/v1
api.timeout=10000
```

### Timeouts

```java
ApiConfig.getConnectionTimeout();    // 10000 ms (10 segundos)
ApiConfig.getReadTimeout();          // 10000 ms (10 segundos)

// Cambiar
ApiConfig.setConnectionTimeout(5000);
ApiConfig.setReadTimeout(5000);
```

## Uso desde las GUIs

### Ejemplo: Listar conceptos en una tabla

```java
public class ConceptosGui extends JFrame {
    private AdministradorJpa admin = AdministradorJpa.getInstancia();
    
    public void cargarConceptos() {
        try {
            List<GenericDto> conceptos = admin.getConceptoJpa().findAll();
            
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Nombre");
            model.addColumn("Valor");
            
            for (GenericDto concepto : conceptos) {
                model.addRow(new Object[]{
                    concepto.getInt("idConcepto"),
                    concepto.getString("nombre"),
                    concepto.getFloat("valor")
                });
            }
            
            table.setModel(model);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar conceptos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

### Ejemplo: Crear nuevo concepto

```java
public void guardarConcepto(String nombre, Float valor) {
    try {
        GenericDto nuevoConcepto = new GenericDto();
        nuevoConcepto.put("nombre", nombre);
        nuevoConcepto.put("valor", valor);
        
        GenericDto creado = admin.getConceptoJpa().create(nuevoConcepto);
        Integer id = creado.getInt("idConcepto");
        
        JOptionPane.showMessageDialog(this, 
            "Concepto creado con ID: " + id);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, 
            "Error al guardar: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
```

### Ejemplo: Actualizar concepto

```java
public void actualizarConcepto(Integer id, String nombre, Float valor) {
    try {
        GenericDto concepto = new GenericDto();
        concepto.put("idConcepto", id);
        concepto.put("nombre", nombre);
        concepto.put("valor", valor);
        
        admin.getConceptoJpa().edit(concepto);
        
        JOptionPane.showMessageDialog(this, "Concepto actualizado");
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, 
            "Error: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
```

### Ejemplo: Eliminar concepto

```java
public void eliminarConcepto(Integer id) {
    try {
        admin.getConceptoJpa().remove(id);
        JOptionPane.showMessageDialog(this, "Concepto eliminado");
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, 
            "Error: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
```

## Manejo de errores

### IOException

Todas las operaciones lanzanIOException si hay problemas de red o del servidor:

```java
try {
    List<GenericDto> items = client.findAll();
} catch (IOException e) {
    // Manejar: conexión perdida, timeout, servidor no disponible
    logger.log(Level.SEVERE, "Error de comunicación", e);
}
```

### Validación de respuestas

El cliente REST no realiza validaciones de negocio (eso es responsabilidad del backend):

```java
GenericDto dto = client.find(99); // 404 lanzará IOException
```

## Logging

Las operaciones REST registran en logs:

```java
// Nivel FINE: Operaciones exitosas
// Nivel SEVERE: Errores

// Habilitar en logging.properties
com.licensis.notaire.api.client.level=FINE
com.licensis.notaire.servicios.level=FINE
```

## Compilación y ejecución

### Compilar el frontend

```bash
cd /home/matias/workspace/notaire/notaire
mvn clean package -pl frontend-swing -DskipTests
```

### Ejecutar el frontend

```bash
# Asegurar que el backend esté corriendo
docker compose up -d

# Esperar a que esté listo (20-30 segundos)

# Ejecutar el JAR del frontend
java -jar frontend-swing/target/frontend-swing-1.0-SNAPSHOT.jar
```

### Cambiar la URL de la API al ejecutar

```bash
java -Dapi.base.url=http://192.168.1.100:8080/api/v1 \
     -jar frontend-swing/target/frontend-swing-1.0-SNAPSHOT.jar
```

## Cambios a código existente

Para migrar código GUI existente que usaba JPA directo:

### Antes (JPA directo):

```java
ConceptoJpaController jpa = admin.getConceptoJpa();
Concepto concepto = jpa.findConcepto(1);
// ... uso directo de Concepto
```

### Después (REST):

```java
GenericRestClient client = admin.getConceptoJpa();
GenericDto concepto = client.find(1);
Integer id = concepto.getInt("idConcepto");
String nombre = concepto.getString("nombre");
// ... uso de GenericDto
```

## Limitaciones conocidas

1. **No hay persistencia local** - El frontend no cachea datos
2. **Conexión siempre requerida** - Sin conectividad a la API, no funciona
3. **Tipos genéricos** - GenericDto no proporciona autocompletado de IDE para campos

## Mejoras futuras

1. Implementar caché local para mejor performance
2. Modo offline con sincronización posterior
3. Cliente específico para cada entidad (reducir uso de reflection)
4. Soporte para búsquedas y filtros
5. Autenticación y autorización (JWT tokens)

## Arquitectura de carpetas

```
frontend-swing/
├── src/main/java/com/licensis/notaire/
│   ├── api/
│   │   └── client/
│   │       ├── ApiConfig.java          # Configuración de la API
│   │       ├── RestClient.java         # Cliente HTTP + JSON
│   │       └── BaseRestClient.java     # Clase base CRUD
│   ├── servicios/
│   │   ├── AdministradorJpa.java       # Singleton proxy
│   │   └── GenericRestClient.java      # Cliente REST para entidades
│   ├── dto/
│   │   └── GenericDto.java             # DTO genérico con Map
│   └── gui/
│       ├── Login.java                  # Punto de entrada
│       ├── Principal.java              # Ventana principal
│       └── ...resto de GUIs
└── target/
    └── frontend-swing-1.0-SNAPSHOT.jar # JAR ejecutable
```

## Conclusión

El frontend Swing ahora está completamente desacoplado del backend mediante una capa REST. Esto permite:

- Desarrollo independiente del frontend y backend
- Múltiples clientes de la misma API
- Fácil actualización sin recompilación
- Mejor testabilidad y mantenibilidad

La migración de código existente es simple: reemplazar llamadas JPA con llamadas REST usando `GenericDto`.
