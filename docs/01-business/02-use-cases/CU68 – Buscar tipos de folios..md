# CU68 – Buscar tipos de folios.

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU68 – Buscar tipos de folios. |
| **Actores** | Escribano |
| **Propósito** | Busca la lista de tipos de folios registrados. |
| **Descripción** | El escribano solicita la lista de tipos de folios. El sistema busca y devuelve la lista de todos los tipos de folios registrados. El escribano selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: el nombre del tipo de folio y las observaciones. |
| **Tipo** | Secundario. |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #94 (Administrar folios), RF #119 (Diferencias entre protocolo principal y auxiliar) |
| **GitHub ID** | #221 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El escribano solicita la lista de tipos de folios. |  |
| 2 |  | Busca y devuelve la lista de todos los tipos de folios registrados. |
| 3 | Selecciona un tipo de folio. |  |
| 4 |  | Muestra el detalle del tipo de folio seleccionado, donde se indica: (El nombre del tipo de folio; Las observaciones.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | no existen tipos de folios registrados. | El sistema gestiona la excepción y notifica al usuario. |
