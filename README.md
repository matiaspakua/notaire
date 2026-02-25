# Notaire - Sistema de Administración de Escribanía

## Resumen del Proyecto

**Notaire** es un sistema de administración para la gestión de escribanía, originalmente desarrollado hace más de 10 años como una aplicación monolítica Java Swing con conexión directa a MySQL. Este proyecto documenta el proceso de refactoring y modernización completa del sistema hacia una arquitectura moderna de microservicios.

---

## Introducción: El Proceso de Refactoring

### ¿Por qué refactorizar?

El código original de Notaire, aunque funcional, presentaba varios problemas que motivaron el proceso de modernización:

1. **Acoplamiento fuerte**: La GUI Swing estaba directamente conectada a la base de datos MySQL mediante JDBC, sin ninguna capa de abstracción
2. **Sin separación de responsabilidades**: La lógica de negocio, la presentación y el acceso a datos estaban entremezcladas en los mismos formularios
3. **Tecnología obsoleta**: Java 8 (sin actualizaciones de seguridad), MySQL 5.7, y librerías sin soporte
4. **Sin integración externa**: No existía forma de comunicar la aplicación con otros sistemas
5. **Dificultad de mantenimiento**: Cualquier cambio requería modificar código directamente relacionado con la interfaz gráfica

### Visión del Proyecto Modernizado

El objetivo principal fue transformar el monolito Java en una arquitectura moderna:

```
Monolito Java Swing  →  API REST + PostgreSQL + Docker
(10+ años)               (2026)
```

**Arquitectura objetivo:**
- **Backend**: API REST con Spring Boot 3.2.9 y Java 21 LTS
- **Frontend**: Cliente Swing que consume la API REST
- **Base de Datos**: PostgreSQL 16 en contenedor Docker
- **Módulos**: Separación en backend-api, frontend-swing, notary-shared
- **Documentación**: Swagger/OpenAPI para la API REST

---

## Arquitectura Original (Monolito)

La siguiente imagen muestra la arquitectura original del proyecto Notaire:

![Arquitectura Original](images/arquitectura-original.png)

### Componentes de la Arquitectura Original

| Componente | Descripción |
|------------|-------------|
| **GUI Swing** | Interfaz gráfica con JFrame y JPanel |
| **Event Handlers** | Manejadores de eventos con lógica de presentación |
| **ControllerNegocio** | Clase central con toda la lógica de negocio |
| **JDBC Directo** | Conexiones SQL directas sin pooling |
| **MySQL 5.7** | Base de datos relacional (27 tablas) |

### Problemas Identificados

1. **Acoplamiento fuerte**: La capa GUI depende directamente de la base de datos
2. **Sin separación de capas**: UI + Negocio + Datos todo en el mismo código
3. **JDBC directo sin pooling**: Conexiones costosas y lentas
4. **Java 8 obsoleto**: Sin actualizaciones de seguridad
5. **Sin API REST**: No existe forma de integración con sistemas externos

---

## Arquitectura Actual (Refactorizada)

La siguiente imagen muestra la arquitectura actual del proyecto Notaire:

![Arquitectura Actual](images/arquitectura-notaire.png)

### Componentes de la Nueva Arquitectura

| Capa | Componente | Descripción |
|------|------------|-------------|
| **Frontend** | GUI Views | Formularios Swing (JFrame, JPanel) |
| | GUI Controllers | Event Handlers con SwingWorker |
| | REST Client | HttpClient para consumo de API |
| | GUI Models | TableModel, ComboBoxModel |
| **Backend** | REST Controllers | Endpoints con @RestController |
| | Business Services | Lógica de negocio con @Service |
| | JPA Controllers | Persistencia con Hibernate |
| | Domain Entities | Objetos del dominio (negocio package) |
| **Datos** | PostgreSQL 16 | Base de datos en Docker |
| | HikariCP | Pool de conexiones |
| **Shared** | notary-shared | DTOs y código común |

---

## Cronología del Proceso de Migración

### Fase 1: Orígenes y Mantenimiento (2014-2018)

| Fecha | Commit | Descripción |
|-------|--------|-------------|
| 2014-03-27 | `fa4de5e` | Arreglo de log4j, agregados paquetes de iconos |
| 2014-03-28 | `43d8d54` | Primer TestCase con JUnit |
| 2014-04-02 | `7e3e87b` | JOB en Jenkins para compilar Notaire |
| 2014-06-12 | `e187044` | Archivo de configuración para la aplicación |
| 2016-04-16 | `2969110` | Archivo de integración continua para GitLab |
| 2018-07-05 | `da7510c` | CI con MySQL para testing |

**Estado inicial**: Aplicación monolítica Java 7/8 con MySQL, GUI Swing, conexión directa a base de datos.

---

### Fase 2: Inicio del Refactoring (Diciembre 2025)

