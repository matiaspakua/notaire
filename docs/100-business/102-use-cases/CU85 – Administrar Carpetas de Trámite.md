# CU85 – Administrar Carpetas de Trámite

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU85 – Administrar Carpetas de Trámite |
| **Actores** | Recepcionista/Gestor, Escribano |
| **Propósito** | Generar la carpeta física/lógica que agrupa la documentación de un trámite y gestionar su ciclo de vida (activa, en espera, archivada). |
| **Descripción** | Cuando un cliente solicita un trámite y se inicia una gestión (ver CU02), el sistema genera automáticamente una carpeta de trámite con número único, que agrupa la documentación asociada. La carpeta transita por los estados activa, espera y archivada a medida que avanza el trámite, y puede consultarse en cualquier momento para saber en qué estado se encuentra la documentación física/lógica de un trámite. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #104 (Administrar carpetas de trámite), RF #105 (Generar carpeta de trámite), RF #106 (Estados de carpeta); CU02, CU16 |
| **GitHub ID** | _pendiente — se completa al crear el issue de implementación_ |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Recepcionista/Gestor inicia una gestión para un trámite solicitado por un cliente (CU02). | |
| 2 | | Genera automáticamente una carpeta de trámite con número único, vinculada a la gestión y al/los trámite(s) que agrupa, en estado "Activa". |
| 3 | El Recepcionista/Gestor o Escribano consulta el estado de la carpeta de un trámite. | Muestra número de carpeta, estado actual, gestión y trámite(s) asociados. |
| 4 | El Recepcionista/Gestor pone la carpeta en espera (por ejemplo, mientras se aguarda documentación del cliente). | Cambia el estado de la carpeta a "Espera" y registra el motivo. |
| 5 | El Escribano archiva la gestión asociada (CU16). | Cambia el estado de la carpeta a "Archivada". |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | La gestión agrupa más de un trámite | El sistema genera una carpeta por trámite, cada una con su propio número único, todas vinculadas a la misma gestión. |
| 5.1 | Se intenta archivar una carpeta que sigue en estado "Espera" sin motivo resuelto | El sistema alerta y solicita confirmación explícita antes de archivar. |
