# 📜 Notaire

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ed?logo=docker)](https://www.docker.com/)

[![CI - Build & Test](https://github.com/matiaspakua/notaire/actions/workflows/ci.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/ci.yml)
[![CD - Docker](https://github.com/matiaspakua/notaire/actions/workflows/cd.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/cd.yml)
[![PR Validation](https://github.com/matiaspakua/notaire/actions/workflows/pr-validation.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/pr-validation.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

### Sistema de gestión para escribanía, modernizado desde un monolito Java Swing hacia arquitectura de microservicios.

</div>

---

## 🎯 Sobre el Proyecto

Notaire es la **modernización completa** de un sistema de administración de escribanía, transformando una aplicación de escritorio Java Swing monolítica en una arquitectura moderna de microservicios con API REST.

### Estado del Proyecto

| Fase | Estado | Descripción |
|------|--------|-------------|
| 🏗️ **Backend API** | ✅ Completado | REST API con Spring Boot |
| 🔄 **Frontend Swing** | 🔄 En Transición | Cliente transicional REST |
| 📊 **Base de Datos** | ✅ Completado | PostgreSQL con Flyway |
| 📱 **Frontend Moderno** | 📋 Planificado | Próxima iteración |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                        ARQUITECTURA NOTAIRE                      │
└─────────────────────────────────────────────────────────────────┘

    ┌─────────────┐         ┌─────────────────────────────────┐
    │   USUARIO   │         │         CLIENTES                │
    └──────┬──────┘         └───────────────┬─────────────────┘
           │                                  │
           │         ┌───────────────────────┼───────────────────────┐
           │         │                       │                       │
           ▼         ▼                       ▼                       │
    ┌─────────────────────────────────────────────────────────────┐
    │                      BACKEND API                            │
    │                    Spring Boot 4.0.4                        │
    │                  Java 21 + PostgreSQL 16                    │
    ├─────────────────────────────────────────────────────────────┤
    │                                                             │
    │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐      │
    │   │   API   │  │ SERVICE │  │REPOSITORY│ │ ENTITIES │      │
    │   │/api/v1/*│  │ Business│  │  JPA    │  │  JPA    │      │
    │   └─────────┘  │  Logic  │  └─────────┘  └─────────┘      │
    │                 └─────────┘                                 │
    │                                                             │
    └──────────────────────────┬──────────────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │     POSTGRESQL 16    │
                    │    (Docker + Flyway) │
                    └─────────────────────┘
```

### Módulos del Proyecto

```
notaire/
├── 📦 backend-api/          # REST API (Spring Boot)
│   ├── api/                 # Controllers REST
│   ├── service/             # Lógica de negocio
│   ├── repository/          # Spring Data JPA
│   └── negocio/             # Entidades JPA
│
├── 🖥️ frontend-swing/        # Cliente transicional (Swing)
│   ├── gui/                 # Vistas Swing
│   └── api/client/          # Cliente REST
│
├── 🔗 notaire-shared/       # DTOs y contratos compartidos
│
├── 🗄️ init-db/              # Scripts PostgreSQL
│
├── 📚 docs/                 # Documentación completa
│   ├── 01-business/         # Requisitos y casos de uso
│   ├── 02-architecture/     # ADRs y diagramas
│   ├── 03-development/      # Setup y guías técnicas
│   ├── 04-operations/       # CI/CD y seguridad
│   └── 05-api/              # Referencia de API
│
└── 🐳 docker-compose.yml    # Orquestación Docker
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 21+
- Maven 3.9+
- Docker y Docker Compose

### 1. Clonar y ejecutar

```bash
# Clonar repositorio
git clone https://github.com/matiaspakua/notaire.git
cd notaire

# Iniciar todos los servicios (PostgreSQL + API)
bash scripts/start.sh
```

### 2. Verificar

| Servicio | URL |
|----------|-----|
| 🌐 **API Backend** | http://localhost:8080 |
| 📖 **Swagger UI** | http://localhost:8080/swagger-ui.html |
| 💚 **Health Check** | http://localhost:8080/actuator/health |
| 🗄️ **pgAdmin** | http://localhost:5050 |

---

## 📊 Estadísticas del Proyecto

```
Commits:        253+
Contribuidores:  2
Java Files:     23
Módulos:         4
APIs:           12+
Casos de Uso:   54
```

### Cobertura de Código

| Módulo | Cobertura | Estado |
|--------|----------|--------|
| backend-api | 80%+ | ✅ Verificado |

---

## 🧪 Testing

```bash
# Todos los tests
mvn test

# Tests con cobertura
mvn test && mvn jacoco:report

# Tests específicos
mvn test -pl backend-api -Dtest=*ControllerTest
```

### Tipos de Tests

| Tipo | Descripción | Herramienta |
|------|-------------|------------|
| 🧪 Unitarios | Lógica de negocio | JUnit 5 + Mockito |
| 🔗 Integración | API + DB | Spring Boot Test |
| 🎭 E2E | Flujos de usuario | Robot Framework |
| 📡 HTTP | Endpoints REST | Bruno/curl |

---

## 📁 Estructura de Documentación

```
docs/
├── README.md                    # Índice general
├── 01-business/                # 📋 Requisitos y Negocio
│   ├── 01-requirements/        # Historias de usuario
│   ├── 02-usecases/           # Casos de uso (54 CUs)
│   ├── 03-data-model/         # Modelo de datos
│   └── 04-manuals/            # Manuales de usuario
│
├── 02-architecture/           # 🏛️ Arquitectura
│   ├── 01-adr/                # Architecture Decision Records
│   ├── 02-overview/            # SAD y diagramas
│   └── 03-sar/               # Software Architecture Reports
│
├── 03-development/             # 🛠️ Desarrollo
│   ├── 01-setup/              # Setup de entorno
│   ├── 02-build/              # Guías de build
│   └── 03-testing/            # Estrategia de testing
│
├── 04-operations/              # ⚙️ Operaciones
│   ├── 01-cicd/              # Pipelines CI/CD
│   ├── 02-deployment/         # Despliegue
│   └── 03-security/           # Seguridad
│
├── 05-api/                     # 📡 API REST
│   └── openapi/               # Especificaciones OpenAPI
│
└── 06-learning/               # 📚 Aprendizaje
    └── onboarding/            # Guías para nuevos devs
```

---

## 🔧 Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|------------|---------|
| ☕ **Runtime** | Java | 21 |
| 🌱 **Framework** | Spring Boot | 4.0.4 |
| 🗄️ **Database** | PostgreSQL | 16 |
| 🐳 **Container** | Docker | Latest |
| 📊 **ORM** | Spring Data JPA | - |
| 🧪 **Testing** | JUnit 5, Mockito | - |
| 📈 **Coverage** | JaCoCo | 0.8.11 |
| 🔍 **Security** | Trivy | Latest |
| 📝 **Migrations** | Flyway | Latest |

---

## 🤝 Contribuir

1. Fork el repositorio
2. Crear una rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -m 'feat: agregar nueva funcionalidad'`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Abrir un Pull Request

### Convenciones

- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/)
- **Branches**: `<ISSUE-ID>/<type>/<description>`
- **Java**: Google Style + Checkstyle
- **Coverage**: Mínimo 80%

---

## 📖 Recursos

| Recurso | Enlace |
|---------|--------|
| 📘 Documentación | [/docs/README.md](docs/README.md) |
| 📡 API Swagger | http://localhost:8080/swagger-ui.html |
| 🐛 Issues | [GitHub Issues](https://github.com/matiaspakua/notaire/issues) |
| 📝 Changelog | [CHANGELOG.md](CHANGELOG.md) |

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">

**Desarrollado con ☕ y 🎵 por [Matías Miguez](https://github.com/matiaspakua)**

*Modernizando la escribanía del futuro, un microservicio a la vez.*

</div>
