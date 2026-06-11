# Notaire API Testing - Progress Report

## Estado: En Progreso

**Última actualización:** 15 de Abril 2026  
**Branch:** `fix/bruno-sintaxis-tests`  
**PR:** [#334](https://github.com/matiaspakua/notaire/pull/334)

---

## Resumen Ejecutivo

Se está trabajando en completar los tests de API usando Bruno CLI con assertions CHAI. El objetivo es:
1. Verificar que todos los endpoints de la API estén correctamente implementados
2. Mapear los 68 casos de uso del sistema a endpoints REST
3. Crear una suite completa de tests API con Bruno

---

## Análisis Completado

### Endpoints API Identificados (27 Controllers, 100+ endpoints)

| Controller | Path Base | Operaciones |
|------------|-----------|------------|
| UsuarioController | `/api/v1/usuarios` | GET, POST, PUT, DELETE, login |
| PersonaController | `/api/v1/personas` | GET, POST, PUT, DELETE, buscar |
| GestionController | `/api/v1/gestiones` | GET, POST, PUT, DELETE, estado-actual |
| PresupuestoController | `/api/v1/presupuestos` | GET, POST, PUT, DELETE |
| EscrituraController | `/api/v1/escrituras` | GET, POST, PUT, DELETE |
| TrámiteController | `/api/v1/tramites` | GET, POST, PUT, DELETE |
| ConceptoController | `/api/v1/conceptos` | GET, POST, PUT, DELETE |
| PagoController | `/api/v1/pagos` | GET, POST, PUT, DELETE |
| ItemController | `/api/v1/items` | GET, POST, PUT, DELETE |
| TestimonioController | `/api/v1/testimonio` | GET, POST, PUT, DELETE |
| InmuebleController | `/api/v1/inmueble` | GET, POST, PUT, DELETE |
| CopiaController | `/api/v1/copia` | GET, POST, PUT, DELETE |
| SuplenciaController | `/api/v1/suplencia` | GET, POST, PUT, DELETE |
| FolioController | `/api/v1/folio` | GET, POST, PUT, DELETE |
| DocumentoPresentadoController | `/api/v1/documento-presentado` | GET, POST, PUT, DELETE |
| MovimientoTestimonioController | `/api/v1/movimiento-testimonio` | GET, POST, PUT, DELETE |
| PlantillaTramiteController | `/api/v1/plantilla-tramite` | GET |
| PlantillaPresupuestoController | `/api/v1/plantilla-presupuestos` | GET, POST, PUT, DELETE |
| RegistroAuditoriaController | `/api/v1/registro-auditoria` | GET, POST, DELETE |
| HistorialController | `/api/v1/historial` | GET, POST, PUT, DELETE |
| ReporteController | `/api/v1/reportes/*` | GET (PDF reports) |
| TipoDeFolioController | `/api/v1/tipo-folio` | CRUD |
| TipoIdentificacionController | `/api/v1/tipo-identificacion` | CRUD |
| TipoDeTramiteController | `/api/v1/tipo-tramite` | CRUD |
| TipoDeDocumentoController | `/api/v1/tipo-de-documento` | CRUD |
| EstadoDeGestionController | `/api/v1/estado-gestion` | CRUD |

### Casos de Uso (68 total)

Los casos de uso están en `docs/01-business/03_CU - Casos de Uso/`

| Categoría | Cantidad | Ejemplos |
|-----------|----------|----------|
| Core Business | ~25 | CU01-CU16 (Presupuesto, Gestión, Escritura) |
| Entidades | ~20 | CU17-CU35 (Persona, Cliente, Usuario, etc.) |
| Catálogos | ~15 | CU26-CU40 (Tipos, Conceptos, Estados) |
| Búsquedas | ~8 | CU60-CU68 (Buscar por entidad) |

---

## Estructura de Tests Bruno

```
backend-api/api-test/
├── bruno.json                 # Collection config
├── environments/
│   └── Developmen.bru        # Environment variables
├── auth/
│   ├── folder.bru
│   └── login.bru            # ✅ Funcionando
├── personas/                 # ✅ Tests básicos funcionando
├── usuarios/                 # ⚠️ Error 500 en algunos endpoints
├── gestiones/                # ✅ Tests básicos funcionando
├── presupuestos/             # ✅ Tests básicos funcionando
├── escrituras/               # ⚠️ Error 500 en algunos endpoints
├── tramites/                 # ⚠️ Error 500 en algunos endpoints
├── conceptos/                # ✅ Tests básicos funcionando
├── pagos/                    # ⚠️ Error 500 en algunos endpoints
├── items/                    # ⚠️ Error 500 en algunos endpoints
├── catalogos/               # ⚠️ Error 500 en DELETE/PUT
├── reportes/                 # ⚠️ Requiere ID existente
├── auditoria/                # ✅ Funcionando
└── results-*.{json,junit,html}  # Reportes generados
```

---

## Correcciones Realizadas

### 1. Sintaxis Bruno Corregida

**Problema:** Los archivos `folder.bru` usaban formato incorrecto

**Antes (incorrecto):**
```bru
name: Auth
uid: auth-folder
```

**Después (correcto):**
```bru
meta {
  name: Auth
  type: folder
}
```

### 2. Environment File Corregido

**Antes:**
```bru
base_url: http://localhost:8080
token: 
```

**Después:**
```bru
vars {
  base_url: http://localhost:8080
  token: 
  user_id: 
  ...
}
```

### 3. Query Parameters Corregidos

**Antes (incorrecto):**
```bru
params {
  nombre: 
  apellido: 
}
```

**Después (correcto):**
```bru
query {
  nombre: 
  apellido: 
}
```

### 4. Login Test Corregido

**Problema:** El body JSON no se enviaba correctamente

**Solución:** Usar `body:text` en lugar de `body:json`

```bru
post {
  url: {{base_url}}/api/v1/usuarios/login
  body: text  # ✅ Funciona
  auth: none
}

body:text {
  {"nombre":"admin","contrasenia":"admin"}
}
```

---

## Tests Mejorados con CHAI

Ejemplo de assertions CHAI más completos:

```javascript
tests {
  test("Status 200 - Personas retrieved successfully", function () {
    expect(res.getStatus()).to.equal(200);
  });
   
  test("Response is an array", function () {
    const body = res.getBody();
    expect(body).to.be.an('array');
  });
  
  test("Response Content-Type is JSON", function () {
    expect(res.getHeader('content-type')).to.include('application/json');
  });
  
  test("Each persona has required fields", function () {
    const body = res.getBody();
    if (body.length > 0) {
      expect(body[0]).to.have.property('idPersona');
      expect(body[0]).to.have.property('nombre');
      expect(body[0]).to.have.property('apellido');
    }
  });
}
```

---

## Estado de Tests

### Tests Pasando ✅

| Endpoint | Status | Notes |
|----------|--------|-------|
| `GET /api/v1/personas` | ✅ 200 | Array response |
| `GET /api/v1/gestiones` | ✅ 200 | Array response |
| `GET /api/v1/presupuestos` | ✅ 200 | Array response |
| `GET /api/v1/conceptos` | ✅ 200 | Array response |
| `GET /api/v1/escrituras` | ✅ 200 | Array response |
| `GET /api/v1/tramites` | ✅ 200 | Array response |
| `GET /api/v1/pagos` | ✅ 200 | Array response |
| `GET /api/v1/usuarios` | ✅ 200 | Array response |
| `POST /api/v1/usuarios/login` | ✅ 200 | Login funciona |
| `GET /api/v1/registro-auditoria` | ✅ 200 | Array response |

### Tests Fallando ❌

| Endpoint | Status | Causa |
|----------|--------|--------|
| `DELETE /api/v1/tipo-documento/{id}` | 500 | FK constraint o ID no existe |
| `DELETE /api/v1/tipo-tramite/{id}` | 500 | FK constraint o ID no existe |
| `DELETE /api/v1/tipo-folio/{id}` | 500 | FK constraint o ID no existe |
| `PUT /api/v1/personas/{id}` | 404 | ID no existe |
| `PUT /api/v1/usuarios/{id}` | 404 | ID no existe |
| `GET /api/v1/personas/{id}` | 404 | ID no existe |
| `GET /api/v1/tipos-folio` | 500 | Error en consulta |
| `GET /api/v1/tipos-documento` | 500 | Error en consulta |
| `GET /api/v1/estados-gestion` | 500 | Error en consulta |
| `GET /api/v1/escribanos-disponibles` | 500 | Error en lógica |
| `GET /api/v1/testimonios` | 500 | Error en consulta |
| `GET /api/v1/movimientos-testimonio` | 500 | Error en consulta |

---

## Cómo Continuar

### 1. Investigar Errores 500 del Backend

Los errores 500 indican problemas en el backend. Verificar:

```bash
# Ver logs de la aplicación
cd backend-api && mvn spring-boot:run

# O revisar logs de Docker
bash scripts/logs.sh
```

Endpoints que requieren investigación:
- `TipoDeFolioController`
- `TipoDeDocumentoController`
- `EstadoDeGestionController`
- `EscrituraController.buscarEscribanosDisponibles()`

### 2. Ejecutar Tests Bruno

```bash
# Asegurarse que la API esté corriendo
cd backend-api && mvn spring-boot:run &

# Ir a la carpeta de tests
cd backend-api/api-test

# Ejecutar todos los tests
bru run --env Developmen --reporter-json results.json --reporter-junit results-junit.xml --reporter-html results.html

# Ejecutar solo una carpeta
bru run auth/ --env Developmen
bru run personas/ --env Developmen
```

### 3. Agregar Más Tests

Los tests POST/PUT/DELETE requieren:
1. Datos de prueba válidos
2. IDs existentes en la base de datos

Ejemplo de test POST con datos válidos:

```bru
post {
  url: {{base_url}}/api/v1/personas
  body: text
  auth: none
}

headers {
  Content-Type: application/json
}

body:text {
  {
    "nombre": "Test",
    "apellido": "Persona",
    "numeroIdentificacion": "12345678",
    "esCliente": true,
    "dtoTipoIdentificacion": {
      "idTipoIdentificacion": 1
    }
  }
}

tests {
  test("Status 201 or 200", function () {
    expect(res.getStatus()).to.be.oneOf([200, 201]);
  });
  
  test("Response has idPersona", function () {
    const body = res.getBody();
    expect(body).to.have.property('idPersona');
    if (body.idPersona) {
      bru.setVar('persona_id', body.idPersona);
    }
  });
}
```

### 4. Comandos Útiles

```bash
# Verificar API corriendo
curl -s http://localhost:8080/api/v1/personas | jq .

# Verificar environment
cat backend-api/api-test/environments/Developmen.bru

# Contar archivos de test
find backend-api/api-test -name "*.bru" | wc -l

# Ver resultados
cat backend-api/api-test/results.json | jq '.'
```

### 5. Documentación de Referencia

- **Casos de Uso:** `docs/01-business/03_CU - Casos de Uso/`
- **API Controller:** `backend-api/src/main/java/com/licensis/notaire/api/`
- **Entidades:** `backend-api/src/main/java/com/licensis/notaire/negocio/`
- **DTOs:** `notaire-shared/src/main/java/com/licensis/notaire/dto/`
- **Documentación Bruno:** https://docs.usebruno.com/

---

## Próximos Pasos Prioritarios

1. **Investigación Backend (Alta)** - Corregir los errores 500 en los controllers afectados
2. **Tests POST (Alta)** - Crear tests de creación con datos válidos
3. **Tests PUT/DELETE (Alta)** - Completar CRUD para cada entidad
4. **Plan de Testing (Media)** - Documentar estrategia completa de testing
5. **Reporte (Media)** - Generar reporte final de cobertura

---

## Archivos Importantes

| Archivo | Descripción |
|---------|-------------|
| `backend-api/api-test/bruno.json` | Configuración de colección |
| `backend-api/api-test/environments/Developmen.bru` | Variables de ambiente |
| `backend-api/api-test/results.json` | Resultados de tests |
| `docs/01-business/03_CU - Casos de Uso/` | Documentación de negocio |
| `backend-api/src/main/java/com/licensis/notaire/api/` | Controllers REST |

---

## Contacto / Notas

- **Usuario de prueba:** `admin` / `admin` (puede no funcionar si no existe en DB)
- **Base URL:** `http://localhost:8080`
- **API requiere PostgreSQL corriendo en puerto 5432**

---

*Este documento fue generado el 15 de Abril de 2026 para continuar el trabajo de testing de API.*
