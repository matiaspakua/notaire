# CU59 – Consultar Suplencias

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU59 – Consultar Suplencias |
| **Actores** | Gestor/recepcionista. |
| **Propósito** | Permite listar las suplencias registradas. |
| **Descripción** | El Gestor/recepcionista requiere una lista de o las suplencias registradas en un año indicado. El sistema busca y presenta una lista de todas las suplencias registradas para el año seleccionado, indicando para suplencia, los siguientes datos: El escribano suplantado, El escribano suplente, La fecha de inicio, La fecha de fin de suplencia, Y las observaciones. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #113 (Administrar suplencias), RF #116 (Historial de suplencias); CU61 |
| **GitHub ID** | #212 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El gestor/recepcionista solicita una lista de todas las suplencias. |  |
| 2 |  | Solicita que se indique el año para el cual buscar todas las suplencias registradas. |
| 3 | El gestor/recepcionista indica el año. |  |
| 4 |  | Busca y retorna una lista de todas las suplencias registrada para el período indicado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen suplencias registradas para el período indicado. | El sistema gestiona la excepción y notifica al usuario. |