| Fecha | Commit | Descripción |
|-------|--------|-------------|
| 2025-12-20 | `778c25d` | **First separation of modules** - Separación inicial en módulos Maven |
| 2025-12-20 | `d7cf75b` | **Upgrade Java to 21** - Upgrade Java 8 → 21 y Spring Boot 2.7 → 3.2.9 |
| 2025-12-20 | `70fe932` | **Migrate javax to jakarta** - Migración de paquetes javax a jakarta para Jakarta EE |
| 2025-12-20 | `f2f090a` | Migración de javax.transaction a jakarta.transaction |
| 2025-12-22 | `dde4803` | Limpieza general del proyecto |
| 2025-12-22 | `af394c6` | Agregadas Cursor rules y .cursorringore |
| 2025-12-22 | `cf68433` | Documentación legacy en /docs para migración |

**Cambios clave**:
- Creación de estructura multi-módulo Maven
- Upgrade a Java 21 LTS y Spring Boot 3.2.9
- Preparación para migrar de MySQL a PostgreSQL

---

### Fase 3: Refactoring Principal (Enero-Febrero 2026)

| Fecha | Commit | Descripción |
|-------|--------|-------------|
| 2026-01-31 | `bbb7c43` | **General refactor** - Refactorización general del código |
| 2026-02-05 | `811152c` | **Major refactor to separate layers** - Separación de capas (Controller/Service/Repository) |
| 2026-02-15 | `a5b2c36` | Refactoring y trabajo en carpeta /docs |
| 2026-02-19 | `e9a7c36` | **Creating a migration plan** - Creación formal del plan de migración |
| 2026-02-20 | `93afb51` | Proceso de migración de monolito a módulos Swing |
| 2026-02-20 | `341cbf5` | Migración de formularios por lotes (batches) |
| 2026-02-21 | `dc4c562` | Batch migration Step 1/6 |
| 2026-02-21 | `11b092e` | **Batch B success** - Migración de clientes exitosa |
| 2026-02-22 | `f7da74e` | Testing de la aplicación en ejecución |
| 2026-02-24 | `caf084b` | Mejoras en AGENTS.md |

**Cambios clave**:
- Separación en 3 módulos: `backend-api`, `frontend-swing`, `notaire-shared`
- Creación de REST Controllers para entidades del dominio
- Implementación de Docker Compose con PostgreSQL
- Migración de formularios Swing de ControllerNegocio a REST API

---

### Fase 4: Estado Actual (Febrero 2026)

| Componente | Estado | Detalle |
|------------|--------|---------|
| **Backend API** | ~95% | Endpoints críticos listos; faltan 2-3 para casos específicos |
| **Swing Forms** | ~70% | ~30 formularios aún usan ControllerNegocio |
| **Reportes PDF** | ✅ Listo | ReporteController con 10 endpoints JasperReports |
| **Docker Compose** | ✅ Listo | postgres + backend + pgadmin |
| **Tests E2E** | Parcial | Shell tests; JUnit domain tests |

---

## Proceso de Migración de Base de Datos

### Resumen de la Migración MySQL → PostgreSQL

La migración de la base de datos fue un paso crítico en el proceso de modernización del proyecto. A continuación se documenta el proceso seguido:

| Etapa | Descripción | Estado |
|-------|-------------|--------|
| 1. Export | Exportar datos desde MySQL 5.7 | ✅ Completado |
| 2. Conversión | Convertir tipos MySQL a PostgreSQL | ✅ Completado |
| 3. Schema | Crear 01-schema.sql | ✅ Completado |
| 4. Datos | Crear 02-data.sql | ✅ Completado |
| 5. Validación | Verificar integridad de datos | ✅ Completado |

### Archivos de Migración

```
init-db/
├── 01-schema.sql      # Esquema de tablas PostgreSQL
├── 02-data.sql        # Datos iniciales
└── migrate.load       # Script pgloader (referencia histórica)
```

### Conversión de Tipos MySQL → PostgreSQL

| Tipo MySQL | Tipo PostgreSQL | Notas |
|------------|----------------|-------|
| INT | INTEGER | Tipo base |
| DATETIME | TIMESTAMP | Fecha y hora |
| AUTO_INCREMENT | SERIAL | Auto-incremento |
| TINYINT(1) | BOOLEAN | Booleanos |
| TEXT | TEXT | Textos largos |
| VARCHAR(n) | VARCHAR(n) | Textos variables |
| BIGINT | BIGINT | Enteros grandes |

### Uso de pgloader (Alternativo)

El archivo `init-db/migrate.load` contiene un script de **pgloader** que puede usarse para migraciones futuras:

```bash
# 1. Instalar pgloader
sudo apt-get install pgloader

# 2. Ejecutar migración
cd init-db/
pgloader migrate.load
```

