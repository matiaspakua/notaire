# CU40 – Modificar tipo de folio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU40 – Modificar tipo de folio |
| **Actores** | Escribano |
| **Propósito** | Permite modificar un tipo de folio. |
| **Descripción** | El escribano decide modificar el nombre y/o observaciones de un tipo de folio existente, para lo cual solicita al sistema la lista de todos los tipos de folios registrados. El sistema presenta la lista de folios disponibles, el escribano selecciona uno de ellos y cambia el nombre y/o observaciones del mismo. Luego el sistema registra los cambios indicados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #94 (Administrar folios), RF #119 (Diferencias entre protocolo principal y auxiliar); CU68 |
| **GitHub ID** | #193 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El escribano decide cambiar el nombre y/o observaciones de un tipo de folios. |  |
| 2 |  | Presenta una lista de todos los tipos de folios registrados. |
| 3 | El escribano selecciona uno de ellos y modifica el nombre y/o observaciones, y guarda los cambios realizados. |  |
| 4 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipo de folios registrados. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | Alguno de los datos indicados no es válido. | El sistema gestiona la excepción y notifica al usuario. |
