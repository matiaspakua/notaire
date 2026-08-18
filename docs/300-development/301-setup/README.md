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

## Estructura de Módulos

```bash
notaire/
├── backend-api/           # API REST (Spring Boot)
│   ├── src/main/java/
│   └── pom.xml
├── frontend-swing/        # Cliente Swing
│   └── pom.xml
├── notaire-shared/        # DTOs compartidos
│   └── pom.xml
└── pom.xml              # Parent POM
```

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
├── dto/                 # DTOs
│   └── DtoPersona.java
├── jpa/                 # Legacy (a eliminar)
└── exception/           # Excepciones
```

## Base de Datos

### Schema

El schema es gestionado por Flyway (migraciones en `backend-api/src/main/resources/db/migration/`). Los scripts históricos `init-db/` están archivados en `docs/archive/init-db/`.

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
