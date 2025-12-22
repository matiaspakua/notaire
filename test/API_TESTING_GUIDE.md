# Notaire REST API - Manual Testing Guide

This guide provides curl examples for testing all API endpoints manually.

## Prerequisites

- API running at `http://localhost:8080`
- cURL installed
- jq installed (optional, for formatted JSON output)

```bash
# Start the API
bash start.sh

# In another terminal, run tests
bash test.sh

# Or test manually with examples below
```

## Base URL
```
http://localhost:8080/api/v1
```

## Swagger Documentation
```
http://localhost:8080/swagger-ui.html
```

---

## Testing Commands by Entity

### 1. Conceptos

```bash
# List all conceptos
curl -X GET "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json"

# Get single concepto (replace 1 with actual ID)
curl -X GET "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json"

# Create new concepto
curl -X POST "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Nuevo Concepto",
    "codigo": "CONC001"
  }'

# Update concepto
curl -X PUT "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Concepto Actualizado",
    "codigo": "CONC001"
  }'

# Delete concepto
curl -X DELETE "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json"
```

### 2. Personas

```bash
# List all personas
curl -X GET "http://localhost:8080/api/v1/personas"

# Get single persona
curl -X GET "http://localhost:8080/api/v1/personas/1"

# Create new persona
curl -X POST "http://localhost:8080/api/v1/personas" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "apellido": "García",
    "cedula": "12345678",
    "email": "juan@example.com"
  }'

# Update persona
curl -X PUT "http://localhost:8080/api/v1/personas/1" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Carlos",
    "apellido": "García López",
    "cedula": "12345678",
    "email": "juan.carlos@example.com"
  }'

# Delete persona
curl -X DELETE "http://localhost:8080/api/v1/personas/1"
```

### 3. Trámites

```bash
# List all tramites
curl -X GET "http://localhost:8080/api/v1/tramites"

# Get single tramite
curl -X GET "http://localhost:8080/api/v1/tramites/1"

# Create new tramite
curl -X POST "http://localhost:8080/api/v1/tramites" \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "TRM001",
    "fechaInicio": "2024-01-15",
    "estado": "ACTIVO"
  }'

# Update tramite
curl -X PUT "http://localhost:8080/api/v1/tramites/1" \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "TRM001",
    "fechaInicio": "2024-01-15",
    "estado": "COMPLETADO"
  }'

# Delete tramite
curl -X DELETE "http://localhost:8080/api/v1/tramites/1"
```

### 4. Escrituras

```bash
# List all escrituras
curl -X GET "http://localhost:8080/api/v1/escrituras"

# Get single escritura
curl -X GET "http://localhost:8080/api/v1/escrituras/1"

# Create new escritura
curl -X POST "http://localhost:8080/api/v1/escrituras" \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "ESC-2024-001",
    "fecha": "2024-01-15"
  }'

# Update escritura
curl -X PUT "http://localhost:8080/api/v1/escrituras/1" \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "ESC-2024-001",
    "fecha": "2024-01-20"
  }'

# Delete escritura
curl -X DELETE "http://localhost:8080/api/v1/escrituras/1"
```

### 5. Presupuestos

```bash
# List all presupuestos
curl -X GET "http://localhost:8080/api/v1/presupuestos"

# Get single presupuesto
curl -X GET "http://localhost:8080/api/v1/presupuestos/1"

# Create new presupuesto
curl -X POST "http://localhost:8080/api/v1/presupuestos" \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "PRES-2024-001",
    "monto": 5000.00,
    "estado": "PENDIENTE"
  }'

# Update presupuesto
curl -X PUT "http://localhost:8080/api/v1/presupuestos/1" \
  -H "Content-Type: application/json" \
  -d '{
    "numero": "PRES-2024-001",
    "monto": 6000.00,
    "estado": "APROBADO"
  }'

# Delete presupuesto
curl -X DELETE "http://localhost:8080/api/v1/presupuestos/1"
```

### 6. Items

```bash
# List all items
curl -X GET "http://localhost:8080/api/v1/items"

# Get single item
curl -X GET "http://localhost:8080/api/v1/items/1"

# Create new item
curl -X POST "http://localhost:8080/api/v1/items" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Item de Presupuesto",
    "cantidad": 1,
    "precio": 1000.00
  }'

# Update item
curl -X PUT "http://localhost:8080/api/v1/items/1" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Item Actualizado",
    "cantidad": 2,
    "precio": 1200.00
  }'

# Delete item
curl -X DELETE "http://localhost:8080/api/v1/items/1"
```

### 7. Usuarios

```bash
# List all usuarios
curl -X GET "http://localhost:8080/api/v1/usuarios"

# Get single usuario
curl -X GET "http://localhost:8080/api/v1/usuarios/1"

# Create new usuario
curl -X POST "http://localhost:8080/api/v1/usuarios" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "admin",
    "password": "admin123",
    "email": "admin@notaire.com"
  }'

# Update usuario
curl -X PUT "http://localhost:8080/api/v1/usuarios/1" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "admin",
    "password": "newpassword123",
    "email": "admin@notaire.com"
  }'

# Delete usuario
curl -X DELETE "http://localhost:8080/api/v1/usuarios/1"
```

### Other Entities

