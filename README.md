# Notaire - Sistema de Administración de Escribanía

<div align="center">
  <img src="images/logoRojoLetraMediano.png" alt="Notaire Logo" width="200"/>
  
  ![Java 21](https://img.shields.io/badge/Java-21-orange)
  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.9-brightgreen)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
  ![Docker](https://img.shields.io/badge/Docker-Ready-blue)
  
  > Sistema de gestión para escribanía, refactorizado desde un monolito Java Swing a una arquitectura moderna de microservicios con REST API.
</div>

---

## 🚀 Inicio Rápido

```bash
# Iniciar servicios completos
bash scripts/start.sh

# Ejecutar aplicación
cd backend-api && mvn spring-boot:run
cd frontend-swing && mvn exec:java -Dexec.mainClass="com.licensis.notaire.gui.Login"
```

[![Swagger UI](https://img.shields.io/badge/Swagger-UI-green)](http://localhost:8080/swagger-ui.html)
[![Health Check](https://img.shields.io/badge/Health-Check-blue)](http://localhost:8080/actuator/health)

---

## 📋 Tabla de Contenidos

1. [🎯 Resumen del Proyecto](#-resumen-del-proyecto)
2. [🛠️ Stack Tecnológico](#️-stack-tecnológico)
3. [🏗️ Arquitectura del Sistema](#️-arquitectura-del-sistema)
4. [🚀 Cómo Ejecutar el Proyecto](#-cómo-ejecutar-el-proyecto)
5. [🤖 Herramientas de IA Utilizadas](#-herramientas-de-ia-utilizadas)
6. [⚙️ Configuración de OpenCode y Skills](#️-configuración-de-opencode-y-skills)
7. [📚 Documentación del Proyecto](#-documentación-del-proyecto)
8. [📅 Cronología de la Migración](#-cronología-de-la-migración)
9. [✅ Tareas Pendientes](#-tareas-pendientes)

---

## CI/CD Pipeline

[![CI - Build, Test & Security](https://github.com/matiaspakua/notaire/actions/workflows/ci.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/ci.yml)
[![CD - Build & Publish Docker](https://github.com/matiaspakua/notaire/actions/workflows/cd.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/cd.yml)
[![PR Validation](https://github.com/matiaspakua/notaire/actions/workflows/pr-validation.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/pr-validation.yml)

### Workflows Disponibles

| Workflow | Descripción | Trigger |
|----------|-------------|---------|
| **CI** | Build, test, coverage, security scan | Push a main/develop/feature/** |
| **CD** | Build & publish Docker image | Push a tags v* o main |
| **PR Validation** | Validación de PR | Pull requests |
| **OpenCode** | Integración con OpenCode AI | Comandos /oc en comentarios |

### Features del CI

- **Build**: Compilación Maven con Java 21
- **Unit Tests**: Tests unitarios con JUnit 5
- **Integration Tests**: Tests de integración
- **Code Coverage**: JaCoCo (80% mínimo requerido)
- **Security Scan**: Trivy para vulnerabilidades
- **Docker Build**: Construcción de imagen
- **Code Quality**: SpotBugs y Checkstyle

### Configuración de Secrets

Para GitHub Actions, configurar en Settings > Secrets:

| Secret | Descripción |
|--------|-------------|
| DOCKERHUB_USERNAME | Usuario de Docker Hub (opcional) |
| DOCKERHUB_TOKEN | Token de Docker Hub (opcional) |

### Uso de GitHub Container Registry

La imagen se publica automáticamente en:
```
ghcr.io/matiaspakua/notaire/backend:latest
```

---

## 🎯 1. Resumen del Proyecto

**Notaire** es un sistema de administración para la gestión de escribanía, originalmente desarrollado hace más de 14 años como una aplicación monolítica Java Swing con conexión directa a MySQL.

### Arquitectura Original (Monolito)

![Arquitectura Original](images/arquitectura-original.png)

| Componente | Descripción |
|------------|-------------|
| **GUI Swing** | Interfaz gráfica con JFrame y JPanel |
| **Event Handlers** | Manejadores de eventos con lógica de negocio |
| **ControllerNegocio** | Clase central con toda la lógica de negocio |
| **JDBC Directo** | Conexiones SQL directas sin pooling |
| **MySQL 5.7** | Base de datos relacional (27 tablas) |

### Problemas del Código Original

| ❌ Problema | Descripción |
|-------------|-------------|
| **Acoplamiento fuerte** | GUI Swing directamente conectada a MySQL mediante JDBC |
| **Sin separación de responsabilidades** | Lógica de negocio, presentación y acceso a datos entremezclados |
| **Tecnología obsoleta** | Java 6/8 sin actualizaciones de seguridad, MySQL 5.7 |
| **Sin integración externa** | No existía forma de comunicar la aplicación con otros sistemas |
| **Dificultad de mantenimiento** | Cualquier cambio requería modificar código de la interfaz gráfica |

### ✅ Visión del Proyecto Modernizado

```
Monolito Java Swing + MySQL    →    API REST + PostgreSQL + Docker
        (10+ años)                          (2026)
```

---

## 🛠️ 2. Stack Tecnológico

### Comparación: Original vs Actual

| Componente | Original | Actual |
|------------|----------|--------|
| **Java** | 8 | 21 LTS |
| **Framework** | N/A | Spring Boot 3.2.9 |
| **Base de Datos** | MySQL 5.7 | PostgreSQL 16 |
| **GUI** | Swing (Monolito) | Swing + REST Client |
| **API** | No existe | REST API |
| **Contenedores** | No | Docker + Docker Compose |
| **Pool de Conexiones** | No | HikariCP |
| **Documentación API** | No | Swagger/OpenAPI |

### Estructura de Módulos Maven

```
notaire/
├── backend-api/          # API REST con Spring Boot
├── frontend-swing/       # Cliente GUI Swing
├── notaire-shared/       # DTOs y código común
├── init-db/              # Scripts de PostgreSQL
└── scripts/              # Scripts de automatización
```

---

## 🏗️ 3. Arquitectura del Sistema

### Arquitectura Actual (Refactorizada)

![Arquitectura Actual](images/arquitectura-notaire.png)

| Capa | Componente | Descripción |
|------|------------|-------------|
| **🎨 Frontend** | GUI Views | Formularios Swing (JFrame, JPanel) |
| | GUI Controllers | Event Handlers con SwingWorker |
| | REST Client | HttpClient para consumo de API |
| | GUI Models | TableModel, ComboBoxModel |
| **⚙️ Backend** | REST Controllers | Endpoints con @RestController |
| | Business Services | Lógica de negocio con @Service |
| | JPA Controllers | Persistencia con Hibernate |
| | Domain Entities | Objetos del dominio |
| **🗄️ Datos** | PostgreSQL 16 | Base de datos en Docker |
| | HikariCP | Pool de conexiones |
| **🔗 Shared** | notary-shared | DTOs y código común |

---

## 🚀 4. Cómo Ejecutar el Proyecto

### Prerrequisitos

| Herramienta | Versión Mínima |
|-------------|----------------|
| Java | 21 LTS |
| Maven | 3.8+ |
| Docker | 24+ |
| Docker Compose | 2.0+ |

### Iniciar la Aplicación

#### Opción 1: Backend + Frontend completo

```bash
# 1. Iniciar servicios con Docker Compose (PostgreSQL)
bash scripts/start.sh

# 2. Compilar el proyecto completo
mvn clean install

# 3. Ejecutar el Backend (API REST)
cd backend-api && mvn spring-boot:run

# 4. Ejecutar el Frontend (en otra terminal)
cd frontend-swing && mvn exec:java -Dexec.mainClass="com.licensis.notaire.gui.Login"
```

#### Opción 2: Solo Frontend (con backend ya corriendo)

```bash
# El frontend requiere que la API esté corriendo en http://localhost:8080
cd frontend-swing && mvn exec:java -Dexec.mainClass="com.licensis.notaire.gui.Login"
```

#### Opción 3: Generar JAR ejecutable

```bash
# Generar JAR con todas las dependencias
cd frontend-swing && mvn clean package

# Ejecutar el JAR
java -jar frontend-swing/target/frontend-swing-jar-with-dependencies.jar
```

### Comandos de Build

```bash
# Compilar todo el proyecto
mvn clean install

# Compilar un módulo específico
mvn clean install -pl backend-api

# Package para despliegue
mvn clean package

# Skip tests durante build
mvn clean install -DskipTests
```

### Comandos de Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un módulo
mvn test -pl backend-api

# Ejecutar una clase de test
mvn test -Dtest=DocumentServiceTest

# Ejecutar un método de test
mvn test -Dtest=DocumentServiceTest#shouldCreateDocument

# Ejecutar tests que coincidan con un patrón
mvn test -Dtest="*ServiceTest"

# Tests de integración HTTP
bash scripts/test.sh

# Run tests with coverage
mvn test -pl backend-api

# Generate HTML report
mvn jacoco:report -pl backend-api

# View report
open backend-api/target/site/jacoco/index.html
```

### Acceder a la Aplicación

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentación interactiva de la API |
| **Health Check** | http://localhost:8080/actuator/health | Estado de salud del servicio |
| **pgAdmin** | http://localhost:5050 | Interfaz web para PostgreSQL |
| **API Base** | http://localhost:8080/api/v1 | Endpoint base de la API REST |

### Detener la Aplicación

```bash
# Detener servicios
bash scripts/stop.sh

# Ver logs
bash scripts/logs.sh
```

---

## 🤖 5. Herramientas de IA Utilizadas

El proceso de migración fue asistido por diferentes herramientas de IA que evolucionaron con las necesidades del proyecto.

### Evolución de Herramientas

| Herramienta | Período | Uso Principal |
|------------------------|---------|---------------------------------------------------------------|
| **Google Antigravity** | Inicio | Búsqueda de patrones, entender código existente |
| **VS Code + Copilot** | Medio | Autocompletado, refactoring inline, tests |
| **Cursor** | Medio-Avanzado | Edit multi-archivo, búsqueda semántica, reglas personalizadas |
| **OpenCode** | Actual | Herramientas nativas, MCPs, control total |
| **Claude Code** | Actual | Como complemento a OpenCode |

### ¿Por qué OpenCode?

1. **Gratuito y open source** - No requiere suscripción
2. **Arquitectura extensible** - MCPs permiten conectar cualquier herramienta
3. **Herramientas nativas** - bash, archivos, git integrados
4. **Comunidad activa** - Desarrollo constante de nuevas features
5. **Perfecto para DevOps** - Ejecutar Docker, compilar, testear desde el chat

---

## ⚙️ 6. Configuración de OpenCode y Skills

### Skills Configurados

El proyecto cuenta con **7 skills** especializados en `.claude/skills/`:

| Skill | Descripción |
|-------|-------------|
| **java** | Desarrollo Java profesional JDK 17+ |
| **Java** | Reglas críticas para evitar bugs comunes |
| **senior-backend** | APIs REST, PostgreSQL, seguridad backend |
| **test-master** | Testing y QA (unit, integration, E2E) |
| **senior-devops** | CI/CD, Docker, Kubernetes |
| **agile-product-owner** | Gestión de sprints y backlog |
| **software-functional-analyst** | Análisis funcional y modelado de datos |

### AGENTS.md

Archivo principal de configuración del agente.

**Ubicación**: `.claude/AGENTS.md`

Contiene:
- Comandos de build y ejecución
- Convenciones de código Java
- Arquitectura backend y frontend
- Reglas de calidad de código
- Patrones prohibidos

### Documentación de Desarrollo

Ver la Wiki del proyecto: https://github.com/matiaspakua/notaire/wiki

### MCPs Configurados

| MCP Server | Propósito |
|-------------|-----------|
| **Filesystem** | Acceso al sistema de archivos |
| **Draw.io** | Diagramas de arquitectura |
| **Git** | Control de versiones |
| **Web Fetch** | Consultar documentación |

---

## 📚 7. Documentación del Proyecto

Toda la documentación del proyecto está organizada en la carpeta `/docs` siguiendo una estructura clara y jerárquica.

### 🗂️ Estructura de Documentación

```
docs/
├── 01-business/           # 📋 Requerimientos y análisis de negocio
│   ├── README.md
│   ├── 01-requirements/   # 📋 Requerimientos funcionales
│   ├── 02-use-cases/      # 🎯 Casos de uso
│   ├── 03-actors/         # 👥 Actores del sistema
│   ├── 04-data-model/     # 💾 Modelo de datos
│   └── 05-manuals/        # 📖 Manuales de usuario
├── 02-architecture/       # 🏗️ Arquitectura del sistema
│   ├── README.md
│   ├── 01-adr/           # 📝 Decisiones de arquitectura
│   ├── 02-overview/      # 📊 SAD y diagramas
│   ├── 03-diagrams/      # 🎨 Diagramas técnicos
│   ├── 04-patterns/      # 🔄 Patrones de diseño
│   └── sad.md            # 📋 Software Architecture Document
├── 03-development/        # 💻 Desarrollo y código
│   ├── README.md
│   ├── 01-setup/         # ⚙️ Configuración del entorno
│   ├── 02-build/         # 🔨 Build y despliegue
│   ├── 03-testing/       # 🧪 Testing y QA
│   └── 04-code-standards/# 📏 Estándares de código
├── 04-operations/         # 🚀 Operaciones y DevOps
│   ├── README.md
│   ├── 01-devsecops/     # 🔒 DevSecOps
│   ├── 02-deployment/    # 🚀 Despliegue
│   ├── 03-security/      # 🔐 Seguridad
│   ├── 04-monitoring/    # 📊 Monitoreo
│   └── 05-backup/        # 💾 Backup y recuperación
├── 05-api/                # 🔌 Documentación de API
│   ├── README.md
│   ├── 01-overview/      # 📋 Visión general
│   ├── 02-endpoints/     # 🎯 Endpoints
│   ├── 03-schemas/       # 📋 Esquemas de datos
│   └── 04-examples/      # 💡 Ejemplos de uso
└── 06-learning/           # 🎓 Recursos de aprendizaje
    ├── README.md
    ├── 01-onboarding/    # 👋 Incorporación de nuevos devs
    ├── 02-architecture-overview/  # 🏛️ Visión general arquitectura
    └── 03-refactoring-guide/      # 🔄 Guía de refactoring
```

### 🧭 Cómo Navegar la Documentación

#### Para Desarrolladores Nuevos:
1. **Inicio**: Lee `docs/README.md` para entender la estructura general
2. **Setup**: Ve a `docs/03-development/01-setup/` para configurar el entorno
3. **Arquitectura**: Revisa `docs/02-architecture/02-overview/sad.md` (SAD)
4. **API**: Consulta `docs/05-api/` para entender los endpoints
5. **Testing**: Lee `docs/03-development/03-testing/` para estrategias de testing

#### Para Contribuidores:
1. **Estándares**: Revisa `docs/03-development/04-code-standards/`
2. **ADR**: Lee decisiones de arquitectura en `docs/02-architecture/01-adr/`
3. **DevOps**: Consulta `docs/04-operations/` para procesos de CI/CD

#### Para Stakeholders:
1. **Requerimientos**: `docs/01-business/01-requirements/`
2. **Casos de Uso**: `docs/01-business/02-use-cases/`
3. **Arquitectura**: `docs/02-architecture/02-overview/sad.md`

### 📖 Documentos Clave

| Documento | Descripción | Ubicación |
|-----------|-------------|-----------|
| **SAD** | Software Architecture Document | `docs/02-architecture/02-overview/sad.md` |
| **SRS** | Software Requirements Specification | `docs/01-business/01-requirements/SRS.md` |
| **ADR** | Architecture Decision Records | `docs/02-architecture/01-adr/` |
| **Testing Strategy** | Estrategia de Testing | `docs/03-development/03-testing/TEST_STRATEGY.md` |
| **Setup Guide** | Guía de Configuración | `docs/03-development/01-setup/` |

---

## 📅 8. Cronología de la Migración

### Fase 1: Orígenes (2014-2018)

| Fecha | Descripción |
|-------|-------------|
| 2014-03 | Primer commit, log4j, iconos |
| 2014-03 | Primer TestCase con JUnit |
| 2014-04 | JOB en Jenkins |
| 2016-04 | CI para GitLab |
| 2018-07 | CI con MySQL para testing |

### Fase 2: Inicio del Refactoring (Diciembre 2025)

| Fecha | Descripción |
|-------|-------------|
| 2025-12-20 | Separación inicial en módulos Maven |
| 2025-12-20 | Upgrade Java 8 → 21, Spring Boot 2.7 → 3.2.9 |
| 2025-12-20 | Migración javax → jakarta |
| 2025-12-22 | Limpieza general del proyecto |

### Fase 3: Refactoring Principal (Enero-Febrero 2026)

| Fecha | Descripción |
|-------|-------------|
| 2026-01-31 | Refactorización general del código |
| 2026-02-05 | Separación de capas (Controller/Service/Repository) |
| 2026-02-19 | Creación formal del plan de migración |
| 2026-02-20 | Migración de formularios por lotes |
| 2026-02-21 | Batch migration - Clientes |
| 2026-02-24 | Mejoras en AGENTS.md |

### Estado Actual (Febrero 2026...)

| Componente | Estado | Detalle |
|------------|--------|---------|
| **Backend API** | ✅ | Endpoints críticos listos |
| **Swing Forms** | 🔄 | ~30 formularios en migración |
| **Reportes PDF** | ✅ | 10 endpoints JasperReports |
| **Docker Compose** | ✅ | postgres + backend + pgadmin |
| **Tests E2E** | 🔄 | Shell tests, JUnit domain tests |

---

## Screenshots

### Login

![Login](images/login.png)

### Principal

![Principal](images/principal.png)

---

## CI/CD - Pipeline de Integración y Entrega Continua

### Visión General

El proyecto Notaire utiliza GitHub Actions para implementar CI/CD con las siguientes características:

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐
│   Commit    │ -> │     CI      │ -> │     CD     │ -> │  Production │
│             │    │ Build/Test  │    │   Docker   │    │   Deploy   │
└─────────────┘    └──────────────┘    └─────────────┘    └─────────────┘
```

### Workflows Implementados

#### 1. CI - Build, Test & Security (`ci.yml`)

Ejecuta en cada push y PR:

| Job | Descripción | Tiempo estimado |
|-----|-------------|-----------------|
| `build` | Compilación Maven | ~2 min |
| `unit-tests` | Tests unitarios | ~3 min |
| `integration-tests` | Tests de integración | ~5 min |
| `coverage` | JaCoCo coverage report | ~3 min |
| `security` | Trivy vulnerability scan | ~2 min |
| `docker-build` | Build imagen Docker | ~5 min |

---

## ✅ 9. Tareas Pendientes

### Próximas Prioridades

- [ ] Completar migración del frontend a Next.js
- [ ] Implementar autenticación JWT
- [ ] Agregar tests de integración end-to-end
- [ ] Configurar monitoreo con Prometheus/Grafana
- [ ] Implementar logging estructurado
- [ ] Crear documentación de deployment
- [ ] Migrar reportes JasperReports a nueva arquitectura
- [ ] Implementar cache con Redis
- [ ] Agregar validación de entrada con Bean Validation
- [ ] Crear API de notificaciones

### Mejoras Futuras

- [ ] Migración completa a microservicios
- [ ] Implementación de GraphQL
- [ ] Container orchestration con Kubernetes
- [ ] CI/CD avanzado con Blue-Green deployment
- [ ] Implementación de feature flags
- [ ] Sistema de auditoría avanzado
- [ ] Integración con servicios externos
- [ ] API Gateway con Spring Cloud Gateway

---

<div align="center">

**🎉 ¡Gracias por contribuir al proyecto Notaire! 🎉**

*Para más información, consulta la [documentación completa](docs/README.md) o abre un [issue](https://github.com/matiaspakua/notaire/issues) en GitHub.*

[![GitHub issues](https://img.shields.io/github/issues/matiaspakua/notaire)](https://github.com/matiaspakua/notaire/issues)
[![GitHub stars](https://img.shields.io/github/stars/matiaspakua/notaire)](https://github.com/matiaspakua/notaire/stargazers)
[![GitHub license](https://img.shields.io/github/license/matiaspakua/notaire)](https://github.com/matiaspakua/notaire/blob/main/LICENSE)

</div>

---

## 3. Arquitectura del Sistema


### Arquitectura Actual (Refactorizada)

![Arquitectura Actual](images/arquitectura-notaire.png)

| Capa | Componente | Descripción |
|------|------------|-------------|
| **Frontend** | GUI Views | Formularios Swing (JFrame, JPanel) |
| | GUI Controllers | Event Handlers con SwingWorker |
| | REST Client | HttpClient para consumo de API |
| | GUI Models | TableModel, ComboBoxModel |
| **Backend** | REST Controllers | Endpoints con @RestController |
| | Business Services | Lógica de negocio con @Service |
| | JPA Controllers | Persistencia con Hibernate |
| | Domain Entities | Objetos del dominio |
| **Datos** | PostgreSQL 16 | Base de datos en Docker |
| | HikariCP | Pool de conexiones |
| **Shared** | notary-shared | DTOs y código común |

---

## 4. Cómo Ejecutar el Proyecto

### Prerrequisitos

| Herramienta | Versión Mínima |
|-------------|----------------|
| Java | 21 LTS |
| Maven | 3.8+ |
| Docker | 24+ |
| Docker Compose | 2.0+ |

### Iniciar la Aplicación

#### Opción 1: Backend + Frontend completo

```bash
# 1. Iniciar servicios con Docker Compose (PostgreSQL)
bash scripts/start.sh

# 2. Compilar el proyecto completo
mvn clean install

# 3. Ejecutar el Backend (API REST)
cd backend-api && mvn spring-boot:run

# 4. Ejecutar el Frontend (en otra terminal)
cd frontend-swing && mvn exec:java -Dexec.mainClass="com.licensis.notaire.gui.Login"
```

#### Opción 2: Solo Frontend (con backend ya corriendo)

```bash
# El frontend requiere que la API esté corriendo en http://localhost:8080
cd frontend-swing && mvn exec:java -Dexec.mainClass="com.licensis.notaire.gui.Login"
```

#### Opción 3: Generar JAR ejecutable

```bash
# Generar JAR con todas las dependencias
cd frontend-swing && mvn clean package

# Ejecutar el JAR
java -jar frontend-swing/target/frontend-swing-jar-with-dependencies.jar
```

### Comandos de Build

```bash
# Compilar todo el proyecto
mvn clean install

# Compilar un módulo específico
mvn clean install -pl backend-api

# Package para despliegue
mvn clean package

# Skip tests durante build
mvn clean install -DskipTests
```

### Comandos de Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un módulo
mvn test -pl backend-api

# Ejecutar una clase de test
mvn test -Dtest=DocumentServiceTest

# Ejecutar un método de test
mvn test -Dtest=DocumentServiceTest#shouldCreateDocument

# Ejecutar tests que coincidan con un patrón
mvn test -Dtest="*ServiceTest"

# Tests de integración HTTP
bash scripts/test.sh

# Run tests with coverage
mvn test -pl backend-api

# Generate HTML report
mvn jacoco:report -pl backend-api

# View report
open backend-api/target/site/jacoco/index.html
```

### Acceder a la Aplicación

| Servicio | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **Health Check** | http://localhost:8080/actuator/health |
| **pgAdmin** | http://localhost:5050 |
| **API Base** | http://localhost:8080/api/v1 |

### Detener la Aplicación

```bash
# Detener servicios
bash scripts/stop.sh

# Ver logs
bash scripts/logs.sh
```

---

## 5. Herramientas de IA Utilizadas

El proceso de migración fue asistido por diferentes herramientas de IA que evolucionaron con las necesidades del proyecto.

### Evolución de Herramientas

| Herramienta            | Período | Uso Principal                                                 |
|------------------------|---------|---------------------------------------------------------------|
| **Google Antigravity** | Inicio | Búsqueda de patrones, entender código existente               |
| **VS Code + Copilot**  | Medio | Autocompletado, refactoring inline, tests                     |
| **Cursor**             | Medio-Avanzado | Edit multi-archivo, búsqueda semántica, reglas personalizadas |
| **OpenCode**           | Actual | Herramientas nativas, MCPs, control total                     |
| **Claude Code**        | Actual | Como complemento a OpenCode                                   |

### ¿Por qué OpenCode?

1. **Gratuito y open source** - No requiere suscripción
2. **Arquitectura extensible** - MCPs permiten conectar cualquier herramienta
3. **Herramientas nativas** - bash, archivos, git integrados
4. **Comunidad activa** - Desarrollo constante de nuevas features
5. **Perfecto para DevOps** - Ejecutar Docker, compilar, testear desde el chat

---

## 6. Configuración de OpenCode y Skills

### Skills Configurados

El proyecto cuenta con **7 skills** especializados en `.agents/skills/`:

| Skill | Descripción |
|-------|-------------|
| **java** | Desarrollo Java profesional JDK 17+ |
| **Java** | Reglas críticas para evitar bugs comunes |
| **senior-backend** | APIs REST, PostgreSQL, seguridad backend |
| **test-master** | Testing y QA (unit, integration, E2E) |
| **senior-devops** | CI/CD, Docker, Kubernetes |
| **agile-product-owner** | Gestión de sprints y backlog |
| **software-functional-analyst** | Análisis funcional y modelado de datos |

### AGENTS.md

Archivo principal de configuración del agente.

**Ubicación**: `.agents/AGENTS.md`

Contiene:
- Comandos de build y ejecución
- Convenciones de código Java
- Arquitectura backend y frontend
- Reglas de calidad de código
- Patrones prohibidos

### Documentación de Desarrollo

Ver la Wiki del proyecto: https://github.com/matiaspakua/notaire/wiki



### Configuración de MCPs

| MCP Server | Propósito |
|-------------|-----------|
| **Filesystem** | Acceso al sistema de archivos |
| **Draw.io** | Diagramas de arquitectura |
| **Git** | Control de versiones |
| **Web Fetch** | Consultar documentación |

#### Instalar servidor MCP drawio

```bash
$ sudo npm install -g @drawio/mcp
```


### Instalación y configuración de Github

```bash
(type -p wget >/dev/null || (sudo apt update && sudo apt install wget -y)) \
	&& sudo mkdir -p -m 755 /etc/apt/keyrings \
	&& out=$(mktemp) && wget -nv -O$out https://cli.github.com/packages/githubcli-archive-keyring.gpg \
	&& cat $out | sudo tee /etc/apt/keyrings/githubcli-archive-keyring.gpg > /dev/null \
	&& sudo chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg \
	&& sudo mkdir -p -m 755 /etc/apt/sources.list.d \
	&& echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
	&& sudo apt update \
	&& sudo apt install gh -y
```


Referencia:
https://github.com/cli/cli/blob/trunk/docs/install_linux.md

Luego de la instalación, hace falta conectar el CLI de Github con la cuenta personal y luego, utilizar OpenCode (o claude) para que pueda usar el CLI de github y crear PR, ISSUES y otras acciones necesarias en el repositorio.

El siguiente comando inicia el proceso para autorizar con github:

```bash
$ gh auth login
```


### Hooks de OpenCode (simil Claude Code)

https://dev.to/einarcesar/does-opencode-support-hooks-a-complete-guide-to-extensibility-k3p

Para agregar controles custom antes y despues de cada acción a ejecutar por los modelos.



### Sub-Agentes

https://opencode.ai/docs/agents/




### Migración de Base de Datos

| Etapa | Estado |
|-------|--------|
| Export desde MySQL 5.7 | ✅ Completado |
| Conversión de tipos | ✅ Completado |
| Creación de schema | ✅ Completado |
| Carga de datos | ✅ Completado |
| Validación | ✅ Completado |

---

## 8. Cronología de la Migración

### Fase 1: Orígenes (2014-2018)

| Fecha | Descripción |
|-------|-------------|
| 2014-03 | Primer commit, log4j, iconos |
| 2014-03 | Primer TestCase con JUnit |
| 2014-04 | JOB en Jenkins |
| 2016-04 | CI para GitLab |
| 2018-07 | CI con MySQL para testing |

### Fase 2: Inicio del Refactoring (Diciembre 2025)

| Fecha | Descripción |
|-------|-------------|
| 2025-12-20 | Separación inicial en módulos Maven |
| 2025-12-20 | Upgrade Java 8 → 21, Spring Boot 2.7 → 3.2.9 |
| 2025-12-20 | Migración javax → jakarta |
| 2025-12-22 | Limpieza general del proyecto |

### Fase 3: Refactoring Principal (Enero-Febrero 2026)

| Fecha | Descripción |
|-------|-------------|
| 2026-01-31 | Refactorización general del código |
| 2026-02-05 | Separación de capas (Controller/Service/Repository) |
| 2026-02-19 | Creación formal del plan de migración |
| 2026-02-20 | Migración de formularios por lotes |
| 2026-02-21 | Batch migration - Clientes |
| 2026-02-24 | Mejoras en AGENTS.md |

### Estado Actual (Febrero 2026...)

| Componente | Estado | Detalle |
|------------|--------|---------|
| **Backend API** | ✅ | Endpoints críticos listos |
| **Swing Forms** | 🔄 | ~30 formularios en migración |
| **Reportes PDF** | ✅ | 10 endpoints JasperReports |
| **Docker Compose** | ✅ | postgres + backend + pgadmin |
| **Tests E2E** | 🔄 | Shell tests, JUnit domain tests |


## Screenshots

### Login

![Login](images/login.png)

### Principal

![Principal](images/principal.png)

---

## CI/CD - Pipeline de Integración y Entrega Continua

### Visión General

El proyecto Notaire utiliza GitHub Actions para implementar CI/CD con las siguientes características:

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐
│   Commit    │ -> │     CI      │ -> │     CD     │ -> │  Production │
│             │    │ Build/Test  │    │   Docker   │    │   Deploy   │
└─────────────┘    └──────────────┘    └─────────────┘    └─────────────┘
```

### Workflows Implementados

#### 1. CI - Build, Test & Security (`ci.yml`)

Ejecuta en cada push y PR:

| Job | Descripción | Tiempo estimado |
|-----|-------------|-----------------|
| `build` | Compilación Maven | ~2 min |
| `unit-tests` | Tests unitarios | ~3 min |
| `integration-tests` | Tests de integración | ~5 min |
| `coverage` | JaCoCo coverage report | ~3 min |
| `security` | Trivy vulnerability scan | ~2 min |
| `docker-build` | Build imagen Docker | ~5 min |
| `quality` | SpotBugs checkstyle | ~2 min |

#### 2. CD - Build & Publish Docker (`cd.yml`)

Publica imagen Docker cuando:
- Se hace push a `main`
- Se crea un tag `v*`

Ubicación: `ghcr.io/matiaspakua/notaire/backend`

#### 3. PR Validation (`pr-validation.yml`)

Valida pull requests:
- Título y descripción semántica
- Compilación rápida
- Análisis de dependencias
- Lint con Checkstyle
- Auto-comentario en PR

### Configuración de Tests

```bash
# Ejecutar tests unitarios
mvn test -pl backend-api -Dtest="**/unit/*"

# Ejecutar tests de integración  
mvn test -pl backend-api -Dtest="**/integration/*"

# Coverage con JaCoCo
mvn test -pl backend-api
mvn jacoco:report -pl backend-api

# Ver reporte
# backend-api/target/site/jacoco/index.html
```

### Cobertura de Código

- **Target**: 80% mínimo
- **Method**: 80% mínimo
- **Branch**: 80% mínimo

El reporte se publica automáticamente en PRs con comentarios de cobertura.

### Seguridad

#### Escaneo de Vulnerabilidades

| Herramienta | Qué escanea |
|-------------|-------------|
| Trivy | Contenedores y dependencias |
| SpotBugs | Bytecode Java |
| Dependabot | Actualizaciones de dependencias |
| GitHub Code Scanning | SAST integrado |

#### Imágenes Docker Seguras

- Base: `eclipse-temurin:21-jre-alpine` (~180MB)
- Usuario no-root
- Health checks integrados
- Scaneo con Trivy en CI

### Dependabot

Configurado para:
- Actualizaciones semanales de Maven
- Actualizaciones de GitHub Actions
- Grupos de dependencias

### GitHub Packages

La imagen Docker se publica en:
- **ghcr.io** (GitHub Container Registry)

```bash
# Pull de la imagen
docker pull ghcr.io/matiaspakua/notaire/backend:latest
```

### Variables de Entorno para CI

```yaml
JAVA_VERSION: '21'
MAVEN_OPTS: -Xmx1024m -XX:MaxMetaspaceSize=512m
REGISTRY: ghcr.io
```

---

## Cómo Editar la Wiki

La wiki del proyecto está alojada en el repositorio separado del proyecto principal. Para editar o agregar contenido:

### Método 1: Clonar el repositorio de wiki

```bash
# Clonar el repositorio de wiki
git clone https://github.com/matiaspakua/notaire.wiki.git

# Agregar o editar archivos markdown
cdmaire.wiki

# Los archivos .md se convierten en páginas de wiki
# Ejemplo: Home.md -> página "Home"

# Commit y push
git add .
git commit -m "Agregar nueva página"
git push
```

### Método 2: Desde la interfaz de GitHub

1. Ir a https://github.com/matiaspakua/notaire/wiki
2. Click en "New Page" o editar una página existente
3. Escribir el contenido en Markdown
4. Guardar la página

### Estructura de archivos en `docs/wiki/`

Los archivos Markdown en esta carpeta son el espejo de la wiki. La wiki es la fuente de verdad. Cambios en `docs/wiki/` se sincronizan manualmente a la wiki:

#### Documentación del Negocio (en wiki)
| Archivo | Página en Wiki | Descripción |
|---------|----------------|-------------|
| `Business-Documentation.md` | Documentación de Negocio | Índice general de todas las secciones |
| (08 secciones en subdirs) | 00-09 Sectores SDLC | Cronograma, Requerimientos, Actores, Casos, Datos, Progreso, Manuales, EA, App, Templates |

#### Documentación del Desarrollo (migrada a wiki)
| Archivo | Página en Wiki | Descripción |
|---------|----------------|-------------|
| `Home.md` | Home | Página principal y descripción del proyecto |
| `Development-Setup.md` | Development Setup | Configuración del ambiente de desarrollo |
| `Refactoring-Plan.md` | Refactoring Plan | Plan de migración a microservicios |
| `PLAN.md` | Plan de Acción | Acciones pendientes (Marzo 2026) |
| `PLANO_REFACTORING.md` | Plan Técnico JPA | Detalles de migración JpaControllers → Spring Data |
| `DevSecOps-Pipeline.md` | DevSecOps Pipeline | CI/CD, seguridad, workflows |
| `SECURITY.md` | Security Policy | Políticas de seguridad |
| `CONTRIBUTING.md` | Contributing | Guía de contribución |
| (Agent-Sessions.md) | Agent Sessions | Logs de sesiones de IA (links a repo principal) |

### Estructura de la Wiki Actualizada (Marzo 2026)

**Estado:** Wiki completamente migrada desde `docs/` (vea issue #228)
- ✅ 9 documentos de desarrollo (Home, Setup, Plans, Security, Contributing)
- ✅ 10 secciones de documentación de negocio (Cronograma, Requerimientos, Actores, Casos, Datos, Progreso, Manuales, EA, App, Templates)
- ✅ 68 casos de uso individuales
- ✅ 29 imágenes y diagramas
- ✅ 5 archivos de tracking de progreso (CSV)

**Notas importantes:**
- La **wiki es la fuente de verdad** para documentación
- Ediciones se hacen directamente en el repositorio wiki (`matiaspakua/notaire.wiki`)
- Los archivos en `docs/wiki/` del repo principal son un espejo para referencia local
- Para cambios significativos, usar GitHub web UI o clonar `notaire.wiki.git`

---

## Recursos

| Recurso | URL |
|---------|-----|
| Documentación OpenCode | https://docs.opencode.ai |
| MCP Protocol | https://modelcontextprotocol.io |
| Spring Boot | https://spring.io/projects/spring-boot |
| Java 21 | https://docs.oracle.com/en/java/javase/21/ |
| PostgreSQL | https://www.postgresql.org/ |

---

*Documento generado como parte del proceso de modernización del proyecto Notaire - Febrero 2026*
