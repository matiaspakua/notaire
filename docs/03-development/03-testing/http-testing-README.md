# Notaire API - HTTP Tests

Este directorio contiene scripts para probar todos los endpoints de la API REST de Notaire.

## Estructura

- `test-all-endpoints.sh` - Script master que ejecuta todos los tests
- `01-auth.sh` - Tests de autenticación
- `02-usuarios.sh` - Tests de usuarios
- `03-conceptos.sh` - Tests de conceptos
- `04-personas.sh` - Tests de personas
- `05-tramites.sh` - Tests de trámites
- `06-escrituras.sh` - Tests de escrituras
- `07-presupuestos.sh` - Tests de presupuestos

## Requisitos

- `curl` instalado
- Backend ejecutándose en `http://localhost:8080`
- Base de datos PostgreSQL levantada con Docker Compose

## Uso

### Opción 1: Ejecutar todos los tests

```bash
chmod +x test-all-endpoints.sh
./test-all-endpoints.sh
```

### Opción 2: Ejecutar tests individuales

```bash
chmod +x 01-auth.sh
./01-auth.sh

chmod +x 02-usuarios.sh
./02-usuarios.sh

# etc...
```

### Opción 3: Tests manuales con curl

```bash
# Obtener todos los conceptos
curl -X GET "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json"

# Crear un nuevo concepto
curl -X POST "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Test", "monto": 100}'

# Obtener concepto por ID
curl -X GET "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json"

# Actualizar concepto
curl -X PUT "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Updated", "monto": 150}'

# Eliminar concepto
curl -X DELETE "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json"
```

## Endpoints Disponibles

### Authentication
- `POST /api/v1/auth/login` - Login con usuario y contraseña

### Usuarios
- `GET /api/v1/usuarios` - Obtener todos los usuarios
- `GET /api/v1/usuarios/{id}` - Obtener usuario por ID

### Conceptos
- `GET /api/v1/conceptos` - Obtener todos los conceptos
- `GET /api/v1/conceptos/{id}` - Obtener concepto por ID
- `POST /api/v1/conceptos` - Crear nuevo concepto
- `PUT /api/v1/conceptos/{id}` - Actualizar concepto
- `DELETE /api/v1/conceptos/{id}` - Eliminar concepto

### Personas
- `GET /api/v1/personas` - Obtener todas las personas
- `GET /api/v1/personas/{id}` - Obtener persona por ID
- `POST /api/v1/personas` - Crear nueva persona
- `PUT /api/v1/personas/{id}` - Actualizar persona
- `DELETE /api/v1/personas/{id}` - Eliminar persona

### Trámites
- `GET /api/v1/tramites` - Obtener todos los trámites
- `GET /api/v1/tramites/{id}` - Obtener trámite por ID
- `POST /api/v1/tramites` - Crear nuevo trámite
- `PUT /api/v1/tramites/{id}` - Actualizar trámite
- `DELETE /api/v1/tramites/{id}` - Eliminar trámite

### Escrituras
- `GET /api/v1/escrituras` - Obtener todas las escrituras
- `GET /api/v1/escrituras/{id}` - Obtener escritura por ID
- `POST /api/v1/escrituras` - Crear nueva escritura
- `PUT /api/v1/escrituras/{id}` - Actualizar escritura
- `DELETE /api/v1/escrituras/{id}` - Eliminar escritura

### Presupuestos
- `GET /api/v1/presupuestos` - Obtener todos los presupuestos
- `GET /api/v1/presupuestos/{id}` - Obtener presupuesto por ID
- `POST /api/v1/presupuestos` - Crear nuevo presupuesto
- `PUT /api/v1/presupuestos/{id}` - Actualizar presupuesto
- `DELETE /api/v1/presupuestos/{id}` - Eliminar presupuesto

## Documentación Swagger

Una vez que la aplicación esté ejecutándose, accede a:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

## Troubleshooting

### Error: Connection refused
- Verificar que el backend está ejecutándose en puerto 8080
- Verificar que Docker Compose está levantado: `docker-compose ps`

### Error: Database connection
- Verificar PostgreSQL está corriendo: `docker-compose logs postgres`
- Verificar credenciales en `application.properties`

### Error: 404 Not Found
- Verificar que el endpoint existe en los controladores
- Verificar que el servicio Tomcat está completamente iniciado
