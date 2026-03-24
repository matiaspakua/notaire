# Notaire - Sistema de Administración de Escribanía

![Logo](images/logoRojoLetraMediano.png)

> Sistema de gestión para escribanía, refactorizado desde un monolito Java Swing a una arquitectura moderna de microservicios con REST API.

---

## Tabla de Contenidos

1. [Resumen del Proyecto](#1-resumen-del-proyecto)
2. [Stack Tecnológico](#2-stack-tecnológico)
3. [Arquitectura del Sistema](#3-arquitectura-del-sistema)
4. [Cómo Ejecutar el Proyecto](#4-cómo-ejecutar-el-proyecto)
5. [Herramientas de IA Utilizadas](#5-herramientas-de-ia-utilizadas)
6. [Configuración de OpenCode y Skills](#6-configuración-de-opencode-y-skills)
7. [Documentación del Proyecto](#7-documentación-del-proyecto)
8. [Cronología de la Migración](#8-cronología-de-la-migración)
9. [Tareas Pendientes](#9-tareas-pendientes)

---

## 1. Resumen del Proyecto

**Notaire** es un sistema de administración para la gestión de escribanía, originalmente desarrollado hace más de 14 años (durante la universidad) como una aplicación monolítica con Java y con UI usando  Swing (de java, ahora ya casi en desuso) con conexión directa a MySQL.

El proyecto en si, tenia una arquitectura de capas sencillo, pero robusto para ser un monolito. El siguiente diagrama es un ejemplo de la arquitectura original:

### Arquitectura Original (Monolito)

![Arquitectura Original](images/arquitectura-original.png)

| Componente            | Descripción                                  |
| --------------------- | -------------------------------------------- |
| **GUI Swing**         | Interfaz gráfica con JFrame y JPanel         |
| **Event Handlers**    | Manejadores de eventos con lógica de negocio |
| **ControllerNegocio** | Clase central con toda la lógica de negocio  |
| **JDBC Directo**      | Conexiones SQL directas sin pooling          |
| **MySQL 5.7**         | Base de datos relacional (27 tablas)         |

El diagrama fue generado con OpenCode y el conector MCP a drawio local.


### Problemas del Código Original

| Problema | Descripción |
|----------|-------------|
| **Acoplamiento fuerte** | GUI Swing directamente conectada a MySQL mediante JDBC |
| **Sin separación de responsabilidades** | Lógica de negocio, presentación y acceso a datos entremezclados |
| **Tecnología obsoleta** | Java 6/8 sin actualizaciones de seguridad, MySQL 5.7 |
| **Sin integración externa** | No existía forma de comunicar la aplicación con otros sistemas |
| **Dificultad de mantenimiento** | Cualquier cambio requería modificar código de la interfaz gráfica |

### Visión del Proyecto Modernizado

```
Monolito Java Swing + MySQL    →    API REST + PostgreSQL + Docker
        (10+ años)                          (2026)
```

---

## 2. Stack Tecnológico

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


### IMPORTANTE

Respecto al ambiente de instalación, es decir:

- Java
- Git
- Maven
- Docker
- Python
- VSCode u otros IDE's
- etc.

Se asume que todas las herramientas ya se encuentran instaladas en el ambiente de desarrollo. 

Para más instrucciones, dirigirse a las paginas oficiales de cada proyecto e instalar las herramientas, según sea neccesario en cada ambiente.



### Estructura de Módulos Maven

```
notaire/
├── backend-api/          # API REST con Spring Boot
├── frontend-swing/       # Cliente GUI Swing
├── notary-shared/        # DTOs y código común
├── init-db/              # Scripts de PostgreSQL
└── scripts/              # Scripts de automatización
```

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

```bash
# 1. Iniciar servicios con Docker Compose
bash scripts/start.sh

# 2. Compilar el proyecto
mvn clean install

# 3. Ejecutar la aplicación (desde backend-api)
cd backend-api && mvn spring-boot:run
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

| Herramienta | Período | Uso Principal |
|------------|---------|----------------|
| **Google Antigravity** | Inicio | Búsqueda de patrones, entender código existente |
| **VS Code + Copilot** | Medio | Autocompletado, refactoring inline, tests |
| **Cursor** | Medio-Avanzado | Edit multi-archivo, búsqueda semántica, reglas personalizadas |
| **OpenCode** | Actual | Herramientas nativas, MCPs, control total |

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

**Ubicación**: `./AGENTS.md`

Contiene:
- Comandos de build y ejecución
- Convenciones de código Java
- Arquitectura backend y frontend
- Reglas de calidad de código
- Patrones prohibidos

### Documentación de Desarrollo

| Archivo | Descripción |
|---------|-------------|
| `docs/development/programming.md` | Mejores prácticas de Java fundamentales |
| `docs/development/refactoring.md` | Reglas para migrar monolito a microservicios |

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


---

## 7. Documentación del Proyecto

### Estructura de Documentación

```
notaire/
├── AGENTS.md                    # Configuración del agente IA
├── .agents/skills/              # Skills de OpenCode
│   ├── java/
│   ├── backend/
│   ├── testing/
│   ├── devops/
│   ├── product-owner/
│   └── analyst/
├── docs/
│   ├── business/                # Documentación de negocio
│   ├── development/             # Guías de desarrollo
│   └── ...
├── init-db/
│   ├── 01-schema.sql           # Esquema PostgreSQL
│   └── 02-data.sql             # Datos iniciales
└── scripts/
    ├── start.sh                # Iniciar aplicación
    ├── stop.sh                 # Detener aplicación
    └── test.sh                 # Tests de integración
```

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

### Estado Actual (Febrero 2026)

| Componente | Estado | Detalle |
|------------|--------|---------|
| **Backend API** | ✅ | Endpoints críticos listos |
| **Swing Forms** | 🔄 | ~30 formularios en migración |
| **Reportes PDF** | ✅ | 10 endpoints JasperReports |
| **Docker Compose** | ✅ | postgres + backend + pgadmin |
| **Tests E2E** | 🔄 | Shell tests, JUnit domain tests |

---

## 9. Tareas Pendientes

### Prioridad Alta - Validación Funcional

- [ ] Completar migración del 30% de formularios restantes
- [ ] Validar que todos los formularios funcionen con la API REST
- [ ] Probar flujos completos de negocio (cliente → gestión → presupuesto → pago)
- [ ] Verificar generación de reportes PDF
- [ ] Testing manual de casos edge

### Prioridad Alta - Backend

- [ ] Implementar autenticación JWT/OAuth2
- [ ] Completar el 5% de APIs restantes
- [ ] Agregar paginación a todos los endpoints

### Prioridad Media - Testing y Calidad

- [ ] Coverage de tests al 80% mínimo
- [ ] Tests de integración con Testcontainers
- [ ] Tests E2E automatizados
- [ ] Configurar pipeline CI/CD
- [ ] Análisis estático de código (SonarQube)
- [ ] Logs en formato Prometheus
- [ ] Visualización con Grafana
- [ ] Tests unitarios con tags a requerimientos

### Prioridad Media - Base de Datos

- [ ] Flyway para migración y versionado de base de datos

### Prioridad Baja - Despliegue

- [ ] Configurar Kubernetes (K8s)
- [ ] Implementar Docker multi-stage build
- [ ] Configurar HTTPS/TLS
- [ ] Setup de monitoreo (Prometheus, Grafana)
- [ ] Backup automatizado de PostgreSQL

### Prioridad Baja - Documentación

- [ ] Documentar todos los endpoints en Swagger
- [ ] Migrar documentación original a Markdown
- [ ] Crear guía de instalación para producción
- [ ] Documentar arquitectura del sistema
- [ ] Crear manual de usuario

---

## Screenshots

### Login

![Login](images/login.png)

### Principal

![Principal](images/principal.png)

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
