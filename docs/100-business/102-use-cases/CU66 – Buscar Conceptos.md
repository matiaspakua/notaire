# CU66 – Buscar Conceptos

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU66 – Buscar Conceptos |
| **Actores** | Escribano |
| **Propósito** | Busca la lista de conceptos registrados. |
| **Descripción** | El escribano solicita la lista de conceptos registrados. El sistema busca y devuelve una lista con todos los conceptos registrados. El escribano selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: el nombre, el valor, el porcentaje, y si se trata de un concepto con valor fijo o no. |
| **Tipo** | Secundario. |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #60 (Ingresar nuevos conceptos) |
| **GitHub ID** | #219 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano solicita la lista de conceptos registrados. |  |
| 2 |  | Busca y devuelve una lista con todos los conceptos registrados. |
| 3 | Selecciona uno de los conceptos. |  |
| 4 |  | Muestra el detalle del concepto seleccionado: (el nombre; el valor; el porcentaje; si se trata de un concepto con valor fijo o no.) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen conceptos registrados. | El sistema gestiona la excepción y notifica al usuario. |
