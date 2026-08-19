# CU75 – Gestión de Base de Datos y Migraciones (Database Management and Migrations)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU75 – Gestión de Base de Datos y Migraciones (Database Management and Migrations) |
| **Actores** | Administrador de Base de Datos, Equipo DevOps, Equipo de Desarrollo |
| **Propósito** | Gestionar el versionado del esquema de base de datos, migraciones estructurales con Flyway, integridad referencial y procedimientos de respaldo y recuperación para la persistencia notarial. |
| **Descripción** | Asegura que la evolución del modelo de datos de la escribanía se realice de manera determinista, segura y reproducible, garantizando la integridad de datos de clientes, escrituras, folios y trámites. |
| **Tipo** | Soporte / Arquitectura |
| **Referencias Cruzadas** | RF #85 (Acceso a la base de datos), RF #86 (Java VM), RF #87 (Sistema operativo), RF #92 (Lenguaje de programación), RF #93 (Motor de base de datos) |
| **GitHub ID** | #264, #275, #292, #271, #270 |

## Alcance Técnico

- Versionado secuencial e inmutable del esquema con scripts Flyway (V1..V11+).
- Restricción de acceso directo a la base de datos exclusivamente a cuentas administrativas seguras.
- Documentación de diagramas entidad-relación (ERD) y diccionarios de datos.
- Políticas de backup periódico y procedimientos de recuperación ante desastres (DRP) con objetivos RTO/RPO.

## Procedimiento de Migración y Mantenimiento

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El desarrollador o DBA crea un script de migración Flyway versionado (ej. `V12__add_index.sql`). | Valida la sintaxis del script y el checksum de migraciones previas. |
| 2 | La aplicación inicia en el entorno de ejecución Java. | Flyway aplica automáticamente las migraciones pendientes en una transacción única. |
| 3 | El sistema verifica la integridad referencial y las restricciones de negocio. | Registra la versión aplicada en la tabla `flyway_schema_history` y habilita el arranque de la API. |
| 4 | El DBA ejecuta rutinas de mantenimiento (vacuum, reindex, backups). | Genera respaldos encriptados y reporta el estado de salud de la base de datos. |

## Criterios de Aceptación

- [x] Esquema de base de datos versionado mediante migraciones Flyway reproducibles.
- [x] Restricción de acceso directo a base de datos implementada (solo credenciales autorizadas).
- [x] Procedimientos de respaldo automatizados y plan de recuperación ante desastres definido.
- [x] Compatibilidad verificada con Java 21 y PostgreSQL 16.