**Nota**: Este método no se usó activamente. Se prefirió la conversión manual de SQL para mayor control.

### Configuración en Docker

La base de datos PostgreSQL se configura automáticamente al iniciar Docker Compose:

```yaml
# docker-compose.yml
postgres:
  image: postgres:16
  environment:
    POSTGRES_DB: notary
    POSTGRES_USER: admin
    POSTGRES_PASSWORD: admin
  volumes:
    - ./init-db:/docker-entrypoint-initdb.d:ro
```

Los scripts en `init-db/` se ejecutan automáticamente al crear el contenedor.

---

## Estado de la Migración

### Formularios Migrados (Completados)

| Batch | Módulo | Formularios | Estado |
|-------|--------|-------------|--------|
| A | Usuarios | ActividadUsuario, DarAltaUsuario, ListarPersonasUsuario | ✅ Completado |
| B | Clientes | Clientes, BuscarCliente, DarAltaPersona, AdministrarCliente, ListarPersonas | ✅ Completado |

### Formularios Pendientes (Por Migrar)

| Prioridad | Módulo | Formularios |
|-----------|--------|-------------|
| 1 | Gestiones | BuscarGestion, ListaGestionesCliente, ModificarGestion, DetalleGestion |
| 2 | Escrituras | BuscarEscritura, ListaEscrituras, DetalleEscritura |
| 3 | Testimonios | GenerarTestimonio, VerificarTestimonio, RetirarTestimonio |
| 4 | Inscripciones | IngresarParaInscripcion, RegistrarInscripcion, RegistrarReingreso |
| 5 | Presupuestos | CrearPresupuesto, DetalleValoresTramites, BuscarInmueble |
| 6 | Protocolo | ModificarFolio, IngresarFolios |

### APIs Disponibles

Se han implementado los siguientes endpoints REST:

- `/api/v1/personas` - Personas
- `/api/v1/usuarios` - Usuarios
- `/api/v1/gestiones` - Gestiones
- `/api/v1/tramites` - Trámites
- `/api/v1/escrituras` - Escrituras
- `/api/v1/presupuestos` - Presupuestos
- `/api/v1/items` - Items
- `/api/v1/pagos` - Pagos
- `/api/v1/folios` - Folios
- `/api/v1/testimonios` - Testimonios
- `/api/v1/reportes` - Reportes PDF (10 endpoints)
- Y más...

---

## Guía de Prompts para Refactoring

A continuación se documentan los prompts utilizados para llevar adelante cada fase de la migración.

### 2.1 Prompts para Configuración Inicial

```markdown
# Prompt: Actualizar proyecto Java a última versión LTS

El proyecto Notaire es una aplicación Java monolitica. Necesito:
1. Upgrade de Java 8 a Java 21 LTS
2. Upgrade de Spring Boot 2.7 a 3.2.9
3. Migrar paquetes javax a jakarta (Jakarta EE)
4. Actualizar todas las dependencias obsoletas

Pasos a seguir:
- Actualizar pom.xml con las nuevas versiones
- Cambiar imports de javax.* a jakarta.*
- Verificar compatibilidad de librerias
- Ejecutar tests de integracion
```

### 2.2 Prompts para Separación de Módulos

```markdown
# Prompt: Crear estructura multi-modulo Maven

Crear una estructura multi-modulo Maven para refactorizar un monolito Java:

Modulos requeridos:
1. **notaire-shared**: DTOs y codigo comun compartido
2. **backend-api**: API REST con Spring Boot (Java 21)
3. **frontend-swing**: Cliente GUI Swing (sin dependencias de base de datos)

Requisitos:
- Cada modulo debe tener su propio pom.xml
- Dependencias entre modulos configuradas correctamente
- Build con mvn clean install debe funcionar
```

### 2.3 Prompts para Dockerización

```markdown
# Prompt: Crear Docker Compose para la aplicacion

Crear docker-compose.yml con:
1. PostgreSQL 16 como base de datos
2. Backend API como servicio
3. pgAdmin para administracion de DB
4. Variables de entorno en .env
5. Health checks para cada servicio
6. Red compartida entre servicios

Consideraciones:
- Usar postgres:16-alpine
- Configurar inicializacion de DB desde scripts
- Exponer puertos: 5432 (postgres), 8080 (backend), 5050 (pgadmin)
```

### 2.4 Prompts para Creación de REST API

```markdown
# Prompt: Crear REST Controller para entidad

Crear un REST Controller completo para la entidad {Entidad} siguiendo las mejores practicas:

Estructura requerida:
- @RestController con @RequestMapping("/api/v1/{recurso}")
- Constructor injection para el service
- Metodos: GET (listar/buscar), POST (crear), PUT (actualizar), DELETE (eliminar)
- DTOsRequest y DTOsResponse para todas las operaciones
- Validacion con javax.validation (@NotNull, @NotBlank, etc.)
- Manejo de excepciones con @ControllerAdvice
- Documentacion con @Operation y @ApiResponse
```

