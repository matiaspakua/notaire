# Notaire API — HTTP Tests

Guía de pruebas manuales de la API REST de Notaire con `curl`. Los scripts
automatizados viven en `testing/http/` (no en este directorio de
documentación); todos los comandos de este documento asumen backend
corriendo en `http://localhost:8080`.

## Estructura (`testing/http/`)

- `test-all-endpoints.sh` / `test-all-endpoints-v2.sh` — scripts master que ejecutan todos los tests
- `01-auth.sh` … `08-items.sh` — tests por entidad (auth, usuarios, conceptos, personas, trámites, escrituras, presupuestos, items)

Para pruebas de API con **colecciones Bruno** (105 requests) y su estado
actual de cobertura, ver [`backend-api/api-test/`](../../../../backend-api/api-test/README.md)
y [`COVERAGE.md`](../../../../backend-api/api-test/COVERAGE.md).

## Requisitos

- `curl` (y opcionalmente `jq` para formatear salida)
- Backend ejecutándose en `http://localhost:8080` (`bash scripts/start.sh`)
- PostgreSQL levantado vía Docker Compose

## Uso

```bash
# Todos los tests
cd testing/http
chmod +x test-all-endpoints.sh && ./test-all-endpoints.sh

# Test individual
chmod +x 01-auth.sh && ./01-auth.sh
```

## Documentación Swagger

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

## Ejemplos manuales con curl

```bash
# Listar
curl -X GET "http://localhost:8080/api/v1/conceptos" -H "Content-Type: application/json"

# Crear
curl -X POST "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Test", "codigo": "TEST001"}'

# Obtener por ID / actualizar / eliminar
curl -X GET "http://localhost:8080/api/v1/conceptos/1"
curl -X PUT "http://localhost:8080/api/v1/conceptos/1" -H "Content-Type: application/json" -d '{"descripcion": "Test", "codigo": "TEST001"}'
curl -X DELETE "http://localhost:8080/api/v1/conceptos/1"
```

El mismo patrón CRUD aplica a todas las entidades. Endpoints principales
(verificados contra `@RequestMapping` de cada controller):

```
/api/v1/auth  /api/v1/usuarios  /api/v1/personas  /api/v1/tramites
/api/v1/escrituras  /api/v1/presupuestos  /api/v1/items  /api/v1/folio
/api/v1/testimonio  /api/v1/pagos  /api/v1/inmueble  /api/v1/copia
/api/v1/historial  /api/v1/tipo-de-documento  /api/v1/tipo-identificacion
/api/v1/suplencia  /api/v1/documento-presentado  /api/v1/movimiento-testimonio
/api/v1/registro-auditoria  /api/v1/estado-gestion  /api/v1/gestiones
/api/v1/plantilla-presupuestos  /api/v1/plantilla-tramite  /api/v1/reportes
/api/v1/roles  /api/v1/tipo-folio  /api/v1/tipo-tramite
/api/v1/workflow-definition  /api/v1/workflow-node  /api/v1/workflow-transition
```

Nota: `usuarios` no tiene campo `email` en `DtoUsuario`; `contrasenia` es
write-only.

## Patrones útiles

```bash
# Paginación
curl "http://localhost:8080/api/v1/conceptos?page=0&size=10&sort=id,desc"

# Formatear con jq
curl -s "http://localhost:8080/api/v1/conceptos" | jq '.'

# Casos de error
curl -X GET "http://localhost:8080/api/v1/conceptos/99999"   # 404
curl -X POST "http://localhost:8080/api/v1/conceptos" -H "Content-Type: application/json" -d '{ invalid json }'  # 400
```

## Troubleshooting

- **Connection refused**: verificar que el backend corre en :8080 (`docker-compose ps`)
- **Database connection**: `docker-compose logs postgres`, revisar credenciales en `application.properties`
- **404 Not Found**: verificar el `@RequestMapping` del controller y que Tomcat terminó de iniciar
