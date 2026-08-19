# CU53 – Modificar Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU53 – Modificar Gestión |
| **Actores** | Recepcionista/Gestor, Cliente |
| **Propósito** | Modifica una gestión en particular. |
| **Descripción** | El Gestor/Recepcionista decide modificar una gestión en particular. Busca la gestión deseada. El sistema muestra los trámites asociados a cada presupuesto, el encabezado de la gestión, las observaciones, el escribano a cargo, el cliente de referencia y los clientes asociados a la gestión. El Gestor/Recepcionista procede a modificar alguno de los datos de la gestión encontrada: El encabezado, las observaciones o los clientes asociados. Finalmente, confirma los cambios realizados. |
| **Tipo** | Primario y esencial. |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #13 (Registrar inicio de gestión de trámites), RF #104 (Administrar carpetas de trámite), RF #106 (Estados de carpeta), RF #110 (Registrar historial de cambios de estado), RF #115 (Asignar suplente a una gestión) |
| **GitHub ID** | #206 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor/Recepcionista decide modificar una gestión en particular. |  |
| 2 |  | Solicita número de gestión, o tipo y número de identificación, o nombre y apellido del cliente asociado a la gestión, para realizar la búsqueda de la misma. |
| 3 | El Gestor/Recepcionista ingresa alguno de los datos solicitados. |  |
| 4 |  | Busca y muestra los datos de la gestión encontrada: (Nombre cliente asociado; Presupuestos asociados; Fecha de inicio; Escribano a cargo; Clientes asociados) |
| 5 | El Gestor/Recepcionista puede modificar: (Modificar el detalle del encabezado,; Modificar el detalle de las observaciones,; Agregar o quitar Clientes asociados) Y confirma los cambios realizados. |  |
| 6 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La gestión no existe. | El sistema gestiona la excepción y notifica al usuario. |
| 4.2 | La gestión indicada ya se encuentra archivada, por lo tanto, no puede ser modificada. | El sistema gestiona la excepción y notifica al usuario. |
| 5.1 | No se encuentran registrados más presupuestos para el cliente indicado. | El sistema gestiona la excepción y notifica al usuario. |
| 5.1 | La gestión no puede no tener presupuestos asociados. | El sistema gestiona la excepción y notifica al usuario. |
| 5.1 | La gestión no puede no tener clientes asociados. | El sistema gestiona la excepción y notifica al usuario. |
