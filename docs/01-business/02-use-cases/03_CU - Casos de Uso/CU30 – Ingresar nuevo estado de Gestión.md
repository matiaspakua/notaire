# CU30 – Ingresar nuevo estado de Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU30 – Ingresar nuevo estado de Gestión |
| **Actores** | Escribano |
| **Propósito** | Da de alta un nuevo estado de gestión. |
| **Descripción** | El Escribano decide dar de alta un nuevo estado, para ser utilizado en el proceso de las gestiones de escrituras. El sistema solicita los datos necesarios. El escribano ingresa los datos solicitados y guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #59 (Ingresar nuevos estados), RF #107 (Estados y transiciones del trámite), RF #108 (Definir estados del trámite), RF #109 (Definir transiciones válidas de estado) |
| **GitHub ID** | #183 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide dar de alta un nuevo estado de gestión. |  |
| 2 |  | Solicita que se ingrese el nombre del estado y observaciones. |
| 3 | El Escribano indica el nombre del nuevo estado, observaciones, y lo confirma. |  |
| 4 |  | Registra el nuevo estado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | El estado ya existe. | El sistema gestiona la excepción y notifica al usuario. |
