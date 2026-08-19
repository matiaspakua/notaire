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

### 5. Iniciar Frontend (Next.js)

El cliente web activo vive en `frontend/` (no confundir con `deprecated-frontend-swing/`, el
cliente Swing legacy). Requiere el backend corriendo en `http://localhost:8080`.

```bash
cd frontend
cp .env.local.example .env.local   # ajustar API URL si es necesario
npm install
npm run dev                         # http://localhost:3000
```

Ver [`frontend/README.md`](../../../frontend/README.md) para stack completo, scripts disponibles
y cobertura de módulos por Caso de Uso.

## Estructura de Módulos

```bash
notaire/
├── backend-api/                # API REST (Spring Boot) — módulo del reactor Maven
│   ├── src/main/java/
│   └── pom.xml
├── notaire-shared/              # DTOs y código compartido — módulo del reactor Maven
│   └── pom.xml
├── frontend/                    # Cliente web (Next.js) — desarrollo activo, ver frontend/README.md
├── deprecated-frontend-swing/   # Cliente Swing legacy — DEPRECATED, excluido del reactor Maven/CI
│   └── pom.xml
└── pom.xml                      # Parent POM (solo backend-api + notaire-shared)
```

`deprecated-frontend-swing/` no recibe funcionalidad nueva; todo cliente nuevo se desarrolla en
`frontend/`. Ver [`deprecated-frontend-swing/README.md`](../../../deprecated-frontend-swing/README.md).

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

Los DTOs (`DtoPersona`, etc.) viven en el módulo `notaire-shared`
(`com.licensis.notaire.dto`), no en `backend-api` — ver
[DTO-MAPPING-GUIDE.md](../302-code-standards/DTO-MAPPING-GUIDE.md).

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
