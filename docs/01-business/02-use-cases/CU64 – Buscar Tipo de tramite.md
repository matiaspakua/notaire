# CU64 – Buscar Tipo de trámite

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU64 – Buscar Tipo de trámite |
| **Actores** | Escribano/Gestor/Recepcionista |
| **Propósito** | Busca la lista de tipo de trámites disponibles. |
| **Descripción** | El Escribano/Gestor/Recepcionista requiere utilizar un tipo de trámite, para lo cual solicita al sistema dicha lista. El sistema busca y devuelve una lista de todos los tipos de trámites registrados. El Escribano/Gestor/Recepcionista selecciona un tipo de trámite y el sistema muestra el detalle del mismo, en el cual se indica: El nombre del tipo de trámite, si se archiva o no, si se inscribe o no, si tiene asociado inmueble y las observaciones. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #56 (Ingresar nuevos trámites) |
| **GitHub ID** | #217 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Gestor/Recepcionista requiere utilizar un tipo de trámite. |  |
| 2 |  | Busca y presenta la lista de todos los tipos de trámites registrados. |
| 3 | El Escribano/Gestor/Recepcionista selecciona un tipo de trámite. |  |
| 4 |  | Muestra el detalle del tipo de trámite, donde indica: (El nombre del tipo de trámite; si se archiva o no; si se inscribe o no; si tiene asociado inmueble; las observaciones.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | no existen tipo de trámites registrados. | El sistema gestiona la excepción y notifica al usuario. |
