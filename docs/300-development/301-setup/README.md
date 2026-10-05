# Setup de Desarrollo

## Requisitos Previos

| Herramienta | Versión Mínima |
|------------|----------------|
| Java JDK | 21 |
| Maven | 3.9+ |
| Docker | 24+ |
| PostgreSQL Client | 15+ |

## Instalación

### 1. Clonar Repositorio

```bash
git clone https://github.com/matiaspakua/notaire.git
cd notaire
```

### 2. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```bash
# Database
POSTGRES_DB=notaire
POSTGRES_USER=notaire
POSTGRES_PASSWORD=your_password

# API
API_PORT=8080
```

### 3. Iniciar Ambiente

```bash
# Iniciar Docker con PostgreSQL
bash scripts/start.sh

# Ver logs
bash scripts/logs.sh

# Detener
bash scripts/stop.sh
```

### 4. Compilar Proyecto

```bash
mvn clean install
```

### 5. Start Frontend (Next.js)

The active web client lives in `frontend/`. The Swing desktop modules
(`frontend-swing` / `deprecated-frontend-swing`) were **removed** (#1046); do not
look for them on disk. Requires the backend at `http://localhost:8080`.

```bash
cd frontend
cp .env.local.example .env.local   # adjust API URL if needed
npm install
npm run dev                         # http://localhost:3000
```

See [`frontend/README.md`](../../../frontend/README.md) for the full stack, scripts,
and use-case module coverage.

## Module structure

```bash
notaire/
├── backend-api/                # REST API (Spring Boot) — Maven reactor module
│   ├── src/main/java/
│   └── pom.xml
├── frontend/                    # Web client (Next.js) — active development
└── pom.xml                      # Parent POM (backend-api only)
```

Swing Robot E2E under `testing/e2e-swing/` is hard-deprecated (#811); active UI
E2E is Playwright (`cd testing/e2e && npm test`).

## Comandos de Desarrollo

### Compilación

```bash
# Compilar todo
mvn clean install

# Compilar módulo específico con dependencias
mvn clean install -pl backend-api -am

# Saltar tests
mvn clean install -DskipTests
```

### Tests

```bash
# Ejecutar todos los tests
mvn test

# Tests de un módulo
mvn test -pl backend-api

# Tests específicos
mvn test -Dtest=PresupuestoEntityTest

# Ver cobertura
mvn jacoco:report
```

### Ejecución

```bash
# Backend
cd backend-api && mvn spring-boot:run

# API Docs
# http://localhost:8080/swagger-ui.html
```

## Estructura de Packages

### Backend API

```
com.licensis.notaire/
├── api/                  # Controllers REST
│   └── PersonaController.java
├── service/             # Servicios de negocio
│   └── PersonaService.java
├── repository/          # Repositorios JPA
│   └── PersonaRepository.java
├── negocio/             # Entidades
│   └── Persona.java
├── security/            # JWT y filtros de seguridad
├── config/              # Configuración de beans
├── jpa/                 # Legacy (a eliminar)
└── exception/           # Excepciones
```

Los DTOs (`DtoPersona`, etc.) viven en `backend-api` (`com.licensis.notaire.dto`); el
módulo `notaire-shared` fue retirado (ADR-024) y los servicios externos consumen la
API REST (OpenAPI) — ver [DTO-MAPPING-GUIDE.md](../302-code-standards/DTO-MAPPING-GUIDE.md).

## Base de Datos

### Schema

El schema es gestionado por Flyway (migraciones en `backend-api/src/main/resources/db/migration/`). Los scripts históricos `init-db/` están archivados en `docs/000-archive/init-db/`.

### Índices

Verificar que los índices necesarios estén creados para:
- `personas.id_persona`
- `escrituras.id_escritura`
- `presupuestos.id_presupuesto`
- Foreign keys frecuentemente consultadas

## Troubleshooting

### Puerto en uso

```bash
# Linux/Mac
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Docker no inicia

```bash
docker ps
docker-compose logs
docker-compose down -v
docker-compose up -d
```

### Maven build falla

```bash
# Limpiar cache
mvn dependency:purge-local-repository
mvn clean install -U
```

## Repo hygiene (ignore rules & manuals)

- Do **not** commit `.serena/` (local AI tooling). It is gitignored (#1050).
- Needed text assets such as `testing/e2e-swing/requirements.txt` remain
  trackable for ignore-rule hygiene (#1050) even though the Swing Robot suite is
  retired (#811); there is no global `*.txt` ban (use `*.local.txt` for scratch notes).
- The historical user-manual **PDF** is a GitHub Release asset (`docs-manuals`),
  not an ordinary git blob. Fetch with `bash scripts/fetch-user-manual.sh`
  (see [ADR-022](../../200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md)
  and [`docs/100-business/105-manuals/`](../../100-business/105-manuals/)).
