# CU65 – Buscar Tipos de documentos

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU65 – Buscar Tipos de documentos |
| **Actores** | Escribano/Gestor/Recepcionista |
| **Propósito** | Busca la lista de todos los tipos de documentos registrados. |
| **Descripción** | El Escribano/Gestor/Recepcionista solicita un tipo de documento en particular. El sistema busca y devuelve una lista de todos los tipos de documentos registrados. El Escribano/Gestor/Recepcionista selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: El nombre del tipo de documento, si se vence o no, los días de valides y qué entidad hace entrega del mismo (cliente o entidad externa). |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #57 (Ingresar nuevos documentos) |
| **GitHub ID** | #218 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Gestor/Recepcionista solicita un tipo de documento. |  |
| 2 |  | El sistema busca y devuelve una lista de todos los tipos de documentos registrados. |
| 3 | El Escribano/Gestor/Recepcionista selecciona un tipo de documento. |  |
| 4 |  | Muestra el detalle del tipo de documento, donde se indica: (El nombre del tipo de documento; si se vence o no; los días de valides; Qué entidad hace entrega del mismo (cliente o entidad externa).) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de documentos registrados. | El sistema gestiona la excepción y notifica al usuario. |
