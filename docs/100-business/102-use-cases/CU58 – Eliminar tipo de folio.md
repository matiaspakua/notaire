# CU58 – Eliminar tipo de folio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU58 – Eliminar tipo de folio |
| **Actores** | Escribano |
| **Propósito** | Permite dar de baja/deshabilitar un tipo de folio. |
| **Descripción** | El escribano decide dar de baja un tipo de folio existente, para lo cual solicita al sistema la lista de tipos de folios registrados. El sistema presenta la lista de todos los tipos de folios existentes. El escribano selecciona uno de ellos e indica que se deshabilite (dar de baja) y el sistema guarda los cambios indicados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #94 (Administrar folios), RF #119 (Diferencias entre protocolo principal y auxiliar); CU68 |
| **GitHub ID** | #211 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El escribano decide deshabilitar / dar de baja un tipo de folio y solicita al sistema la lista de folios disponibles. |  |
| 2 |  | Busca y presenta una lista de todos los tipos de folios registrados. |
| 3 | El escribano selecciona un tipo de folio y guarda los cambios. |  |
| 4 |  | Procede a cambiar el estado del tipo de folio a “deshabilitado” y guarda los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de folios registrados. | El sistema gestiona la excepción y notifica al usuario. |
