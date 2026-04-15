# Quick Start Guide - API Testing

## Para Continuar el Trabajo

### 1. Checkout del Branch
```bash
cd ~/workspace/notaire
git checkout fix/bruno-sintaxis-tests
git pull origin fix/bruno-sintaxis-tests
```

### 2. Asegurarse que Todo está Corriendo
```bash
# Verificar API
curl -s http://localhost:8080/api/v1/personas | jq 'length'

# Si no está corriendo, iniciar:
cd backend-api && mvn spring-boot:run &
```

### 3. Ejecutar Tests
```bash
cd backend-api/api-test
bru run --env Developmen --reporter-json results.json
```

### 4. Ver Resultados
```bash
cat results.json | jq '.'
open results.html  # Abrir en navegador
```

---

## Lo Que Necesita Arreglarse

| Prioridad | Issue | Ubicación |
|-----------|-------|-----------|
| 🔴 ALTA | Error 500 en `GET /api/v1/tipos-folio` | TipoDeFolioController |
| 🔴 ALTA | Error 500 en `DELETE /api/v1/tipo-documento/*` | FK constraints |
| 🔴 ALTA | Login tarda 30 segundos | UsuarioController.login() |
| 🟡 MEDIA | Tests POST sin datos válidos | Agregar datos de prueba |
| 🟡 MEDIA | Tests PUT/DELETE fallan con 404 | Requieren ID existente |
| 🟢 BAJA | Documentar plan de testing | docs/testing/ |

---

## Comandos Más Usados

```bash
# Solo tests de auth
bru run auth/login.bru --env Developmen

# Solo tests de personas
bru run personas/ --env Developmen

# Todos los tests
bru run --env Developmen

# Con reportes
bru run --env Developmen --reporter-json results.json --reporter-html results.html
```

---

## Estructura de Tests

```
backend-api/api-test/
├── auth/login.bru              ← Login funciona
├── personas/get-personas.bru   ← GET funciona
├── conceptos/get-conceptos.bru ← GET funciona
└── catalogos/delete-*.bru     ← Error 500
```

---

## Documentación Completa

Ver: `docs/testing/PROGRESS-REPORT.md`