Similar patterns apply to all other entities:
- `/api/v1/folios`
- `/api/v1/testimonios`
- `/api/v1/pagos`
- `/api/v1/inmuebles`
- `/api/v1/copias`
- `/api/v1/historial`
- `/api/v1/tipodedocumento`
- `/api/v1/tipoidentificacion`
- `/api/v1/suplencias`
- `/api/v1/documentospresentados`
- `/api/v1/movimientotestimonio`
- `/api/v1/registroauditoria`

---

## Testing Patterns

### Pattern 1: List with Pagination (if supported)

```bash
# List first page
curl "http://localhost:8080/api/v1/conceptos?page=0&size=10&sort=id,desc"

# List with filtering (depending on entity)
curl "http://localhost:8080/api/v1/conceptos?search=codigo:CONC"
```

### Pattern 2: Formatted Output with jq

```bash
# Pretty print JSON response
curl -s "http://localhost:8080/api/v1/conceptos" | jq .

# Extract specific fields
curl -s "http://localhost:8080/api/v1/conceptos" | jq '.[].descripcion'

# Filter results
curl -s "http://localhost:8080/api/v1/conceptos" | jq '.[] | select(.codigo | contains("CONC"))'
```

### Pattern 3: Error Handling Testing

```bash
# Test 404 (not found)
curl -X GET "http://localhost:8080/api/v1/conceptos/99999"

# Test 400 (bad request - invalid JSON)
curl -X POST "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{ invalid json }'

# Test 500 (server error - database issue)
# This would only occur if database connection fails
```

---

## Testing Workflow

### 1. Start Application
```bash
bash start.sh
# Wait for "✓ Notaire Application is running!"
```

### 2. Verify API is Running
```bash
curl -s http://localhost:8080/swagger-ui.html | head -20
```

### 3. Get List of Conceptos
```bash
curl -s "http://localhost:8080/api/v1/conceptos" | jq '.' | head -30
```

### 4. Create New Concepto
```bash
curl -X POST "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Test Concepto",
    "codigo": "TEST001"
  }' | jq '.'
```

### 5. Get Created Concepto
```bash
# Note the ID from previous response
curl -X GET "http://localhost:8080/api/v1/conceptos/1" | jq '.'
```

### 6. Update Concepto
```bash
curl -X PUT "http://localhost:8080/api/v1/conceptos/1" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Updated Concepto",
    "codigo": "TEST001-UPDATED"
  }' | jq '.'
```

### 7. Delete Concepto
```bash
curl -X DELETE "http://localhost:8080/api/v1/conceptos/1" | jq '.'
```

---

## Response Examples

### Successful GET Response (200 OK)
```json
{
  "conceptoID": 1,
  "descripcion": "Concepto de Prueba",
  "codigo": "CONC001"
}
```

### Successful POST Response (201 Created)
```json
{
  "conceptoID": 2,
  "descripcion": "Nuevo Concepto",
  "codigo": "CONC002"
}
```

### Error Response (404 Not Found)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Concepto not found with ID: 999",
  "path": "/api/v1/conceptos/999"
}
```

### Error Response (500 Server Error)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Database connection failed",
  "path": "/api/v1/conceptos"
}
```

---

## Troubleshooting

### API Not Responding
```bash
# Check if service is running
curl -v http://localhost:8080/swagger-ui.html

# Check logs
docker-compose logs -f backend
```

### Database Connection Error
```bash
# Check PostgreSQL is running
docker-compose ps

# Test database connection
docker-compose exec postgres psql -U notaire -d notaire -c "SELECT 1"
```

### Port Already in Use
```bash
# Find process using port 8080
netstat -tlnp | grep 8080

# Kill process and restart
bash stop.sh
sleep 2
bash start.sh
```

### JSON Parse Error
```bash
# Validate JSON before sending
echo '{"key": "value"}' | jq '.'

# Use heredoc for multi-line JSON
curl -X POST "http://localhost:8080/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d @- <<EOF
{
  "descripcion": "Multi-line JSON",
  "codigo": "TEST001"
}
EOF
```

---

## Performance Testing

### Test Response Time
```bash
# Single request
time curl -s "http://localhost:8080/api/v1/conceptos" > /dev/null

# Multiple requests (load test)
for i in {1..100}; do
  curl -s "http://localhost:8080/api/v1/conceptos" > /dev/null &
done
wait
```

### Monitor Resource Usage
```bash
# In another terminal
docker stats backend postgres
```

---

## Automated Testing

### Run Full Test Suite
```bash
bash test.sh
```

### Run Individual Entity Tests
```bash
cd test/http
bash 01-auth.sh
bash 02-usuarios.sh
bash 03-conceptos.sh
# ... etc for other entities
```

---

## API Contract Testing

### Verify Response Schema
```bash
# Get OpenAPI schema
curl -s "http://localhost:8080/v3/api-docs" | jq '.paths' | head -50

# Get specific endpoint schema
curl -s "http://localhost:8080/v3/api-docs" | \
  jq '.paths["/api/v1/conceptos"].get'
```

---

## Notes

- All timestamps are in ISO 8601 format
- All numeric IDs are integers
- All money amounts are in the currency defined in the database
- Date fields accept format: YYYY-MM-DD
- DateTime fields accept format: YYYY-MM-DDTHH:MM:SS

---

## References

- **API Base**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

For more information, see `DEPLOYMENT.md`
