# Notaire API — HTTP Tests

Guía de pruebas manuales de la API REST de Notaire con `curl`. El script
automatizado vive en `testing/integration/http/test-all-endpoints-v2.sh` y se
ejecuta con `bash testing/scripts/run.sh integration` (ver
[`testing/README.md`](../../../../testing/README.md)); todos los comandos de este
documento asumen backend corriendo en `http://localhost:8080`.

Para pruebas de API con **colecciones Bruno** (105 requests) y su estado
actual de cobertura, ver [`backend-api/api-test/`](../../../../backend-api/api-test/README.md)
y [`COVERAGE.md`](../../../../backend-api/api-test/COVERAGE.md).

## Requisitos

- `curl` (y opcionalmente `jq` para formatear salida)
- Backend ejecutándose en `http://localhost:8080` (`bash workspace/stack/start.sh`)
- PostgreSQL levantado vía Docker Compose

## Uso

```bash
# Suite de integración completa (contra un stack en ejecución)
bash testing/scripts/run.sh integration
```

## Documentación Swagger

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

## Ejemplos manuales con curl

```bash
# Listar
curl -X GET "http://localhost:8080/api/v1/concepts" -H "Content-Type: application/json"

# Crear
curl -X POST "http://localhost:8080/api/v1/concepts" \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Test", "codigo": "TEST001"}'

# Obtener por ID / actualizar / eliminar
curl -X GET "http://localhost:8080/api/v1/concepts/1"
curl -X PUT "http://localhost:8080/api/v1/concepts/1" -H "Content-Type: application/json" -d '{"descripcion": "Test", "codigo": "TEST001"}'
curl -X DELETE "http://localhost:8080/api/v1/concepts/1"
```

El mismo patrón CRUD aplica a todas las entidades. Endpoints principales
(verificados contra `@RequestMapping` de cada controller):

```text
/api/v1/auth  /api/v1/users  /api/v1/people  /api/v1/procedures
/api/v1/deeds  /api/v1/budgets  /api/v1/items  /api/v1/folios
/api/v1/testimonies  /api/v1/payments  /api/v1/properties  /api/v1/copies
/api/v1/history  /api/v1/document-types  /api/v1/identification-types
/api/v1/substitutions  /api/v1/submitted-documents  /api/v1/testimony-movements
/api/v1/registro-auditoria  /api/v1/management-statuses  /api/v1/managements
/api/v1/budget-templates  /api/v1/procedure-templates  /api/v1/reports
/api/v1/roles  /api/v1/folio-types  /api/v1/procedure-types
/api/v1/workflow-definition  /api/v1/workflow-node  /api/v1/workflow-transition
```

Nota: `usuarios` no tiene campo `email` en `DtoLoginUser`; `contrasenia` es
write-only.

## Patrones útiles

```bash
# Paginación
curl "http://localhost:8080/api/v1/concepts?page=0&size=10&sort=id,desc"

# Formatear con jq
curl -s "http://localhost:8080/api/v1/concepts" | jq '.'

# Casos de error
curl -X GET "http://localhost:8080/api/v1/concepts/99999"   # 404
curl -X POST "http://localhost:8080/api/v1/concepts" -H "Content-Type: application/json" -d '{ invalid json }'  # 400
```

## Troubleshooting

- **Connection refused**: verificar que el backend corre en :8080 (`docker-compose ps`)
- **Database connection**: `docker-compose logs postgres`, revisar credenciales en `application.properties`
- **404 Not Found**: verificar el `@RequestMapping` del controller y que Tomcat terminó de iniciar
