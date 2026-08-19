# Lista de Actores

Actores identificados en los 82 casos de uso del sistema (`docs/100-business/102-use-cases/`). Un caso de uso puede listar más de un actor (p. ej. "Escribano/Gestor/Recepcionista") cuando la acción puede ser ejecutada por cualquiera de esos roles indistintamente; esas combinaciones no son actores nuevos, son composiciones de los actores atómicos listados abajo.

## Actores de negocio

| Actor | Descripción |
|-------|-------------|
| **Persona** | Alguien que se acerca a la escribanía a solicitar información o un presupuesto. No es todavía un Cliente; contiene un conjunto mínimo de datos. |
| **Cliente** | Persona que ha iniciado o ya ha realizado trámites en la escribanía. Contiene la información completa de una persona. |
| **Escribano** | Administra los trámites, redacta y firma (autoriza) escrituras y actas. Administra folios. |
| **Recepcionista** | Emite información sobre los trámites, genera presupuestos y realiza el ABM básico (clientes, personas, etc.). |
| **Gestor** | Inicia y controla el avance de los trámites. |
| **Administrador** | Gestiona la configuración del sistema, tablas base, plantillas, usuarios, suplencias y la auditoría. Usado en CU48, CU51, CU71, CU73, CU79, entre otros. |

## Actores técnicos / organizacionales

Estos actores aparecen únicamente en los casos de uso de soporte y arquitectura (CU74–CU78), que documentan responsabilidades operativas del sistema en sí (rendimiento, migraciones, calidad, monitoreo, seguridad) más que flujos de negocio con un usuario final.

| Actor | Descripción | Casos de uso |
|-------|-------------|--------------|
| **Administrador del Sistema** | Configura y supervisa aspectos técnicos globales (caché, seguridad). | CU74, CU78 |
| **Administrador de Base de Datos** | Ejecuta y valida migraciones de esquema (Flyway). | CU75 |
| **Equipo de Desarrollo** | Implementa y mantiene las estrategias técnicas (caché, migraciones, seguridad). | CU74, CU75, CU78 |
| **Equipo de Operaciones / DevOps / SRE** | Opera la infraestructura, ejecuta migraciones y responde a incidentes. | CU74, CU75, CU77 |
| **Equipo de QA** | Define y mantiene la infraestructura de pruebas y calidad. | CU76 |
| **Ingenieros de Guardia** | Responden a incidentes de producción (on-call). | CU77 |
| **Oficial de Seguridad** | Vela por el cumplimiento de políticas de seguridad y compliance. | CU78 |
| **Pipeline CI/CD** | Actor de sistema: ejecuta build, tests y despliegues automáticos. | CU76 |

## Notas

- Esta lista se generó por extracción automática del campo `Actores` de los 82 archivos `CU*.md` (ver `docs/100-business/102-use-cases/`) y debe mantenerse sincronizada cuando se agreguen o modifiquen casos de uso.
- El actor **Administrador** no figuraba previamente en esta lista pese a estar referenciado en varios casos de uso de negocio (CU48, CU51, CU71, CU73, CU79); se agregó en esta revisión.
