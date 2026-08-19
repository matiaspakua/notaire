# CU32 – Modificar tipo de documento

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU32 – Modificar tipo de documento |
| **Actores** | Escribano |
| **Propósito** | Modifica las características de un documento. |
| **Descripción** | El Escribano decide modificar los datos de un tipo de documento en particular. El sistema presenta una lista de todos los tipos de documento disponibles. El Escribano selecciona uno de ellos, y el sistema los datos del mismo. El Escribano modifica algunos de los datos solicitados y guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #57 (Ingresar nuevos documentos); CU65 |
| **GitHub ID** | #185 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar los datos de un tipo de documento en particular. |  |
| 2 |  | Muestra una lista de todos los tipos de documento disponibles. |
| 3 | Selecciona uno de ellos. |  |
| 4 |  | Presenta: (Nombre del documento; Si posee o no vencimiento; Cantidad de días de validez; Quién lo entrega) |
| 5 | Modifica los datos necesarios y confirma los cambios realizados. |  |
| 6 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No hay documentos disponibles. | El sistema gestiona la excepción y notifica al usuario. |
