# CU36 – Ingresar tipos de folio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU36 – Ingresar tipos de folio |
| **Actores** | Escribano |
| **Propósito** | Permite dar de alta un nuevo tipo de folio. |
| **Descripción** | El Escribano decide dar de alta un tipo de folio. El sistema solicita que se indique el nombre y/o observaciones para el nuevo tipo de folio. El escribano ingresa los datos indicados y el sistema registra el nuevo tipo de folio. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #94 (Administrar folios), RF #119 (Diferencias entre protocolo principal y auxiliar) |
| **GitHub ID** | #189 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide agregar un nuevo tipo de folio. |  |
| 2 |  | Solicita que se indique el nombre del nuevo tipo de folio y/o observaciones para el mismo. |
| 3 | El escribano ingresa los datos indicados y guarda los cambios. |  |
| 4 |  | Registra el nuevo tipo de folio. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | El tipo de folio indicado ya se encuentra registrado, pero ha sido deshabilitado, por lo tanto, se procede a habilitarlo nuevamente. | El sistema gestiona la excepción y notifica al usuario. |