### 2.5 Prompts para Migración de Formularios Swing

```markdown
# Prompt: Migrar formulario Swing de ControllerNegocio a REST

Migrar el formulario {NombreFormulario}.java de usar ControllerNegocio a usar REST API:

Pasos:
1. Identificar todas las llamadas a ControllerNegocio.* en el formulario
2. Mapear cada llamada a un endpoint REST existente o crearlo
3. Usar AdministradorJpa.getInstancia().getXxxJpa() y GenericRestClient
4. Usar RestMapper para mapeos de datos
5. Reemplazar ControllerNegocio por codigo REST
6. Probar con la aplicacion en ejecucion
```

### 2.6 Prompts para Refactoring de Arquitectura

```markdown
# Prompt: Aplicar reglas de codigo limpio

Aplicar las siguientes reglas de refactoring al codigo:

1. **SOLID Principles**: SRP, OCP, LSP, ISP, DIP
2. **Naming Conventions**:
   - Variables/metodos: camelCase (isLoading, hasError)
   - Clases: PascalCase
   - Constantes: UPPER_SNAKE_CASE
3. **Code Structure**:
   - Max 20-30 lineas por metodo
   - Max 3-4 parametros por metodo
   - Usar Optional<T> para retornos nullable
   - Retornar Collections.emptyList() en vez de null
4. **Imports**: Sin wildcards, orden: java, javax, third-party, own packages
```

---

## Pasos Siguientes (Futuro)

### Validación Funcional

- [ ] Completar migración del 30% de formularios restantes
- [ ] Validar que todos los formularios funcionen correctamente con la API REST
- [ ] Probar flujos completos de negocio (cliente → gestión → presupuesto → pago)
- [ ] Verificar generación de reportes PDF
- [ ] Testing manual de casos edge

### Mejoras de Backend

- [ ] Implementar autenticación JWT/OAuth2
- [ ] Agregar rate limiting
- [ ] Implementar cache con Redis
- [ ] Completar el 5% de APIs restantes
- [ ] Agregar paginación a todos los endpoints

### Testing y Calidad

- [ ] Coverage de tests al 80% mínimo
- [ ] Tests de integración con Testcontainers
- [ ] Tests E2E automatizados
- [ ] Configurar pipeline CI/CD
- [ ] Análisis estático de código (SonarQube)

### Despliegue

- [ ] Configurar Kubernetes (K8s)
- [ ] Implementar Docker multi-stage build
- [ ] Configurar HTTPS/TLS
- [ ] Setup de monitoreo (Prometheus, Grafana)
- [ ] Backup automatizado de PostgreSQL

### Documentación

- [ ] Documentar todos los endpoints en Swagger
- [ ] Crear guía de instalación para producción
- [ ] Documentar arquitectura del sistema
- [ ] Crear manual de usuario

---

## Comandos de Uso

### Build y Ejecución

```bash
# Compilar todo el proyecto
mvn clean install

# Compilar un modulo especifico
mvn clean install -pl backend-api

# Ejecutar backend API
cd backend-api && mvn spring-boot:run

# Iniciar aplicacion (Docker Compose)
bash scripts/start.sh

# Detener aplicacion
bash scripts/stop.sh
```

### Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un modulo
mvn test -pl backend-api

# Ejecutar una clase de test
mvn test -Dtest=DocumentServiceTest

# Ejecutar un metodo de test
mvn test -Dtest=DocumentServiceTest#shouldCreateDocument

# Tests de integracion HTTP
bash scripts/test.sh
```

### API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Documentacion Swagger
curl http://localhost:8080/swagger-ui.html

# Ejemplo de consulta
curl http://localhost:8080/api/v1/personas
```

---

## Resumen de Comandos Utilizados

```bash
# Build
mvn clean install
mvn clean install -pl backend-api

# Testing
mvn test
mvn test -Dtest=ClassName#methodName

# Docker
bash scripts/start.sh
bash scripts/stop.sh
bash scripts/test.sh

# API
curl http://localhost:8080/api/v1/personas
```

---

## stack Tecnológico

| Componente | Original | Actual |
|------------|----------|--------|
| Java | 8 | 21 LTS |
| Framework | N/A | Spring Boot 3.2.9 |
| Base de Datos | MySQL 5.7 | PostgreSQL 16 |
| GUI | Swing (Monolito) | Swing + REST Client |
| API | No existe | REST API |
| Contenedores | No | Docker + Docker Compose |
| Pool de Conexiones | No | HikariCP |

---

*Ultima actualizacion: 25 de Febrero de 2026*

*Proyecto en proceso de migracion - Arquitectura en evolucion*
