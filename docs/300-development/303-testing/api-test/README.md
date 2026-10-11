# Notaire API — HTTP Tests

Manual checks for the Notaire REST API with `curl`. The automated script lives
in `testing/integration/http/test-all-endpoints-v2.sh` and runs with
`bash testing/scripts/run.sh integration` (see
[`testing/README.md`](../../../../testing/README.md)). Every command in this
document assumes the backend is running at `http://localhost:8080`.

For API checks with **Bruno collections** (105 requests) and their current
coverage, see [`backend-api/api-test/`](../../../../backend-api/api-test/README.md)
and [`COVERAGE.md`](../../../../backend-api/api-test/COVERAGE.md).

## Requirements

- `curl` (and optionally `jq` to format output)
- Backend running at `http://localhost:8080` (`bash workspace/stack/start.sh`)
- PostgreSQL started through Docker Compose

## Usage

```bash
# Full integration suite (against a running stack)
bash testing/scripts/run.sh integration
```

## Swagger

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API docs (JSON)**: `http://localhost:8080/v3/api-docs`

## Manual curl examples

```bash
# List
curl -X GET "http://localhost:8080/api/v1/concepts" -H "Content-Type: application/json"

# Create
curl -X POST "http://localhost:8080/api/v1/concepts" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "value": 1}'

# Get by id / update / delete
curl -X GET "http://localhost:8080/api/v1/concepts/1"
curl -X PUT "http://localhost:8080/api/v1/concepts/1" -H "Content-Type: application/json" -d '{"name": "Test", "value": 1}'
curl -X DELETE "http://localhost:8080/api/v1/concepts/1"
```

The same CRUD pattern applies to every entity. Main endpoints (checked against
each controller `@RequestMapping`):

```text
/api/v1/auth  /api/v1/users  /api/v1/people  /api/v1/procedures
/api/v1/deeds  /api/v1/budgets  /api/v1/items  /api/v1/folios
/api/v1/testimonies  /api/v1/payments  /api/v1/properties  /api/v1/copies
/api/v1/history  /api/v1/document-types  /api/v1/identification-types
/api/v1/substitutions  /api/v1/submitted-documents  /api/v1/testimony-movements
/api/v1/audit-log  /api/v1/management-statuses  /api/v1/managements
/api/v1/budget-templates  /api/v1/procedure-templates  /api/v1/reports
/api/v1/roles  /api/v1/folio-types  /api/v1/procedure-types
/api/v1/workflow-definition  /api/v1/workflow-node  /api/v1/workflow-transition
```

Note: users have no `email` field. `password` is write-only.

## Useful patterns

```bash
# Pagination
curl "http://localhost:8080/api/v1/concepts?page=0&size=10&sort=id,desc"

# Format with jq
curl -s "http://localhost:8080/api/v1/concepts" | jq '.'

# Error cases
curl -X GET "http://localhost:8080/api/v1/concepts/99999"   # 404
curl -X POST "http://localhost:8080/api/v1/concepts" -H "Content-Type: application/json" -d '{ invalid json }'  # 400
```

## Troubleshooting

- **Connection refused**: check that the backend is running on :8080 (`docker-compose ps`)
- **Database connection**: `docker-compose logs postgres`, and check credentials in `application.properties`
- **404 Not Found**: check the controller `@RequestMapping` and that Tomcat finished starting
